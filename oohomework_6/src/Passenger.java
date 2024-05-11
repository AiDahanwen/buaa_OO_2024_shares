import com.oocourse.elevator2.PersonRequest;

public class Passenger {
    private int id;
    private int from;
    private int to;
    private PersonRequest myself;

    public Passenger(int id, int from, int to, PersonRequest myself) {
        this.id = id;
        this.from = from;
        this.to = to;
        this.myself = myself;
    }

    public int getFrom() {
        return from;
    }

    public int getTo() {
        return to;
    }

    public int getId() {
        return id;
    }

    public PersonRequest getMyself() {
        return myself;
    }
}
