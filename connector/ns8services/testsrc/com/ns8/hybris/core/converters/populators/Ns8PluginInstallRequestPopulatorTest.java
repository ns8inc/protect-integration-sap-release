package com.ns8.hybris.core.converters.populators;

import com.ns8.hybris.core.data.Ns8PluginInstallRequest;
import com.ns8.hybris.core.model.NS8MerchantModel;
import de.hybris.bootstrap.annotations.UnitTest;
import de.hybris.platform.servicelayer.config.ConfigurationService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Answers.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.when;

@UnitTest
@RunWith(MockitoJUnitRunner.class)
public class Ns8PluginInstallRequestPopulatorTest {

    private static final String EMAIL = "email";
    private static final String FIRST_NAME = "firstName";
    private static final String LAST_NAME = "lastName";
    private static final String PHONE_NUMBER = "phoneNumber";
    private static final String STORE_URL = "storeURL";
    private static final String NS_8_SERVICES_CONNECTOR_VERSION_CONFIG_KEY = "ns8services.connector.version";
    private static final String BUILD_VERSION_CONFIG_KEY = "build.version";
    private static final String DEFAULT_CONNECTOR_VERSION = "develop";
    private static final String BUILD_VERSION = "1905.14";

    @InjectMocks
    private Ns8PluginInstallRequestPopulator testObj;

    @Mock(answer = RETURNS_DEEP_STUBS)
    private ConfigurationService configurationServiceMock;

    @Mock
    private NS8MerchantModel ns8MerchantModelMock;

    @Before
    public void setUp() {
        when(configurationServiceMock.getConfiguration().getString(BUILD_VERSION_CONFIG_KEY)).thenReturn(BUILD_VERSION);
        when(configurationServiceMock.getConfiguration().getString(NS_8_SERVICES_CONNECTOR_VERSION_CONFIG_KEY, DEFAULT_CONNECTOR_VERSION)).thenReturn(DEFAULT_CONNECTOR_VERSION);
    }

    @Test
    public void populate_shouldFillMerchantDataDetails() {
        var ns8PluginInstallRequest = new Ns8PluginInstallRequest();

        when(ns8MerchantModelMock.getEmail()).thenReturn(EMAIL);
        when(ns8MerchantModelMock.getFirstName()).thenReturn(FIRST_NAME);
        when(ns8MerchantModelMock.getLastName()).thenReturn(LAST_NAME);
        when(ns8MerchantModelMock.getPhone()).thenReturn(PHONE_NUMBER);
        when(ns8MerchantModelMock.getStoreUrl()).thenReturn(STORE_URL);

        testObj.populate(ns8MerchantModelMock, ns8PluginInstallRequest);

        assertThat(ns8PluginInstallRequest.getEmail()).isEqualTo(EMAIL);
        assertThat(ns8PluginInstallRequest.getFirstName()).isEqualTo(FIRST_NAME);
        assertThat(ns8PluginInstallRequest.getLastName()).isEqualTo(LAST_NAME);
        assertThat(ns8PluginInstallRequest.getPhone()).isEqualTo(PHONE_NUMBER);
        assertThat(ns8PluginInstallRequest.getStoreUrl()).isEqualTo(STORE_URL);
        assertThat(ns8PluginInstallRequest.getPlatformVersion()).isEqualTo(BUILD_VERSION);
        assertThat(ns8PluginInstallRequest.getModuleVersion()).isEqualTo(DEFAULT_CONNECTOR_VERSION);
    }
}
