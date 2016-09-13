Implements the AkkademyDb service in chapter 2.

***

Extra debug logging capability has been added to this version.  The project uses
the Simple Logging Facade for Java (SLF4J) ([slf4j.org](http://www.slf4j.org/))
and LOGBack ([logback.qos.ch](http://logback.qos.ch/)) as recommended in the Akka
docs.

Various debug switches have been enabled in the `application.conf`

***

Test with:

`$ sbt test`

Launch to allow remote client interaction with:

`$ sbt run` 
