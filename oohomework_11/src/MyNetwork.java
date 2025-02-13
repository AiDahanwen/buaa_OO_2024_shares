import com.oocourse.spec3.main.Network;
import com.oocourse.spec3.main.Tag;
import com.oocourse.spec3.main.Person;
import com.oocourse.spec3.main.Message;
import com.oocourse.spec3.main.EmojiMessage;
import com.oocourse.spec3.main.RedEnvelopeMessage;

import java.util.HashMap;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Iterator;

public class MyNetwork implements Network {
    private HashMap<Integer, Person> persons; //找人的时候用
    private HashMap<Integer, Node> nodes;
    private HashMap<Integer, Message> messages;
    private HashSet<Integer> emojiIdList;
    private HashMap<Integer, Integer> emojiHeatList; //似乎不关注消息本身，而更加关注的是id
    private HashMap<Integer, EmojiMessage> emojiMessages;
    private int tripleSum;
    private int coupleSum;
    private DisjointSet disjointSet;
    private boolean changed;
    private Tools myTool;

    public MyNetwork() {
        persons = new HashMap<>();
        disjointSet = new DisjointSet();
        tripleSum = 0;
        coupleSum = 0;
        nodes = new HashMap<>();
        messages = new HashMap<>();
        emojiHeatList = new HashMap<>();
        emojiIdList = new HashSet<>();
        changed = false;
        myTool = new Tools(this);
        emojiMessages = new HashMap<>();
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
        myTool.addPersonToTag(personId1, personId2, tagId);
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
                tripleSum = myTool.maintainTriples(id1, id2, 0, tripleSum); //此时已有一条边
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
                    Node node1 = nodes.get(id1);
                    Node node2 = nodes.get(id2);
                    node1.removeNeighbor(node2);
                    node2.removeNeighbor(node1);
                    maintainBlocks(id1, id2, 1);
                    tripleSum = myTool.maintainTriples(id1, id2, 1, tripleSum);
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
        return myTool.queryValue(id1, id2);
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
        return myTool.queryTagValueSum(personId, tagId);
    }

    public int queryTagAgeVar(int personId, int tagId) throws MyPersonIdNotFoundException,
            MyTagIdNotFoundException {
        return myTool.queryTagAgeVar(personId, tagId);
    }

    public int queryBestAcquaintance(int id) throws MyPersonIdNotFoundException,
            MyAcquaintanceNotFoundException {
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
        if (changed) {
            maintainCoupleSum();
        }
        changed = false;
        return coupleSum;
    }

    public int queryShortestPath(int id1, int id2) throws MyPersonIdNotFoundException,
            MyPathNotFoundException {
        boolean include1 = containsPerson(id1);
        boolean include2 = containsPerson(id2);
        if (include1 && include2) {
            if (disjointSet.find(id1) == disjointSet.find(id2)) {
                return myTool.findShortestPath(nodes.get(id1), nodes.get(id2));
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
        myTool.delPersonFromTag(personId1, personId2, tagId);
    }

    public void delTag(int personId, int tagId) throws MyPersonIdNotFoundException,
            MyTagIdNotFoundException {
        myTool.delTag(personId, tagId);
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
                coupleSum++;
            }
        }
        coupleSum = coupleSum / 2;
    }

    public boolean containsMessage(int id) {
        return messages.containsKey(id);
    }

    public void addMessage(Message message) throws MyEqualMessageIdException,
            MyEmojiIdNotFoundException, MyEqualPersonIdException {
        int id = message.getId();
        if (!containsMessage(id)) {
            MyPerson person1 = (MyPerson) message.getPerson1();
            MyPerson person2 = (MyPerson) message.getPerson2();
            boolean isPersonEqual = person1.equals(person2);
            if (message instanceof EmojiMessage) {
                int emoId = ((EmojiMessage) message).getEmojiId();
                if (!containsEmojiId(emoId)) {
                    throw new MyEmojiIdNotFoundException(emoId);
                }
                emojiMessages.put(id, (EmojiMessage) message);
            }
            if (message.getType() == 0) {
                if (isPersonEqual) {
                    throw new MyEqualPersonIdException(person1.getId());
                }
            }
            messages.put(id, message);
        } else {
            throw new MyEqualMessageIdException(message.getId());
        }
    }

    public Message getMessage(int id) {
        if (containsMessage(id)) {
            return messages.get(id);
        } else {
            return null;
        }
    }

    public void sendMessage(int id) throws MyRelationNotFoundException,
            MyMessageIdNotFoundException, MyTagIdNotFoundException {
        if (containsMessage(id)) {
            Message m = getMessage(id);
            MyPerson person1 = (MyPerson) m.getPerson1();
            MyPerson person2 = (MyPerson) m.getPerson2();
            int socialValue = m.getSocialValue();
            if (m.getType() == 0) {
                if (person1.isLinked(person2)) {
                    if (!person1.equals(person2)) {
                        messages.remove(m.getId(), m);
                        person1.addSocialValue(socialValue);
                        person2.addSocialValue(socialValue);
                        if (m instanceof RedEnvelopeMessage) {
                            int money = ((RedEnvelopeMessage) m).getMoney();
                            person1.subMoney(money);
                            person2.addMoney(money);
                        } else if (m instanceof EmojiMessage) {
                            int emoId = ((EmojiMessage) m).getEmojiId();
                            emojiIdList.add(emoId); //改成hashset
                            addHeat(emoId);
                            emojiMessages.remove(m.getId());
                        }
                        person2.receiveMessage(m);
                    }
                } else {
                    throw new MyRelationNotFoundException(person1.getId(), person2.getId());
                }
            } else {
                if (person1.containsTag(m.getTag().getId())) {
                    MyTag myTag = (MyTag) m.getTag();
                    myTag.addSocialValue(socialValue);
                    person1.addSocialValue(socialValue);
                    messages.remove(m.getId(), m);
                    if (m instanceof RedEnvelopeMessage) {
                        int size = myTag.getSize();
                        if (size > 0) {
                            int money = ((RedEnvelopeMessage) m).getMoney();
                            person1.subTagMoney(money, size);
                            myTag.addMoney(money);
                        }
                    } else if (m instanceof EmojiMessage) {
                        int emoId = ((EmojiMessage) m).getEmojiId();
                        emojiIdList.add(emoId);
                        addHeat(emoId);
                        emojiMessages.remove(m.getId());
                    }
                } else {
                    throw new MyTagIdNotFoundException(m.getTag().getId());
                }
            }
        } else {
            throw new MyMessageIdNotFoundException(id);
        }
    }

    public int querySocialValue(int id) throws MyPersonIdNotFoundException {
        if (containsPerson(id)) {
            return getPerson(id).getSocialValue();
        } else {
            throw new MyPersonIdNotFoundException(id);
        }
    }

    public List<Message> queryReceivedMessages(int id) throws MyPersonIdNotFoundException {
        if (containsPerson(id)) {
            return getPerson(id).getReceivedMessages();
        } else {
            throw new MyPersonIdNotFoundException(id);
        }
    }

    @Override
    public boolean containsEmojiId(int id) {
        return emojiIdList.contains(id);
    }

    public void storeEmojiId(int id) throws MyEqualEmojiIdException {
        if (!containsEmojiId(id)) {
            emojiIdList.add(id);
            emojiHeatList.put(id, 0);
        } else {
            throw new MyEqualEmojiIdException(id);
        }
    }

    public int queryMoney(int id) throws MyPersonIdNotFoundException {
        if (containsPerson(id)) {
            return getPerson(id).getMoney();
        } else {
            throw new MyPersonIdNotFoundException(id);
        }
    }

    public int queryPopularity(int id) throws MyEmojiIdNotFoundException {
        if (containsEmojiId(id)) {
            return emojiHeatList.get(id);
        } else {
            throw new MyEmojiIdNotFoundException(id); //此处传的是emojiId
        }
    }

    public void clearNotices(int personId) throws MyPersonIdNotFoundException {
        if (containsPerson(personId)) {
            ((MyPerson) getPerson(personId)).clearNotices();
        } else {
            throw new MyPersonIdNotFoundException(personId);
        }
    }

    public int deleteColdEmoji(int limit) {
        ArrayList<Integer> deleteEmojis = new ArrayList<>();
        Iterator<Integer> iterator = emojiHeatList.keySet().iterator();
        while (iterator.hasNext()) {
            Integer emoId = iterator.next();
            if (emojiHeatList.get(emoId) < limit) {
                iterator.remove();
                emojiIdList.remove(emoId);
                deleteEmojis.add(emoId);
            }
        }
        removeEmojis(deleteEmojis);
        return emojiIdList.size();
    }

    public Person[] getPersons() {
        return null;
    }

    public HashMap<Integer, Person> getMyPersons() {
        return persons;
    }

    public HashMap<Integer, Node> getNodes() {
        return nodes;
    }

    public void addHeat(int emoId) {
        if (emojiHeatList.containsKey(emoId)) {
            emojiHeatList.put(emoId, emojiHeatList.get(emoId) + 1);
        } else {
            emojiHeatList.put(emoId, 1);
        }
    }

    public Message[] getMessages() {
        return messages.values().toArray(new Message[0]);
    }

    public int[] getEmojiIdList() {
        return null;
    }

    public int[] getEmojiHeatList() {
        return null;
    }

    public void removeEmojis(ArrayList<Integer> deleteEmojis) {
        Iterator<EmojiMessage> iterator = emojiMessages.values().iterator();
        while (iterator.hasNext()) {
            EmojiMessage m = iterator.next();
            int emoId = m.getEmojiId();
            if (deleteEmojis.contains(emoId)) {
                messages.remove(m.getId());
                iterator.remove();
            }
        }
    }
}