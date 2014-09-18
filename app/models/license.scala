package models

import play.api.Play
import play.api.Play.current

import scalaj.http.Http
import scalaj.http.HttpOptions

import javax.imageio._
import java.io.FileInputStream

class License(id: String) {
  val alias = {
    id match {
      case "0" => "All Rights Reserved"
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


  val text = id match {
    case "0" => "All Rights Reserved - "
    case _ => ""
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

  val iconBufferedImage: Option[java.awt.image.BufferedImage] = id match {
    case "0" => None
    case _ => {
      val reader = ImageIO.getImageReadersBySuffix("png").next
      val file_asset = Play.getFile(s"app/assets/licenses/$alias.png")
      val imageInputStream = ImageIO.createImageInputStream(new FileInputStream(file_asset))

      reader.setInput(imageInputStream)
      val image = reader.readAll(0, null) // Important, also read metadata

      Some(image.getRenderedImage.asInstanceOf[java.awt.image.BufferedImage])
    }
  }
}