package reverseString 

import org.scalatest.{FunSpecLike, Matchers}
import scala.concurrent.{Await, Future}
import scala.concurrent.duration._
import scala.language.postfixOps


class ReverseStringServiceSpec extends FunSpecLike with Matchers {

  describe("With a ReverseStringService object") {

    import scala.concurrent.ExecutionContext.Implicits.global

    it("should reverse a String") {
      val future = ReverseStringService.reverse("blah")
      val rs = Await.result(future, 1 second) 
      rs should equal("halb")
    }

    it("should fail with a non-String type") {
      val future = ReverseStringService.reverse(1234)
      intercept[Exception] {
        Await.result(future, 1 second)
      }
    } 

    it("should reverse all Strings in a List and gracefully handle non-String types") {
      val listOfFutures: List[Future[String]] = 
        List("foo", "bar", "-u2*", 123).map(x => ReverseStringService.reverse(x))
      val futureOfList: Future[List[String]] = 
        Future.sequence(listOfFutures.map(f => f.recover{case _: Exception => ""}))
      val rs = Await.result(futureOfList, 1 second)
      rs should equal(List("oof", "rab", "*2u-", ""))
    }
  }
}
