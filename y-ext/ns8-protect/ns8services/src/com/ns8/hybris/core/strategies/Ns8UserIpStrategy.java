package com.ns8.hybris.core.strategies;

import javax.servlet.http.HttpServletRequest;

/**
 * Strategy for getting the users ip from a {@link HttpServletRequest}
 */
public interface Ns8UserIpStrategy {

    /**
     * Gets the users IP
     * @param request The http request of the user
     * @return the ip of the user making the request
     */
    String getUserIp(HttpServletRequest request);
}
