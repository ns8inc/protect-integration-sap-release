package com.ns8.hybris.core.strategies.impl;

import com.google.common.net.HttpHeaders;
import de.hybris.bootstrap.annotations.UnitTest;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import javax.servlet.http.HttpServletRequest;

import static org.assertj.core.api.Assertions.assertThat;

@UnitTest
@RunWith(MockitoJUnitRunner.class)
public class DefaultNs8UserIpStrategyTest {

    private static final String USER_IP = "the ip of the user";

    private DefaultNs8UserIpStrategy testObj = new DefaultNs8UserIpStrategy();

    @Mock
    private HttpServletRequest requestMock;

    @Test
    public void getUserIp_ShouldReturnXForwardedForHeader() {
        Mockito.when(requestMock.getHeader(HttpHeaders.X_FORWARDED_FOR)).thenReturn(USER_IP);

        final String result = testObj.getUserIp(requestMock);

        assertThat(result).isEqualTo(USER_IP);
    }

}
