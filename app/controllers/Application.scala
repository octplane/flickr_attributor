package controllers

import models._
import play.api._
import play.api.mvc._
import play.api.mvc.Results._

import scala.collection.JavaConversions._

import scalaj.http.Http
import scalaj.http.HttpOptions


import java.io.InputStreamReader
import play.api.libs.json._
import play.api.cache.Cached
import play.api.Play.current

import javax.imageio._
import javax.imageio.ImageWriteParam._
import javax.imageio.plugins.jpeg.JPEGImageWriteParam

import java.awt.Graphics2D
import java.awt.image.BufferedImage
import java.awt.Color
import java.awt.Font
import java.awt.color.ColorSpace
import java.awt.RenderingHints

import java.io.ByteArrayOutputStream

import scala.util.Random


object Application extends Controller {

  // def index = Action {
  //   val candidates = Seq("6280941316", "145197704", "2271154446",
  //     "2013404", "5996465579",
  //     "7176605114", "6709759539")


  // 	Ok(views.html.index("Your new application is ready.", Random.shuffle(candidates).head))
  // }

  def attribute(id: String, size: String) = Cached
    .status(_ => "/attribute/"+ id + "/" + size, 200)
    .includeStatus(500, 60)
    .includeStatus(404, 120) { Action
    {
      try {

      	val img = new FlickrImage(id)

        img.status match {
          case "ok" => {
            val title = img.title
            val user = img.user
            val license = new License(img.license.toString)
            val lic_text = license.text

            val src = img.images(size)
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

            var x:Int = 
              license.iconBufferedImage match {
                case None => { 10 }
                case Some(icn) => {
                  g2d.drawImage(icn, 1, sourceBuffer.getHeight + 3, null )
                  90
                }
              }

            g2d.setPaint(Color.lightGray)
            g2d.setFont(new Font("Serif", Font.PLAIN, 14))

            val text = s"$lic_text$user on Flickr '$title'"
            val fm = g2d.getFontMetrics()
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


            Ok(bo.toByteArray).as("image/jpeg")

          }
          case msg => NotFound(msg)
        }
      } catch {
        case e:Exception => {
          println(e.printStackTrace)
          InternalServerError(e.toString)
        }
      }
    }
  }
}


