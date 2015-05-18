package com.codepoetics.jermaine.matchers;

import com.fasterxml.jackson.databind.JsonNode;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.Matchers;
import org.hamcrest.number.IsCloseTo;

import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

import static org.hamcrest.Matchers.equalTo;

public final class JsonTreeMatcher {

    private JsonTreeMatcher() {
    }

    public static Matcher<JsonNode> isText(String expected) {
        return isText(equalTo(expected));
    }

    public static Matcher<JsonNode> isText(Matcher<? super String> matcher) {
        return matching(JsonNode::asText, matcher);
    }

    public static Matcher<JsonNode> isBoolean(boolean expected) {
        return isBoolean(equalTo(expected));
    }

    public static Matcher<JsonNode> isBoolean(Matcher<? super Boolean> matcher) {
        return matching(JsonNode::asBoolean, matcher);
    }

    public static Matcher<JsonNode> isInteger(int expected) {
        return isInteger(equalTo(expected));
    }

    public static Matcher<JsonNode> isInteger(Matcher<? super Integer> matcher) {
        return matching(JsonNode::asInt, matcher);
    }

    public static Matcher<JsonNode> isLong(long expected) {
        return isLong(equalTo(expected));
    }

    public static Matcher<JsonNode> isLong(Matcher<? super Long> matcher) {
        return matching(JsonNode::asLong, matcher);
    }

    public static Matcher<JsonNode> isDouble(double expected, double error) {
        return isDouble(IsCloseTo.closeTo(expected, error));
    }

    public static Matcher<JsonNode> isDouble(Matcher<? super Double> matcher) {
        return matching(JsonNode::asDouble, matcher);
    }

    public static Matcher<JsonNode> isJsonNull() {
        return LambdaMatcher.of(JsonNode::isNull,
                d -> d.appendText("<null>"),
                (t, d) -> d.appendText("was not null"),
                equalTo(true));
    }

    public static Matcher<JsonNode> hasField(String name, Matcher<? super JsonNode> matcher) {
        return LambdaMatcher.of(
                node -> node.get(name),
                getFieldDescriber(name, matcher),
                getMismatchDescriber(name, matcher),
                matcher);
    }

    private static BiConsumer<JsonNode, Description> getMismatchDescriber(String name, Matcher<? super JsonNode> matcher) {
        return (t, d) -> matcher.describeMismatch(t, d.appendText(name).appendText(": "));
    }

    private static Consumer<Description> getFieldDescriber(String name, Matcher<? super JsonNode> matcher) {
        return d -> matcher.describeTo(d.appendText(name).appendText(": "));
    }

    public static JsonObjectMatcher isObject() {
        return new JsonObjectMatcher();
    }

    @SafeVarargs
    public static Matcher<JsonNode> isArray(Matcher<? super JsonNode>...matchers) {
        return matchers.length == 0 ? isEmptyArray() : isArray(Matchers.contains(matchers));
    }

    public static Matcher<JsonNode> isEmptyArray() {
        return LambdaMatcher.of(
                node -> node.elements().hasNext(),
                d -> d.appendText("is empty"),
                (t, d) -> d.appendText("was not empty"),
                equalTo(false));
    }

    public static Matcher<JsonNode> isArray(List<Matcher<? super JsonNode>> matchers) {
        return matchers.size() == 0 ? isEmptyArray() : isArray(Matchers.contains(matchers));
    }

    public static Matcher<JsonNode> isArray(Matcher<Iterable<? extends JsonNode>> matcher) {
        return matching(node -> node, matcher);
    }

    private static <T> Matcher<JsonNode> matching(
            Function<JsonNode, T> extractor,
            Matcher<? super T> matcher) {
        return LambdaMatcher.of(extractor, matcher::describeTo, matcher::describeMismatch, matcher);
    }

}
