package com.akkademy.messages

case class Set(key: String, value: Any)
case class SetIfNotExists(key: String, value: Any)
case class Get(key: String)
case class Delete(key: String)

case class KeyNotFound(key: String) extends Exception
case class UnexpectedMessage() extends Exception
