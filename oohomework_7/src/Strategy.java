import java.util.ArrayList;
import java.util.HashMap;

public class Strategy {
    private final RequestList request;
    private HashMap<Integer, Passenger> passengers;

    public Strategy(RequestList request, HashMap<Integer, Passenger> passengers) {
        this.request = request;
        this.passengers = passengers;
    }

    public Suggestion getSuggestion(int nowFloor, int direction, int capacity,
                                    int changeFloor, String type) {
        if (request.haveNormalReset()) {
            return Suggestion.NORMALRESET;
        }
        if (request.haveDoubleReset()) {
            return Suggestion.DOUBLERESET;
        } // reset first
        //look schedule but note that when is double, a change will happen
        if (canOpenForOut(nowFloor) || canOpenForIn(nowFloor, direction, capacity)) {
            return Suggestion.OPEN;
        }   // use lock to prevent a and b at the changefloor at the same time
        if (!passengers.isEmpty()) {
            return Suggestion.MOVE;
        } else { // there is no people in the elevator
            synchronized (request) {
                //TimableOutput.println("the request is empty whether or not?" + " id is " + eleId);
                if (request.isEmpty()) { //请求列表为空，
                    if (request.isEnd()) { //request不会再变化了
                        //TimableOutput.println("the request is end!" + " id is " + eleId);
                        return Suggestion.END;
                    } else {
                        //TimableOutput.println("the request is wait" + " id is " + eleId);
                        return Suggestion.WAIT;
                    }
                } else { //only request of people
                    if (hasSameDirection(nowFloor, direction, changeFloor, type)) {
                        return Suggestion.MOVE; //在原始方向上继续移动
                    } else { //列表不为空且没有相同方向的
                        return Suggestion.REVERSE; //为什么此处reverse？
                    }
                }
            }
        }
    }

    private boolean hasSameDirection(int nowFloor, int direction, int changeFloor, String type) {
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
            if (it.getToFloor() == nowFloor) {
                return true;
            }
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
                if (pass.getToFloor() > nowFloor && direction > 0) {
                    return true;
                } else if (pass.getToFloor() < nowFloor && direction < 0) {
                    return true;
                }
            }
        }
        return false;
    }
}
