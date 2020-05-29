package com.ns8.hybris.core.strategies.impl;

import com.google.common.net.HttpHeaders;
import de.hybris.bootstrap.annotations.UnitTest;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import javax.servlet.http.HttpServletRequest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@UnitTest
@RunWith(MockitoJUnitRunner.class)
public class DefaultNs8UserIpStrategyTest {

    private static final String USER_IP = "the ip of the user";
    public static final String REMOTE_ADDRESS = "remoteAddress";

    private DefaultNs8UserIpStrategy testObj = new DefaultNs8UserIpStrategy(HttpHeaders.X_FORWARDED_FOR);

    @Mock
    private HttpServletRequest requestMock;

    @Test
    public void getUserIp_WhenForwarderForExists_ShouldReturnXForwardedForHeader() {
        when(requestMock.getHeader(HttpHeaders.X_FORWARDED_FOR)).thenReturn(USER_IP);

        final String result = testObj.getUserIp(requestMock);

        assertThat(result).isEqualTo(USER_IP);
    }

    @Test
    public void getUserIp_WhenForwarderForDoesNotExist_ShouldReturnRemoteAddress() {
        when(requestMock.getHeader(HttpHeaders.X_FORWARDED_FOR)).thenReturn(null);
        when(requestMock.getRemoteAddr()).thenReturn(REMOTE_ADDRESS);

        final String result = testObj.getUserIp(requestMock);

        assertThat(result).isEqualTo(REMOTE_ADDRESS);
    }

    @Test
    public void getUserIp_WhenForwarderForUnknown_ShouldReturnRemoteAddress() {
        when(requestMock.getHeader(HttpHeaders.X_FORWARDED_FOR)).thenReturn("unknown");
        when(requestMock.getRemoteAddr()).thenReturn(REMOTE_ADDRESS);

        final String result = testObj.getUserIp(requestMock);

        assertThat(result).isEqualTo(REMOTE_ADDRESS);
    }

    @Test
    public void getUserIp_WhenForwarderForHasMultimpleValues_ShouldReturnFirstValue() {
        when(requestMock.getHeader(HttpHeaders.X_FORWARDED_FOR)).thenReturn(REMOTE_ADDRESS.concat(", address2"));
        when(requestMock.getRemoteAddr()).thenReturn(REMOTE_ADDRESS);

        final String result = testObj.getUserIp(requestMock);

        assertThat(result).isEqualTo(REMOTE_ADDRESS);
    }

}
