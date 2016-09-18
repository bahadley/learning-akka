Chapter 3:  Getting the Message Across 

***

Various message patterns are used to demonstrate communications between actors.

The following actors are used:

* **spy**: receives *Rumor* messages from the Main object.

* **secrets**: receives *Snoop* messages from **spy** and ultimately returns plaintext back to **spy**.

* **cache**: receives *Get* messages from **secrets** and returns ciphertext.

* **decrypter**: receives *DecryptCipherText* messages from **secrets** and returns plaintext.


***

Examples:

* **ask-and-tell** ([code](ask-and-tell/src/main/scala/AskAndTell.scala)):
shows the ask pattern in contrast with a tell exchange between **secrets** and **decrypter**.

* **ask-only** ([code](ask-only/src/main/scala/AskOnly.scala)):
replaces the tell exchange with the ask pattern.

* **ask-and-pipeto** ([code](ask-and-pipeto/src/main/scala/AskAndPipeTo.scala)):
shows how a Future can be piped to an actor.

* **ask-and-replyto** ([code](ask-and-replyto/src/main/scala/AskAndReplyTo.scala)):
shows the ask replyTo pattern in place of the pipe pattern.

* **ask-without-await** ([code](ask-without-await/src/main/scala/AskWithoutAwait.scala)):
shows the ask pattern using `onComplete` rather than blocking threads with `Await`

***

Prerequisite:  

The Chapter 2 `akkademydb-messages` project is published locally (see [details](../ch2/akkademydb-messages/README.md))


Instructions for running examples:

1) Run `/akkademydb$ sbt publish-local` (see [details](akkademydb/README.md)).

2) Change into example directories and use `$ sbt run`

***

