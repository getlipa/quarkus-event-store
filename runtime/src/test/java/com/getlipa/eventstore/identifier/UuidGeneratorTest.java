package com.getlipa.eventstore.identifier;

import com.fasterxml.uuid.Generators;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.UUID;

class UuidGeneratorTest {

    private final UuidGenerator uuidGenerator = new UuidGenerator();

    @Test
    public void generate(){
        UUID uuid = uuidGenerator.generate("test","test");
        UUID expectedUuid = UUID.fromString("29343727-8efb-5bfe-8c4a-dc8a89ad0e0f");

        Assertions.assertEquals(expectedUuid, uuid);
        Assertions.assertTrue(uuidGenerator.generators.isEmpty());
    }

    @Test
    public void generateAndSaveNamespace(){
        Assertions.assertTrue(uuidGenerator.generators.isEmpty());

        UUID uuid = uuidGenerator.generate("$test","test");
        UUID expectedUuid = UUID.fromString("87ed65e0-7981-5c62-ba5b-5456cfdb450f");

        Assertions.assertEquals(expectedUuid, uuid);
        Assertions.assertTrue(uuidGenerator.generators.containsKey("$test"));
    }

    @Test
    public void generateWithSavedNamespace(){
        uuidGenerator.generators.put("$test", Generators.nameBasedGenerator(Generators.nameBasedGenerator().generate("NOT_TEST!")));

        UUID uuid = uuidGenerator.generate("$test","test");
        UUID expectedUuid = UUID.fromString("d8a18815-8319-5c23-a8ae-259b6269d55b");

        Assertions.assertEquals(expectedUuid, uuid);
        Assertions.assertTrue(uuidGenerator.generators.containsKey("$test"));
    }

}
