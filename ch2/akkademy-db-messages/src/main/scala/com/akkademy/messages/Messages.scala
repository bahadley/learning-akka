package com.akkademy.messages

case class SetRequest(key: String, value: Any)
case class GetRequest(key: String)

case class KeyNotFoundException(key: String) extends Exception
