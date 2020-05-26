package com.ns8.hybris.core.converters.populators;

import com.ns8.hybris.core.data.Ns8SessionData;
import de.hybris.bootstrap.annotations.UnitTest;
import de.hybris.platform.core.model.order.OrderModel;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@UnitTest
@RunWith(MockitoJUnitRunner.class)
public class DefaultNs8SessionDataPopulatorTest {

    private static final String CUSTOMER_IP = "customer ip";
    private static final String USER_AGENT = "user agent";
    private static final String LANGUAGE_VALUE_FROM_SESSION = "en";
    private static final Long WIDTH_VALUE = 2000L;
    private static final Long HEIGHT_VALUE = 1700L;

    private DefaultNs8SessionDataPopulator testObj = new DefaultNs8SessionDataPopulator();

    @Mock
    private OrderModel orderMock;

    private Ns8SessionData target;

    @Before
    public void setUp() {
        target = new Ns8SessionData();
        when(orderMock.getCustomerIp()).thenReturn(CUSTOMER_IP);
        when(orderMock.getCustomerUserAgent()).thenReturn(USER_AGENT);
        when(orderMock.getAcceptLanguage()).thenReturn(LANGUAGE_VALUE_FROM_SESSION);
        when(orderMock.getScreenHeight()).thenReturn(HEIGHT_VALUE);
        when(orderMock.getScreenWidth()).thenReturn(WIDTH_VALUE);
    }

    @Test
    public void populate_ShouldPopulateTheTargetProperly() {
        testObj.populate(orderMock, target);

        assertThat(target.getIp()).isEqualTo(CUSTOMER_IP);
        assertThat(target.getUserAgent()).isEqualTo(USER_AGENT);
        assertThat(target.getAcceptLanguage()).isEqualTo(LANGUAGE_VALUE_FROM_SESSION);
        assertThat(target.getScreenHeight()).isEqualTo(HEIGHT_VALUE);
        assertThat(target.getScreenWidth()).isEqualTo(WIDTH_VALUE);
    }
}
