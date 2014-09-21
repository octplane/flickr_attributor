package models

import play.api._
import play.api.libs.json._

import scalaj.http.Http
import scalaj.http.HttpOptions


class FlickrImage(id: String) {
  val flickREndpoint = "https://api.flickr.com/services/rest/"

  val aliasToLabel = Map(
    "s" -> "Square",
    "q" -> "Large Square",
    "t" -> "Thumbnail",
    "m" -> "Small",
    "n" -> "Small 320",
    ""  -> "Medium",
    "z" -> "Medium 640",
    "c" -> "Medium 800",
    "b" -> "Large",
    "h" -> "Large 1600",
    "k" -> "Large 2048",
    "o" -> "Original"
    )

  val labelToAlias = aliasToLabel map {_.swap}

  val key = Play.current.configuration.getString("flickr.key").get

  val rawTitle = (__ \ 'photo \ 'title \ '_content).json.pick[JsString]
  val rawLicense = (__ \ 'photo \ 'license ).json.pick[JsString]
  val rawUser = ( __ \ 'photo \ 'owner \ 'username).json.pick[JsString]
  val stat = ( __ \ 'stat ).json.pick[JsString]

  val baseRequest = Http(flickREndpoint)
      .param("api_key", key)
      .param("nojsoncallback", "1")
      .param("format", "json")
      .option(HttpOptions.connTimeout(2000))
      .option(HttpOptions.readTimeout(5000))

  lazy val (info: JsValue, status: String) = {
    val src: String = baseRequest
      .param("method","flickr.photos.getInfo")
      .param("photo_id", id).asString

    val ret: JsValue = Json.parse(src)

    ret.transform(stat) match {
      case JsError(_) => (ret, "ok")
      case JsSuccess(v, p) => {
        v.value match {
          case "ok" => (ret, "ok")
          case _ => (ret, src)
        }
      }
    }
  }

  lazy val sizes = {
    val src = baseRequest
      .param("method","flickr.photos.getSizes")
      .param("photo_id", id).asString
    Json.parse(src)
  }

  lazy val title = info.transform(rawTitle).getOrElse(new JsString("")).value
  lazy val license = info.transform(rawLicense).get.value
  lazy val user = info.transform(rawUser).get.value

  lazy val images = {
    val picker = (__ \ 'sizes \ 'size ).json.pick[JsArray]
    sizes.transform(picker).get.value.map( sz =>
      labelToAlias((sz \ "label").asOpt[JsString].get.value) -> (sz \ "source").asOpt[JsString].get.value
    ).toMap
  }


}
