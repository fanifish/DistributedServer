import java.io.IOException;
import java.io.PrintStream;
import java.net.ConnectException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.NoSuchElementException;
import java.util.Scanner;
/**
 * Creates a TCP client connection to connect to the server
 * @author Faniel Ghirmay and Jose Garcia
 *
 */
public class TCPClient implements Runnable {
	Scanner din;
	PrintStream pout;
	Socket server;
    String nameServer[];
    int ServerPort[];
    public TCPClient(String[] host, int[] port){
    	nameServer = host;
    	ServerPort = port;
    }
	public void getSocket() {
		for(int i=0; i < nameServer.length; i++){
		 try{	
			 //nameServer[i], ServerPort[i]
			server = new Socket();	
			//server.setSoTimeout(100);
		//	System.out.println("Connecting to server " + i);
			server.connect(new InetSocketAddress(nameServer[i], ServerPort[i]), 100);
			server.setSoTimeout(100); // server times out
			din = new Scanner(server.getInputStream());
			pout = new PrintStream(server.getOutputStream());
		//	if(server.isConnected()){
		//	System.out.println("new client connection with " + ServerPort[i]);
			break; // as soon as we get a good connection we get out
		//	}
		 }catch(ConnectException e){
		//	 System.out.println("Failed attempt going to next");
			continue;
		 }catch(SocketTimeoutException e){
		//	 System.out.println("Socket timed out");
			 continue;
		 }catch(IOException e){
		//	 System.out.println("THe server is down going to next");
			 continue;
		 }
		}
	}
	/**
	 * Send TCP Message to the server
	 * @param message
	 * @return
	 * @throws IOException
	 */
	public String send(String message)
			throws SocketTimeoutException, NoSuchElementException,  ConnectException, IOException  {
		getSocket();
		pout.println(message);
		pout.flush();
		String retValue = null;
		retValue = din.nextLine();
		server.close();
		return retValue;
	}
	public void clear() throws IOException {
		getSocket();
		pout.println("clear ");
		pout.flush();
		server.close();
	}
	@Override
	public void run() {
		// TODO Auto-generated method stub
		
	}
}
