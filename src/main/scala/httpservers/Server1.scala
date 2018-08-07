package httpservers

import scala.concurrent.Future
import akka.actor.{ ActorSystem }
import akka.stream.{ ActorMaterializer }
import akka.stream.scaladsl.{ Source, Sink }
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.http.scaladsl.model.HttpMethods._
import akka.http.scaladsl.server._
import akka.http.scaladsl.server.Directives._
import akka.stream.scaladsl.Tcp.ServerBinding

object Server1 {

  implicit val system = ActorSystem("server-system")
  implicit val materializer = ActorMaterializer()
  implicit val executionContext = system.dispatcher

  val requestHandler: HttpRequest => HttpResponse = {
    case HttpRequest(GET, Uri.Path("/"), _, _, _) => {
      val msgEntity = HttpEntity(ContentTypes.`text/html(UTF-8)`, "<html><body>Hello world</body></html>")
      HttpResponse(entity = msgEntity)
    }
    case HttpRequest(GET, Uri.Path("/ping"), _, _, _) => {
      HttpResponse(entity = "pong")
    }
    case HttpRequest(GET, Uri.Path("/crash"), _, _, _) => sys.error("BOOM")
    case req: HttpRequest => req.discardEntityBytes(); HttpResponse(404, entity = "Unknown resource")
  }

  def main1(args: Array[String]): Unit = {
    println("================ Server1 starts ================")

    val bindingSrc: Source[Http.IncomingConnection, Future[Http.ServerBinding]] =
      Http().bind(interface = "localhost", port = 8080)

    val bindingFut: Future[Http.ServerBinding] =
      bindingSrc.to(Sink.foreach(conn => {
        println("new connection from: " + conn.remoteAddress)
        conn.handleWithSyncHandler(requestHandler)
      })).run()
  }

  def main(args: Array[String]): Unit = {
    println("================ Server1 starts ================")

    // http server binding
    val route: Route = {
      path("") {
        get {
          complete(HttpEntity(ContentTypes.`text/html(UTF-8)`, "<html><h>YEAH!</h></html>"))
        }
      }
    }
    val bindingFuture: Future[Http.ServerBinding] =
      Http().bindAndHandle(handler = route, interface = "localhost", port = 8080)

    // listen to 'StdIn' for termination
    scala.io.StdIn.readLine()
    bindingFuture.flatMap(_.unbind()).onComplete(_ => system.terminate())
  }

}