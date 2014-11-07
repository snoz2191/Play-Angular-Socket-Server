import play.Logger
import play.api.GlobalSettings
import play.api.mvc.WithFilters
import play.filters.gzip.GzipFilter

object Global extends WithFilters with GlobalSettings {

  def onStart(app: App) {
    Logger.info("Application has started, Yahoo!")
  }

}
