package io.makerplayground.project.expression;

import io.makerplayground.project.term.NumberWithUnitTerm;
import io.makerplayground.project.term.OperatorTerm;
import io.makerplayground.project.term.Term;
import io.makerplayground.project.term.ValueTerm;

import java.util.List;

public class CustomNumberExpression extends Expression {

    public CustomNumberExpression() {
        super(Type.CUSTOM_NUMBER);
    }

    public CustomNumberExpression(List<Term> terms) {
        super(Type.CUSTOM_NUMBER);
        this.getTerms().addAll(terms);
    }

    CustomNumberExpression(CustomNumberExpression e) {
        super(e);
    }

    @Override
    public boolean isValid() {
        return !getTerms().isEmpty() && isValidTerms();
    }

    public boolean isValidTerms() {
        List<Term> terms = getTerms();

        /* check parenthesis */
        int countParen = 0;
        for (Term term : terms) {
            if (term instanceof OperatorTerm) {
                if (OperatorTerm.OP.OPEN_PARENTHESIS.equals(term.getValue())) { countParen++; }
                else if(OperatorTerm.OP.CLOSE_PARENTHESIS.equals(term.getValue())) {
                    countParen--;
                    if (countParen < 0) { return false; }
                }
            }
        }
        if (countParen != 0) { return false; }

        /* check each term */
        for (Term t: terms) {
            if (!t.isValid()) {
                return false;
            }
        }

        /* check valid sequence */
        for (int i=0; i<terms.size()-1; i++) {
            Term term = terms.get(i);
            Term nextTerm = terms.get(i+1);
            if (isNumberOrValueTerm(term)) {
                if (isNumberOrValueTerm(nextTerm) || isParenTerm(nextTerm)) {
                    return false;
                }
            } else if (isOperationNotParenTerm(term)) {
                if (isOperationNotParenTerm(nextTerm) || isParenTerm(nextTerm)) {
                    return false;
                }
            } else if (OperatorTerm.OP.OPEN_PARENTHESIS.equals(term.getValue())) {
                if (isOperationNotParenTerm(term) || OperatorTerm.OP.CLOSE_PARENTHESIS.equals(term.getValue())) {
                    return false;
                }
            } else if (OperatorTerm.OP.CLOSE_PARENTHESIS.equals(term.getValue())) {
                if (isNumberOrValueTerm(term) || OperatorTerm.OP.OPEN_PARENTHESIS.equals(term.getValue())) {
                    return false;
                }
            }
        }

        /* check last */
        if (terms.size() > 0) {
            Term last = terms.get(terms.size()-1);
            if (OperatorTerm.OP.OPEN_PARENTHESIS.equals(last.getValue()) || isOperationNotParenTerm(last)) {
                return false;
            }
        }

        return true;
    }

    private boolean isParenTerm(Term t) {
        return t instanceof OperatorTerm &&
                (OperatorTerm.OP.OPEN_PARENTHESIS.equals(t.getValue()) || OperatorTerm.OP.CLOSE_PARENTHESIS.equals(t.getValue()));
    }

    private boolean isOperationNotParenTerm(Term t) {
        return t instanceof OperatorTerm && !isParenTerm(t);
    }

    private boolean isNumberOrValueTerm(Term t) {
        return t instanceof ValueTerm || t instanceof NumberWithUnitTerm;
    }

}
