package tpcdi 

import akka.actor.{Actor, ActorLogging, ActorSystem, Props}
import scala.concurrent.{Await, Future}
import scala.concurrent.duration.Duration
import slick.driver.H2Driver.api._

case class Add(in_id: String, in_name: String, in_sc_id: String)

object Main extends App {
  val system = ActorSystem("tpcdi")
  val inds = system.actorOf(Props[Industries], name = "industries")

  inds ! Add("AM", "Aerospace & Defense", "BM") 

  Thread.sleep(500)  // Increase if necessary
  system.terminate
}

class Industries extends Actor with ActorLogging {

  def insert(i: Add): DBIO[Int] =
      sqlu"insert into industry values (${i.in_id}, ${i.in_name}, ${i.in_sc_id})"

  val db = Database.forConfig("tpcdi")

  def receive = {
    case msg: Add =>

      val f: Future[_] = {
        db.run(insert(msg))
      }
      Await.result(f, Duration.Inf)
  }
}
