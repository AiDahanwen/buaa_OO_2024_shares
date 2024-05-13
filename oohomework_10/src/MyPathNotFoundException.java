import com.oocourse.spec2.exceptions.PathNotFoundException;

import java.util.HashMap;

public class MyPathNotFoundException extends PathNotFoundException {
    private int id1;
    private int id2;
    private static int times = 0;
    private static HashMap<Integer, Integer> idTimes = new HashMap<>();

    public MyPathNotFoundException(int id1, int id2) {
        this.id1 = id1;
        this.id2 = id2;
        times++;
        if (idTimes.containsKey(id1)) {
            idTimes.put(id1, idTimes.get(id1) + 1);
        } else {
            idTimes.put(id1, 1);
        }
        if (idTimes.containsKey(id2)) {
            idTimes.put(id2, idTimes.get(id2) + 1);
        } else {
            idTimes.put(id2, 1);
        }
    }

    public void print() {
        int small = Math.min(id1, id2);
        int large = Math.max(id1, id2);
        System.out.printf("pnf-%d, %d-%d, %d-%d", times, small, idTimes.get(small),
                large, idTimes.get(large));
        System.out.println();
    }
}
