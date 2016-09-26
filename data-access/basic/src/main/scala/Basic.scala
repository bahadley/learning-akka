package basic

import akka.actor.{Actor, ActorLogging, ActorSystem, Props}
import akka.util.Timeout
import scala.concurrent.duration._
import scala.concurrent.{Await, Future}
import scala.language.postfixOps
import slick.driver.H2Driver.api._



object Main extends App {
  val system = ActorSystem("basic")
  val cafe = system.actorOf(Props[Cafe], name = "cafe")

  cafe ! "Create"

  Thread.sleep(500)  // Increase if necessary
  system.terminate
}


class Cafe extends Actor with ActorLogging {
  import scala.concurrent.ExecutionContext.Implicits.global

  private implicit val timeout = Timeout(2 seconds)

  // Definition of the SUPPLIERS table
  class Suppliers(tag: Tag) extends Table[(Int, String, String, String, String, String)](tag, "SUPPLIERS") {
    def id = column[Int]("SUP_ID", O.PrimaryKey) // This is the primary key column
    def name = column[String]("SUP_NAME")
    def street = column[String]("STREET")
    def city = column[String]("CITY")
    def state = column[String]("STATE")
    def zip = column[String]("ZIP")
    // Every table needs a * projection with the same type as the table's type parameter
    def * = (id, name, street, city, state, zip)
  }
  val suppliers = TableQuery[Suppliers]

  // Definition of the COFFEES table
  class Coffees(tag: Tag) extends Table[(String, Int, Double, Int, Int)](tag, "COFFEES") {
    def name = column[String]("COF_NAME", O.PrimaryKey)
    def supID = column[Int]("SUP_ID")
    def price = column[Double]("PRICE")
    def sales = column[Int]("SALES")
    def total = column[Int]("TOTAL")
    def * = (name, supID, price, sales, total)
    // A reified foreign key relation that can be navigated to create a join
    def supplier = foreignKey("SUP_FK", supID, suppliers)(_.id)
  }
  val coffees = TableQuery[Coffees]


  def receive =  {
    case "Create" =>

      val db = Database.forConfig("h2mem1")
      try {

        val setup = DBIO.seq(
          // Create the tables, including primary and foreign keys
          (suppliers.schema ++ coffees.schema).create,

          // Insert some suppliers
          suppliers += (101, "Acme, Inc.",      "99 Market Street", "Groundsville", "CA", "95199"),
          suppliers += ( 49, "Superior Coffee", "1 Party Place",    "Mendocino",    "CA", "95460"),
          suppliers += (150, "The High Ground", "100 Coffee Lane",  "Meadows",      "CA", "93966"),
          // Equivalent SQL code:
          // insert into SUPPLIERS(SUP_ID, SUP_NAME, STREET, CITY, STATE, ZIP) values (?,?,?,?,?,?)

          // Insert some coffees (using JDBC's batch insert feature, if supported by the DB)
          coffees ++= Seq(
            ("Colombian",         101, 7.99, 0, 0),
            ("French_Roast",       49, 8.99, 0, 0),
            ("Espresso",          150, 9.99, 0, 0),
            ("Colombian_Decaf",   101, 8.99, 0, 0),
            ("French_Roast_Decaf", 49, 9.99, 0, 0)
          )
          // Equivalent SQL code:
          // insert into COFFEES(COF_NAME, SUP_ID, PRICE, SALES, TOTAL) values (?,?,?,?,?)
        )
        val setupFuture = db.run(setup)

        Await.result(setupFuture, timeout.duration)

        // Read all coffees and print them to the console
        println("Coffees:")
        db.run(coffees.result).map(_.foreach {
          case (name, supID, price, sales, total) =>
          log.debug("  " + name + "\t" + supID + "\t" + price + "\t" + sales + "\t" + total)
        })
        // Equivalent SQL code:
        // select COF_NAME, SUP_ID, PRICE, SALES, TOTAL from COFFEES
      } finally db.close

    case "Read" =>
      val db = Database.forConfig("h2mem1")
      try {
        // Read all coffees and print them to the console
        println("Coffees:")
        db.run(coffees.result).map(_.foreach {
          case (name, supID, price, sales, total) =>
          println("  " + name + "\t" + supID + "\t" + price + "\t" + sales + "\t" + total)
        })
        // Equivalent SQL code:
        // select COF_NAME, SUP_ID, PRICE, SALES, TOTAL from COFFEES
    } finally db.close

  }
}
