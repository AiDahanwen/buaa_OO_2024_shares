import com.oocourse.spec3.exceptions.RelationNotFoundException;

import java.util.HashMap;

public class MyRelationNotFoundException extends RelationNotFoundException {
    private int id1;
    private int id2;
    private static int times = 0;
    private static HashMap<Integer, Integer> idTimes = new HashMap<>();

    public MyRelationNotFoundException(int id1, int id2) {
        this.id1 = id1;
        this.id2 = id2;
        times++;
        if (idTimes.containsKey(id1)) {
            idTimes.put(id1, idTimes.get(id1) + 1);
        } else {
            idTimes.put(id1, 1);
        }
        if (id1 != id2) {
            if (idTimes.containsKey(id2)) {
                idTimes.put(id2, idTimes.get(id2) + 1);
            } else {
                idTimes.put(id2, 1);
            }
        }
    }

    @Override
    public void print() {
        int smaller;
        int larger;
        smaller = Math.min(id1, id2);
        larger = Math.max(id1, id2);
        System.out.printf("rnf-%d, %d-%d, %d-%d", times, smaller, idTimes.get(smaller),
                larger, idTimes.get(larger));
        System.out.println();
    }
}
