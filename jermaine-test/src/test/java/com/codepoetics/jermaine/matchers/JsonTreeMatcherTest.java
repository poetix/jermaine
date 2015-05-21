package com.codepoetics.jermaine.matchers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.hamcrest.Matcher;
import org.hamcrest.StringDescription;
import org.junit.Test;

import java.io.IOException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.containsString;

public class JsonTreeMatcherTest {

    private final ObjectMapper mapper = new ObjectMapper();

    @Test public void
    matches_json() throws IOException {
        JsonNode node = mapper.readTree("{\"list\":[\"foo\",12,1000,3.14,true,null],\"int\":42,\"null\":null}");

        assertThat(node, JsonTreeMatcher.isObject()
                .withField("list",
                        JsonTreeMatcher.isOrderedArray(
                                JsonTreeMatcher.isText("foo"),
                                JsonTreeMatcher.isInteger(12),
                                JsonTreeMatcher.isLong(1000L),
                                JsonTreeMatcher.isDouble(3.14, 0.001),
                                JsonTreeMatcher.isBoolean(true),
                                JsonTreeMatcher.isJsonNull()))
                .withField("int", JsonTreeMatcher.isInteger(42))
                .withField("null", JsonTreeMatcher.isJsonNull()));
    }

    @Test public void
    describes_expected_json() throws IOException {
        Matcher<JsonNode> matcher = JsonTreeMatcher.isObject()
                .withField("list",
                        JsonTreeMatcher.isOrderedArray(
                                JsonTreeMatcher.isText("foo"),
                                JsonTreeMatcher.isInteger(12),
                                JsonTreeMatcher.isLong(1000L),
                                JsonTreeMatcher.isDouble(3.14, 0.001),
                                JsonTreeMatcher.isBoolean(true),
                                JsonTreeMatcher.isJsonNull()))
                .withField("int", JsonTreeMatcher.isInteger(42))
                .withField("null", JsonTreeMatcher.isJsonNull());

        StringDescription description = new StringDescription();

        matcher.describeTo(description);

        assertThat(description.toString(), allOf(
                containsString("int: <42>"),
                containsString("null: <null>"),
                containsString("list: iterable containing " +
                    "[\"foo\", <12>, <1000L>, a numeric value within <0.001> of <3.14>, <true>, <null>]")));
    }

}
