package controllers

import play.api._
import play.api.mvc._

import scalaj.http.Http
import scalaj.http.HttpOptions


import java.io.InputStreamReader
import play.api.libs.json._

import javax.imageio._

import java.awt.Graphics2D
import java.awt.image.BufferedImage
import java.awt.Color
import java.awt.Font
import java.awt.color.ColorSpace
import java.awt.RenderingHints

import java.io.ByteArrayOutputStream
import javax.imageio.ImageWriteParam._
import javax.imageio.plugins.jpeg.JPEGImageWriteParam

import scala.collection.JavaConversions._

object Application extends Controller {

  def index = Action {


    Ok(views.html.index("Your new application is ready."))
  }

  def attribute(id: String) = Action {




    val img = new FlickrImage(id)
    val title = img.title
    val user = img.user
    val license = new License(img.license.toString)

    println(license.icon)

    val src = img.images("Medium 640")
    val reader = ImageIO.getImageReadersBySuffix("jpg").next();

    // Fix me: Colors are distorted...
    val sourceImage = Http(src)
      .option(HttpOptions.connTimeout(2000))
      .option(HttpOptions.readTimeout(5000)) { inputStream =>
        val imageInputStream = ImageIO.createImageInputStream(inputStream);
        reader.setInput(imageInputStream);
        reader.readAll(0, null); // Important, also read metadata
    }

    val sourceBuffer = sourceImage.getRenderedImage.asInstanceOf[java.awt.image.BufferedImage]

    val w = sourceBuffer.getWidth
    val h = sourceBuffer.getHeight
    val target = new BufferedImage(
      w, h+21, sourceBuffer.getType)

    val g2d = target.createGraphics()

    // g2d.setRenderingHint(
    //   RenderingHints.KEY_TEXT_ANTIALIASING,
    //   RenderingHints.VALUE_TEXT_ANTIALIAS_GASP)

    g2d.drawImage(sourceBuffer, 0, 0, null)
    g2d.drawImage(license.iconBufferedImage, 1, sourceBuffer.getHeight + 3, null )
    g2d.setPaint(Color.lightGray)
    g2d.setFont(new Font("Serif", Font.PLAIN, 14))

    val text = s"$title by $user on Flickr"
    val fm = g2d.getFontMetrics()
    val x = 90
    val y = sourceBuffer.getHeight + 18 - fm.getMaxDescent
    g2d.drawString(text, x, y)
    g2d.dispose();

    val writer = ImageIO.getImageWriter(reader)
    val param = new JPEGImageWriteParam(null)
    param.setCompressionMode(MODE_EXPLICIT)
    param.setCompressionQuality(0.98f)
    param.setOptimizeHuffmanTables(true)

    val bo = new ByteArrayOutputStream
    val ios = ImageIO.createImageOutputStream(bo)
    writer.setOutput(ios)
    writer.write(null, new IIOImage(target, null, reader.getImageMetadata(0)),  param)


    Ok(bo.toByteArray).as("image/jpeg")  }

}

class FlickrImage(id: String) {
  val flickREndpoint = "https://api.flickr.com/services/rest/"

  val key = Play.current.configuration.getString("flickr.key").get

  val rawTitle = (__ \ 'photo \ 'title \ '_content).json.pick[JsString]
  val rawLicense = (__ \ 'photo \ 'license ).json.pick[JsString]
  val rawUser = ( __ \ 'photo \ 'owner \ 'username).json.pick[JsString]

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
  lazy val user = info.transform(rawUser).get.value

  lazy val images = {
    val picker = (__ \ 'sizes \ 'size ).json.pick[JsArray]
    sizes.transform(picker).get.value.map( sz =>
      (sz \ "label").asOpt[JsString].get.value -> (sz \ "source").asOpt[JsString].get.value
    ).toMap
  }


}

class License(id: String) {
  val alias = {
    id match {
      case "1" => "by-nc-sa"
      case "2" => "by-nc"
      case "3" => "by-nc-nd"
      case "4" => "by"
      case "5" => "by-sa"
      case "6" => "by-nd"
    }
  }

  val icon = {
    val lic = alias
    s"http://i.creativecommons.org/l/$lic/2.0/80x15.png"
  }

  // <license id="0" name="All Rights Reserved" url="" />
  // <license id="1" name="Attribution-NonCommercial-ShareAlike License" url="http://creativecommons.org/licenses/by-nc-sa/2.0/" />
  // <license id="2" name="Attribution-NonCommercial License" url="http://creativecommons.org/licenses/by-nc/2.0/" />
  // <license id="3" name="Attribution-NonCommercial-NoDerivs License" url="http://creativecommons.org/licenses/by-nc-nd/2.0/" />
  // <license id="4" name="Attribution License" url="http://creativecommons.org/licenses/by/2.0/" />
  // <license id="5" name="Attribution-ShareAlike License" url="http://creativecommons.org/licenses/by-sa/2.0/" />
  // <license id="6" name="Attribution-NoDerivs License" url="http://creativecommons.org/licenses/by-nd/2.0/" />
  // <license id="7" name="No known copyright restrictions" url="http://flickr.com/commons/usage/" />
  // <license id="8" name="United States Government Work" url="http://www.usa.gov/copyright.shtml" />

  val iconBufferedImage = {
    val reader = ImageIO.getImageReadersBySuffix("png").next

    // Fix me: Colors are distorted...
    val image = Http(icon)
      .option(HttpOptions.connTimeout(2000))
      .option(HttpOptions.readTimeout(5000)) { inputStream =>
        val imageInputStream = ImageIO.createImageInputStream(inputStream)
        reader.setInput(imageInputStream);
        reader.readAll(0, null); // Important, also read metadata
    }

    image.getRenderedImage.asInstanceOf[java.awt.image.BufferedImage]
  }



}