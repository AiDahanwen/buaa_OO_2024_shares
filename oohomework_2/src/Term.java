import java.math.BigInteger;
import java.util.ArrayList;

public class Term {
    private final ArrayList<Factor> factors;

    public Term() {
        this.factors = new ArrayList<>();
    }

    public void addFactor(Factor factor) {
        this.factors.add(factor);
    }

    public Poly toPoly() {
        Mono mono = new Mono("0", BigInteger.ONE, null); //如果不是指数函数就是null,不太对
        ArrayList<Mono> monos = new ArrayList<>();
        monos.add(mono);
        Poly poly = new Poly(monos);
        for (Factor factor : factors) {
            poly = poly.multPoly(factor.toPoly());
        }
        return poly;
    }  //乘，null则表达不是指数函数8
}
