import java.util.HashMap;

public class Strategy {
    private final Request request;
    private HashMap<Integer, Passenger> passengers;

    public Strategy(Request request, HashMap<Integer, Passenger> passengers) {
        this.request = request;
        this.passengers = passengers;
    }

    public Suggestion getSuggestion(int nowFloor,int direction) {
        //look调度算法
        if (canOpenForOut(nowFloor,direction) || canOpenForIn(nowFloor,direction)) {
            return Suggestion.OPEN;
        }  //判断是否开门
        if (!passengers.isEmpty()) { //开门后判断是否有人，
            return Suggestion.MOVE; //确实需要动态维护里面的passengers
        } else { //说明电梯里没人
            if (request.isEmpty()) { //请求列表为空，
                if (request.isEnd()) {
                    return Suggestion.END;
                } else {
                    return Suggestion.WAIT;
                }
            } else { //列表不为空了，
                if (hasSameDirection(nowFloor,direction)) {
                    return Suggestion.MOVE; //在原始方向上继续移动
                } else { //列表不为空且没有相同方向的
                    return Suggestion.REVERSE; //为什么此处reverse？
                }
            }
        }
    }

    private boolean hasSameDirection(int nowFloor,int direction) { //电梯此时没人，判断一下
        synchronized (request) {
            HashMap<Integer, HashMap<Integer, Passenger>> fromMap = request.getRequestTable();
            for (Integer integer : fromMap.keySet()) {
                if (integer > nowFloor && direction > 0 && (!fromMap.get(integer).isEmpty())) {
                    return true;
                } else if (integer < nowFloor && direction < 0 &&
                        (!fromMap.get(integer).isEmpty())) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean canOpenForOut(int nowFloor,int direction) {
        for (Passenger it : passengers.values()) {
            if (it.getTo() == nowFloor) {
                return true;
            } //只需要找到是否有，具体的不用管
        }
        return false;
    }

    private boolean canOpenForIn(int nowFloor,int direction) {
        if (passengers.size() >= 6) {
            return false;
        }
        synchronized (request) {
            HashMap<Integer, Passenger> passers = request.getRequestTable().get(nowFloor);
            if (passers != null) {
                for (Passenger pass : passers.values()) {
                    if (pass.getTo() > nowFloor && direction > 0) {
                        return true;
                    } else if (pass.getTo() < nowFloor && direction < 0) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
}
