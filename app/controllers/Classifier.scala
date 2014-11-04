package controllers

import java.io._
import java.net._
import akka.actor._
import play.api.mvc._
import scala.io._
import play.api.Play.current

/**
 * Created by domingo on 03/11/14.
 */
object Classifier {
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
