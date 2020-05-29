package com.ns8.hybris.core.interceptors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.util.StreamUtils;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 * Interceptor providing logging of rest requests and responses
 */
public class RequestResponseLoggingInterceptor implements ClientHttpRequestInterceptor {

    protected static final Logger LOG = LogManager.getLogger(RequestResponseLoggingInterceptor.class);

    @Override
    public ClientHttpResponse intercept(final HttpRequest request, final byte[] body, final ClientHttpRequestExecution execution) throws IOException {
        logRequest(request, body);
        final ClientHttpResponse response = execution.execute(request, body);
        logResponse(response);
        return response;
    }

    private void logRequest(final HttpRequest request, final byte[] body) throws IOException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("=========================== request begin ================================================");
            LOG.debug("URI         : {}", request::getURI);
            LOG.debug("Method      : {}", request::getMethod);
            LOG.debug("Headers     : {}", request::getHeaders);
            LOG.debug("Request body: {}", new String(body, StandardCharsets.UTF_8));
            LOG.debug("========================== request end ================================================");
        }
    }

    private void logResponse(final ClientHttpResponse response) throws IOException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("============================ response begin ==========================================");
            LOG.debug("Status code  : {}", response.getStatusCode());
            LOG.debug("Status text  : {}", response.getStatusText());
            LOG.debug("Headers      : {}", response.getHeaders());
            LOG.debug("Response body: {}", StreamUtils.copyToString(response.getBody(), Charset.defaultCharset()));
            LOG.debug("======================= response end =================================================");
        }
    }
}
