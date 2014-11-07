package controllers

import java.io._
import java.net._
import akka.actor._
import models.{ExtendedTweet, Tweet}
import play.api.libs.json.{JsArray, JsObject, Json, JsValue}
import play.api.mvc._
import scala.collection.mutable.ArrayBuffer
import scala.io._
import play.api.Play.current
import models.Tweet._
import com.couchbase.client.CouchbaseClient
import scala.collection.JavaConverters._

/**
 * Created by domingo on 03/11/14.
 */
object Classifier {
  val uri = new URI(s"http://127.0.0.1:8091/pools")
  val client = new CouchbaseClient(List(uri).asJava, "SentimentStats", "")


  def socket = WebSocket.acceptWithActor[JsValue, JsValue] { request => out =>
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
      case msg: JsObject =>
        var tweet = msg.as[Tweet]
        socketOut.print(tweet.text.replace("\n",""))
        socketOut.flush()
        val polarity = socketIn.next()
        val tweetToSend = Json.toJson(ExtendedTweet(tweet,polarity,List("cliente","banco","reclamo","duda")))
        client.set(tweet.id_str,0,tweetToSend.toString())
        out ! tweetToSend
      case msg: JsArray =>
        out ! msg.as[List[Tweet]].foldLeft(JsArray()){(acc,x) =>
          socketOut.print(x.text.replace("\n",""))
          socketOut.flush()
          val polarity = socketIn.next()
          val tweetToSend = Json.toJson(ExtendedTweet(x,polarity,List("cliente","banco","reclamo","duda")))
          client.set(x.id_str,tweetToSend.toString())
          acc.append(tweetToSend.asInstanceOf[JsObject])
        }
    }

    override def postStop() = {
      //self ! PoisonPill
      s.close()
    }
  }
}
