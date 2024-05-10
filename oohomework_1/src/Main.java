import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        String input = scanner.nextLine();
        Other other = new Other();
        String input1 = other.remove(input);
        //System.out.println(input1);
        Lexer lexer = new Lexer(input1);
        Parser parser = new Parser(lexer);
        Expr expr = parser.parseExpr();
        //System.out.println(expr);
        other.calculate(expr.toString());
    }
}
