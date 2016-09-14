package com.akkademy

import akka.actor.{ActorSelection, ActorSystem}
import akka.pattern.ask
import akka.util.Timeout
import com.akkademy.messages._
import scala.concurrent.Future
import scala.concurrent.duration._
import scala.language.postfixOps


class AkkademyClient(remoteAddress: String) {

  private implicit val timeout = Timeout(20 seconds)

  private val path = s"akka.tcp://akkademy@$remoteAddress/user/akkademy-db"
  private val akkademydb = ActorSystem("client").actorSelection(path)

  def set(key: String, value: Any): Future[Any] = {
    akkademydb ? SetRequest(key, value)
  }

  def get(key: String): Future[Any] = {
    akkademydb ? GetRequest(key)
  }
}