package com.getlipa.eventstore.identifier;

import com.fasterxml.uuid.Generators;
import com.fasterxml.uuid.impl.NameBasedGenerator;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class UuidGenerator {

    public static final UuidGenerator INSTANCE = new UuidGenerator();
    private final NameBasedGenerator defaultGenerator = Generators.nameBasedGenerator();

    final Map<String, NameBasedGenerator> generators = new HashMap<>();

    public UUID generate(final String namespace, final String name){

        if (generators.containsKey(namespace)) {
            return generators.get(namespace).generate(name);
        }

        if (!namespace.startsWith("$")) {
            return Generators.nameBasedGenerator(defaultGenerator.generate(namespace)).generate(name);
        }

        NameBasedGenerator generator = Generators.nameBasedGenerator(defaultGenerator.generate(namespace));
        generators.put(namespace, generator);

        return generator.generate(name);

    }
}
