package examples

import scala.concurrent.duration._
import scala.concurrent.Future
import akka.actor.{ Actor, ActorLogging, ActorSystem, Props, ActorRef }
import spray.json.DefaultJsonProtocol._
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.stream.{ ActorMaterializer }
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.model.StatusCodes
import akka.util.Timeout
import akka.pattern.ask
/////////////////////////////////////////////////////////////////////////////////////
case class Bid(userId: String, offer: Int)
case object GetBids
case class Bids(bids: List[Bid])

class Auction extends Actor with ActorLogging {
  var bids = List.empty[Bid]

  def receive = {
    case bid @ Bid(userId, offer) => {
      bids = bids :+ bid; log.info(s"Bid complete: $userId, $offer")
    }
    case GetBids => sender() ! Bids(bids)
    case _ => log.info("Invalid message")
  }
}

object WebServer4 {

  implicit val bidFormat = jsonFormat2(Bid)
  implicit val bidsFormat = jsonFormat1(Bids)

  implicit val system = ActorSystem("server-system")
  implicit val materializer = ActorMaterializer()
  implicit val executionContext = system.dispatcher

  val auction = system.actorOf(Props[Auction], "auction")

  def route: Route = {
    path("auction") {
      put {
        parameter("bid".as[Int], "user") { (bid, user) =>
          auction ! Bid(user, bid)
          complete((StatusCodes.Accepted, "bid placed"))
        }
      } ~
        get {
          implicit val timeout: Timeout = 5.seconds
          val bids: Future[Bids] = (auction ? GetBids).mapTo[Bids]
          complete(bids)
        }
    }
  }

  def main(args: Array[String]): Unit = {
    println("==================== WebServer4 starts ==================")

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
}