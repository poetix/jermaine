package com.codepoetics.jermaine.matchers;

import com.fasterxml.jackson.databind.JsonNode;
import org.hamcrest.Matcher;
import org.hamcrest.Matchers;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

final class MatcherParameter implements Supplier<Matcher<? super JsonNode>> {

    public static MatcherParameter of(Type type, Object value, boolean isOrdered) {
        if (rawTypeOf(type).equals(Object.class)) {
            return new MatcherParameter(value.getClass(), value, value.getClass(), isOrdered);
        }
        return new MatcherParameter(type, value, rawTypeOf(type), isOrdered);
    }

    private static Class<?> rawTypeOf(Type parameterType) {
        return parameterType instanceof Class
                ? (Class<?>) parameterType
                : (Class<?>) ((ParameterizedType) parameterType).getRawType();
    }

    private static Type firstTypeParameterOf(Type type) {
        return ((ParameterizedType) type).getActualTypeArguments()[0];
    }

    private static Type matcherSupertypeOf(Type type) {
        if (rawTypeOf(type).equals(Matcher.class)) {
            return type;
        }
        return Stream.of(rawTypeOf(type).getGenericInterfaces())
                .filter(t -> rawTypeOf(t).equals(Matcher.class))
                .findAny()
                .orElseThrow(() -> new IllegalArgumentException("Type " + type.getTypeName() + " does not extend Matcher"));
    }

    private MatcherParameter(Type type, Object value, Class<?> rawType, boolean isOrdered) {
        this.type = type;
        this.value = value;
        this.rawType = rawType;
        this.isOrdered = isOrdered;
    }

    private final Type type;
    private final Object value;
    private final Class<?> rawType;
    private final boolean isOrdered;

    @Override
    public Matcher<? super JsonNode> get() {
        if (rawType.isArray()) {
            return getVarArgsMatcher();
        }

        if (Matcher.class.isAssignableFrom(rawType)) {
            return getPromotedMatcher(firstTypeParameterOf(matcherSupertypeOf(type)), (Matcher<?>) value);
        }

        return getPromotedMatcher(type, Matchers.equalTo(value));
    }

    private Matcher<? super JsonNode> getVarArgsMatcher() {
        List<Matcher<? super JsonNode>> varArgMatchers = Stream.of((Object[]) value)
                .map(varArg -> MatcherParameter.of(rawType.getComponentType(), varArg, isOrdered).get())
                .collect(Collectors.toList());
        return isOrdered
                ? JsonTreeMatcher.isOrderedArray(varArgMatchers)
                : JsonTreeMatcher.isUnorderedArray(varArgMatchers);
    }

    private Matcher<? super JsonNode> getPromotedMatcher(Type valueType, Matcher<?> matcher) {
        Class<?> valueClass = rawTypeOf(valueType);

        if (valueClass.equals(JsonNode.class)) {
            return (Matcher<? super JsonNode>) matcher;
        }

        if (valueClass.equals(Iterable.class)) {
            if (rawTypeOf(firstTypeParameterOf(valueType)).equals(JsonNode.class)) {
                Matcher<Iterable<? extends JsonNode>> iterableMatcher = (Matcher<Iterable<? extends JsonNode>>) matcher;
                return isOrdered
                        ? JsonTreeMatcher.isOrderedArray(iterableMatcher)
                        : JsonTreeMatcher.isUnorderedArray(iterableMatcher);
            }
            throw new IllegalArgumentException("Don't yet know how to promote Matcher<Iterable<Foo>>");
        }

        if (valueClass.equals(String.class)) {
            return JsonTreeMatcher.isText((Matcher<String>) matcher);
        }

        if (valueClass.equals(Integer.class) || valueClass.equals(int.class)) {
            return JsonTreeMatcher.isInteger((Matcher<Integer>) matcher);
        }

        if (valueClass.equals(Long.class) || valueClass.equals(long.class)) {
            return JsonTreeMatcher.isLong((Matcher<Long>) matcher);
        }

        if (valueClass.equals(Boolean.class) || valueClass.equals(boolean.class)) {
            return JsonTreeMatcher.isBoolean((Matcher<Boolean>) matcher);
        }

        if (valueClass.equals(Double.class) || valueClass.equals(double.class)) {
            return JsonTreeMatcher.isDouble((Matcher<Double>) matcher);
        }

        throw new IllegalArgumentException("Cannot match instance of " + valueClass.getSimpleName());
    }

}
