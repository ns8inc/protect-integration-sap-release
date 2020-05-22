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

import static org.mockito.Mockito.*;

@UnitTest
@RunWith(MockitoJUnitRunner.class)
public class UserSessionDataFilterTest {

    private static final String USER_IP = "user ip";
    private static final String USER_AGENT = "user agent";

    @InjectMocks
    private UserSessionDataFilter testObj;

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

        testObj.doFilterInternal(requestMock, responseMock, filterChainMock);

        verify(sessionServiceMock).setAttribute(Ns8servicesConstants.USER_IP_SESSION_ATTR, USER_IP);
        verify(sessionServiceMock).setAttribute(Ns8servicesConstants.USER_AGENT_SESSION_ATTR, USER_AGENT);
        verify(filterChainMock).doFilter(requestMock, responseMock);
    }

}
