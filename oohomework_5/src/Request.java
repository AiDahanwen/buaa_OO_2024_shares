import java.util.HashMap;

public class Request {
    private HashMap<Integer, HashMap<Integer, Passenger>> requestTable; //从integer层出发的所有乘客。
    private boolean isEnd;
    private int highest;
    private static boolean LOG = false;

    public Request(int high) {
        requestTable = new HashMap<>();
        for (int i = 1; i <= high; i++) {
            requestTable.put(i, new HashMap<>());
        }
        isEnd = false;
        highest = high;
    }

    public synchronized void waitRequest() {
        try {
            wait();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public synchronized void addRequest(Passenger passenger) {
        requestTable.get(passenger.getFrom()).put(passenger.getId(), passenger);
        notifyAll();
    }

    public synchronized void removeRequest(Passenger passenger) {
        requestTable.get(passenger.getFrom()).remove(passenger);
    }

    public synchronized boolean isEmpty() {
        for (int i = 1; i <= highest; i++) {
            if (!requestTable.get(i).isEmpty()) {
                return false;
            }
        }
        return true;
    }

    public synchronized void setEnd() {
        isEnd = true;
        notifyAll();
    }

    public synchronized boolean isEnd() {
        return isEnd;
    }

    public synchronized HashMap<Integer, HashMap<Integer, Passenger>> getRequestTable() {
        return this.requestTable;
    }
}
