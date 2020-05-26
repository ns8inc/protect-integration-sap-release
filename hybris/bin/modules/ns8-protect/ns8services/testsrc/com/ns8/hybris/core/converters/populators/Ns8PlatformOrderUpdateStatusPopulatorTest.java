package com.ns8.hybris.core.converters.populators;

import com.ns8.hybris.core.data.Ns8PlatformOrderUpdateStatus;
import de.hybris.bootstrap.annotations.UnitTest;
import de.hybris.platform.core.model.order.OrderModel;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@UnitTest
@RunWith(MockitoJUnitRunner.class)
public class Ns8PlatformOrderUpdateStatusPopulatorTest {

    private static final String ORDER_CODE = "orderId";
    private static final String PLATFORM_STATUS_CODE = "platformStatus";

    @InjectMocks
    private Ns8PlatformOrderUpdateStatusPopulator testObj;

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private OrderModel orderMock;

    @Test
    public void populate_shouldFillOrderUpdateStatus() {
        when(orderMock.getCode()).thenReturn(ORDER_CODE);
        when(orderMock.getStatus().toString()).thenReturn(PLATFORM_STATUS_CODE);

        final Ns8PlatformOrderUpdateStatus ns8PlatformOrderUpdateStatus = new Ns8PlatformOrderUpdateStatus();
        testObj.populate(orderMock, ns8PlatformOrderUpdateStatus);

        assertThat(ns8PlatformOrderUpdateStatus.getName()).isEqualTo(ORDER_CODE);
        assertThat(ns8PlatformOrderUpdateStatus.getPlatformStatus()).isEqualTo(PLATFORM_STATUS_CODE);
    }
}
