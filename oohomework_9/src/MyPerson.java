import com.oocourse.spec1.main.Person;

import java.util.HashMap;

public class MyPerson implements Person {
    private int id;
    private String name; //要求不为空
    private int age;
    private HashMap<Integer, Person> acquaintance; //数组要被建立起来，指针不为null
    private HashMap<Integer, Integer> value; //数组指针不能为null

    public MyPerson(int id, String name, int age) {
        this.id = id;
        this.name = name;
        this.age = age;
        acquaintance = new HashMap<>();
        value = new HashMap<>();
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

    @Override
    public boolean equals(Object obj) {
        if (obj == null || !(obj instanceof MyPerson)) {
            return false;
        }
        return ((MyPerson) obj).getId() == this.id;
    }

    public boolean isLinked(Person person) {
        return acquaintance.containsValue(person) || person.getId() == this.id;
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
            this.value.put(person.getId(), value);
        }
    }

    public void modifyValue(MyPerson person, int newValue) {
        if (acquaintance.containsValue(person) && value.containsKey(person.getId())) {
            value.put(person.getId(), newValue);
        }
    }

    public void unLinked(MyPerson person) {
        acquaintance.remove(person.getId());
        value.remove(person.getId());
    }

    public MyPerson haveSameAcquaintance(MyPerson person) {
        if (person.getAcquaintance().size() < acquaintance.size()) {
            for (Person item : person.getAcquaintance().values()) {
                if ((this.isLinked(item)) && (!item.equals(person)) &&
                        (!item.equals(this))) {
                    return (MyPerson) item;
                }
            }
        } else {
            for (Person item : acquaintance.values()) {
                if ((person.isLinked(item)) && (!item.equals(person)) &&
                        (!item.equals(this))) {
                    return (MyPerson) item;
                }
            }
        }
        return null;  //区分大小是为了节省遍历时间，不区分也可以
    }

    public HashMap<Integer, Person> getAcquaintance() {
        return acquaintance;
    }

    public boolean strictEquals(Person person) {
        return true;
    }
}
