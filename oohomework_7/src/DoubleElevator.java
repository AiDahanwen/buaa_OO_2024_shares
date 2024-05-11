import com.oocourse.elevator3.Request;

import java.util.ArrayList;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class DoubleElevator extends Elevator {
    private Elevator elevatorA;
    private Elevator elevatorB;
    private int changeFloor;
    private Lock lock;
    private int id;
    private int hasSolved;

    public DoubleElevator(int id, int changeFloor, Dispatch dispatch,
                          ArrayList<Request> waitingList, int hasSolved) {
        super(id, dispatch, waitingList, null);
        this.id = id;
        lock = new ReentrantLock();
        this.hasSolved = hasSolved;
        elevatorA = new Elevator(id, dispatch, waitingList, lock); //A
        elevatorB = new Elevator(id, dispatch, waitingList, lock); //B
        elevatorA.getMyRequest().setType("A");
        elevatorB.getMyRequest().setType("B");
        elevatorA.setNowFloor(changeFloor - 1);
        elevatorB.setNowFloor(changeFloor + 1);
        elevatorA.start();
        elevatorB.start();
        this.changeFloor = changeFloor;
    }

    public Elevator getElevatorA() {
        return elevatorA;
    }

    public Elevator getElevatorB() {
        return elevatorB;
    }

    public int getChangeFloor() {
        return changeFloor;
    }

    public void setMove(int newMove) {
        elevatorA.getParameter().setMove(newMove);
        elevatorB.getParameter().setMove(newMove);
    }

    public void setCapacity(int newCapacity) {
        elevatorA.getParameter().setCapacity(newCapacity);
        elevatorB.getParameter().setCapacity(newCapacity);
    }

    public void setEnd() {
        elevatorA.getMyRequest().setEnd();
        elevatorB.getMyRequest().setEnd();
    }

    public synchronized int getSolvedSize() {
        return elevatorA.getSolvedSize() + elevatorB.getSolvedSize() + hasSolved;
    }

    public void setParameter(int newMove, int newCapacity) {
        setCapacity(newCapacity);
        setMove(newMove);
    }
}
