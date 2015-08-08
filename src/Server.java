import java.io.Console;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Scanner;
import java.util.StringTokenizer;

/**
 * A fault tolerant Distributed server that manages the library book reservation system
 * 
 * @author Faniel Ghirmay and Jose Garcia
 * 
 */

public class Server {
	int id = 0;
	String ip_addrs = null;
	TCPServer tcpServer;
	BookStore store;
	String ip[];
	int tcp[];
	
	/**
	 * When a server is created it has the knowledge of the existance of other servers
	 * @param capacity
	 * @param tcpPort
	 * @param numOfServers
	 * @param id
	 * @param ip
	 */

	public Server(int capacity, int tcpPort[], int numOfServers, int id,
			String ip[]) {
		if (ip[id - 1].equals("127.0.0.1")) {
			store = new BookStore(capacity); // create a book store for all servers to insure fault tolerant 
			
			tcpServer = new TCPServer(tcpPort[id - 1], store, ip, id - 1, tcpPort);
			
			tcpServer.addServerLink(new ServerLink(id - 1, numOfServers,
					tcpPort, ip));
			Thread server = new Thread(tcpServer);
			server.start();
		}
	}

	public static void main(String args[]) {
		int capacity = 0;
		int numOfServers = 0;
		int id = 0;
		String ip[];
		int tcp[];
		int intCount = 0;
		int crushTime = 0;
		System.out.println("Server input");
		Scanner sc = new Scanner(System.in);
		StringTokenizer str = new StringTokenizer(sc.nextLine());

		id = new Integer(str.nextToken());
		numOfServers = new Integer(str.nextToken());
		capacity = new Integer(str.nextToken());
		ip = new String[numOfServers];
		tcp = new int[numOfServers];

		for (int i = 0; i < numOfServers; i++) {
			str = new StringTokenizer(sc.nextLine());
			ip[i] = str.nextToken(":");
			tcp[i] = new Integer(str.nextToken());
		}
		/*
		 * Starting the Library server
		 */
		Server library = new Server(capacity, tcp, numOfServers, id, ip);

		String val = null;
		while (sc.hasNext()) {
			str = new StringTokenizer(sc.nextLine());
			if (str != null) {
				if (str.nextToken().equals("crash")) {
			//		System.out.print("new crash");
					intCount = new Integer(str.nextToken());
					crushTime = new Integer(str.nextToken());
					library.tcpServer.assertCrush(intCount, crushTime);
				}
			}
		}
	}
}
