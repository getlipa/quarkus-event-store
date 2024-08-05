package com.getlipa.eventstore.identifier;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.UUID;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

class IdTest {

    @ParameterizedTest
    @MethodSource
    public void uuid(Id id, String expectedUuid) {
        assertEquals(expectedUuid, id.toUuid().toString());
    }

    public static Stream<Arguments> uuid() {
        return Stream.of(
                Arguments.of(Id.derive("test", "test"), "1a6d2a43-49e7-54ec-9f0c-2431861a5186"),
                Arguments.of(Id.derive("test"), "a94a8fe5-ccb1-5ba6-9c4c-0873d391e987"),
                Arguments.of(Id.nil(), "00000000-0000-0000-0000-000000000000"),
                Arguments.of(Id.numeric(0), "00000000-0000-0000-0000-000000000000"),
                Arguments.of(Id.numeric(1), "00000000-0000-0000-0000-000000000001")
        );
    }

    @Test
    public void equalsAndHashCode() {
        final var one = Id.derive("test","test");
        final var two = Id.derive("test","test");

        assertEquals(one, two);
        assertEquals(one.hashCode(), two.hashCode());
    }

    @Test
    public void random(){
        Assertions.assertNotEquals(Id.random(), Id.random());
    }
}