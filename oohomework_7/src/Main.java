import com.oocourse.elevator3.Request;
import com.oocourse.elevator3.TimableOutput;

import java.util.ArrayList;
import java.util.HashMap;

public class Main {
    public static void main(String[] args) {
        TimableOutput.initStartTimestamp();
        ArrayList<Request> waitingList = new ArrayList<>(); //暂时先不重构
        HashMap<Integer, Elevator> elevators = new HashMap<>();

        //dispatch part
        Dispatch dispatch = new Dispatch(waitingList, elevators);
        dispatch.start();

        //elevator part
        for (int i = 1; i <= 6; i++) {
            Elevator elevator = new Elevator(i, dispatch, waitingList, null);
            elevators.put(i, elevator);
            elevator.start(); //启动elevator进程
        }

        //input part
        InputHandler inputHandler = new InputHandler(waitingList, dispatch);
        inputHandler.start(); //启动input进程
    }
}
