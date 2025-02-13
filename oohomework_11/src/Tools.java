import com.oocourse.spec3.main.Person;
import com.oocourse.spec3.main.Tag;

import java.util.HashMap;
import java.util.LinkedList;

public class Tools {
    private MyNetwork myNetwork;

    public Tools(MyNetwork network) {
        myNetwork = network;
    }

    public void addPersonToTag(int personId1, int personId2, int tagId) throws
            MyPersonIdNotFoundException, MyRelationNotFoundException,
            MyTagIdNotFoundException, MyEqualPersonIdException {
        boolean include1 = myNetwork.containsPerson(personId1);
        boolean include2 = myNetwork.containsPerson(personId2);
        if (include1 && include2) {
            Person person1 = myNetwork.getPerson(personId1);
            Person person2 = myNetwork.getPerson(personId2);
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

    public int queryValue(int id1, int id2) throws MyPersonIdNotFoundException,
            MyRelationNotFoundException {
        boolean include1 = myNetwork.containsPerson(id1);
        boolean include2 = myNetwork.containsPerson(id2);
        boolean linked;
        HashMap<Integer, Person> persons = myNetwork.getMyPersons();
        if (include1 && include2) {
            linked = myNetwork.getPerson(id1).isLinked(persons.get(id2));
            if (linked) {
                return myNetwork.getPerson(id1).queryValue(persons.get(id2));
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

    public int queryTagValueSum(int personId, int tagId) throws MyPersonIdNotFoundException,
            MyTagIdNotFoundException {
        if (myNetwork.containsPerson(personId)) {
            Person person = myNetwork.getPerson(personId);
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
        if (myNetwork.containsPerson(personId)) {
            Person person = myNetwork.getPerson(personId);
            if (person.containsTag(tagId)) {
                return person.getTag(tagId).getAgeVar();
            } else {
                throw new MyTagIdNotFoundException(tagId);
            }
        } else {
            throw new MyPersonIdNotFoundException(personId);
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
        HashMap<Integer, Node> nodes = myNetwork.getNodes();
        for (Node item : nodes.values()) {
            item.setSearched(false);
            item.setPredecessor(null);
        }
        if (count == 0) { //end == start
            return 0;
        }
        return count - 1;
    }

    public int maintainTriples(int id1, int id2, int op, int tripleSum) {  //背景：二者联通
        MyPerson person1 = (MyPerson) myNetwork.getPerson(id1);
        MyPerson person2 = (MyPerson) myNetwork.getPerson(id2);
        HashMap<Integer, Person> acquaintance1 = person1.getAcquaintance();
        HashMap<Integer, Person> acquaintance2 = person2.getAcquaintance();
        int temp = tripleSum;
        if (op == 0) {
            if (acquaintance1.size() < acquaintance2.size()) {
                for (Person item : acquaintance1.values()) {
                    if (person2.isLinked(item) && (!item.equals(person1)) &&
                            (!item.equals(person2))) {
                        temp++;
                    }
                }
            } else {
                for (Person item : acquaintance2.values()) {
                    if (person1.isLinked(item) && (!item.equals(person1))
                            && (!item.equals(person2))) {
                        temp++;
                    }
                }
            }
        } else {
            if (acquaintance1.size() < acquaintance2.size()) {
                for (Person item : acquaintance1.values()) {
                    if (person2.isLinked(item) && (!item.equals(person1)) &&
                            (!item.equals(person2))) {
                        temp--;
                    }
                }
            } else {
                for (Person item : acquaintance2.values()) {
                    if (person1.isLinked(item) && (!item.equals(person1))
                            && (!item.equals(person2))) {
                        temp--;
                    }
                }
            }
        }
        return temp;
    }

    public void delPersonFromTag(int personId1, int personId2, int tagId)
            throws MyPersonIdNotFoundException, MyTagIdNotFoundException {
        boolean include1 = myNetwork.containsPerson(personId1);
        boolean include2 = myNetwork.containsPerson(personId2);
        if (include1 && include2) {
            Person person1 = myNetwork.getPerson(personId1);
            Person person2 = myNetwork.getPerson(personId2);
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
        if (myNetwork.containsPerson(personId)) {
            Person person = myNetwork.getPerson(personId);
            if (person.containsTag(tagId)) {
                person.delTag(tagId);
            } else {
                throw new MyTagIdNotFoundException(tagId);
            }
        } else {
            throw new MyPersonIdNotFoundException(personId);
        }
    }
}
