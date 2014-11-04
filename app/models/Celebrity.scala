package models

import models.Name.NameBSONReader
import play.api.libs.json.Json
import reactivemongo.bson.{BSONDocument, BSONDocumentReader, BSONDocumentWriter, BSONObjectID, BSONObjectIDIdentity, BSONStringHandler}
import reactivemongo.bson.Producer.nameValue2Producer
import play.modules.reactivemongo.json.BSONFormats._

/*
 * Author: Sari Haj Hussein
 */

case class Celebrity(id: Option[BSONObjectID], name: Name, website: String, bio: String)

object Celebrity {
  /** serialize/Deserialize a Celebrity into/from JSON value */
  implicit val celebrityFormat = Json.format[Celebrity]

  /** serialize a Celebrity into a BSON */
  implicit object CelebrityBSONWriter extends BSONDocumentWriter[Celebrity] {
    def write(celebrity: Celebrity): BSONDocument =
      BSONDocument(
        "_id" -> celebrity.id.getOrElse(BSONObjectID.generate),
        "name" -> celebrity.name,
        "website" -> celebrity.website,
        "bio" -> celebrity.bio)
  }

  /** deserialize a Celebrity from a BSON */
  implicit object CelebrityBSONReader extends BSONDocumentReader[Celebrity] {
    def read(doc: BSONDocument): Celebrity =
      Celebrity(
        doc.getAs[BSONObjectID]("_id"),
        doc.getAs[Name]("name").get,
        doc.getAs[String]("website").get,
        doc.getAs[String]("bio").get)
  }
}