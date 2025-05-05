
public class MainClass {

	private static boolean debug = false;

	/**
	 *
	 */

	public static void main(String[] args) {
		try {
			// initiate the base classes
			XmlRpcQmx qmx = new XmlRpcQmx();
			MainClass mc = new MainClass();
			// initiate the GUI
			// GUI myGui = new GUI();
			GUI.xmain(args, qmx, mc);

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

}
