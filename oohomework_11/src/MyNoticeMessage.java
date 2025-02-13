import com.oocourse.spec3.main.NoticeMessage;
import com.oocourse.spec3.main.Person;
import com.oocourse.spec3.main.Tag;

public class MyNoticeMessage implements NoticeMessage {
    private int id;
    private String string;
    private int type;
    private MyPerson person1;
    private MyPerson person2;
    private MyTag tag;

    public MyNoticeMessage(int messageId, String noticeString,
                           Person messagePerson1, Person messagePerson2) {
        type = 0;
        tag = null;
        id = messageId;
        person1 = (MyPerson) messagePerson1;
        person2 = (MyPerson) messagePerson2;
        string = noticeString;
    }

    public MyNoticeMessage(int messageId, String noticeString,
                           Person messagePerson1, Tag messageTag) {
        type = 1;
        person2 = null;
        id = messageId;
        person1 = (MyPerson) messagePerson1;
        tag = (MyTag) messageTag;
        string = noticeString;
    }

    public String getString() {
        return string;
    }

    @Override
    public int getType() {
        return type;
    }

    @Override
    public int getId() {
        return id;
    }

    @Override
    public int getSocialValue() {
        return string.length();
    }

    @Override
    public Person getPerson1() {
        return person1;
    }

    @Override
    public Person getPerson2() {
        return person2;
    }

    @Override
    public Tag getTag() {
        return tag;
    }
}
