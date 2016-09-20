package selfMessage 

import akka.actor.{Actor, ActorLogging, ActorRef, ActorSystem, Props, Status}
import akka.event.LoggingReceive
import akka.pattern.ask
import akka.util.Timeout
import scala.collection.mutable.HashMap
import scala.concurrent.Future
import scala.concurrent.duration._
import scala.language.postfixOps
import scala.util.{Failure, Success}


case class Rumor(id: String)
case class Snoop(id: String)
case class Set(key: String, value: Any)
case class Get(key: String)
case class KeyNotFound(key: String) extends Exception
case class Decrypt(cipherText: String)


object Main extends App {
  val system = ActorSystem("selfmessage")
  val cache = system.actorOf(Props[Cache], name = "cache")
  val secrets = system.actorOf(Props(classOf[Secrets], cache.path.toString), name = "secrets")
  val spy = system.actorOf(Props(classOf[Spy], secrets.path.toString), name = "spy")

  // Pre-populate the cache
  val id = "hasit"
  cache ! Set(id, "(encryption)Akka rocks!")

  spy ! Rumor(id) 
  spy ! Rumor("non-existent") 

  Thread.sleep(500)  // Increase if necessary
  system.terminate
}

class Spy(secretsPath: String) extends Actor with ActorLogging {
  import scala.concurrent.ExecutionContext.Implicits.global
  private implicit val timeout = Timeout(2 seconds)

  private val secrets = context.actorSelection(secretsPath) 

  def receive = LoggingReceive {
    case msg: Rumor =>
      log.info("Rcvd Rumor(id: {})", msg.id)

      val f: Future[String] = ask(secrets, Snoop(msg.id)).mapTo[String]
      f onComplete {
        case Success(secret: String) =>
          log.info("rumor {}: {}", msg.id, secret)
        case Failure(e) =>
          log.info("rumor {} was problematic: {}", msg.id, e.getMessage)
      }
  }
}

class Secrets(cachePath: String) extends Actor with ActorLogging {
  import scala.concurrent.ExecutionContext.Implicits.global
  private implicit val timeout = Timeout(2 seconds)

  case class DecryptTo(actorRef: ActorRef, ct: String)

  private val cache = context.actorSelection(cachePath) 
  private val decrypter = context.actorOf(Props[Decrypter], name = "decrypter")

  def receive = LoggingReceive {
    case msg: Snoop =>
      log.info("Rcvd Snoop(id: {})", msg.id)
      val senderRef = sender()
      val f = ask(cache, Get(msg.id)).mapTo[String]
      f onComplete {
        case Success(ct: String) =>
          log.info("cipherText: {}", ct)
          context.self ! DecryptTo(senderRef, ct)
        case Failure(e: KeyNotFound) =>
          senderRef ! Status.Failure(new Exception("not found in cache"))
        case Failure(e) =>
          senderRef ! Status.Failure(e)
      }
    case msg: DecryptTo =>
      val f = ask(decrypter, Decrypt(msg.ct)).mapTo[String]
      f onComplete {
        case Success(pt: String) =>
          log.info("plainText: {}", pt)
          msg.actorRef ! pt 
        case Failure(e) =>
          msg.actorRef ! Status.Failure(e)
      }
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
