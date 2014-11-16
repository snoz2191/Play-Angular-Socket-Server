package models

import play.api.libs.functional.syntax._
import play.api.libs.json._

/**
 * Created by domingo on 10/11/14.
 */

/* Class Summary
* Attributes:
*   positive - Number of positives tweets
*   neutral - Number of neutral tweets
*   negative - Number of negative tweets
*/
case class Summary(positive: Int, neutral: Int, negative: Int) {
  //Overloading bynary operand "+"
  def +(s : Summary) =
    new Summary(this.positive + s.positive, this.neutral + s.neutral, this.negative + s.negative)
}

object Summary {
  /** serialize/Deserialize a Summary into/from JSON value */

  implicit val SummaryToJson : Writes[Summary] = (
      (__ \ "positive").write[Int] and
      (__ \ "neutral").write[Int] and
      (__ \ "negative").write[Int]
    )(unlift(Summary.unapply))

  implicit val jsonToSummary : Reads[Summary] = (
      (__ \ "positive").read[Int] and
      (__ \ "neutral").read[Int] and
      (__ \ "negative").read[Int]
    )(Summary.apply _)
}
