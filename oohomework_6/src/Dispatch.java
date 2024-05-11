import com.oocourse.elevator2.PersonRequest;
import com.oocourse.elevator2.Request;
import com.oocourse.elevator2.ResetRequest;
import com.oocourse.elevator2.TimableOutput;

import java.util.ArrayList;
import java.util.HashMap;

public class Dispatch extends Thread {
    private HashMap<Integer, RequestList> requests; //requests指的是id为integer的电梯所接受的请求的集合
    private HashMap<Integer, Elevator> elevators;
    private ArrayList<Request> waitingList;
    private boolean end;
    private int resetNumber;
    private int finishedReset;

    public Dispatch(ArrayList<Request> waitingList) {
        this.waitingList = waitingList;
        requests = new HashMap<>();
        elevators = new HashMap<>();
        end = false;
        for (int i = 1; i <= 6; i++) {
            RequestList req = new RequestList(11, i);
            requests.put(i, req);
            Elevator elevator = new Elevator(i, req, waitingList, this);
            elevators.put(i, elevator);
            elevator.start();
        }
        resetNumber = 0;
        finishedReset = 0;
    }

    @Override
    public void run() {
        while (true) {
            Request request;
            boolean test1 = end; //
            boolean test2;
            synchronized (waitingList) {
                test2 = waitingList.isEmpty();
                waitingList.notifyAll();
            }
            if (test1 && test2) {
                if (resetNumber == finishedReset) { //输入结束且waitinglist是空的，
                    // 说明request不会再变化了
                    for (int i = 1; i <= requests.size(); i++) {
                        requests.get(i).setEnd();
                    }
                    //TimableOutput.println("the dispatcher end!!");
                    break;
                } else {
                    TimableOutput.println("will sleep 1200 ms -1");
                    try {
                        sleep(1200); //reset执行完
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
            //下面这部分加锁吗？？
            synchronized (waitingList) {
                while (!end && waitingList.isEmpty()) { //出来有三种可能：end为true，waitinglist不为空，或者二者兼有
                    try {
                        waitingList.wait();
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }
                if (end && waitingList.isEmpty()) {
                    if (resetNumber == finishedReset) { //输入结束且waitinglist是空的，
                        // 说明request不会再变化了
                        for (int i = 1; i <= requests.size(); i++) {
                            requests.get(i).setEnd();
                        }
                        //TimableOutput.println("the dispatcher end!!");
                        break;
                    } else {
                        TimableOutput.println("will sleep 1200ms -2");
                        try {
                            sleep(1200);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        continue;
                    }
                }
                request = waitingList.get(0); //前面判断的时候是false，现在为true。。。
                waitingList.remove(request);
                waitingList.notifyAll(); //注意
            }
            if (request instanceof PersonRequest) {
                dispatchPeople((PersonRequest) request);
            } else if (request instanceof ResetRequest) {
                dispatchReset((ResetRequest) request);
                resetNumber++;
            }
        }
    } //此处锁的问题需要重视！

    private void dispatchPeople(PersonRequest personRequest) {
        // 1. 找到是否有停层
        ArrayList<Elevator> waiting = findWait(); //
        // 2. 找到同方向的
        ArrayList<Elevator> sameDirection = findSame(personRequest);
        // 3. 判断谁离得近
        Elevator nearest = findNearest(waiting, sameDirection, personRequest);
        if (nearest == null) {
            nearest = findFewest();
        }
        while (nearest == null) { //注意是while
            try {
                sleep(1200);
            } catch (Exception e) {
                e.printStackTrace();
            }
            nearest = findFewest();
        }
        // nearest 即为最后分派的电梯
        TimableOutput.println("RECEIVE-" + personRequest.getPersonId() + "-" +
                nearest.getElevatorId());
        nearest.getMyRequestList().addRequest(personRequest);
        //System.out.println("one person has been dispatched! ");
        //waitingList.remove(personRequest);
        //System.out.println("one person request has been removed!");
    } //电梯分配

    private void dispatchReset(ResetRequest resetRequest) { //resetAccept和加进去之间的时间间隔太长了
        elevators.get(resetRequest.getElevatorId()).setReset();
        requests.get(resetRequest.getElevatorId()).addRequest(resetRequest);
        //waitingList.remove(resetRequest);
    }

    private ArrayList<Elevator> findWait() {
        ArrayList<Elevator> waiting = new ArrayList<>();
        for (int i = 1; i <= 6; i++) {
            Elevator temp = elevators.get(i); //
            if (temp.isWait() && !temp.isReset()) { //dispatch线程要读isreset
                //TimableOutput.println("elevator " + temp.getElevatorId() + " is not reset!");
                waiting.add(temp);
            }
        }
        return waiting;
    }

    private ArrayList<Elevator> findSame(PersonRequest person) { //同方向的
        ArrayList<Elevator> same = new ArrayList<>();
        for (int i = 1; i <= 6; i++) {
            Elevator temp = elevators.get(i);
            if (temp.isReset()) {
                continue;
            }
            if (isSameDirectiorn(temp, person) &&
                    (temp.getPeopleNumber() + temp.getRequestNumber()) < temp.getCapacity()) {
                same.add(temp);
                //TimableOutput.println("elevator " + temp.getElevatorId() + " is not reset!");
            }
        }
        return same;
    }

    private Elevator findNearest(ArrayList<Elevator> waiting, ArrayList<Elevator> same,
                                 PersonRequest person) {
        Elevator nearest = null;
        int min = 100;
        ArrayList<Elevator> sameDistance = new ArrayList<>();
        for (Elevator item : waiting) {
            if (Math.abs(item.getNowFloor() - person.getFromFloor()) < min) {
                nearest = item;
                min = Math.abs(item.getNowFloor() - person.getFromFloor());
                sameDistance.clear();
                sameDistance.add(item);
            } else if (Math.abs(item.getNowFloor() - person.getFromFloor()) == min) {
                sameDistance.add(item);
            }
        }
        for (Elevator item : same) {
            if (Math.abs(item.getNowFloor() - person.getFromFloor()) < min) {
                nearest = item;
                min = Math.abs(item.getNowFloor() - person.getFromFloor());
                sameDistance.clear();
                sameDistance.add(item);
            } else if (Math.abs(item.getNowFloor() - person.getFromFloor()) == min) {
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

    private Elevator findFewest() { //请求最少
        Elevator fewest = null;
        int min = 2147483647;
        for (Elevator item : elevators.values()) {
            if (item.isReset()) {
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
        }
        return quick;
    }

    public void setEnd() {
        end = true; //该end仅表示输入是否结束，与dispatch进程的结束无关
    }

    public synchronized void addFinish() {
        finishedReset++;
    }
}
