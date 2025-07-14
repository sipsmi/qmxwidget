/*
 *  G0FOZ    code (at) bockhampton.info
 *  Copyleft
 *  No responsibility will be taken for impact of this code on your system!
 */

/*
 *  main entry point for a system that interacts with various QMX and serial port configurations 
 * through command-line arguments and JSON files. It contains utility functions to handle numerical 
 * conversions and mappings of UI codes 
 * to more technical terms used by the software.
 */

/*
 * Key Improvement Opportunities:
 * 1. Improve error handling in input parsing methods (getIntFromString, getFloatFromString) to ensure better robustness.
 * 2. Consider reducing the complexity of methods such as intToStringScaled by breaking them down into smaller functions.
 * 3. The use of debug flags could be enhanced with a logging framework for better control over debug messages.
 * 4. The repetitive error handling with System.exit(1) could be replaced with exceptions or a more graceful handling approach.
 */

import java.io.File; // Importing File class for file operations
import java.io.IOException; // Importing IOException for error handling during file operations
import java.nio.file.Files; // Importing Files for reading file contents
import java.nio.file.Paths; // Importing Paths for easier file path management
import java.util.HashMap; // Importing HashMap to store key-value pairs for mappings
import java.util.Map; // Importing Map for the map interface

import org.json.JSONException; // Importing JSONException for handling JSON errors
import org.json.JSONObject; // Importing JSONObject class for JSON operations

public class MainClass {

	private static boolean debug = true; // Debugging flag to control debug message output
	private static String myCall = "GX0XXX"; // Default call 
	private static String jsonSerialPort="";

	// Map UI codes to useful QMX codes
	private static final Map<String, String> UICodeMap = new HashMap<String, String>() {
		private static final long serialVersionUID = 1L; // Unique ID for serialization
		{
			// Initializing UI to QMX code mappings
			put("preamble", "");
			put("get_wpm", "");
			put("postamble", ";");
			put("ptton", "TQ1");
			put("pttoff", "TQ0");

			// MODE Mappings
			put("LSB", "1");
			put("USB", "2");
			put("CW", "3");
			put("FSK", "6");
			put("CWR", "7");
			put("FSR", "9");

			// Reverse mappings for mode IDs to strings
			put("1", "LSB");
			put("2", "USB");
			put("3", "CW");
			put("6", "FSK");
			put("7", "CWR");
			put("9", "FSR");

			// STEPS Mappings
			put(" 100 Hz", "100");
			put(" 500 Hz", "500");
			put(" 1 KHz", "1000");
			put("10 KHz", "10000");
			put("100 KHz", "100000");
			put(" 1 MHz", "1000000");
		}
	};

	/**
	 * 
	 */
	public MainClass() {
		try {
			// Constructor may initialize components, currently empty
		} catch (Exception e) {
			// Handles exceptions during initialization
			System.err.println("Main Class Configuration Error: " + e.getMessage());
			// e.printStackTrace(); // Optional: Uncomment for detailed stack trace
			// System.exit(1); // Potentially problematic exit, consider throwing an
			// exception instead
		}
	}

	/**
	 * The main method serves as the entry point of the application. It initializes
	 * necessary components and fetches configuration parameters from a JSON file
	 * specified as a command-line argument. Expected Input: - String array of
	 * command-line arguments (args) Expected Output: - None directly, but the
	 * application initializes based on config.
	 */
	public static void main(String[] args) {
		try {
			// Initiating the base classes for processing QMX and serial port communication
			XmlRpcQmx qmx = new XmlRpcQmx();


			JSONObject jsonObj = null; // Initialize JSON object for configurations
			int argc = args.length; // Capture the number of command-line arguments

			// Validate command-line arguments: Expecting exactly one argument
			// (configuration file)
			if (argc != 1) { // Error handling for incorrect number of parameters
				System.out.println("Usage(" + argc + "): <command> <configuration file.json>"); // Indicate expected
																								// usage
				System.exit(1); // Exit with error status for invalid input
			} else if (Files.exists(Paths.get(args[0]))) { // Check if the provided file exists
				try {
					// Retrieve the parameters stored in the provided JSON file
					jsonObj = getParamAsJSON(args[0]); // Parse parameters to JSON object
					dispDebug(jsonObj.toString()); // Output debug information for verification
					myCall = jsonObj.getString("Callsign"); // Fetch and store the callsign
					// Configure server details with default fallback
					// XmlRpcQmx.setServer(jsonObj.has("Server") ? jsonObj.getString("Server") :
					// XmlRpcQmx.getServer());
					// XmlRpcQmx.setServerPort(jsonObj.has("ServerPort") ?
					// jsonObj.getString("ServerPort") : XmlRpcQmx.getServerPort());
					// "Callsign": "G0FOZ",
					// "rigctldAddress": "localhost",
					// "rigctldPort": 4532,
					// "qmxDevice": "/dev/QMX07"
					RigctlClient.setServerHost(jsonObj.has("rigctldAddress") ? jsonObj.getString("rigctldAddress")
							: RigctlClient.getServerHost());
					RigctlClient.setServerPort(
							jsonObj.has("rigctldPort") ? jsonObj.getInt("rigctldPort") : RigctlClient.getServerPort());
					jsonSerialPort = jsonObj.has("qmxDevice") ? jsonObj.getString("qmxDevice") : QSerialPort.getPortName() ;

					qmx.init(); // Initialize QMX with provided parameters
				} catch (JSONException err) {
					System.out.println("Error" + err.toString()); // Catch JSON-related parsing issues
					System.exit(1); // Exit on failure
				}
			} else {
				// Handles case where the specified file does not exist
				System.out.println("Error could not open file:" + args[0]);
				System.exit(1); // Exit with error status
			}

			// MainClass mc = new MainClass(); // Instantiate the main class
			MainClass.setMyCall(myCall); // Set the callsign for processing
			QSerialPort serialPort = new QSerialPort(jsonSerialPort);
			// Initiate GUI elements
			GUI.xmain(args, qmx, new MainClass(), serialPort); // Initialize GUI with provided parameters

		} catch (Exception e) {
			// Catches unexpected errors during application execution
			System.err.println("XML-RPC Error: " + e.getMessage());
			e.printStackTrace(); // Output the stack trace for analysis
			System.exit(1); // Exit with error status
		}
	}

	/**
	 * Converts a float value to a scaled string representation in hexadecimal
	 * format. Expected Input: - float value: the value to be scaled - float
	 * valuemin: the minimum value of the range - float valuemax: the maximum value
	 * of the range - int range: the scaling range to apply Expected Output: - A
	 * string representing the scaled hexadecimal value.
	 */
	public static String intToStringScaled(float value, float valuemin, float valuemax, int range) {
		float fdecval = ((value - valuemin) / (valuemax - valuemin)) * range; // Scale value within the specified range
		int intval = (int) fdecval; // Convert scaled float to integer
		String retval = ""; // Initialize string to hold the return value

		// Construct return value based on the integer value obtained from scaling.
		if (intval > 200) {
			retval = "x02 "; // Prefix for values above 200
			intval -= 200; // Adjust integer value for representation
		} else if (intval > 100) {
			retval = "0x01 "; // Prefix for values above 100
			intval -= 100; // Adjust integer value for representation
		}
		retval = retval + "x" + String.format("%02d", intval).toUpperCase(); // Format and convert to hex
		dispDebug("range convert val:" + value + " min:" + valuemin + " max:" + valuemax + " range:" + range + " --> "
				+ retval); // Debug output
		return retval; // Return the formatted string
	}

	/**
	 * Converts a string to an integer after stripping non-numeric characters.
	 * Handles potential conversion errors gracefully. Expected Input: - String arg:
	 * the string to be converted Expected Output: - int: the integer value derived
	 * from the string, defaults to 0 on error.
	 */

	public int getIntFromString(String arg) {
		try {
			if (arg == null || arg.length() < 1) {
				return 0; // Return 0 for null or empty strings
			}
			String nums = arg.replaceAll("[^0-9]", ""); // Remove all non-numeric characters
			if (nums == null || nums.length() < 1) {
				return 0; // Return 0 if no numeric characters found
			}
			int val = Integer.parseInt(nums); // Parse the cleaned numeric string to int
			return val; // Return the parsed integer
		} catch (Exception e) {
			System.err.println("Conversion Error: " + e.getMessage()); // Log any conversion errors
			e.printStackTrace(); // Provide a stack trace for debugging
			return 0; // Default to 0 on error
		}
	}

	/**
	 * Converts a string to a float after stripping non-numeric characters. Handles
	 * potential conversion errors gracefully. Expected Input: - String arg: the
	 * string to be converted Expected Output: - float: the float value derived from
	 * the string, defaults to 0 on error.
	 */
	public float getFloatFromString(String arg) {
		try {
			if (arg == null || arg.length() < 1) {
				return 0; // Return 0 for null or empty strings
			}
			String nums = arg.replaceAll("[^0-9\\.]", ""); // Remove all non-numeric characters
			if (nums == null || nums.length() < 1) {
				return 0; // Return 0 if no numeric characters found
			}
			float val = Float.parseFloat(nums); // Parse the cleaned numeric string to float
			return val; // Return the parsed float
		} catch (Exception e) {
			System.err.println("Conversion Error: " + e.getMessage()); // Log any conversion errors
			e.printStackTrace(); // Provide a stack trace for debugging
			return 0; // Default to 0 on error
		}
	}

	/**
	 * Fetches the corresponding mode string for a given integer identifier (mapped
	 * from UICodeMap). Expected Input: - int ival: the integer identifier for the
	 * mode Expected Output: - String: the mode string associated with the
	 * identifier, or null if not found.
	 */
	public String getModeString(int ival) {
		return UICodeMap.get(Integer.toString(ival)); // Lookup mode string in the mapping
	}

	// get step size in HZ from the UI spinner string
	/**
	 * Retrieves the step size in Hertz based on a string identifier. Expected
	 * Input: - String stepStr: the string identifier for the step size Expected
	 * Output: - int: the corresponding step size in Hertz, or 0 if not found.
	 */
	public int getStepString(String stepStr) {
		return getIntFromString(UICodeMap.get(stepStr)); // Retrieve and convert to int using mappings
	}

	/**
	 * Sets the mode based on a string identifier, converting it to an integer.
	 * Expected Input: - String sval: the string identifier for the mode Expected
	 * Output: - int: the integer representation of the mode, determined by the step
	 * string.
	 */
	public int setModeInt(String sval) {
		return getStepString(sval); // Delegate to getStepString for mapped value
	}

	/**
	 * Utility method to display debug messages when debug mode is enabled. Expected
	 * Input: - String $msg: the message to be displayed Expected Output: - None.
	 * Outputs to console if debug is enabled.
	 */
	private static void dispDebug(String $msg) {
		if (debug) { // Check if debugging is enabled
			System.out.println("Debug: " + $msg); // Print debug message
		}
	}

	/**
	 * Extracts parameters from a JSON file and returns them as a JSONObject.
	 * Expected Input: - String json_file: the path to the JSON file Expected
	 * Output: - JSONObject: the parsed JSON object, or exits on IOException.
	 */
	public static JSONObject getParamAsJSON(String json_file) {
		File file = new File(json_file); // Create a File object from the provided path
		try {
			// Read all bytes from the file, converting to a String
			String content = new String(Files.readAllBytes(Paths.get(file.toURI())));
			JSONObject json = new JSONObject(content); // Parse the content as a JSON object
			return json; // Return the JSON object
		} catch (IOException e) {
			e.printStackTrace(); // Log any IO errors encountered
			System.exit(1); // Exit on failure to handle the JSON file
		}
		return null; // This line is unreachable due to System.exit(1) above
	}

	/**
	 * Retrieves the current callsign. Expected Output: - String: the current
	 * callsign held in myCall.
	 */
	public String getMyCall() {
		return myCall; // Return the stored callsign
	}

	/**
	 * Sets the callsign to a new value. Expected Input: - String myCallin: the new
	 * callsign to set Expected Output: - None. Updates the myCall variable.
	 */
	public static void setMyCall(String myCallin) {
		myCall = myCallin; // Update myCall with the provided value
	}

	/**
	 * Converts an integer frequency to a human-readable string in MHz. Expected
	 * Input: - int ifreq: the frequency in Hz Expected Output: - String: the
	 * frequency formatted as a string in MHz.
	 */
	public String getHumanFreqString(int ifreq) {
		float f = ifreq / 1000000.0f; // Convert frequency from Hz to MHz
		return String.format("%#11.6f MHz", f); // Format the frequency as a string
	}

	/**
	 * @param responseString
	 * @return
	**/
	public String getCWTextFromTB(String responseString) {
		// TODO Auto-generated method stub
		if (responseString.length() < 6)
			return "";
		else {
			return extractSubstring(responseString, 5, ';');
		}

	}

	/**
	 * Extract a subsctring from a string
	 * @param input   =input string
	 * @param startPos  = integer start position 0...
	 * @param endChar = delimiter at end of string
	 * @return
	 */
	public static String extractSubstring(String input, int startPos, char endChar) {
		// Validate input parameters
		if (input == null || input.isEmpty()) {
			return "";
		}

		if (startPos < 0 || startPos >= input.length()) {
			return "";
		}

		// Find the index of the end character starting from startPos
		int endIndex = input.indexOf(endChar, startPos);

		// If end character not found, return empty string or the rest of the string
		if (endIndex == -1) {
			return ""; // or return input.substring(startPos) to get the rest
		}

		// Extract and return the substring
		return input.substring(startPos, endIndex);
	}

}
