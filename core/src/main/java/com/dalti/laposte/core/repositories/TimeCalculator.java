package com.dalti.laposte.core.repositories;

import androidx.annotation.AnyThread;
import androidx.annotation.MainThread;

import net.objecthunter.exp4j.Expression;
import net.objecthunter.exp4j.ExpressionBuilder;
import net.objecthunter.exp4j.function.Function;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Singleton;

import dz.jsoftware95.silverbox.android.common.StringUtil;

@Singleton
@MainThread
public class TimeCalculator {

    private final TimeExpression averageRemainingTimeExpression;

    @Inject
    public TimeCalculator() {
        this.averageRemainingTimeExpression = getTimeExpression(StringSetting.AVERAGE_REMAINING_TIME_EXPRESSION);
    }

    private TimeExpression getTimeExpression(StringSetting setting) {
        TimeExpression expression = getTimeExpression(AppConfig.getInstance().getRemoteString(setting));
        if (expression != null && expression.isValid())
            return expression;
        else
            return getTimeExpression(setting.getDefaultString());
    }

    private TimeExpression getTimeExpression(String expression) {
        LinkedList<String> tokens = StringUtil.parse(expression);
        String exp = tokens.pollLast();
        if (exp != null)
            try {
                return new TimeExpression(exp, tokens);
            } catch (RuntimeException e) {
                Teller.warn("error while parsing: '" + expression + "'", e);
                return null;
            }
        else
            return null;
    }

    public Integer calcAverageRemainingTime(Parameters parameters) {
        return evaluateTimeExpression(averageRemainingTimeExpression, parameters);
    }

    public Integer evaluateTimeExpression(TimeExpression expression, Parameters parameters) {
        try {
            if (expression != null && parameters.variablesValues.keySet().containsAll(expression.requiredVars))
                return expression.evaluate(parameters);
            else
                return null;
        } catch (RuntimeException e) {
            Teller.warn("could not calc time with params: " + parameters, e);
            return null;
        }
    }

    static final class TimeExpression {

        static final Function MIN = new Function("min", 2) {
            @Override
            public double apply(double... args) {
                return Math.min(args[0], args[1]);
            }
        };
        static final Function MAX = new Function("max", 2) {
            @Override
            public double apply(double... args) {
                return Math.max(args[0], args[1]);
            }
        };
        static final Function AVG = new Function("avg", 4) {
            @Override
            public double apply(double... args) {
                double x1 = args[0], c1 = args[1], x2 = args[2], c2 = args[3];
                return (x1 * c1 + x2 * c2) / (c1 + c2);
            }
        };
        static final char ASSIGNMENT_SYMBOL = '=';

        final Expression expression;
        final Set<String> requiredVars;
        final Map<String, Double> optionalVars;

        TimeExpression(String expression, List<String> variables) {
            Set<String> required = new HashSet<>(8);
            Map<String, Double> optional = new HashMap<>(8);
            ExpressionBuilder builder = new ExpressionBuilder(expression);
            for (String def : variables) {
                int i = def.indexOf(ASSIGNMENT_SYMBOL);
                if (i < 0) {
                    builder.variable(def);
                    required.add(def);
                } else {
                    String name = def.substring(0, i);
                    builder.variable(name);
                    optional.put(name, Double.valueOf(def.substring(i + 1)));
                }
            }
            this.expression = builder.functions(MIN, MAX).build();
            this.optionalVars = optional;
            this.requiredVars = required;
        }

        public boolean isValid() {
            return expression.validate().isValid();
        }

        public int evaluate(Parameters parameters) {
            Map<String, Double> variablesValues = new HashMap<>(parameters.variablesValues.size());
            double now = System.currentTimeMillis();
            variablesValues.put(Parameters.CURRENT_TIME_VAR, now);
            variablesValues.putAll(optionalVars);
            variablesValues.putAll(parameters.variablesValues);
            expression.setVariables(variablesValues);
            return (int) expression.evaluate();
        }
    }

    @AnyThread
    public static final class Parameters {
        public static final String TICKET_VAR = "t";
        public static final String CURRENT_TOKEN_VAR = "c";
        public static final String REMAINING_VAR = "r";
        public static final String AVERAGE_SERVE_VAR = "a";
        public static final String WAITING_VAR = "w";
        public static final String TIMESTAMP_VAR = "u";
        public static final String CURRENT_TIME_VAR = "m";

        private final Map<String, Double> variablesValues;

        public Parameters(int ticket, int currentToken, int averageServeTime, Integer waiting, Long timestamp) {
            Map<String, Double> variables = new HashMap<>();
            put(variables, TICKET_VAR, ticket);
            put(variables, CURRENT_TOKEN_VAR, currentToken);
            put(variables, AVERAGE_SERVE_VAR, averageServeTime);
            if (ticket - currentToken > 0)
                put(variables, REMAINING_VAR, ticket - currentToken);

            if (waiting != null)
                variables.put(WAITING_VAR, waiting.doubleValue());
            if (timestamp != null)
                variables.put(TIMESTAMP_VAR, timestamp.doubleValue());

            this.variablesValues = Collections.unmodifiableMap(variables);
        }

        private void put(Map<String, Double> variables, String key, double value) {
            variables.put(key, value);
        }

        @Override
        public String toString() {
            return "Parameters{" + variablesValues + '}';
        }
    }
}
