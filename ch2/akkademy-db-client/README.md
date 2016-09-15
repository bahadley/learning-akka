Implements the AkkademyDb client in chapter 2.

***

Test with:

`$ sbt test`

***

Notes:

The code and configuration assume that the client and server will run on the same host.  

The client will need its own remote settings if different hosts are used.  See the troubleshooting
section for how this can be done.  Appropriate IP addresses will need to be configured in both
`application.conf` files and the hard-coded address in `AkkademyClientSpec.scala` updated. 

***

Troubleshooting:

Increase the Future timeout values used in the `AkkademyClient` and `AkkademyClientSpec` classes if the
client encounters **timeout errors**.  This may occur in memory constrained environments such as VMs
or containers.  It was observed that by default sbt, both ActorSystems, and associated JVMs required
over 900MB of RAM.  Latency will increase dramatically if swapping to disk occurs.  If necessary,
increase the timeouts to a large value such as 30 seconds to eliminate these issues while diagnosing
unexplained problems.

If **message delivery failures** occur, verify the correctness of the `remote.netty` settings in the 
server's `application.conf` file and the hard-coded remote address specified in `AkkademyClientSpec.scala`.  
The hard-coded address should match the server's `remote.netty` settings.  

Akka automatically configures the client to use a different subnet than the server's when both run on
the same host.  This avoids conflicts if both listen on the same port in the same subnet.  Consider 
adding an explicit remote setting to the client's `application.conf` file if messages are not being 
delivered.  For example:    

```
  ...

  remote {
    enabled-transports = ["akka.remote.netty.tcp"]
    netty.tcp {
      hostname = "127.0.0.1"
      port = 2553
    }
  }

  ...
```

Turn on DEBUG logging in the server's `application.conf` file if all else fails.

*** 

