import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import com.oocourse.elevator2.PersonRequest;
import com.oocourse.elevator2.ResetRequest;
import com.oocourse.elevator2.Request;

public class RequestList {
    private HashMap<Integer, ArrayList<Passenger>> requestPeople; //分开存，一个是顾客的，一个是reset的请求？
    private ArrayList<ResetRequest> requestReset; //存储重置请求
    private ArrayList<Request> requestTable; //存放该电梯所有的请求/
    private boolean isEnd;
    private int id;
    private static boolean LOG = false;

    public RequestList(int high, int id) {
        requestTable = new ArrayList<>();
        requestPeople = new HashMap<>();
        for (int i = 1; i <= high; i++) {
            requestPeople.put(i, new ArrayList<>());
        }
        requestReset = new ArrayList<>();
        isEnd = false;
        this.id = id;
    }

    public synchronized void waitRequest() {
        try {
            wait();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public synchronized void addRequest(Request request) {
        requestTable.add(request);
        if (request instanceof PersonRequest) {
            PersonRequest pr = (PersonRequest) request;
            Passenger passenger = new Passenger(pr.getPersonId(), pr.getFromFloor(),
                    pr.getToFloor(), pr);
            requestPeople.get(((PersonRequest) request).getFromFloor()).add(passenger);

        } else if (request instanceof ResetRequest) {
            requestReset.add((ResetRequest) request);
        }
        notifyAll();
        //TimableOutput.println("has notified!");
    }

    public synchronized void removeRequest(Request request) {
        requestTable.remove(request);
    }

    public synchronized boolean isEmpty() {
        if (!requestTable.isEmpty()) {
            //TimableOutput.println(requestTable.get(0).toString());
        } //42-FROM-7-to-1
        return requestTable.isEmpty();
    }

    public synchronized void setEnd() {
        isEnd = true;  //此处end说明request不会再变化了，但是此时request不一定为空
        notifyAll();
    }

    public synchronized boolean isEnd() {
        return isEnd;
    }

    public synchronized ArrayList<Request> getRequestTable() {
        return this.requestTable;
    }

    public synchronized int getSize() {
        return requestTable.size();
    }

    public synchronized ArrayList<ResetRequest> getRequestReset() {
        return requestReset;
    }

    public synchronized HashMap<Integer, ArrayList<Passenger>> getRequestPeople() {
        return requestPeople;
    }

    public synchronized void removePersonRequest(Passenger person) {
        requestPeople.remove(person);
        removeRequest(person.getMyself());
    }

    public synchronized void removeResetRequest(ResetRequest reset) {
        requestReset.remove(reset);
        removeRequest(reset);
    }

    public synchronized void clearRequest() {
        for (int i = 1; i <= 11; i++) {
            requestPeople.get(i).clear();
        }
        Iterator<Request> iterator = requestTable.iterator();
        while (iterator.hasNext()) {
            Request request = iterator.next();
            if (request instanceof PersonRequest) {
                iterator.remove();
            }
        }
    }
}
