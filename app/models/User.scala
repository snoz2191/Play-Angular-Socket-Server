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
case class User( _id: String, score: Int)

object User {
  /** serialize/Deserialize a Tweet into/from JSON value */
  //  implicit val tweetFormat = Json.format[Tweet]

  implicit val UserToJson : Writes[User] = (
      (__ \ "_id").write[String] and
      (__ \ "score").write[Int]
    )(unlift(User.unapply))

  implicit val jsonToUser : Reads[User] = (
      (__ \ "_id").read[String] and
      (__ \ "score").read[Int]
    )(User.apply _)

  /** serialize a User into a BSON */
  implicit object UserBSONWriter extends BSONDocumentWriter[User] {
    def write(user: User): BSONDocument =
      BSONDocument(
        "_id" -> user._id,
        "score" -> user.score)
  }

  /** deserialize a Tweet from a BSON */
  implicit object UserBSONReader extends BSONDocumentReader[User] {
    def read(doc: BSONDocument): User = {
      User(
        doc.getAs[String]("_id").get,
        doc.getAs[Int]("score").get)
    }
  }
}
