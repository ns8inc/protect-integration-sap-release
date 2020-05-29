package com.ns8.hybris.backoffice.widgets.controllers.ns8showorder;

import com.ns8.hybris.core.model.NS8MerchantModel;
import com.ns8.hybris.core.services.api.Ns8EndpointService;
import de.hybris.bootstrap.annotations.UnitTest;
import de.hybris.platform.basecommerce.model.site.BaseSiteModel;
import de.hybris.platform.core.model.order.OrderModel;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.internal.util.reflection.Whitebox;
import org.mockito.runners.MockitoJUnitRunner;
import org.zkoss.zul.Textbox;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@UnitTest
@RunWith(MockitoJUnitRunner.class)
public class Ns8ShowOrderControllerTest {

    private static final String ORDER_CODE = "orderCode";
    private static final String API_KEY = "apiKey";
    private static final String CLIENT_API_URL = "https://test.com";

    @Spy
    @InjectMocks
    private Ns8ShowOrderController testObj;

    @Mock
    private Ns8EndpointService ns8EndpointServiceMock;
    @Mock
    private OrderModel orderMock;
    @Mock
    private BaseSiteModel baseSiteMock;
    @Mock
    private NS8MerchantModel ns8merchantMock;
    @Mock
    private Textbox orderNumberMock;
    @Mock
    private Textbox accessTokenMock;

    @Before
    public void setUp() {
        Whitebox.setInternalState(testObj, "orderNumber", orderNumberMock);
        Whitebox.setInternalState(testObj, "accessToken", accessTokenMock);
        Whitebox.setInternalState(testObj, "orderModel", orderMock);

        when(orderMock.getSite()).thenReturn(baseSiteMock);
        when(baseSiteMock.getNs8Merchant()).thenReturn(ns8merchantMock);
        when(ns8merchantMock.getEnabled()).thenReturn(true);
        when(ns8merchantMock.getApiKey()).thenReturn(API_KEY);
    }

    @Test
    public void getClientApiUrl_ShouldReturnClientApiUrl() {
        when(ns8EndpointServiceMock.getBaseClientURL()).thenReturn(CLIENT_API_URL);

        final String result = testObj.getClientApiUrl();

        assertThat(result).isEqualTo(CLIENT_API_URL);
    }

    @Test
    public void getMerchantApiKey_ShouldReturnMerchantKey() {
        final String result = testObj.getMerchantApiKey();

        assertThat(result).isEqualTo(API_KEY);
    }

    @Test
    public void showCurrentOrder_ShouldSetTheInputOrder() {
        when(orderMock.getCode()).thenReturn(ORDER_CODE);
        when(testObj.getMerchantApiKey()).thenReturn(API_KEY);

        testObj.showCurrentOrder(orderMock);

        verify(testObj).setOrderModel(orderMock);
        verify(orderNumberMock).setValue(ORDER_CODE);
        verify(accessTokenMock).setValue(API_KEY);
    }
}
