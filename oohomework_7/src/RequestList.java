import com.oocourse.elevator3.Request;
import com.oocourse.elevator3.PersonRequest;
import com.oocourse.elevator3.DoubleCarResetRequest;
import com.oocourse.elevator3.NormalResetRequest;
import com.oocourse.elevator3.TimableOutput;

import java.util.ArrayList;
import java.util.HashMap;

public class RequestList {
    private ArrayList<Request> requestTable;
    private HashMap<Integer, ArrayList<Passenger>> requestPeople;
    private ArrayList<NormalResetRequest> requestNormalReset;
    private ArrayList<DoubleCarResetRequest> requestDoubleReset;
    private boolean end;
    private ArrayList<Passenger> buffer;
    private boolean eleIsReset;
    private String type;
    private int eleId;

    public RequestList(int eleId) {
        requestTable = new ArrayList<>();
        requestPeople = new HashMap<>();
        for (int i = 1; i <= 11; i++) {
            ArrayList<Passenger> temp = new ArrayList<>();
            requestPeople.put(i, temp);
        }
        requestNormalReset = new ArrayList<>();
        requestDoubleReset = new ArrayList<>();
        end = false;
        buffer = new ArrayList<>();
        eleIsReset = false; // this variable should be maintained by the elevator
        type = ""; // this type should be modified by the elevator
        this.eleId = eleId;
    }

    public synchronized void waitRequest() {
        try {
            wait();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public synchronized void setEnd() {
        //TimableOutput.println("the request is end" + " id is " + eleId);
        end = true;
        notifyAll();
    }

    public synchronized void addPersonRequest(PersonRequest person) {
        Passenger passenger = new Passenger(person.getPersonId(),
                person.getToFloor(), person.getFromFloor(), person);
        requestTable.add(person);
        if (eleIsReset) {
            buffer.add(passenger);
            return;
        } // the content in buffer is added in elevator.reset, not here
        if (!type.isEmpty()) {
            TimableOutput.println("RECEIVE-" + passenger.getId() + "-"
                    + eleId + "-" + type);
        } else {
            TimableOutput.println("RECEIVE-" + passenger.getId() + "-" + eleId);
        }
        requestPeople.get(passenger.getFromFloor()).add(passenger);
        notifyAll();
    } // attention: the double has been broken down into two pieces

    public synchronized void addNormalResetRequest(NormalResetRequest nrRequest) {
        requestNormalReset.add(nrRequest);
        requestTable.add(nrRequest);
        notifyAll();
    }

    public synchronized void addDoubleCarResetRequest(DoubleCarResetRequest dcrRequest) {
        requestDoubleReset.add(dcrRequest);
        requestTable.add(dcrRequest);
        notifyAll();
    }

    public synchronized int getRequestNumber() {
        notifyAll();
        return requestTable.size() + buffer.size();
    }

    public synchronized void setType(String type) {
        this.type = type;
        notifyAll();
    }

    public synchronized void setEleIsReset(boolean temp) {
        eleIsReset = temp;
        notifyAll();
    }

    public synchronized boolean isEnd() {
        notifyAll();
        return end;
    }

    public synchronized boolean haveNormalReset() {
        notifyAll();
        return !requestNormalReset.isEmpty();
    }

    public synchronized boolean haveDoubleReset() {
        notifyAll();
        return !requestDoubleReset.isEmpty();
    }

    public synchronized HashMap<Integer, ArrayList<Passenger>> getRequestPeople() {
        notifyAll();
        return requestPeople;
    }

    public synchronized ArrayList<DoubleCarResetRequest> getRequestDoubleReset() {
        notifyAll();
        return requestDoubleReset;
    }

    public synchronized ArrayList<NormalResetRequest> getRequestNormalReset() {
        notifyAll();
        return requestNormalReset;
    }

    public synchronized boolean isEmpty() {
        notifyAll();
        return requestTable.isEmpty();
    }

    public synchronized void removePersonRequest(Passenger passenger) {
        requestTable.remove(passenger.getMyself());
        requestPeople.get(passenger.getFromFloor()).remove(passenger);
        notifyAll();
    }

    public synchronized void clearRequest() {
        requestTable.clear();
        for (int i = 1; i <= 11; i++) {
            requestPeople.get(i).clear();
        }
        notifyAll();
    }

    public synchronized void removeNormalReset(NormalResetRequest nrRequest) {
        requestTable.remove(nrRequest);
        requestNormalReset.remove(nrRequest);
        notifyAll();
    }

    public synchronized void removeDoubleReset(DoubleCarResetRequest dcrRequest) {
        requestTable.remove(dcrRequest);
        requestDoubleReset.remove(dcrRequest);
        notifyAll();
    }

    public synchronized void bufferIn() {
        //TimableOutput.println(buffer + " " + eleId);
        for (Passenger passenger : buffer) {
            TimableOutput.println("RECEIVE-" + passenger.getId() + "-" + eleId);
            requestPeople.get(passenger.getFromFloor()).add(passenger);
        }
        buffer.clear();
        notifyAll();
    }

    public synchronized void bufferInTwo(DoubleElevator doubleElevator) {
        //TimableOutput.println(buffer + " " + eleId);
        if (buffer.isEmpty()) {
            return;
        }
        int minFloorA = 1;
        int maxFloorA = doubleElevator.getChangeFloor();
        int minFloorB = doubleElevator.getChangeFloor();
        int maxFloorB = 11;
        for (Passenger passenger : buffer) {
            PersonRequest newRequest = new PersonRequest(passenger.getFromFloor(),
                    passenger.getToFloor(), passenger.getId());
            if (passenger.getFromFloor() >= minFloorA && passenger.getFromFloor() <= maxFloorA) {
                doubleElevator.getElevatorA().getMyRequest().addPersonRequest(newRequest);
            } else if (passenger.getFromFloor() >= minFloorB &&
                    passenger.getToFloor() <= maxFloorB) {
                doubleElevator.getElevatorB().getMyRequest().addPersonRequest(newRequest);
            }
        }
        notifyAll();
    }
}
