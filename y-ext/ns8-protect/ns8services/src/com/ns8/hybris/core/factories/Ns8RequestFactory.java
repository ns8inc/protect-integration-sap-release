package com.ns8.hybris.core.factories;

import org.springframework.beans.factory.FactoryBean;
import org.springframework.http.client.BufferingClientHttpRequestFactory;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;

/**
 * Request factory bean which returns a {@see BufferingClientHttpRequestFactory}
 * This is required to preserve the response when using the logging request/response
 * interceptor
 */
public class Ns8RequestFactory implements FactoryBean<ClientHttpRequestFactory> {
    @Override
    public ClientHttpRequestFactory getObject() {
        return new BufferingClientHttpRequestFactory(new SimpleClientHttpRequestFactory());
    }

    @Override
    public Class<?> getObjectType() {
        return ClientHttpRequestFactory.class;
    }

    @Override
    public boolean isSingleton() {
        return true;
    }
}
