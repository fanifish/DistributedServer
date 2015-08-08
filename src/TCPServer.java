import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * TCPServer emulates the library and calls TCPServerThread to handle client
 * requests
 * 
 * @author Faniel Ghirmay and Jose Garcia
 * 
 */
public class TCPServer implements Runnable {
	BookStore books;
	int ServerPort;
	int serverId;
	int id;
	String ip[];
	int port[];
	ServerLink peers;
	LamportMutex mutex;
	InetAddress addrs;
	int count;
	int crushCount;
	int crushTime;
	boolean crush = false;
	ServerSocket listener;
	Integer objectRecieve = null;
	public TCPServer(int port, BookStore books, String ip[], int id,int tcpPort[]) {
		ServerPort = port;
		this.books = books;
		this.id = id;
		count = 0;
		this.ip = ip;
		this.port = tcpPort;
		crushCount = 0;
		crushTime = 0;
		crush = false;
		try {
			listener = new ServerSocket(ServerPort);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public String recieve() {
		try {		
			Socket s = null; // will be the socket to client
			//(s = listener.accept()) != null
		//	System.out.println("server with id "+ id +" started");
			while ((s = listener.accept()) != null) {
					//System.out.println("Server is "+ id);
					Thread t = new Thread(new TCPServerThread(s, books, this.mutex,
						this.id, objectRecieve, listener)); // handling books
					t.start();
					if(mutex.counter == crushCount && crush){
						try {
							listener.close(); // close connection
						//	System.out.println("#########################Server "+id+" Crush##############");
							Thread.currentThread().sleep(crushTime);
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							//e.printStackTrace();
				//System.out.println("Interuptted");
						}finally{
					//		System.out.println("Server "+ id +" awake");
							crush = false;
							listener = new ServerSocket(ServerPort);
							mutex.peers.tryToConnect();// connect with other servers
							mutex.counter = 0;
						}
					}
			}	
		} catch (IOException e) {
			System.err.println("Server aborted:" + e);
		}
		return new String();
	}
	public void addServerLink(ServerLink link){
		peers = link;
		mutex = new LamportMutex(link);
		peers.addMutex(mutex);
	}
	public void assertCrush(int crushCount, int crushTime){
		this.crushCount = crushCount;
		this.crushTime = crushTime;
		mutex.counter = 0;
		crush = true;
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		this.recieve();
	}

}
