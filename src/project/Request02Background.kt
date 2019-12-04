package project

import kotlin.concurrent.thread

fun loadContributorsBackground(req: RequestData, callback: (List<User>) -> Unit) {
   thread {
      val contribs = loadContributorsBlocking(req)
      callback(contribs)
   }
}
