package org.onap.so.adapters.cnf;

import org.junit.Test;

import static org.junit.Assert.*;

public class AaiConfigurationTest {

    private AaiConfiguration tested = new AaiConfiguration();

    @Test
    public void shouldReturnDefaultFalse() {
        boolean actual = tested.isEnabled();
        assertFalse(actual);
    }
}