package in.kuros.jfirebase.provider.firebase;

import in.kuros.jfirebase.entity.Id;
import in.kuros.jfirebase.entity.IdReference;
import in.kuros.jfirebase.entity.Parent;
import in.kuros.jfirebase.exception.PersistenceException;
import lombok.Data;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class EntityHelperImplTest {


    @Test
    void shouldThrowIdValuesArePresent() {
        final EntityHelperTestObject testObj = new EntityHelperTestObject();
        Assertions.assertThrows(PersistenceException.class, () -> EntityHelper.INSTANCE.validateIdsNotNull(testObj));
        testObj.setId(RandomStringUtils.randomAlphanumeric(5));
        Assertions.assertThrows(PersistenceException.class, () -> EntityHelper.INSTANCE.validateIdsNotNull(testObj));
        testObj.setRefId(RandomStringUtils.randomAlphanumeric(5));
        Assertions.assertThrows(PersistenceException.class, () -> EntityHelper.INSTANCE.validateIdsNotNull(testObj));
        testObj.setAnotherRef(RandomStringUtils.randomAlphanumeric(5));
        Assertions.assertDoesNotThrow(() -> EntityHelper.INSTANCE.validateIdsNotNull(testObj));
    }

    @Data
    private static class EntityHelperTestObject {
        @Id
        private String id;
        @Parent
        @IdReference(EntityHelperReferenceObject.class)
        private String refId;
        @IdReference(EntityHelperReferenceObject.class)
        private String anotherRef;
        private String value;
    }

    @Data
    private static class EntityHelperReferenceObject {
        @Id
        private String id;
        private String name;
    }

}
