
import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class RigctlClient {
	
	private static Socket socket;
	private static BufferedWriter writer ;
	private static BufferedReader reader ;
	
    public RigctlClient(String serverHost, int serverPort) {
		super();
		try
		{
		RigctlClient.socket = new Socket(serverHost, serverPort);
		RigctlClient.writer = new BufferedWriter(
                new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.US_ASCII));
		RigctlClient.reader = new BufferedReader(
                new InputStreamReader(socket.getInputStream(), StandardCharsets.US_ASCII));
		}
		catch (IOException e) {
            System.err.println("Error communicating with rigctld: " + e.getMessage());
        }
		
	}

	public void sendFrequency( long frequency) throws IOException {
        try  {

            // Construct the command to set frequency
            String command = "F " + frequency + "\n";
            writer.write(command);
            writer.flush();

            // Read and verify response
            String response = reader.readLine();
            if (response == null || !response.startsWith("RPRT 0")) {
                throw new IOException("Failed to set frequency. Server response: " + response);
            }
        }
		catch (IOException e) {
            System.err.println("Error communicating with rigctld: " + e.getMessage());
        }
    }

    
}