import com.oocourse.spec3.main.EmojiMessage;
import com.oocourse.spec3.main.Person;
import com.oocourse.spec3.main.Tag;

public class MyEmojiMessage implements EmojiMessage {
    private int id; //message的id
    private int emojiId; //表情编号
    private int type;
    private MyPerson person1;
    private MyPerson person2;
    private MyTag tag;

    public MyEmojiMessage(int messageId, int emojiNumber,
                          Person messagePerson1, Person messagePerson2) {
        type = 0;
        tag = null;
        id = messageId;
        person1 = (MyPerson) messagePerson1;
        person2 = (MyPerson) messagePerson2;
        emojiId = emojiNumber;
    }

    public MyEmojiMessage(int messageId, int emojiNumber, Person messagePerson1, Tag messageTag) {
        type = 1;
        person2 = null;
        id = messageId;
        person1 = (MyPerson) messagePerson1;
        tag = (MyTag) messageTag;
        emojiId = emojiNumber;
    }

    public int getEmojiId() {
        return emojiId;
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
        return emojiId;
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
