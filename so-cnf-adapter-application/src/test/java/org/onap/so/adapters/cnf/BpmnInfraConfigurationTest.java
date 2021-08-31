package org.onap.so.adapters.cnf;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.Assert.*;

public class BpmnInfraConfigurationTest {

    private BpmnInfraConfiguration tested = new BpmnInfraConfiguration();
    private final String name = "Basic";
    private final String value = "123123123";

    @Before
    public void setUp() {
        ReflectionTestUtils.setField(tested, "auth", name + " " + value);
    }

    @Test
    public void shouldTestName() {
        Assert.assertEquals(name, tested.getHeaderName());
    }

    @Test
    public void shouldTestValue() {
        Assert.assertEquals(value, tested.getHeaderValue());
    }

    @Test(expected = RuntimeException.class)
    public void shouldThrowExceptionWhenAuthEmpty() {
        ReflectionTestUtils.setField(tested, "auth", "");
        tested.getHeaderName();
    }
}