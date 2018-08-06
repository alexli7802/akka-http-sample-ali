package httpbasics

import akka.util.ByteString
import akka.http.scaladsl.model._
import akka.http.scaladsl.model.HttpMethods._
import akka.http.scaladsl.model.headers._

object HttpClient {

  val homeUri = Uri("/abc") // path ?
  val req1 = HttpRequest(GET, uri = homeUri)
  val req2 = HttpRequest(GET, uri = "/index")
  val req3 = {
    val data = ByteString("abc")
    HttpRequest(POST, uri = "/receive", entity = data)
  }

  val req4 = {
    import headers.BasicHttpCredentials
    import HttpCharsets._
    import HttpProtocols._
    import MediaTypes._
    // -------------------------------------------------------------------------------
    val userData = ByteString("abc")
    val authorization = headers.Authorization(BasicHttpCredentials("user", "pass"))
    HttpRequest(
      PUT,
      uri = "/user",
      entity = HttpEntity(`text/plain` withCharset `UTF-8`, userData),
      headers = List(authorization),
      protocol = `HTTP/1.0`)
  }

  // 'raw request uri'
  val req5 = {
    import headers.`Raw-Request-URI`
    val h = `Raw-Request-URI`("/a/b%2BC")
    HttpRequest(uri = "/ignore", headers = List(h))
  }
}

object HttpServer {
  import StatusCodes._

  val resp1 = HttpResponse(200) // 200: OK

  val resp2 = HttpResponse(NotFound) // 404: NotFound

  val resp3 = HttpResponse(404, entity = "Unfortunately, the resource couldn't be found.")

  val h = headers.Location("http://example.com/other")
  val resp4 = HttpResponse(Found, headers = List(h))

  case class User(name: String, pass: String)
  def parseRequest(): HttpRequest => Option[User] = { req =>
    for {
      Authorization(BasicHttpCredentials(name, pass)) <- req.header[Authorization]
    } yield User(name, pass)
  }
}

object HttpBasics1 {

}
