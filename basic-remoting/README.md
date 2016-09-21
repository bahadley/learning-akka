Chapter 2:  Actors and Concurrency

***

AkkademyDB is a simple in-memory key/value store.  It demonstrates:

* Interaction between an Actor and non-Actor client.

* Akka remoting.

* Ask pattern with Futures.

***

Instructions for running AkkademyDB:

1) Run `/akkademydb-messages$ sbt publish-local` (see [details](akkademydb-messages/README.md)).

2) Run `/akkademydb-server$ sbt run` (see [details](akkademydb-server/README.md)).

3) Using a different terminal, run `/akkademydb-client$ sbt test` (see [details](akkademydb-client/README.md)).

Troubleshooting ideas can be found in [akkademydb-client](akkademydb-client/README.md).  

***
