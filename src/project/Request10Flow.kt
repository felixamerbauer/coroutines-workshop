package project

import kotlinx.coroutines.flow.*

fun loadContributorsFlow(req: RequestData) : Flow<List<User>> = flow {
    val service = createGitHubService(req.username, req.password)
    log.info("Loading ${req.org} repos")
    val repos = service.listOrgRepos(req.org).await()
    log.info("${req.org}: loaded ${repos.size} repos")
    var contribs = emptyList<User>()
    for (repo in repos) {
        val users = service.listRepoContributors(req.org, repo.name).await()
        log.info("${repo.name}: loaded ${users.size} contributors")
        contribs = (contribs + users).aggregateSlow()
        emit(contribs)
    }
    log.info("Total: ${contribs.size} contributors")

    TODO()
}
