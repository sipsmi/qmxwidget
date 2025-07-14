/*
 *  G0FOZ    code (at) bockhampton.info
 *  Copyleft
 *  No responsibility will be taken for impact of this code on your system!
 */


/*
 * This Java class, QSerialPort, provides functionalities for interacting with a specified serial port using the jSerialComm library.
 * It handles communication opening, data listening, message formatting through predefined command codes, and response management.
 * Features include sending commands and receiving responses while maintaining synchronization for thread safety.
 */

/*
 * Key Improvement Opportunities:
 * 1. Error handling could be more robust in methods like sendAndReceive; consider using specific exception types or more descriptive messages.
 * 2. The use of static fields for serial port management raises concerns about state management and concurrency in a multi-threaded environment; consider instance fields where necessary.
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
    //private static InputStream input; // Input stream for receiving data
    private static boolean commsIsOpen = false; // Flag indicating if communication is open
    //private static boolean hasRegRx = false; // Unused variable; intended for registering received messages
    String sCommand = ""; // Unused variable; potentially intended for command tracking
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
    public QSerialPort(String portNameIn ) {
    	portName = portNameIn;
    	commonQSerialPort();
    }
    public QSerialPort( ) {
    	commonQSerialPort();
    }
    private void  commonQSerialPort() {
        System.out.println("Available Serial Ports (look for QMX):");
        // IMPROVEMENT: Consider uncommenting port listing for debugging
        // ports = SerialPort.getCommPorts();
        // for (SerialPort port : ports) {
        //    System.out.println(" - " + port.getSystemPortName());
        // }

        // Open a specific serial port (e.g. /dev/ttyUSB0)
        serialPort = SerialPort.getCommPort(portName); // Get the specified port
        serialPort.setBaudRate(115200); // Set communication speed
        serialPort.setNumDataBits(8); // Set data bits to 8
        serialPort.setNumStopBits(SerialPort.ONE_STOP_BIT); // Set one stop bit
        serialPort.setParity(SerialPort.NO_PARITY); // No parity checking

        serialPort.setComPortTimeouts(SerialPort.TIMEOUT_NONBLOCKING, 0, 0); // Non-blocking read timeout
        output = serialPort.getOutputStream(); // Initialize output stream for sending data

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

        if (!serialPort.openPort()) { // Attempt to open the serial port
            System.out.println("Failed to open port: " + portName); // Log failure
            return; // Exit constructor if port cannot be opened
        } else {
            commsIsOpen = true; // Set flag indicating communication is open
            System.out.println("Opened port: " + portName); // Log successful connection
        }
    }

    /*
     * Sends a command string and waits for a response.
     * @param arg The command string to send.
     * @return The response string or an error message if communication is not open.
     */
    private String sendAndReceive(String arg) {
        if (commsIsOpen) // Check if communication is open
            try {
                String response = ""; // Variable to hold response (not currently utilized)
                // Build the complete command from the mapping
                String catCode = catCodeMap.get("preamble") + arg + catCodeMap.get("postamble"); // Construct command message
                byte[] messageBytes = catCode.getBytes(); // Convert command to byte array
                output.write(messageBytes); // Write the command to the output stream
                output.flush(); // Ensure all bytes are sent out
                // System.out.println("Message sent: " + arg); // Log the sent message
                return response; // Return the (unused) response
            } catch (Exception e) { // Catch all exceptions that may occur during communication
                System.err.println("Serial Error: " + e.getMessage()); // Log error message 
                // TEST CASE: Consider scenarios where sendAndReceive fails due to I/O issues
                // checkErrorResponse(e); // Unused error handling function
                // e.printStackTrace(); // Uncomment for detailed stack trace
                return "Error"; // Return a general error message
            }
        else { // If communication is not open
            System.err.println("Serial Error: no comms open " + arg); // Log that no communication exists
            return "Error"; // Return an error message indicating no communication
        }
    }

    /*
     * Sends a command string specifically for CAT (Computer Aided Transceiver) communication.
     * @param arg The command string to send.
     * @return The response obtained from the serial communication.
     */
    public String sendCatStringmain(String arg) {
        String retval = ""; // Initialize return value
        if (commsIsOpen) // Check if communication is open
            retval = sendAndReceive(arg); // Send command and get response
        return retval; // Return the response
    }

    /*
     * Stub method intended for setting an integer value via FLRig commands; currently not implemented.
     * @param command The command string for setting the integer.
     * @param value The integer value to set.
     * @return An error message until the method is implemented.
     */
    public String flrigSetInteger(String command, int value) {
        return "Error"; // Return error as the method is not implemented yet
        // IMPROVEMENT: Implement method logic or throw UnsupportedOperationException
    }

    /*
     * Stub method for retrieving an integer value using CAT communication; currently returns 0 without implementation.
     * @param arg The argument string for the command.
     * @return Currently returns 0 as placeholder.
     */
    public Integer getCATInteger(String arg) {
        return 0; // Return 0; functionality not implemented yet
        // IMPROVEMENT: Implement method or clarify intended behavior, possibly throw UnsupportedOperationException
    }

    /*
     * Stub method for fetching a string using CAT communication; returns an error message as placeholder.
     * @param arg The argument string for the command.
     * @return An error message since functionality is not implemented.
     */
    public String getCATString(String arg) {
        return "Error"; // Return error as the method is not implemented yet
        // IMPROVEMENT: Implement method logic or throw UnsupportedOperationException
    }

    /*
     * Checks if the communication port is currently open.
     * @return True if communication is established, otherwise false.
     */
    public static boolean isCommsIsOpen() {
        return commsIsOpen; // Return the state of the communication flag
    }

    /*
     * Sets the state of the communication flag (open/closed).
     * @param commsIsOpen The state to set for communication.
     */
    public static void setCommsIsOpen(boolean commsIsOpen) {
        QSerialPort.commsIsOpen = commsIsOpen; // Update the communication state
    }

    /*
     * Gets the current port name being used for communication.
     * @return The name of the serial port.
     */
    public static String getPortName() {
        return portName; // Return the current port name
    }

    /*
     * Sets the serial port name to be used for communication.
     * @param portName The name of the serial port to set.
     */
    public static void setPortName(String portName) {
        QSerialPort.portName = portName; // Update the static port name
    }

    /*
     * Gets the array of available serial ports.
     * @return The array of SerialPort objects.
     */
    public static SerialPort[] getPorts() {
        return ports; // Return the array of serial ports
    }

    /*
     * Sets the array of available serial ports; potentially used for listing ports.
     * @param ports The array of SerialPort objects to set.
     */
    public static void setPorts(SerialPort[] ports) {
        QSerialPort.ports = ports; // Update the static array of ports
    }

    /*
     * Gets the current SerialPort object being used.
     * @return The SerialPort object.
     */
    public static SerialPort getSerialPort() {
        return serialPort; // Return the current SerialPort object
    }

    /*
     * Sets the current SerialPort object being used for communication.
     * @param serialPort The SerialPort object to set.
     */
    public static void setSerialPort(SerialPort serialPort) {
        QSerialPort.serialPort = serialPort; // Update the static SerialPort object
    }

    /**
     * Add a string to the response list.
     * 
     * @param str The string to add; must not be null or empty.
     */
    public synchronized void addResponse(String str) {
        if (str != null && !str.trim().isEmpty()) { // Check for null or empty string
            responseList.add(str); // Add the string to the response list
            // System.out.println("Added: " + str); // Log added string
        }
    }

    /**
     * Fetch and remove the first string from the response list.
     * 
     * @return The first string from the list or null if empty.
     */
    public synchronized String fetchAndRemoveResponse() {
        if (!responseList.isEmpty()) { // Check if the response list is not empty
            String str = responseList.remove(0); // Remove and store the first string
            // System.out.println("Fetched and removed: " + str); // Log fetched string
            return str; // Return the fetched string
        }
        System.out.println("Array is empty - nothing to fetch"); // Log empty list scenario
        return null; // Return null if list is empty
    }

    /**
     * Fetch and remove a specific string from the response list.
     * 
     * @param str The string to find and remove.
     * @return True if found and removed, false otherwise.
     */
    public synchronized boolean fetchAndRemoveResponse(String str) {
        boolean removed = responseList.remove(str); // Attempt to remove the specified string
        if (removed) {
            // System.out.println("Fetched and removed specific string: " + str); // Log successful removal
        } else {
            // System.out.println("String not found: " + str); // Log if string not found
        }
        return removed; // Return whether the string was found and removed
    }

    /**
     * Get the current size of the response list.
     * 
     * @return The number of elements in the response list.
     */
    public synchronized int sizeResponse() {
        return responseList.size(); // Return the size of the response list
    }
}
