Chapter 2:  Actors and Concurrency

***

Instructions for running AkkademyDB:

1) Run `$ sbt publish-local` in akkademydb-messages (see [details](akkademydb-messages/README.md)).

2) Run `$ sbt run` in akkademydb-server (see [details](akkademydb-server/README.md)).

3) Using a different terminal, run `$ sbt test` in akkademydb-client (see [details](akkademydb-client/README.md)).

Troubleshooting ideas can be found in [akkademydb-client](akkademydb-client/README.md).  

***

Intruction from text for project homework:

* Add an atomic `SetIfNotExists` and Delete messages to the AkkademyDB App.

* If you need some sbt experience, move the messages to their own project and publish it.

* Complete Tests for the Client - we don't have tests for the failure cases such as getting
a missing message.  You should complete this.

***
