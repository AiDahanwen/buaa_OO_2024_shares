import com.oocourse.elevator2.ElevatorInput;
import com.oocourse.elevator2.Request;

import java.io.IOException;
import java.util.ArrayList;

public class InputHandler extends Thread {
    private Dispatch dispatch;
    private static final boolean LOG = true; //是否输出调试信息
    private ArrayList<Request> waitingList;

    public InputHandler() {
        waitingList = new ArrayList<>(); //总的请求队列，所有的请求先放入此队列进行
        dispatch = new Dispatch(waitingList); //
        dispatch.start();
    }

    @Override
    public void run() {
        ElevatorInput elevatorInput = new ElevatorInput(System.in);
        while (true) { //开始接受input，进行分配
            Request request = elevatorInput.nextRequest(); //单个请求
            if (request == null) {  //输入结束
                dispatch.setEnd();
                //TimableOutput.println("the input handler end!");
                synchronized (waitingList) {
                    waitingList.notifyAll();
                } //
                break; //停掉输入进程，该线程可正常结束。
            } else {
                synchronized (waitingList) {
                    waitingList.add(request); //把请求放入waitingList
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
