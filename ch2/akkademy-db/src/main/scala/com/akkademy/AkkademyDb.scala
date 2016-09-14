package com.akkademy

import akka.actor.{Actor, ActorLogging, ActorSystem, Props, Status}
import akka.event.LoggingReceive
import com.akkademy.messages._
import scala.collection.mutable.HashMap

class AkkademyDb extends Actor with ActorLogging {

  val map = new HashMap[String, Any]

  def receive = LoggingReceive {

    case m: SetRequest =>
      log.info("received SetRequest - key: {} value: {}", m.key, m.value)
      map.put(m.key, m.value)
      sender ! Status.Success

    case m: GetRequest =>
      log.info("received GetRequest - key: {}", m.key)
      map.get(m.key) match {
        case Some(value) => sender ! value 
        case None => sender ! Status.Failure(new KeyNotFoundException(m.key))
      }

    case _ => Status.Failure(new ClassNotFoundException)
  }
}

object Main extends App {
  ActorSystem("akkademy").actorOf(Props[AkkademyDb], name = "akkademy-db")
}
