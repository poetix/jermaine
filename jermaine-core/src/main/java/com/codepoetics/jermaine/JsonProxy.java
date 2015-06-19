package com.codepoetics.jermaine;

import com.codepoetics.navn.Name;
import com.fasterxml.jackson.databind.JsonNode;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Optional;
import java.util.function.Function;

public class JsonProxy implements InvocationHandler {

    private final JsonNode target;

    private JsonProxy(JsonNode target) {
        this.target = target;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        if (method.getDeclaringClass().isAssignableFrom(JsonProxy.class)) {
            return method.invoke(this, args);
        }

        String fieldName = Name.of(method.getName()).withoutFirst().toCamelCase();
        Function<JsonNode, Object> mapper = JsonTypeMapper.of(method.getGenericReturnType()).mapper();
        JsonNode value = target.get(fieldName);

        if (method.getReturnType().equals(Optional.class)) {
            return value == null
                    ? Optional.empty()
                    : mapper.apply(value);
        }

        return value == null
                ? null
                : mapper.apply(value);
    }

    interface NodeCapture {
        <T> T with(Class<? extends T> proxyClass);
    }

    public static NodeCapture wrapping(JsonNode node) {
        return new NodeCapture() {
            @Override
            public <T> T with(Class<? extends T> proxyClass) {
                return (T) Proxy.newProxyInstance(proxyClass.getClassLoader(),
                        new Class<?>[] { proxyClass },
                        new JsonProxy(node));
            }
        };
    }

}
