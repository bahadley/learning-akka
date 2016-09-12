package akkademy 

import akka.actor.Actor
import akka.event.Logging
import scala.collection.mutable.HashMap
import akkademy.messages.SetRequest

class AkkademyDb extends Actor {

  val map = new HashMap[String, Object]
  val log = Logging(context.system, this)

  def receive = {
    case SetRequest(key, value) => {
      log.info("received SetRequest - key: {} value: {}", key, value);
      map.put(key, value)
    }
    case o => log.info("received unexpected message: {}", o) 
  }
}
