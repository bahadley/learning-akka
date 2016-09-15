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
client encounters **timeout errors**.  This may occur in memory constrained environments such as VMs.  It was
observed that by default sbt, both ActorSystems, and associated JVMs required over 900MB of RAM.  Latency 
will increase dramatically if swapping to disk occurs.  

If **message delivery failures** occur, verify the correctness of the `remote.netty` settings in the 
server's `application.conf` file and the hard-coded remote address specified in `AkkademyClientSpec.scala`.  
The hard-coded address should match the server's `remote.netty` settings.  

Akka automatically configures the client to use a different subnet than the one used by the server when 
both run on the same host.  This avoids conflicts that would occur if they both listened on the same port 
within the same subnet.  Consider adding an explicit remote setting to the client's `application.conf` 
file if messages are not be delivered.  For example:    

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

