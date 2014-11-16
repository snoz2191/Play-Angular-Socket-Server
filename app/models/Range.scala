package models

import java.util.Date

import play.api.libs.functional.syntax._
import play.api.libs.json._

/**
 * Created by domingo on 10/11/14.
 */

/* Class Range
* Attributes:
*   initial - Initial date of the range
*   end - Ending date of the range
*/
case class Range(initial: Date, end: Date)

object Range {
  /** serialize/Deserialize a Range into/from JSON value */

  implicit val RangeToJson : Writes[Range] = (
    (__ \ "initial").write[Date] and
      (__ \ "end").write[Date]
    )(unlift(Range.unapply))

  implicit val jsonToRange : Reads[Range] = (
    (__ \ "initial").read[Date] and
      (__ \ "end").read[Date]
    )(Range.apply _)
}
