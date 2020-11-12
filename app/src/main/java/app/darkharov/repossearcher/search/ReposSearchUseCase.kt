package app.darkharov.repossearcher.search

import app.darkharov.repossearcher.commons.LoadingState
import java.lang.Exception

class ReposSearchUseCase(
    private val api: SearchApi,
    val query: String,
    private val pageSize: Int,
    private val concurrency: Int,
    private val delayMillis: Long,
    private val listener: Listener
) {

    @Volatile
    private var finished = false

    @Volatile
    private var completedRequestsCount = 0

    private val searchThreads by lazy { createRequests() }
    private val results = mutableSetOf<Repo>()

    fun perform() {
        Thread {

            when {
                query.isBlank() -> emitEmptyResult()
                else -> startSearch()
            }

        }.start()
    }

    private fun emitEmptyResult() {
        listener.onNextState(
            LoadingState.Completed(
                RepoSearchResult.EMPTY
            )
        )
    }

    private fun startSearch() {
        listener.onNextState(LoadingState.Started())
        Thread.sleep(delayMillis)
        startThreads()
    }

    private fun startThreads() {
        doIfNotFinished {
            searchThreads.forEach { thread ->
                thread.start()
            }
        }
    }

    fun cancel() {
        doIfNotFinished { finished = true }
    }

    private fun createRequests() =
        (0 until concurrency).map { pageIndex ->
            SearchThread(
                api,
                query = query,
                page = pageIndex,
                pageSize = pageSize
            )
        }

    @Synchronized
    private fun processException(e: Throwable) {
        tryToFinish(LoadingState.Fail(e))
    }

    @Synchronized
    private fun processRequestResult(items: List<Repo>) {

        results += items
        completedRequestsCount++

        if (completedRequestsCount == searchThreads.size) {

            val sorted = results.toList().sortedByDescending(Repo::stars)
            val result = RepoSearchResult(query, sorted)

            val state = LoadingState.Completed(result)

            tryToFinish(state)
        }
    }

    private fun tryToFinish(state: LoadingState<RepoSearchResult>) {
        doIfNotFinished {
            listener.onNextState(state)
            finished = true
        }
    }

    private fun doIfNotFinished(block: () -> Unit) {
        if (!finished) {
            synchronized(this) {
                if (!finished) {
                    block()
                }
            }
        }
    }


    private inner class SearchThread(
        val api: SearchApi,
        val query: String,
        val page: Int,
        val pageSize: Int
    ) : Thread() {

        override fun run() {
            try {
                processRequestResult(searchRepos())
            } catch (e: Throwable) {
                processException(e)
            }
        }

        private fun searchRepos(): List<Repo> {

            val response =
                api.searchRepos(query, page = page * pageSize, perPage = pageSize)
                    .execute()

            return response
                .body()
                ?.items
                ?: throw Exception(response.message())
        }
    }


    interface Listener {
        fun onNextState(state: LoadingState<RepoSearchResult>)
    }
}
