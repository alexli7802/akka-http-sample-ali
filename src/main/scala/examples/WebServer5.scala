package examples

import akka.actor.{ ActorSystem }
import akka.stream.{ ActorMaterializer }
import akka.http.scaladsl.model.HttpMethods.{ GET }
import akka.http.scaladsl.model._

object WebServer5 {

  implicit val system = ActorSystem("server-system")
  implicit val materializer = ActorMaterializer()
  implicit val executionContext = system.dispatcher

  val requestHandler: HttpRequest => HttpResponse = {
    case HttpRequest(GET, Uri.Path("/"), _, _, _) => {
      HttpResponse(entity = HttpEntity(
        ContentTypes.`text/html(UTF-8)`, "<html><body>Hello world!</body></html>"))
    }
    case HttpRequest(GET, Uri.Path("/ping"), _, _, _) => HttpResponse(entity = "PONG")
    case HttpRequest(GET, Uri.Path("/crash"), _, _, _) => sys.error("BOOM")
    case r: HttpRequest => r.discardEntityBytes(); HttpResponse(404, entity = "Unknown resource!")
  }

  def main(args: Array[String]): Unit = {
    val bindingFuture = akka.http.scaladsl.Http().bindAndHandleSync(requestHandler, "localhost", 8080)
    println(s"""
    Server online at http://localhost:8080
    Press RETURN to stop ...  
    """)

    scala.io.StdIn.readLine()
    bindingFuture.flatMap(_.unbind()).onComplete(_ => system.terminate())
  }
}