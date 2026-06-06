/*
 * G0FOZ    code (at) bockhampton.info
 * Copyleft
 * No responsibility will be taken for impact of this code on your system!
 */

import com.fazecast.jSerialComm.SerialPort; 
import com.fazecast.jSerialComm.SerialPortDataListener; 
import com.fazecast.jSerialComm.SerialPortEvent; 

import java.io.OutputStream; 
import java.util.HashMap; 
import java.util.Map; 

import java.nio.charset.StandardCharsets; 
import java.util.ArrayList; 
import java.util.List; 

public class QSerialPort {
    private static String portName = "/dev/QMX00"; // Port name for communication
    private static SerialPort[] ports; // Array of available serial ports
    private static SerialPort serialPort; // Single serial port used for communication
    private static OutputStream output; // Output stream for sending data
    private static boolean commsIsOpen = false; // Flag indicating if communication is open
    String sCommand = ""; 
    private static final StringBuilder responseBuffer = new StringBuilder(); // Buffer to accumulate incoming data
    private static final char END_DELIMITER = ';'; // Character indicating the end of a command message

    // Mapping of command codes to their respective strings
    private static final Map<String, String> catCodeMap = new HashMap<String, String>() {
        private static final long serialVersionUID = 1L; // Serialization identifier
        {
            put("preamble", ""); // Initial part of command message
            put("get_wpm", ""); // Placeholder for getting words per minute
            put("postamble", ";"); // Ending part of command message
            put("ptton", "TQ1"); // Command to turn transmission on
            put("pttoff", "TQ0"); // Command to turn transmission off
        }
    };

    private List<String> responseList = new ArrayList<>(); // List to store response messages

    /*
     * Constructor for QSerialPort class 
     * Initializes the serial port, sets parameters, and establishes a data listener.
     */
    public QSerialPort(String portNameIn) {
        portName = portNameIn;
        commonQSerialPort();
    }
    
    public QSerialPort() {
        commonQSerialPort();
    }
    
    private void commonQSerialPort() {
        System.out.println("Available Serial Ports (look for QMX):");
        // ports = SerialPort.getCommPorts();
        // for (SerialPort port : ports) {
        //    System.out.println(" - " + port.getSystemPortName());
        // }

        try {
            // FIX 1: Initialize the serial port BEFORE attempting to set its parameters
            serialPort = SerialPort.getCommPort(portName); 

            if (serialPort == null) {
                System.err.println("Serial Error: Port object could not be created for " + portName);
                return;
            }

            serialPort.setBaudRate(115200); // Set communication speed
            serialPort.setNumDataBits(8); // Set data bits to 8
            serialPort.setNumStopBits(SerialPort.ONE_STOP_BIT); // Set one stop bit
            serialPort.setParity(SerialPort.NO_PARITY); // No parity checking
            serialPort.setComPortTimeouts(SerialPort.TIMEOUT_NONBLOCKING, 0, 0); // Non-blocking read timeout
            
            // Set up listener for incoming data
            serialPort.addDataListener(new SerialPortDataListener() {
                @Override
                public int getListeningEvents() {
                    return SerialPort.LISTENING_EVENT_DATA_AVAILABLE; // Listen for data availability events
                }

                @Override
                public void serialEvent(SerialPortEvent event) {
                    if (event.getEventType() != SerialPort.LISTENING_EVENT_DATA_AVAILABLE)
                        return; // Only process data available events

                    byte[] buffer = new byte[serialPort.bytesAvailable()]; // Allocate a buffer based on available bytes
                    int numRead = serialPort.readBytes(buffer, buffer.length); // Read incoming bytes into the buffer
                    String received = new String(buffer, 0, numRead, StandardCharsets.UTF_8); // Convert buffer to String

                    // Accumulate and check for complete message
                    for (char c : received.toCharArray()) { // Iterate over each character in the received message
                        responseBuffer.append(c); // Append character to buffer
                        if (c == END_DELIMITER) { // Check for end delimiter to identify complete messages
                            String completeMsg = responseBuffer.toString().trim(); // Get complete message and trim whitespace
                            addResponse(completeMsg); // Add complete message to response list
                            responseBuffer.setLength(0); // Clear the buffer for the next message
                        }
                    }
                }
            });

            // FIX 2: Safely attempt to open the port. If it fails, log and exit the method gracefully.
            if (!serialPort.openPort()) { 
                System.err.println("Failed to open port (it may not exist or is in use): " + portName); 
                commsIsOpen = false;
                return; 
            } else {
                // Initialize output stream ONLY after the port is successfully opened
                output = serialPort.getOutputStream(); 
                commsIsOpen = true; 
                System.out.println("Opened port: " + portName); 
            }
            
        } catch (Exception e) {
            // FIX 3: Catch any unexpected jSerialComm exceptions so the program doesn't crash
            System.err.println("Exception caught while initializing serial port: " + e.getMessage());
            commsIsOpen = false;
        }
    }

    /*
     * Sends a command string and waits for a response.
     */
    private String sendAndReceive(String arg) {
        if (commsIsOpen && output != null) { // Added null check for output stream safety
            try {
                String response = ""; 
                String catCode = catCodeMap.get("preamble") + arg + catCodeMap.get("postamble"); 
                byte[] messageBytes = catCode.getBytes(); 
                output.write(messageBytes); 
                output.flush(); 
                return response; 
            } catch (Exception e) { 
                System.err.println("Serial Error during send: " + e.getMessage()); 
                return "Error"; 
            }
        } else { 
            System.err.println("Serial Error: no comms open " + arg); 
            return "Error"; 
        }
    }

    /*
     * Sends a command string specifically for CAT (Computer Aided Transceiver) communication.
     */
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

    public synchronized void addResponse(String str) {
        if (str != null && !str.trim().isEmpty()) { 
            responseList.add(str); 
        }
    }

    public synchronized String fetchAndRemoveResponse() {
        if (!responseList.isEmpty()) { 
            return responseList.remove(0); 
        }
        System.out.println("Array is empty - nothing to fetch"); 
        return null; 
    }

    public synchronized boolean fetchAndRemoveResponse(String str) {
        return responseList.remove(str); 
    }

    public synchronized int sizeResponse() {
        return responseList.size(); 
    }
}