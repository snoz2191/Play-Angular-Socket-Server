import java.net.URI

import com.couchbase.client.CouchbaseClient
import com.couchbase.client.protocol.views.{ViewDesign, DesignDocument}
import play.Logger
import play.api.{Application, GlobalSettings}
import play.api.mvc.WithFilters
import play.filters.gzip.GzipFilter

object Global extends WithFilters with GlobalSettings {
  override def onStart(app: Application) = {

  }
}
