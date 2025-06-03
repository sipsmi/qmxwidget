# qmxwidget

![1.00](qmx.png)

This little widget exploits the latest QMX CAT codes ( LCD read, power, swr ) and supplements the control FLRIG currently has.
It will eventually support two modes, direct and via Flrig, but for now only XML-RPC via Flrig.

The code has been modfied to send frequency data to SDR++ program via its rigctld emulation server.    This therefore adds a bandscope to the QMX.  If this is enbaled it sends commands to the QMX to ensure audio card is in I/Q mode.  Instructions to configure SDR++ to follow\...

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
	"XMLRPCport": "12345",
    "rigctldAddress": "localhost",
    "rigctldPort": 4532,
    "useRigCtld": true
}
```

Configuration parameters

| Parameter          | Example value | Description                                                                                               |
| :----------------- | :------------ | :-------------------------------------------------------------------------------------------------------- |
| Callsign           | G0FOZ         | Your callsign - will be used for the CQ button and other macros.                                          |
| XMLRPCaddress      | localhost     | The IP address or name of the FLRig instance RPC server.  Other examples 127.0.0.1   myflrig.mydomain.com |
| XMLRPCport         | 12345         | IP port number of the FLRIg instance - defaults to 12345                                                  |
| rigctldAddress     | localhost     | SDR bandscope display frequency data                                                                      |
| rigctldPort        | 4532          | Port for frequency control of bandscope                                                                   |
| useRigctld         | true          | turn on/off this feature                                                                                  | 