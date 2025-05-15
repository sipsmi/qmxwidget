# qmxwidget

![1.00](qmx.png)

This little widget exploits the latest QMX CAT codes ( LCD read, power, swr ) and supplements the control FLRIG curently has.
It will eventually support two modes, direct and via Flrig, but for now only XML-RPC via Flrig

![1.00](arch2.png)

## Running

It may be run from a composite JAR file but requires a configuration file:

```
java -jar qmx.jar   config.json 
```

Remember to change the parameters to suit!

## Configuration

Parameters are input as a configuration file in JSON format:

```
{
	"Callsign": "G0FOZ",
	"XMLRPCaddress": "localhost",
	"XMLRPCport": "12345"
}
```

Configuration parameters

| Parameter     | Example value | Description                                                                                               |
| :------------ | :------------ | :-------------------------------------------------------------------------------------------------------- |
| Callsign      | G0FOZ         | Your callsign - will be used for the CQ button and other macros.                                          |
| XMLRPCaddress | localhost     | The IP address or name of the FLRig instance RPC server.  Other examples 127.0.0.1   myflrig.mydomain.com |
| XMLRPCport    | 12345         | IP port number of the FLRIg instance - defaults to 12345                                                  |

<br />
