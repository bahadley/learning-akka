Chapter 2:  Actors and Concurrency

***

Instructions for running AkkademyDB:

1) Follow the instructions in [akkademy-db-messages](akkademy-db-messages/README.md) to
publish the messages locally.

2) Launch the server as shown in [akkademy-db-server](akkademy-db-server/README.md).
The output should show that an Actor has been created.

3) Run the client tests as shown in [akkademy-db-client](akkademy-db-client/README.md)
within a separate terminal.  All the tests should succeed.

Troubleshooting ideas can be found in [akkademy-db-client](akkademy-db-client/README.md).  

***

Intruction from text for project homework:

* Add an atomic `SetIfNotExists` and Delete messages to the AkkademyDB App.

* If you need some sbt experience, move the messages to their own project and publish it.

* Complete Tests for the Client - we don't have tests for the failure cases such as getting
a missing message.  You should complete this.

***
