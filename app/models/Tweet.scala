package models

import play.api.Logger
import play.api.libs.json.{Writes, Reads, Json}
import play.api.libs.functional.syntax._
import play.api.libs.json._
import reactivemongo.bson.{BSONDocument, BSONDocumentReader, BSONDocumentWriter, BSONObjectID, BSONObjectIDIdentity, BSONStringHandler, BSONNumberLike}
import reactivemongo.bson.Producer.nameValue2Producer
import play.modules.reactivemongo.json.BSONFormats._
/*
 * Created by domingo on 08/08/14.
 */
case class Tweet(_id: Option[BSONObjectID], text: String, posivote: Int, neuvote: Int, negvote: Int, user: Option[String])

object Tweet {
  /** serialize/Deserialize a Tweet into/from JSON value */
//  implicit val tweetFormat = Json.format[Tweet]

  implicit val TweetToJson : Writes[Tweet] = (
    (__ \ "_id").writeNullable[String].contramap[Option[BSONObjectID]](l => l.map(_.stringify)) and
      (__ \ "text").write[String] and
      (__ \ "posivote").write[Int] and
      (__ \ "neuvote").write[Int] and
      (__ \ "negvote").write[Int] and
      (__ \ "user").writeNullable[String]
    )(unlift(Tweet.unapply))

  implicit val jsonToTweet : Reads[Tweet] = (
    (__ \ "_id").readNullable[String].map[Option[BSONObjectID]](l => l.map(BSONObjectID(_))) and
      (__ \ "text").read[String] and
      (__ \ "posivote").read[Int] and
      (__ \ "neuvote").read[Int] and
      (__ \ "negvote").read[Int] and
      (__ \ "user").readNullable[String]
    )(Tweet.apply _)

  /** serialize a Tweet into a BSON */
  implicit object TweetBSONWriter extends BSONDocumentWriter[Tweet] {
    def write(tweet: Tweet): BSONDocument =
      BSONDocument(
        "_id" -> tweet._id.getOrElse(BSONObjectID.generate),
        "text" -> tweet.text,
        "posivote" -> tweet.posivote,
        "neuvote" -> tweet.neuvote,
        "negvote" -> tweet.negvote,
        "user" -> tweet.user.get)
  }


  /** deserialize a Tweet from a BSON */
  implicit object TweetBSONReader extends BSONDocumentReader[Tweet] {
    def read(doc: BSONDocument): Tweet = {
      Tweet(
        doc.getAs[BSONObjectID]("_id"),
        doc.getAs[String]("text").get,
        doc.getAs[Int]("posivote").get,
        doc.getAs[Int]("neuvote").get,
        doc.getAs[Int]("negvote").get,
        doc.getAs[String]("user"))
    }
  }
}
