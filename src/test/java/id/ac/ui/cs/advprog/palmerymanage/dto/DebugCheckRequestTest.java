package id.ac.ui.cs.advprog.palmerymanage.dto;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class DebugCheckRequestTest {

    @Test
    void sourceGetterAndSetterWork() {
        DebugCheckRequest request = new DebugCheckRequest();
        assertNull(request.getSource());

        request.setSource("frontend-debug");
        assertEquals("frontend-debug", request.getSource());
    }
}
