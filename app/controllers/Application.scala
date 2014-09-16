package controllers

import play.api._
import play.api.mvc._

import scalaj.http.Http
import scalaj.http.HttpOptions


import java.io.InputStreamReader
import play.api.libs.json._

import javax.imageio._
import java.io.ByteArrayOutputStream
import javax.imageio.ImageWriteParam._

object Application extends Controller {


  def index = Action {


    Ok(views.html.index("Your new application is ready."))
  }

  def attribute(id: String) = Action {




    val img = new FlickrImage(id)
    val t = img.title
    val l = img.license.toString

    val src = img.images("Medium 640")
    val reader = ImageIO.getImageReadersBySuffix("jpg").next();

    // Fix me: Colors are distorted...
    val i = Http(src)
      .option(HttpOptions.connTimeout(2000))
      .option(HttpOptions.readTimeout(5000)) { inputStream =>
        val imageInputStream = ImageIO.createImageInputStream(inputStream);
        reader.setInput(imageInputStream);
        reader.readAll(0, null); // Important, also read metadata
    }

    val renderedImage = i.getRenderedImage();

    // Modify renderedImage as you like

    val writer = ImageIO.getImageWriter(reader);
    val param = writer.getDefaultWriteParam();
    param.setCompressionMode(MODE_COPY_FROM_METADATA); // This modes ensures closest to original compression

    val bo = new ByteArrayOutputStream
    val ios = ImageIO.createImageOutputStream(bo)
    writer.setOutput(ios)
    writer.write(null, i, param)


    Ok(bo.toByteArray).as("image/jpeg")  }

}

class FlickrImage(id: String) {
  val flickREndpoint = "https://api.flickr.com/services/rest/"

  val key = Play.current.configuration.getString("flickr.key").get


  val rawTitle = (__ \ 'photo \ 'title \ '_content).json.pick[JsString]
  val rawLicense = (__ \ 'photo \ 'license ).json.pick[JsString]


  val baseRequest = Http(flickREndpoint)
      .param("api_key", key)
      .param("nojsoncallback", "1")
      .param("format", "json")
      .option(HttpOptions.connTimeout(2000))
      .option(HttpOptions.readTimeout(5000))

  lazy val info = {
    val src = baseRequest
      .param("method","flickr.photos.getInfo")
      .param("photo_id", id).asString

    Json.parse(src)
  }

  lazy val sizes = {
    val src = baseRequest
      .param("method","flickr.photos.getSizes")
      .param("photo_id", id).asString

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