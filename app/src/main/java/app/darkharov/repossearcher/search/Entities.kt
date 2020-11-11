package app.darkharov.repossearcher.search

import com.google.gson.annotations.SerializedName

data class Repo(
    val id: String,
    val name: String,
    @SerializedName("stargazers_count") val stars: Int,
)


data class ReposSearchResponse(
    val items: List<Repo>
)


data class SearchResult(
    val query: String,
    val repos: List<Repo>
) {
    companion object {
        val EMPTY = SearchResult(
            query = "",
            repos = emptyList()
        )
    }
}
