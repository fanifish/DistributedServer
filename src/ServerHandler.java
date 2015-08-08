import java.util.StringTokenizer;

/**
 * Includes methods needed to handle requests from other instances of a server
 * @author faniel Ghirmay and jose Garcia
 *
 */
public class ServerHandler {
	ServerHandler(){
		
	}
	public void serverMessageHandler(LamportMutex mutex, int time, int src, String booknumber){
		if(mutex != null){
			mutex.handleMsg(time, src, booknumber);
		}
	} 
	public void serverMessage(String booknumber, String clientid, StringTokenizer tag, LamportMutex mutex){
		if(booknumber.equals("request")){  // to distinguish from server and client req
			int src = new Integer(clientid);
			int time = new Integer(tag.nextToken());
			serverMessageHandler(mutex, time, src, booknumber);
			return;
		}else if(booknumber.equals("release")){
			int src = new Integer(clientid);
			int time = new Integer(tag.nextToken());
			serverMessageHandler(mutex, time, src, booknumber);
			return;
		}else if(booknumber.equals("ack")){
			int src = new Integer(clientid);
			int time = new Integer(tag.nextToken());
			serverMessageHandler(mutex, time, src, booknumber);
			return;
		}
	}
	/**
	 * Handles the server message that ensures consistency
	 * @param st
	 * @param book
	 */
	public void serverUpdateHandler(StringTokenizer st, BookStore book, LamportMutex mutex){
		String clientId = st.nextToken();
		String bookNumber = st.nextToken();
		String request = st.nextToken();
		
		if(request.equals("reserve")){			
			book.getBook(bookNumber, clientId);	// make the book unavailable
		}else if(request.equals("return")){
			book.returnBook(bookNumber, clientId);
		}else if(request.equals("Queue")){
				mutex.q[new Integer(clientId)]  = new Integer(bookNumber); 
		}else if(request.equals("updated")){
				mutex.update = false;
		}else if(request.equals("clock")){
				mutex.v.receiveAction(new Integer(clientId), new Integer(bookNumber));
		}
	}
	
}
