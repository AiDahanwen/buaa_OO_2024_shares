import com.oocourse.spec2.exceptions.EqualTagIdException;

import java.util.HashMap;

public class MyEqualTagIdException extends EqualTagIdException {
    private int id;
    private static HashMap<Integer, Integer> idTimes = new HashMap<>();
    private static int times = 0;

    public MyEqualTagIdException(int id) {
        this.id = id;
        times++;
        if (idTimes.containsKey(id)) {
            idTimes.put(id, idTimes.get(id) + 1);
        } else {
            idTimes.put(id, 1);
        }
    }

    public void print() {
        System.out.printf("eti-%d, %d-%d", times, id, idTimes.get(id));
        System.out.println();
    }
}
