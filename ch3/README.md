Chapter 3:  Getting the Message Across 

***

Various message patterns are used to demonstrate communications between actors.

The following actors are used:

* **spy**: receives *Rumor* messages from the Main object.

* **secrets**: receives *Snoop* messages from **spy** and ultimately returns plaintext back to **spy**.

* **cache**: receives *Get* messages from **secrets** and returns ciphertext.

* **decrypter**: receives *Decrypt* messages from **secrets** and returns plaintext.


***

Examples:

* **ask-with-awaits** ([code](ask-with-awaits/src/main/scala/AskWithAwaits.scala)):
shows the ask pattern made simple but inefficient with blocking threads.

* **ask-without-awaits** ([code](ask-without-awaits/src/main/scala/AskWithoutAwaits.scala)):
shows the ask pattern without blocking using `onComplete` and failure handling.

* **replyto** ([code](replyto/src/main/scala/ReplyTo.scala)):
shows the ask replyTo pattern.

* **ask-and-pipeto** ([code](ask-and-pipeto/src/main/scala/AskAndPipeTo.scala)):
shows how a Future can be piped to an actor.

* **forward** ([code](forward/src/main/scala/Forward.scala)):
uses a **forwarder** actor that forwards a *Rumor* message from **spy** to **secrets**.


***

Prerequisite:  

The Chapter 2 `akkademydb-messages` project is published locally (see [details](../ch2/akkademydb-messages/README.md))


Instructions for running examples:

1) Run `/akkademydb$ sbt publish-local` (see [details](akkademydb/README.md)).

2) Change into example directories and use `$ sbt run`

***

