import java.util.ArrayList;

public class Main {
    public static void main(String[] args) {
        Entry entry = new Entry();
        ArrayList<Function> functions = entry.processFunction();
        String input1 = entry.processExpr();
        Other other1 = new Other();
        String input2 = other1.myReplace(input1, functions);
        String input3 = other1.remove(input2);
        String input4 = other1.addK(input3);
        Lexer lexer = new Lexer(input4);
        Parser parser = new Parser(lexer);
        Expr expr = parser.parseExpr();
        Poly poly = expr.toPoly();
        System.out.println(poly.toString());
    }
}
