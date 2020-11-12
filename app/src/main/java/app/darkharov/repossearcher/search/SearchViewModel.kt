package app.darkharov.repossearcher.search

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import app.darkharov.repossearcher.commons.LoadingState
import app.darkharov.repossearcher.commons.RetrofitBuilderFactory

class SearchViewModel : ViewModel() {

    private lateinit var reposSearch: ReposSearchUseCase

    val state = MutableLiveData<LoadingState<SearchResult>>()

    fun processSearchQuery(query: String) {
        if (newSearchRequestNeeded(query)) {
            startSearch(query)
        }
    }

    private fun newSearchRequestNeeded(newQuery: String) =
        !(::reposSearch.isInitialized)
                || (reposSearch.query != newQuery)
                || (state.value is LoadingState.Fail)

    private fun startSearch(query: String) {

        if (::reposSearch.isInitialized) {
            reposSearch.cancel()
        }

        reposSearch = createReposSearchUseCase(query)
        reposSearch.perform()
    }

    private fun createReposSearchUseCase(query: String) =
        ReposSearchUseCase(
            api = RetrofitBuilderFactory.api("https://api.github.com"),
            query = query,
            pageSize = 15,
            concurrency = 2,
            delayMillis = 700,
            listener = ReposSearchListenerImpl()
        )


    private inner class ReposSearchListenerImpl : ReposSearchUseCase.Listener {

        override fun onNextState(state: LoadingState<SearchResult>) {
            this@SearchViewModel.state.postValue(state)
        }
    }
}
