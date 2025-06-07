import com.fazecast.jSerialComm.SerialPort;

import java.io.InputStream;
import java.io.OutputStream;

public class QSerialPort {
	private static String portName = "/dev/QMX07";
	private static SerialPort[] ports;
	private static SerialPort serialPort;
	private static OutputStream output;
	private static InputStream input; 
	private static boolean commsIsOpen = false;


	public QSerialPort() {
		System.out.println("Available Serial Ports (look for QMX):");
		//ports = SerialPort.getCommPorts();
		//for (SerialPort port : ports) {
		//	System.out.println(" - " + port.getSystemPortName());
		//}

		// Open a specific serial port (e.g. /dev/ttyUSB0)
		serialPort = SerialPort.getCommPort(portName);
		serialPort.setBaudRate(115200);
		serialPort.setNumDataBits(8);
		serialPort.setNumStopBits(SerialPort.ONE_STOP_BIT);
		serialPort.setParity(SerialPort.NO_PARITY);
		serialPort.setComPortTimeouts(SerialPort.TIMEOUT_READ_SEMI_BLOCKING, 200, 0); // 200 ms read timeout
		
		output = serialPort.getOutputStream();
		input = serialPort.getInputStream();

		if (!serialPort.openPort()) {
			System.out.println("Failed to open port: " + portName);
			return;
		}
		else 
		{
			commsIsOpen = true;
			System.out.println("Opened port: " + portName);
		}
	}

	public static String main(String arg) {

		if ( commsIsOpen) try  {


			output.write(arg.getBytes());
			output.flush();

			// Read response (if any)
			StringBuilder response = new StringBuilder();
			int available;
			while ((available = input.available()) > 0) {
				byte[] buffer = new byte[available];
				int bytesRead = input.read(buffer);
				if (bytesRead > 0) {
					response.append(new String(buffer, 0, bytesRead));
				}
			}

			if (response.length() > 0) {
				System.out.println("Received: " + response.toString().trim());
				return response.toString().trim();
			} else {
				System.out.println("No response received.");
				return null;
			}

		} catch (Exception e) {
			e.printStackTrace();
			return null;
		} finally {
			serialPort.closePort();
		}
		else
		{
			return null;
		}
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