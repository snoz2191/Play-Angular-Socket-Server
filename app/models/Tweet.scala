package models

import play.api.Logger
import play.api.libs.json.{Writes, Reads, Json}
import play.api.libs.functional.syntax._
import play.api.libs.json._
/*
 * Created by domingo on 08/08/14.
 */

/* Class Tweet
* Class that holds the attributes that conforms a tweet, and it's compatible with the Twitter API
* and Social+ backend.
*/
case class Tweet(profile_image_url_https: String, created_at: String, id_str: String, from_user: String,
                 from_user_id_str:String, from_username: String, text: String, lang: String, id: Long,
                 possibly_sensitive: Boolean, retweeted: Boolean, followers_count: Int, favorite_count: Int,
                 retweet_count: Int)

object Tweet {
  /** serialize/Deserialize a Tweet into/from JSON value */

  implicit val TweetToJson : Writes[Tweet] = (
      (__ \ "profile_image_url_https").write[String] and
      (__ \ "created_at").write[String] and
      (__ \ "id_str").write[String] and
      (__ \ "from_user").write[String] and
      (__ \ "from_username").write[String] and
      (__ \ "from_user_id_str").write[String] and
      (__ \ "text").write[String] and
      (__ \ "lang").write[String] and
      (__ \ "id").write[Long] and
      (__ \ "possibly_sensitive").write[Boolean] and
      (__ \ "retweeted").write[Boolean] and
      (__ \ "followers_count").write[Int] and
      (__ \ "favorite_count").write[Int] and
      (__ \ "retweet_count").write[Int]
    )(unlift(Tweet.unapply))

  implicit val jsonToTweet : Reads[Tweet] = (
      (__ \ "profile_image_url_https").read[String] and
      (__ \ "created_at").read[String] and
      (__ \ "id_str").read[String] and
      (__ \ "from_user").read[String] and
      (__ \ "from_username").read[String] and
      (__ \ "from_user_id_str").read[String] and
      (__ \ "text").read[String] and
      (__ \ "lang").read[String] and
      (__ \ "id").read[Long] and
      (__ \ "possibly_sensitive").read[Boolean] and
      (__ \ "retweeted").read[Boolean] and
      (__ \ "followers_count").read[Int] and
      (__ \ "favorite_count").read[Int] and
      (__ \ "retweet_count").read[Int]
    )(Tweet.apply _)
}
