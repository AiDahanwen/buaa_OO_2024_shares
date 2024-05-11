import java.io.IOException;
import java.util.ArrayList;

import com.oocourse.elevator3.ElevatorInput;
import com.oocourse.elevator3.Request;

public class InputHandler extends Thread {
    private ArrayList<Request> waitingList; //note: data shouldn't be stored in a thread
    private Dispatch dispatch;

    public InputHandler(ArrayList<Request> waitingList, Dispatch dispatch) {
        this.waitingList = waitingList;
        this.dispatch = dispatch;
    }

    @Override
    public void run() {
        ElevatorInput elevatorInput = new ElevatorInput(System.in);
        while (true) {
            //TimableOutput.println("inputttttt!!");
            Request request = elevatorInput.nextRequest(); //单个请求
            if (request == null) {  // input end
                dispatch.setEnd();
                synchronized (waitingList) {
                    waitingList.notifyAll();
                } // because the wait condition has changed so here is a notifyAll();
                // notifyAll means the lock will be released, so it should be in a syn block
                //TimableOutput.println("the input is end!!");
                break;
            } else {
                synchronized (waitingList) {
                    waitingList.add(request);
                    waitingList.notifyAll();
                }
            }
        }
        try {
            elevatorInput.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
