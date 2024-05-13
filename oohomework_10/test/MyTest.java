import com.oocourse.spec2.exceptions.EqualPersonIdException;
import com.oocourse.spec2.exceptions.EqualRelationException;
import com.oocourse.spec2.exceptions.PersonIdNotFoundException;
import com.oocourse.spec2.exceptions.RelationNotFoundException;
import com.oocourse.spec2.main.Person;
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
        //在prepareData中构造你需要的网络
        //先捏两组试试水
        Object[][] object = new Object[1][];
        MyNetwork myNetwork1 = generateOneNetwork();
        object[0] = new Object[]{myNetwork1};
        return Arrays.asList(object);
    }

    @Test
    public void testQueryCoupleSum() throws PersonIdNotFoundException, EqualRelationException, EqualPersonIdException,
            RelationNotFoundException {
        myNetwork1.addRelation(1, 2, 100);
        myNetwork1.addRelation(3, 4, 100);
        myNetwork1.addRelation(5, 6, 100);
        Person[] oldPersons = myNetwork1.getPersons();
        assertEquals(3, myNetwork1.queryCoupleSum());
        Person[] newPersons = myNetwork1.getPersons();
        assertEquals(oldPersons.length, newPersons.length);
        for (int i = 0; i < oldPersons.length; i++) {
            assertTrue(((MyPerson) oldPersons[i]).strictEquals(newPersons[i]));
        } //pure检测


        myNetwork1.addRelation(2, 3, 200);
        assertEquals(2, myNetwork1.queryCoupleSum()); // >bestValue
        myNetwork1.addRelation(3, 6, 300);
        assertEquals(1, myNetwork1.queryCoupleSum()); // >bestValue
        myNetwork1.addPerson(new MyPerson(7, "w", 1));
        myNetwork1.addPerson(new MyPerson(8, "t", 1));
        myNetwork1.addRelation(1, 7, 1);
        myNetwork1.addRelation(7, 8, 50);
        assertEquals(2, myNetwork1.queryCoupleSum());
        myNetwork1.addRelation(1, 8, 1);
        myNetwork1.modifyRelation(1, 8, 80); // mr -> >bestValue
        assertEquals(1, myNetwork1.queryCoupleSum());

        myNetwork1.modifyRelation(2, 3, -100);
        assertEquals(2, myNetwork1.queryCoupleSum()); // mr  -> =bestValue id < bestId
        myNetwork1.addRelation(2, 4, 100);
        assertEquals(2, myNetwork1.queryCoupleSum()); // ar -> =bestValue id >bestId
        myNetwork1.addPerson(new MyPerson(0, "o", 1));
        myNetwork1.addRelation(2, 0, 100); //ar -> = bestValue id < bestId
        assertEquals(2, myNetwork1.queryCoupleSum());
        myNetwork1.addRelation(2, 6, 100);
        assertEquals(2, myNetwork1.queryCoupleSum()); //id > bestid

        myNetwork1.modifyRelation(1, 2, -500);
        myNetwork1.modifyRelation(1, 8, -500);
        myNetwork1.modifyRelation(1, 7, -500);
        myNetwork1.modifyRelation(7, 8, -500);
        myNetwork1.modifyRelation(2, 0, -500);
        myNetwork1.modifyRelation(2, 3, -500);
        myNetwork1.modifyRelation(2, 4, -500);
        myNetwork1.modifyRelation(2, 6, -500);
        myNetwork1.modifyRelation(3, 4, -500);
        myNetwork1.modifyRelation(3, 6, -500);
        myNetwork1.modifyRelation(6, 5, -500);
        assertEquals(0, myNetwork1.queryCoupleSum()); //no relations

        myNetwork1.addRelation(1, 2, 100);
        myNetwork1.addRelation(2, 3, 50);
        myNetwork1.addRelation(3, 4, 100);
        assertEquals(2, myNetwork1.queryCoupleSum()); // ar -> <bestId
        myNetwork1.modifyRelation(2, 3, 20);
        assertEquals(2, myNetwork1.queryCoupleSum());

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
}