package com.akkademy

import akka.actor.{Actor, ActorLogging, ActorSystem, Props, Status}
import akka.event.LoggingReceive
import com.akkademy.messages._
import scala.collection.mutable.HashMap

class AkkademyDb extends Actor with ActorLogging {
  val map = new HashMap[String, Object]

  def receive = LoggingReceive {
    case SetRequest(key, value) =>
      log.info("received SetRequest - key: {} value: {}", key, value)
      map.put(key, value)
      sender ! Status.Success
    case GetRequest(key) =>
      log.info("received GetRequest - key: {}", key)
      val response: Option[Object] = map.get(key)
      response match{
        case Some(x) => sender ! x
        case None => sender ! Status.Failure(new KeyNotFoundException(key))
      }
    case _ => Status.Failure(new ClassNotFoundException)
  }
}

object Main extends App {
  ActorSystem("akkademy").actorOf(Props[AkkademyDb], name = "akkademy-db")
}
