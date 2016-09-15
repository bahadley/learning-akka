Implements the AkkademyDB server in chapter 2.

***

Test with:

`$ sbt test`

Launch for client interaction:

`$ sbt run` 

Shutdown with ctrl-C

***

Notes:

Extra debug logging capability has been added.  This project uses
the Simple Logging Facade for Java (SLF4J) ([slf4j.org](http://www.slf4j.org/))
and LOGBack ([logback.qos.ch](http://logback.qos.ch/)) as recommended by the Akka
docs.

Various debug switches have been enabled in the `application.conf` file.

Also note the use of ActorLogging mixin and LoggingReceive in `AkkademyDb.scala`. The
latter is necessary for the logging of received messages with `DEBUG` logging.
