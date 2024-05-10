import java.util.ArrayList;

public class Expr implements Factor {
    private final ArrayList<Term> terms;

    public Expr() {
        this.terms = new ArrayList<>();
    }

    public Expr(ArrayList<Term> terms) {
        this.terms = terms;
    }

    public void addTerm(Term term) {
        this.terms.add(term);
    }

    public ArrayList<Term> getTerms() {
        return terms;
    }

    @Override
    public Poly toPoly() {
        Poly poly = new Poly();
        for (Term term : terms) {
            poly = poly.addPoly(term.toPoly());
        }
        return poly;
    }
}
