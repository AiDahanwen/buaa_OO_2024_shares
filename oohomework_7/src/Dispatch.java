import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import com.oocourse.elevator3.Request;
import com.oocourse.elevator3.PersonRequest;
import com.oocourse.elevator3.DoubleCarResetRequest;
import com.oocourse.elevator3.NormalResetRequest;
import com.oocourse.elevator3.ResetRequest;

public class Dispatch extends Thread {
    private ArrayList<Request> waitingList;
    private HashMap<Integer, Elevator> elevators;
    private boolean end;
    private HashSet<Integer> allPassengers;
    private int resetNum;
    private int finishNum;

    public Dispatch(ArrayList<Request> waitingList, HashMap<Integer, Elevator> elevators) {
        this.waitingList = waitingList;
        this.elevators = elevators;
        end = false;
        allPassengers = new HashSet<>();
        resetNum = 0;
        finishNum = 0;
    }

    @Override
    public void run() {
        while (true) {
            Request request;
            boolean empty;
            synchronized (waitingList) {
                empty = waitingList.isEmpty();
                waitingList.notifyAll();
            }
            if (end && empty) {
                synchronized (waitingList) {
                    waitWaitingList();
                }
                if (allSolved() && (resetNum == finishNum)) {
                    for (int i = 1; i <= 6; i++) {
                        Elevator temp = elevators.get(i);
                        if (temp instanceof DoubleElevator) {
                            ((DoubleElevator) temp).setEnd();
                        } else {
                            temp.getMyRequest().setEnd();
                        }
                    }
                    break;
                }
            }
            synchronized (waitingList) {
                while (!end && waitingList.isEmpty()) {
                    waitWaitingList();
                }
                if (end && waitingList.isEmpty()) {
                    waitWaitingList();
                    if (allSolved() && (resetNum == finishNum)) {
                        for (int i = 1; i <= 6; i++) {
                            Elevator temp = elevators.get(i);
                            if (temp instanceof DoubleElevator) {
                                ((DoubleElevator) temp).setEnd();
                            } else {
                                temp.getMyRequest().setEnd();
                            }
                        }
                        break;
                    }
                    continue;
                }
                request = waitingList.get(0);
                waitingList.remove(request);
                waitingList.notifyAll();
            }
            dispatchRequest(request);
        }
    } //54 lines

    public void setEnd() {
        end = true;
    }

    private void dispatchPeopleRequest(PersonRequest personRequest) {
        allPassengers.add(personRequest.getPersonId());
        ArrayList<Elevator> waiting = findWait(personRequest); // empty
        ArrayList<Elevator> same = findSame(personRequest); //empty
        Elevator choose = findNearest(waiting, same, personRequest);
        if (choose == null) {
            choose = findFewest(personRequest);
        }
        choose.getMyRequest().addPersonRequest(personRequest);
    } /* parameter analysis mode and when one elevator is reset,
       not pass, but add the request in its buffer.
       in this case the nearest elevator will not be null? */

    private void dispatchResetRequest(ResetRequest resetRequest) {
        Elevator target;
        resetNum++;
        if (resetRequest instanceof NormalResetRequest) {
            target = elevators.get(((NormalResetRequest) resetRequest).getElevatorId());
            target.getMyRequest().addNormalResetRequest((NormalResetRequest) resetRequest);
        } else if (resetRequest instanceof DoubleCarResetRequest) {
            target = elevators.get(((DoubleCarResetRequest) resetRequest).getElevatorId());
            target.getMyRequest().addDoubleCarResetRequest((DoubleCarResetRequest) resetRequest);
        }
    }

    private ArrayList<Elevator> findWait(PersonRequest personRequest) {
        ArrayList<Elevator> waiting = new ArrayList<>();
        for (int i = 1; i <= 6; i++) {
            Elevator temp = elevators.get(i);
            if (temp instanceof DoubleElevator) {
                Elevator tempA = ((DoubleElevator) temp).getElevatorA();
                Elevator tempB = ((DoubleElevator) temp).getElevatorB();
                if (tempA.isWait() && tempA.isInBound(personRequest) &&
                        canArrive(tempA, personRequest, "A")) {
                    waiting.add(tempA);
                }
                if (tempB.isWait() && tempB.isInBound(personRequest) &&
                        canArrive(tempB, personRequest, "B")) {
                    waiting.add(tempB);
                }
            } else {
                if (temp.isWait()) {
                    waiting.add(temp);
                }
            }
        }
        return waiting;
    } // reset and wait can not happen at the same time

    private ArrayList<Elevator> findSame(PersonRequest person) {
        ArrayList<Elevator> same = new ArrayList<>();
        for (int i = 1; i <= 6; i++) {
            Elevator temp = elevators.get(i);
            if (temp instanceof DoubleElevator) {
                Elevator tempA = ((DoubleElevator) temp).getElevatorA();
                Elevator tempB = ((DoubleElevator) temp).getElevatorB();
                if (isSameDirectiorn(tempA, person) && tempA.isInBound(person) &&
                        (tempA.getPeopleNumber() + tempA.getRequestNumber())
                                < tempA.getCapacity() && canArrive(tempA, person, "A")) {
                    same.add(tempA);
                }
                if (isSameDirectiorn(tempB, person) && tempB.isInBound(person) &&
                        (tempB.getPeopleNumber() + tempB.getRequestNumber())
                                < tempB.getCapacity() && canArrive(tempB, person, "B")) {
                    same.add(tempB);
                }
            } else {
                if (isSameDirectiorn(temp, person) &&
                        (temp.getPeopleNumber() + temp.getRequestNumber()) < temp.getCapacity()) {
                    same.add(temp);
                }
            }
        }
        return same;
    } // not pass the reset

    private Elevator findNearest(ArrayList<Elevator> waiting, ArrayList<Elevator> same,
                                 PersonRequest person) {
        Elevator nearest = null;
        int min = 100;
        ArrayList<Elevator> sameDistance = new ArrayList<>();
        for (Elevator item : waiting) {
            int distance = Math.abs(item.getNowFloor() - person.getFromFloor());
            if (distance < min) {
                min = distance;
                sameDistance.clear();
                sameDistance.add(item);
            } else if (distance == min) {
                sameDistance.add(item);
            }
        }
        for (Elevator item : same) {
            int distance = Math.abs(item.getNowFloor() - person.getFromFloor());
            if (distance < min) {
                min = distance;
                sameDistance.clear();
                sameDistance.add(item);
            } else if (distance == min) {
                sameDistance.add(item);
            }
        }
        nearest = findQuick(sameDistance);
        return nearest;
    }

    private boolean isSameDirectiorn(Elevator elevator, PersonRequest person) {
        int personDirection;
        if ((person.getToFloor() - person.getFromFloor()) < 0) {
            personDirection = -1;
        } else {
            personDirection = 1;
        }
        int nowFloor = elevator.getNowFloor();
        int fromFloor = person.getFromFloor();
        int eleDirection = elevator.getDirection();
        boolean temp1 = fromFloor > nowFloor && eleDirection > 0;
        boolean temp2 = fromFloor < nowFloor && eleDirection < 0;
        return personDirection == eleDirection && (temp1 || temp2);
    }

    private Elevator findFewest(PersonRequest personRequest) { //请求最少
        Elevator fewest = null;
        int min = 2147483647;
        for (Elevator item : elevators.values()) {
            if (item instanceof DoubleElevator) {
                Elevator a = ((DoubleElevator) item).getElevatorA();
                Elevator b = ((DoubleElevator) item).getElevatorB();
                if (a.getRequestNumber() < min && canArrive(a, personRequest, "A") &&
                        a.isInBound(personRequest)) {
                    fewest = a;
                    min = a.getRequestNumber();
                }
                if (b.getRequestNumber() < min && canArrive(b, personRequest, "B") &&
                        b.isInBound(personRequest)) {
                    fewest = b;
                    min = b.getRequestNumber();
                }
                continue;
            }
            if (item.getRequestNumber() < min) {
                fewest = item;
                min = item.getRequestNumber();
            }
        }
        return fewest;
    }

    private Elevator findQuick(ArrayList<Elevator> sameDistance) { //move最快
        int minSpeed = 100000;
        Elevator quick = null;
        for (Elevator ele : sameDistance) {
            if (ele.getMove() < minSpeed) {
                quick = ele;
                minSpeed = ele.getMove();
            }
            if ((ele.getMove() == minSpeed) && (ele.isDouble())) {
                quick = ele;
            }
        }
        return quick;
    }

    public void replaceElevator(DoubleElevator doubleElevator) {
        elevators.put(doubleElevator.getEleId(), doubleElevator);
    }

    private boolean allSolved() {
        int sum = 0;
        for (int i = 1; i <= 6; i++) {
            sum += elevators.get(i).getSolvedSize();
        }
        //TimableOutput.println(allPassengers);
        //TimableOutput.println(allPassengers.size());
        //TimableOutput.println(sum);
        if (sum == allPassengers.size()) {
            synchronized (waitingList) {
                waitingList.notifyAll();
            }
            return true;
        }
        return false;
    }

    private boolean canArrive(Elevator temp, PersonRequest personRequest, String type) {
        int transfer = temp.getChangeFloor();
        if (personRequest.getFromFloor() != transfer) {
            return true;
        }
        if (type.equals("A") && personRequest.getToFloor() > transfer) {
            return false;
        } else if (type.equals("B") && personRequest.getToFloor() < transfer) {
            return false;
        }
        return true;
    }

    private void dispatchRequest(Request request) {
        if (request instanceof PersonRequest) {
            dispatchPeopleRequest((PersonRequest) request);
        } else if (request instanceof ResetRequest) {
            dispatchResetRequest((ResetRequest) request);
        }
    }

    private void waitWaitingList() {
        try {
            waitingList.wait();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void addFinishReset() {
        finishNum++;
        synchronized (waitingList) {
            waitingList.notifyAll();
        }
    }
}
