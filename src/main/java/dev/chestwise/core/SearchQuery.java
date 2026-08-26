package dev.chestwise.core;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public record SearchQuery(List<String> terms, List<String> namespaces, List<String> tags) {
    public SearchQuery {
        terms = List.copyOf(terms);
        namespaces = List.copyOf(namespaces);
        tags = List.copyOf(tags);
    }

    public static SearchQuery parse(String raw) {
        List<String> terms = new ArrayList<>();
        List<String> namespaces = new ArrayList<>();
        List<String> tags = new ArrayList<>();
        for (String token : raw.toLowerCase(Locale.ROOT).trim().split("\\s+")) {
            if (token.isBlank()) {
                continue;
            }
            if (token.charAt(0) == '@' && token.length() > 1) {
                namespaces.add(token.substring(1));
            } else if (token.charAt(0) == '#' && token.length() > 1) {
                tags.add(normalizeTag(token.substring(1)));
            } else {
                terms.add(token);
            }
        }
        return new SearchQuery(terms, namespaces, tags);
    }

    private static String normalizeTag(String tag) {
        return tag.contains(":") ? tag : "minecraft:" + tag;
    }
}

