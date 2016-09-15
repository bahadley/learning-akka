Implements the AkkademyDB client in chapter 2.

***

Test with:

`$ sbt test`

***

Notes:

The code and configuration assume that the client and server will run on the same host.  

The client will need its own remote settings if actual remote hosts are used.  See the troubleshooting
section for an example.  Appropriate IP addresses will need to be configured in the client and
server `application.conf` files, as well as the hard-coded value in `AkkademyDbClientSpec.scala`. 

***

Troubleshooting:

Increase the Future timeout values used in the `AkkademyDbClient` and `AkkademyDbClientSpec` classes if the
client encounters **timeout errors**.  This may occur in memory constrained environments such as VMs
or containers.  It was observed that by default sbt, both ActorSystems, and associated JVMs required
over 900MB of RAM.  Latency will increase dramatically if swapping to disk occurs.  If necessary,
increase the timeouts to a large value such as 30 seconds to eliminate these issues while diagnosing
unexplained problems.

If **message delivery failures** occur, verify the correctness of the `remote.netty` settings in the 
server's `application.conf` file and the hard-coded remote address value in `AkkademyDbClientSpec.scala`.  
The latter should match the server's `remote.netty` settings.  

Akka automatically configures the client's ActorSystem to use a different IP address than the server's 
when both run on the same host.  This avoids conflicts that would occur if both listened on the same IP 
and port number.  Consider adding an explicit remote setting to the client's `application.conf` file if
messages are not being delivered.  For example:    

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

