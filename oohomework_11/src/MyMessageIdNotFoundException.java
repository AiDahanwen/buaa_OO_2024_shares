import com.oocourse.spec3.exceptions.MessageIdNotFoundException;

import java.util.HashMap;

public class MyMessageIdNotFoundException extends MessageIdNotFoundException {
    private int id;
    private static int times = 0;
    private static HashMap<Integer, Integer> idTimes = new HashMap<>();

    public MyMessageIdNotFoundException(int id) {
        this.id = id;
        times++;
        if (idTimes.containsKey(id)) {
            int old = idTimes.get(id);
            idTimes.put(id, old + 1);
        } else {
            idTimes.put(id, 1);
        }
    }

    public void print() {
        System.out.printf("minf-%d, %d-%d", times, id, idTimes.get(id));
        System.out.println();
    }
}
