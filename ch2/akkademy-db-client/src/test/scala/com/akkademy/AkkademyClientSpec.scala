package com.akkademy

import org.scalatest.{FunSpecLike, Matchers}
import scala.concurrent.Await
import scala.concurrent.duration._
import scala.language.postfixOps
import com.akkademy.messages._


class AkkademyClientSpec extends FunSpecLike with Matchers {

  describe("akkademy-db-client") {

    val client = new AkkademyClient("127.0.0.1:2552")

    val timeout = 2 seconds

    it("should set a value and then get the same value") {
      val f1 = client.set("key-1", 123)
      val rs1 = Await.result(f1, timeout)

      val f2 = client.get("key-1")
      val rs2 = Await.result(f2, timeout)
      rs2 should equal(123)
    }

    it("should set a value and then delete the same value") {
      val f1 = client.set("key-2", "blah")
      val rs1 = Await.result(f1, timeout)

      val f2 = client.delete("key-2")
      val rs2 = Await.result(f2, timeout)
      rs2 should equal("blah")
    }

    it("should fail when key is not found for a get") {
      val f1 = client.get("r6*me@")
      intercept[KeyNotFoundException] {
        Await.result(f1, timeout)
      } 
    }

    it("should fail when key is not found for a delete") {
      val f1 = client.delete("-gaJ(n")
      intercept[KeyNotFoundException] {
        Await.result(f1, timeout)
      } 
    }

    it("should fail when sent an unexpected request") {
      val f1 = client.sendUnexpected 
      intercept[UnexpectedRequestException] {
        Await.result(f1, timeout)
      }
    }
  }
}
