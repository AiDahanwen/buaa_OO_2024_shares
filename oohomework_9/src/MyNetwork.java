import com.oocourse.spec1.main.Network;
import com.oocourse.spec1.main.Person;

import java.util.HashMap;

public class MyNetwork implements Network {
    private HashMap<Integer, Person> persons; //找人的时候用
    private int tripleSum;
    //private HashMap<Integer, Integer> parents; //储存父节点，key为本身，value为父节点
    //private HashMap<Integer, Integer> ranks; //储存根节点的秩
    private DisjointSet disjointSet;

    public MyNetwork() {
        persons = new HashMap<>();
        disjointSet = new DisjointSet();
        tripleSum = 0;
    }

    @Override
    public boolean containsPerson(int id) {
        return persons.containsKey(id);
    }

    @Override
    public Person getPerson(int id) {
        return persons.getOrDefault(id, null);
    }

    @Override
    public void addPerson(Person person) throws MyEqualPersonIdException {
        MyPerson myPerson = (MyPerson) person;
        if (!containsPerson(person.getId())) {
            persons.put(person.getId(), myPerson);
            disjointSet.add(myPerson);
        } else {
            throw new MyEqualPersonIdException(person.getId());
        }
    }

    @Override
    public void addRelation(int id1, int id2, int value) throws
            MyPersonIdNotFoundException, MyEqualRelationException {
        boolean include1 = containsPerson(id1);
        boolean include2 = containsPerson(id2);
        if (include1 && include2) {
            MyPerson person1 = (MyPerson) getPerson(id1);
            MyPerson person2 = (MyPerson) getPerson(id2);
            if (!person1.isLinked(person2)) {
                person1.addAcquaintance(person2, value);
                person2.addAcquaintance(person1, value);
                maintainTriples(id1, id2, 0); //此时已有一条边
                maintainBlocks(id1, id2, 0); //注意此时两个人已经连接上了
            } else {
                throw new MyEqualRelationException(id1, id2);
            }
        } else {
            if (!include1) {
                throw new MyPersonIdNotFoundException(id1);
            } else {
                throw new MyPersonIdNotFoundException(id2);
            }
        }
    }

    @Override
    public void modifyRelation(int id1, int id2, int value) throws
            MyPersonIdNotFoundException, MyEqualPersonIdException, MyRelationNotFoundException {
        boolean include1 = containsPerson(id1);
        boolean include2 = containsPerson(id2);
        boolean notEqual = id1 != id2;
        if (include1 && include2 && notEqual) {
            MyPerson person1 = (MyPerson) getPerson(id1);
            MyPerson person2 = (MyPerson) getPerson(id2);
            int newValue1 = person1.queryValue(person2) + value;
            int newValue2 = person2.queryValue(person1) + value;
            if (person1.isLinked(person2)) {
                if (newValue1 > 0) {
                    person1.modifyValue(person2, newValue1);
                    person2.modifyValue(person1, newValue2);
                } else { //解除person1和person2的关系
                    person1.unLinked(person2);
                    person2.unLinked(person1);
                    maintainBlocks(id1, id2, 1);
                    maintainTriples(id1, id2, 1);
                }
            } else {
                throw new MyRelationNotFoundException(id1, id2);
            }
        } else if (!include1) {
            throw new MyPersonIdNotFoundException(id1);
        } else if (!include2) {
            throw new MyPersonIdNotFoundException(id2);
        } else {
            throw new MyEqualPersonIdException(id1);
        }
    }

    @Override
    public int queryValue(int id1, int id2) throws MyPersonIdNotFoundException,
            MyRelationNotFoundException {
        boolean include1 = containsPerson(id1);
        boolean include2 = containsPerson(id2);
        boolean linked;
        if (include1 && include2) {
            linked = getPerson(id1).isLinked(persons.get(id2));
            if (linked) {
                return getPerson(id1).queryValue(persons.get(id2));
            } else {
                throw new MyRelationNotFoundException(id1, id2);
            }
        } else {
            if (!include1) {
                throw new MyPersonIdNotFoundException(id1);
            } else {
                throw new MyPersonIdNotFoundException(id2);
            }
        }
    }

    @Override
    public boolean isCircle(int id1, int id2) throws MyPersonIdNotFoundException {
        boolean include1 = containsPerson(id1);
        boolean include2 = containsPerson(id2);
        if (include1 && include2) {
            return disjointSet.find(id1) == disjointSet.find(id2);
        } else {
            if (!include1) {
                throw new MyPersonIdNotFoundException(id1);
            } else {
                throw new MyPersonIdNotFoundException(id2);
            }
        }
    }

    public int queryBlockSum() {
        return disjointSet.getBlockSum();
    } //remake

    public int queryTripleSum() {
        //return triples.size();
        return tripleSum;
    }

    public void maintainBlocks(int id1, int id2, int op) { //背景：二者联通，恐怕不能路径压缩，因为会破坏结构
        if (op == 0) {
            disjointSet.merge(id1, id2);
        } else {
            disjointSet.delete((MyPerson) getPerson(id1), (MyPerson) getPerson(id2));
        }
    }

    public void maintainTriples(int id1, int id2, int op) {  //背景：二者联通
        MyPerson person1 = (MyPerson) getPerson(id1);
        MyPerson person2 = (MyPerson) getPerson(id2);
        HashMap<Integer, Person> acquaintance1 = person1.getAcquaintance();
        HashMap<Integer, Person> acquaintance2 = person2.getAcquaintance();
        if (op == 0) {
            //将遍历所有人改成遍历只遍历acquaintance，看是否有相同的acquaintance
            if (acquaintance1.size() < acquaintance2.size()) {
                for (Person item : acquaintance1.values()) {
                    if (person2.isLinked(item) && (!item.equals(person1)) &&
                            (!item.equals(person2))) {
                        tripleSum++;
                    }
                }
            } else {
                for (Person item : acquaintance2.values()) {
                    if (person1.isLinked(item) && (!item.equals(person1))
                            && (!item.equals(person2))) {
                        tripleSum++;
                    }
                }
            }
        } else {
            if (acquaintance1.size() < acquaintance2.size()) {
                for (Person item : acquaintance1.values()) {
                    if (person2.isLinked(item) && (!item.equals(person1)) &&
                            (!item.equals(person2))) {
                        tripleSum--;
                    }
                }
            } else {
                for (Person item : acquaintance2.values()) {
                    if (person1.isLinked(item) && (!item.equals(person1))
                            && (!item.equals(person2))) {
                        tripleSum--;
                    }
                }
            }
        }
    }

    public Person[] getPersons() {
        return null;
    }
}
