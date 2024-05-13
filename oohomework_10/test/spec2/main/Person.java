package spec2.main;

public interface Person {
    int getId();

    String getName();

    int getAge();

    boolean containsTag(int id);

    Tag getTag(int id);

    void addTag(Tag tag);

    void delTag(int id);

    boolean equals(Object obj);

    boolean isLinked(Person person);

    int queryValue(Person person);
}
