import com.oocourse.elevator2.PersonRequest;
import com.oocourse.elevator2.Request;
import com.oocourse.elevator2.ResetRequest;
import com.oocourse.elevator2.TimableOutput;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

public class Elevator extends Thread {
    private HashMap<Integer, Passenger> passengers; //电梯所载的乘客，这个还是可以有的
    private ArrayList<Request> waitingList;
    private final RequestList myRequest;
    private Strategy strategy;
    private int id;
    private int nowFloor; //现在所处的楼层
    private int direction; //电梯运行的方向,1为向上，-1为向下
    private Parameter parameter;
    private boolean isWait;
    private boolean isReset;
    private Dispatch dispatch;

    public Elevator(int id, RequestList request, ArrayList<Request> waitingList,
                    Dispatch dispatch) {
        this.parameter = new Parameter();
        this.waitingList = waitingList;
        this.nowFloor = parameter.getInitial();
        this.id = id;
        myRequest = request;
        passengers = new HashMap<>();
        direction = 1;
        //initDirection = 1;
        strategy = new Strategy(myRequest, passengers, waitingList);
        isReset = false;
        isWait = false;
        this.dispatch = dispatch;
    }

    @Override
    public void run() {
        while (true) {
            Suggestion suggestion = strategy.getSuggestion(nowFloor, direction,
                    parameter.getCapacity());
            TimableOutput.println(suggestion + " id is " + id);
            if (suggestion == Suggestion.END) {
                //TimableOutput.println("the elevator " + id + " is end!!!");
                break;
            } else if (suggestion == Suggestion.MOVE) {
                isWait = false;
                move();
            } else if (suggestion == Suggestion.OPEN) {
                isWait = false;
                openAndClose();
            } else if (suggestion == Suggestion.WAIT) {
                elevatorWait();
            } else if (suggestion == Suggestion.REVERSE) {
                isWait = false;
                direction = -direction;
            } else if (suggestion == Suggestion.RESET) {
                isWait = false;
                elevatorReset();
            }
        }
    }

    private void move() {
        try {
            sleep(parameter.getMove());
        } catch (Exception e) {
            e.printStackTrace();
        }
        Suggestion suggestion = strategy.getSuggestion(nowFloor, direction,
                parameter.getCapacity());
        if (suggestion != Suggestion.MOVE) {
            return;
        }
        nowFloor += direction;
        TimableOutput.println("ARRIVE-" + nowFloor + "-" + id);
    }

    private void openAndClose() {
        TimableOutput.println("OPEN-" + nowFloor + "-" + id);
        letout();
        try {
            sleep(parameter.getOpen() + parameter.getClose());
        } catch (Exception e) {
            e.printStackTrace();
        }
        letin();
        TimableOutput.println("CLOSE-" + nowFloor + "-" + id);
    }

    private void letout() {
        Iterator<Passenger> iterator = passengers.values().iterator();
        while (iterator.hasNext()) {
            Passenger pass = iterator.next();
            if (pass.getTo() == nowFloor) {
                iterator.remove();
                TimableOutput.println("OUT-" + pass.getId() + "-" + nowFloor + "-" +
                        id);
            }
        }
    }

    private void letin() {
        if (passengers.size() == parameter.getCapacity()) {
            return;
        }
        synchronized (myRequest) {
            Iterator<Passenger> iterator =
                    myRequest.getRequestPeople().get(nowFloor).iterator();
            while (iterator.hasNext()) {
                if (passengers.size() == parameter.getCapacity()) {
                    break; //注意此处边进人边判断！！！
                }
                Passenger passer = iterator.next();
                if (canIn(passer)) {
                    passengers.put(passer.getId(), passer);
                    iterator.remove();
                    TimableOutput.println("IN-" + passer.getId() + "-" + nowFloor + "-" +
                            id);
                    myRequest.removePersonRequest(passer);
                }
            }
        }
    }

    private boolean canIn(Passenger passenger) {
        return (passenger.getTo() > nowFloor && direction > 0) ||
                (passenger.getTo() < nowFloor && direction < 0);
    }

    public int getPeopleNumber() {
        return passengers.size();
    }

    public int getCapacity() {
        return parameter.getCapacity();
    }

    public RequestList getMyRequestList() {
        synchronized (myRequest) {
            return this.myRequest;
        }
    }

    public int getDirection() {
        return direction;
    }

    public int getNowFloor() {
        return nowFloor;
    }

    public int getRequestNumber() {
        return myRequest.getSize();
    }

    private void elevatorReset() {
        // 1.修改参数
        // 2. 让人出去，重新new出request对象，放进waitinglist，注意加锁
        isReset = true;
        allOut(); //先出人再reset begin
        TimableOutput.println("RESET_BEGIN-" + id);
        for (Passenger person : passengers.values()) {
            if (nowFloor == person.getTo()) {
                continue;
            }
            PersonRequest newRequest = new PersonRequest(nowFloor, person.getTo(),
                    person.getId());
            synchronized (waitingList) {
                waitingList.add(newRequest);
                waitingList.notifyAll();
            }
        }
        passengers.clear();
        HashMap<Integer, ArrayList<Passenger>> tempPeopleRequest = myRequest.getRequestPeople();
        for (Integer item : tempPeopleRequest.keySet()) {
            ArrayList<Passenger> tempPassengers = tempPeopleRequest.get(item);
            if (!tempPassengers.isEmpty()) {
                for (Passenger passenger : tempPassengers) {
                    PersonRequest newRequest = new PersonRequest(passenger.getFrom(),
                            passenger.getTo(), passenger.getId());
                    synchronized (waitingList) {
                        waitingList.add(newRequest);
                        waitingList.notifyAll();
                    }
                }
            }
        }
        myRequest.clearRequest();
        //修改参数
        try {
            sleep(1200);
        } catch (Exception e) {
            e.printStackTrace();
        }
        ResetRequest resetRequest = myRequest.getRequestReset().get(0);
        parameter.setMove((int) (resetRequest.getSpeed() * 1000));
        parameter.setCapacity(resetRequest.getCapacity());
        TimableOutput.println("RESET_END-" + id);
        isReset = false;
        myRequest.removeResetRequest(myRequest.getRequestReset().get(0));
        dispatch.addFinish();
    }

    private void elevatorWait() {
        isWait = true;
        myRequest.waitRequest();
    }

    private void allOut() {
        if (passengers.isEmpty()) {
            return;
        }
        TimableOutput.println("OPEN-" + nowFloor + "-" + id); //注意此处缺失，就是出去之前应该先开门！！！
        try {
            sleep(parameter.getOpen());
        } catch (Exception e) {
            e.printStackTrace();
        }

        for (Passenger person : passengers.values()) {
            TimableOutput.println("OUT-" + person.getId() + "-" + nowFloor + "-" +
                    id);
        }
        try {
            sleep(parameter.getClose());
        } catch (Exception e) {
            e.printStackTrace();
        }

        TimableOutput.println("CLOSE-" + nowFloor + "-" + id);
    }

    public int getMove() {
        return parameter.getMove();
    }

    public synchronized boolean isWait() {
        return isWait;
    }

    public synchronized boolean isReset() {
        return isReset;
    }

    public int getElevatorId() {
        return this.id;
    }

    public synchronized void setReset() {
        isReset = true;
    }
}
