package com.ns8.hybris.core.listeners;

import com.google.common.collect.ImmutableList;
import com.ns8.hybris.core.merchant.services.Ns8MerchantService;
import com.ns8.hybris.core.model.NS8MerchantModel;
import com.ns8.hybris.core.services.api.Ns8ApiService;
import de.hybris.bootstrap.annotations.UnitTest;
import de.hybris.platform.basecommerce.model.site.BaseSiteModel;
import de.hybris.platform.core.PK;
import de.hybris.platform.core.model.order.OrderModel;
import de.hybris.platform.servicelayer.model.ModelService;
import de.hybris.platform.tx.AfterSaveEvent;
import junitparams.JUnitParamsRunner;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.mockito.Mockito.*;

@UnitTest
@RunWith(JUnitParamsRunner.class)
public class Ns8OrderAfterSaveListenerTest {

    @InjectMocks
    private Ns8OrderAfterSaveListener testObj;

    @Mock
    private ModelService modelService;
    @Mock
    private Ns8ApiService ns8ApiService;
    @Mock
    private Ns8MerchantService ns8MerchantServiceMock;
    @Mock
    private OrderModel orderMock, originalOrderMock;
    @Mock
    private BaseSiteModel baseSiteMock;
    @Mock
    private NS8MerchantModel ns8MerchantMock;

    private AfterSaveEvent afterSaveOrderEvent, afterSaveOtherEvent;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);

        final PK orderPK = PK.fromLong(8796094136365L);
        afterSaveOrderEvent = new AfterSaveEvent(orderPK, AfterSaveEvent.UPDATE);
        afterSaveOtherEvent = new AfterSaveEvent(PK.fromLong(8796094398468L), AfterSaveEvent.UPDATE);

        when(modelService.get(orderPK)).thenReturn(orderMock);
        when(orderMock.getSite()).thenReturn(baseSiteMock);
        when(baseSiteMock.getNs8Merchant()).thenReturn(ns8MerchantMock);
        when(ns8MerchantServiceMock.isMerchantActive(ns8MerchantMock)).thenReturn(Boolean.TRUE);
        when(orderMock.getSendOrderToNs8()).thenReturn(Boolean.TRUE);
    }

    @Test
    public void afterSave_WhenOrderHasSendOrderFlagTrueAndMerchantIsActive_ShouldSendOrderToNs8() {
        testObj.afterSave(ImmutableList.of(afterSaveOrderEvent));

        verify(ns8ApiService).triggerUpdateOrderStatusAction(orderMock);
    }

    @Test
    public void afterSave_WhenOrderHasSendOrderFlagFalseAndMerchantIsActive_ShouldNotSendOrderToNs8() {
        when(orderMock.getSendOrderToNs8()).thenReturn(Boolean.FALSE);

        testObj.afterSave(ImmutableList.of(afterSaveOrderEvent));

        verifyZeroInteractions(ns8ApiService);
    }

    @Test
    public void afterSave_WhenMerchantIsDisabled_ShouldNotSendOrderToNs8() {
        when(ns8MerchantServiceMock.isMerchantActive(ns8MerchantMock)).thenReturn(Boolean.FALSE);

        testObj.afterSave(ImmutableList.of(afterSaveOrderEvent));

        verifyZeroInteractions(ns8ApiService);
    }

    @Test
    public void afterSave_WhenNotOrderModel_ShouldDoNothing() {
        testObj.afterSave(ImmutableList.of(afterSaveOtherEvent));

        verifyZeroInteractions(ns8ApiService);
    }

    @Test
    public void afterSave_WhenOrderModelHasVersion_ShouldDoNothing() {
        when(orderMock.getOriginalVersion()).thenReturn(originalOrderMock);

        testObj.afterSave(ImmutableList.of(afterSaveOtherEvent));

        verifyZeroInteractions(ns8ApiService);
    }
}
