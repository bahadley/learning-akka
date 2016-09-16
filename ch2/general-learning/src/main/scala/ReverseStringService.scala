package reverseString

import akka.actor.{Actor, ActorSystem, Props, Status}
import akka.pattern.ask
import akka.util.Timeout
import scala.concurrent.Future
import scala.concurrent.duration._
import scala.language.postfixOps


object ReverseStringService {

  private class ReverseString extends Actor {
    def receive = {
      case s: String => sender() ! s.reverse
      case _ => sender() ! Status.Failure(new Exception("unexpected message"))
    }
  }

  private implicit val timeout = Timeout(1 second)

  private val system = ActorSystem("StringServices") 
  private val actor = system.actorOf(Props[ReverseString])

  def reverse(s: Any): Future[String] = {
    (actor ? s).mapTo[String]
  }
}
