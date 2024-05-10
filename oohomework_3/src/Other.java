import java.util.ArrayList;

public class Other {

    public String remove(String input) {
        String temp = removeSpace(input);
        temp = removeSign(temp);
        temp = temp.replace("-", "+-1*");
        if (temp.charAt(0) == '+') {
            temp = temp.substring(1);
        }
        temp = temp.replace("*+-", "*-");
        temp = temp.replace("(+-", "(-");
        temp = temp.replace("-1*1*", "-1*");
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
                        isVary(input.charAt(begin - 1)) || input.charAt(begin - 1) == ')')) {
                    sb2.append('+');
                }
            }
        }
        return sb2.toString();
    }

    public String myReplace(String input1, ArrayList<Function> functions) {
        String in = input1;
        in = in.replace("exp(", "e(");
        boolean flag = in.contains("f") || in.contains("g") || in.contains("h");
        while (flag) {
            if (in.contains("f")) {
                in = find("f", functions, in);
                //System.out.println(in);
            }
            if (in.contains("g")) {
                in = find("g", functions, in);
            }
            if (in.contains("h")) {
                in = find("h", functions, in);
            }
            flag = in.contains("f") || in.contains("g") || in.contains("h");
        }
        return in;
    }

    private String find(String funcName, ArrayList<Function> functions, String src) {
        String temp = src;
        for (Function function : functions) {
            if (function.getFuncName().equals(funcName)) {
                temp = function.expansion(temp);
                break;
            }
        }
        return temp;
    }

    public String addK(String str) {
        String str1 = str.replace("(", "((");
        str1 = str1.replace(")", "))");
        return str1;
    }

    private boolean isVary(char ch) {
        return ch == 'x' || ch == 'y' || ch == 'z';
    }
}
