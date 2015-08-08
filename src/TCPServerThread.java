import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.PrintWriter;
import java.net.ConnectException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.channels.SocketChannel;
import java.util.NoSuchElementException;
import java.util.Scanner;
import java.util.StringTokenizer;

public class TCPServerThread implements Runnable {
	Socket theClient;
	BookStore books;
	LamportMutex mutex;
	ServerHandler handler;
	ServerSocket listener;
	int ack[];
	int myId;
	int crushCount;
	int crushTime;
	int k;
	Integer object;

	public TCPServerThread(Socket s, BookStore books, LamportMutex mutex,
			int id, Integer object, ServerSocket listener) {
		this.books = books;
		k = 0;
		theClient = s;
		this.mutex = mutex;
		myId = id;
		ack = new int[mutex.N];
		handler = new ServerHandler();
		this.object = object;
		this.listener = listener;
	}

	public void run() {
		boolean client = false;
		StringTokenizer tag = null;
		String clientid = null;
		String booknumber = null;
		PrintWriter pout = null;
		InputStream input;
		try {
			Scanner sc = new Scanner(theClient.getInputStream());
			pout = new PrintWriter(theClient.getOutputStream());
			String command = sc.nextLine();
			//System.out.println(myId + " recieved message " + command);
			// StringTokenizer st = new StringTokenizer(command);
			// String msg = st.nextToken();
			tag = new StringTokenizer(command);
			clientid = tag.nextToken();
			if (clientid.equals("Server")) { // if it is a server update message
												// to ensure consistency
				handler.serverUpdateHandler(tag, books, mutex);
				return;
			}
			booknumber = tag.nextToken();
		} catch(NoSuchElementException e){
			return;
		}catch (IOException e) {
			System.err.println(e);
		}
		if (booknumber.equals("request")) { // to distinguish from server and
											// client req
			int src = new Integer(clientid);
			int time = new Integer(tag.nextToken());
			if (mutex != null) {
				mutex.handleMsg(time, src, booknumber);
			} 
			close();
			return;
		} else if (booknumber.equals("release")) {
			int src = new Integer(clientid);
			int time = new Integer(tag.nextToken());
			if (mutex != null) {
				mutex.handleMsg(time, src, booknumber);
			}
			close();
			return;
		} else if (booknumber.equals("ack")) {
			int src = new Integer(clientid);
			int time = new Integer(tag.nextToken());
			if (mutex != null) {
				mutex.handleMsg(time, src, booknumber);
			} 
			close();
			return;
		} else if (booknumber.equals("hello")) { // another server wants to join
			mutex.update = true;
			Util.println("Server " + clientid + " says hello and server "+ myId+" requests update cs" );
			mutex.requestCS();
			Util.println("Server " +myId +" gets cs");
				if(mutex.update){
					Util.println("Server " + myId + " is updeting  " + clientid );
					mutex.peers.addServer(new Integer(clientid), theClient, books, mutex);
				}	
			mutex.releaseCS();
			Util.println("Server " + myId +" releases update cs");
			return;
		} else if (booknumber.equals("update")) { // recieve update from server
			int src = new Integer(clientid);
			Util.println(myId + " recieved update from " + clientid);
			object = src; // signal we expect an object
			close();
			return;
		}

		// else server handles client request
		// logic for handling server request
		String retVal = new String();

		String request = tag.nextToken();
		String toSend = "" + clientid + " " + booknumber;
		mutex.counter++;
		if (request.equals("reserve")) {
			Util.println(myId + " requestCS");
			mutex.requestCS();// wait to enter the cs
			Util.println(myId + " In CS");
			if(booknumber == null || clientid == null){
				return;
			}
			String retBook = books.getBook(booknumber, clientid);
			if (retBook.equals("fail")) {
				retVal = new String("fail " + clientid + " " + booknumber);
			} else {
				System.out.println("Ret book is " + retBook);
				 mutex.peers.broadcastMessage("Server " + clientid + " "
                                                + booknumber + " " + request);
                                retVal = new String(clientid + " " + retBook);
			}

		} else if (request.equals("return")) {
			String ret = books.returnBook(booknumber, clientid);
			mutex.peers.broadcastMessage("Server " + clientid + " "
					+ booknumber + " " + request);
			if (ret.equals("fail")) {
				retVal = "fail";
			} else {
				retVal = "returned";
			}
		}
		pout.println(retVal);
		pout.flush();
		mutex.releaseCS();
		Util.println(myId + " releaseCS");
		try {
			theClient.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void close() {
		try {
			theClient.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
