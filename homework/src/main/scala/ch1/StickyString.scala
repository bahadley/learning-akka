package ch1

import akka.actor.{Actor, ActorLogging}

class StickyString extends Actor with ActorLogging {

  var lastString = Option.empty[String]

  def receive = {
    case s: String => 
      log.info("string message received: {}", s)
      lastString = Option(s)
    case m => 
      log.info("unexpected message received: {}", m)
  }
}
