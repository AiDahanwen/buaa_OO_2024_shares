package com.oocourse.spec3.main;

public interface Tag {
    int getId();

    boolean equals(Object obj);

    void addPerson(Person person);

    boolean hasPerson(Person person);

    int getValueSum();

    int getAgeMean();

    int getAgeVar();

    void delPerson(Person person);

    int getSize();
}
