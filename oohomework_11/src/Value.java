public class Value implements Comparable<Value> {
    private int id;
    private int value;

    public Value(int id, int value) {
        this.id = id;
        this.value = value;
    }

    public int getId() {
        return id;
    }

    public int getValue() {
        return value;
    }

    public void setValue(int newValue) {
        value = newValue;
    }

    @Override
    public int compareTo(Value o) {
        if (o.value != this.value) {
            return Integer.compare(this.value, o.value);
        } else {
            return Integer.compare(o.id, this.id);
        }
    }
}
