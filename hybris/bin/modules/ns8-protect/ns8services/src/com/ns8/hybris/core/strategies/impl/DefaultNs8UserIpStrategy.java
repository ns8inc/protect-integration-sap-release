package com.ns8.hybris.core.strategies.impl;

import com.google.common.net.HttpHeaders;
import com.ns8.hybris.core.strategies.Ns8UserIpStrategy;

import javax.servlet.http.HttpServletRequest;

/**
 * {@inheritDoc}
 */
public class DefaultNs8UserIpStrategy implements Ns8UserIpStrategy {

    /**
     * {@inheritDoc}
     */
    @Override
    public String getUserIp(final HttpServletRequest request) {
        return request.getHeader(HttpHeaders.X_FORWARDED_FOR);
    }
}
