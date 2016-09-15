package com.akkademy

import org.scalatest.{FunSpecLike, Matchers}
import scala.concurrent.Await
import scala.concurrent.duration._
import scala.language.postfixOps
import com.akkademy.messages._


class AkkademyDbClientSpec extends FunSpecLike with Matchers {

  private val serverAddr = "127.0.0.1:2552" 

  describe("akkademydb-client") {

    val client = new AkkademyDbClient(serverAddr)

    val timeout = 2 seconds

    it("should set a value and then get the same value") {
      val k = "key-1"
      val v = 123 

      val f1 = client.set(k, v)
      val rs1 = Await.result(f1, timeout)
      rs1 should equal(None)

      val f2 = client.get(k)
      val rs2 = Await.result(f2, timeout)
      rs2 should equal(v)
    }

    it("should set a value and then delete it") {
      val k = "key-2"
      val v = "blah"

      val f1 = client.set(k, v)
      val rs1 = Await.result(f1, timeout)
      rs1 should equal(None)

      val f2 = client.delete(k)
      val rs2 = Await.result(f2, timeout)
      rs2 should equal(v)

      val f3 = client.get(k)
      intercept[KeyNotFound] {
        Await.result(f3, timeout)
      }
    }

    it("should set a value that does not already exist (using SetIfNotExists)") {
      val k = "key-3"
      val v = 123.45 

      val f1 = client.setIfNotExists(k, v)
      val rs1 = Await.result(f1, timeout)
      rs1 should equal(v)

      val f2 = client.get(k)
      val rs2 = Await.result(f2, timeout)
      rs2 should equal(v)
    }

    it("should not set a value that already exists (using SetIfNotExists)") {
      val k = "key-4"
      val v1 = true 
      val v2 = false 

      val f1 = client.set(k, v1)
      val rs1 = Await.result(f1, timeout)
      rs1 should equal(None)

      val f2 = client.setIfNotExists(k, v2)
      val rs2 = Await.result(f2, timeout)
      rs2 should equal(v1)

      val f3 = client.get(k)
      val rs3 = Await.result(f3, timeout)
      rs3 should equal(v1)
    }

    it("should fail when key is not found for a get") {
      val f1 = client.get("r6*me@")
      intercept[KeyNotFound] {
        Await.result(f1, timeout)
      } 
    }

    it("should fail when key is not found for a delete") {
      val f1 = client.delete("-gaJ(n")
      intercept[KeyNotFound] {
        Await.result(f1, timeout)
      } 
    }

    it("should fail when sent an unexpected message") {
      val f1 = client.sendUnexpected("junk") 
      intercept[UnexpectedMessage] {
        Await.result(f1, timeout)
      }
    }
  }
}
