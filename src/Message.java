import java.util.StringTokenizer;

public class Message {
	int src;
	int dest;
	String type;
	String message;
	Message(int src, int dest, String type, String message){
		this.src = src;
		this.dest = dest;
		this.type = type;
		this.message = message;
	}
	 public static Message parseMsg(StringTokenizer st){
	        int srcId = Integer.parseInt(st.nextToken());
	        int destId = Integer.parseInt(st.nextToken());
	        String tag = st.nextToken();
	        String buf = st.nextToken();
	        return new Message(srcId, destId, tag, buf);
	 }
	 public int getSrc(){return src;}
	 public int getDest(){return dest;}
	 public String getType(){return type;}
	 public String getMessage(){return message;}
	 public int getMessageInt() {
	        StringTokenizer st = new StringTokenizer(message);
	        return Integer.parseInt(st.nextToken());
	}
}
