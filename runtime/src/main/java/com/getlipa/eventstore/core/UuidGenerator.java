package com.getlipa.eventstore.core;

import com.fasterxml.uuid.Generators;
import com.fasterxml.uuid.impl.NameBasedGenerator;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class UuidGenerator {

    private final NameBasedGenerator defaultGenerator = Generators.nameBasedGenerator();

    private final Map<String, NameBasedGenerator> generators = new HashMap<>();

    public UUID generate(final String namespace, final String name){

        if (!namespace.startsWith("$")) {
            return Generators.nameBasedGenerator(defaultGenerator.generate(namespace)).generate(name);
        }

        if (!generators.containsKey(namespace)) {
            generators.put(namespace, Generators.nameBasedGenerator(defaultGenerator.generate(namespace)));
        }

        return generators.get(namespace).generate(name);

    }

}
