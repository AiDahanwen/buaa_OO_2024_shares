import com.oocourse.spec2.main.Person;
import com.oocourse.spec2.main.Tag;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

public class MyPerson implements Person {
    private int id;
    private String name; //要求不为空
    private int age;
    private HashMap<Integer, Person> acquaintance; //数组要被建立起来，指针不为null
    private ArrayList<Value> sortedValues; //用于bestAcquaintance的修改
    private HashMap<Integer, Integer> value; //数组指针不能为null
    private HashMap<Integer, Value> helpValue; //为了提高查找效率
    private HashMap<Integer, Tag> tags;
    private int bestAcquaintanceId;
    private int bestValue;

    public MyPerson(int id, String name, int age) {
        this.id = id;
        this.name = name;
        this.age = age;
        acquaintance = new HashMap<>();
        value = new HashMap<>();
        tags = new HashMap<>();
        sortedValues = new ArrayList<>();
        bestAcquaintanceId = -1;
        bestValue = -1;
        helpValue = new HashMap<>();
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public int getAge() {
        return age;
    }

    public boolean containsTag(int id) {
        return tags.containsKey(id);
    }

    public Tag getTag(int id) {
        if (containsTag(id)) {
            return tags.get(id);
        }
        return null;
    }

    public void addTag(Tag tag) {
        tags.put(tag.getId(), tag);
    }

    public void delTag(int id) {
        if (containsTag(id)) {
            tags.remove(id);
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null || !(obj instanceof MyPerson)) {
            return false;
        }
        return ((MyPerson) obj).getId() == this.id;
    }

    public boolean isLinked(Person person) {
        return acquaintance.containsValue(person)
                || person.getId() == this.id;
    }

    public int queryValue(Person person) {
        if (acquaintance.containsValue(person)) {
            return value.get(person.getId());
        } else {
            return 0;
        }
    }

    public void addAcquaintance(Person person, int value) {
        if (!this.isLinked(person)) {
            acquaintance.put(person.getId(), person);
            Value v = new Value(person.getId(), value);
            sortedValues.add(v);
            helpValue.put(person.getId(), v);
            this.value.put(person.getId(), value);
            if (value > bestValue) {
                bestValue = value;
                bestAcquaintanceId = person.getId();
            } else if (value == bestValue && person.getId() < bestAcquaintanceId) {
                bestAcquaintanceId = person.getId();
            }
        }
    }

    public void modifyValue(MyPerson person, int newValue) {
        if (acquaintance.containsValue(person) && value.containsKey(person.getId())) {
            value.put(person.getId(), newValue);
            int index = sortedValues.indexOf(helpValue.get(person.id));
            sortedValues.get(index).setValue(newValue);
            helpValue.get(person.id).setValue(newValue);
            if (newValue > bestValue) {
                bestValue = newValue;
                bestAcquaintanceId = person.id;
            } else if (newValue == bestValue && person.id < bestAcquaintanceId) {
                bestAcquaintanceId = person.id;
            } else {
                if (person.id == bestAcquaintanceId) {
                    sortAndUpdate();
                }
            }
        }
    }

    public void unLinked(MyPerson person) {
        for (Tag item : tags.values()) {
            if (item.hasPerson(person)) {
                item.delPerson(person);
            }
        } //是否还有最佳做法？
        acquaintance.remove(person.getId());
        value.remove(person.getId());
        sortedValues.remove(helpValue.get(person.id));
        helpValue.remove(person.id);
        if (person.id == bestAcquaintanceId) {
            sortAndUpdate();
        }
    }

    public HashMap<Integer, Person> getAcquaintance() {
        return acquaintance;
    }

    public int getBestAcquaintanceId() {
        return this.bestAcquaintanceId;
    }

    public boolean strictEquals(Person person) {
        return true;
    }

    public void sortAndUpdate() {
        if (sortedValues.isEmpty()) {
            bestValue = -1;
            bestAcquaintanceId = -1;
            return;
        }
        Value[] build = sortedValues.toArray(new Value[0]);
        Arrays.sort(build);
        bestAcquaintanceId = build[build.length - 1].getId();
        bestValue = build[build.length - 1].getValue();
    }

    public boolean haveTags() {
        return !tags.isEmpty();
    }

    public HashMap<Integer, Tag> getTags() {
        return tags;
    }
}
