package com.akkademy

import org.scalatest.{FunSpecLike, Matchers}
import scala.concurrent.Await
import scala.concurrent.duration._
import scala.language.postfixOps
import com.akkademy.messages._


class AkkademyClientSpec extends FunSpecLike with Matchers {

  val client = new AkkademyClient("127.0.0.1:2552")

  describe("akkademyDbClient") {
    it("should set a value") {
        val f1 = client.set("123", 456)
        val rs1 = Await.result(f1, 20 seconds)

        val f2 = client.get("123")
        val rs2 = Await.result(f2, 20 seconds)
        rs2 should equal(456)
    }
  }
}
