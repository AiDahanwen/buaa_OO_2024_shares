import com.oocourse.elevator1.TimableOutput;

import java.util.HashMap;
import java.util.Iterator;

public class Elevator extends Thread {
    private HashMap<Integer, Passenger> passengers; //电梯所载的乘客
    private final Request myRequest;
    private Strategy strategy;
    private int id;
    private int nowFloor; //现在所处的楼层
    private int direction; //电梯运行的方向,1为向上，-1为向下
    private int initial;
    private int capacity;
    private int open;
    private int close;
    private int move;
    private int highestfloor;
    private static final boolean LOG = true;

    public Elevator(int id, int init, int capacity, int open, int clo, int move,
                    int high, Request request) {
        this.initial = init;
        this.capacity = capacity;
        this.open = open;
        this.close = clo;
        this.move = move;
        this.nowFloor = init;
        this.highestfloor = high;
        this.id = id;
        myRequest = request;
        passengers = new HashMap<>();
        direction = 1;
        strategy = new Strategy(myRequest, passengers);
    }

    @Override
    public void run() {
        while (true) {
            Suggestion suggestion = strategy.getSuggestion(nowFloor, direction);
            if (suggestion == Suggestion.END) {
                break;
            } else if (suggestion == Suggestion.MOVE) {
                try {
                    move();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                } //此处的try catch是必须的吗
            } else if (suggestion == Suggestion.OPEN) {
                try {
                    openAndClose();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                } //此处的try catch必须？
            } else if (suggestion == Suggestion.WAIT) {
                myRequest.waitRequest(); // who wait?,elevator or request?
            } else { //REVERSE
                direction = -direction;
            }
        }
    }

    private void move() throws InterruptedException {
        nowFloor += direction;
        sleep(move);
        TimableOutput.println("ARRIVE-" + nowFloor + "-" + id);
    }

    private void openAndClose() throws InterruptedException {
        TimableOutput.println("OPEN-" + nowFloor + "-" + id);
        letout();
        sleep(2 * open);
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
                        pass.getEleId());
            }
        }
    }

    private void letin() {
        if (passengers.size() == capacity) {
            return;
        }
        synchronized (myRequest) {
            Iterator<Passenger> iterator =
                    myRequest.getRequestTable().get(nowFloor).values().iterator();
            while (iterator.hasNext()) {
                if (passengers.size() == capacity) {
                    break; //注意此处边进人边判断！！！
                }
                Passenger passer = iterator.next();
                if (canIn(passer)) {
                    passengers.put(passer.getId(), passer);
                    iterator.remove();
                    TimableOutput.println("IN-" + passer.getId() + "-" + nowFloor + "-" +
                            passer.getEleId());
                }
            }
        }
    }

    private boolean canIn(Passenger passenger) {
        return (passenger.getTo() > nowFloor && direction > 0) ||
                (passenger.getTo() < nowFloor && direction < 0);
    }
}
