import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
/**
 * Thread safe implimentation of a book shelf
 * 
 * @author Faniel Ghirmay and Jose Garcia
 *
 */
public class BookStore implements Serializable {
	int capacity;
	
	HashMap<String, String> checkedOutList;
	
	public BookStore(int capacity) {
		this.capacity = capacity;
		checkedOutList = new HashMap<String, String>(capacity);
		for (int i = 0; i < capacity; i++) {
			checkedOutList.put("b"+i, "0");
		}
	}

	public synchronized String getBook(String book, String client) {
		String retVal;
		if(checkedOutList.get(book).equals("0")){
			checkedOutList.put(book, client);
			return book;
		}
		return "fail";
	}

	public synchronized String returnBook(String book, String client) {
		if(checkedOutList.get(book).equals(client)){
			checkedOutList.put(book, "0");
			return "returned";
		}
		return "fail";
	}
}
