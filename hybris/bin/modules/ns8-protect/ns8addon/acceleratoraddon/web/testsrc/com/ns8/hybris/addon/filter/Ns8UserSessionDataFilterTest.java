package com.ns8.hybris.addon.filter;

import com.google.common.net.HttpHeaders;
import com.ns8.hybris.core.constants.Ns8servicesConstants;
import com.ns8.hybris.core.strategies.Ns8UserIpStrategy;
import de.hybris.bootstrap.annotations.UnitTest;
import de.hybris.platform.servicelayer.session.SessionService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import javax.servlet.FilterChain;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@UnitTest
@RunWith(MockitoJUnitRunner.class)
public class Ns8UserSessionDataFilterTest {

    private static final String USER_IP = "user ip";
    private static final String USER_AGENT = "user agent";
    private static final Long WIDTH_VALUE = 2000L;
    private static final Long HEIGHT_VALUE = 1700L;
    private static final String ACCEPT_LANGUAGE_HEADER_VALUE = "acceptLanguageHeaderValue";

    @InjectMocks
    private Ns8UserSessionDataFilter testObj;

    @Mock
    private SessionService sessionServiceMock;
    @Mock
    private Ns8UserIpStrategy ns8UserIpStrategyMock;

    @Mock
    private FilterChain filterChainMock;
    @Mock
    private HttpServletRequest requestMock;
    @Mock
    private HttpServletResponse responseMock;

    @Test
    public void doFilterInternal_ShouldSetUserIpAndAgentToSession() throws Exception {
        when(ns8UserIpStrategyMock.getUserIp(requestMock)).thenReturn(USER_IP);
        when(requestMock.getHeader(HttpHeaders.USER_AGENT)).thenReturn(USER_AGENT);
        when(requestMock.getHeader(HttpHeaders.ACCEPT_LANGUAGE)).thenReturn(ACCEPT_LANGUAGE_HEADER_VALUE);
        when(requestMock.getHeader(Ns8servicesConstants.SCREEN_HEIGHT_SESSION_ATTR)).thenReturn(String.valueOf(HEIGHT_VALUE));
        when(requestMock.getHeader(Ns8servicesConstants.SCREEN_WIDTH_SESSION_ATTR)).thenReturn(String.valueOf(WIDTH_VALUE));

        testObj.doFilterInternal(requestMock, responseMock, filterChainMock);

        verify(sessionServiceMock).setAttribute(Ns8servicesConstants.USER_IP_SESSION_ATTR, USER_IP);
        verify(sessionServiceMock).setAttribute(Ns8servicesConstants.ACCEPT_LANGUAGE_SESSION_ATTR, ACCEPT_LANGUAGE_HEADER_VALUE);
        verify(sessionServiceMock).setAttribute(Ns8servicesConstants.SCREEN_HEIGHT_SESSION_ATTR, HEIGHT_VALUE);
        verify(sessionServiceMock).setAttribute(Ns8servicesConstants.SCREEN_WIDTH_SESSION_ATTR, WIDTH_VALUE);
        verify(sessionServiceMock).setAttribute(Ns8servicesConstants.USER_AGENT_SESSION_ATTR, USER_AGENT);
        verify(filterChainMock).doFilter(requestMock, responseMock);
    }

}
