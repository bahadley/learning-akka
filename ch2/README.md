Chapter 2:  Actors and Concurrency

***

AkkademyDB is a simple in-memory key/value store.  It demonstrates the use of an Actor, the Ask
pattern, and Futures. 

***

Instructions for running AkkademyDB:

1) Run `/akkademydb-messages$ sbt publish-local` (see [details](akkademydb-messages/README.md)).

2) Run `/akkademydb-server$ sbt run` (see [details](akkademydb-server/README.md)).

3) Using a different terminal, run `/akkademydb-client$ sbt test` (see [details](akkademydb-client/README.md)).

Troubleshooting ideas can be found in [akkademydb-client](akkademydb-client/README.md).  

***

Intruction from text for project homework:

* Add an atomic `SetIfNotExists` and Delete messages to the AkkademyDB App.

* If you need some sbt experience, move the messages to their own project and publish it.

* Complete Tests for the Client - we don't have tests for the failure cases such as getting
a missing message.  You should complete this.

***
