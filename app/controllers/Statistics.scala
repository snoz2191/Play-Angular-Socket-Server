package controllers

import java.net.URI
import java.util.{Calendar, LinkedHashMap}
import com.couchbase.client.CouchbaseClient
import com.couchbase.client.protocol.views._
import models.{Summary, Range}
import play.Logger
import play.api.mvc.Controller
import scala.collection.JavaConverters._
import akka.actor.FSM.Failure
import akka.actor.Status.Success
import play.api.mvc._
import scala.concurrent.ExecutionContext.Implicits.global
import play.api.libs.json._
import play.api.mvc._
import scala.concurrent.Future
import scala.collection.mutable.Map

/**
 * Created by domingo on 08/11/14.
 */
object Statistics extends Controller {
  val systemProperties = System.getProperties
  systemProperties.put("viewmode","production") //Set to store the views on production mode
  System.setProperties(systemProperties)
  val uri = new URI(s"http://127.0.0.1:8091/pools")
  val client = new CouchbaseClient(List(uri).asJava, "SentimentStats", "") //Connecting to the couchbase client
  val designDoc = new DesignDocument("stats") //Creating an empty design document
  val viewName = "by_date" //Name of the view
  //Map Function of the view
  val mapFunction =
    """
      |function (doc, meta) {
      |  var dateArray = dateToArray(doc.created_at);
      |  if (doc.polarity == "Positive") {
      |    for ( var i = 0; i < doc.categories.length; i++ ) {
      |    	dateArray.push(doc.categories[i]);
      |    	emit(dateArray,[1,0,0]);
      |      	dateArray.splice(-1,1);
      |    }
      |  } else if ( doc.polarity == "Neutral") {
      |    for ( var i = 0; i < doc.categories.length; i++ ) {
      |    	dateArray.push(doc.categories[i]);
      |    	emit(dateArray,[0,1,0]);
      |        dateArray.splice(-1,1);
      |    }
      |  } else if ( doc.polarity == "Negative") {
      |    for ( var i = 0; i < doc.categories.length; i++ ) {
      |    	dateArray.push(doc.categories[i]);
      |    	emit(dateArray,[0,0,1]);
      |      	dateArray.splice(-1,1);
      |    }
      |  }
      |}
    """.stripMargin

  //Reduce Function for the view
  val reduceFuction =
    """
      |function (keys, values, rereduce) {
      |  var out = [];
      |  //Prefill the arrays with zeroes.
      |  for(i = 0; i < 3; i++) {
      |    out[i] = 0;
      |  }
      |  for(v in values) {
      |    if(!rereduce) { //Values are the output of map
      |      out[values[v][0] ? 0 : values[v][1] ? 1 : 2 ] += 1;
      |    } else { //Values are the output of reduce
      |      // Combine the arrays
      |      for(h in values[v]) {
      |        out[h] += values[v][h];
      |      }
      |    }
      |  }
      |  return out;
      |}
    """.stripMargin

  //Creating the design document with the specified map and reduce functions
  val viewDesign = new ViewDesign(viewName,mapFunction,reduceFuction)
  designDoc.getViews().add(viewDesign)
  client.createDesignDoc( designDoc )

  /*Function getStatistics
  *   Receives: Json with "initial" and "end" fields, both of them are dates
  *   Returns: Json with a statistical summary of each category sentiment
  * */
  def getStatistics = Action { request =>
    request.body.asJson.fold[Result](Status(BAD_REQUEST))( json =>
      json.validate[Range].fold(
        valid = { rango =>
          val categoryPos = 6 //Position of the category on the key of each row
          val initialCal = Calendar.getInstance()
          val endCal = Calendar.getInstance()
          val view = client.getView("stats","by_date")  //Accessing the view to queried
          val query = new Query()                       //Creating an empty query
          initialCal.setTime(rango.initial)
          endCal.setTime(rango.end)
          query.setGroupLevel(7) //Setting group level so that groups by all of the key value
          //Setting range so it takes that it takes into account only the tweets in the selected range of dates
          query.setRange("["+initialCal.get(Calendar.YEAR)+","+initialCal.get(Calendar.MONTH)+","+
            initialCal.get(Calendar.DAY_OF_MONTH)+",0,0]",
            "["+endCal.get(Calendar.YEAR)+","+(endCal.get(Calendar.MONTH)+1)+","+
              endCal.get(Calendar.DAY_OF_MONTH)+",23,0]")
          val rawResults = client.query(view,query)
          rawResults.getErrors.asScala.foreach(error => Logger.warn(s"Statistics query error: ${error.getFrom} -> ${error.getReason}"))
          //Summarizing all the row data for each category
          val M: Map[String,Summary] = Map()
          rawResults.asScala map { row =>
            val key = row.getKey.drop(1).dropRight(1).split(",").map( _.replace("\"","") )
            val value = row.getValue.drop(1).dropRight(1).split(",")
            //Each position in summary corresponds to the values thrown by couchbase in the form [x,y,z] where each one
            //represents the positive, neutral and negative count respectively
            M(key(categoryPos)) = M.getOrElse(key(categoryPos),Summary(0,0,0)) + Summary(value(0).toInt, value(1).toInt, value(2).toInt)
          }
          Ok(Json.toJson(M.toMap))
        },
        invalid = e => BadRequest(Json.obj("err" -> e.toString()))
      )
    )
  }
}
