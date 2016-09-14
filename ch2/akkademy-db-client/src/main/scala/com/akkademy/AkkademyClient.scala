package com.akkademy

import akka.actor.{ActorSelection, ActorSystem}
import akka.pattern.ask
import akka.util.Timeout
import com.akkademy.messages._
import scala.concurrent.Await
import scala.concurrent.duration._
import scala.language.postfixOps

class AkkademyClient(remoteAddress: String) {
  private implicit val timeout = Timeout(2 seconds)

  private val addr = s"akka.tcp://akkademy@$remoteAddress/user/akkademy-db"
  private val remoteDb = ActorSystem("client").actorSelection(addr)

  def set(key: String, value: Object) = {
    remoteDb ? SetRequest(key, value)
  }

  def get(key: String) = {
    remoteDb ? GetRequest(key)
  }
}

object Main {
  private val client = new AkkademyClient("127.0.0.1:2552")

  def main(args: Array[String]): Unit = {
    client.set("123", new Integer(123))

    val future = client.get("123")
    val rs = Await.result(future.mapTo[Integer], 10 seconds)
    assert(rs == 123)
  }
}
