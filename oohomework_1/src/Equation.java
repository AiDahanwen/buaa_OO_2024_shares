import java.math.BigInteger;
import java.util.HashMap;

public class Equation {
    private HashMap<Integer, String> coefficient;

    public Equation(HashMap<Integer, String> coefficient) {
        this.coefficient = coefficient;
    }

    public Equation mult(Equation e) {
        HashMap<Integer, String> temp = new HashMap<>();
        for (Integer key : coefficient.keySet()) {
            for (Integer key1 : e.coefficient.keySet()) {
                if (temp.containsKey(key + key1)) {
                    String str = temp.get(key + key1);
                    temp.put(key + key1, bigAdd(str, bigMult(coefficient.get(key),
                            e.coefficient.get(key1))));
                } else {
                    temp.put(key + key1, bigMult(coefficient.get(key), e.coefficient.get(key1)));
                }
            }
        }
        return new Equation(temp);
    }

    public Equation pow(String in) {
        String exp = in.substring(1);
        Equation equ = this;
        Equation equ1 = this;
        for (int i = 1; i < Integer.parseInt(exp); i++) {
            equ1 = equ1.mult(equ);
        }
        if (Integer.parseInt(exp) == 0) {
            return becomeOne();
        }
        return equ1;
    }

    public Equation add(Equation e) {
        HashMap<Integer, String> temp = new HashMap<>();
        for (Integer key : coefficient.keySet()) {
            for (Integer key1 : e.coefficient.keySet()) {
                if (key1.equals(key)) { //指数相等
                    String sum = bigAdd(coefficient.get(key), e.coefficient.get(key1));
                    temp.put(key, sum);
                    coefficient.put(key, "a");
                    e.coefficient.put(key1, "a");
                }
            }
        }
        for (Integer key : coefficient.keySet()) {
            if (!coefficient.get(key).equals("a")) {
                temp.put(key, coefficient.get(key));
            }
        }
        for (Integer key : e.coefficient.keySet()) {
            if (!e.coefficient.get(key).equals("a")) {
                temp.put(key, e.coefficient.get(key));
            }
        }
        return new Equation(temp);
    }

    private String bigAdd(String s1, String s2) {
        BigInteger b1 = new BigInteger(s1);
        BigInteger b2 = new BigInteger(s2);
        return b1.add(b2).toString();
    }

    private String bigMult(String s1, String s2) {
        BigInteger b1 = new BigInteger(s1);
        BigInteger b2 = new BigInteger(s2);
        return b1.multiply(b2).toString();
    }

    public HashMap<Integer, String> getCoefficient() {
        return coefficient;
    }

    public Equation becomeOne() {
        HashMap<Integer, String> temp = new HashMap<>();
        temp.put(0, "1");
        return new Equation(temp);
    }
}
