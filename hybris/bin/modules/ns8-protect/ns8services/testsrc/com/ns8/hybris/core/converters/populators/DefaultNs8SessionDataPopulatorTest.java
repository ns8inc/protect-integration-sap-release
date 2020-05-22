package com.ns8.hybris.core.converters.populators;

import com.ns8.hybris.core.data.NS8SessionData;
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

    private DefaultNs8SessionDataPopulator testObj = new DefaultNs8SessionDataPopulator();

    @Mock
    private OrderModel orderMock;

    private NS8SessionData target;

    @Before
    public void setUp() throws Exception {
        target = new NS8SessionData();
        when(orderMock.getCustomerIp()).thenReturn(CUSTOMER_IP);
        when(orderMock.getCustomerUserAgent()).thenReturn(USER_AGENT);
    }

    @Test
    public void populate() {

        testObj.populate(orderMock, target);

        assertThat(target.getIp()).isEqualTo(CUSTOMER_IP);
        assertThat(target.getUserAgent()).isEqualTo(USER_AGENT);
        assertThat(target.getAcceptLanguage()).isNullOrEmpty();
        assertThat(target.getScreenHeight()).isNull();
        assertThat(target.getScreenWidth()).isNull();
    }
}
