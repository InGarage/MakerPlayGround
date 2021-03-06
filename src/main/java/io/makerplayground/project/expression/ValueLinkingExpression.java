/*
 * Copyright (c) 2019. The Maker Playground Authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.makerplayground.project.expression;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.makerplayground.device.shared.Parameter;
import io.makerplayground.device.shared.NumberWithUnit;
import io.makerplayground.device.shared.Unit;
import io.makerplayground.device.shared.constraint.NumericConstraint;
import io.makerplayground.project.ProjectValue;
import io.makerplayground.project.term.*;

import java.util.Collections;
import java.util.List;

public class ValueLinkingExpression extends Expression {

    private final Parameter destParam;
    private final boolean inverse;

    private static final List<Term.Type> termType = List.of(Term.Type.OPERATOR, Term.Type.OPERATOR, Term.Type.OPERATOR
            , Term.Type.VALUE, Term.Type.OPERATOR, Term.Type.NUMBER, Term.Type.OPERATOR, Term.Type.OPERATOR, Term.Type.OPERATOR
            , Term.Type.NUMBER, Term.Type.OPERATOR, Term.Type.NUMBER, Term.Type.OPERATOR, Term.Type.OPERATOR, Term.Type.OPERATOR
            , Term.Type.OPERATOR, Term.Type.NUMBER, Term.Type.OPERATOR, Term.Type.NUMBER, Term.Type.OPERATOR, Term.Type.OPERATOR
            , Term.Type.OPERATOR, Term.Type.NUMBER);
    private static final List<Object> termValue = List.of(Operator.OPEN_PARENTHESIS, Operator.OPEN_PARENTHESIS
            , Operator.OPEN_PARENTHESIS, NumberWithUnit.ZERO, Operator.MINUS, NumberWithUnit.ZERO, Operator.CLOSE_PARENTHESIS
            , Operator.DIVIDE, Operator.OPEN_PARENTHESIS, NumberWithUnit.ZERO, Operator.MINUS
            , NumberWithUnit.ZERO, Operator.CLOSE_PARENTHESIS, Operator.CLOSE_PARENTHESIS, Operator.MULTIPLY
            , Operator.OPEN_PARENTHESIS, NumberWithUnit.ZERO, Operator.MINUS, NumberWithUnit.ZERO, Operator.CLOSE_PARENTHESIS
            , Operator.CLOSE_PARENTHESIS, Operator.PLUS, NumberWithUnit.ZERO);

    public ValueLinkingExpression(Parameter destParam) {
        this(destParam, Collections.emptyList(), false);
    }

    /**
     *
     * @param t list of {@link Term} in the following format
     *              (((fromValue - fromMinRange)/(fromMaxRange - fromMinRange)) * (toMaxRange - toMinRange)) + toMinRange
     */
    public ValueLinkingExpression(Parameter destParam, List<Term> t, boolean inverse) {
        super(Type.VALUE_LINKING);
        this.destParam = destParam;
        this.inverse = inverse;

        if (t.isEmpty()) {
            for (int i=0; i<termType.size(); i++) {
                if (termType.get(i) == Term.Type.OPERATOR) {
                    terms.add(new OperatorTerm((Operator) termValue.get(i)));
                } else if (termType.get(i) == Term.Type.VALUE) {
                    terms.add(new ValueTerm(null));
                } else if (termType.get(i) == Term.Type.NUMBER) {
                    terms.add(new NumberWithUnitTerm(new NumberWithUnit(0, Unit.NOT_SPECIFIED)));
                } else {
                    throw new IllegalStateException();
                }
            }

            NumberWithUnitTerm toMinRangeTerm = new NumberWithUnitTerm(new NumberWithUnit(destParam.getMinimumValue(), destParam.getUnit().get(0)));
            terms.set(18, toMinRangeTerm);
            terms.set(22, toMinRangeTerm);

            NumberWithUnitTerm toMaxRangeTerm = new NumberWithUnitTerm(new NumberWithUnit(destParam.getMaximumValue(), destParam.getUnit().get(0)));
            terms.set(16, toMaxRangeTerm);
        } else if (t.size() == 23) {
            boolean valid = true;
            for (int i=0; i<termType.size(); i++) {
                if (t.get(i).getType() != termType.get(i)) {
                    valid = false;
                    break;
                }
                if ((t.get(i).getType() == Term.Type.OPERATOR) && (t.get(i).getValue() != termValue.get(i))) {
                    valid = false;
                    break;
                }
            }
            if (!t.get(5).equals(t.get(11)) || !t.get(18).equals(t.get(22))) {
                valid = false;
            }
            if (valid) {
                terms.addAll(t);
            } else {
                throw new IllegalStateException("Found invalid term!!!");
            }
        } else {
            throw new IllegalStateException("Found invalid term!!!");
        }
    }

    public ValueLinkingExpression(ValueLinkingExpression e) {
        super(e);
        destParam = e.destParam;
        inverse = e.inverse;
    }

    @JsonIgnore
    public ProjectValue getSourceValue() {
        return ((ValueTerm) terms.get(3)).getValue();
    }

    public ValueLinkingExpression setSourceValue(ProjectValue v) {
        ValueLinkingExpression newExpression = new ValueLinkingExpression(this);
        if (v == null) {
            newExpression.terms.set(3, new ValueTerm(null));
            newExpression.terms.set(5, new NumberWithUnitTerm(NumberWithUnit.ZERO));
            newExpression.terms.set(11, new NumberWithUnitTerm(NumberWithUnit.ZERO));
            newExpression.terms.set(9, new NumberWithUnitTerm(NumberWithUnit.ZERO));
        } else {
            newExpression.terms.set(3, new ValueTerm(v));

            NumericConstraint constraint = (NumericConstraint) v.getValue().getConstraint();
            NumberWithUnitTerm newMinTerm = new NumberWithUnitTerm(new NumberWithUnit(
                    (constraint.getMax() - constraint.getMin()) * 0.25 + constraint.getMin(), constraint.getUnit()));
            NumberWithUnitTerm newMaxTerm = new NumberWithUnitTerm(new NumberWithUnit(
                    (constraint.getMax() - constraint.getMin()) * 0.75 + constraint.getMin(), constraint.getUnit()));

            if (!inverse) {
                newExpression.terms.set(5, newMinTerm);
                newExpression.terms.set(11, newMinTerm);
                newExpression.terms.set(9, newMaxTerm);
            } else {
                newExpression.terms.set(5, newMaxTerm);
                newExpression.terms.set(11, newMaxTerm);
                newExpression.terms.set(9, newMinTerm);
            }
        }

        return newExpression;
    }

    @JsonIgnore
    public NumberWithUnit getSourceLowValue() {
        return ((NumberWithUnitTerm) terms.get(5)).getValue();
    }

    public ValueLinkingExpression setSourceLowValue(NumberWithUnit n) {
        ValueLinkingExpression newExpression = new ValueLinkingExpression(this);
        NumberWithUnitTerm newMinTerm = new NumberWithUnitTerm(n);
        newExpression.terms.set(5, newMinTerm);
        newExpression.terms.set(11, newMinTerm);
        return newExpression;
    }

    @JsonIgnore
    public NumberWithUnit getSourceHighValue() {
        return ((NumberWithUnitTerm) terms.get(9)).getValue();
    }

    public ValueLinkingExpression setSourceHighValue(NumberWithUnit n) {
        ValueLinkingExpression newExpression = new ValueLinkingExpression(this);
        NumberWithUnitTerm newMaxTerm = new NumberWithUnitTerm(n);
        newExpression.terms.set(9, newMaxTerm);
        return newExpression;
    }

    @JsonIgnore
    public Parameter getDestinationParameter() {
        return destParam;
    }

    @JsonIgnore
    public NumberWithUnit getDestinationLowValue() {
        return ((NumberWithUnitTerm) terms.get(18)).getValue();
    }

    public ValueLinkingExpression setDestinationLowValue(NumberWithUnit n) {
        ValueLinkingExpression newExpression = new ValueLinkingExpression(this);
        NumberWithUnitTerm newMinTerm = new NumberWithUnitTerm(n);
        newExpression.terms.set(18, newMinTerm);
        newExpression.terms.set(22, newMinTerm);
        return newExpression;
    }

    @JsonIgnore
    public NumberWithUnit getDestinationHighValue() {
        return ((NumberWithUnitTerm) terms.get(16)).getValue();
    }

    public ValueLinkingExpression setDestinationHighValue(NumberWithUnit n) {
        ValueLinkingExpression newExpression = new ValueLinkingExpression(this);
        NumberWithUnitTerm newMaxTerm = new NumberWithUnitTerm(n);
        newExpression.terms.set(16, newMaxTerm);
        return newExpression;
    }

    public boolean isInverse() {
        return inverse;
    }

    public ValueLinkingExpression setInverse(boolean b) {
        return new ValueLinkingExpression(destParam, terms, b);
    }

    @Override
    public ValueLinkingExpression deepCopy() {
        return new ValueLinkingExpression(this);
    }

    @Override
    public boolean isValid() {
        return super.isValid();
    }
}


