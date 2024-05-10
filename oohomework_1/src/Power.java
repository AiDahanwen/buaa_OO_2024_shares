import java.math.BigInteger;

public class Power implements Factor {
    private BigInteger exponent;
    private String var; //储存是谁的幂次

    public Power(BigInteger exponent, String var) {
        this.var = var;
        this.exponent = exponent;
    }

    public BigInteger getExponent() {
        return this.exponent;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(var);
        sb.append(" ");
        sb.append("^");
        sb.append(exponent.toString());
        return sb.toString();
    }
}
