import com.oocourse.spec2.main.Person;
import com.oocourse.spec2.main.Tag;

import java.util.HashMap;

public class MyTag implements Tag {
    private int id;
    private HashMap<Integer, Person> persons;
    private int ageSum;
    private int agePowerSum;
    private int valueSum;

    public MyTag(int id) {
        this.id = id;
        persons = new HashMap<>();
        ageSum = 0;
        agePowerSum = 0;
        valueSum = 0;
    }

    public int getId() {
        return id;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null || !(obj instanceof Tag)) {
            return false;
        }
        return ((Tag) obj).getId() == id;
    }

    public boolean hasPerson(Person person) {
        return persons.containsValue(person);
    }

    public void addPerson(Person person) {
        if (!hasPerson(person)) {
            persons.put(person.getId(), person);
        }
        int age = person.getAge();
        ageSum += age;
        agePowerSum += age * age;
        HashMap<Integer, Person> a = ((MyPerson) person).getAcquaintance();
        for (Person item : a.values()) {
            if (persons.containsValue(item)) {
                addValueSum(item.queryValue(person));
            }
        }
    }

    public int getValueSum() {
        return valueSum * 2;
    }

    public int getAgeMean() {
        int n = persons.size();
        if (n == 0) {
            return 0;
        }
        return ageSum / n;
    }

    public int getAgeVar() {
        int n = persons.size();
        if (n == 0) {
            return 0;
        }
        int mean = getAgeMean();
        int temp = agePowerSum - 2 * mean * ageSum + n * mean * mean;
        return temp / n;
    }

    public void delPerson(Person person) {
        if (hasPerson(person)) {
            persons.remove(person.getId(), person);
            int age = person.getAge();
            ageSum -= age;
            agePowerSum -= age * age;
            HashMap<Integer, Person> a = ((MyPerson) person).getAcquaintance();
            for (Person item : a.values()) {
                if (persons.containsValue(item)) {
                    subValueSum(item.queryValue(person));
                }
            }
        }
    }

    public int getSize() {
        return persons.size();
    }

    public void addValueSum(int addValue) {
        valueSum += addValue;
    } //addRelation

    public void subValueSum(int subValue) {
        valueSum -= subValue;
    }

    public void modifyValueSum(int newValue, int oldValue) {
        valueSum = valueSum - oldValue + newValue;
    }
}
