package com.anikanov.paper.crawler.domain;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

public class Dictionary {
    private List<Set<String>> aliases;

    private static final String DICTIONARY_SEPARATOR = ",";

    public Dictionary() {
        this.aliases = Collections.emptyList();
    }

    public void applyNewDictionary(File file) throws Exception {
        final Scanner scanner = new Scanner(file);
        while (scanner.hasNext()) {
            final String line = scanner.nextLine();
            final Set<String> commons = Arrays.stream(line.split(DICTIONARY_SEPARATOR)).map(String::trim).map(String::toLowerCase).collect(Collectors.toSet());
            final Optional<Set<String>> existing = aliases.stream().filter(list -> list.stream().anyMatch(commons::contains)).findAny();
            if (existing.isPresent()) {
                existing.get().addAll(commons);
            } else {
                aliases.add(commons);
            }
        }
    }

    public void overrideDictionary(File file) throws Exception {
        aliases.clear();
        applyNewDictionary(file);
    }

    public Set<String> getAliases(String word) {
        return aliases.stream().filter(list -> list.contains(word.trim().toLowerCase())).findFirst().orElse(Set.of(word));
    }
}
