package com.ns8.hybris.core.converters.populators;

import com.ns8.hybris.core.data.Ns8Status;
import com.ns8.hybris.core.data.Ns8UpdateOrderStatus;
import com.ns8.hybris.core.fraud.services.Ns8FraudService;
import de.hybris.bootstrap.annotations.UnitTest;
import de.hybris.platform.core.enums.OrderStatus;
import de.hybris.platform.core.model.order.OrderModel;
import org.junit.Before;
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
public class Ns8UpdateOrderStatusPopulatorTest {

    private static final String ORDER_CODE = "orderId";
    private static final String PLATFORM_STATUS_CODE = "platformStatus";

    @InjectMocks
    private Ns8UpdateOrderStatusPopulator testObj;

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private OrderModel orderMock;
    @Mock
    private Ns8FraudService ns8FraudServiceMock;

    @Before
    public void setUp() {
        when(ns8FraudServiceMock.isOrderFraudChecked(orderMock)).thenReturn(true);
    }

    @Test
    public void populate_WhenOrderNotScored_ShouldFillTheTargetWithoutStatus() {
        when(orderMock.getCode()).thenReturn(ORDER_CODE);
        when(orderMock.getStatus().toString()).thenReturn(PLATFORM_STATUS_CODE);
        when(ns8FraudServiceMock.isOrderFraudChecked(orderMock)).thenReturn(false);

        final Ns8UpdateOrderStatus ns8UpdateOrderStatus = new Ns8UpdateOrderStatus();
        testObj.populate(orderMock, ns8UpdateOrderStatus);

        assertThat(ns8UpdateOrderStatus.getName()).isEqualTo(ORDER_CODE);
        assertThat(ns8UpdateOrderStatus.getPlatformStatus()).isEqualTo(PLATFORM_STATUS_CODE);
        assertThat(ns8UpdateOrderStatus.getStatus()).isNull();
    }

    @Test
    public void populate_WhenOrderCancelling_ShouldFillTheTargetWithStatusCancelled() {
        when(orderMock.getCode()).thenReturn(ORDER_CODE);
        when(orderMock.getStatus()).thenReturn(OrderStatus.CANCELLING);

        final Ns8UpdateOrderStatus ns8UpdateOrderStatus = new Ns8UpdateOrderStatus();
        testObj.populate(orderMock, ns8UpdateOrderStatus);

        assertThat(ns8UpdateOrderStatus.getName()).isEqualTo(ORDER_CODE);
        assertThat(ns8UpdateOrderStatus.getPlatformStatus()).isEqualTo(OrderStatus.CANCELLING.toString());
        assertThat(ns8UpdateOrderStatus.getStatus()).isEqualTo(Ns8Status.CANCELLED);
    }

    @Test
    public void populate_WhenOrderAlreadyCancelled_ShouldFillTheTargetWithStatusCancelled() {
        when(orderMock.getCode()).thenReturn(ORDER_CODE);
        when(orderMock.getStatus()).thenReturn(OrderStatus.CANCELLED);

        final Ns8UpdateOrderStatus ns8UpdateOrderStatus = new Ns8UpdateOrderStatus();
        testObj.populate(orderMock, ns8UpdateOrderStatus);

        assertThat(ns8UpdateOrderStatus.getName()).isEqualTo(ORDER_CODE);
        assertThat(ns8UpdateOrderStatus.getPlatformStatus()).isEqualTo(OrderStatus.CANCELLED.toString());
        assertThat(ns8UpdateOrderStatus.getStatus()).isEqualTo(Ns8Status.CANCELLED);
    }

    @Test
    public void populate_WhenOrderScoredAndManuallyCheckedFraudulent_ShouldFillTheTargetWithStatusCancelled() {
        when(orderMock.getCode()).thenReturn(ORDER_CODE);
        when(orderMock.getStatus().toString()).thenReturn(PLATFORM_STATUS_CODE);
        when(orderMock.getFraudulent()).thenReturn(true);

        final Ns8UpdateOrderStatus ns8UpdateOrderStatus = new Ns8UpdateOrderStatus();
        testObj.populate(orderMock, ns8UpdateOrderStatus);

        assertThat(ns8UpdateOrderStatus.getName()).isEqualTo(ORDER_CODE);
        assertThat(ns8UpdateOrderStatus.getPlatformStatus()).isEqualTo(PLATFORM_STATUS_CODE);
        assertThat(ns8UpdateOrderStatus.getStatus()).isEqualTo(Ns8Status.CANCELLED);
    }

    @Test
    public void populate_WhenOrderScoredAndManuallyCheckedApproved_ShouldFillTheTargetWithStatusApproved() {
        when(orderMock.getCode()).thenReturn(ORDER_CODE);
        when(orderMock.getStatus().toString()).thenReturn(PLATFORM_STATUS_CODE);
        when(orderMock.getFraudulent()).thenReturn(false);

        final Ns8UpdateOrderStatus ns8UpdateOrderStatus = new Ns8UpdateOrderStatus();
        testObj.populate(orderMock, ns8UpdateOrderStatus);

        assertThat(ns8UpdateOrderStatus.getName()).isEqualTo(ORDER_CODE);
        assertThat(ns8UpdateOrderStatus.getPlatformStatus()).isEqualTo(PLATFORM_STATUS_CODE);
        assertThat(ns8UpdateOrderStatus.getStatus()).isEqualTo(Ns8Status.APPROVED);
    }
}
