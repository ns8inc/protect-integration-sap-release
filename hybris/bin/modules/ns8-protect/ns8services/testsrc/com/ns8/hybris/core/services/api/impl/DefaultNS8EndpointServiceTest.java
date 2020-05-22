package com.ns8.hybris.core.services.api.impl;

import de.hybris.bootstrap.annotations.UnitTest;
import de.hybris.platform.servicelayer.config.ConfigurationService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@UnitTest
@RunWith(MockitoJUnitRunner.class)
public class DefaultNS8EndpointServiceTest {

    private static final String CLIENT_BASE_URL_VALUE = "clientURL";
    private static final String BACKEND_BASE_URL_VALUE = "backendURL";
    private static final String ENVIRONMENT_CONFIGURATION_VALUE = "environment";
    private static final String NS_8_SERVICES_API_ENVIRONMENT_CONFIGURATION_KEY = "ns8services.api.environment";
    private static final String NS_8_SERVICES_BASE_URL_CLIENT_API_CONFIGURATION_KEY = "ns8services.base.url.client.api.";
    private static final String NS_8_SERVICES_BASE_URL_BACKEND_API_CONFIGURATION_KEY = "ns8services.base.url.backend.api.";

    @InjectMocks
    private DefaultNS8EndpointService testObj;

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ConfigurationService configurationServiceMock;

    @Before
    public void setUp() {
        when(configurationServiceMock.getConfiguration().getString(NS_8_SERVICES_API_ENVIRONMENT_CONFIGURATION_KEY)).thenReturn(ENVIRONMENT_CONFIGURATION_VALUE);
    }

    @Test
    public void getBaseBackEndURL_shouldReturnBackendAPIURL() {
        when(configurationServiceMock.getConfiguration().getString(NS_8_SERVICES_BASE_URL_BACKEND_API_CONFIGURATION_KEY + ENVIRONMENT_CONFIGURATION_VALUE)).thenReturn(BACKEND_BASE_URL_VALUE);

        final String result = testObj.getBaseBackendURL();

        assertThat(result).isEqualTo(BACKEND_BASE_URL_VALUE);
    }

    @Test
    public void getBaseClientURL_shouldReturnClientAPIURL() {
        when(configurationServiceMock.getConfiguration().getString(NS_8_SERVICES_BASE_URL_CLIENT_API_CONFIGURATION_KEY + ENVIRONMENT_CONFIGURATION_VALUE)).thenReturn(CLIENT_BASE_URL_VALUE);

        final String result = testObj.getBaseClientURL();

        assertThat(result).isEqualTo(CLIENT_BASE_URL_VALUE);
    }
}
