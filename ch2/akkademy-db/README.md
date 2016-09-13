Implements the AkkademyDb service in chapter 2.

***

Extra debug logging capability has been added.  The project uses
the Simple Logging Facade for Java (SLF4J) ([slf4j.org](http://www.slf4j.org/))
and LOGBack ([logback.qos.ch](http://logback.qos.ch/)) as is recommended in the Akka
docs.

Various debug switches have been enabled in the `application.conf` file.

Also note the use of ActorLogging mixin and LoggingReceive in `AkkademyDb.scala`. The
latter is necessary for logging of received messages.

***

Test with:

`$ sbt test`

Launch to allow client interaction:

`$ sbt run` 
