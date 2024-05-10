import java.math.BigInteger;

public class ExprFactor implements Factor {
    private BigInteger exp; //指数
    private Expr expr; //表达式本身

    public ExprFactor(BigInteger exp, Expr expr) {
        this.exp = exp;
        this.expr = expr;
    }

    @Override
    public Poly toPoly() {
        return expr.toPoly().powPoly(exp);
    }
}
