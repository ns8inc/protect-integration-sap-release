package com.ns8.hybris.core.order.interceptors;

import de.hybris.bootstrap.annotations.UnitTest;
import de.hybris.platform.core.enums.OrderStatus;
import de.hybris.platform.core.model.order.OrderModel;
import de.hybris.platform.servicelayer.interceptor.InterceptorContext;
import de.hybris.platform.servicelayer.interceptor.InterceptorException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.internal.util.reflection.Whitebox;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static de.hybris.platform.core.enums.OrderStatus.*;
import static org.mockito.Mockito.*;

@UnitTest
@RunWith(MockitoJUnitRunner.class)
public class Ns8SendOrderToNs8PrepareInterceptorTest {


    @InjectMocks
    private Ns8SendOrderToNs8PrepareInterceptor testObj;

    @Mock
    private InterceptorContext contextMock;
    @Mock
    private OrderModel orderMock;

    private List<OrderStatus> orderStatuses;

    @Before
    public void setUp() {
        when(contextMock.isNew(orderMock)).thenReturn(Boolean.FALSE);
        when(contextMock.isModified(orderMock)).thenReturn(Boolean.TRUE);
        when(contextMock.getDirtyAttributes(orderMock)).thenReturn(Map.of(OrderModel.STATUS, Collections.emptySet()));
        orderStatuses = List.of(
                CANCELLED,
                COMPLETED,
                PAYMENT_NOT_CAPTURED,
                PAYMENT_CAPTURED,
                CANCELLING);
        Whitebox.setInternalState(testObj, "orderStatuses", orderStatuses);
        when(orderMock.getStatus()).thenReturn(CANCELLED);
    }

    @Test
    public void onPrepare_WhenOrderStatusChangedAndInTheList_ShouldSetFlagTrue() throws InterceptorException {
        testObj.onPrepare(orderMock, contextMock);

        verify(orderMock).setSendOrderToNs8(Boolean.TRUE);
    }

    @Test
    public void onPrepare_WhenOrderStatusChangedAndNotInTheList_ShouldDoNothing() throws InterceptorException {
        when(orderMock.getStatus()).thenReturn(PAYMENT_AMOUNT_NOT_RESERVED);

        testObj.onPrepare(orderMock, contextMock);

        verify(orderMock, never()).setSendOrderToNs8(anyBoolean());
    }

    @Test
    public void onPrepare_WhenOrderStatusNotChanged_ShouldDoNothing() throws InterceptorException {
        when(contextMock.getDirtyAttributes(orderMock)).thenReturn(Map.of(OrderModel.NAME, Collections.emptySet()));

        testObj.onPrepare(orderMock, contextMock);

        verify(orderMock, never()).setSendOrderToNs8(anyBoolean());
    }

    @Test
    public void onPrepare_WhenOrderIsNew_ShouldDoNothing() throws InterceptorException {
        when(contextMock.isNew(orderMock)).thenReturn(Boolean.TRUE);

        testObj.onPrepare(orderMock, contextMock);

        verify(orderMock, never()).setSendOrderToNs8(anyBoolean());
    }
}
