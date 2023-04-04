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
        final Set<Set<String>> opened = new HashSet<>();
        if (expression.startsWith("{") && expression.endsWith("}")) {
            expression = expression.replaceFirst("\\{", "");
            expression = expression.substring(0, expression.length() - 1);
        } else if (expression.startsWith("!{") && expression.endsWith("}")) {
            expression = expression.replaceFirst("!\\{", "");
            expression = expression.substring(0, expression.length() - 1);
            expression = negate(expression);
        }
        final Set<Set<String>> acceptedCombinations = new HashSet<>();
        final Set<String> combination = new HashSet<>();
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
                continue;
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
                        combination.add(word.toString());
                        word = new StringBuilder();
                        acceptedCombinations.add(new HashSet<>(combination));
                        combination.clear();
                    } else if (symbol == '&') {
                        if (exp.toString().equals("!")) {
                            throw new RuntimeException("invalid expression format");
                        }
                        combination.add(word.toString());
                        word = new StringBuilder();
                    } else {
                        if (exp.toString().equals("!")) {
                            exp = new StringBuilder();
                            word.append("!");
                        }
                        word.append(symbol);
                    }
                } else {
                    if (symbol == '|') {
                        acceptedCombinations.addAll(parse(exp.toString()));
                    } else if (symbol == '&') {
                        final Set<Set<String>> parsedExp = parse(exp.toString());
                        final String cutExpression = expression.substring(i + 1, expression.length() - 1);
//                        if (cutExpression.startsWith("{")) {
//
//                        }
                        final Set<String> nextMultiplier = parse(expression.substring(i + 1, expression.length() - 1)).stream().findFirst().orElse(Collections.emptySet());
                        final int indexesToSkip = String.join("", nextMultiplier).length() + nextMultiplier.size();
                        i += indexesToSkip;
                        acceptedCombinations.addAll(openBraces(Set.of(parsedExp, Set.of(nextMultiplier))));
                    } else {
                        throw new RuntimeException("invalid expression format");
                    }
                }
            } else {
                exp.append(symbol);
            }
        }
//        final Set<Set<String>> acceptedCombinations = Arrays.stream(expression.split("\\|"))
//                .map(group -> Arrays.stream(group.split("&")).map(String::trim).map(String::toLowerCase).collect(Collectors.toSet())).collect(Collectors.toSet());
//        acceptedCombinations.forEach(combination -> {
//            final Set<String> common = combination.stream().filter(word -> !word.matches("!?\\{.+}")).collect(Collectors.toSet());
//            final Set<Set<Set<String>>> parsedBraces = combination.stream().filter(word -> word.matches("!?\\{.+}")).map(this::parse).collect(Collectors.toSet());
//            parsedBraces.add(Set.of(common));
//            opened.addAll(openBraces(parsedBraces));
//        });
        return opened;
    }

    private String negate(String expression) {

        return expression;
    }

    private Set<Set<String>> openBraces(Set<Set<Set<String>>> input) {
        if (input.isEmpty()) {
            return Collections.emptySet();
        }
        List<Set<Set<String>>> braces = new ArrayList<>(input);
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
