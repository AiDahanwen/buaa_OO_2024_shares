import com.oocourse.spec3.exceptions.*;
import com.oocourse.spec3.main.*;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.*;

import java.util.Arrays;
import java.util.Collection;

import static org.junit.Assert.*;

@RunWith(Parameterized.class)
public class MyTest {
    private MyNetwork myNetwork1;

    public MyTest(MyNetwork myNetwork1) {
        this.myNetwork1 = myNetwork1;
    }

    @Parameters
    public static Collection prepareData() throws EqualPersonIdException {
        Object[][] object = new Object[1][];
        MyNetwork myNetwork1 = generateOneNetwork();
        object[0] = new Object[]{myNetwork1};
        return Arrays.asList(object);
    }

    @Test
    public void testDeleteColdEmoji() throws PersonIdNotFoundException, EqualRelationException, EqualPersonIdException,
            RelationNotFoundException, EqualEmojiIdException, EqualMessageIdException, EmojiIdNotFoundException,
            MessageIdNotFoundException, TagIdNotFoundException {
        myNetwork1.storeEmojiId(1001);
        myNetwork1.storeEmojiId(1002);
        myNetwork1.storeEmojiId(1003);
        myNetwork1.storeEmojiId(1004);
        myNetwork1.storeEmojiId(1005);
        myNetwork1.storeEmojiId(1006);
        // storeEmojiId

        myNetwork1.addRelation(1, 2, 1);
        myNetwork1.addRelation(3, 4, 2);
        myNetwork1.addRelation(4, 5, 3);
        myNetwork1.addRelation(1, 5, 4);
        myNetwork1.addRelation(3, 5, 5);
        //addRelation

        myNetwork1.addMessage(new MyRedEnvelopeMessage(1, 100, myNetwork1.getPerson(1), myNetwork1.getPerson(2)));
        myNetwork1.addMessage(new MyRedEnvelopeMessage(2, 100, myNetwork1.getPerson(1), myNetwork1.getPerson(2)));
        myNetwork1.addMessage(new MyNoticeMessage(3, "A", myNetwork1.getPerson(3), myNetwork1.getPerson(4)));
        myNetwork1.addMessage(new MyNoticeMessage(4, "B", myNetwork1.getPerson(3), myNetwork1.getPerson(4)));
        myNetwork1.addMessage(new MyEmojiMessage(5, 1001, myNetwork1.getPerson(4), myNetwork1.getPerson(5)));
        myNetwork1.addMessage(new MyEmojiMessage(6, 1002, myNetwork1.getPerson(5), myNetwork1.getPerson(1)));
        myNetwork1.addMessage(new MyEmojiMessage(7, 1003, myNetwork1.getPerson(3), myNetwork1.getPerson(5)));
        //addMessage

        myNetwork1.sendMessage(5);
        myNetwork1.sendMessage(6); //1001 -> 1, 1002 -> 1

        myNetwork1.addMessage(new MyEmojiMessage(8, 1001, myNetwork1.getPerson(1), myNetwork1.getPerson(2)));
        myNetwork1.addMessage(new MyEmojiMessage(9, 1001, myNetwork1.getPerson(3), myNetwork1.getPerson(4)));

        myNetwork1.sendMessage(8);
        myNetwork1.sendMessage(9); //此时1001为3,边界
        myNetwork1.sendMessage(7); //1003 -> 1

        myNetwork1.addMessage(new MyEmojiMessage(10, 1003, myNetwork1.getPerson(4), myNetwork1.getPerson(5)));
        myNetwork1.addMessage(new MyEmojiMessage(11, 1003, myNetwork1.getPerson(4), myNetwork1.getPerson(5)));
        myNetwork1.addMessage(new MyEmojiMessage(12, 1003, myNetwork1.getPerson(4), myNetwork1.getPerson(5)));

        myNetwork1.sendMessage(10);
        myNetwork1.sendMessage(11);
        myNetwork1.sendMessage(12); //1003 -> 4, 1001 -> 3 ,1002-> 1
        //sendMessage

        myNetwork1.addMessage(new MyEmojiMessage(13, 1003, myNetwork1.getPerson(1), myNetwork1.getPerson(2)));
        myNetwork1.addMessage(new MyEmojiMessage(14, 1001, myNetwork1.getPerson(1), myNetwork1.getPerson(2)));
        myNetwork1.addMessage(new MyEmojiMessage(15, 1002, myNetwork1.getPerson(3), myNetwork1.getPerson(4)));
        myNetwork1.addMessage(new MyEmojiMessage(16, 1004, myNetwork1.getPerson(3), myNetwork1.getPerson(4)));

        //int[] oldEmojiIdList = {1001,1002,1003,1004,1005,1006};
        int[] oldEmojiIdList = myNetwork1.getEmojiIdList();
        int[] oldEmojiHeatList = myNetwork1.getEmojiHeatList();
        //int[] oldEmojiHeatList = {3,1,4,0,0,0};
        Message[] oldMessages = myNetwork1.getMessages();
        //record the old;

        int result = myNetwork1.deleteColdEmoji(3);
        //deleteColdEmoji

        //int[] newEmojiIdList = {1001,1003};
        int[] newEmojiIdList = myNetwork1.getEmojiIdList();
        //int[] newEmojiHeatList = {3,4};
        int[] newEmojiHeatList = myNetwork1.getEmojiHeatList();
        Message[] newMessages = myNetwork1.getMessages();
        //prepare the data

        //check 1
        //检验1001和1003仍然在
        boolean flag1 = false;
        boolean flag2 = false;
        for (int i = 0; i < newEmojiIdList.length; i++) {
            if (newEmojiIdList[i] == 1001) {
                flag1 = true;
            } else if (newEmojiIdList[i] == 1003) {
                flag2 = true;
            }
        }
        assertTrue(flag1 && flag2);

        //check 2
        for (int i = 0; i < newEmojiIdList.length; i++) {
            boolean flag = false;
            for (int j = 0; j < oldEmojiIdList.length; j++) {
                if (oldEmojiIdList[j] == newEmojiIdList[i] && oldEmojiHeatList[j] == newEmojiHeatList[i]) {
                    flag = true;
                    break;
                }
            }
            assertTrue(flag);
        }
        //check 3
        assertEquals(2, newEmojiIdList.length);
        //check 4
        assertEquals(newEmojiHeatList.length, newEmojiIdList.length);
        //check 5 & 6
        for (int i = 0; i < oldMessages.length; i++) {
            Message m = null;
            if (oldMessages[i] instanceof EmojiMessage) {
                if (containsEmoId(((EmojiMessage) oldMessages[i]).getEmojiId(), newEmojiIdList)) {
                    m = oldMessages[i];
                }
            } else {
                m = oldMessages[i];
            }
            if (m == null) {
                continue;
            }
            boolean mark = false;
            for (int j = 0; j < newMessages.length; j++) {
                if (newMessages[j].getId() == m.getId()) {
                    mark = strictEqualMessage(m, newMessages[j]);
                    break;
                }
            }
            assertTrue(mark);
        }
        //check 7
        assertEquals(6, newMessages.length);
        //check8
        assertEquals(2, result);
    }

    public static MyNetwork generateOneNetwork() throws EqualPersonIdException {
        MyNetwork myNetwork1 = new MyNetwork();
        myNetwork1.addPerson(new MyPerson(1, "a", 1));
        myNetwork1.addPerson(new MyPerson(2, "b", 1));
        myNetwork1.addPerson(new MyPerson(3, "c", 1));
        myNetwork1.addPerson(new MyPerson(4, "d", 1));
        myNetwork1.addPerson(new MyPerson(5, "e", 1));
        myNetwork1.addPerson(new MyPerson(6, "f", 1));
        return myNetwork1;
    }

    public boolean containsEmoId(int emoId, int[] newEmojiIdList) {
        for (int i = 0; i < newEmojiIdList.length; i++) {
            if (newEmojiIdList[i] == emoId) {
                return true;
            }
        }
        return false;
    }

    public boolean strictEqualMessage(Message m1, Message m2) {
        boolean flag1 = m1.getSocialValue() == m2.getSocialValue();
        boolean flag2 = m1.getType() == m2.getType();
        if (m1 instanceof RedEnvelopeMessage && m2 instanceof RedEnvelopeMessage) {
            return flag1 && flag2 &&
                    ((RedEnvelopeMessage) m1).getMoney() == ((RedEnvelopeMessage) m2).getMoney();
        } else if (m1 instanceof NoticeMessage && m2 instanceof NoticeMessage) {
            return flag1 && flag2 &&
                    ((NoticeMessage) m1).getString().equals(((NoticeMessage) m2).getString());
        } else if (m1 instanceof EmojiMessage && m2 instanceof EmojiMessage) {
            return flag1 && flag2 &&
                    ((EmojiMessage) m1).getEmojiId() == ((EmojiMessage) m2).getEmojiId();
        }
        return false;
    }
}