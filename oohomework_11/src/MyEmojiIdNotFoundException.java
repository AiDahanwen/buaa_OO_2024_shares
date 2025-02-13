import com.oocourse.spec3.exceptions.EmojiIdNotFoundException;

import java.util.HashMap;

public class MyEmojiIdNotFoundException extends EmojiIdNotFoundException {
    private int id;
    private static int times = 0;
    private static HashMap<Integer, Integer> idTimes = new HashMap<>();

    public MyEmojiIdNotFoundException(int id) {
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
        System.out.printf("einf-%d, %d-%d", times, id, idTimes.get(id));
        System.out.println();
    }
}
