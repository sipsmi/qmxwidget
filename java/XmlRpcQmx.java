//import java.awt.BorderLayout;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;



import org.apache.xmlrpc.client.XmlRpcClient;
import org.apache.xmlrpc.client.XmlRpcClientConfigImpl;

public class XmlRpcQmx {

	private static XmlRpcClient client;
	private static boolean debug = false;

	private static final Map<String, String> catCodeMap = new HashMap<String, String>() {
		private static final long serialVersionUID = 1L;
		{
			put("preamble","");
			put("get_wpm","" );
			put("postamble",";");
			put("ptton", "TQ1");
			put("pttoff", "TQ0");
		}
	};

	public XmlRpcQmx() {
		try {
		// Configure the XML-RPC client
		XmlRpcClientConfigImpl config = new XmlRpcClientConfigImpl();
		config.setServerURL(new URL("http://localhost:12345"));
		// Create the client instance
		client = new XmlRpcClient();
		client.setConfig(config);
	} catch (Exception e) {
		System.err.println("XML-RPC Error: " + e.getMessage());
		e.printStackTrace();
	}

	}



	public  String sendCatStringmain( String arg) {
		try {
			String catCode =  catCodeMap.get("preamble")+  arg + catCodeMap.get("postamble");
			//Object[] catString = new Object[] {};
			String catresponse = (String) client.execute("rig.cat_string", new Object[] {catCode});
			dispDebug("Sending cat string "+  catCode );
			return catresponse;
		} catch (Exception e) {
			System.err.println("XML-RPC Error: " + e.getMessage());
			e.printStackTrace();
			return "Error";
		}

	}
	
	public  String flrigSetInteger(String command, int value) {
		try {
			String catresponse = (String) client.execute(command, new Object[] {value});
			dispDebug("Sending cat string "+  command + value );
			return catresponse;
		} catch (Exception e) {
			System.err.println("XML-RPC Error: " + e.getMessage());
			e.printStackTrace();
			return "Error";
		}

	}

	public  Integer getCATInteger(String arg) {
		try {
			Object[] catString = new Object[] {  };
			Integer catresponse = (Integer) client.execute(arg, catString);
			return catresponse;
		} catch (Exception e) {
			System.err.println("XML-RPC Error: " + e.getMessage());
			e.printStackTrace();
			return 0;
		}

	}

	public String getCATString( String arg) {
		try {
			Object[] catString = new Object[] {  };
			String catresponse = (String) client.execute(arg, catString);
			return catresponse;
		} catch (Exception e) {
			System.err.println("XML-RPC Error: " + e.getMessage());
			e.printStackTrace();
			return "Error";
		}

	}
	
	private void dispDebug(String $msg) {

		if (debug) {
			System.out.println("Debug: " + $msg);
		}

	}

}
