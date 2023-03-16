package com.anikanov.paper.crawler.domain;

import com.anikanov.paper.crawler.config.GlobalConstants;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class KeyWordsFilter {
    private final List<Set<String>> acceptedCombinations;

    public KeyWordsFilter(String expression) {
        acceptedCombinations = Arrays.stream(expression.split("\\|"))
                .map(group -> Arrays.stream(group.split("&")).map(String::trim).map(String::toLowerCase).collect(Collectors.toSet())).toList();
    }

    public boolean accepts(String text) {
        return acceptedCombinations.stream()
                .anyMatch(combination -> combination.stream().allMatch(word -> {
                    final Set<String> aliases = GlobalConstants.DICTIONARY.getAliases(word);
                    return aliases.stream().anyMatch(alias -> text.trim().toLowerCase().contains(alias.trim().toLowerCase()));
                }));
    }
}
