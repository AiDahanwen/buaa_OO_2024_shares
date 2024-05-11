public class Passenger {
    private int id;
    private int from;
    private int to;
    private int eleId; //其要乘坐的电梯id

    public Passenger(int id, int from, int to, int eleId) {
        this.id = id;
        this.from = from;
        this.to = to;
        this.eleId = eleId;
    }

    public int getFrom() {
        return from;
    }

    public int getTo() {
        return to;
    }

    public int getEleId() {
        return eleId;
    }

    public int getId() {
        return id;
    }
}
