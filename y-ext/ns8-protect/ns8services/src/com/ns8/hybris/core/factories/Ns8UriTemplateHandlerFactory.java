package com.ns8.hybris.core.factories;

import org.springframework.beans.factory.FactoryBean;
import org.springframework.web.util.DefaultUriBuilderFactory;
import org.springframework.web.util.UriBuilderFactory;

/**
 * Uri template handler factory which prevents the request to be re-encoded
 */
public class Ns8UriTemplateHandlerFactory implements FactoryBean<UriBuilderFactory> {
    @Override
    public UriBuilderFactory getObject() {
        final DefaultUriBuilderFactory defaultUriBuilderFactory = new DefaultUriBuilderFactory();
        defaultUriBuilderFactory.setEncodingMode(DefaultUriBuilderFactory.EncodingMode.NONE);
        return defaultUriBuilderFactory;
    }

    @Override
    public Class<?> getObjectType() {
        return UriBuilderFactory.class;
    }

    @Override
    public boolean isSingleton() {
        return true;
    }
}
