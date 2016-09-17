package askAwait 

import akka.actor.{Actor, ActorLogging, ActorSystem, Props}
import akka.event.LoggingReceive
import akka.pattern.ask
import akka.util.Timeout
import com.akkademy.messages._
import com.akkademy.AkkademyDb
import scala.concurrent.{Await, Future}
import scala.concurrent.duration._
import scala.language.postfixOps


case class Test(id: String)
case class Decrypt(cipherText: String)


class DecryptionService(cachePath: String) extends Actor with ActorLogging {

  private val cache = context.actorSelection(cachePath) 
  private val decrypter = context.actorOf(Props[Decrypter], name = "decrypter")

  private implicit val timeout = Timeout(2 seconds)

  def receive = LoggingReceive {
    case msg: Test =>
      log.info("Rcvd Test(id: [{}])", msg.id)

      val f1: Future[String] = ask(cache, Get(msg.id)).mapTo[String]
      val ct = Await.result(f1, timeout.duration)

      log.info("ciphertext: [{}]", ct)

      val f2: Future[String] = ask(decrypter, Decrypt(ct)).mapTo[String]
      val pt = Await.result(f2, timeout.duration) 

      log.info("plaintext: [{}]", pt)
  }
}


class Decrypter extends Actor with ActorLogging {
  def receive = LoggingReceive {
    case msg: Decrypt =>
      log.info("Rcvd Decrypt(cipherText: [{}])", msg.cipherText)
      sender() ! msg.cipherText.replace("(encryption)", "")
  }
}


object Main extends App {

  val system = ActorSystem("ask-await")
  val cache = system.actorOf(Props[AkkademyDb], name = "cache")
  val service = system.actorOf(Props(classOf[DecryptionService], cache.path.toString), name = "service")

  val id = "1"

  // Pre-populate the cache
  cache ! Set(id, "(encryption)Be sure to drink your Ovaltine")

  service ! Test(id) 

  Thread.sleep(2000)
  system.terminate
}
