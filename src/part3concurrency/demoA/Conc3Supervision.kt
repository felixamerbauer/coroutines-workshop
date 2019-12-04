package part3concurrency.demoA

import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel

fun main() = runBlocking<Unit> {
    supervisorScope {
        launch {
            delay(500)
            error("job1: something went wrong")
        }
        launch {
            delay(1000)
            println("job2: done")
        }
        delay(2000)
    }
}

fun CoroutineScope.producer(channel: Channel<Int>) = launch {
    for (x in 1..5) {
        delay(500)
        channel.send(x * x)
    }
}
