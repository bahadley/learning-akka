package pong

import akka.actor.{ActorSystem, Props}
import akka.pattern.ask
import akka.util.Timeout
import org.scalatest.{FunSpecLike, Matchers}
import scala.concurrent.Await
import scala.concurrent.duration._
import scala.language.postfixOps

class AskExamplesTest extends FunSpecLike with Matchers {

  implicit val timeout = Timeout(5 seconds)

  val pongActor = ActorSystem().actorOf(Props[PongActor])

  describe("Pong actor") {
    it("should respond with Pong") {
      val future = pongActor ? "Ping"  // uses the implicit timeout
      val result = Await.result(future.mapTo[String], 1 second)
      assert(result == "Pong")
    }
    it("should fail on unexpected message") {
      val future = pongActor ? "284garBage,<5"
      intercept[Exception] {
        Await.result(future.mapTo[String], 1 second)
      }
    }
  }
}
