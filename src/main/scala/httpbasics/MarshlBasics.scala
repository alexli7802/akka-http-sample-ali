package httpbasics

import scala.concurrent.{ Future, Await }
import scala.concurrent.duration._
import akka.actor.ActorSystem
import akka.http.scaladsl.marshalling.Marshal
import akka.http.scaladsl.model.{ MessageEntity, HttpResponse, HttpRequest, headers, MediaTypes }

object MarshlBasics {

  implicit val system = ActorSystem("test-system")
  import system.dispatcher

  def test1(): Unit = {
    val string = "Yeah"
    val entityFuture: Future[MessageEntity] = Marshal(string).to[MessageEntity]
    val entity = Await.result(entityFuture, 1.second)
    println("entity = " + entity)
    println(entity.contentType)
  }

  def test2(): Unit = {
    val respFuture: Future[HttpResponse] = Marshal(404 -> "Easy,pal").to[HttpResponse]
    val resp = Await.result(respFuture, 1.second)
    println("http-response: \n\t" + resp)
  }

  def test3(): Unit = {
    val h = headers.Accept(MediaTypes.`application/json`)
    val req = HttpRequest(headers = List(h))
    val respFuture = Marshal("Plaintext").toResponseFor(req)
    val resp = Await.result(respFuture, 1.second)
    println(resp)
  }

  def main(args: Array[String]): Unit = {
    println("==================== MarshalBasics starts =======================")

    test3()
    system.terminate()
  }
}