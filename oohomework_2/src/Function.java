import java.util.ArrayList;

public class Function {
    private int varNumber; //参数个数
    private String arguments; //参数组成的字符串
    private String expression;  //函数的表达式,应该为化简过的
    private String funcName; //存储函数名字

    public Function(String temp) {
        String[] group1 = temp.split("=");
        expression = group1[1].replace("exp", "e"); //去掉exp变为e
        String name = group1[0];
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < name.length(); i++) {
            char ch = name.charAt(i);
            if (ch == 'g' || ch == 'f' || ch == 'h') {
                funcName = String.valueOf(ch);
            } else if (ch == 'x' || ch == 'y' || ch == 'z') {
                sb.append(ch);
            }
        }
        arguments = sb.toString(); //xyz集
        varNumber = arguments.length();
        for (int i = 0; i < varNumber; i++) {
            expression = expression.replace(arguments.charAt(i), (char) ('a' + i));
        }
    }

    public String getFuncName() {
        return this.funcName;
    }

    public String expansion(String in) {
        String temp1 = expression;
        int find = in.indexOf(funcName);
        int count = 0;
        int i;
        StringBuilder sb = new StringBuilder();
        ArrayList<String> variables = new ArrayList<>();
        for (i = find + 1; i < in.length(); i++) {
            char ch = in.charAt(i);
            if (ch == '(') {
                count++;
            } else if (ch == ')') {
                count--;
            } else if (ch == ',' && count == 1) {
                variables.add(sb.toString());
                sb.setLength(0);
                continue;
            }
            if (count == 0) {
                variables.add(sb.toString());
                break;
            }
            if (i == find + 1) {
                continue;
            }
            sb.append(ch);
        }
        String str1;
        String str2;
        if (find > 0) {
            str1 = in.substring(0, find);
        } else {
            str1 = null;
        }
        str2 = in.substring(i + 1);
        for (i = 0; i < varNumber; i++) {
            String re = "(" + variables.get(i) + ")";
            temp1 = temp1.replace(String.valueOf((char) ('a' + i)), re);
        }
        if (str1 == null) {
            return "(" + temp1 + ")" + str2;
        }
        return str1 + "(" + temp1 + ")" + str2;
    } //替换
}
