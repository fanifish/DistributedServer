
public class LamportMutex implements Lock{
    DirectClock v;
    int[] q; // request queue
    ServerLink peers;
    int N;
    int myId;
    int counter = 0;
    boolean update = false;
    public LamportMutex(ServerLink peers) {
    	this.peers = peers;
    	myId = peers.getMyId();
    	N = peers.numOfServers;	
        v = new DirectClock(N, myId);
        q = new int[N];
        for (int j = 0; j < N; j++) 
            q[j] = Symbols.Infinity;
    }
    public synchronized void requestCS() {
        v.tick();
        q[myId] = v.getValue(myId);
        peers.broadcastMessage("request", q[myId]);
    //    snapShot();
    //    vsnapShot();
        while (!okayCS() && !peers.serversAvailable.isEmpty()) // if there are no other servers run solo
           myWait();
 //       snapShot();
   //     vsnapShot();
    }
    public synchronized void releaseCS() {
        q[myId] = Symbols.Infinity;
        peers.broadcastMessage("release", v.getValue(myId));
    //    snapShot();
    }
    boolean okayCS() {
    	for(int j: peers.serversAvailable){
		//	if(peers.isStillConnected(j)){
    		Util.println(j+"");
				if (isGreater(q[myId], myId, q[j], j))
	                return false;
	            if (isGreater(q[myId], myId, v.getValue(j), j))
	                return false;
		//	}
		}
    	/*
        for (int j = 0; j < N; j++){
            if (isGreater(q[myId], myId, q[j], j))
                return false;
            if (isGreater(q[myId], myId, v.getValue(j), j))
                return false;
        }
        */
        return true;
    }
    boolean isGreater(int entry1, int pid1, int entry2, int pid2) {
        if (entry2 == Symbols.Infinity) return false;
        //if (entry2 == 0) return false;
        return ((entry1 > entry2) 
                || ((entry1 == entry2) && (pid1 > pid2)));
    }
    public synchronized void handleMsg(int m, int src, String tag) {
        int timeStamp = m;
        v.receiveAction(src, timeStamp);
        if (tag.equals("request")) {
        	Util.println(src +" request " +" " + timeStamp +"-->" + myId);
            q[src] = timeStamp;
     //       snapShot();
   //         vsnapShot();
            peers.sendMessage(src, "ack", v.getValue(myId));
        } else if (tag.equals("release")){
            q[src] = Symbols.Infinity;
            notify(); // okayCS() may be true now
        }else if(tag.equals("ack")){
        	notifyAll();
        }
    }	
    public void myWait() {
        try {
        	wait();
        } catch (InterruptedException e) {System.err.println(e);
        }
    }
    public void snapShot(){
    	System.out.print("my id is "+ myId+" --> ");
    	for(int i=0; i < N; i++){
    		System.out.print(""+q[i]+" ");
    	}
    	System.out.println();
    }
    public void vsnapShot(){
    	System.out.print("Vect my id is "+ myId+" --> ");
    	for(int i=0; i < N; i++){
    		System.out.print(""+v.clock[i]+" ");
    	}
    	System.out.println();
    }
}
