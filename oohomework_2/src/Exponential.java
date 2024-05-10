import java.math.BigInteger;
import java.util.ArrayList;

public class Exponential implements Factor {
    private Poly poly;
    private BigInteger exponent;

    public Exponential(Factor factor, BigInteger exp) {
        poly = factor.toPoly();
        exponent = exp;
    }

    public Poly toPoly() {
        ArrayList<Mono> monos = new ArrayList<>();
        monos.add(new Mono("0", exponent, null));
        ArrayList<Mono> monos1 = new ArrayList<>();
        if (poly.multPoly(new Poly(monos)).allZero()) {
            monos1.add(new Mono(BigInteger.ZERO, "1", null));
        } else {
            monos1.add(new Mono("0", BigInteger.ONE, poly.multPoly(new Poly(monos))));
        }
        return new Poly(monos1);
    }

}
