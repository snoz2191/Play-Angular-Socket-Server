package controllers

import akka.actor._
import akka.actor.FSM.Failure
import akka.actor.Status.Success
import play.api.mvc._
import reactivemongo.core.commands.{GroupField, SumValue, Match, Aggregate}
import scala.concurrent.ExecutionContext.Implicits.global
import models.User
import models.User._
import models.User.UserBSONReader
import models.User.UserBSONReader
import play.api.libs.json.{JsValue, Json}
import play.api.mvc.Action
import play.api.mvc.Controller
import play.modules.reactivemongo.MongoController
import reactivemongo.api.collections.default.BSONCollection
import reactivemongo.bson.BSONDocument
import reactivemongo.bson.BSONNumberLike
import reactivemongo.bson.BSONDocumentIdentity
import reactivemongo.bson.BSONObjectID
import reactivemongo.bson.BSONObjectIDIdentity
import reactivemongo.bson.BSONStringHandler
import reactivemongo.bson.BSONArray
import reactivemongo.bson.Producer.nameValue2Producer
import models.Name.NameBSONWriter
import scala.concurrent.Future
import play.api.Play.current
import java.net._
import java.io._
import scala.io._

/**
 * Created by domingo on 08/08/14.
 */
object Users extends Controller with MongoController {

  val collection = db.collection[BSONCollection]("tweets")

      def index () = Action.async {
        val query = Aggregate(collection.name,
          Seq(
            Match(BSONDocument("user" -> BSONDocument("$ne" -> ""))),
            GroupField("user")("score" -> SumValue(1))
          )
        )
        val cursor = collection.db.command(query) // get all the fields of all the tweets
        cursor.map {
         s =>  Ok(Json.toJson(s.toList.map{
           a => a.get("_id").get
             User(a.getAs[String]("_id").get, a.getAs[Int]("score").get)
         }))
        }
      }

  def socket = WebSocket.acceptWithActor[String, String] { request => out =>
    MyWebSocketActor.props(out)
  }

  object MyWebSocketActor {
    def props(out: ActorRef) = Props(new MyWebSocketActor(out))
  }

  class MyWebSocketActor(out: ActorRef) extends Actor {

    val s = new Socket(InetAddress.getByName("localhost"), 9999)
    lazy val socketIn = new BufferedSource(s.getInputStream()).getLines()
    val socketOut = new PrintStream(s.getOutputStream())

    def receive = {
      case msg: String =>
        socketOut.println(msg)
        socketOut.flush()
        out ! (socketIn.next())
    }

    override def postStop() = {
      //self ! PoisonPill
      s.close()
    }
  }

}


