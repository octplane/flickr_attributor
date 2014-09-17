package controllers

import models._
import play.api._
import play.api.mvc._

import scala.collection.JavaConversions._

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


object Application extends Controller {

  def index = Action {


    Ok(views.html.index("Your new application is ready."))
  }

  def attribute(id: String) = Action {




    val img = new FlickrImage(id)
    val title = img.title
    val user = img.user
    val license = new License(img.license.toString)

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


