package pong

import akka.actor.{ActorSystem, Props}
import akka.pattern.ask
import akka.util.Timeout

import org.scalatest.{FunSpecLike, Matchers}

import scala.concurrent.{Await, Future, Promise}
import scala.concurrent.duration._
import scala.language.postfixOps


class FutureExamples extends FunSpecLike with Matchers {

  // Set a timeout for the futures.
  implicit val timeout = Timeout(5 seconds)

  val pongActor = ActorSystem().actorOf(Props[PongActor])


  describe("Uses Akka asynchronous API with blocking tests (i.e., Await.result())") {

    it("should respond with Pong (p. 49)") {
      val future = pongActor ? "Ping"  // uses the implicit timeout
      val result = Await.result(future.mapTo[String], 1 second)
      assert(result == "Pong")
    }

    it("should fail on unexpected message (p.49)") {
      val future = pongActor ? "284garBage,<5"
      intercept[Exception] {
        Await.result(future.mapTo[String], 1 second)
      }
    }
  }


  describe("Uses Scala Futures/Promises API with non-blocking and blocking tests"){

    // Import Scala's out of the box ExecutionContext for a global static thread pool. 
    import scala.concurrent.ExecutionContext.Implicits.global

    // Function used to simplify tests.  Avoids verbose code like: 
    //   ((pongActor ? "Ping").mapTo[String]).onSuccess( ...   
    def askPong(message: String): Future[String] = (pongActor ? message).mapTo[String]


    it("should print to console (uses onSuccess(), p. 52)"){
      askPong("Ping").onSuccess({
        case x: String => println("replied with: " + x)
      })
      Thread.sleep(100)  // Thread.sleep() should only be used in tests 
    }

    it("should transform result (uses map(), p. 53)"){
      val f: Future[Char] = askPong("Ping").map(x => x.charAt(0))
      val c = Await.result(f, 1 second)
      c should equal('P')
    }

    it("should transform result within a chained asynchronous operation (uses flatMap(), p. 54)"){
      // Sends "Ping".  Gets back "Pong".  Then sends "Ping" again.
      val f: Future[String] = askPong("Ping").flatMap(x => {
        assert(x == "Pong")
        askPong("Ping")
      })
      val c = Await.result(f, 1 second)
      c should equal("Pong")
    }

    it("should print to console (uses onFailure(), p. 55)"){
      askPong("causeError").onFailure{
        case _: Exception => println("exception handled")
      }
    }

    it("should verify failure with assertion (uses onFailure() and Promise, p. 55)"){
      val res = Promise()
      askPong("causeError").onFailure{
        case _: Exception =>
          res.failure(new Exception("failed!"))
      }

      intercept[Exception]{
        Await.result(res.future, 1 second)
      }
    }

    it("should recover on failure (uses recover(), p. 56)"){
      val f = askPong("causeError").recover({
        case _: Exception => "default"
      })

      val result = Await.result(f, 1 second)
      result should equal("default")
    }

    it("should recover on failure with an async retry (uses recoverWith(), p. 56)"){
      val f = askPong("causeError").recoverWith({
        case _: Exception => askPong("Ping")
      })

      val result = Await.result(f, 1 second)
      result should equal("Pong")
    }

    it("should chain together multiple operations (uses flatMap() and recover(), p. 57)"){
      val f = askPong("Ping").flatMap(x => askPong("Ping" + x)).recover({
        case _: Exception => "There was an error"
      })

      val result = Await.result(f, 1 second)
      result should equal("There was an error")
    }

    it("should be handled with for comprehension (shows combining futures, p. 58)"){
      val f1 = Future{4}
      val f2 = Future{5}

      val futureAddition =
        for{
          res1 <- f1
          res2 <- f2
        } yield res1 + res2
      val additionResult = Await.result(futureAddition, 1 second)
      assert(additionResult == 9)
    }

    it("should handle a list of futures"){
      val listOfFutures: List[Future[String]] = 
        List("Ping", "Ping", "failure").map(x => askPong(x))

      // Flip the types of List[Future] to a Future[List[String]] so the result it easier to work with
      val futureOfList: Future[List[String]] = 
        Future.sequence(listOfFutures.map(f => f.recover{case _: Exception => ""}))

      val result = Await.result(futureOfList, 1 second)
      result should equal(List("Pong", "Pong", ""))
    }
  }
}
