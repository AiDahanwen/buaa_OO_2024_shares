public class Triples {
    private MyPerson person1;
    private MyPerson person2;
    private MyPerson person3;

    public Triples(MyPerson person1, MyPerson person2, MyPerson person3) {
        this.person1 = person1;
        this.person2 = person2;
        this.person3 = person3;
    }

    public boolean haveRelation(int id1, int id2) {
        int id11 = person1.getId();
        int id22 = person2.getId();
        int id33 = person3.getId();
        boolean temp1 = (id1 == id11) || (id1 == id22) || (id1 == id33);
        boolean temp2 = (id2 == id11) || (id2 == id22) || (id2 == id33);
        return temp1 && temp2;
    }
}
