## Share and respect the license !

~~ <center>
~~ ![Creative Commons](/images/6709759539.jpg)
~~ </center>

### What's wrong with Creative Commons picture sharing ?

Since twitter started supporting images in post, most clients now have the ability to display these pictures along with the post.

Adding a picture to a tweet increases [clicks, retweets and favorites] and will improve your social communication!

Unfortunately, your photos might not be good enough to illustrate your point, so you might want to use other people's photos.

But sharing a creative commons usually requires [attribution of the work you use] and in some context, it means losing some precious characters.

Displaying the license information on the media brings a simple solution to that problem.

### And so was born the *Flickr Attributor*

This webapp generates photos that contain the attribution and details of the licence at the bottom of it. Such as this picture:

~~ <center>
~~ ![A Puppy !](/images/8165495019.jpg)
~~ </center>

Of course, there is no link, and not full identifier of the picture in the generated legend, and as specified the [Creative Commons guidelines], "_There is no one right way; just make sure your attribution is reasonable and suited to the medium you're working with._" I'm including the CC icon, the author and Flickr mention and the photo title.

## How do I use this ?

Flickr Attributor is a Web service which means it has no user interface. To generate a picture with attribution, you just need its Flickr identifier.

'' http://fa.oct.zoy.org/flickr/2696912806

And you can get the image with its license. You can also use this bookmarklet ~<a href="javascript:(function(){var%20l=window.location.href;l=%22http://fa.oct.zoy.org/flickr/%22+l.replace(/https:\/\/www.flickr.com\/photos\/[^\/]+\//,%22%22).replace(/\/.*/,%22%22),window.open(l);})()">~Flickr Attribute~</a>~ on any flickr Photo page.

~~ <center>
~~ ![A tree](http://fa.oct.zoy.org/flickr/2696912806)
~~ </center>

This gets you the "Medium" sized image. You can also request other sizes by appending a variable to the URL:

'' "s" -> "Square",
'' "q" -> "Large Square",
'' "t" -> "Thumbnail",
'' "m" -> "Small",
'' "n" -> "Small 320",
'' ""  -> "Medium",
'' "z" -> "Medium 640",
'' "c" -> "Medium 800",
'' "b" -> "Large",
'' "h" -> "Large 1600",
'' "k" -> "Large 2048",
'' "o" -> "Original"

The result looks like [http://fa.oct.zoy.org/flickr/455488392/z].

Sometimes, the version you'll ask for won't be available. If so, you'll get a 404 error, and that's life.

The result is cached for a while, but **I'd appreciate if you downloaded the generated picture and use your own service instead of hotlinking it**.

Everything is open source under the MIT License. It means you can browse the source on Github or run your own Flickr Attributor.

[Get the sources, report issues and ask for features there].


**This application is not endorsed by Flickr in any way. I'm using their API and a key as a regular API user.**

## How to run this application.

This application is using the play framework. To run it, you can just type:

'' ./activator run

It will probably download half of the Internet, and then start:

''--- (Running the application from SBT, auto-reloading is enabled) ---
''[info] play - Listening for HTTP on /0:0:0:0:0:0:0:0:9000
''(Server started, use Ctrl+D to stop and go back to the console...)

This is the development mode by default.

If you want to package/run the application in production, the project makes use of the [sbt native packager](http://www.scala-sbt.org/sbt-native-packager/GettingStartedApplications/index.html). The docker generator has been configured:

'' sbt docker:publishLocal

or

'' sbt docker:publish # publish the artifact on the docker hub.