import java.math.BigInteger;
import java.util.ArrayList;

public class Poly {
    private ArrayList<Mono> monos;

    public Poly(ArrayList<Mono> monos) {
        this.monos = monos;
    }

    public Poly() {
        this.monos = new ArrayList<>();
    }

    public boolean isEqual(Poly poly) {
        int flag = 0;
        if (poly == null) {
            return false;
        }
        if (poly.getMonos().size() != monos.size()) {
            return false;
        }
        for (Mono mono1 : monos) {
            flag = 0;
            for (Mono mono2 : poly.getMonos()) {
                if (mono2.isEqual(mono1)) {
                    flag = 1;
                    break;
                }
            }
            if (flag == 0) {
                return false;
            }
        }
        return flag == 1;
    }

    public Poly addPoly(Poly poly) {
        int flag = 0;
        for (Mono mono1 : poly.getMonos()) { //要加进来的数字
            flag = 0;
            for (Mono mono2 : monos) {
                if (mono2.canMerge(mono1)) { //2和1可以合并
                    mono2.setCoefficient(mono1.getCoefficient().add(mono2.getCoefficient()));
                    flag = 1;
                    break;
                }
            }
            if (flag == 0) {
                monos.add(mono1);
            }
        }
        return this;
    }

    public Poly multPoly(Poly poly) {
        Poly poly1 = new Poly();
        for (Mono mono1 : monos) { //
            for (Mono mono2 : poly.getMonos()) { //
                Mono resMono = mono1.multMono(mono2); //此处的resMono完全是一个新的
                Mono p = canMerge(resMono, poly1);
                if (p != null) { //也即可以合并
                    poly1.addMono(p.mergeMono(resMono));
                    poly1.removeOneMono(p);
                    continue;
                }
                poly1.addMono(resMono);
            }
        }
        return poly1;
    }

    public void removeOneMono(Mono p) {
        monos.remove(p);
    }

    public Mono canMerge(Mono resMono, Poly poly) {
        for (Mono temp : poly.getMonos()) {
            if (temp.canMerge(resMono)) {
                return temp;
            }
        }
        return null;
    }

    public Poly powPoly(BigInteger exp) {
        Poly temp = this;
        Poly every = this;
        BigInteger count = exp;
        if (exp.equals(BigInteger.ZERO)) {
            Mono mono = new Mono(BigInteger.ZERO, "1", null);
            ArrayList<Mono> monos1 = new ArrayList<>();
            monos1.add(mono);
            return new Poly(monos1);
        }
        while (count.compareTo(BigInteger.ONE) > 0) {
            temp = temp.multPoly(every);
            count = count.subtract(BigInteger.ONE);
        }
        return temp;
    }

    public String toString() {
        if (allZero()) {
            return "0";
        }
        StringBuilder sb = new StringBuilder();
        Mono first = null;
        for (Mono mono : monos) {
            if (!mono.isMinus() && !mono.isZero()) {
                first = mono;
                break;
            }
        }
        if (first != null) {
            sb.append(first.toString());
        }
        for (Mono mono : monos) {
            if (sb.length() != 0 && !mono.isMinus() && !mono.isZero() &&
                    !mono.isEqual(first)) {
                sb.append("+");
            }
            if (!mono.isZero() && !mono.isEqual(first)) {
                sb.append(mono.toString());
            }
        }
        return sb.toString();
    }

    public ArrayList<Mono> getMonos() {
        return monos;
    }

    public void addMono(Mono mono) {
        monos.add(mono);
    }

    public boolean allZero() {
        for (Mono mono : monos) {
            if (!mono.isZero()) {
                return false;
            }
        }
        return true;
    }

    public Poly creatSame() {
        Poly newPoly = new Poly();
        for (Mono mono : monos) {
            newPoly.addMono(mono.creatSame());
        }
        return newPoly;
    }
}
