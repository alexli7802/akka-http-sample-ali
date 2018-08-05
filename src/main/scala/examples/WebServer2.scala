package examples

import scala.concurrent.Future
import akka.Done
import akka.actor.{ ActorSystem }
import akka.stream.{ ActorMaterializer }
import spray.json.DefaultJsonProtocol._
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.http.scaladsl.server.Directives._

final case class Item(name: String, id: Long)
final case class Order(items: List[Item])

object WebServer2 {

  implicit val system = ActorSystem("server-system")
  implicit val materializer = ActorMaterializer()
  implicit val executionContext = system.dispatcher

  implicit val itemFormat = jsonFormat2(Item)
  implicit val orderFormat = jsonFormat1(Order)

  var orders: List[Item] = Nil

  def route: Route = {
    get {
      pathPrefix("item" / LongNumber) { id =>
        val maybeItem: Future[Option[Item]] = fetchItem(id)

        onSuccess(maybeItem) {
          case Some(item) => complete(item)
          case None => complete(StatusCodes.NotFound)
        }
      }
    } ~
      post {
        path("create-order") {
          entity(as[Order]) { order =>
            val saved: Future[Done] = saveOrder(order)
            onComplete(saved) { done =>
              complete("order created")
            }
          }
        }
      }
  }

  def main(args: Array[String]): Unit = {
    println("============== web-server starts ==============")

    val bindingFuture = akka.http.scaladsl.Http().bindAndHandle(route, "localhost", 8080)
    println(s"""
    Server online at http://localhost:8080
    Press RETURN to stop ...  
    """)

    scala.io.StdIn.readLine()
    bindingFuture
      .flatMap(_.unbind())
      .onComplete(_ => system.terminate())

  }

  private def fetchItem(itemId: Long): Future[Option[Item]] = Future {
    orders.find(o => o.id == itemId)
  }

  private def saveOrder(order: Order): Future[Done] = {
    orders = order match {
      case Order(items) => items ::: orders
      case _ => orders
    }
    Future { Done }
  }
}