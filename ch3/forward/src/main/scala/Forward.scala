package forward 

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
case class DecryptCipherText(cipherText: String)


object Main extends App {
  val system = ActorSystem("ask-and-tell")
  val cache = system.actorOf(Props[AkkademyDb], name = "cache")
  val secrets = system.actorOf(Props(classOf[Secrets], cache.path.toString), name = "secrets")
  val forwarder = system.actorOf(Props(classOf[Forwarder], secrets.path.toString), name = "forwarder")
  val spy = system.actorOf(Props(classOf[Spy], forwarder.path.toString), name = "spy")

  val id = "1"

  // Pre-populate the cache
  cache ! Set(id, "(encryption)Akka rocks!")

  spy ! Rumor(id) 

  Thread.sleep(4000)
  system.terminate
}

class Spy(forwarderPath: String) extends Actor with ActorLogging {
  private val forwarder = context.actorSelection(forwarderPath) 
  private implicit val timeout = Timeout(2 seconds)

  def receive = LoggingReceive {
    case msg: Rumor =>
      log.info("Rcvd Rumor(id: [{}])", msg.id)
      val f: Future[String] = ask(forwarder, msg).mapTo[String]
      val secret = Await.result(f, timeout.duration)
      log.info("secret: [{}]", secret)
  }
}

class Forwarder(secretsPath: String) extends Actor with ActorLogging {
  private val secrets = context.actorSelection(secretsPath) 
  private implicit val timeout = Timeout(2 seconds)

  def receive = LoggingReceive {
    case msg: Rumor =>
       secrets forward msg
  }
}

class Secrets(cachePath: String) extends Actor with ActorLogging {
  private val cache = context.actorSelection(cachePath) 
  private val decrypter = context.actorOf(Props[Decrypter], name = "decrypter")
  private implicit val timeout = Timeout(2 seconds)

  def receive = LoggingReceive {
    case msg: Rumor =>
      log.info("Rcvd Rumor(id: [{}])", msg.id)

      val f1: Future[String] = ask(cache, Get(msg.id)).mapTo[String]
      val ct = Await.result(f1, timeout.duration)
      log.info("ciphertext: [{}]", ct)

      val f2: Future[String] = ask(decrypter, DecryptCipherText(ct)).mapTo[String]
      val pt = Await.result(f2, timeout.duration)
      log.info("plaintext: [{}]", pt)

      sender() ! pt
  }
}

class Decrypter extends Actor with ActorLogging {
  def receive = LoggingReceive {
    case msg: DecryptCipherText =>
      log.info("Rcvd Decrypt(cipherText: [{}])", msg.cipherText)
      val pt = msg.cipherText.replace("(encryption)", "")
      sender() ! pt 
  }
}
