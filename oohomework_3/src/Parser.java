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
            Expr expr = parseExpr();
            lexer.next();
            if (lexer.peek().equals("^")) {
                lexer.next();
                BigInteger exponent = new BigInteger(lexer.peek());
                lexer.next();
                return new ExprFactor(exponent, expr);
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
        } else if (lexer.peek().equals("e")) { //说明是指数函数
            lexer.next(); //peek -> (
            lexer.next(); //peak -> 参数
            Factor factor1 = parseFactor(); //得到exp中的表达式
            lexer.next(); //括号跳过
            if (lexer.peek().equals("^")) {
                lexer.next();
                BigInteger exponent1 = new BigInteger(lexer.peek());
                lexer.next();
                return new Exponential(factor1, exponent1);
            }
            BigInteger n = new BigInteger("1");
            return new Exponential(factor1, n);
        } else if (lexer.peek().equals("d")) {
            lexer.next(); //peek -> x
            lexer.next(); //perk -> (
            Factor factor2 = parseFactor();
            return new Derivation(factor2);
        } else {
            BigInteger num = new BigInteger(lexer.peek());
            lexer.next();
            return new Number(num);
        }
    }
}