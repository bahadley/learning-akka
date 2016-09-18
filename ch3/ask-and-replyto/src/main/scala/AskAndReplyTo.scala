package askAndReplyTo 

import akka.actor.{Actor, ActorLogging, ActorRef, ActorSystem, Props}
import akka.event.LoggingReceive
import akka.pattern.ask
import akka.util.Timeout
import com.akkademy.messages.{Get, Set}
import com.akkademy.AkkademyDb
import scala.concurrent.{Await, Future}
import scala.concurrent.duration._
import scala.language.postfixOps


case class Rumor(id: String)
case class Snoop(id: String)
case class DecryptCipherText(spy: ActorRef, cipherText: String)


object Main extends App {
  val system = ActorSystem("ask-and-tell")
  val cache = system.actorOf(Props[AkkademyDb], name = "cache")
  val secrets = system.actorOf(Props(classOf[Secrets], cache.path.toString), name = "secrets")
  val spy = system.actorOf(Props(classOf[Spy], secrets.path.toString), name = "spy")

  val id = "1"

  // Pre-populate the cache
  cache ! Set(id, "(encryption)Akka rocks!")

  spy ! Rumor(id) 

  Thread.sleep(500)
  system.terminate
}

class Spy(secretsPath: String) extends Actor with ActorLogging {
  private val secrets = context.actorSelection(secretsPath) 
  private implicit val timeout = Timeout(2 seconds)

  def receive = LoggingReceive {
    case msg: Rumor =>
      log.info("Rcvd Rumor(id: [{}])", msg.id)
      val f: Future[String] = ask(secrets, Snoop(msg.id)).mapTo[String]
      val secret = Await.result(f, timeout.duration)
      log.info("secret: [{}]", secret)
  }
}

class Secrets(cachePath: String) extends Actor with ActorLogging {
  import scala.concurrent.ExecutionContext.Implicits.global

  private val cache = context.actorSelection(cachePath) 
  private val decrypter = context.actorOf(Props[Decrypter], name = "decrypter")
  private implicit val timeout = Timeout(2 seconds)

  def receive = LoggingReceive {
    case msg: Snoop =>
      log.info("Rcvd Snoop(id: [{}])", msg.id)
      val f1: Future[String] = ask(cache, Get(msg.id)).mapTo[String]
      val ct = Await.result(f1, timeout.duration)
      log.info("ciphertext: [{}]", ct)

      // Equivalent to: ask(decrypter, DecryptCipherText(sender(), ct)) 
      decrypter ? DecryptCipherText(sender(), ct) 
  }
}

class Decrypter extends Actor with ActorLogging {
  def receive = LoggingReceive {
    case msg: DecryptCipherText =>
      log.info("Rcvd Decrypt(cipherText: [{}])", msg.cipherText)
      val plainText = msg.cipherText.replace("(encryption)", "")
      msg.spy ! plainText 
  }
}
