package app.darkharov.repossearcher.search

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import androidx.recyclerview.widget.RecyclerView
import app.darkharov.repossearcher.R
import app.darkharov.repossearcher.commons.LoadingState
import app.darkharov.repossearcher.commons.LongIdsAssigner

class SearchActivity : AppCompatActivity() {

    private val viewModel by viewModels<SearchViewModel>()


    private val searchInput by lazy { findViewById<EditText>(R.id.search_input) }
    private val clear by lazy { findViewById<ImageView>(R.id.clear) }

    private val recyclerView by lazy { findViewById<RecyclerView>(R.id.search_result) }
    private val reposAdapter = ReposAdapter()

    private val progressBar by lazy { findViewById<ProgressBar>(R.id.progress_bar) }
    private val message by lazy { findViewById<TextView>(R.id.message) }
    private val tryAgainButton by lazy { findViewById<Button>(R.id.try_again) }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)
        setSupportActionBar(findViewById(R.id.toolbar))
        initViews()
        observeStateChanges()
    }

    private fun initViews() {

        recyclerView.adapter = reposAdapter

        searchInput.addTextChangedListener(
            afterTextChanged = {
                performSearchQuery()
            }
        )

        clear.setOnClickListener { searchInput.text = null }
        tryAgainButton.setOnClickListener { performSearchQuery() }
    }

    private fun performSearchQuery() {
        val query = searchInput.text?.toString().orEmpty().trim()
        viewModel.processSearchQuery(query)
    }

    private fun observeStateChanges() {
        viewModel.state.observe(this) { state ->

            message.text = computeMessage(state)

            updateSearchResults(state)

            progressBar.isVisible = state is LoadingState.Started
            tryAgainButton.isVisible = state is LoadingState.Fail
        }
    }

    private fun computeMessage(state: LoadingState<RepoSearchResult>?) =
        when (state) {
            is LoadingState.Started -> null
            is LoadingState.Fail -> state.errorMessage(this)
            is LoadingState.Completed -> nothingFoundOrNull(state)
            else -> null
        }

    private fun nothingFoundOrNull(state: LoadingState.Completed<RepoSearchResult>) =
        when {
            state.data.nothingFound -> getString(R.string.nothing_found)
            else -> null
        }


    private fun updateSearchResults(state: LoadingState<RepoSearchResult>?) {
        when (state) {
            is LoadingState.Completed -> {
                reposAdapter.list = state.data.repos
                recyclerView.isVisible = true
                recyclerView.scrollToPosition(0)
            }
            is LoadingState.Fail -> {
                reposAdapter.list = emptyList()
                recyclerView.isVisible = false
            }
            else -> Unit
        }
    }


    private inner class ReposAdapter : RecyclerView.Adapter<ReposAdapter.ViewHolder>() {

        var list by LongIdsAssigner.listDelegate<Repo>(
            adapter = this,
            initial = emptyList()
        )

        override fun getItemCount() = list.size

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

            val view = LayoutInflater.from(parent.context).inflate(
                R.layout.item_repo,
                parent,
                false
            )

            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            holder.apply {
                name.text = list[position].name
                stars.text = list[position].stars.toString()
            }
        }


        inner class ViewHolder(v: View) : RecyclerView.ViewHolder(v) {
            val name: TextView = v.findViewById(R.id.name)
            val stars: TextView = v.findViewById(R.id.stars)
        }
    }
}
