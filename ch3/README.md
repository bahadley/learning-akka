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

* **ask-and-tell**:  shows the ask pattern in contrast with a tell exchange between **secrets** and **decrypter**.

* **ask-only**: replaces the tell exchange with the ask pattern.

* **ask-and-pipeto**: shows how a Future can be piped to an actor.

* **ask-and-replyto**: shows the ask replyTo pattern in place of the pipe pattern.

* **ask-with-await**: shows the ask pattern using `onComplete` rather than blocking threads with `Await`

***

Prerequisite:  

The Chapter 2 `akkademydb-messages` project is published locally (see [details](../ch2/akkademydb-messages/README.md))


Instructions for running examples:

1) Run `/akkademydb$ sbt publish-local` (see [details](akkademydb/README.md)).

2) Change into example directories and use `$ sbt run`

***

