package com.akkademy

import org.scalatest.{FunSpec, Matchers}
import scala.concurrent.Await
import scala.concurrent.duration._
import scala.language.postfixOps
import com.akkademy.messages._


class AkkademyDbClientSpec extends FunSpec with Matchers {

  private val serverAddr = "127.0.0.1:2552" 

  private val client = new AkkademyDbClient(serverAddr)

  private val timeout = 2 seconds

  private val keys = Vector(
    "key-0", "key-1", "key-2", 
    "key-3", "key-4", "r6*me@", 
    "-gaJ(n", "junk")

  private val values = Vector(
    123, 456, 789, 
    "blah", 123.45, true, 
    false)

  describe("set() and get()") {
    it("should add key/value and then get the same value back") {
      val f1 = client.set(keys(0), values(0))
      val rs1 = Await.result(f1, timeout)
      rs1 should equal(None)

      val f2 = client.get(keys(0))
      val rs2 = Await.result(f2, timeout)
      rs2 should equal(values(0))
    }
  }

  describe("set() and get()") {
    it("should return the old value if updating an existing key and get the new value") {
      val f1 = client.set(keys(1), values(1))
      val rs1 = Await.result(f1, timeout)
      rs1 should equal(None)

      val f2 = client.set(keys(1), values(2))
      val rs2 = Await.result(f2, timeout)
      rs2 should equal(Some(values(1)))

      val f3 = client.get(keys(1))
      val rs3 = Await.result(f3, timeout)
      rs3 should equal(values(2))
    }
  }

  describe("set(), delete(), and get()") {
    it("should add key/value, delete it, and then get a KeyNotFound") {
      val f1 = client.set(keys(2), values(3))
      val rs1 = Await.result(f1, timeout)
      rs1 should equal(None)

      val f2 = client.delete(keys(2))
      val rs2 = Await.result(f2, timeout)
      rs2 should equal(values(3))

      val f3 = client.get(keys(2))
      intercept[KeyNotFound] {
        Await.result(f3, timeout)
      }
    }
  }

  describe("setIfNotExists()") {
    it("should add key/value if the key does not exist") {
      val f1 = client.setIfNotExists(keys(3), values(4))
      val rs1 = Await.result(f1, timeout)
      rs1 should equal(values(4))

      val f2 = client.get(keys(3))
      val rs2 = Await.result(f2, timeout)
      rs2 should equal(values(4))
    }

    it("should not set a value for a key that already exists") {
      val f1 = client.set(keys(4), values(5))
      val rs1 = Await.result(f1, timeout)
      rs1 should equal(None)

      val f2 = client.setIfNotExists(keys(4), values(6))
      val rs2 = Await.result(f2, timeout)
      rs2 should equal(values(5))

      val f3 = client.get(keys(4))
      val rs3 = Await.result(f3, timeout)
      rs3 should equal(values(5))
    }
  }

  describe("get()") {
    it("should fail with KeyNotFound when key is not found") {
      val f1 = client.get(keys(5))
      intercept[KeyNotFound] {
        Await.result(f1, timeout)
      } 
    }
  }

  describe("delete()") {
    it("should fail with KeyNotFound when key is not found") {
      val f1 = client.delete(keys(6))
      intercept[KeyNotFound] {
        Await.result(f1, timeout)
      } 
    }
  }

  describe("sendUnexpected()") {
    it("should fail with UnexpectedMessage when sent a message") {
      val f1 = client.sendUnexpected(keys(7)) 
      intercept[UnexpectedMessage] {
        Await.result(f1, timeout)
      }
    }
  }

  describe("clear()") {
    it("should send a response") {
      val f1 = client.clear
      val rs1 = Await.result(f1, timeout)
      rs1 should not equal(None)
    }
  }
}
