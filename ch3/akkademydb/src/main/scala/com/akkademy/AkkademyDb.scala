package com.akkademy

import akka.actor.{Actor, ActorLogging, Status}
import akka.event.LoggingReceive
import com.akkademy.messages._
import scala.collection.mutable.HashMap


class AkkademyDb extends Actor with ActorLogging {

  val map = new HashMap[String, Any]

  def receive = LoggingReceive {

    case msg: Get =>
      log.info("Rcvd Get(key: [{}])", msg.key)
      map.get(msg.key) match {
        case Some(value) => sender() ! value 
        case None        => sender() ! Status.Failure(KeyNotFound(msg.key))
      }

    case msg: Set =>
      log.info("Rcvd Set(key: [{}], value: [{}])", msg.key, msg.value)
      sender() ! map.put(msg.key, msg.value)

    case msg: SetIfNotExists =>
      log.info("Rcvd SetIfNotExists(key: [{}], value: [{}])", msg.key, msg.value)
      sender() ! map.getOrElseUpdate(msg.key, msg.value)

    case msg: Delete =>
      log.info("Rcvd Delete(key: [{}])", msg.key)
      map.remove(msg.key) match {
        case Some(value) => sender() ! value 
        case None        => sender() ! Status.Failure(KeyNotFound(msg.key))
      }

    case msg: Clear =>
      log.info("Rcvd Clear")
      map.clear 
      sender() ! Status.Success

    case _ => sender() ! Status.Failure(UnexpectedMessage())
  }
}
