package com.ns8.hybris.core.strategies.impl;

import com.ns8.hybris.core.strategies.Ns8UserIpStrategy;
import org.apache.commons.lang.StringUtils;

import javax.servlet.http.HttpServletRequest;

/**
 * {@inheritDoc}
 */
public class DefaultNs8UserIpStrategy implements Ns8UserIpStrategy {

    protected final String headerToCheck;

    public DefaultNs8UserIpStrategy(final String headerToCheck) {
        this.headerToCheck = headerToCheck;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getUserIp(final HttpServletRequest request) {
        final String ipAddress = getIpFromHeader(request);
        return ipAddress != null ? ipAddress : request.getRemoteAddr();
    }

    /**
     * Given a request, it will try to find a valid IP value for the header.
     * If the header value is null or empty, it returns null. Otherwise it
     * will return the first value of the list or the only value present.
     *
     * @param request the {@see HttpServletRequest}
     * @return the IP from the header
     */
    protected String getIpFromHeader(final HttpServletRequest request) {
        final String headerValue = request.getHeader(headerToCheck);
        if (StringUtils.isBlank(headerValue) || "unknown".equalsIgnoreCase(headerValue)) {
            return null;
        }
        return headerValue.contains(",") ? headerValue.split(",")[0] : headerValue;
    }

}
