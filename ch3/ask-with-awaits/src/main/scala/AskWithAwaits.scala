package ask 

import akka.actor.{Actor, ActorLogging, ActorSystem, Props, Status}
import akka.event.LoggingReceive
import akka.pattern.ask
import akka.util.Timeout
import scala.collection.mutable.HashMap
import scala.concurrent.{Await, Future}
import scala.concurrent.duration._
import scala.language.postfixOps


case class Rumor(id: String)
case class Snoop(id: String)
case class Set(key: String, value: Any)
case class Get(key: String)
case class KeyNotFound(key: String) extends Exception
case class Decrypt(cipherText: String)


object Main extends App {
  val system = ActorSystem("ask")
  val cache = system.actorOf(Props[Cache], name = "cache")
  val secrets = system.actorOf(Props(classOf[Secrets], cache.path.toString), name = "secrets")
  val spy = system.actorOf(Props(classOf[Spy], secrets.path.toString), name = "spy")

  // Pre-populate the cache
  val id = "1"
  cache ! Set(id, "(encryption)Akka rocks!")

  spy ! Rumor(id) 

  Thread.sleep(500)  // Increase if necessary
  system.terminate
}

class Spy(secretsPath: String) extends Actor with ActorLogging {
  private implicit val timeout = Timeout(2 seconds)

  private val secrets = context.actorSelection(secretsPath) 

  def receive = LoggingReceive {
    case msg: Rumor =>
      log.info("Rcvd Rumor(id: {})", msg.id)

      val f: Future[String] = ask(secrets, Snoop(msg.id)).mapTo[String]
      val secret = Await.result(f, timeout.duration)
      log.info("secret: {}", secret)
  }
}

class Secrets(cachePath: String) extends Actor with ActorLogging {
  import scala.concurrent.ExecutionContext.Implicits.global
  private implicit val timeout = Timeout(2 seconds)

  private val cache = context.actorSelection(cachePath) 
  private val decrypter = context.actorOf(Props[Decrypter], name = "decrypter")

  def receive = LoggingReceive {
    case msg: Snoop =>
      log.info("Rcvd Snoop(id: {})", msg.id)

      val f1: Future[String] = ask(cache, Get(msg.id)).mapTo[String]
      val ct = Await.result(f1, timeout.duration)
      log.info("ciphertext: {}", ct)

      val f2: Future[String] = ask(decrypter, Decrypt(ct)).mapTo[String]
      val pt = Await.result(f2, timeout.duration)
      log.info("plaintext: {}", pt)

      sender() ! pt
  }
}

class Cache extends Actor with ActorLogging {
  val map = new HashMap[String, Any]
  def receive = LoggingReceive {
    case msg: Get =>
      log.info("Rcvd Get(key: {})", msg.key)
      map.get(msg.key) match {
        case Some(value) => sender() ! value
        case None        => sender() ! Status.Failure(KeyNotFound(msg.key))
      }
    case msg: Set =>
      log.info("Rcvd Set(key: {}, value: {})", msg.key, msg.value)
      sender() ! map.put(msg.key, msg.value)
  }
}

class Decrypter extends Actor with ActorLogging {
  def receive = LoggingReceive {
    case msg: Decrypt =>
      log.info("Rcvd Decrypt(cipherText: {})", msg.cipherText)
      sender() ! msg.cipherText.replace("(encryption)", "") 
  }
}
