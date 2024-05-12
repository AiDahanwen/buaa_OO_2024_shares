package spec1.main;

public interface Person {
    int getId();

    String getName();

    int getAge();

    boolean equals(Object obj);

    boolean isLinked(Person person);

    int queryValue(Person person);
}
