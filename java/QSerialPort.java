import com.fazecast.jSerialComm.SerialPort;
import com.fazecast.jSerialComm.SerialPortDataListener;
import com.fazecast.jSerialComm.SerialPortEvent;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class QSerialPort {
	private static String portName = "/dev/QMX07";
	private static SerialPort[] ports;
	private static SerialPort serialPort;
	private static OutputStream output;
	private static InputStream input;
	private static boolean commsIsOpen = false;
	private static boolean hasRegRx = false;
	String sCommand = "";
	private static final StringBuilder responseBuffer = new StringBuilder();
	private static final char END_DELIMITER = ';';

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

	private List<String> responseList = new ArrayList<>();

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
		// serialPort.setComPortTimeouts(SerialPort.TIMEOUT_READ_SEMI_BLOCKING, 30, 0);
		// // 200 ms read timeout
		serialPort.setComPortTimeouts(SerialPort.TIMEOUT_NONBLOCKING, 0, 0); // 200 ms read timeout
		output = serialPort.getOutputStream();
		input = serialPort.getInputStream();

		//
		// Set up listener for incoming data
		serialPort.addDataListener(new SerialPortDataListener() {
			@Override
			public int getListeningEvents() {
				return SerialPort.LISTENING_EVENT_DATA_AVAILABLE;
			}

			@Override
			public void serialEvent(SerialPortEvent event) {
				if (event.getEventType() != SerialPort.LISTENING_EVENT_DATA_AVAILABLE)
					return;

				byte[] buffer = new byte[serialPort.bytesAvailable()];
				int numRead = serialPort.readBytes(buffer, buffer.length);
				String received = new String(buffer, 0, numRead, StandardCharsets.UTF_8);

				// Accumulate and check for complete message
				for (char c : received.toCharArray()) {
					responseBuffer.append(c);
					if (c == END_DELIMITER) {
						String completeMsg = responseBuffer.toString().trim();
						addResponse(completeMsg);
						responseBuffer.setLength(0); // Clear buffer
					}
				}
			}
		});

		if (!serialPort.openPort()) {
			System.out.println("Failed to open port: " + portName);
			return;
		} else {
			commsIsOpen = true;
			System.out.println("Opened port: " + portName);
		}
	}

//	}

	private String sendAndReceive(String arg) {
		if (commsIsOpen)
			try {
				String response = "";
				String catCode = catCodeMap.get("preamble") + arg + catCodeMap.get("postamble");
				byte[] messageBytes = catCode.getBytes();
				output.write(messageBytes);
				output.flush();
				// System.out.println("Message sent: " + arg);
				return response;
			} catch (Exception e) {
				System.err.println("Serial Error: " + e.getMessage());
				// checkErrorResponse(e);
				// e.printStackTrace();
				return "Error";
			}
		else {
			System.err.println("Serial Error: no comms open " + arg);
			return "Error";

		}

	}


	public String sendCatStringmain(String arg) {
		String retval = "";
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

	/**
	 * Add a string to the array
	 * 
	 * @param str The string to add
	 */
	public synchronized void addResponse(String str) {
		if (str != null && !str.trim().isEmpty()) {
			responseList.add(str);
			// System.out.println("Added: " + str);
		}
	}

	/**
	 * Fetch and remove the first string from the array
	 * 
	 * @return The first string or null if array is empty
	 */
	public synchronized String fetchAndRemoveResponse() {
		if (!responseList.isEmpty()) {
			String str = responseList.remove(0);
			// System.out.println("Fetched and removed: " + str);
			return str;
		}
		System.out.println("Array is empty - nothing to fetch");
		return null;
	}

	/**
	 * Fetch and remove a specific string from the array
	 * 
	 * @param str The string to find and remove
	 * @return true if found and removed, false otherwise
	 */
	public synchronized boolean fetchAndRemoveResponse(String str) {
		boolean removed = responseList.remove(str);
		if (removed) {
			// System.out.println("Fetched and removed specific string: " + str);
		} else {
			// System.out.println("String not found: " + str);
		}
		return removed;
	}

	/**
	 * Get current size of the array
	 * 
	 * @return Number of elements in the array
	 */
	public synchronized int sizeResponse() {
		return responseList.size();
	}

}