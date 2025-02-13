import com.oocourse.spec3.main.Person;
import com.oocourse.spec3.main.RedEnvelopeMessage;
import com.oocourse.spec3.main.Tag;

public class MyRedEnvelopeMessage implements RedEnvelopeMessage {
    private int id;
    private int money;
    private int type;
    private MyPerson person1;
    private MyPerson person2;
    private MyTag tag;

    public MyRedEnvelopeMessage(int messageId, int luckyMoney,
                                Person messagePerson1, Person messagePerson2) {
        type = 0;
        tag = null;
        id = messageId;
        money = luckyMoney;
        person1 = (MyPerson) messagePerson1;
        person2 = (MyPerson) messagePerson2;
    }

    public MyRedEnvelopeMessage(int messageId, int luckyMoney,
                                Person messagePerson1, Tag messageTag) {
        type = 1;
        person2 = null;
        id = messageId;
        money = luckyMoney;
        person1 = (MyPerson) messagePerson1;
        tag = (MyTag) messageTag;
    }

    public int getMoney() {
        return money;
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
        return money * 5;
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
