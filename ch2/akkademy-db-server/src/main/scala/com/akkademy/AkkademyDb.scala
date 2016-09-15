package com.akkademy

import akka.actor.{Actor, ActorLogging, ActorSystem, Props, Status}
import akka.event.LoggingReceive
import com.akkademy.messages._
import scala.collection.mutable.HashMap
import org.slf4j.LoggerFactory

class AkkademyDb extends Actor with ActorLogging {

  val map = new HashMap[String, Any]

  def receive = LoggingReceive {

    case r: SetRequest =>
      log.info("Received SetRequest; key: [{}]; value: [{}]", r.key, r.value)
      map.put(r.key, r.value)
      sender ! Status.Success

    case r: GetRequest =>
      log.info("Received GetRequest; key: [{}]", r.key)
      map.get(r.key) match {
        case Some(value) => sender ! value 
        case None        => sender ! Status.Failure(new KeyNotFoundException(r.key))
      }

    case r: DeleteRequest =>
      log.info("Received DeleteRequest; key: [{}]", r.key)
      map.remove(r.key) match {
        case Some(value) => sender ! value 
        case None        => sender ! Status.Failure(new KeyNotFoundException(r.key))
      }

    case _ => sender ! Status.Failure(UnexpectedRequestException())
  }
}

object Main extends App {

  val log = LoggerFactory.getLogger(Main.getClass)

  val system = ActorSystem("akkademy")
  val actor = system.actorOf(Props[AkkademyDb], name = "akkademy-db")

  log.info("Actor started; has path: [{}]", actor.path)
}
