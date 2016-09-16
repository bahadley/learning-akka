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

  /**
   *  Returns the value associated with a key or KeyNotFound exception if
   *  key does not exist.
   */
  def get(key: String): Future[Any] = {
    akkademyDb ? Get(key)
  }

  /**
   *  Adds a new key/value pair and optionally returns previously bound 
   *  value. If the key already exists, it will be overridden by the new value. 
   */
  def set(key: String, value: Any): Future[Any] = {
    akkademyDb ? Set(key, value)
  }

  /**
   *  If given key is already exists, returns associated value,
   *  otherwise, stores key map and returns that value.
   */
  def setIfNotExists(key: String, value: Any): Future[Any] = {
    akkademyDb ? SetIfNotExists(key, value)
  }

  /* 
   *  Removes a key, returning the value associated previously with that 
   *  key as an option or KeyNotFound exception if key does not exist.
   */
  def delete(key: String): Future[Any] = {
    akkademyDb ? Delete(key)
  }

  /* 
   *  Removes all keys.
   */
  def clear(): Future[Any] = {
    akkademyDb ? Clear()
  }

  /*
   * Exercises unexpected message handling. 
   */ 
  def sendUnexpected(msg: String): Future[Any] = {
    akkademyDb ? msg 
  }
}
