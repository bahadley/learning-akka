package com.akkademy

import akka.actor.ActorSystem
import akka.testkit.TestActorRef
import com.akkademy.messages.SetRequest
import org.scalatest.{FunSpecLike, Matchers}


class AkkademyDbSpec extends FunSpecLike with Matchers {
  implicit val system = ActorSystem()

  describe("akkademyDb") {
    describe("given SetRequest"){
      it("should place key/value into map"){
        val actor = TestActorRef(new AkkademyDb)
        actor ! SetRequest("key", "value")

        val akkademyDb = actor.underlyingActor
        akkademyDb.map.get("key") should equal(Some("value"))
      }
    }
  }
}
