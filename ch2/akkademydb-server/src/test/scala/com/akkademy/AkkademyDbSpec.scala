package com.akkademy

import akka.actor.ActorSystem
import akka.testkit.TestActorRef
import com.akkademy.messages._
import org.scalatest.{FunSpecLike, Matchers}


class AkkademyDbSpec extends FunSpecLike with Matchers {
  implicit val system = ActorSystem()

  describe("akkademy-db-server") {
    describe("given a Set message"){
      it("should place key/value into map"){
        val actor = TestActorRef(new AkkademyDb)
        actor ! Set("key", "value")

        val akkademyDb = actor.underlyingActor
        akkademyDb.map.get("key") should equal(Some("value"))
      }
    }
  }
}
