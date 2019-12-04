package project

import kotlinx.coroutines.suspendCancellableCoroutine
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

//@Suppress("UNCHECKED_CAST")
//suspend fun <T> Call<T>.await(
//    noContent: (Response<T>) -> T = { errorResponse(it) }
//): T = suspendCoroutine { cont ->
//    enqueue(object : Callback<T> {
//        override fun onResponse(call: Call<T>, response: Response<T>) {
//            when (response.code()) {
//                200 -> cont.resume(response.body() as T) // OK
//                204 -> cont.resumeWith(runCatching { noContent(response) }) // NO CONTENT
//                else -> cont.resumeWithException(ErrorResponse(response))
//            }
//        }
//
//        override fun onFailure(call: Call<T>, t: Throwable) {
//            cont.resumeWithException(t)
//        }
//    })
//}

@Suppress("UNCHECKED_CAST")
suspend fun <T> Call<T>.await(
        noContent: (Response<T>) -> T = { errorResponse(it) }
): T = suspendCancellableCoroutine { cont ->
    enqueue(object : Callback<T> {
        override fun onResponse(call: Call<T>, response: Response<T>) {
            when (response.code()) {
                200 -> cont.resume(response.body() as T) // OK
                204 -> cont.resumeWith(runCatching { noContent(response) }) // NO CONTENT
                else -> cont.resumeWithException(ErrorResponse(response))
            }
        }

        override fun onFailure(call: Call<T>, t: Throwable) {
            cont.resumeWithException(t)
        }
    })
    cont.invokeOnCancellation { cancel() }
}

suspend fun <T> Call<List<T>>.await(): List<T> =
        await(noContent = { emptyList() })

