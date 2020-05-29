package com.ns8.hybris.addon.filter;

import com.google.common.net.HttpHeaders;
import com.ns8.hybris.core.constants.Ns8servicesConstants;
import com.ns8.hybris.core.strategies.Ns8UserIpStrategy;
import de.hybris.platform.servicelayer.session.SessionService;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Filter to add user ip and user-agent to the session
 */
public class Ns8UserSessionDataFilter extends OncePerRequestFilter {

    protected final SessionService sessionService;
    protected final Ns8UserIpStrategy ns8UserIpStrategy;

    public Ns8UserSessionDataFilter(final SessionService sessionService, final Ns8UserIpStrategy ns8UserIpStrategy) {
        this.sessionService = sessionService;
        this.ns8UserIpStrategy = ns8UserIpStrategy;
    }

    @Override
    protected void doFilterInternal(final HttpServletRequest request, final HttpServletResponse response, final FilterChain filterChain) throws ServletException, IOException {
        sessionService.setAttribute(Ns8servicesConstants.USER_AGENT_SESSION_ATTR, request.getHeader(HttpHeaders.USER_AGENT));
        sessionService.setAttribute(Ns8servicesConstants.ACCEPT_LANGUAGE_SESSION_ATTR, request.getHeader(HttpHeaders.ACCEPT_LANGUAGE));
        sessionService.setAttribute(Ns8servicesConstants.USER_IP_SESSION_ATTR, ns8UserIpStrategy.getUserIp(request));

        if(request.getHeader(Ns8servicesConstants.SCREEN_HEIGHT_SESSION_ATTR) != null) {
            sessionService.setAttribute(Ns8servicesConstants.SCREEN_HEIGHT_SESSION_ATTR, Long.valueOf(request.getHeader(Ns8servicesConstants.SCREEN_HEIGHT_SESSION_ATTR)));
        }
        if(request.getHeader(Ns8servicesConstants.SCREEN_WIDTH_SESSION_ATTR) != null) {
            sessionService.setAttribute(Ns8servicesConstants.SCREEN_WIDTH_SESSION_ATTR, Long.valueOf(request.getHeader(Ns8servicesConstants.SCREEN_WIDTH_SESSION_ATTR)));
        }

        filterChain.doFilter(request, response);
    }
}
