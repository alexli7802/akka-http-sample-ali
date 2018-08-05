package examples

import scala.util.Random
import akka.util.ByteString
import akka.actor.{ ActorSystem }
import akka.stream.{ ActorMaterializer }
import akka.stream.scaladsl._
import akka.http.scaladsl._
import akka.http.scaladsl.model._
import akka.http.scaladsl.server.{ Route }
import akka.http.scaladsl.server.Directives._

object WebServer3 {

  implicit val system = ActorSystem("server-system")
  implicit val materializer = ActorMaterializer()
  implicit val executionContext = system.dispatcher

  val numbers = Source.fromIterator(() => Iterator.continually(Random.nextInt))

  def route: Route = {
    path("random") {
      get {
        complete(HttpEntity(ContentTypes.`text/plain(UTF-8)`, numbers.map(n => ByteString(s"$n\n"))))
      }
    }
  }

  def main(args: Array[String]): Unit = {
    println("============= WebServer3 starts ===============")

    val binding = Http().bindAndHandle(route, "localhost", 8000)
    println(s"""
    Server online at http://localhost:8080
    Press RETURN to stop ...  
    """)

    scala.io.StdIn.readLine()
    binding.flatMap(_.unbind()).onComplete(_ => system.terminate())
  }
}