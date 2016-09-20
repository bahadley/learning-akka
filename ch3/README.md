Chapter 3:  Getting the Message Across 

***

Various message patterns are used to demonstrate communications between actors.

The following actors are used:

* **spy**: receives *Rumor* messages from the Main object.

* **secrets**: receives *Snoop* messages from **spy** and ultimately returns plaintext back to **spy**.

* **cache**: receives *Get* messages from **secrets** and returns ciphertext.

* **decrypter**: receives *Decrypt* messages from **secrets** and returns plaintext.

It is assumed that **decrypter** implements a long running CPU-bound process to decrypt ciphertext and
**cache** is I/0 bound.  These assumptions prompt the use of Futures within **spy** and **secrets**.

***

The following examples are intentionally self-contained so that all the code related to a pattern can be
easily read and modified.

* **ask-with-awaits** ([code](ask-with-awaits/src/main/scala/AskWithAwaits.scala)):
shows the ask pattern made simple but inefficient with blocking threads.

* **ask-without-awaits** ([code](ask-without-awaits/src/main/scala/AskWithoutAwaits.scala)):
shows the ask pattern without blocking using `onComplete` and failure handling.

* **replyto** ([code](replyto/src/main/scala/ReplyTo.scala)):
uses the replyTo pattern by including a **spy** ActorRef in the *Decrypt* message.

* **pipeto** ([code](pipeto/src/main/scala/PipeTo.scala)):
shows how a Future can be piped to an actor.

* **forward** ([code](forward/src/main/scala/Forward.scala)):
**forwarder** relays a *Rumor* message from **spy** to **secrets**, which responds back to **spy**.

* **self-message** ([code](self-message/src/main/scala/SelfMessage.scala)):
**secrets** sends a message to itself in order to separate **cache** and **decrypter** related tasks. 

* **variety** ([code](variety/src/main/scala/Variety.scala)):
shows different styles of handling Futures and exceptions (i.e., `onComplete`, `recover`, `foreach`)

***

To run examples:

Change into any example directory and use `$ sbt run`

***

