import com.oocourse.elevator2.TimableOutput;

public class Main {
    private static final boolean LOG = true;

    public static void main(String[] args) {
        TimableOutput.initStartTimestamp();
        InputHandler inputHandler = new InputHandler();
        inputHandler.start();
    }
}
