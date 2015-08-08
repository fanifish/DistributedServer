import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.BindException;
import java.net.ConnectException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Set;
import java.util.StringTokenizer;

public class ServerLink {
	PrintWriter dataOut[];
	BufferedReader dataIn[];
	BufferedReader dIn;
	int port[];
	String ip[];
	Socket link[];
	ServerSocket listner;
	int myId, numOfServers;
	LamportMutex m;
	HashSet<Integer> serversAvailable; // holds the id of the server that is available
	ServerLink() {
		
	}
	ServerLink(int id, int numOfServers, int port[], String ip[]) {
		myId = id;
		this.numOfServers = numOfServers;
		dataOut = new PrintWriter[numOfServers];
		dataIn = new BufferedReader[numOfServers];
		link = new Socket[numOfServers];
		this.ip = ip;
		this.port = port;	
		serversAvailable = new HashSet<Integer>();
		tryToConnect();
	}
	public void addMutex(LamportMutex m){
		this.m = m;
	}
	/**
	 * Connects to the available servers if there r none then it tries to go solo
	 */
	public void tryToConnect(){
		for(int i=0; i < numOfServers; i++){
			//try {
				if(i != myId){
					try{
						link[i] = new Socket(ip[i], port[i]);
					}catch(ConnectException e){
						continue;
					} catch (UnknownHostException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					getLink(i);
					if(link[i].isConnected()){
						serversAvailable.add(i);
						sendMessage(i, myId + " hello" + " " + "null");
						closeLink(i);
					}	
				}
		//	} catch (UnknownHostException e) {
				// TODO Auto-generated catch block
		//		e.printStackTrace();
		//	} catch (IOException e) {
				// TODO Auto-generated catch block
		//		e.printStackTrace();
		//	}
		}
	}
	public void sendMessage(int src, int dest, String message, String type) {
		String toSend = new String(src + " " + type + " " + message);
		getLink(dest);
		this.dataOut[dest].println(toSend);
		this.dataOut[dest].flush();	
		closeLink(dest);
	}
	public void sendMessage(int dest, String message, int timeStamp) {
		String toSend = new String(myId + " " + message + " " + timeStamp);
		getLink(dest);
		this.dataOut[dest].println(toSend);
		this.dataOut[dest].flush();
		closeLink(dest);
	}
	public void sendMessage(int dest, String message) {
		String toSend = new String(message);
		getLink(dest);
		this.dataOut[dest].println(toSend);
		this.dataOut[dest].flush();
		closeLink(dest);
	}
	public Message recieveMessage(int fromId) {
		String getline = null;
		try {
			getline = dataIn[fromId].readLine();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			// e.printStackTrace();
		}
		StringTokenizer st = null;
		if (getline != null) {
		//	Util.println("Message from server recieved");
			st = new StringTokenizer(getline);
		}
		return Message.parseMsg(st);
	}
	public int getMyId(){
		return myId;
	}
	public int getNumOfServers(){
		return numOfServers;
	}
	public synchronized void addServer(int id, Socket s, BookStore book, LamportMutex mutex){
		serversAvailable.add(id);		
		sendMessage(id, myId + " "+"update"+" ");
		// update the requesting servers book store
		for (String key : book.checkedOutList.keySet()) {
			if(!book.checkedOutList.get(key).equals("0")){
				sendMessage(id,"Server "+ book.checkedOutList.get(key) + " " + key + " " + "reserve");
			}	
		}
		// update the queue of the requesting server
		for(int i=0; i < mutex.q.length; i++){
			sendMessage(id,"Server "+ i+ " "+ mutex.q[i]+" "+"Queue");
		}
		// update the vector clock
		sendMessage(id,"Server "+ myId + " "+ mutex.v.clock[myId] +" "+"clock");
		broadcastMessage("Server "+ id + " " + myId + " "+ "updated" );
	}
	public void getLink(int dest){
		if(link[dest] == null){
			try {
				try{
					link[dest] = new Socket(ip[dest], port[dest]);
				}catch(ConnectException e){
					serversAvailable.remove(dest);
					m.q[dest] = Symbols.Infinity; // incase server was abraptly removed from connection
				}
				dataOut[dest] = new PrintWriter(link[dest].getOutputStream());
				dataIn[dest] =  new BufferedReader( new InputStreamReader(link[dest].getInputStream()));
			} catch (UnknownHostException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}else{
			try {
				dataOut[dest] = new PrintWriter(link[dest].getOutputStream());
				dataIn[dest] =  new BufferedReader( new InputStreamReader(link[dest].getInputStream()));
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}	
		}
	}
	public void closeLink(int id){
		if(link[id] != null){
		try {
			link[id].close();
			link[id] = null;
		} catch (IOException e) {
			// TODO Auto-generated catch block
		//	e.printStackTrace();
		}
		}
	}
	public boolean isStillConnected(int id){
		if(link[id] == null){
			try {
				try{
					link[id] = new Socket(ip[id], port[id]);
				}catch(ConnectException e){
					serversAvailable.remove(id);
					return false;
				}
				
			} catch (UnknownHostException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		closeLink(id);
		return true;
	}
	public synchronized void broadcastMessage(String req, int time){
		for(int i: serversAvailable){
			if(i != myId && isStillConnected(i)){
				sendMessage(i, req, time );
			}
		}
	}
	public synchronized void broadcastMessage(String req){
		for(int i: serversAvailable){
			if(i != myId && isStillConnected(i)){
				sendMessage(i, req);
			}
		}
	}
}
