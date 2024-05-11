public class Parameter {
    private int move;
    private final int open;
    private final int close;
    private int capacity;

    public Parameter() {
        move = 400;
        open = 200;
        close = 200;
        capacity = 6;
    }

    public int getMove() {
        return move;
    }

    public int getOpen() {
        return open;
    }

    public int getClose() {
        return close;
    }

    public int getCapacity() {
        return capacity;
    }

    public void setMove(int newMove) {
        move = newMove;
    }

    public void setCapacity(int newCapacity) {
        capacity = newCapacity;
    }
}
