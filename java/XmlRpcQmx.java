
//import java.awt.BorderLayout;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.apache.xmlrpc.client.XmlRpcClient;
import org.apache.xmlrpc.client.XmlRpcClientConfigImpl;

public class XmlRpcQmx {

	private static XmlRpcClient client;
	private static boolean debug = false;
	private static String server = "localhost";
	private static String serverPort = "12345";
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

	public static Map<String, String> getCatcodemap() {
		return catCodeMap;
	}

	public XmlRpcQmx() {
		setCommsIsOpen( true );
	}

	public void init() {
		try {
			// Configure the XML-RPC client
			XmlRpcClientConfigImpl config = new XmlRpcClientConfigImpl();
			config.setServerURL(new URL("http://" + getServer() + ":" + getServerPort()));
			// Create the client instance
			client = new XmlRpcClient();
			client.setConfig(config);

		} catch (Exception e) {
			System.err.println("XML-RPC Error: " + e.getMessage());
			checkErrorResponse(e);
			//e.printStackTrace();
			System.exit(1);
		}
	

	}

	public String sendCatStringmain(String arg) {
		if ( commsIsOpen) try {
			String catCode = catCodeMap.get("preamble") + arg + catCodeMap.get("postamble");
			// Object[] catString = new Object[] {};
			String catresponse = (String) client.execute("rig.cat_string", new Object[] { catCode });
			dispDebug("Sending cat string " + catCode);
			return catresponse;
		} catch (Exception e) {
			System.err.println("XML-RPC Error: " + e.getMessage());
			checkErrorResponse(e);
			//e.printStackTrace();
			return "Error";
		}
		else return "Error";

	}

	public String flrigSetInteger(String command, int value) {
		if ( commsIsOpen) try {
			String catresponse = (String) client.execute(command, new Object[] { value });
			dispDebug("Sending cat string " + command + value);
			return catresponse;
		} catch (Exception e) {
			System.err.println("XML-RPC Error: " + e.getMessage());
			checkErrorResponse(e);
			//e.printStackTrace();
			return "Error";
		}
		else return "Error";

	}

	public Integer getCATInteger(String arg) {
		if ( commsIsOpen) try {
			Object[] catString = new Object[] {};
			Integer catresponse = (Integer) client.execute(arg, catString);
			return catresponse;
		} catch (Exception e) {
			System.err.println("XML-RPC Error: " + e.getMessage());
			checkErrorResponse(e);
			//e.printStackTrace();
			return 0;
		}
		else return 0;

	}

	public String getCATString(String arg) {
		if ( commsIsOpen) try {
			Object[] catString = new Object[] {};
			String catresponse = (String) client.execute(arg, catString);
			return catresponse;
		} catch (Exception e) {
			System.err.println("XML-RPC Error: " + e.getMessage());
			checkErrorResponse(e);
			//e.printStackTrace();
			return "Error";
		}
		else return "Error";

	}

	public void dispDebug(String $msg) {

		if (debug) {
			System.out.println("Debug: " + $msg);
		}

	}

	public static String getServer() {
		return server;
	}

	public static void setServer(String server) {
		XmlRpcQmx.server = server;
	}

	public static String getServerPort() {
		return serverPort;
	}

	public static void setServerPort(String serverPort) {
		XmlRpcQmx.serverPort = serverPort;
	}

	public static boolean isCommsIsOpen() {
		return XmlRpcQmx.commsIsOpen;
	}

	private static void setCommsIsOpen(boolean setter) {
		XmlRpcQmx.commsIsOpen = setter;
	}
	
	private static void checkErrorResponse(Exception e) {
		if ( e.getMessage().matches("(?i).*Connection refused.*")  )	setCommsIsOpen( false );
		//System.out.println( "Is Open: "+XmlRpcQmx.isCommsIsOpen() + " "+ e.getMessage() );
	}
	
	

}
