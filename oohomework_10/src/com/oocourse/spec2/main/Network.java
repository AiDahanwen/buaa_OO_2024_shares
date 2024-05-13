package com.oocourse.spec2.main;

import com.oocourse.spec2.exceptions.*;

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

    void addTag(int personId, Tag tag) throws PersonIdNotFoundException, EqualTagIdException;

    void addPersonToTag(int personId1, int personId2, int tagId) throws PersonIdNotFoundException,
            RelationNotFoundException, TagIdNotFoundException, EqualPersonIdException;

    int queryTagValueSum(int personId, int tagId)
            throws PersonIdNotFoundException, TagIdNotFoundException;

    int queryTagAgeVar(int personId, int tagId)
            throws PersonIdNotFoundException, TagIdNotFoundException;

    void delPersonFromTag(int personId1, int personId2, int tagId)
            throws PersonIdNotFoundException, TagIdNotFoundException;

    void delTag(int personId, int tagId)
            throws PersonIdNotFoundException, TagIdNotFoundException;

    int queryBestAcquaintance(int id)
            throws PersonIdNotFoundException, AcquaintanceNotFoundException;

    int queryCoupleSum();

    int queryShortestPath(int id1, int id2)
            throws PersonIdNotFoundException, PathNotFoundException;
}
