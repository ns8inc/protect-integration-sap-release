package com.ns8.hybris.core.services.impl;

import de.hybris.bootstrap.annotations.UnitTest;
import de.hybris.platform.core.model.order.OrderModel;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Java6Assertions.assertThat;
import static org.mockito.Mockito.when;

@UnitTest
@RunWith(MockitoJUnitRunner.class)
public class DefaultNS8FraudServiceTest {

    @InjectMocks
    private DefaultNS8FraudService testObj;

    @Mock
    private OrderModel orderMock;

    @Before
    public void setUp() {
        when(orderMock.getRiskEventPayload()).thenReturn("{payload : ''}");
    }

    @Test(expected = IllegalArgumentException.class)
    public void hasOrderBeenScored_WhenOrderNull_ShouldThrowException() {
        testObj.hasOrderBeenScored(null);
    }

    @Test
    public void hasOrderBeenScored_WhenOrderHasPayload_ShouldReturnTrue() {
        final boolean result = testObj.hasOrderBeenScored(orderMock);

        assertThat(result).isTrue();
    }

    @Test
    public void hasOrderBeenScored_WhenOrderHasNotPayload_ShouldReturnFalse() {
        when(orderMock.getRiskEventPayload()).thenReturn(null);

        final boolean result = testObj.hasOrderBeenScored(orderMock);

        assertThat(result).isFalse();
    }
}
