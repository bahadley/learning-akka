package askAwait 

import akka.actor.{Actor, ActorLogging, ActorSystem, Props}
import akka.event.LoggingReceive
import akka.pattern.ask
import akka.util.Timeout
import com.akkademy.messages.{Get, Set}
import com.akkademy.AkkademyDb
import scala.concurrent.{Await, Future}
import scala.concurrent.duration._
import scala.language.postfixOps


case class Rumor(id: String)
case class Whisper(id: String)
case class DecryptCipherText(cipherText: String)
case class DecryptResponse(plainText: String)


object Main extends App {
  val system = ActorSystem("ask-await")
  val cache = system.actorOf(Props[AkkademyDb], name = "cache")
  val secrets = system.actorOf(Props(classOf[Secrets], cache.path.toString), name = "secrets")
  val snoop = system.actorOf(Props(classOf[Snoop], secrets.path.toString), name = "snoop")

  val id = "1"

  // Pre-populate the cache
  cache ! Set(id, "(encryption)Akka rocks!")

  snoop ! Rumor(id) 

  Thread.sleep(500)
  system.terminate
}

class Snoop(secretsPath: String) extends Actor with ActorLogging {
  private val secrets = context.actorSelection(secretsPath) 
  private implicit val timeout = Timeout(2 seconds)

  def receive = LoggingReceive {
    case msg: Rumor =>
      log.info("Rcvd Rumor(id: [{}])", msg.id)
      val f: Future[String] = ask(secrets, Whisper(msg.id)).mapTo[String]
      val secret = Await.result(f, timeout.duration)
      log.info("secret: [{}]", secret)
  }
}

class Secrets(cachePath: String) extends Actor with ActorLogging {
  private val cache = context.actorSelection(cachePath) 
  private val decrypter = context.actorOf(Props[Decrypter], name = "decrypter")
  private implicit val timeout = Timeout(2 seconds)

  def receive = LoggingReceive {
    case msg: Whisper =>
      log.info("Rcvd Whisper(id: [{}])", msg.id)
      val f: Future[String] = ask(cache, Get(msg.id)).mapTo[String]
      val ct = Await.result(f, timeout.duration)
      log.info("ciphertext: [{}]", ct)
      decrypter ! DecryptCipherText(ct)

    case msg: DecryptResponse =>
      log.info("plaintext: [{}]", msg.plainText)
  }
}

class Decrypter extends Actor with ActorLogging {
  def receive = LoggingReceive {
    case msg: DecryptCipherText =>
      log.info("Rcvd Decrypt(cipherText: [{}])", msg.cipherText)
      val plainText = msg.cipherText.replace("(encryption)", "")
      sender() ! DecryptResponse(plainText) 
  }
}
