import java.util.ArrayList;
import java.util.HashMap;

public class Other {
    private ArrayList<Equation> operate = new ArrayList<>();
    private int pos = 0;

    public String remove(String input) {
        String temp = removeSpace(input);
        //System.out.println(temp);
        temp = removeSign(temp);
        //System.out.println(temp);
        temp = temp.replace("-", "+-1*");
        if (temp.charAt(0) == '+') {
            temp = temp.substring(1);
        }
        temp = temp.replace("*+-", "*-");
        temp = temp.replace("(+-", "(-");
        return temp;
    }

    public String removeSpace(String input) {
        String temp1 = input.replace(" ", "");
        temp1 = temp1.replace("\t", "");
        return temp1;
    }

    public String removeSign(String input) {
        StringBuilder sb2 = new StringBuilder();
        for (int i = 0; i < input.length(); i++) {
            char ch = input.charAt(i);
            int count = 0;
            while (ch != '+' && ch != '-') {
                sb2.append(ch);
                i++;
                if (i == input.length()) {
                    break;
                }
                ch = input.charAt(i);
            }
            int begin = i;
            while (ch == '+' || ch == '-') {
                if (ch == '-') {
                    count++;
                }
                i++;
                ch = input.charAt(i);
            }
            i--;
            if (count % 2 != 0) {
                sb2.append('-');
            } else if (count % 2 == 0 && i != input.length() - 1) {
                if (begin > 0 && (Character.isDigit(input.charAt(begin - 1)) ||
                        input.charAt(begin - 1) == 'x' || input.charAt(begin - 1) == ')')) {
                    sb2.append('+');
                }
            }
        }
        return sb2.toString();
    }

    public void calculate(String in) {
        String[] temp = in.split(" ");
        for (int i = 0; i < temp.length; i++) {
            if (isOperator(temp[i])) {
                Equation e1 = pop();
                Equation e2 = null;
                if (temp[i].equals("+")) {
                    e2 = pop();
                    push(e2.add(e1));
                } else if (temp[i].equals("*")) {
                    e2 = pop();
                    push(e2.mult(e1));
                } else {
                    push(e1.pow(temp[i]));
                }
            } else { //运算数
                push(new Equation(Put(temp[i])));
            }
        }
        Print();
    }

    private Equation pop() {
        pos--;
        Equation equ = operate.get(pos);
        operate.remove(pos);
        return equ;
    }

    private void push(Equation equation) {
        operate.add(equation);
        pos++;
    }

    private boolean isOperator(String in) {
        return in.equals("+") || in.equals("*") || (in.charAt(0) == '^');
    }

    private HashMap<Integer, String> Put(String str) {
        HashMap<Integer, String> temp = new HashMap<>();
        int index = str.indexOf('x');
        String num = null;
        String exp = null;
        if (index != -1) { //包含x
            if (index == str.length() - 1) {
                num = getNumber(str, 1);
                temp.put(1, num);
            } else if (str.charAt(index + 1) == '^') {
                exp = getNumber(str, 2);
                num = getNumber(str, 1);
                temp.put(Integer.parseInt(exp), num);
            } else {
                num = getNumber(str, 1);
                temp.put(1, num);
            }
        } else { //不包含x
            temp.put(0, str);
        }
        return temp;
    }

    private String getNumber(String str, int op) {
        String[] cut = str.split("\\*");
        String num = null;
        String exp = null;
        int i;
        for (i = 0; i < cut.length; i++) {
            if (!cut[i].contains("x")) {
                num = cut[i];
            } else {
                String[] cutExp = cut[i].split("\\^");
                for (int j = 0; j < cutExp.length; j++) {
                    if (!cutExp[j].contains("x")) {
                        exp = cutExp[j];
                    }
                }
            }
        }
        if (cut.length == 1) {
            num = "1";
        }
        if (op == 1) {
            return num;
        }
        return exp;
    }

    private void Print() {
        Equation finalEqu = operate.get(pos - 1);
        HashMap<Integer, String> temp = finalEqu.getCoefficient();
        StringBuilder finalStr1 = new StringBuilder();
        StringBuilder finalStr2 = new StringBuilder();
        if (allZero(temp)) {
            System.out.println("0");
            return;
        }
        for (Integer key : temp.keySet()) {
            String str = temp.get(key);
            if (str.charAt(0) == '-') {
                str = determineStr(str, "-1", key);
                finalStr2.append(str);
            } else if (!str.equals("0")) {
                if (!finalStr1.toString().isEmpty()) {
                    finalStr1.append("+");
                }
                str = determineStr(str, "1", key);
                finalStr1.append(str);
            }
        }
        System.out.println(finalStr1.append(finalStr2));
    }

    public String determineStr(String str, String end, int i) {
        String str1 = str;
        if (str1.equals(end)) {
            if (i == 1) {
                if (end.equals("-1")) {
                    str1 = "-x";
                } else {
                    str1 = "x";
                }
            } else if (i > 1) {
                if (end.equals("-1")) {
                    str1 = "-x^" + i;
                } else {
                    str1 = "x^" + i;
                }
            }
        } else {
            if (i == 1) {
                str1 = str + "*x";
            } else if (i > 1) {
                str1 = str + "*x^" + i;
            }
        }
        return str1;
    }

    private boolean allZero(HashMap<Integer, String> temp) {
        for (String str : temp.values()) {
            if (!str.equals("0")) {
                return false;
            }
        }
        return true;
    }
}
