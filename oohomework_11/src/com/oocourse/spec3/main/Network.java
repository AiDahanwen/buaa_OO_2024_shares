package com.oocourse.spec3.main;

import com.oocourse.spec3.exceptions.*;

import java.util.List;

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

    boolean containsMessage(int id);

    void addMessage(Message message) throws
            EqualMessageIdException, EmojiIdNotFoundException, EqualPersonIdException;

    Message getMessage(int id);

    void sendMessage(int id) throws
            RelationNotFoundException, MessageIdNotFoundException, TagIdNotFoundException;

    int querySocialValue(int id) throws PersonIdNotFoundException;

    List<Message> queryReceivedMessages(int id) throws PersonIdNotFoundException;

    boolean containsEmojiId(int id);

    void storeEmojiId(int id) throws EqualEmojiIdException;

    int queryMoney(int id) throws PersonIdNotFoundException;

    int queryPopularity(int id) throws EmojiIdNotFoundException;

    int deleteColdEmoji(int limit);

    void clearNotices(int personId) throws PersonIdNotFoundException;

    int queryBestAcquaintance(int id)
            throws PersonIdNotFoundException, AcquaintanceNotFoundException;

    int queryCoupleSum();

    int queryShortestPath(int id1, int id2)
            throws PersonIdNotFoundException, PathNotFoundException;
}
