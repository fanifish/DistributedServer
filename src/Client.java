import java.io.IOException;
import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.util.NoSuchElementException;
import java.util.Scanner;
import java.util.StringTokenizer;

public class Client {
	static String ip_addresses[] = null;
	static int portNumber[] = null;
	public Client() {

	}

	public static void main(String args[]) {
		TCPClient tcpReq = null;	
		Scanner sc = new Scanner(System.in);
		String next = new String(sc.nextLine());
		StringTokenizer str;
		str = new StringTokenizer(next);
		String clientId = str.nextToken();
		int numOfServers = new Integer(str.nextToken());
		ip_addresses = new String[numOfServers];
		portNumber = new int[numOfServers];
		for(int i=0; i < numOfServers; i++){
			str = new StringTokenizer(sc.nextLine());
			ip_addresses[i] = str.nextToken(":"); 
			portNumber[i] =	new Integer(str.nextToken());	
		}
		String booknumber = null;
		String request;
		while (sc.hasNext()) {
			try{
			next = sc.nextLine();
			str = new StringTokenizer(next);
			booknumber = str.nextToken();
			if (booknumber.equals("sleep")) {
				int time = new Integer(str.nextToken());
				try {
					Thread.currentThread().sleep(time);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					// e.printStackTrace();
				} finally {
					continue;
				}
			}
			}catch(NoSuchElementException e){
				//e.printStackTrace();
				//System.out.println("no such elemnt");
				continue;
			}
			request = str.nextToken();
			tcpReq = new TCPClient(ip_addresses, portNumber);
			String inst = clientId + " " + booknumber
					+ " " + request;
			String response = null;
			try {
				
				while(true){
					try{
						response = tcpReq.send(inst);
					}catch(SocketTimeoutException e){
						tcpReq.server.close();
						continue; // try to send again if server timed out
					}catch(NoSuchElementException e){
						tcpReq.server.close();
						continue;
					}catch(IOException e){
				//		System.out.println("IO exception");
					}catch(NullPointerException e){
						if(tcpReq == null){
			//				System.out.println("tcp client null");
						}
						continue;
					}
					break;
				}
				if(response.equals("returned")){
					response = "free "+clientId +" " +booknumber;
				}else if(response.equals("fail")){
					response = "fail "+clientId + " "+booknumber;
				}
				System.out.println(response);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				// e.printStackTrace();
			//	System.out.println("io exception");
			} 	
		}
	}
}
