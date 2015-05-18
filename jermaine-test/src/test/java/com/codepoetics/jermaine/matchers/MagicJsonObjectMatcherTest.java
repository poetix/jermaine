package com.codepoetics.jermaine.matchers;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.hamcrest.Matcher;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

import static com.codepoetics.jermaine.matchers.MagicJsonObjectMatcherTest.PersonMatcher.aPerson;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class MagicJsonObjectMatcherTest {

    private final ObjectMapper mapper = new ObjectMapper();

    public interface PersonMatcher extends Matcher<JsonNode> {
        static PersonMatcher aPerson() {
            return MagicJsonObjectMatcher.matching(PersonMatcher.class);
        }

        PersonMatcher withName(String expected);
        PersonMatcher withName(Matcher<String> matcher);
        PersonMatcher withAge(int expected);
        PersonMatcher withAge(Matcher<Integer> matcher);
        PersonMatcher withFriends(PersonMatcher...matchers);
        PersonMatcher withFriends(Matcher<Iterable<JsonNode>> matchers);

        PersonMatcher withoutFriends();

        @JsonProperty("friends")
        PersonMatcher withTwoFriends(PersonMatcher firstFriend, Matcher<JsonNode> secondFriend);
    }

    private JsonNode node;

    @Before
    public void readTree() throws IOException {
        node = mapper.readTree(
                "{\"name\":\"Andrea Dworkin\"," +
                        " \"age\":42," +
                        " \"friends\": [" +
                        "    {\"name\":\"Shulamith Firestone\",\"age\":23}," +
                        "    {\"name\":\"Kate Millett\",\"age\":35,\"friends\":[]}" +
                        "  ]" +
                        "}");
    }

    @Test public void
    matches_with_varargs_of_matchers() {
        assertThat(node, aPerson()
                .withName("Andrea Dworkin")
                .withAge(greaterThan(40))
                .withFriends(
                        aPerson()
                                .withName(containsString("Firestone"))
                                .withAge(23)
                                .withoutFriends(),
                        aPerson()
                                .withName("Kate Millett")
                                .withAge(35)
                                .withFriends()));
    }


    @Test public void
    matches_with_multiple_params() {
        assertThat(node, aPerson()
                .withName("Andrea Dworkin")
                .withAge(greaterThan(40))
                .withTwoFriends(
                        aPerson()
                                .withName(containsString("Firestone"))
                                .withAge(23),
                        aPerson()
                                .withName("Kate Millett")
                                .withAge(35)));
    }

    @Test public void
    matches_with_matcher_of_iterable() {
        assertThat(node, aPerson()
                .withName("Andrea Dworkin")
                .withAge(greaterThan(40))
                .withFriends(hasItems(
                        aPerson()
                                .withName("Kate Millett")
                                .withAge(35),
                        aPerson()
                                .withName("Shulamith Firestone")
                                .withAge(lessThan(30)))));
    }

}
