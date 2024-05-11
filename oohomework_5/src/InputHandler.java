import com.oocourse.elevator1.PersonRequest;
import com.oocourse.elevator1.ElevatorInput;

import java.io.IOException;
import java.util.HashMap;

public class InputHandler extends Thread {
    private final HashMap<Integer, Request> requests; //每个电梯一个request
    private HashMap<Integer, Elevator> elevators; //电梯的
    private static final boolean LOG = true; //是否输出调试信息
    private static final int INITIAL = 1; //初始位置
    private static final int CAPACITY = 6; //限乘人数
    private static final int OPEN = 200; //开门时间
    private static final int CLOSE = 200; //关门时间
    private static final int MOVE = 400; //移动一层楼的时间
    private static final int HIGHESTFLOOR = 11; //最高到达的楼层
    private static final int ELEVATORNUMBER = 6; //电梯数量

    public InputHandler() {
        requests = new HashMap<>();
        elevators = new HashMap<>();
        for (int i = 1; i <= ELEVATORNUMBER; i++) {
            Request request = new Request(HIGHESTFLOOR);
            requests.put(i, request);
            Elevator elevator = new Elevator(i, INITIAL, CAPACITY, OPEN, CLOSE,
                    MOVE, HIGHESTFLOOR, request);
            elevators.put(i, elevator);
            elevator.start(); //每个elevator开始跑
        }
    }

    @Override
    public void run() {
        ElevatorInput elevatorInput = new ElevatorInput(System.in);
        while (true) { //开始接受input，进行分配
            PersonRequest personRequest = elevatorInput.nextPersonRequest();
            if (personRequest == null) {
                for (int i = 1; i <= requests.size(); i++) {
                    requests.get(i).setEnd();
                }
                break;
            } else {
                int eleId = personRequest.getElevatorId();
                Passenger passenger = new Passenger(personRequest.getPersonId(),
                        personRequest.getFromFloor(),
                        personRequest.getToFloor(), eleId);
                Request tempRequest = requests.get(personRequest.getElevatorId());
                tempRequest.addRequest(passenger);
            }
        }
        try {
            elevatorInput.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
