import com.oocourse.spec3.exceptions.TagIdNotFoundException;

import java.util.HashMap;

public class MyTagIdNotFoundException extends TagIdNotFoundException {
    private int id;
    private static int times = 0;
    private static HashMap<Integer, Integer> idTimes = new HashMap<>();

    public MyTagIdNotFoundException(int id) {
        this.id = id;
        times++;
        if (idTimes.containsKey(id)) {
            idTimes.put(id, idTimes.get(id) + 1);
        } else {
            idTimes.put(id, 1);
        }
    }

    public void print() {
        System.out.printf("tinf-%d, %d-%d", times, id, idTimes.get(id));
        System.out.println();
    }
}
