import java.math.BigInteger;

public class Mono {
    private BigInteger exp; //指数
    private BigInteger coefficient;// 系数
    private Poly polyOfExp; //表示exp内的表达式

    public Mono(String exp, BigInteger coefficient, Poly poly) {
        this.exp = new BigInteger(exp);
        this.coefficient = coefficient;
        this.polyOfExp = poly;
    }

    public Mono(BigInteger exp, String coefficient, Poly poly) {
        this.exp = exp;
        this.coefficient = new BigInteger(coefficient);
        this.polyOfExp = poly;
    }  //两种构造方法，不一定都会用。

    public Mono(BigInteger exp, BigInteger coefficient, Poly poly) {
        this.exp = exp;
        this.coefficient = coefficient;
        this.polyOfExp = poly;
    }

    public String toString() {
        StringBuilder sb;
        if (isZero()) {
            return "0";
        } else if (noExp() || polyOfExp.allZero()) { //没有exp函数
            return determineStr();
        } else { //有exp函数
            String str = determineStr(); //
            sb = new StringBuilder();
            if (!str.equals("1") && !str.equals("-1")) {
                sb.append(str); //添加系数
                sb.append("*");
            } else if (str.equals("-1")) {
                sb.append("-");
            }
            sb.append("exp(");
            if (isNotFactor(polyOfExp)) {
                sb.append("(");
                sb.append(polyOfExp.toString());
                sb.append(")");
            } else {
                sb.append(polyOfExp.toString());
            }
            sb.append(")");
        }
        return sb.toString();
    }

    private String determineStr() {
        StringBuilder sb = new StringBuilder();
        if (coIsOne() && !hasX()) {
            return "1";
        } else if (coIsOne() && hasOneX()) {
            return "x";
        } else if (coIsOne() && hasMoreX()) {
            sb.append("x^");
            sb.append(exp.toString());
        } else if (!coIsOne() && !hasX() && !coIsMinusOne()) {
            return coefficient.toString();
        } else if (!coIsOne() && hasOneX() && !coIsMinusOne()) {
            sb.append(coefficient.toString());
            sb.append("*x");
        } else if (!coIsOne() && hasMoreX() && !coIsMinusOne()) {
            sb.append(coefficient.toString());
            sb.append("*x^");
            sb.append(exp.toString());
        } else if (coIsMinusOne() && !hasX()) { //表示负一
            return "-1";
        } else if (coIsMinusOne() && hasOneX()) {
            return "-x";
        } else if (coIsMinusOne() && hasMoreX()) {
            sb.append("-x^");
            sb.append(exp.toString());
        }
        return sb.toString();
    }

    private Boolean coIsMinusOne() {
        return coefficient.toString().equals("-1");
    }

    public BigInteger getExp() {
        return this.exp;
    }

    public BigInteger getCoefficient() {
        return this.coefficient;
    }

    public Poly getPolyOfExp() {
        return this.polyOfExp;
    }

    public boolean isEqual(Mono mono) {
        if (mono == null) {
            return false;
        }
        boolean temp1 = exp.equals(mono.getExp()) && coefficient.equals(mono.getCoefficient());
        boolean temp2;
        if (polyOfExp == null && mono.getPolyOfExp() == null) {
            temp2 = true;
        } else if (polyOfExp == null && mono.getPolyOfExp() != null) {
            temp2 = false;
        } else if (polyOfExp != null && mono.getPolyOfExp() == null) {
            temp2 = false;
        } else {
            temp2 = polyOfExp.isEqual(mono.getPolyOfExp());
        }
        return temp1 && temp2;
    }

    public Mono multMono(Mono mono) {
        BigInteger res1 = this.coefficient.multiply(mono.coefficient);
        BigInteger res2 = this.getExp().add(mono.getExp());
        Poly res3;
        Poly temp = mono.getPolyOfExp();
        if (polyOfExp != null && temp != null) {
            Poly newPoly = polyOfExp.creatSame();
            res3 = newPoly.addPoly(temp).creatSame();
        } else if (polyOfExp == null && temp != null) {
            res3 = temp.creatSame();
        } else if (polyOfExp != null && temp == null) {
            res3 = polyOfExp.creatSame();
        } else {
            res3 = null;
        }
        return new Mono(res2, res1, res3);
    }

    public Mono mergeMono(Mono mono) {
        BigInteger res1 = this.coefficient.add(mono.getCoefficient());
        if (polyOfExp == null) {
            return new Mono(exp, res1, null); //完全新的一个mono
        }
        return new Mono(exp, res1, polyOfExp.creatSame());
    }

    private boolean hasX() {
        return !exp.toString().equals("0");
    }

    private boolean hasOneX() {
        return exp.toString().equals("1");
    }

    private boolean noExp() {
        return polyOfExp == null;
    }

    private boolean hasMoreX() {
        return exp.compareTo(BigInteger.ONE) > 0;
    }

    public boolean isZero() {
        return coefficient.toString().equals("0");
    }

    private boolean coIsOne() {
        return coefficient.toString().equals("1");
    }

    private boolean isNotFactor(Poly poly) {
        if (poly.getMonos().size() > 1 && !poly.allZero()) { //注意此时的poly是exp里面的poly,poly里面含有mono
            return true; // 不止一个最小项，肯定要加括号
        } else if (poly.getMonos().size() == 1) { //只有一个最小项
            Mono temp = poly.getMonos().get(0); //取出唯一的最小项
            if (!temp.hasX() && temp.noExp() && !temp.isZero()) { //只有系数
                return false;
            } else if (temp.coIsOne() && temp.hasX() && temp.noExp()) { //只有x
                return false;
            } else if (temp.coIsOne() && !temp.hasX() && !temp.noExp()) { //只有exp
                return false;
            }
        }
        return true;
    }

    public boolean isMinus() {
        return coefficient.compareTo(BigInteger.ZERO) < 0;
    }

    public boolean canMerge(Mono mono) {
        if (polyOfExp == null && mono.getPolyOfExp() == null) {
            return exp.equals(mono.getExp());
        } else if (polyOfExp != null && mono.getPolyOfExp() != null) {
            boolean b = polyOfExp.isEqual(mono.getPolyOfExp());
            return exp.equals(mono.getExp()) && b;
        }
        return false;
    }

    public void setCoefficient(BigInteger coefficient) {
        this.coefficient = coefficient;
    }

    public Mono creatSame() {
        BigInteger newCo = coefficient;
        BigInteger newExp = exp;
        Mono newMono = null;
        if (polyOfExp == null) {
            newMono = new Mono(newExp, newCo, null);
        } else {
            Poly newPoly = polyOfExp.creatSame();
            newMono = new Mono(newExp, newCo, newPoly);
        }
        return newMono;
    }
}
