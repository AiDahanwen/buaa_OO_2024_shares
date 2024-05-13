import com.oocourse.spec2.exceptions.EqualPersonIdException;

import java.util.HashMap;

public class MyEqualPersonIdException extends EqualPersonIdException {
    private static int times;
    private int id;
    private static HashMap<Integer, Integer> idTimes = new HashMap<>();

    public MyEqualPersonIdException(int id) {
        this.id = id;
        times++;
        if (idTimes.containsKey(id)) {
            idTimes.put(id, idTimes.get(id) + 1);
        } else {
            idTimes.put(id, 1);
        }
    }

    @Override
    public void print() {
        System.out.printf("epi-%d, %d-%d", times, id, idTimes.get(id));
        System.out.println();
    }
}

