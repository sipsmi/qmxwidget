import com.fazecast.jSerialComm.SerialPort;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

public class QSerialPort {
	private static String portName = "/dev/QMX07";
	private static SerialPort[] ports;
	private static SerialPort serialPort;
	private static OutputStream output;
	private static InputStream input;
	private static boolean commsIsOpen = false;

	private static final Map<String, String> catCodeMap = new HashMap<String, String>() {
		private static final long serialVersionUID = 1L;
		{
			put("preamble", "");
			put("get_wpm", "");
			put("postamble", ";");
			put("ptton", "TQ1");
			put("pttoff", "TQ0");
		}
	};

	public QSerialPort() {
		System.out.println("Available Serial Ports (look for QMX):");
		// ports = SerialPort.getCommPorts();
		// for (SerialPort port : ports) {
		// System.out.println(" - " + port.getSystemPortName());
		// }

		// Open a specific serial port (e.g. /dev/ttyUSB0)
		serialPort = SerialPort.getCommPort(portName);
		serialPort.setBaudRate(115200);
		serialPort.setNumDataBits(8);
		serialPort.setNumStopBits(SerialPort.ONE_STOP_BIT);
		serialPort.setParity(SerialPort.NO_PARITY);
		serialPort.setComPortTimeouts(SerialPort.TIMEOUT_READ_BLOCKING, 20, 0); // 200 ms read timeout

		output = serialPort.getOutputStream();
		input = serialPort.getInputStream();

		if (!serialPort.openPort()) {
			System.out.println("Failed to open port: " + portName);
			return;
		} else {
			commsIsOpen = true;
			System.out.println("Opened port: " + portName);
		}
	}

	private String sendAndReceive(String arg) {
		if ( commsIsOpen ) try {
			String response= null;
			String catCode = catCodeMap.get("preamble") + arg + catCodeMap.get("postamble");
			byte[] messageBytes = catCode.getBytes();
			output.write(messageBytes);
			output.flush();
			System.out.println("Message sent: " + arg);
			// Read response (if any)
            byte[] readBuffer = new byte[1024];
            int numBytes = input.read(readBuffer);
            if (numBytes > 0) {
                response = new String(readBuffer, 0, numBytes);
            }

			if (response.length() > 0) {
				System.out.println("Received: " + response.toString().trim());
				return response.toString().trim();
			} else {
				System.out.println("No response received.");
				return null;
			}
		} catch (Exception e) {
			System.err.println("Serial Error: " + e.getMessage());
			// checkErrorResponse(e);
			// e.printStackTrace();
			return "Error";
		}
		else
		{
			System.err.println("Serial Error: no comms open "+arg);
			return "Error";
			
		}

	}

	public String sendCatStringmain(String arg) {
		String retval = null;
		if (commsIsOpen)
			retval = sendAndReceive(arg);
		return retval;

	}

	public String flrigSetInteger(String command, int value) {

		return "Error";

	}

	public Integer getCATInteger(String arg) {

		return 0;
	}

	public String getCATString(String arg) {

		return "Error";

	}

	public static boolean isCommsIsOpen() {
		return commsIsOpen;
	}

	public static void setCommsIsOpen(boolean commsIsOpen) {
		QSerialPort.commsIsOpen = commsIsOpen;
	}

	public static String getPortName() {
		return portName;
	}

	public static void setPortName(String portName) {
		QSerialPort.portName = portName;
	}

	public static SerialPort[] getPorts() {
		return ports;
	}

	public static void setPorts(SerialPort[] ports) {
		QSerialPort.ports = ports;
	}

	public static SerialPort getSerialPort() {
		return serialPort;
	}

	public static void setSerialPort(SerialPort serialPort) {
		QSerialPort.serialPort = serialPort;
	}
}