Implements the AkkademyDb client in chapter 2.

***

The client specifies a unique `remote.netty.tcp.port` setting in this project's `application.conf` file. 
This is required if the AkkademyDb server and client are running on the same host.

If failures occur, first verify the correctness of the `remote.netty` settings in the client and server 
`application.conf` files and the hard-coded remote address in `AkkademyClientSpec.scala`.  It should
correspond to the server's `remote.netty` settings.

Also note the large Timeout values used in `AkkademyClient` and `AkkademyClientSpec`.  This was required
to prevent Future timeout errors from occurring during tests.

***

Test with:

`$ sbt test`
