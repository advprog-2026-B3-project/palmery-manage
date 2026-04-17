package id.ac.ui.cs.advprog.palmerymanage.model;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

class IntegrationCheckTest {

    @Test
    void prePersistSetsCreatedAtOnlyOnceAndSourceIsMutable() {
        IntegrationCheck check = new IntegrationCheck("frontend-debug");
        assertEquals("frontend-debug", check.getSource());
        assertNull(check.getCreatedAt());

        check.prePersist();
        Instant createdAt = check.getCreatedAt();
        assertNotNull(createdAt);

        check.prePersist();
        assertEquals(createdAt, check.getCreatedAt());

        check.setSource("manual");
        assertEquals("manual", check.getSource());
    }

    @Test
    void defaultConstructorIsAccessibleForJpa() throws Exception {
        Constructor<IntegrationCheck> constructor = IntegrationCheck.class.getDeclaredConstructor();
        constructor.setAccessible(true);

        IntegrationCheck check = constructor.newInstance();

        assertNull(check.getSource());
        assertNull(check.getCreatedAt());
        assertNull(check.getId());
    }
}
