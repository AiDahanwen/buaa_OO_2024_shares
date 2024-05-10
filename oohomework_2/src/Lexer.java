
public class Lexer {
    private final String input;
    private int pos = 0;
    private String curToken;

    public Lexer(String input) {
        this.input = input;
        this.next();
    }

    private String getNumber() {
        StringBuilder sb = new StringBuilder();
        int flag = 0;
        while (pos < input.length() && input.charAt(pos) == '0') {
            pos++;
        }
        while (pos < input.length() && Character.isDigit(input.charAt(pos))) {
            flag = 1;
            sb.append(input.charAt(pos));
            ++pos;
        }
        if (flag == 0) {
            sb.append("0");
        }
        return sb.toString();
    }

    public void next() {
        if (pos == input.length()) {
            return;
        }
        char c = input.charAt(pos);
        if (Character.isDigit(c)) {
            curToken = this.getNumber();
        } else {
            pos += 1;
            curToken = String.valueOf(c);
        }
    }

    public String peek() {
        return this.curToken;
    }
}
