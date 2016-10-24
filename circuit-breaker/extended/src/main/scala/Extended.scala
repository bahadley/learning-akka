package basic

import akka.actor.{Actor, ActorLogging, ActorRef, ActorSystem, Props, Status}
import akka.pattern.{ask, pipe}
import akka.util.Timeout
import scala.collection.mutable.HashMap
import scala.concurrent.duration._
import scala.concurrent.Future
import scala.language.postfixOps
import scala.util.{Failure, Success}


case class Rumor(id: String)
case class Set(key: String, value: Any)
case class Get(key: String)
case class KeyNotFound(key: String) extends Exception


object Main extends App {
  val system = ActorSystem("basic")
  val data = system.actorOf(Props[Data], name = "data")
  val breaker = system.actorOf(Props(classOf[Breaker], data), name = "breaker")
  val spy = system.actorOf(Props(classOf[Spy], breaker), name = "spy")

  // Pre-populate data 
  val id1 = "hasit"
  data ! Set(id1, "Akka rocks!")

  spy ! Rumor(id1) 

  Thread.sleep(500)  // Increase if necessary
  system.terminate
}

class Spy(breaker: ActorRef) extends Actor with ActorLogging {
  import scala.concurrent.ExecutionContext.Implicits.global
  private implicit val timeout = Timeout(2 seconds)

  def receive =  {
    case msg: Rumor =>
      log.info("Rcvd Rumor(id: {})", msg.id)

      val f = ask(breaker, msg).mapTo[String]
      f onComplete {
        case Success(secret: String) =>
          log.info("rumor {}: {}", msg.id, secret)
        case Failure(e) =>
          log.info("rumor {} was problematic: {}", msg.id, e.getMessage)
      }
  }
}
 
class Breaker(data: ActorRef) extends Actor with ActorLogging {
  import context.dispatcher
  private implicit val timeout = Timeout(2 seconds)
 
  val breaker =
    new ModCircuitBreaker(
      context.system.scheduler,
      maxFailures = 1,
      callTimeout = 1.second,
      resetTimeout = 1.minute).onOpen(notifyMeOnOpen())
 
  def notifyMeOnOpen(): Unit =
    log.warning("CircuitBreaker is now open, and will not close for one minute")
 
  def receive = {
    case msg: Rumor =>
      log.info("Rcvd Rumor(id: {})", msg.id)
      breaker.withModCircuitBreaker(ask(data, Get(msg.id)).mapTo[String]) pipeTo sender()
  }
}

class Data extends Actor with ActorLogging {
  val map = new HashMap[String, Any]
  def receive = {
    case msg: Get =>
      log.info("Rcvd Get(key: {})", msg.key)
      Thread.sleep(1050)
      map.get(msg.key) match {
        case Some(value) => sender() ! value
        case None        => sender() ! Status.Failure(KeyNotFound(msg.key))
      }
    case msg: Set =>
      log.info("Rcvd Set(key: {}, value: {})", msg.key, msg.value)
      map.put(msg.key, msg.value)
  }
}
