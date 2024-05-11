public class Parameter {
    private int initial; //初始位置
    private int capacity;
    private int open; //开门时间
    private int close; //关门时间
    private int move; //移动一层楼的时间
    private int highestFloor; //最高到达的楼层
    private int elevatorNumber; //电梯数量

    public Parameter() {
        initial = 1;
        capacity = 6;
        open = 200;
        close = 200;
        move = 400;
        highestFloor = 11;
        elevatorNumber = 6;
    }

    public int getCapacity() {
        return capacity;
    }

    public int getInitial() {
        return initial;
    }

    public int getElevatornumber() {
        return elevatorNumber;
    }

    public int getClose() {
        return close;
    }

    public int getMove() {
        return move;
    }

    public int getOpen() {
        return open;
    }

    public int getHighestfloor() {
        return highestFloor;
    }

    public void setMove(int newMove) {
        move = newMove;
    }

    public void setCapacity(int newCapacity) {
        capacity = newCapacity;
    }

}
