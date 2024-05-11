import com.oocourse.elevator3.PersonRequest;

public class Passenger {
    private int id;
    private int toFloor;
    private int fromFloor;
    private PersonRequest myself;

    public Passenger(int id, int toFloor, int fromFloor, PersonRequest myself) {
        this.fromFloor = fromFloor;
        this.toFloor = toFloor;
        this.id = id;
        this.myself = myself;
    }

    public int getId() {
        return id;
    }

    public int getToFloor() {
        return toFloor;
    }

    public int getFromFloor() {
        return fromFloor;
    }

    public PersonRequest getMyself() {
        return myself;
    }
}
