package com.getlipa.eventstore.identifier;

import com.fasterxml.uuid.Generators;
import com.fasterxml.uuid.impl.NameBasedGenerator;
import com.google.protobuf.ByteString;
import lombok.RequiredArgsConstructor;

import java.nio.ByteBuffer;
import java.util.Base64;
import java.util.UUID;

public abstract class Id {

    private static final NameBasedGenerator NO_NAMESPACE_GENERATOR = Generators.nameBasedGenerator();

    private UUID uuid;

    public static Id from(UUID uuid) {
        return new FromUuid(uuid);
    }

    public static Id random() {
        return from(UUID.randomUUID());
    }

    public static Id derive(String name) {
        return new Derived(name);
    }

    public static Id derive(String namespace, Id id) {
        return new Namespaced(new Derived(namespace), id);
    }

    public static Id derive(String namespace, String name) {
        return derive(namespace, Id.derive(name));
    }

    public static Id numeric(int id) {
        long mostSigBits = 0L; // All zeros for the most significant bits
        long leastSigBits = id & 0xFFFFFFFFFFFFL; // Ensure the integer fits in 48 bits
        return Id.from(new UUID(mostSigBits, leastSigBits));
    }

    public static Id from(ByteString causationId) {
        return from(causationId.toByteArray());
    }

    public static Id from(final byte[] bytes) {
        ByteBuffer byteBuffer = ByteBuffer.wrap(bytes);
        long high = byteBuffer.getLong();
        long low = byteBuffer.getLong();
        return from(new UUID(high, low));
    }

    public static Id nil() {
        return Id.numeric(0);
    }

    public UUID toUuid() {
        if (uuid == null) {
            uuid = createUuid();
        }
        return uuid;
    }

    abstract UUID createUuid();

    public ByteString toByteString() {
        return ByteString.copyFrom(toBytes());
    }

    public byte[] toBytes() {
        final var buffer = ByteBuffer.wrap(new byte[16]);
        buffer.putLong(toUuid().getMostSignificantBits());
        buffer.putLong(toUuid().getLeastSignificantBits());
        return buffer.array();
    }

    public String shortUUID() {
        final var uuid = toUuid();
        ByteBuffer byteBuffer = ByteBuffer.allocate(16);
        byteBuffer.putLong(uuid.getMostSignificantBits());
        byteBuffer.putLong(uuid.getLeastSignificantBits());

        return Base64.getEncoder().withoutPadding().encodeToString(byteBuffer.array())
                .replaceAll("/", "=")
                .replaceAll("\\+", "-");
    }

    @Override
    public int hashCode() {
        return toUuid().hashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other instanceof Id) {
            return toUuid().equals(((Id) other).toUuid());
        }
        return false;
    }

    @Override
    public String toString() {
        return shortUUID();
    }

    @RequiredArgsConstructor
    public static class Derived extends Id {

        private final String name;

        @Override
        public UUID createUuid() {
            return NO_NAMESPACE_GENERATOR.generate(name);
        }
    }

    @RequiredArgsConstructor
    public static class Namespaced extends Id {

        private final Derived namespace;

        private final Id id;

        @Override
        public UUID createUuid() {
            return Generators.nameBasedGenerator(namespace.toUuid()).generate(id.toBytes());
        }
    }

    @RequiredArgsConstructor
    public static class FromUuid extends Id {

        private final UUID uuid;

        @Override
        public UUID createUuid() {
            return uuid;
        }
    }
}
