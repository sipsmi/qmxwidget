# qmxwidget

![1.00](qmx.png)

This little widget exploits the latest QMX CAT codes ( LCD read, power, swr ) to give PC control and visualisation.
It previously used FLRIG XMLRPC but now connects directly to the QMX.

The code has been modfied to send frequency data to SDR++ program via its rigctld emulation server; should an SDR++ instance not be runnign ( or not have the rigctld running it quietly ignores it)    This therefore adds a bandscope to the QMX.  If this is enabled it sends commands to the QMX to ensure audio card is in I/Q mode.  Instructions to configure SDR++ to follow\...

![1.00](arch2.png)

## Running

It may be run from a single composite JAR file but requires a configuration file:

```
java -jar qmx.jar   config.json 
```

Remember to change the parameters to suit!

## Configuration

Parameters are input as a configuration file in JSON format:

```
{
	"Callsign": "G0FOZ",
    "rigctldAddress": "localhost",
    "rigctldPort": 4532,
    "qmxDevice":  "/dev/QMX07"
}
```

Configuration parameters

| Parameter          | Example value | Description                                                                                               |
| :----------------- | :------------ | :-------------------------------------------------------------------------------------------------------- |
| Callsign           | G0FOZ         | Your callsign - will be used for the CQ button and other macros.                                          |
| XMLRPCaddress      | localhost     | Deprecated. The IP address or name of the FLRig instance RPC server.  Other examples 127.0.0.1   myflrig.mydomain.com |
| XMLRPCport         | 12345         | Dep[recated. IP port number of the FLRIg instance - defaults to 12345                                                  |
| rigctldAddress     | localhost     | SDR bandscope display frequency data                                                                      |
| rigctldPort        | 4532          | Port for frequency control of bandscope                                                                   |
| qmxDevice          | COM3          | The USB/Serial interface, will be COMx on windows, various /dev/.... on Linux/MAX | 