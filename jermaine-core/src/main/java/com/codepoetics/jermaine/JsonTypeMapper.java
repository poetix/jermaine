package com.codepoetics.jermaine;

import com.codepoetics.protonpack.Streamable;
import com.fasterxml.jackson.databind.JsonNode;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;

public class JsonTypeMapper {

    public JsonTypeMapper(Type type, Class<?> rawType) {
        this.type = type;
        this.rawType = rawType;
    }

    public static JsonTypeMapper of(Type type) {
        return new JsonTypeMapper(type, rawTypeOf(type));
    }

    private static Class<?> rawTypeOf(Type type) {
        return (Class<?>) (type instanceof Class
                ? type
                : ((ParameterizedType) type).getRawType());
    }

    private final Type type;
    private final Class<?> rawType;

    private Type getFirstTypeParameter() {
        return ((ParameterizedType) type).getActualTypeArguments()[0];
    }

    public Function<JsonNode, Object> mapper() {
        if (rawType.equals(JsonNode.class)) {
            return n -> n;
        }

        if (rawType.isArray()) {
            return toMappedNodes(rawType.getComponentType()).andThen(s -> s.toArray(Object[]::new));
        }

        if (rawType.equals(List.class)) {
            return toMappedNodes(getFirstTypeParameter()).andThen(Streamable::toList);
        }

        if (rawType.equals(Set.class)) {
            return toMappedNodes(getFirstTypeParameter()).andThen(Streamable::toSet);
        }

        if (rawType.equals(Map.class)) {
            Function<JsonNode, Object> valueMapper = JsonTypeMapper.of(getFirstTypeParameter()).mapper();
            return n -> Streamable.of(n::fields).toMap(Map.Entry::getKey, e -> valueMapper.apply(e.getValue()));
        }

        if (rawType.equals(Optional.class)) {
            return JsonTypeMapper.of(getFirstTypeParameter()).mapper().andThen(Optional::ofNullable);
        }

        if (rawType.equals(String.class)) {
            return JsonNode::asText;
        }

        if (rawType.equals(Integer.class) || rawType.equals(int.class)) {
            return JsonNode::asInt;
        }

        if (rawType.equals(Long.class) || rawType.equals(long.class)) {
            return JsonNode::asLong;
        }

        if (rawType.equals(Double.class) || rawType.equals(double.class)) {
            return JsonNode::asDouble;
        }

        if (rawType.equals(Boolean.class) || rawType.equals(boolean.class)) {
            return JsonNode::asBoolean;
        }

        if (rawType.isInterface()) {
            return n -> JsonProxy.wrapping(n).with(rawType);
        }

        throw new IllegalArgumentException("Unable to map json node to type " + type);
    }

    private Function<JsonNode, Streamable<Object>> toMappedNodes(Type itemType) {
        return n -> Streamable.of(n).map(JsonTypeMapper.of(itemType).mapper());
    }
}
