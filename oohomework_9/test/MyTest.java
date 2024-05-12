import com.oocourse.spec1.exceptions.EqualPersonIdException;
import com.oocourse.spec1.exceptions.EqualRelationException;
import com.oocourse.spec1.exceptions.PersonIdNotFoundException;
import com.oocourse.spec1.exceptions.RelationNotFoundException;
import com.oocourse.spec1.main.Person;
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
    public void testQueryTripleSum() throws PersonIdNotFoundException, EqualRelationException, EqualPersonIdException, RelationNotFoundException {
        myNetwork1.addRelation(1, 2, 1);
        myNetwork1.addRelation(1, 3, 1);
        Person[] oldPersons = myNetwork1.getPersons();
        assertEquals(0, myNetwork1.queryTripleSum());
        Person[] newPersons = myNetwork1.getPersons();
        assertEquals(oldPersons.length, newPersons.length);
        for (int i = 0; i < oldPersons.length; i++) {
            assertTrue(((MyPerson) oldPersons[i]).strictEquals(newPersons[i]));
        }

        myNetwork1.addRelation(2, 3, 1);
        assertEquals(1, myNetwork1.queryTripleSum());
        myNetwork1.addRelation(2, 4, 1);
        myNetwork1.addRelation(4, 5, 1);
        assertEquals(1, myNetwork1.queryTripleSum());
        myNetwork1.addRelation(2, 5, 1);
        myNetwork1.addRelation(1, 5, 1);
        assertEquals(3, myNetwork1.queryTripleSum());
        myNetwork1.addRelation(3, 4, 1);
        myNetwork1.addRelation(3, 5, 1);
        assertEquals(7, myNetwork1.queryTripleSum());

        myNetwork1.modifyRelation(1, 3, -10);
        assertEquals(5, myNetwork1.queryTripleSum());
        myNetwork1.modifyRelation(2, 4, 1);
        assertEquals(5, myNetwork1.queryTripleSum());
        myNetwork1.modifyRelation(3, 4, -10);
        assertEquals(3, myNetwork1.queryTripleSum());

        myNetwork1.modifyRelation(1,2,-10);
        myNetwork1.modifyRelation(1,5,-10);
        myNetwork1.modifyRelation(2,3,-10);
        myNetwork1.modifyRelation(2,4,-10);
        myNetwork1.modifyRelation(2,5,-10);
        myNetwork1.modifyRelation(3,5,-10);
        myNetwork1.modifyRelation(4,5,-10);
        assertEquals(0,myNetwork1.queryTripleSum());

        myNetwork1.addRelation(1,2,1);
        myNetwork1.addRelation(2,4,1);
        myNetwork1.addRelation(1,3,1);
        myNetwork1.addRelation(3,4,1);
        assertEquals(0,myNetwork1.queryTripleSum());

        myNetwork1.addRelation(2,3,1);
        assertEquals(2,myNetwork1.queryTripleSum());
        myNetwork1.modifyRelation(2,3,-10);
        assertEquals(0,myNetwork1.queryTripleSum());

        myNetwork1.addRelation(2,3,1);
        myNetwork1.addRelation(4,5,1);
        myNetwork1.addRelation(2,5,1);
        assertEquals(3,myNetwork1.queryTripleSum());
        myNetwork1.modifyRelation(2,4,-10);
        assertEquals(1,myNetwork1.queryTripleSum());

        myNetwork1.modifyRelation(2,3,-10);
        assertEquals(0,myNetwork1.queryTripleSum());
    }

    public static MyNetwork generateOneNetwork() throws EqualPersonIdException {
        MyNetwork myNetwork1 = new MyNetwork();
        myNetwork1.addPerson(new MyPerson(1, "a", 1));
        myNetwork1.addPerson(new MyPerson(2, "b", 1));
        myNetwork1.addPerson(new MyPerson(3, "c", 1));
        myNetwork1.addPerson(new MyPerson(4, "d", 1));
        myNetwork1.addPerson(new MyPerson(5, "e", 1));
        return myNetwork1;
    }
}