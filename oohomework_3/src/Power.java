import java.math.BigInteger;
import java.util.ArrayList;

public class Power implements Factor {
    private BigInteger exponent;
    private String var; //储存是谁的幂次

    public Power(BigInteger exponent, String var) {
        this.var = var;
        this.exponent = exponent;
    }

    @Override
    public Poly toPoly() { //这里的power只代表x的power
        Mono mono = new Mono(exponent, "1", null);
        ArrayList<Mono> monos = new ArrayList<>();
        monos.add(mono);
        return new Poly(monos);
    }
}
