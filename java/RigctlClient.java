
/*
 *  G0FOZ    code (at) bockhampton.info
 *  Copyleft
 *  No responsibility will be taken for impact of this code on your system!
 */

/*
 * This class, RigctlClient, is designed to communicate with a server (rigctld) over a socket 
 * to set a frequency for radio equipment. It employs basic I/O operations and manages connections 
 * efficiently within its limited scope but lacks robustness in error handling and resource management. 
 */

/*
 * Key Improvement Opportunities:
 * - Implement resource cleanup mechanisms to close socket and streams properly.
 * - Enhance error handling to provide more detailed feedback and avoid silent failures.
 * - Validate input parameters and handle edge cases for frequency more strictly.
 */

import java.io.BufferedReader;  // Imports for buffered I/O operations
import java.io.BufferedWriter;  // Used to write character data to an output stream
import java.io.IOException;  // Handles input/output exceptions
import java.io.InputStreamReader;  // Converts byte streams to character streams
import java.io.OutputStreamWriter;  // Converts character streams to byte streams
import java.net.Socket;  // Represents a client socket for network connections
import java.nio.charset.StandardCharsets;  // Provides standard character encoding options

public class RigctlClient {
    
    // Holds the socket for communication with the rigctld server
    private static Socket socket;
    // Writer to send commands to the server
    private static BufferedWriter writer;
    // Reader to receive responses from the server
    private static BufferedReader reader;
    // Indicates the communication state with the server
    private static boolean commsIsOpen = false;

    /*
     * Constructor for RigctlClient.
     * Initializes the socket connection to the specified server host and port.
     * Inputs: serverHost (String) - the hostname of the rigctld server; 
     *         serverPort (int) - the port number for the connection.
     * Outputs: Initializes communication if successful, otherwise logs an error message.
     */
    public RigctlClient(String serverHost, int serverPort) {
        super();
        try {
            // Establish a socket connection to the specified server and port
            RigctlClient.socket = new Socket(serverHost, serverPort);
            // Initialize the writer to send ASCII commands to the server
            RigctlClient.writer = new BufferedWriter(
                new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.US_ASCII)
            );
            // Initialize the reader to receive ASCII responses from the server
            RigctlClient.reader = new BufferedReader(
                new InputStreamReader(socket.getInputStream(), StandardCharsets.US_ASCII)
            );
            // Mark communications as open after successful connection
            commsIsOpen = true;
        } catch (IOException e) {
            // Log the error if the communication fails during initialization
            System.err.println("Error communicating with rigctld: " + e.getMessage());
        }
    }

    /*
     * Sends a frequency command to the rigctld server.
     * Inputs: frequency (long) - the frequency value to set.
     * Throws: IOException if there are issues during communication or response not as expected.
     * Behavior: Constructs a command string, sends it, and checks the server's response.
     */
    public void sendFrequency(long frequency) throws IOException {
        // Check if the communication state is open before sending commands
        if (commsIsOpen) {
            try {
                // Construct the command to set frequency
                String command = "F " + frequency + "\n";
                writer.write(command);  // Write the command to the output stream
                writer.flush();  // Ensure the command is sent immediately

                // Read and verify the server's response
                String response = reader.readLine();
                // Check if the response is valid, indicating success
                if (response == null || !response.startsWith("RPRT 0")) {
                    throw new IOException("Failed to set frequency. Server response: " + response);
                }
            } catch (IOException e) {
                // Log the error while attempting to communicate with rigctld
                System.err.println("Error communicating with rigctld: " + e.getMessage());
                commsIsOpen = false;  // Set communication status to false on error
            }
        }
    }
    
    // IMPROVEMENT: Consider implementing a cleanup method to properly close the socket and streams
    // to avoid resource leaks. This should be called during object destruction or when closing communication. 
}
