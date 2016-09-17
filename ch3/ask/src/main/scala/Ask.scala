package ask

import akka.actor.{Actor, ActorLogging, ActorSystem, Props, Status}
import akka.event.LoggingReceive
import akka.pattern.ask
import akka.util.Timeout
import com.akkademy.messages._
import com.akkademy.AkkademyDb
import scala.concurrent.Future
import scala.concurrent.duration._
import scala.language.postfixOps


case class ParseArticle(url: String)
case class ParseHtmlArticle(url: String, html: String)
case class HttpResponse(body: String)
case class ArticleBody(url: String, body: String)


class Ask(sourcePath: String) extends Actor with ActorLogging {

  private val source = context.actorSelection(sourcePath) 
  private val cache = context.actorOf(Props[AkkademyDb], name = "cache") 
  private val parser = context.actorOf(Props[Parser], name = "parser") 

  private implicit val timeout = Timeout(2 seconds)

  import scala.concurrent.ExecutionContext.Implicits.global

  def receive = LoggingReceive {
    case msg: ParseArticle =>
      val senderRef = sender()

      val result = source ? Get(msg.url)

      /*
      val result = cacheResult.recoverWith {
        case _: Exception =>
          ((source ? Get(msg.url)).mapTo[String]).onSuccess({
          case s: String =>
            parser ? ParseHtmlArticle(msg.url, sourceResult)    
        )}
      }
      */

      result onComplete {
         case scala.util.Success(body: String) =>
           log.info("Source Hit")
           senderRef ! body 
         case scala.util.Failure(t) =>
           senderRef ! akka.actor.Status.Failure(t) 
         case _ =>
           log.info("Unknown result")
      }
  }
}

class Parser extends Actor with ActorLogging {
  def receive = LoggingReceive {
    case msg: ParseHtmlArticle =>
      log.info(
        "Rcvd ParseHtmlArticle(url: [{}], html: [{}])", 
        msg.url, 
        msg.html)
      sender() ! ArticleBody(
        msg.url, 
        msg.html.replaceAll("</?body>", ""))
  }
}

object Main extends App {

  val system = ActorSystem("ask")
  val source = system.actorOf(Props[AkkademyDb], name = "source")
  val ask = system.actorOf(Props(classOf[Ask], source.path.toString), name = "ask")

  val url1 = "http://www.arrkis.com/news1.html"
  val url2 = "http://www.arrakis.com/news2.html"

  source ! Set(url1, "<body>Harkonnens party</body>")
  source ! Set(url2, "<body>Fremen attack</body>")

  ask ! ParseArticle(url1)
  ask ! ParseArticle(url2)
}
