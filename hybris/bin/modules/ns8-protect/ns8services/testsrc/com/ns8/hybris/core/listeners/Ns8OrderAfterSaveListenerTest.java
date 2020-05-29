package com.ns8.hybris.core.listeners;

import com.ns8.hybris.core.merchant.services.Ns8MerchantService;
import com.ns8.hybris.core.model.NS8MerchantModel;
import com.ns8.hybris.core.services.api.Ns8ApiService;
import de.hybris.bootstrap.annotations.UnitTest;
import de.hybris.platform.core.PK;
import de.hybris.platform.core.enums.OrderStatus;
import de.hybris.platform.core.model.order.OrderModel;
import de.hybris.platform.servicelayer.model.ModelService;
import de.hybris.platform.tx.AfterSaveEvent;
import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.*;

import java.util.Collections;
import java.util.List;

import static org.mockito.Mockito.*;

@UnitTest
@RunWith(JUnitParamsRunner.class)
public class Ns8OrderAfterSaveListenerTest {

    private static final int ORDER_MODEL_TYPE_CODE = 45;
    private static final int OTHER_MODEL_TYPE_CODE = 123;

    @Spy
    @InjectMocks
    private Ns8OrderAfterSaveListener testObj;

    @Mock
    private ModelService modelService;
    @Mock
    private Ns8ApiService ns8ApiService;
    @Mock
    private Ns8MerchantService ns8MerchantServiceMock;
    @Mock
    private AfterSaveEvent afterSaveEventMock;
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private OrderModel orderMock;
    @Mock
    private NS8MerchantModel ns8MerchantMock;

    private PK orderPK;
    private List<AfterSaveEvent> eventList;

    private Object[] parameters() {
        return new Object[]{
                // Currently accepted cases: Type code OrderModel (45), on update and one of the following statuses
                // CANCELLED, COMPLETED, PAYMENT_NOT_CAPTURED, PAYMENT_CAPTURED, CANCELLING
                new Object[]{ORDER_MODEL_TYPE_CODE, AfterSaveEvent.UPDATE, OrderStatus.CANCELLED, 1},
                new Object[]{ORDER_MODEL_TYPE_CODE, AfterSaveEvent.UPDATE, OrderStatus.COMPLETED, 1},
                new Object[]{ORDER_MODEL_TYPE_CODE, AfterSaveEvent.UPDATE, OrderStatus.PAYMENT_NOT_CAPTURED, 1},
                new Object[]{ORDER_MODEL_TYPE_CODE, AfterSaveEvent.UPDATE, OrderStatus.PAYMENT_CAPTURED, 1},
                new Object[]{ORDER_MODEL_TYPE_CODE, AfterSaveEvent.UPDATE, OrderStatus.CANCELLING, 1},
                // Invalid cases: Order code != 45, save events != UPDATE, invalid status
                new Object[]{ORDER_MODEL_TYPE_CODE, AfterSaveEvent.CREATE, OrderStatus.CHECKED_VALID, 0},
                new Object[]{ORDER_MODEL_TYPE_CODE, AfterSaveEvent.CREATE, OrderStatus.COMPLETED, 0},
                new Object[]{OTHER_MODEL_TYPE_CODE, AfterSaveEvent.UPDATE, OrderStatus.COMPLETED, 0}
        };
    }

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        final List<OrderStatus> orderStatuses = List.of(OrderStatus.CANCELLED, OrderStatus.COMPLETED,
                OrderStatus.PAYMENT_NOT_CAPTURED, OrderStatus.PAYMENT_CAPTURED,
                OrderStatus.CANCELLING);
        orderPK = PK.fromLong(1234L);
        doReturn(orderStatuses).when(testObj).getOrderStatuses();
        eventList = Collections.singletonList(afterSaveEventMock);
        when(orderMock.getSite().getNs8Merchant()).thenReturn(ns8MerchantMock);
        when(ns8MerchantServiceMock.isMerchantActive(ns8MerchantMock)).thenReturn(Boolean.TRUE);
    }

    @Test
    @Parameters(method = "parameters")
    public void afterSave_givenAfterSaveEvent_shouldCallAPIAccordingly(final int modelTypeCode, final int afterSaveEventType, final OrderStatus orderStatus, final int expectedNumberOfCalls) {
        when(afterSaveEventMock.getType()).thenReturn(afterSaveEventType);
        when(afterSaveEventMock.getPk()).thenReturn(orderPK);
        when(modelService.get(orderPK)).thenReturn(orderMock);
        when(orderMock.getStatus()).thenReturn(orderStatus);

        doReturn(modelTypeCode).when(testObj).getPkTypecode(orderPK);

        testObj.afterSave(eventList);

        verify(ns8ApiService, times(expectedNumberOfCalls)).triggerUpdateOrderStatusAction(orderMock);
    }

    @Test
    public void afterSave_WhenMerchantIsDisabled_shouldNotCallAPIAccordingly() {
        when(afterSaveEventMock.getType()).thenReturn(AfterSaveEvent.UPDATE);
        when(afterSaveEventMock.getPk()).thenReturn(orderPK);
        when(modelService.get(orderPK)).thenReturn(orderMock);
        when(orderMock.getStatus()).thenReturn(OrderStatus.CANCELLED);
        when(ns8MerchantServiceMock.isMerchantActive(ns8MerchantMock)).thenReturn(Boolean.FALSE);
        doReturn(ORDER_MODEL_TYPE_CODE).when(testObj).getPkTypecode(orderPK);

        testObj.afterSave(eventList);

        verifyZeroInteractions(ns8ApiService);
    }
}
