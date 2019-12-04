package project

import kotlinx.coroutines.newFixedThreadPoolContext
import kotlinx.coroutines.withContext
import org.slf4j.Logger
import org.slf4j.LoggerFactory

val log: Logger = LoggerFactory.getLogger("Contributors")

// todo: write actual aggregation code here
// TODO felix
fun List<User>.aggregate(): List<User> =
        groupingBy { it.login }
                .reduce { login, a, b -> User(login, a.contributions + b.contributions) }
                .values
                .sortedByDescending { it.contributions }

val computation =
        newFixedThreadPoolContext(2, "Computation")

suspend fun List<User>.aggregateSlow(): List<User> = withContext(computation) {
    aggregate()
            .also {
                // Imitate CPU consumption / blocking
                Thread.sleep(1000)
            }
}
