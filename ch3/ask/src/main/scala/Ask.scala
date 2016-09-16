package ask

import akka.actor.{Actor, ActorLogging, ActorSystem, Props, Status}
import akka.event.LoggingReceive
import com.akkademy.messages._
import com.akkademy.AkkademyDb

case class Decrypt(text: String)

class Decrypter extends Actor {
  def receive = {
    case msg: Decrypt =>
  }
}

object Main extends App {

  val system = ActorSystem("ask")
  val source = system.actorOf(Props[AkkademyDb], name = "source")
  val cache = system.actorOf(Props[AkkademyDb], name = "cache")

}
