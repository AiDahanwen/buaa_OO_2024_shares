import com.oocourse.spec3.exceptions.PersonIdNotFoundException;

import java.util.HashMap;

public class MyPersonIdNotFoundException extends PersonIdNotFoundException {
    private int id;
    private static int times;
    private static HashMap<Integer, Integer> idTimes = new HashMap<>();

    public MyPersonIdNotFoundException(int id) {
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
        System.out.printf("pinf-%d, %d-%d", times, id, idTimes.get(id));
        System.out.println();
    }
}
