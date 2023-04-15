package com.anikanov.paper.crawler.domain;

import com.anikanov.paper.crawler.config.GlobalConstants;
import lombok.EqualsAndHashCode;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class KeyWordsFilter {
    private final Set<Combination> combinations;

    public KeyWordsFilter(String expression) {
        combinations = parse(expression).stream().map(Combination::new).collect(Collectors.toSet());
    }

    public boolean accepts(String text) {
        return combinations.stream().allMatch(combination -> combination.accepts(text));
    }

    private Set<Set<String>> parse(String expression) {
        final Set<Set<String>> acceptedCombinations = new HashSet<>();
        Set<Set<String>> subResult = new HashSet<>();
        StringBuilder exp = new StringBuilder();
        StringBuilder word = new StringBuilder();
        int bracketCounter = 0;
        for (int i = 0; i < expression.length(); i++) {
            char symbol = expression.charAt(i);
            if (symbol == '{') {
                bracketCounter++;
                exp.append(symbol);
                continue;
            } else if (symbol == '}') {
                bracketCounter--;
                exp.append(symbol);
                if (i != expression.length() - 1) {
                    continue;
                }
            } else if (symbol == '!') {
                exp.append(symbol);
                continue;
            }
            if (bracketCounter == 0) {
                if (exp.isEmpty() || exp.toString().equals("!")) {
                    if (symbol == '|') {
                        if (exp.toString().equals("!")) {
                            throw new RuntimeException("invalid expression format");
                        }
                        subResult = openBraces(Set.of(subResult, Set.of(Set.of(word.toString()))));
                        acceptedCombinations.addAll(subResult);
                        word = new StringBuilder();
                        subResult = new HashSet<>();
                    } else if (symbol == '&') {
                        if (exp.toString().equals("!")) {
                            throw new RuntimeException("invalid expression format");
                        }
                        subResult = openBraces(Set.of(subResult, Set.of(Set.of(word.toString()))));
                        word = new StringBuilder();
                    } else {
                        if (exp.toString().equals("!")) {
                            exp = new StringBuilder();
                            word.append("!");
                        }
                        word.append(symbol);
                        if (i == expression.length() - 1) {
                            subResult = openBraces(Set.of(subResult, Set.of(Set.of(word.toString()))));
                            acceptedCombinations.addAll(subResult);
                        }
                    }
                } else {
                    if (symbol == '|') {
                        final Set<Set<String>> parsedExpression = exp.toString().startsWith("!") ?
                                parse(negate(exp.toString())) : parse(exp.substring(1, exp.length() - 1));
                        acceptedCombinations.addAll(openBraces(Set.of(subResult, parsedExpression)));
                        exp = new StringBuilder();
                        subResult = new HashSet<>();
                    } else if (symbol == '&') {
                        final Set<Set<String>> parsedExpression = exp.toString().startsWith("!") ?
                                parse(negate(exp.toString())) : parse(exp.substring(1, exp.length() - 1));
                        exp = new StringBuilder();
                        subResult = openBraces(Set.of(subResult, parsedExpression));
                    } else if (i == expression.length() - 1) {
                        final Set<Set<String>> parsedExpression = exp.toString().startsWith("!") ?
                                parse(negate(exp.toString())) : parse(exp.substring(1, exp.length() - 1));
                        acceptedCombinations.addAll(openBraces(Set.of(subResult, parsedExpression)));
                        exp = new StringBuilder();
                        subResult = new HashSet<>();
                    } else {
                        throw new RuntimeException("invalid expression format");
                    }
                }
            } else {
                exp.append(symbol);
            }
        }
        return acceptedCombinations;
    }

    //parses a single bracket !{}
    private String negate(String expression) {
        expression = expression.replaceFirst("!\\{", "");
        expression = expression.substring(0, expression.length() - 1);
        final StringBuilder result = new StringBuilder("{");
        StringBuilder exp = new StringBuilder();
        StringBuilder word = new StringBuilder();
        int bracketCounter = 0;
        for (int i = 0; i < expression.length(); i++) {
            char symbol = expression.charAt(i);
            if (symbol == '{') {
                bracketCounter++;
                exp.append(symbol);
                continue;
            } else if (symbol == '}') {
                bracketCounter--;
                exp.append(symbol);
                if (i != expression.length() - 1) {
                    continue;
                }
            } else if (symbol == '!') {
                exp.append(symbol);
                continue;
            }
            if (bracketCounter == 0) {
                if (exp.isEmpty() || exp.toString().equals("!")) {
                    if (symbol == '|') {
                        if (exp.toString().equals("!")) {
                            throw new RuntimeException("invalid expression format");
                        }
                        final String negatedWord = word.toString().startsWith("!") ? word.toString().replace("!", "") : "!" + word;
                        result.append(negatedWord)
                                .append("}&{");
                        word = new StringBuilder();
                    } else if (symbol == '&') {
                        if (exp.toString().equals("!")) {
                            throw new RuntimeException("invalid expression format");
                        }
                        final String negatedWord = word.toString().startsWith("!") ? word.toString().replace("!", "") : "!" + word;
                        result.append(negatedWord)
                                .append("|");
                        word = new StringBuilder();
                    } else {
                        if (exp.toString().equals("!")) {
                            exp = new StringBuilder();
                            word.append("!");
                        }
                        word.append(symbol);
                        if (i == expression.length() - 1) {
                            final String negatedWord = word.toString().startsWith("!") ? word.toString().replace("!", "") : "!" + word;
                            result.append(negatedWord)
                                    .append("}");
                        }
                    }
                } else {
                    if (symbol == '|') {
                        final String negatedExp = exp.toString().startsWith("!") ? exp.toString().replace("!", "") : "!" + exp;
                        result.append(negatedExp)
                                .append("}&{");
                        exp = new StringBuilder();
                    } else if (symbol == '&') {
                        final String negatedExp = exp.toString().startsWith("!") ? exp.toString().replace("!", "") : "!" + exp;
                        result.append(negatedExp)
                                .append("|");
                        exp = new StringBuilder();
                    } else if (i == expression.length() - 1) {
                        final String negatedExp = exp.toString().startsWith("!") ? exp.toString().replace("!", "") : "!" + exp;
                        result.append(negatedExp)
                                .append("}");
                        exp = new StringBuilder();
                    } else {
                        throw new RuntimeException("invalid expression format");
                    }
                }
            } else {
                exp.append(symbol);
            }
        }
        return result.toString();
    }

    private Set<Set<String>> openBraces(Set<Set<Set<String>>> input) {
        if (input.isEmpty()) {
            return Collections.emptySet();
        }
        List<Set<Set<String>>> braces = input.stream().filter(inp -> !inp.isEmpty()).distinct().toList();
        final Set<Set<String>> result = new HashSet<>(braces.get(0));
        for (int i = 1; i < braces.size(); i++) {
            final Set<Set<String>> sub = new HashSet<>(result);
            result.clear();
            for (Set<String> c1 : sub) {
                for (Set<String> c2 : braces.get(i)) {
                    final Set<String> merged = new HashSet<>(c1);
                    merged.addAll(c2);
                    result.add(merged);
                }
            }
        }
        return result;
    }

    @EqualsAndHashCode
    private static class Combination {
        private final Set<String> required;
        private final Set<String> requiredNot;

        public Combination(Set<String> words) {
            required = new HashSet<>();
            requiredNot = new HashSet<>();
            apply(words);
        }

        public void apply(Set<String> words) {
            words.forEach(word -> {
                if (word.startsWith("!")) {
                    requiredNot.add(word);
                } else {
                    required.add(word);
                }
            });
        }

        public boolean accepts(String text) {
            return containsAllRequired(text) && doesNotContainUnnecessary(text);
        }

        private boolean containsAllRequired(String text) {
            return required.stream().allMatch(word -> {
                final Set<String> regexedAliases = GlobalConstants.DICTIONARY.getRegexedAliases(word);
                return regexedAliases.stream().anyMatch(regex -> {
                    final Matcher matcher = Pattern.compile(regex.trim().toLowerCase()).matcher(text.trim().toLowerCase());
                    return matcher.find();
                });
            });
        }

        private boolean doesNotContainUnnecessary(String text) {
            return requiredNot.stream().noneMatch(word -> {
                final Set<String> regexedAliases = GlobalConstants.DICTIONARY.getRegexedAliases(word);
                return regexedAliases.stream().anyMatch(regex -> {
                    final Matcher matcher = Pattern.compile(regex.trim().toLowerCase()).matcher(text.trim().toLowerCase());
                    return matcher.find();
                });
            });
        }
    }
}
