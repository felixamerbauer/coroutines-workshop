package project

import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope

suspend fun loadContributorsConcurrent(req: RequestData): List<User> = coroutineScope {
    val service = createGitHubService(req.username, req.password)
    log.info("Loading ${req.org} repos")
    val repos = service.listOrgRepos(req.org).await()
    log.info("${req.org}: loaded ${repos.size} repos")
    val deferred = repos.map { repo ->
        async {
            val users = service.listRepoContributors(req.org, repo.name).await()
            log.info("${repo.name}: loaded ${users.size} contributors")
            users

        }
    }
    val contribs = deferred.awaitAll().flatten().aggregate()
    log.info("Total: ${contribs.size} contributors")
    contribs
}

