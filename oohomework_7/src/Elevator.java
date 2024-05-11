import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.concurrent.locks.Lock;

import com.oocourse.elevator3.Request;
import com.oocourse.elevator3.PersonRequest;
import com.oocourse.elevator3.DoubleCarResetRequest;
import com.oocourse.elevator3.NormalResetRequest;
import com.oocourse.elevator3.TimableOutput;

public class Elevator extends Thread {
    private final int id;
    private HashMap<Integer, Passenger> passengers;
    private RequestList myRequest;
    private Parameter parameter;
    private int nowFloor;
    private int direction;
    private boolean isWait;
    private int changeFloor;
    private String type;
    private boolean isDouble;
    private Dispatch dispatch;
    private Strategy strategy;
    private ArrayList<Request> waitingList;
    private Lock lock; // when it isn't double elevator, the lock is null
    private HashSet<Integer> solvedPassengers;

    public Elevator(int id, Dispatch dispatch, ArrayList<Request> waitingList, Lock lock) {
        this.id = id;
        this.parameter = new Parameter();
        nowFloor = 1;
        direction = 1;
        isWait = false;
        passengers = new HashMap<>();
        myRequest = new RequestList(id);
        isDouble = false;
        changeFloor = -1;
        type = "";
        this.dispatch = dispatch;
        strategy = new Strategy(myRequest, passengers);
        this.waitingList = waitingList;
        this.lock = lock;
        solvedPassengers = new HashSet<>();
    } // consider whether the double class should be reserved?

    @Override
    public void run() {
        while (true) {
            Suggestion suggestion = strategy.getSuggestion(nowFloor, direction,
                    parameter.getCapacity(), changeFloor, type);
            if (suggestion != Suggestion.WAIT) {
                isWait = false;
            }
            if (suggestion == Suggestion.END) {
                break;
            } else if (suggestion == Suggestion.MOVE) {
                move();
            } else if (suggestion == Suggestion.OPEN) {
                openAndClose();
            } else if (suggestion == Suggestion.WAIT) {
                elevatorWait();
            } else if (suggestion == Suggestion.REVERSE) {
                direction = -direction;
            } else if (suggestion == Suggestion.NORMALRESET) {
                elevatorNormalReset();
                notifyWaitingList();
            } else if (suggestion == Suggestion.DOUBLERESET) {
                elevatorDoubleReset();
                notifyWaitingList();
                break;
            }
        }
    }

    private void move() {
        sleepMove();
        Suggestion suggestion = strategy.getSuggestion(nowFloor, direction, parameter.getCapacity(), changeFloor, type);
        if (suggestion != Suggestion.MOVE) {
            return;
        } // this block is used to prevent when reset there is more move
        if (shouldLock()) { // double elevator
            lock.lock();
            try {
                nowFloor += direction; // arrive at the changeFloor
                printArrive();
                boolean temp1 = firstOutAtChangeFloor();
                synchronized (myRequest) {
                    boolean temp2 = letinAtChangeFloor(temp1);
                    secondOutAtChangeFloor(temp2 || temp1);
                }
                sleepMove();
                if (type.equals("A")) {
                    nowFloor = changeFloor - 1;
                    printArrive();
                } else if (type.equals("B")) {
                    nowFloor = changeFloor + 1;
                    printArrive();
                }
                direction = -direction;
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                lock.unlock();
            }
        } else {
            nowFloor += direction;
            printArrive();
        }
    }

    private void openAndClose() {
        open();
        letout();
        letin();
        close();
    }

    private void letout() {
        Iterator<Passenger> iterator = passengers.values().iterator();
        while (iterator.hasNext()) {
            Passenger pass = iterator.next();
            if (pass.getToFloor() == nowFloor) {
                solvedPassengers.add(pass.getId());
                notifyWaitingList();
                iterator.remove();
                printOut(pass.getId());
            }
        }
    }

    private void letin() {
        if (passengers.size() == parameter.getCapacity()) {
            return;
        }
        synchronized (myRequest) {
            Iterator<Passenger> iterator = myRequest.getRequestPeople().get(nowFloor).iterator();
            while (iterator.hasNext()) {
                if (passengers.size() == parameter.getCapacity()) {
                    break; //注意此处边进人边判断！！！
                }
                Passenger passer = iterator.next();
                if (canIn(passer)) {
                    passengers.put(passer.getId(), passer);
                    iterator.remove();
                    printIn(passer.getId());
                    myRequest.removePersonRequest(passer);
                }
            }
            myRequest.notifyAll();
        }
    }

    private boolean canIn(Passenger passenger) {
        return (passenger.getToFloor() > nowFloor && direction > 0) || (passenger.getToFloor() < nowFloor && direction < 0);
    }

    private void elevatorWait() {
        isWait = true;
        myRequest.waitRequest();
    }

    private void elevatorDoubleReset() {
        myRequest.setEleIsReset(true);
        allOut(); //先出人再reset begin
        printResetBegin();
        twoAddWaiting();
        sleepReset();
        DoubleCarResetRequest doubleReset = myRequest.getRequestDoubleReset().get(0);
        DoubleElevator doubleElevator = new DoubleElevator(id, doubleReset.getTransferFloor(),
                dispatch, waitingList, this.getSolvedSize());
        dispatch.replaceElevator(doubleElevator);
        doubleElevator.setParameter((int) (doubleReset.getSpeed() * 1000), doubleReset.getCapacity());
        doubleElevator.getElevatorA().setDouble(doubleReset.getTransferFloor(), "A");
        doubleElevator.getElevatorB().setDouble(doubleReset.getTransferFloor(), "B");
        printResetEnd();
        myRequest.removeDoubleReset(doubleReset);
        myRequest.bufferInTwo(doubleElevator);
        dispatch.addFinishReset();
    }

    private void elevatorNormalReset() {
        myRequest.setEleIsReset(true);
        allOut(); //先出人再reset begin
        printResetBegin();
        twoAddWaiting();
        sleepReset();
        NormalResetRequest nrRequest = myRequest.getRequestNormalReset().get(0);
        parameter.setMove((int) (nrRequest.getSpeed() * 1000));
        parameter.setCapacity(nrRequest.getCapacity());
        printResetEnd();
        myRequest.setEleIsReset(false);
        myRequest.removeNormalReset(nrRequest);
        myRequest.bufferIn(); // normal reset
        dispatch.addFinishReset();
    }

    private void allOut() {
        if (passengers.isEmpty()) {
            return;
        }
        open();
        for (Passenger person : passengers.values()) {
            printOut(person.getId());
        }
        close();
    }

    public int getNowFloor() {
        return this.nowFloor;
    }

    public int getDirection() {
        return this.direction;
    }

    public synchronized boolean isWait() {
        return isWait;
    }

    public int getPeopleNumber() {
        return passengers.size();
    }

    public int getCapacity() {
        return parameter.getCapacity();
    }

    public int getMove() {
        return parameter.getMove();
    }

    public int getRequestNumber() {
        return this.myRequest.getRequestNumber();
    }

    public RequestList getMyRequest() {
        return myRequest;
    }

    private boolean shouldLock() {
        boolean temp1 = (nowFloor == changeFloor - 1) && isDouble && type.equals("A") && direction > 0;
        boolean temp2 = (nowFloor == changeFloor + 1) && isDouble && type.equals("B") && direction < 0;
        return temp1 || temp2;
    }

    public boolean isInBound(PersonRequest personRequest) {
        int minFloor = 1;
        int maxFloor = 11;
        if (isDouble && type.equals("A")) {
            maxFloor = changeFloor;
        } else if (isDouble && type.equals("B")) {
            minFloor = changeFloor;
        }
        return (personRequest.getFromFloor() >= minFloor) && (personRequest.getFromFloor() <= maxFloor);
    }

    private void passengersAddWaiting() {
        for (Passenger person : passengers.values()) {
            if (nowFloor == person.getToFloor()) {
                solvedPassengers.add(person.getId());
                notifyWaitingList();
                continue;
            }
            PersonRequest newRequest = new PersonRequest(nowFloor, person.getToFloor(), person.getId());
            addAndNotifyWaitingList(newRequest);
        }
        passengers.clear();
    }

    private void requestsAddWaiting() {
        HashMap<Integer, ArrayList<Passenger>> tempPeopleRequest = myRequest.getRequestPeople();
        for (Integer item : tempPeopleRequest.keySet()) {
            ArrayList<Passenger> tempPassengers = tempPeopleRequest.get(item);
            if (!tempPassengers.isEmpty()) {
                for (Passenger passenger : tempPassengers) {
                    PersonRequest newRequest = new PersonRequest(passenger.getFromFloor(),
                            passenger.getToFloor(), passenger.getId());
                    addAndNotifyWaitingList(newRequest);
                }
            }
        }
        myRequest.clearRequest();
    }

    public int getEleId() {
        return this.id;
    }

    public void setDouble(int changeFloor, String type) {
        this.isDouble = true;
        this.changeFloor = changeFloor;
        this.type = type;
        this.myRequest.setType(type);
    }

    private void printArrive() {
        if (isDouble) {
            TimableOutput.println("ARRIVE-" + nowFloor + "-" + id + "-" + type);
        } else {
            TimableOutput.println("ARRIVE-" + nowFloor + "-" + id);
        }
    }

    private void printOpen() {
        if (isDouble) {
            TimableOutput.println("OPEN-" + nowFloor + "-" + id + "-" + type);
        } else {
            TimableOutput.println("OPEN-" + nowFloor + "-" + id);
        }
    }

    private void printClose() {
        if (isDouble) {
            TimableOutput.println("CLOSE-" + nowFloor + "-" + id + "-" + type);
        } else {
            TimableOutput.println("CLOSE-" + nowFloor + "-" + id);
        }
    }

    private void printResetBegin() {
        if (isDouble) {
            TimableOutput.println("RESET_BEGIN-" + id + "-" + type);
        } else {
            TimableOutput.println("RESET_BEGIN-" + id);
        }
    }

    private void printResetEnd() {
        if (isDouble) {
            TimableOutput.println("RESET_END-" + id + "-" + type);
        } else {
            TimableOutput.println("RESET_END-" + id);
        }
    }

    private void printIn(int passengerId) {
        if (isDouble) {
            TimableOutput.println("IN-" + passengerId + "-" + nowFloor + "-" + id + "-" + type);
        } else {
            TimableOutput.println("IN-" + passengerId + "-" + nowFloor + "-" + id);
        }
    }

    private void printOut(int passengerId) {
        if (isDouble) {
            TimableOutput.println("OUT-" + passengerId + "-" + nowFloor + "-" + id + "-" + type);
        } else {
            TimableOutput.println("OUT-" + passengerId + "-" + nowFloor + "-" + id);
        }
    }

    public void setNowFloor(int newNowFloor) {
        nowFloor = newNowFloor;
    }

    public boolean isDouble() {
        return isDouble;
    }

    public Parameter getParameter() {
        return parameter;
    }

    private boolean letinAtChangeFloor(boolean temp) {
        if (passengers.size() == parameter.getCapacity()) {
            return false;
        }
        if (!temp) {
            printOpen();
            sleepOpen();
        }
        synchronized (myRequest) {
            Iterator<Passenger> iterator =
                    myRequest.getRequestPeople().get(nowFloor).iterator();
            while (iterator.hasNext()) {
                if (passengers.size() == parameter.getCapacity()) {
                    break; //注意此处边进人边判断！！！
                }
                Passenger passer = iterator.next();
                passengers.put(passer.getId(), passer);
                iterator.remove();
                printIn(passer.getId());
                myRequest.removePersonRequest(passer);
            }
            myRequest.notifyAll();
        }
        return true;
    }

    private boolean firstOutAtChangeFloor() {
        if (passengers.isEmpty()) {
            return false;
        }
        printOpen();
        sleepOpen();
        Iterator<Passenger> iterator = passengers.values().iterator();
        while (iterator.hasNext()) {
            Passenger passenger = iterator.next();
            printOut(passenger.getId());
            iterator.remove();
            if (passenger.getToFloor() == nowFloor) {
                solvedPassengers.add(passenger.getId());
                notifyWaitingList();
                continue;
            }
            PersonRequest personRequest = new PersonRequest(nowFloor,
                    passenger.getToFloor(), passenger.getId());
            addAndNotifyWaitingList(personRequest);
        }
        return true;
    }

    private void secondOutAtChangeFloor(boolean temp) {
        Iterator<Passenger> iterator = passengers.values().iterator();
        while (iterator.hasNext()) {
            Passenger passenger = iterator.next();
            if (((type.equals("A")) && (passenger.getToFloor() > changeFloor)) ||
                    ((type.equals("B")) && (passenger.getToFloor() < changeFloor))) {
                printOut(passenger.getId());
                PersonRequest personRequest = new PersonRequest(nowFloor, passenger.getToFloor(), passenger.getId());
                iterator.remove();
                addAndNotifyWaitingList(personRequest);
            }
        }
        if (temp) {
            sleepClose();
            printClose();
        }
    }

    public synchronized int getSolvedSize() {
        return solvedPassengers.size();
    }

    private void sleepOpen() {
        try {
            sleep(parameter.getOpen());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void sleepClose() {
        try {
            sleep(parameter.getClose());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void sleepMove() {
        try {
            sleep(parameter.getMove());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void sleepReset() {
        try {
            sleep(1200);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public int getChangeFloor() {
        return changeFloor;
    }

    private void twoAddWaiting() {
        passengersAddWaiting();
        requestsAddWaiting();
    }

    private void open() {
        printOpen();
        sleepOpen();
    }

    private void close() {
        sleepClose();
        printClose();
    }

    private void notifyWaitingList() {
        synchronized (waitingList) {
            waitingList.notifyAll();
        }
    }

    private void addAndNotifyWaitingList(PersonRequest personRequest) {
        synchronized (waitingList) {
            waitingList.add(personRequest);
            waitingList.notifyAll();
        }
    }
}