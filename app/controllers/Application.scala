package controllers

import play.api._
import play.api.mvc._

import scalaj.http.Http

import java.io.InputStreamReader
import play.api.libs.json._

object Application extends Controller {


  def index = Action {


    Ok(views.html.index("Your new application is ready."))
  }

  def attribute(id: String) = Action {




    val img = new FlickrImage(id)
    val t = img.title
    val l = img.license.toString

    // Ok(views.html.index(s"$t $l"))
    Ok(img.images("Medium 640"))
  }

}

class FlickrImage(id: String) {
  val flickREndpoint = "https://api.flickr.com/services/rest/"

  val key = Play.current.configuration.getString("flickr.key").get


  val rawTitle = (__ \ 'photo \ 'title \ '_content).json.pick[JsString]
  val rawLicense = (__ \ 'photo \ 'license ).json.pick[JsString]



  lazy val info = {
    val src = Http(flickREndpoint).param("method","flickr.photos.getInfo")
      .param("api_key", key)
      .param("photo_id", id)
      .param("nojsoncallback", "1")
      .param("format", "json").asString
    Json.parse(src)
  }

  lazy val sizes = {
    val src = Http(flickREndpoint).param("method","flickr.photos.getSizes")
      .param("api_key", key)
      .param("photo_id", id)
      .param("nojsoncallback", "1")
      .param("format", "json").asString
    Json.parse(src)
  }

  lazy val title = info.transform(rawTitle).get.value
  lazy val license = info.transform(rawLicense).get.value

  lazy val images = {
    val picker = (__ \ 'sizes \ 'size ).json.pick[JsArray]
    sizes.transform(picker).get.value.map( sz =>
      (sz \ "label").asOpt[JsString].get.value -> (sz \ "source").asOpt[JsString].get.value
    ).toMap
  }


}