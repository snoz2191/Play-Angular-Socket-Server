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
object Classifier extends Controller {
  //Connecting to the couchbase server and creating the client
  val uri = new URI(s"http://127.0.0.1:8091/pools")
  val client = new CouchbaseClient(List(uri).asJava, "SentimentStats", "")

  /*Function socket
  *   Function that expects a websocket request a creates an actor to handle
  *   that request and manage the websocket
  * */
  def socket = WebSocket.acceptWithActor[JsValue, JsValue] { request => out =>
    WebSocketActor.props(out)
  }

  object WebSocketActor {
    def props(out: ActorRef) = Props(new WebSocketActor(out))
  }

  //Class WebSocketActor that manages the websocket connection
  class WebSocketActor(out: ActorRef) extends Actor {
    //Creating a socket that connect with the polarity classifier
    val s = new Socket(InetAddress.getByName("localhost"), 9999)
    lazy val socketIn = new BufferedSource(s.getInputStream()).getLines()
    val socketOut = new PrintStream(s.getOutputStream())

    //Receive method that gets the message sended by the websocket
    def receive = {
      //In case is a single tweet
      case msg: JsObject =>
        var tweet = msg.as[Tweet]
        //Sending the text of the tweet to the classifier
        socketOut.print(tweet.text.replace("\n",""))
        socketOut.flush()
        //Receiving the polarity information
        val polarity = socketIn.next()
        val tweetToSend = Json.toJson(ExtendedTweet(tweet,polarity,List("cliente","banco","reclamo","duda")))
        //Sending the tweet to the couchbase server for statistical purposes
        client.set(tweet.id_str,0,tweetToSend.toString())
        out ! tweetToSend

      //In case is a array of tweets
      case msg: JsArray =>
        out ! msg.as[List[Tweet]].foldLeft(JsArray()){(acc,x) =>
          //Sending the text of the tweet to the classifier
          socketOut.print(x.text.replace("\n",""))
          socketOut.flush()
          //Receiving the polarity information
          val polarity = socketIn.next()
          val tweetToSend = Json.toJson(ExtendedTweet(x,polarity,List("cliente","banco","reclamo","duda")))
          //Sending the tweet to the couchbase server for statistical purposes
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
