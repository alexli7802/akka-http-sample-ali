package examples

import akka.actor.{ ActorSystem }
import akka.stream.{ ActorMaterializer }
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server._
import akka.http.scaladsl.model._

object SimpleServer {

  implicit val system = ActorSystem("server-system")
  implicit val materializer = ActorMaterializer()
  implicit val executionContext = system.dispatcher

  def routes: Route = {
    path("hello") {
      get {
        complete(HttpEntity(
          ContentTypes.`text/html(UTF-8)`, "<h1>Say hello to akka-http</h1>"))
      }
    }
  }

  def main(args: Array[String]): Unit = {
    val binding = Http().bindAndHandle(routes, "localhost", 8080)
    println(s"""
    Server online at http://localhost:8080
    Press RETURN to stop ...  
    """)

    scala.io.StdIn.readLine()
    binding
      .flatMap(_.unbind())
      .onComplete(_ => system.terminate())

  }
}