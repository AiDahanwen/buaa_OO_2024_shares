package spec3.main;

public interface Message {
    int getType();

    int getId();

    int getSocialValue();

    Person getPerson1();

    Person getPerson2();

    Tag getTag();

    boolean equals(Object obj);
}
