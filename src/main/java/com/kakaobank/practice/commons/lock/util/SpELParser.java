package com.kakaobank.practice.commons.lock.util;

import org.springframework.expression.EvaluationContext;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;

public class SpELParser {
    public static String parameterToStringValue(String[] parameterNames, Object[] args, String name) {
        ExpressionParser parser = new SpelExpressionParser();
        EvaluationContext context = new StandardEvaluationContext();

        if (parameterNames.length == 0) {
            return "";
        }

        for (int i = 0; i < parameterNames.length; i++) {
            context.setVariable(parameterNames[i], args[i]);
        }

        var result = parser.parseExpression(name).getValue(context);
        return result != null ? result.toString() : "";
    }
}
