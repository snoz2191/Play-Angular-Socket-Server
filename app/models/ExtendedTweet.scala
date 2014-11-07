package models

import play.api.Logger
import play.api.libs.json.{Writes, Reads, Json}
import play.api.libs.functional.syntax._
import play.api.libs.json._

/**
 * Created by domingo on 06/11/14.
 */

case class ExtendedTweet(tweet: Tweet, polarity: String, categories: List[String])

object ExtendedTweet {
  /** serialize/Deserialize a Tweet into/from JSON value */
  //  implicit val tweetFormat = Json.format[Tweet]

  implicit val ExtendedTweetToJson : Writes[ExtendedTweet] = (
      (__).write[Tweet] and
      (__ \ "polarity").write[String] and
      (__ \ "categories").write[List[String]]
    )(unlift(ExtendedTweet.unapply))

  implicit val jsonToExtendedTweet : Reads[ExtendedTweet] = (
      (__ \ "tweet").read[Tweet] and
      (__ \ "polarity").read[String] and
      (__ \ "categories").read[List[String]]
    )(ExtendedTweet.apply _)
}

