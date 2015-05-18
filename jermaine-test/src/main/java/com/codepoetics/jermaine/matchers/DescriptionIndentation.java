package com.codepoetics.jermaine.matchers;

import org.hamcrest.Description;

final class DescriptionIndentation {

    private static final ThreadLocal<Integer> indentationLevel = ThreadLocal.withInitial(() -> 0);

    static void indent() {
        indentationLevel.set(indentationLevel.get() + 1);
    }

    static void outdent() {
        indentationLevel.set(indentationLevel.get() - 1);
    }

    static Description apply(Description description) {
        description.appendText("\n");
        for (int i = 0; i < indentationLevel.get(); i++) {
            description.appendText("\t");
        }
        return description;
    }

    private DescriptionIndentation() {
    }
}
