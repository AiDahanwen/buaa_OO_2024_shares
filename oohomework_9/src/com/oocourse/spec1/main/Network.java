package com.oocourse.spec1.main;
import com.oocourse.spec1.exceptions.*;

public interface Network {
    boolean containsPerson(int id);
    Person getPerson(int id);
    void addPerson(Person person) throws EqualPersonIdException;
    void addRelation(int id1, int id2, int value) throws
            PersonIdNotFoundException, EqualRelationException;
    void modifyRelation(int id1, int id2, int value) throws PersonIdNotFoundException,
            EqualPersonIdException, RelationNotFoundException;
    int queryValue(int id1, int id2) throws
            PersonIdNotFoundException, RelationNotFoundException;

    boolean isCircle(int id1, int id2) throws PersonIdNotFoundException;
    int queryBlockSum();
    int queryTripleSum();
}
