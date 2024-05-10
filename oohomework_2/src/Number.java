import java.math.BigInteger;
import java.util.ArrayList;

public class Number implements Factor {
    private final BigInteger num;

    public Number(BigInteger num) {
        this.num = num;
    }

    @Override
    public Poly toPoly() {
        Mono mono = new Mono("0", num, null);
        ArrayList<Mono> monos = new ArrayList<>();
        monos.add(mono);
        return new Poly(monos);
    }
}
