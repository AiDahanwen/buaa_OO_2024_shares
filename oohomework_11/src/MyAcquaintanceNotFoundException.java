import com.oocourse.spec3.exceptions.AcquaintanceNotFoundException;

import java.util.HashMap;

public class MyAcquaintanceNotFoundException extends AcquaintanceNotFoundException {
    private int id;
    private static int times = 0;
    private static HashMap<Integer, Integer> idTimes = new HashMap<>();

    public MyAcquaintanceNotFoundException(int id) {
        this.id = id;
        times++;
        if (idTimes.containsKey(id)) {
            idTimes.put(id, idTimes.get(id) + 1);
        } else {
            idTimes.put(id, 1);
        }
    }

    public void print() {
        System.out.printf("anf-%d, %d-%d", times, id, idTimes.get(id));
        System.out.println();
    }
}
