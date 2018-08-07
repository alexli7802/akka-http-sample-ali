package httpservers

import akka.http.scaladsl.unmarshalling.{FromRequestUnmarshaller}
import akka.http.scaladsl.marshalling.{ToResponseMarshaller}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.{Route}
import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.coding.Deflate
import akka.actor.{ActorSystem,ActorRef}
import akka.util.Timeout
import akka.pattern.ask
import akka.stream.ActorMaterializer

trait ServerEnv {
  type Money = Double
  type TransactionResult = String
  
  case class User(name: String)
  case class Order(email: String, amount: Money)
  case class Update(order: Order)
  case class OrderItem(i: Int, os: Option[String], s: String)
  
  // unmarshall request entity
  implicit val orderUM: FromRequestUnmarshaller[Order] = ???

  // marshall response entity
  implicit val orderM: ToResponseMarshaller[Order] = ???
  implicit val orderSeqM: ToResponseMarshaller[Seq[Order]] = ???
  
  //run env.
  implicit val timeout: Timeout = ???
  implicit val sys = ActorSystem("server-system")
  implicit val materializer = ActorMaterializer()
  implicit val executionContext = sys.dispatcher
  
  // backend entry points
  def myAuthenticator: Authenticator[User] = ???
  def retrieveOrdersFromDB: Seq[Order] = ???
  def myDbActor: ActorRef = ???
  def processOrderRequest(id: Int, complete: Order=>Unit): Unit = ???

  val route: Route = {
    path("orders") {
      authenticateBasic(realm="admin area", myAuthenticator) { user =>
        get {
          encodeResponseWith(Deflate) {
            complete { retrieveOrdersFromDB }
          }
        } ~
        post {
          decodeRequest {
            entity(as[Order]) { order => complete { ???} }
          }
        }
      }
    } ~
    pathPrefix("order" / IntNumber) { orderId =>
      pathEnd {
        (put | parameter('method ! "put")) {
          formFields(('email, 'total.as[Money])).as(Order) { order =>
            complete { (myDbActor ? Update(order)).mapTo[TransactionResult] }
          }
        } ~
        get {
          logRequest("GET-ORDER") {
            completeWith(instanceOf[Order]) { completer => processOrderRequest(orderId, completer) }
          }
        }
      } ~
      path("items") {
        get {
          parameters(('size.as[Int], 'color ?, 'dangerous ? "no")).as(OrderItem) { orderItem => ??? }
        }
      }
    } ~
    pathPrefix("documentation") {
      encodeResponse { getFromResourceDirectory("docs") }
    } ~
    path("oldApi" / Remaining) { pathRest => 
      redirect("" + pathRest, MovedPermanently)
    }
  }
}

object Server2 extends ServerEnv {

  def main(args: Array[String]): Unit = {

    println("============== Server2 starts ==============")
    val rent: Money = 2.32
    println("rent=" + rent)
  }
}