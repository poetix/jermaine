package com.codepoetics.jermaine.matchers;

import com.fasterxml.jackson.databind.JsonNode;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeDiagnosingMatcher;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

final class JsonObjectMatcher extends TypeSafeDiagnosingMatcher<JsonNode> {
    private final Map<String, Matcher<? super JsonNode>> matchers = new HashMap<>();
    private final Set<String> expectNotPresent = new HashSet<>();

    @Override
    protected boolean matchesSafely(JsonNode objectNode, Description mismatchDescription) {
        if (!objectNode.isObject()) {
            mismatchDescription.appendText("is not object");
            return false;
        }

        DescriptionIndentation.indent();
        boolean matched = checkMatchers(objectNode, mismatchDescription) && checkNotExpected(objectNode, mismatchDescription);
        DescriptionIndentation.outdent();
        return matched;
    }

    private boolean checkNotExpected(JsonNode objectNode, Description mismatchDescription) {
        boolean matched = true;
        for (String fieldName : expectNotPresent) {
            if (objectNode.has(fieldName)) {
                matched = fieldUnexpectedlyPresent(mismatchDescription, fieldName);
            }
        }
        return matched;
    }

    private boolean checkMatchers(JsonNode objectNode, Description mismatchDescription) {
        boolean matched = true;
        for (Map.Entry<String, Matcher<? super JsonNode>> entry : matchers.entrySet()) {
            String fieldName = entry.getKey();

            if (objectNode.has(fieldName)) {
                matched = matched && matchField(objectNode.get(fieldName), mismatchDescription, entry.getValue(), fieldName);
            } else {
                matched = fieldNotPresent(mismatchDescription, fieldName);
            }
        }
        return matched;
    }

    private boolean fieldNotPresent(Description mismatchDescription, String fieldName) {
        DescriptionIndentation.apply(mismatchDescription);
        mismatchDescription.appendText(fieldName).appendText(" not found");
        return false;
    }

    private boolean fieldUnexpectedlyPresent(Description mismatchDescription, String fieldName) {
        DescriptionIndentation.apply(mismatchDescription);
        mismatchDescription.appendText(fieldName).appendText(" not expected, but present");
        return false;
    }

    private boolean matchField(JsonNode node, Description mismatchDescription, Matcher<? super JsonNode> matcher, String fieldName) {
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

        expectNotPresent.forEach(fieldName ->
                DescriptionIndentation.apply(description)
                        .appendText(fieldName)
                        .appendText(" not present"));

        DescriptionIndentation.outdent();
    }

    public JsonObjectMatcher withField(String name, Matcher<? super JsonNode> matcher) {
        matchers.put(name, matcher);
        return this;
    }

    public JsonObjectMatcher withoutField(String name) {
        expectNotPresent.add(name);
        return this;
    }
}
