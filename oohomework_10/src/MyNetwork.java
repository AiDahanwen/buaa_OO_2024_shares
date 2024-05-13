import com.oocourse.spec2.main.Network;
import com.oocourse.spec2.main.Person;
import com.oocourse.spec2.main.Tag;

import java.util.HashMap;
import java.util.LinkedList;

public class MyNetwork implements Network {
    private HashMap<Integer, Person> persons; //找人的时候用
    private HashMap<Integer, Node> nodes;
    private int tripleSum;
    private int coupleSum;
    private DisjointSet disjointSet;
    private boolean changed;

    public MyNetwork() {
        persons = new HashMap<>();
        disjointSet = new DisjointSet();
        tripleSum = 0;
        coupleSum = 0;
        nodes = new HashMap<>();
        changed = false;
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
        int id = person.getId();
        if (!containsPerson(id)) {
            persons.put(id, myPerson);
            disjointSet.add(myPerson);
            nodes.put(id, new Node(id));
        } else {
            throw new MyEqualPersonIdException(person.getId());
        }
    }

    public void addTag(int personId, Tag tag) throws MyPersonIdNotFoundException,
            MyEqualTagIdException {
        if (containsPerson(personId)) {
            Person person = getPerson(personId);
            if (!person.containsTag(tag.getId())) {
                person.addTag(tag);
            } else {
                throw new MyEqualTagIdException(tag.getId());
            }
        } else {
            throw new MyPersonIdNotFoundException(personId);
        }
    }

    public void addPersonToTag(int personId1, int personId2, int tagId) throws
            MyPersonIdNotFoundException, MyRelationNotFoundException,
            MyTagIdNotFoundException, MyEqualPersonIdException {
        boolean include1 = containsPerson(personId1);
        boolean include2 = containsPerson(personId2);
        if (include1 && include2) {
            Person person1 = getPerson(personId1);
            Person person2 = getPerson(personId2);
            if (personId1 != personId2) {
                boolean link = person2.isLinked(person1);
                boolean hasTag = person2.containsTag(tagId);
                if (link && hasTag) {
                    Tag tag = person2.getTag(tagId);
                    if (!tag.hasPerson(person1)) {
                        if (tag.getSize() <= 1111) {
                            tag.addPerson(person1);
                        }
                    } else {
                        throw new MyEqualPersonIdException(personId1);
                    }
                } else {
                    if (!link) {
                        throw new MyRelationNotFoundException(personId1, personId2);
                    } else {
                        throw new MyTagIdNotFoundException(tagId);
                    }
                }
            } else {
                throw new MyEqualPersonIdException(personId1);
            }
        } else {
            if (!include1) {
                throw new MyPersonIdNotFoundException(personId1);
            } else {
                throw new MyPersonIdNotFoundException(personId2);
            }
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
                Node node1 = nodes.get(id1);
                Node node2 = nodes.get(id2);
                node1.addNeighbors(node2);
                node2.addNeighbors(node1);
                maintainTriples(id1, id2, 0); //此时已有一条边
                maintainBlocks(id1, id2, 0); //注意此时两个人已经连接上了
                for (Person itemPerson : persons.values()) {
                    MyPerson itemPerson1 = (MyPerson) itemPerson;
                    if (!itemPerson1.haveTags()) {
                        continue;
                    }
                    HashMap<Integer, Tag> itemTags = itemPerson1.getTags();
                    for (Tag itemTag : itemTags.values()) {
                        if (itemTag.hasPerson(person1) && itemTag.hasPerson(person2)) {
                            ((MyTag) itemTag).addValueSum(value);
                        }
                    }
                }
                changed = true;
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

    @Override //modifyRelation需要修改！！
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
                    Node node1 = nodes.get(id1);
                    Node node2 = nodes.get(id2);
                    node1.removeNeighbor(node2);
                    node2.removeNeighbor(node1);
                    maintainBlocks(id1, id2, 1);
                    maintainTriples(id1, id2, 1);
                }
                for (Person itemPerson : persons.values()) {
                    MyPerson itemPerson1 = (MyPerson) itemPerson;
                    if (!itemPerson1.haveTags()) {
                        continue;
                    }
                    HashMap<Integer, Tag> itemTags = itemPerson1.getTags();
                    for (Tag itemTag : itemTags.values()) {
                        if (itemTag.hasPerson(person1) && itemTag.hasPerson(person2)) {
                            if (newValue1 > 0) {
                                ((MyTag) itemTag).modifyValueSum(newValue1, newValue1 - value);
                            } else {
                                ((MyTag) itemTag).subValueSum(newValue1 - value);
                            }
                        }
                    }
                }
                changed = true;
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
        return tripleSum;
    }

    public int queryTagValueSum(int personId, int tagId) throws MyPersonIdNotFoundException,
            MyTagIdNotFoundException {
        if (containsPerson(personId)) {
            Person person = getPerson(personId);
            if (person.containsTag(tagId)) {
                return person.getTag(tagId).getValueSum();
            } else {
                throw new MyTagIdNotFoundException(tagId);
            }
        } else {
            throw new MyPersonIdNotFoundException(personId);
        }
    }

    public int queryTagAgeVar(int personId, int tagId) throws MyPersonIdNotFoundException,
            MyTagIdNotFoundException {
        if (containsPerson(personId)) {
            Person person = getPerson(personId);
            if (person.containsTag(tagId)) {
                return person.getTag(tagId).getAgeVar();
            } else {
                throw new MyTagIdNotFoundException(tagId);
            }
        } else {
            throw new MyPersonIdNotFoundException(personId);
        }
    }

    public int queryBestAcquaintance(int id) throws MyPersonIdNotFoundException,
            MyAcquaintanceNotFoundException { //acquaintance 中 value的最大值的id，有相同时取最小值
        if (containsPerson(id)) {
            MyPerson person = (MyPerson) getPerson(id);
            if (!person.getAcquaintance().isEmpty()) {
                return person.getBestAcquaintanceId();
            } else {
                throw new MyAcquaintanceNotFoundException(id);
            }
        } else {
            throw new MyPersonIdNotFoundException(id);
        }
    }

    public int queryCoupleSum() {
        if (changed) { //changed为真，即改过了
            maintainCoupleSum();
        }
        changed = false;
        return coupleSum;
    } //两个人相互是best acquaintance

    public int queryShortestPath(int id1, int id2) throws MyPersonIdNotFoundException,
            MyPathNotFoundException {
        boolean include1 = containsPerson(id1);
        boolean include2 = containsPerson(id2);
        if (include1 && include2) {
            if (disjointSet.find(id1) == disjointSet.find(id2)) {
                return findShortestPath(nodes.get(id1), nodes.get(id2));
            } else {
                throw new MyPathNotFoundException(id1, id2);
            }
        } else {
            if (!include1) {
                throw new MyPersonIdNotFoundException(id1);
            } else {
                throw new MyPersonIdNotFoundException(id2);
            }
        }
    }

    public void delPersonFromTag(int personId1, int personId2, int tagId)
            throws MyPersonIdNotFoundException, MyTagIdNotFoundException {
        boolean include1 = containsPerson(personId1);
        boolean include2 = containsPerson(personId2);
        if (include1 && include2) {
            Person person1 = getPerson(personId1);
            Person person2 = getPerson(personId2);
            if (person2.containsTag(tagId)) {
                Tag tag = person2.getTag(tagId);
                if (tag.hasPerson(person1)) {
                    tag.delPerson(person1);
                } else {
                    throw new MyPersonIdNotFoundException(personId1);
                }
            } else {
                throw new MyTagIdNotFoundException(tagId);
            }
        } else {
            if (!include1) {
                throw new MyPersonIdNotFoundException(personId1);
            } else {
                throw new MyPersonIdNotFoundException(personId2);
            }
        }
    }

    public void delTag(int personId, int tagId) throws MyPersonIdNotFoundException,
            MyTagIdNotFoundException {
        if (containsPerson(personId)) {
            Person person = getPerson(personId);
            if (person.containsTag(tagId)) {
                person.delTag(tagId);
            } else {
                throw new MyTagIdNotFoundException(tagId);
            }
        } else {
            throw new MyPersonIdNotFoundException(personId);
        }
    }

    public void maintainBlocks(int id1, int id2, int op) { //背景：二者联通，恐怕不能路径压缩，因为会破坏结构
        if (op == 0) {
            disjointSet.merge(id1, id2);
        } else {
            disjointSet.delete((MyPerson) getPerson(id1), (MyPerson) getPerson(id2));
        }
    }

    public void maintainCoupleSum() {
        coupleSum = 0;
        for (Person person : persons.values()) {
            int best = ((MyPerson) person).getBestAcquaintanceId();
            if (((MyPerson) person).getAcquaintance().isEmpty()) {
                continue;
            }
            if (((MyPerson) getPerson(best)).getBestAcquaintanceId() == person.getId()) {
                //System.out.println(best + " " + person.getId());
                coupleSum++;
            }
        }
        coupleSum = coupleSum / 2;
    }   //couples的维护

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

    public int findShortestPath(Node start, Node end) {
        LinkedList<Node> queue = new LinkedList<>();
        queue.addLast(start);
        while (!queue.isEmpty()) {
            Node node = queue.pollFirst();
            if (node.isSearched()) {
                continue;
            }
            if (node.getId() == end.getId()) {
                return countPath(end);
            } else {
                for (Node item : node.getNeighbors()) {
                    if (item.getPredecessor() == null && item.getId() != start.getId()) {
                        item.setPredecessor(node);
                    }
                    queue.addLast(item);
                }
            }
            node.setSearched(true);
        }
        return -1;
    }

    public int countPath(Node node) {
        int count = 0;
        Node temp = node;
        while (temp.getPredecessor() != null) {
            count++;
            temp = temp.getPredecessor();
        }
        for (Node item : nodes.values()) {
            item.setSearched(false);
            item.setPredecessor(null);
        }
        if (count == 0) { //end == start
            return 0;
        }
        return count - 1;
    }

    public Person[] getPersons() {
        return null;
    }
}
