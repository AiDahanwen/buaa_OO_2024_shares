import java.util.ArrayList;

public class Main {
    public static void main(String[] args) {
        Entry entry = new Entry();
        ArrayList<Function> functions = entry.processFunction(); // 加工读入的函数
        String input1 = entry.processExpr(); //加工表达式
        Other other1 = new Other();
        String input2 = other1.myReplace(input1, functions); //替换函数
        String input3 = other1.remove(input2); //去除替换后冗余的正负号
        String input4 = other1.addK(input3); //把所有表达式的左括号和右括号加倍
        Lexer lexer = new Lexer(input4);
        Parser parser = new Parser(lexer);
        Expr expr = parser.parseExpr();
        Poly poly = expr.toPoly();
        System.out.println(poly.toString());
    }
}
