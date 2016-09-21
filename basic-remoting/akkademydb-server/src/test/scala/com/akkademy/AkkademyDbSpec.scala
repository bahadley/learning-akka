package com.akkademy

import akka.actor.ActorSystem
import akka.testkit.TestActorRef
import com.akkademy.messages._
import org.scalatest.{FunSpec, Matchers}


class AkkademyDbSpec extends FunSpec with Matchers {
  implicit val system = ActorSystem()

  private val actor = TestActorRef(new AkkademyDb)
  private val akkademyDb = actor.underlyingActor

  private val keys = Vector("one", "two")
  private val values = Vector("123", 123, 1.23)
  
  describe("Set(key, value)") {
    it("should set a key and value") {
      actor ! Set(keys(0), values(0)) 
      akkademyDb.map.get(keys(0)) should equal(Some(values(0)))
    }
  }

  describe("SetIfNotExists(key, value)") {
    it("should set a key and value for a non-existing key") {
      actor ! SetIfNotExists(keys(1), values(1)) 
      akkademyDb.map.get(keys(1)) should equal(Some(values(1)))
    }
  }

  describe("SetIfNotExists(key, value)") {
    it("should not set a value for an existing key") {
      actor ! SetIfNotExists(keys(1), values(2)) 
      akkademyDb.map.get(keys(1)) should equal(Some(values(1)))
    }
  }

  describe("Set(key, value)") {
    it("should set a value for an existing key") {
      actor ! Set(keys(1), values(2)) 
      akkademyDb.map.get(keys(1)) should equal(Some(values(2)))
    }
  }

  describe("Delete(key)") {
    it("should delete an existing key and value") {
      actor ! Delete(keys(1)) 
      akkademyDb.map.get(keys(1)) should equal(None)
    }
  }

  describe("Clear") {
    it("should result in an empty map") {
      actor ! Clear() 
      akkademyDb.map.isEmpty should equal(true)
    }
  }
}
