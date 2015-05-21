package com.codepoetics.jermaine.matchers;

import com.fasterxml.jackson.databind.JsonNode;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeDiagnosingMatcher;

import java.util.stream.Collectors;
import java.util.stream.Stream;

class ContainsNodes extends TypeSafeDiagnosingMatcher<Iterable<? extends JsonNode>> {

    @SafeVarargs
    public static ContainsNodes inAnyOrder(Matcher<? super JsonNode>...matchers) {
        return new ContainsNodes(Stream.of(matchers).collect(Collectors.toList()));
    }

    public static ContainsNodes inAnyOrder(Iterable<Matcher<? super JsonNode>> matchers) {
        return new ContainsNodes(matchers);
    }

    private final Iterable<Matcher<? super JsonNode>> matchers;

    private ContainsNodes(Iterable<Matcher<? super JsonNode>> matchers) {
        this.matchers = matchers;
    }

    @Override
    protected boolean matchesSafely(Iterable<? extends JsonNode> jsonNodes, Description description) {
        for (Matcher<? super JsonNode> matcher : matchers) {
            boolean matched = false;
            for (JsonNode node : jsonNodes) {
                if (matcher.matches(node)) {
                    matched = true;
                }
            }
            if (!matched) {
                description.appendText("No item matches ").appendDescriptionOf(matcher).appendText(" in ").appendValueList("[", ",", "]", jsonNodes);
                return false;
            }
        }
        return true;
    }

    @Override
    public void describeTo(Description description) {
        boolean first = true;
        description.appendText("iterable over [");
        for (Matcher<? super JsonNode> matcher : matchers) {
            if (first) {
                first = false;
            } else {
                description.appendText(",");
            }
            matcher.describeTo(description);
        }
        description.appendText("] in any order");
    }
}
