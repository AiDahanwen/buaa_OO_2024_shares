import com.oocourse.spec2.main.Runner;

public class Main {
    public static void main(String[] args) throws Exception {
        Runner runner = new Runner(MyPerson.class, MyNetwork.class, MyTag.class);
        runner.run();
    }
}