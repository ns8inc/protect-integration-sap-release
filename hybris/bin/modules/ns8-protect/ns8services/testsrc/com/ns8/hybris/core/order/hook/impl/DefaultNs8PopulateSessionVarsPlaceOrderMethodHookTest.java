package com.ns8.hybris.core.order.hook.impl;

import com.ns8.hybris.core.constants.Ns8servicesConstants;
import de.hybris.bootstrap.annotations.UnitTest;
import de.hybris.platform.commerceservices.service.data.CommerceCheckoutParameter;
import de.hybris.platform.commerceservices.service.data.CommerceOrderResult;
import de.hybris.platform.core.model.order.OrderModel;
import de.hybris.platform.servicelayer.model.ModelService;
import de.hybris.platform.servicelayer.session.SessionService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.Mockito.*;

@UnitTest
@RunWith(MockitoJUnitRunner.class)
public class DefaultNs8PopulateSessionVarsPlaceOrderMethodHookTest {

    private static final String CUSTOMER_IP_FROM_SESSION = "customer ip from session";
    private static final String USER_AGENT_FROM_SESSION = "user agent from session";

    @InjectMocks
    private DefaultNs8PopulateSessionVarsPlaceOrderMethodHook testObj;

    @Mock
    private SessionService sessionServiceMock;
    @Mock
    private ModelService modelServiceMock;

    @Mock
    private CommerceOrderResult commerceOrderResultMock;
    @Mock
    private CommerceCheckoutParameter commerceCheckoutParameterMock;
    @Mock
    private OrderModel orderMock;

    @Before
    public void setUp() {
        when(commerceOrderResultMock.getOrder()).thenReturn(orderMock);
        when(sessionServiceMock.getAttribute(Ns8servicesConstants.USER_IP_SESSION_ATTR)).thenReturn(CUSTOMER_IP_FROM_SESSION);
        when(sessionServiceMock.getAttribute(Ns8servicesConstants.USER_AGENT_SESSION_ATTR)).thenReturn(USER_AGENT_FROM_SESSION);
    }

    @Test
    public void beforeSubmitOrder_shouldPopulateIpAndAgentFromSession() {
        testObj.beforeSubmitOrder(commerceCheckoutParameterMock, commerceOrderResultMock);

        verify(orderMock).setCustomerIp(CUSTOMER_IP_FROM_SESSION);
        verify(orderMock).setCustomerUserAgent(USER_AGENT_FROM_SESSION);
        verify(modelServiceMock).save(orderMock);
    }

    @Test
    public void afterPlaceOrder_ShouldDoNothing() {
        testObj.afterPlaceOrder(commerceCheckoutParameterMock, commerceOrderResultMock);

        verifyZeroInteractions(commerceCheckoutParameterMock, commerceOrderResultMock, sessionServiceMock, modelServiceMock);
    }

    @Test
    public void beforePlaceOrder_ShouldDoNothing() {
        testObj.beforePlaceOrder(commerceCheckoutParameterMock);

        verifyZeroInteractions(commerceCheckoutParameterMock, commerceOrderResultMock, sessionServiceMock, modelServiceMock);
    }
}
