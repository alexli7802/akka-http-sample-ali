package examples

import scala.util.{ Success, Failure }
import scala.concurrent.Future
import akka.actor.{ ActorSystem }
import akka.stream.{ ActorMaterializer }
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._

object WebClient {

  implicit val system = ActorSystem("client-system")
  implicit val materializer = ActorMaterializer()
  implicit val executionContext = system.dispatcher

  val req1 = HttpRequest(uri = "http://akka.io")

  def main(args: Array[String]): Unit = {
    println("=============== WebClient starts ==============")

    val resp: Future[HttpResponse] = Http().singleRequest(req1)
    resp.onComplete {
      case Success(res) => println(res)
      case Failure(_) => sys.error("something wrong")
    }
  }
}