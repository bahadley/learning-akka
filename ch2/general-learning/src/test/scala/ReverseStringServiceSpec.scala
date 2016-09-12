package reverseString 

import org.scalatest.{FunSpecLike, Matchers}
import scala.concurrent.{Await, Future}
import scala.concurrent.duration._
import scala.language.postfixOps


class ReverseStringServiceSpec extends FunSpecLike with Matchers {

  describe("With a ReverseStringService object") {

    import scala.concurrent.ExecutionContext.Implicits.global

    it("should reverse a String") {
      val s = "blah"
      val future = ReverseStringService.reverse(s)
      val rs = Await.result(future, 1 second) 
      rs should equal(s.reverse)
    }

    it("should fail with a non-String type") {
      val future = ReverseStringService.reverse(55)
      intercept[Exception] {
        Await.result(future, 1 second)
      }
    } 

    it("should reverse all Strings in a List") {
      val ls = List("foo", "bar", "baz")

      val listOfFutures: List[Future[String]] = 
        ls.map(x => ReverseStringService.reverse(x))
      val futureOfList: Future[List[String]] = 
        Future.sequence(listOfFutures.map(f => f.recover{case _: Exception => ""}))

      val result = Await.result(futureOfList, 1 second)
      result should equal(ls.map(x => x.reverse))
    }
  }
}
