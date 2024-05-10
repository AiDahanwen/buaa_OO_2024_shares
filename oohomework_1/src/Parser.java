import java.math.BigInteger;

public class Parser {
    private final Lexer lexer;

    public Parser(Lexer lexer) {
        this.lexer = lexer;
    }

    public Expr parseExpr() {
        Expr expr = new Expr();
        expr.addTerm(parseTerm());
        while (lexer.peek().equals("+")) {
            lexer.next();
            expr.addTerm(parseTerm());
        }
        return expr;
    }

    public Term parseTerm() {
        Term term = new Term();
        term.addFactor(parseFactor());
        while (lexer.peek().equals("*")) {
            lexer.next();
            term.addFactor(parseFactor());
        }
        return term;
    }

    public Factor parseFactor() {
        if (lexer.peek().equals("(")) {
            lexer.next();
            Factor expr = parseExpr();
            lexer.next();
            if (lexer.peek().equals("^")) {
                lexer.next();
                BigInteger exponent = new BigInteger(lexer.peek());
                lexer.next();
                return new Power(exponent, expr.toString());
            }
            return expr;
        } else if (lexer.peek().equals("x")) { //说明是幂函数
            BigInteger exp;
            lexer.next(); //
            if (!(lexer.peek().equals("^"))) {
                exp = new BigInteger("1");
            } else {
                lexer.next();
                exp = new BigInteger(lexer.peek());
                lexer.next();
            }
            return new Power(exp, "x");
        } else if (lexer.peek().equals("-")) {
            lexer.next(); //peek -> 1
            StringBuilder temp = new StringBuilder();
            temp.append("-");
            temp.append(lexer.peek());
            BigInteger number = new BigInteger(temp.toString());
            lexer.next();
            return new Number(number);
        } else {
            BigInteger num = new BigInteger(lexer.peek());
            lexer.next();
            return new Number(num);
        }
    }
}
