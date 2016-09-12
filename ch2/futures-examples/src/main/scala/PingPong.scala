package pong 

import akka.actor.{Actor, Status}

class PongActor extends Actor {

  def receive = {
    case "Ping" => sender() ! "Pong" 
    case _ => sender() ! Status.Failure(new Exception("unexpected message"))
  }
}
