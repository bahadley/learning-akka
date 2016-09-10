package ch1

import akka.actor.ActorSystem
import akka.testkit.TestActorRef
import org.scalatest.{BeforeAndAfterEach, FunSpecLike, Matchers}

class StickyStringSpec extends FunSpecLike with Matchers with BeforeAndAfterEach {

  implicit val system = ActorSystem()
  
  val msgs = Vector("foo", "bar")

  describe("store last string message") {
    describe("send one string") {
      it("should store the string") {
        val actor = TestActorRef(new StickyString)
        actor ! msgs.head 
        actor.underlyingActor.lastString should equal(Some(msgs.head))
      }
    }

    describe("send two strings") {
      it("should store the last string") {
        val actor = TestActorRef(new StickyString)
        msgs.foreach {m => actor ! m}
        actor.underlyingActor.lastString should equal(Some(msgs.last))
      }
    }
  }
}
