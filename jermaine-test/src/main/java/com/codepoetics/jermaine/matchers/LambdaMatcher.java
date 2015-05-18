package com.codepoetics.jermaine.matchers;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeDiagnosingMatcher;

import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

final class LambdaMatcher<I, O> extends TypeSafeDiagnosingMatcher<I> {

    static <I, O> Matcher<I> of(Function<? super I, ? extends O> extractor, Consumer<Description> describer, BiConsumer<O, Description> mismatchDescriber, Matcher<? super O> matcher) {
        return new LambdaMatcher<>(extractor, describer, mismatchDescriber, matcher);
    }

    private final Function<? super I, ? extends O> extractor;
    private final Consumer<Description> describer;
    private final BiConsumer<O, Description> mismatchDescriber;
    private final Matcher<? super O> matcher;

    private LambdaMatcher(Function<? super I, ? extends O> extractor, Consumer<Description> describer, BiConsumer<O, Description> mismatchDescriber, Matcher<? super O> matcher) {
        this.extractor = extractor;
        this.describer = describer;
        this.mismatchDescriber = mismatchDescriber;
        this.matcher = matcher;
    }

    @Override
    protected boolean matchesSafely(I input, Description mismatchDescription) {
        O value = extractor.apply(input);

        if (!matcher.matches(value)) {
            mismatchDescriber.accept(value, mismatchDescription);
            return false;
        }

        return true;
    }

    @Override
    public void describeTo(Description description) {
        describer.accept(description);
    }
}
