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


data class RepoSearchResult(
    val query: String,
    val repos: List<Repo>
) {

    val nothingFound get() = query.isNotBlank() && repos.isEmpty()


    companion object {
        val EMPTY = RepoSearchResult(
            query = "",
            repos = emptyList()
        )
    }
}
