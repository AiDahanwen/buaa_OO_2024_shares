import com.oocourse.elevator2.Request;

import java.util.ArrayList;
import java.util.HashMap;

public class Strategy { //增加reset的书写,修改一些东西。。。
    private final RequestList request; //首先注意request此时要加锁，其次注意request不再全是人的request
    private HashMap<Integer, Passenger> passengers; //passengers需要，但是现在需要重新理一理
    private final ArrayList<Request> waitingList;

    public Strategy(RequestList request, HashMap<Integer, Passenger> passengers,
                    ArrayList<Request> waitingList) {
        this.request = request;
        this.passengers = passengers;
        this.waitingList = waitingList;
    }

    public Suggestion getSuggestion(int nowFloor, int direction, int capacity) {
        //TimableOutput.println("is getting suggestions!");
        if (havaReset()) {
            return Suggestion.RESET;
        }
        //look调度算法
        if (canOpenForOut(nowFloor) || canOpenForIn(nowFloor, direction, capacity)) {
            return Suggestion.OPEN;
        }  //判断是否开门
        if (!passengers.isEmpty()) { //开门后判断是否有人，
            return Suggestion.MOVE; //确实需要动态维护里面的passengers
        } else { //说明电梯里没人
            synchronized (request) {
                if (request.isEmpty()) { //请求列表为空，
                    if (request.isEnd()) { //request不会再变化了
                        return Suggestion.END;
                    } else {
                        return Suggestion.WAIT;
                    }
                } else { //列表不为空了，且只有人的请求没有reset请求
                    if (hasSameDirection(nowFloor, direction)) {
                        //TimableOutput.println("hasSameDirection!!!");
                        return Suggestion.MOVE; //在原始方向上继续移动
                    } else { //列表不为空且没有相同方向的
                        return Suggestion.REVERSE; //为什么此处reverse？
                    }
                }
            }
        }
    }

    private boolean hasSameDirection(int nowFloor, int direction) { //电梯此时没人，判断一下
        HashMap<Integer, ArrayList<Passenger>> fromMap = request.getRequestPeople();
        for (Integer integer : fromMap.keySet()) {
            if (integer > nowFloor && direction > 0 && (!fromMap.get(integer).isEmpty())) {
                return true;
            } else if (integer < nowFloor && direction < 0 &&
                    (!fromMap.get(integer).isEmpty())) {
                return true;
            }
        }
        return false;
    }

    private boolean canOpenForOut(int nowFloor) {
        for (Passenger it : passengers.values()) {
            if (it.getTo() == nowFloor) {
                return true;
            } //只需要找到是否有，具体的不用管
        }
        return false;
    }

    private boolean canOpenForIn(int nowFloor, int direction, int capacity) {
        if (passengers.size() >= capacity) {
            return false;
        }
        synchronized (request) {
            ArrayList<Passenger> passers = request.getRequestPeople().get(nowFloor);
            for (Passenger pass : passers) {
                if (pass.getTo() > nowFloor && direction > 0) {
                    return true;
                } else if (pass.getTo() < nowFloor && direction < 0) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean havaReset() {
        return !request.getRequestReset().isEmpty();
    }
}
