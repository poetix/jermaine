package com.codepoetics.jermaine.matchers;

import com.codepoetics.navn.Name;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import org.hamcrest.Matcher;
import org.hamcrest.SelfDescribing;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.lang.reflect.Type;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class MagicJsonObjectMatcher implements InvocationHandler {

    public static <T extends Matcher<JsonNode>> T matching(Class<T> matcherClass) {
        return (T) Proxy.newProxyInstance(matcherClass.getClassLoader(),
                new Class<?>[] { matcherClass },
                new MagicJsonObjectMatcher());
    }

    private final JsonObjectMatcher matcher = JsonTreeMatcher.isObject();

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        if (method.getDeclaringClass().equals(Object.class)) {
            return method.invoke(this, args);
        }

        if (method.getDeclaringClass().equals(Matcher.class) || method.getDeclaringClass().equals(SelfDescribing.class)) {
            return method.invoke(matcher, args);
        }

        addMatcher(method, args);
        return proxy;
    }

    private void addMatcher(Method method, Object[] args) {
        String fieldName = fieldNameForMethod(method);
        if (null == args) {
            matcher.withoutField(fieldName);
        } else {
            matcher.withField(fieldName, jsonNodeMatcherFor(method.getGenericParameterTypes(), args));
        }
    }

    private String fieldNameForMethod(Method method) {
        if (method.getAnnotation(JsonProperty.class) != null) {
            return method.getAnnotation(JsonProperty.class).value();
        }
        return Name.of(method.getName()).withoutFirst().toCamelCase();
    }

    private Matcher<? super JsonNode> jsonNodeMatcherFor(Type[] parameterTypes, Object[] args) {
        if (parameterTypes.length > 1) {
            return JsonTreeMatcher.isArray(IntStream.range(0, parameterTypes.length).mapToObj(i ->
                    MatcherParameter.of(parameterTypes[i], args[i]).get()).collect(Collectors.toList()));
        }
        return MatcherParameter.of(parameterTypes[0], args[0]).get();
    }

}
