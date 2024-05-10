import java.util.ArrayList;
import java.util.Scanner;

public class Entry {
    private Scanner scanner = new Scanner(System.in);
    private Other other = new Other();

    public ArrayList<Function> processFunction() {
        int n = scanner.nextInt();
        ArrayList<Function> functions = new ArrayList<>();
        scanner.nextLine();
        for (int i = 0; i < n; i++) {
            String temp = scanner.nextLine();//读入函数的表达式
            String temp1 = other.remove(temp); //去掉多余的正负号等等

            functions.add(new Function(temp1));
        }
        return functions;
    }

    public String processExpr() {
        String input = scanner.nextLine();
        return other.remove(input);
    }
}
