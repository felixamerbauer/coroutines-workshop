package project

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.withContext

fun loadContributorsFlowConcurrent(req: RequestData) = flow<List<User>> {
    val service = createGitHubServiceCoroutine(req.username, req.password)
    log.info("Loading ${req.org} repos")
    val repos = service.listOrgRepos(req.org).await()
    log.info("${req.org}: loaded ${repos.size} repos")
    // define resulting flow
    val resultFlow: Flow<List<User>> = repos.asFlow()
            .concurrentMapMerge(4) { repo -> service.listRepoContributors(req.org, repo.name).await() }
            .scan(emptyList<User>()) { contribs, users ->
                (contribs + users).aggregateSlow()
            }
    // emit all results
    emitAll(resultFlow)
}

suspend fun createGitHubServiceCoroutine(username: String, password: String): GitHubService =
        withContext(Dispatchers.IO) {
            createGitHubService(username, password)
        }

fun <T, R> Flow<T>.concurrentMapMerge(concurrency: Int, block: suspend (T) -> R): Flow<R> =
        flatMapMerge(concurrency) { value ->
            flow { emit(block(value)) }
        }
