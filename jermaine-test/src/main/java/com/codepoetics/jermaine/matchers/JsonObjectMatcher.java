package com.codepoetics.jermaine.matchers;

import com.fasterxml.jackson.databind.JsonNode;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeDiagnosingMatcher;

import java.util.Map;

final class JsonObjectMatcher extends TypeSafeDiagnosingMatcher<JsonNode> {
    private final Map<String, Matcher<? super JsonNode>> matchers;

    JsonObjectMatcher(Map<String, Matcher<? super JsonNode>> matchers) {
        this.matchers = matchers;
    }

    @Override
    protected boolean matchesSafely(JsonNode objectNode, Description mismatchDescription) {
        if (!objectNode.isObject()) {
            mismatchDescription.appendText("is not object");
            return false;
        }

        boolean matched = true;
        DescriptionIndentation.indent();
        for (Map.Entry<String, Matcher<? super JsonNode>> entry : matchers.entrySet()) {
            String fieldName = entry.getKey();

            if (objectNode.has(fieldName)) {
                matched = matched && matchField(objectNode.get(fieldName), mismatchDescription, matched, entry.getValue(), fieldName);
            } else {
                matched = fieldNotPresent(mismatchDescription, fieldName);
            }
        }
        DescriptionIndentation.outdent();
        return matched;
    }

    private boolean fieldNotPresent(Description mismatchDescription, String fieldName) {
        DescriptionIndentation.apply(mismatchDescription);
        mismatchDescription.appendText(fieldName).appendText(" not found");
        return false;
    }

    private boolean matchField(JsonNode node, Description mismatchDescription, boolean matched, Matcher<? super JsonNode> matcher, String fieldName) {
        if (!matcher.matches(node)) {
            DescriptionIndentation.apply(mismatchDescription);
            mismatchDescription.appendText(fieldName).appendText(": ");
            matcher.describeMismatch(node, mismatchDescription);
            return false;
        }
        return true;
    }

    @Override
    public void describeTo(Description description) {
        DescriptionIndentation.indent();
        matchers.entrySet().forEach(e -> {
            DescriptionIndentation.apply(description)
                    .appendText(e.getKey())
                    .appendText(": ");
            e.getValue().describeTo(description);
        });
        DescriptionIndentation.outdent();
    }

    public JsonObjectMatcher withField(String name, Matcher<? super JsonNode> matcher) {
        matchers.put(name, matcher);
        return this;
    }
}
