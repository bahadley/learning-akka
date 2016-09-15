Implements the AkkademyDb client in chapter 2.

***

Test with:

`$ sbt test`

***

Notes:

The client specifies a `remote.netty.tcp.port` in this project's `application.conf` file.  This will be
used by the client's own ActorSystem.  A port different than the server's will be required if the client 
is running on the same host as the server.

***

Troubleshooting:

If **message delivery failures** occur, verify the correctness of the `remote.netty` settings in the 
client and server `application.conf` files and the hard-coded remote address in `AkkademyClientSpec.scala`.  
The hard-coded address should match the server's `remote.netty` settings.

Increase the Future timeout values used in the `AkkademyClient` and `AkkademyClientSpec` classes if the
client encounters **timeout errors**.  This may occur in memory constrained environments such as VMs.  It was
observed that sbt, both ActorSystems, and associated JVMs required over 900MB of RAM.  Latency will increase
dramatically if swapping to disk occurs.  
