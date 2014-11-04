package controllers

import akka.actor.FSM.Failure
import akka.actor.Status.Success
import play.api.mvc._
import reactivemongo.core.commands.{GroupField, SumValue, Match, Aggregate}
import scala.concurrent.ExecutionContext.Implicits.global
import models.Tweet
import models.Tweet._
import models.Tweet.TweetBSONReader
import models.Tweet.TweetBSONWriter
import play.api.libs.json.Json
import play.api.mvc.Action
import play.api.mvc.Controller
import play.modules.reactivemongo.MongoController
import reactivemongo.api.collections.default.BSONCollection
import reactivemongo.bson.BSONDocument
import reactivemongo.bson.BSONDocumentIdentity
import reactivemongo.bson.BSONObjectID
import reactivemongo.bson.BSONObjectIDIdentity
import reactivemongo.bson.BSONStringHandler
import reactivemongo.bson.Producer.nameValue2Producer
import models.Name.NameBSONWriter
import scala.concurrent.Future

/**
 * Created by domingo on 08/08/14.
 */
  object Tweets extends Controller with MongoController {

    val collection = db.collection[BSONCollection]("tweets")

    /** list all tweets */
//    def index (UserUpd: String) = Action.async {
//      val cursor = collection.find(BSONDocument("user" -> ""), BSONDocument()).cursor[Tweet] // get all the fields of all the tweets
//      cursor.collect[List]().map(s => Ok(Json.toJson(s)))// convert it to a JSON and return it
//    }

//    def index () = Action.async {
//      val query = Aggregate(collection.name,
//        Seq(
//          Match(BSONDocument("status" -> BSONDocument("$ne" -> ""))),
//          GroupField("user")("score" -> SumValue(1))
//        )
//      )
//      val cursor = collection.db.command(query) // get all the fields of all the tweets
//      cursor.map {
//       s =>  Ok(Json.toJson(s.toList))
//      }
//    }

    def getTweet (UserUpd: String) = Action.async {
      collection.find(BSONDocument("user" -> "")).one[Tweet].flatMap{ t =>
        t.map{ tweet =>
          val blah = tweet.copy(user = Some.apply(UserUpd.toLowerCase()) )
          collection.update(BSONDocument("_id" -> tweet._id.get),
            blah).map{
            _ =>  Ok(Json.toJson(blah))
          }.recover { case x => x.printStackTrace()
              InternalServerError("Interno") }
        }.getOrElse( Future(BadRequest("Fuck") ) )
      }.recover { case x =>
        x.printStackTrace()
        InternalServerError("Externo") }
    }

    /** create a tweet from the given JSON */
    def create() = Action.async(parse.json) { request =>
      println("Entre a Create")
      val tweet = Tweet(Option(BSONObjectID.generate), "Wigan bla bla", 0, 0, 0,  Some("")) // create the tweet
      collection.insert(tweet).map{
                _ => println(tweet)
                  Ok(Json.toJson(tweet)) // return the created tweet in a JSON
      }.recover { case _ => println("FuckCreate")
        InternalServerError }
    }

//
//    /** retrieve the tweet for the given id as JSON */
//    def show(id: String) = Action.async(parse.empty) {
//      val objectID = new BSONObjectID(id) // get the corresponding BSONObjectID
//      // get the tweet having this id (there will be 0 or 1 result)
//      val futureTweet = collection.find(BSONDocument("_id" -> objectID)).one[Tweet]
//      futureTweet.map {
//        S => Ok(Json.toJson(S))
//      }.recover { case _ => InternalServerError}
//    }
//
    /** update the tweet for the given id from the JSON body */
    def updateTweet() = Action.async { request =>
//      println("Llegue")
//      request.body.asOpt[Tweet].map { tweet =>
//        println("Map1")
//        println(tweet._id)
//        collection.update(BSONDocument("_id" -> tweet._id.get),
//        BSONDocument("Text" -> tweet.Text, "PosiVote"->tweet.PosiVote,"NeuVote"->tweet.NeuVote,
//        "NegVote"->tweet.NegVote, "User" -> tweet.User)).map{
//          _ => Ok(Json.toJson(tweet))
//        }.recover { case _ => InternalServerError}
//      }.getOrElse(Future(BadRequest("lhkh")))


      request.body.asJson.fold(Future(BadRequest("no tiene un json")))(json =>
        json.validate[Tweet].fold(
         valid ={ tweet =>   collection.update(BSONDocument("_id" -> tweet._id.get),
           TweetBSONWriter.write(tweet)).map{
               _ => Ok(Json.toJson(tweet))
              }.recover { case _ => InternalServerError("fallo loco")}
         },
          invalid = e => Future(BadRequest(e.toString()))
        )
      )
//      val objectID = BSONObjectID.apply(id) // get the corresponding BSONObjectID
//      val Text = request.body.\("Text").toString().replace("\"", "")
//      val PosVote = request.body.\("PosVote").as[Int]
//      val NeuVote = request.body.\("NeuVote").as[Int]
//      val NegVote = request.body.\("NegVote").as[Int]
//      val User = request.body.\("User").asOpt[String]
//      val modifier = BSONDocument(// create the modifier tweet
//        "$set" -> BSONDocument(
//          "Text" -> Text,
//          "PosVote" -> PosVote,
//          "NeuVote" -> NeuVote,
//          "NegVote" -> NegVote,
//          "User" -> User
//        ))
//      collection.update(BSONDocument("_id" -> objectID), modifier).map(
//        _ => Ok(Json.toJson(Tweet(Option(objectID), Text, PosVote, NeuVote, NegVote, User)))) // return the modified tweet in a JSON
    }
//
//    /** delete a tweet for the given id */
//    def delete(id: String) = Action.async(parse.empty) {
//      val objectID = new BSONObjectID(id) // get the corresponding BSONObjectID
//      collection.remove(BSONDocument("_id" -> objectID)).map(// remove the tweet
//        _ => Ok(Json.obj())).recover{ case _ => InternalServerError} // and return an empty JSON while recovering from errors if any
//    }
  }


