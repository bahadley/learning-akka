package com.akkademy

import akka.actor.{ActorSelection, ActorSystem}
import akka.pattern.ask
import akka.util.Timeout
import com.akkademy.messages._
import scala.concurrent.Future
import scala.concurrent.duration._
import scala.language.postfixOps


class AkkademyClient(remoteAddress: String) {

  private implicit val timeout = Timeout(2 seconds)

  private val path = s"akka.tcp://akkademy-server@$remoteAddress/user/akkademyDb"
  private val akkademyDb = ActorSystem("akkademy-client").actorSelection(path)

  def get(key: String): Future[Any] = {
    akkademyDb ? Get(key)
  }

  def set(key: String, value: Any): Future[Any] = {
    akkademyDb ? Set(key, value)
  }

  def setIfNotExists(key: String, value: Any): Future[Any] = {
    akkademyDb ? SetIfNotExists(key, value)
  }

  def delete(key: String): Future[Any] = {
    akkademyDb ? Delete(key)
  }

  // Use to test sending unexpected requests 
  def sendUnexpected: Future[Any] = {
    akkademyDb ? "junk"
  }
}
