package io.makerplayground.project.term;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class OperatorTermTest {


    @Test
    void isValidTest() {
        OperatorTerm tester = new OperatorTerm(OperatorTerm.Operator.PLUS);
        assertNotNull(tester);
    }

    @Test
    void PlusToString(){
        OperatorTerm tester = new OperatorTerm(OperatorTerm.Operator.PLUS);
        assertEquals("+",tester.getValue().toString());
    }

    @Test
    void MinusToString(){
        OperatorTerm tester = new OperatorTerm(OperatorTerm.Operator.MINUS);
        assertEquals("-",tester.getValue().toString());
    }
    @Test
    void MultiplyToString(){
        OperatorTerm tester = new OperatorTerm(OperatorTerm.Operator.MULTIPLY);
        assertEquals("x",tester.getValue().toString());
    }
    @Test
    void DivideToString(){
        OperatorTerm tester = new OperatorTerm(OperatorTerm.Operator.DIVIDE);
        assertEquals("\u00F7",tester.getValue().toString());
    }
    @Test
    void GreaterThanToString(){
        OperatorTerm tester = new OperatorTerm(OperatorTerm.Operator.GREATER_THAN);
        assertEquals(">",tester.getValue().toString());
    }
    @Test
    void LessThanToString(){
        OperatorTerm tester = new OperatorTerm(OperatorTerm.Operator.LESS_THAN);
        assertEquals("<",tester.getValue().toString());
    }

    @Test
    void GreaterThanOrEqualToString(){
        OperatorTerm tester = new OperatorTerm(OperatorTerm.Operator.GREATER_THAN_OR_EQUAL);
        assertEquals(">=",tester.getValue().toString());
    }

    @Test
    void LessThanOrEqualToString(){
        OperatorTerm tester = new OperatorTerm(OperatorTerm.Operator.LESS_THAN_OR_EQUAL);
        assertEquals("<=",tester.getValue().toString());
    }

    @Test
    void AndToString(){
        OperatorTerm tester = new OperatorTerm(OperatorTerm.Operator.AND);
        assertEquals("and",tester.getValue().toString());
    }

    @Test
    void OrToString(){
        OperatorTerm tester = new OperatorTerm(OperatorTerm.Operator.OR);
        assertEquals("or",tester.getValue().toString());
    }

    @Test
    void NotToString(){
        OperatorTerm tester = new OperatorTerm(OperatorTerm.Operator.NOT);
        assertEquals("not",tester.getValue().toString());
    }

    @Test
    void OpenParenthesisToString(){
        OperatorTerm tester = new OperatorTerm(OperatorTerm.Operator.OPEN_PARENTHESIS);
        assertEquals("(",tester.getValue().toString());
    }

    @Test
    void CloseParenthesisToString(){
        OperatorTerm tester = new OperatorTerm(OperatorTerm.Operator.CLOSE_PARENTHESIS);
        assertEquals(")",tester.getValue().toString());
    }

    @Test
    void PlusToCCode(){
        OperatorTerm tester = new OperatorTerm(OperatorTerm.Operator.PLUS);
        assertEquals("+",tester.toCCode());
    }

    @Test
    void MinusToCCode(){
        OperatorTerm tester = new OperatorTerm(OperatorTerm.Operator.MINUS);
        assertEquals("-",tester.toCCode());
    }
    @Test
    void MultiplyToCCode(){
        OperatorTerm tester = new OperatorTerm(OperatorTerm.Operator.MULTIPLY);
        assertEquals("*",tester.toCCode());
    }
    @Test
    void DivideToCCode(){
        OperatorTerm tester = new OperatorTerm(OperatorTerm.Operator.DIVIDE);
        assertEquals("/",tester.toCCode());
    }
    @Test
    void GreaterThanToCCode(){
        OperatorTerm tester = new OperatorTerm(OperatorTerm.Operator.GREATER_THAN);
        assertEquals(">",tester.toCCode());
    }
    @Test
    void LessThanToCCode(){
        OperatorTerm tester = new OperatorTerm(OperatorTerm.Operator.LESS_THAN);
        assertEquals("<",tester.toCCode());
    }

    @Test
    void GreaterThanOrEqualToCCode(){
        OperatorTerm tester = new OperatorTerm(OperatorTerm.Operator.GREATER_THAN_OR_EQUAL);
        assertEquals(">=",tester.toCCode());
    }

    @Test
    void LessThanOrEqualToCCode(){
        OperatorTerm tester = new OperatorTerm(OperatorTerm.Operator.LESS_THAN_OR_EQUAL);
        assertEquals("<=",tester.toCCode());
    }

    @Test
    void AndToCCode(){
        OperatorTerm tester = new OperatorTerm(OperatorTerm.Operator.AND);
        assertEquals("&&",tester.toCCode());
    }

    @Test
    void OrToCCode(){
        OperatorTerm tester = new OperatorTerm(OperatorTerm.Operator.OR);
        assertEquals("||",tester.toCCode());
    }

    @Test
    void NotToCCode(){
        OperatorTerm tester = new OperatorTerm(OperatorTerm.Operator.NOT);
        assertEquals("!",tester.toCCode());
    }

    @Test
    void OpenParenthesisToCCode(){
        OperatorTerm tester = new OperatorTerm(OperatorTerm.Operator.OPEN_PARENTHESIS);
        assertEquals("(",tester.toCCode());
    }

    @Test
    void CloseParenthesisToCCode(){
        OperatorTerm tester = new OperatorTerm(OperatorTerm.Operator.CLOSE_PARENTHESIS);
        assertEquals(")",tester.toCCode());
    }
}