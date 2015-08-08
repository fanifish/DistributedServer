import java.util.StringTokenizer;

/**
 * Includes a number of methods to handle client request
 * @author ffg92
 *
 */
public class ClientHandler {
	ClientHandler(){
		
	}
	public String handleRequest(String booknumber, String clientid, StringTokenizer tag, LamportMutex mutex, BookStore books){
		String retVal = new String();
		
		String request = tag.nextToken();
		String toSend = ""+clientid+" "+booknumber;
		
		if(request.equals("reserve")){
			System.out.println("requesting cs");
			
			System.out.println("got in critical section");
			String retBook = books.getBook(booknumber, clientid);
			if(retBook != null){
				System.out.println("reserving book");
				mutex.peers.broadcastMessage(clientid+" "+booknumber+" "+request);
				retVal = new String("c"+clientid + " "+ retBook);
			}else{
				retVal = new String("fail " + "c"+clientid + " " + booknumber);
			}
			
		}else if(request.equals("return")){
			books.returnBook(booknumber, clientid);
			mutex.peers.broadcastMessage(clientid+" "+booknumber+" "+request);
		}
		return retVal;
	}
}
