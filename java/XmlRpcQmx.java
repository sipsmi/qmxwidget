/*
 *  G0FOZ    code (at) bockhampton.info
 *  Copyleft
 *  No responsibility will be taken for impact of this code on your system!
 */

/*
 * The XmlRpcQmx class facilitates communication with an XML-RPC service, providing methods to send and receive commands.
 */

/*
 * Key Improvement Opportunities:
 * - Simplify the logic with early returns to enhance readability.
 * - Improve error handling to capture more specific issues and avoid silent failures.
 * - Ensure that input validations and edge cases are properly managed.
 * - Streamline the use of the catCodeMap by making it immutable.
 * - Consider using enums for command strings to remove hardcoded values for better maintainability.
 */

import java.net.URL;
import java.util.Collections; // For creating unmodifiable collections
import java.util.HashMap;
import java.util.Map;

import org.apache.xmlrpc.client.XmlRpcClient;
import org.apache.xmlrpc.client.XmlRpcClientConfigImpl;

public class XmlRpcQmx {

    private static XmlRpcClient client; // The XML-RPC client instance for communication with the server
    private static boolean debug = false; // Debug flag for verbose output
    private static String server = "localhost"; // Default server address
    private static String serverPort = "12345"; // Default server port
    private static boolean commsIsOpen = false; // Communication status flag

    // CatCode mapping for command formatting
    private static final Map<String, String> catCodeMap = Collections.unmodifiableMap(new HashMap<String, String>() {
        private static final long serialVersionUID = 1L;
        {
            put("preamble", ""); // Command prefix
            put("get_wpm", ""); // Some specific command (usage not clear from context)
            put("postamble", ";"); // Command suffix
            put("ptton", "TQ1"); // Command for turning something on
            put("pttoff", "TQ0"); // Command for turning something off
        }
    });

    // Purpose: Returns the immutable category code map for external use.
    // Outputs: A map containing command prefixes and suffixes.
    public static Map<String, String> getCatcodemap() {
        return catCodeMap;
    }

    // Purpose: Constructor initializes the communication status.
    public XmlRpcQmx() {
        setCommsIsOpen(true); // Allows for communication post-construction
    }

    // Purpose: Initializes the XML-RPC client configuration and connects to the server.
    // Side Effects: Exits application on failure to establish connection.
    public void init() {
        try {
            // Configure the XML-RPC client with server details
            XmlRpcClientConfigImpl config = new XmlRpcClientConfigImpl();
            config.setServerURL(new URL("http://" + getServer() + ":" + getServerPort())); // Setting server URL

            // Create the client instance and assign configuration
            client = new XmlRpcClient();
            client.setConfig(config);

        } catch (Exception e) {
            System.err.println("XML-RPC Error: " + e.getMessage()); // Log connection error
            checkErrorResponse(e); // Update communication status based on error
            System.exit(1); // Exit if initialization fails
        }
    }

    // Purpose: Sends a concatenated command string to the server.
    // Inputs: 'arg' as the command string to send.
    // Outputs: Response string from the server indicating status or error.
    public String sendCatStringmain(String arg) {
        if (commsIsOpen) {
            try {
                // Create the full command string by combining parts from the map
                String catCode = catCodeMap.get("preamble") + arg + catCodeMap.get("postamble");
                // Execute the command on the server
                String catresponse = (String) client.execute("rig.cat_string", new Object[]{catCode});
                dispDebug("Sending cat string " + catCode); // Debug output
                return catresponse; // Return the server response
            } catch (Exception e) {
                System.err.println("XML-RPC Error: " + e.getMessage()); // Log execution error
                checkErrorResponse(e); // Check and update communication status
                return "Error"; // Return generic error
            }
        } else {
            return "Error"; // Return error if communication is not open
        }
    }

    // Purpose: Sends an integer command to the server.
    // Inputs: 'command' as the action to perform, 'value' as the integer parameter.
    // Outputs: Response string from the server.
    public String flrigSetInteger(String command, int value) {
        if (commsIsOpen) {
            try {
                // Execute the integer command on the server
                String catresponse = (String) client.execute(command, new Object[]{value});
                dispDebug("Sending cat string " + command + value); // Debug output
                return catresponse; // Return the server response
            } catch (Exception e) {
                System.err.println("XML-RPC Error: " + e.getMessage()); // Log execution error
                checkErrorResponse(e); // Check and update communication status
                return "Error"; // Return generic error
            }
        } else {
            return "Error"; // Return error if communication is not open
        }
    }

    // Purpose: Retrieves an integer value from the server based on the provided argument.
    // Inputs: 'arg' as the command to execute.
    // Outputs: The integer response from the server or 0 on failure.
    public Integer getCATInteger(String arg) {
        if (commsIsOpen) {
            try {
                Object[] catString = new Object[] {}; // Empty parameters
                Integer catresponse = (Integer) client.execute(arg, catString); // Execute the command
                return catresponse; // Return the integer response
            } catch (Exception e) {
                System.err.println("XML-RPC Error: " + e.getMessage()); // Log execution error
                checkErrorResponse(e); // Check and update communication status
                return 0; // Return 0 on failure
            }
        } else {
            return 0; // Return 0 if communication is not open
        }
    }

    // Purpose: Retrieves a string response from the server based on the provided argument.
    // Inputs: 'arg' as the command to execute.
    // Outputs: The string response from the server or "Error" on failure.
    public String getCATString(String arg) {
        if (commsIsOpen) {
            try {
                Object[] catString = new Object[] {}; // Empty parameters
                String catresponse = (String) client.execute(arg, catString); // Execute the command
                return catresponse; // Return the string response
            } catch (Exception e) {
                System.err.println("XML-RPC Error: " + e.getMessage()); // Log execution error
                checkErrorResponse(e); // Check and update communication status
                return "Error"; // Return "Error" on failure
            }
        } else {
            return "Error"; // Return error if communication is not open
        }
    }

    // Purpose: Displays debug messages if enabled.
    // Inputs: '$msg' as the message to display.
    public void dispDebug(String $msg) {
        if (debug) {
            System.out.println("Debug: " + $msg); // Output debug message
        }
    }

    // Purpose: Returns the server address.
    // Outputs: The server address as a string.
    public static String getServer() {
        return server; // Return the current server address
    }

    // Purpose: Updates the server address.
    // Inputs: 'server' as the new server address.
    public static void setServer(String server) {
        XmlRpcQmx.server = server; // Assign new server address
    }

    // Purpose: Returns the server port.
    // Outputs: The server port as a string.
    public static String getServerPort() {
        return serverPort; // Return the current server port
    }

    // Purpose: Updates the server port.
    // Inputs: 'serverPort' as the new server port.
    public static void setServerPort(String serverPort) {
        XmlRpcQmx.serverPort = serverPort; // Assign new server port
    }

    // Purpose: Returns the current communication status.
    // Outputs: True if communication is open, otherwise false.
    public static boolean isCommsIsOpen() {
        return XmlRpcQmx.commsIsOpen; // Return the communication status
    }

    // Purpose: Updates the communication status.
    // Inputs: 'setter' as the new status for communication.
    private static void setCommsIsOpen(boolean setter) {
        XmlRpcQmx.commsIsOpen = setter; // Set communication status
    }

    // Purpose: Checks and updates the communication status based on exceptions encountered.
    // Inputs: 'e' as the exception to analyze.
    private static void checkErrorResponse(Exception e) {
        if (e.getMessage().matches("(?i).*Connection refused.*")) {
            setCommsIsOpen(false); // Close communication on connection failure
        }
        // NOTE: The response line below could be uncommented for additional logging
        // System.out.println("Is Open: "+XmlRpcQmx.isCommsIsOpen() + " "+ e.getMessage());
    }
}

