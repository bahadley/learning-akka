package com.akkademy

import akka.actor.{Actor, ActorLogging, ActorSystem, Props, Status}
import akka.event.LoggingReceive
import com.akkademy.messages._
import scala.collection.mutable.HashMap
import org.slf4j.LoggerFactory

class AkkademyDb extends Actor with ActorLogging {

  val map = new HashMap[String, Any]

  def receive = LoggingReceive {

    case msg: Get =>
      log.info("Received Get; key: [{}]", msg.key)
      map.get(msg.key) match {
        case Some(value) => sender ! value 
        case None        => sender ! Status.Failure(KeyNotFound(msg.key))
      }

    case msg: Set =>
      log.info("Received Set; key: [{}]; value: [{}]", msg.key, msg.value)
      sender ! map.put(msg.key, msg.value)

    case msg: SetIfNotExists =>
      log.info("Received SetIfNotExists; key: [{}]; value: [{}]", msg.key, msg.value)
      sender ! map.getOrElseUpdate(msg.key, msg.value)

    case msg: Delete =>
      log.info("Received Delete; key: [{}]", msg.key)
      map.remove(msg.key) match {
        case Some(value) => sender ! value 
        case None        => sender ! Status.Failure(KeyNotFound(msg.key))
      }

    case _ => sender ! Status.Failure(UnexpectedMessage())
  }
}

object Main extends App {

  val log = LoggerFactory.getLogger(Main.getClass)

  val system = ActorSystem("akkademy-server")
  val actor = system.actorOf(Props[AkkademyDb], name = "akkademyDb")

  log.info("Actor started; has path: [{}]", actor.path)
}
