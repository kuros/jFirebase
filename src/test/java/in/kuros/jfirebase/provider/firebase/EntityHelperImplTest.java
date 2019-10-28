package in.kuros.jfirebase.provider.firebase;

import com.google.common.collect.Sets;
import in.kuros.jfirebase.entity.CreateTime;
import in.kuros.jfirebase.entity.Entity;
import in.kuros.jfirebase.entity.EntityDeclarationException;
import in.kuros.jfirebase.entity.Id;
import in.kuros.jfirebase.entity.IdReference;
import in.kuros.jfirebase.entity.Parent;
import in.kuros.jfirebase.entity.UpdateTime;
import in.kuros.jfirebase.exception.PersistenceException;
import lombok.Data;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.Date;
import java.util.Set;
import java.util.stream.Collectors;

class EntityHelperImplTest {


    @Test
    void shouldThrowIdValuesArePresent() {
        final EntityObject testObj = new EntityObject();
        Assertions.assertThrows(PersistenceException.class, () -> EntityHelper.INSTANCE.validateIdsNotNull(testObj));
        testObj.setId(RandomStringUtils.randomAlphanumeric(5));
        Assertions.assertThrows(PersistenceException.class, () -> EntityHelper.INSTANCE.validateIdsNotNull(testObj));
        testObj.setParentId(RandomStringUtils.randomAlphanumeric(5));
        Assertions.assertThrows(PersistenceException.class, () -> EntityHelper.INSTANCE.validateIdsNotNull(testObj));
        testObj.setSuperParentId(RandomStringUtils.randomAlphanumeric(5));
        Assertions.assertDoesNotThrow(() -> EntityHelper.INSTANCE.validateIdsNotNull(testObj));
    }

    @Test
    void shouldGetDocumentPathForSimpleEntity() {
        final EntitySuperParent parent = new EntitySuperParent();
        parent.setId(RandomStringUtils.randomAlphanumeric(2));
        parent.setName(RandomStringUtils.randomAlphanumeric(3));

        final String documentPath = EntityHelper.INSTANCE.getDocumentPath(parent);

        Assertions.assertEquals("super/" + parent.getId(), documentPath);
    }

    @Test
    void shouldGetDocumentPathForHierarchyEntity() {
        final EntityObject object = new EntityObject();
       object.setId(RandomStringUtils.randomAlphanumeric(4));
       object.setParentId(RandomStringUtils.randomAlphanumeric(4));
       object.setSuperParentId(RandomStringUtils.randomAlphanumeric(4));
       object.setValue(RandomStringUtils.randomAlphanumeric(4));

        final String documentPath = EntityHelper.INSTANCE.getDocumentPath(object);

        Assertions.assertEquals("super/" + object.getSuperParentId() + "/parent/" + object.getParentId() + "/obj/" + object.getId(),  documentPath);
    }

    @Test
    void shouldGetCollectionForHierarchyEntity() {
        final EntityObject object = new EntityObject();
       object.setId(RandomStringUtils.randomAlphanumeric(4));
       object.setParentId(RandomStringUtils.randomAlphanumeric(4));
       object.setSuperParentId(RandomStringUtils.randomAlphanumeric(4));
       object.setValue(RandomStringUtils.randomAlphanumeric(4));

        final String collectionPath = EntityHelper.INSTANCE.getCollectionPath(object);

        Assertions.assertEquals("super/" + object.getSuperParentId() + "/parent/" + object.getParentId() + "/obj" ,  collectionPath);
    }

    @Test
    void shouldSetTheIdOfEntity() {
        final String id = RandomStringUtils.randomAlphanumeric(4);
        final EntityObject object = new EntityObject();

        EntityHelper.INSTANCE.setId(object, id);

        Assertions.assertEquals(id, object.getId());
    }

    @Test
    void shouldGetTheIdOfEntity() {
        final EntityObject object = new EntityObject();
        object.setId(RandomStringUtils.randomAlphanumeric(5));

        final String id = EntityHelper.INSTANCE.getId(object);

        Assertions.assertEquals(id, object.getId());
    }

    @Test
    void shouldSetCreateTimeForEntity() {
        final EntitySuperParent object = new EntitySuperParent();

        Assertions.assertNull(object.getCreated());

        EntityHelper.INSTANCE.setCreateTime(object);

        Assertions.assertNotNull(object.getCreated());
    }

    @Test
    void shouldSetUpdateTimeForEntity() {
        final EntitySuperParent object = new EntitySuperParent();

        Assertions.assertNull(object.getUpdateTime());

        EntityHelper.INSTANCE.setUpdateTime(object);

        Assertions.assertNotNull(object.getUpdateTime());
    }

    @Test
    void shouldGetUpdateField() {
        final Field updateTimeField = EntityHelper.INSTANCE.getUpdateTimeField(EntitySuperParent.class);
        Assertions.assertEquals("updateTime", updateTimeField.getName());
    }

    @Test
    void shouldGetAllRequiredFields() {
        final Set<Field> allRequiredIdFields = EntityHelper.INSTANCE.getAllRequiredIdFields(EntityObject.class);
        final Set<String> actualFieldNames = allRequiredIdFields
                .stream()
                .map(Field::getName)
                .collect(Collectors.toSet());

        final Set<String> expected = Sets.newHashSet("id", "parentId", "superParentId");

        Assertions.assertEquals(expected, actualFieldNames);
    }

    @Test
    void shouldThrowExceptionWhenIdFieldsAreMissing() {
        final EntityObject entityObject = new EntityObject();

        Assertions.assertThrows(PersistenceException.class, () ->EntityHelper.INSTANCE.validateIdsNotNull(entityObject));
        entityObject.setId(RandomStringUtils.randomAlphanumeric(4));
        Assertions.assertThrows(PersistenceException.class, () ->EntityHelper.INSTANCE.validateIdsNotNull(entityObject));
        entityObject.setParentId(RandomStringUtils.randomAlphanumeric(4));
        Assertions.assertThrows(PersistenceException.class, () ->EntityHelper.INSTANCE.validateIdsNotNull(entityObject));
        entityObject.setSuperParentId(RandomStringUtils.randomAlphanumeric(4));
        Assertions.assertDoesNotThrow(() ->EntityHelper.INSTANCE.validateIdsNotNull(entityObject));
    }

    @Test
    void shouldThrowExceptionWhenEntityNotFound() {
        Assertions.assertThrows(EntityDeclarationException.class, () -> EntityHelper.getEntity(Object.class));
    }

    @Test
    void shouldGetMappedEntity() {
        final Entity entity = EntityHelper.getEntity(EntityObject.class);
        Assertions.assertNotNull(entity);
        Assertions.assertEquals("obj", entity.value());
    }

    @Test
    void shouldGetMappedCollection() {
        final String mappedCollection = EntityHelper.getMappedCollection(EntityObject.class);
        Assertions.assertEquals("obj", mappedCollection);
    }


    @Data
    @Entity("obj")
    private static class EntityObject {
        @Id
        private String id;
        @Parent
        @IdReference(EntityParent.class)
        private String parentId;
        @IdReference(EntitySuperParent.class)
        private String superParentId;
        private String value;
    }

    @Data
    @Entity("parent")
    private static class EntityParent {
        @Id
        private String id;
        @Parent
        @IdReference(EntitySuperParent.class)
        private String parentId;
        private String name;
    }

    @Data
    @Entity("super")
    private static class EntitySuperParent {
        @Id
        private String id;
        private String name;

        @CreateTime
        private Date created;

        @UpdateTime
        private Date updateTime;
    }

}
