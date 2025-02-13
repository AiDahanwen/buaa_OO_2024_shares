import com.oocourse.spec3.main.Message;
import com.oocourse.spec3.main.Person;
import com.oocourse.spec3.main.Tag;

public class MyMessage implements Message {
    private int id;
    private int socialValue;
    private int type;
    private MyPerson person1;
    private MyPerson person2;
    private MyTag tag;

    public MyMessage(int messageId, int messageSocialValue,
                     Person messagePerson1, Person messagePerson2) {
        type = 0;
        tag = null;
        id = messageId;
        socialValue = messageSocialValue;
        person1 = (MyPerson) messagePerson1;
        person2 = (MyPerson) messagePerson2;
    }

    public MyMessage(int messageId, int messageSocialValue, Person messagePerson1, Tag messageTag) {
        type = 1;
        person2 = null;
        id = messageId;
        socialValue = messageSocialValue;
        person1 = (MyPerson) messagePerson1;
        tag = (MyTag) messageTag;
    }

    public int getType() {
        return type;
    }

    public int getId() {
        return id;
    }

    public int getSocialValue() {
        return socialValue;
    }

    public Person getPerson1() {
        return person1;
    }

    public Person getPerson2() {
        return person2;
    }

    public Tag getTag() {
        return tag;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj != null && obj instanceof Message) {
            return ((Message) obj).getId() == id;
        }
        return false;
    }
}
