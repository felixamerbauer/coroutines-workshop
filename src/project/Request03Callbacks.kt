package project

import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

// TODO felix check in detail
fun loadContributorsCallbacks(req: RequestData, callback: (List<User>) -> Unit) {
    val service = createGitHubService(req.username, req.password)
    log.info("Loading ${req.org} repos")
    service.listOrgRepos(req.org).responseCallback { repos ->
        log.info("${req.org}: loaded ${repos.size} repos")
        val all = ArrayList<User>()
        var repoIndex = 0
        // A function to process next repository or be done with it
        fun processNextRepo() {
            if (repoIndex >= repos.size) {
                val contribs = all.aggregate()
                log.info("Total: ${contribs.size} contributors")
                callback(contribs)
                return
            }
            val repo = repos[repoIndex++]
            service.listRepoContributors(req.org, repo.name).responseCallback { users ->
                log.info("${repo.name}: loaded ${users.size} contributors")
                all += users
                processNextRepo()
            }
        }
        // Start processing
        processNextRepo()
    }
}

@Suppress("UNCHECKED_CAST")
fun <T> Call<T>.responseCallback(
        callback: (T) -> Unit,
        noContent: (Response<T>) -> T = { errorResponse(it) }
) {
    enqueue(object : Callback<T> {
        override fun onResponse(call: Call<T>, response: Response<T>) {
            when (response.code()) {
                200 -> callback(response.body() as T) // OK
                204 -> callback(noContent(response)) // NO CONTENT
                else -> errorResponse(response)
            }
        }

        override fun onFailure(call: Call<T>, t: Throwable) {
            log.error("Call failed", t)
        }
    })
}

fun <T> Call<List<T>>.responseCallback(
        callback: (List<T>) -> Unit
) =
        responseCallback(callback, noContent = { emptyList() })
