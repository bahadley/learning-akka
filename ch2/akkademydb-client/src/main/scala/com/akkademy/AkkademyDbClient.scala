package com.akkademy

import akka.actor.{ActorSelection, ActorSystem}
import akka.pattern.ask
import akka.util.Timeout
import com.akkademy.messages._
import scala.concurrent.Future
import scala.concurrent.duration._
import scala.language.postfixOps


class AkkademyDbClient(remoteAddress: String) {

  private implicit val timeout = Timeout(2 seconds)

  private val path = s"akka.tcp://akkademydb-server@$remoteAddress/user/akkademydb"
  private val akkademyDb = ActorSystem("akkademydb-client").actorSelection(path)

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

  // Used to test sending unexpected requests 
  def sendUnexpected(msg: String): Future[Any] = {
    akkademyDb ? msg 
  }
}
