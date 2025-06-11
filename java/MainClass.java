import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import org.json.JSONException;
import org.json.JSONObject;

public class MainClass {

	private static boolean debug = true;
	private static String myCall = "GX0XXX";

	public MainClass() {
		try {

		} catch (Exception e) {
			System.err.println("Main Class Configuraiton Error: " + e.getMessage());
			// e.printStackTrace();
			// System.exit(1);
		}
	}

	/**
	 *
	 */

	public static void main(String[] args) {
		try {
			// initiate the base classes
			XmlRpcQmx qmx = new XmlRpcQmx();
			QSerialPort serialPort = new QSerialPort();
			// get parameters
			JSONObject jsonObj = null;
			// Get the paramgers
			// get the command line arguments
			// check files exist and ebough args
			int argc = args.length;
			if (argc != 1) { // no parameter given
				System.out.println("Useage(" + argc + "): <command> <configuration file.json>");
				System.exit(1);
			} else if (Files.exists(Paths.get(args[0]))) { // get from file
				try {
					jsonObj = getParamAsJSON(args[0]); // get params into JSON
					// TODO Auto-generated constructor stub
					dispDebug(jsonObj.toString());
					myCall = jsonObj.getString("Callsign");
					XmlRpcQmx.setServer(jsonObj.has("Server") ? jsonObj.getString("Server") : XmlRpcQmx.getServer());
					XmlRpcQmx.setServerPort(
							jsonObj.has("ServerPort") ? jsonObj.getString("ServerPort") : XmlRpcQmx.getServerPort());
					qmx.init();

				} catch (JSONException err) {
					System.out.println("Error" + err.toString());
					System.exit(1);
				}
			} else {
				System.out.println("Error could not open file:" + args[0]);
				System.exit(1);
			}

			MainClass mc = new MainClass();
			mc.setMyCall(myCall);
			// initiate the GUI
			// GUI myGui = new GUI();
			GUI.xmain(args, qmx, mc, serialPort);

		} catch (Exception e) {
			System.err.println("XML-RPC Error: " + e.getMessage());
			e.printStackTrace();
			System.exit(1);
		}
	}

	public static String intToStringScaled(float value, float valuemin, float valuemax, int range) {
		float fdecval = ((value - valuemin) / (valuemax - valuemin)) * range;
		int intval = (int) fdecval;
		String retval = "";
		// not 255 return as x02 x55 - stupid is yes - all others are Hex !
		if (intval > 200) {
			retval = "x02 ";
			intval -= 200;
		} else if (intval > 100) {
			retval = "0x01 ";
			intval -= 100;
		}
		retval = retval + "x" + String.format("%02d", intval).toUpperCase();
		dispDebug("range convert val:" + value + " min:" + valuemin + " max:" + valuemax + " range:" + range + " --> "
				+ retval);
		return retval;
	}

	public int getIntFromString(String arg) {
		try {
			if (arg == null || arg.length() < 1) {
				return 0;
			}
			String nums = arg.replaceAll("[^0-9]", "");
			if (nums == null || nums.length() < 1) {
				return 0;
			}
			int val = Integer.parseInt(nums);
			return val;
		} catch (Exception e) {
			System.err.println("Conversin Error: " + e.getMessage());
			e.printStackTrace();
			return 0;
		}

	}

	public float getFloatFromString(String arg) {
		try {
			if (arg == null || arg.length() < 1) {
				return 0;
			}
			String nums = arg.replaceAll("[^0-9\\.]", "");
			if (nums == null || nums.length() < 1) {
				return 0;
			}
			float val = Float.parseFloat(nums);
			return val;
		} catch (Exception e) {
			System.err.println("Conversin Error: " + e.getMessage());
			e.printStackTrace();
			return 0;
		}

	}

	public String getModeString(int ival) {
		try {
			String retval = "CW";
			switch (ival) {
			case 1:
				retval = "LSB";
				break;
			case 2:
				retval = "USB";
				break;
			case 3:
				retval = "CW";
				break;
			case 6:
				retval = "FSK";
				break;
			case 7:
				retval = "CWR";
				break;
			case 9:
				retval = "FSR";
				break;

			default:
				break;

			}

			return retval;
		} catch (Exception e) {
			e.printStackTrace();
			return "Error";
		}

	}
	
	public int getStepString(String stepStr) {
		try {
			int retval = 500;
			switch (stepStr) {
			case "100 Hz":
				retval = 100;
				break;
			case "500Hz":
				retval = 500;
				break;
			case "1 KHz":
				retval = 1000;
				break;

			default:
				break;

			}

			return retval;
		} catch (Exception e) {
			e.printStackTrace();
			return 500;
		}

	}

	public int setModeInt(String sval) {
		try {
			int retval = 1;
			switch (sval) {
			case "LSB":
				retval = 1;
				break;
			case "USB":
				retval = 2;
				break;
			case "CW":
				retval = 3;
				break;
			case "FSK":
				retval = 6;
				break;
			case "CWR":
				retval = 7;
				break;
			case "FSR":
				retval = 9;
				break;

			default:
				break;

			}

			return retval;
		} catch (Exception e) {
			e.printStackTrace();
			return 0;
		}

	}

	private static void dispDebug(String $msg) {

		if (debug) {
			System.out.println("Debug: " + $msg);
		}

	}

	// Little utility to extract field to JSON object hierachy
	/**
	 * @param json_file
	 * @return
	 */
	public static JSONObject getParamAsJSON(String json_file) {
		File file = new File(json_file);
		try {
			String content = new String(Files.readAllBytes(Paths.get(file.toURI())));
			JSONObject json = new JSONObject(content);
			return json;
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
		}
		return null;
	}

	public String getMyCall() {
		return myCall;
	}

	public void setMyCall(String myCallin) {
		myCall = myCallin;
	}
	
    public String getHumanFreqString(int ifreq) {
        float f =  ifreq / 1000000.0f;
        return String.format("%#11.6f MHz",f);
    }

}
