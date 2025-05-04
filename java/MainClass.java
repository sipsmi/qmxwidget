

public class MainClass {

private static boolean debug = false;

	/**
	 *
	 */

	public static void main(String[] args) {
		try {
			// initiate the base classes
			XmlRpcQmx ic7000 = new XmlRpcQmx();
			MainClass mc = new MainClass();
            // initiate the GUI
			//GUI myGui = new GUI();
			GUI.xmain(args,ic7000,mc);


			System.out.println("Rig Info: " + ic7000.getCATString( "rig.get_info"));
			System.out.println("Power: " + ic7000.getCATInteger( "rig.get_power")  + " Watts");
			System.out.println("CatResp: " + ic7000.sendCatStringmain( "x1A x05 x00 x13 "+ intToStringScaled(12,6, 60,255)));
			System.out.println("CatResp: " + ic7000.sendCatStringmain( "x1A x05 x00 x13"));

		} catch (Exception e) {
			System.err.println("XML-RPC Error: " + e.getMessage());
			e.printStackTrace();
			System.exit(1);
		}
	}

	public static String intToStringScaled(float value ,float valuemin,float valuemax, int range)
	{
		float fdecval =    ((value-valuemin)/(valuemax-valuemin)) *  range;
		int  intval = (int) fdecval;
		String retval = "";
		// not 255 return as x02 x55 - stupid is yes - all others are Hex !
		if ( intval > 200 ) { retval = "x02 "; intval -= 200; }
		else if ( intval > 100 )
		{   retval = "0x01 ";   intval -= 100; 	}
		retval = retval + "x" + String.format("%02d", intval).toUpperCase();
	    dispDebug("range convert val:"+value+" min:" + valuemin+" max:" + valuemax
				   + " range:"+range + " --> "+ retval);
		return retval;
	}

	private static void dispDebug( String $msg) {

		if (debug) {
			
			System.out.println("Debug: " + $msg);
		}

}


}
