package in.kuros.jfirebase.entity;

import in.kuros.jfirebase.entity.EntityParentCache.FieldCollectionMapping;
import lombok.Data;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class EntityParentCacheTest {

    @Test
    void shouldFindNoMappedClassFieldForNoParentMapping() {
        final List<FieldCollectionMapping> mappedClassFields = EntityParentCache.INSTANCE.getFieldCollectionMappings(WithNoParent.class);

        assertEquals(0, mappedClassFields.size());
    }

    @Test
    void shouldFindParentByClass() {
        final List<FieldCollectionMapping> mappedClassFields = EntityParentCache.INSTANCE.getFieldCollectionMappings(WithParentByClass.class);

        assertEquals(1, mappedClassFields.size());
        assertEquals("parent", mappedClassFields.get(0).getField());
        assertEquals(WithNoParent.class, mappedClassFields.get(0).getMappedClass());
    }

    @Test
    void shouldFindParentByIdReference() {
        final List<FieldCollectionMapping> mappedClassFields = EntityParentCache.INSTANCE.getFieldCollectionMappings(WithIdReference.class);

        assertEquals(2, mappedClassFields.size());
        assertEquals("superParent", mappedClassFields.get(0).getField());
        assertEquals(WithNoParent.class, mappedClassFields.get(0).getMappedClass());
        assertEquals("parent", mappedClassFields.get(1).getField());
        assertEquals(WithParentByClass.class, mappedClassFields.get(1).getMappedClass());
    }

    @Test
    void shouldFindParentByCollection() {
        final List<FieldCollectionMapping> mappedClassFields = EntityParentCache.INSTANCE.getFieldCollectionMappings(WithParentByCollection.class);

        assertEquals(1, mappedClassFields.size());
        assertEquals("parent", mappedClassFields.get(0).getField());
        assertEquals(IdReference.DEFAULT.class, mappedClassFields.get(0).getMappedClass());
        assertEquals("some-collection", mappedClassFields.get(0).getCollection());
    }

    @Test
    void shouldFindParentByParentCollection() {
        final List<FieldCollectionMapping> mappedClassFields = EntityParentCache.INSTANCE.getFieldCollectionMappings(WithParentByCollectionAndParentCollection.class);

        assertEquals(2, mappedClassFields.size());
        assertEquals("superParent", mappedClassFields.get(0).getField());
        assertEquals(IdReference.DEFAULT.class, mappedClassFields.get(0).getMappedClass());
        assertEquals("parent-collection", mappedClassFields.get(0).getCollection());
        assertEquals("parent", mappedClassFields.get(1).getField());
        assertEquals(IdReference.DEFAULT.class, mappedClassFields.get(1).getMappedClass());
        assertEquals("some-collection", mappedClassFields.get(1).getCollection());
    }

    @Test
    void shouldFindParentByParentCollectionClassMapping() {
        final List<FieldCollectionMapping> mappedClassFields = EntityParentCache.INSTANCE.getFieldCollectionMappings(WithParentByCollectionAndParentCollectionMappedByClass.class);

        assertEquals(2, mappedClassFields.size());
        assertEquals("superParent", mappedClassFields.get(0).getField());
        assertEquals(RefParent.class, mappedClassFields.get(0).getMappedClass());
        assertEquals("", mappedClassFields.get(0).getCollection());
        assertEquals("parent", mappedClassFields.get(1).getField());
        assertEquals(IdReference.DEFAULT.class, mappedClassFields.get(1).getMappedClass());
        assertEquals("some-collection", mappedClassFields.get(1).getCollection());
    }



    @Data
    @Entity("no-parent")
    private static class WithNoParent {
        @Id
        private String id;
    }

    @Data
    @Entity("with-parent")
    private static class WithParentByClass {
        @Id
        private String id;
        @Parent(value = WithNoParent.class)
        private String parent;
    }

    @Data
    @Entity("with-id-reference")
    private static class WithIdReference {
        @Id
        private String id;
        @Parent(WithParentByClass.class)
        private String parent;
        @IdReference(WithNoParent.class)
        private String superParent;
    }

    @Data
    @Entity("with-parent")
    private static class WithParentByCollection {
        @Id
        private String id;
        @Parent(collection = "some-collection")
        private String parent;
    }

    @Data
    @Entity("with-parent")
    private static class WithParentByCollectionAndParentCollection {
        @Id
        private String id;
        @Parent(collection = "some-collection", collectionParent = @CollectionParent(collection = "parent-collection"))
        private String parent;
        @IdReference(collection = "parent-collection")
        private String superParent;
    }

    @Data
    @Entity("ref-parent")
    private static class RefParent {
        @Id
        private String id;
    }

    @Data
    @Entity("with-parent")
    private static class WithParentByCollectionAndParentCollectionMappedByClass {
        @Id
        private String id;
        @Parent(collection = "some-collection", collectionParent = @CollectionParent(RefParent.class))
        private String parent;
        @IdReference(RefParent.class)
        private String superParent;
    }
}