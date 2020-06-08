package com.ns8.hybris.core.merchant.services.impl;

import com.ns8.hybris.core.integration.exceptions.Ns8IntegrationException;
import com.ns8.hybris.core.merchant.parameter.builder.MerchantParameters;
import com.ns8.hybris.core.model.NS8MerchantModel;
import com.ns8.hybris.core.services.api.Ns8ApiService;
import de.hybris.bootstrap.annotations.UnitTest;
import de.hybris.platform.basecommerce.model.site.BaseSiteModel;
import de.hybris.platform.servicelayer.model.ModelService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;

import java.util.Optional;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

@UnitTest
@RunWith(MockitoJUnitRunner.class)
public class DefaultNs8MerchantServiceTest {

    private static final String PHONE_NUMBER = "0987654321";
    private static final String MERCHANT_EMAIL = "merchant@email.com";
    private static final String STORE_URL = "www.merchant.com";
    private static final String MERCHANT_FIRST_NAME = "merchantname";
    private static final String MERCHANT_LAST_NAME = "merchantlastname";

    @InjectMocks
    private DefaultNs8MerchantService testObj;

    @Mock
    private ModelService modelServiceMock;
    @Mock
    private Ns8ApiService ns8ApiServiceMock;
    @Mock
    private NS8MerchantModel ns8MerchantMock;
    @Mock
    private BaseSiteModel baseSiteMock;
    private MerchantParameters merchantParameters;

    @Before
    public void setUp() {
        merchantParameters = MerchantParameters.MerchantParametersBuilder.getInstance()
                .withEmail(MERCHANT_EMAIL)
                .withStoreUrl(STORE_URL)
                .withMerchantFirstName(MERCHANT_FIRST_NAME)
                .withMerchantLastName(MERCHANT_LAST_NAME)
                .withPhone(PHONE_NUMBER)
                .build();
        when(modelServiceMock.create(NS8MerchantModel.class)).thenReturn(new NS8MerchantModel());
        when(ns8MerchantMock.getEnabled()).thenReturn(Boolean.TRUE);
        when(ns8MerchantMock.getApiKey()).thenReturn("apikey");
    }

    @Test(expected = IllegalArgumentException.class)
    public void createMerchant_WhenMerchantParametersIsNull_ShouldThrowException() {
        testObj.createMerchant(null);
    }

    @Test
    public void createMerchant_WhenEverythingWorksFine_ShouldCreateTheNS8Merchant() {
        final Optional<NS8MerchantModel> result = testObj.createMerchant(merchantParameters);

        verify(modelServiceMock).create(NS8MerchantModel.class);
        assertThat(result.isPresent()).isTrue();
        final NS8MerchantModel resultNS8Merchant = result.get();
        assertThat(resultNS8Merchant.getEmail()).isEqualTo(MERCHANT_EMAIL);
        assertThat(resultNS8Merchant.getStoreUrl()).isEqualTo(STORE_URL);
        assertThat(resultNS8Merchant.getFirstName()).isEqualTo(MERCHANT_FIRST_NAME);
        assertThat(resultNS8Merchant.getLastName()).isEqualTo(MERCHANT_LAST_NAME);
        assertThat(resultNS8Merchant.getPhone()).isEqualTo(PHONE_NUMBER);
    }

    @Test(expected = Ns8IntegrationException.class)
    public void createMerchant_WhenIntegrationError_ShouldRemoveTheModelAndReturnOptionalEmpty() throws Ns8IntegrationException {
        doThrow(new Ns8IntegrationException("exception", HttpStatus.SERVICE_UNAVAILABLE)).when(ns8ApiServiceMock).triggerPluginInstallEvent(any(NS8MerchantModel.class));

        testObj.createMerchant(merchantParameters);
    }

    @Test
    public void addMerchantToBaseSite_ShouldAddTheMerchantToTheSite() {
        testObj.addMerchantToBaseSite(ns8MerchantMock, baseSiteMock);

        verify(baseSiteMock).setNs8Merchant(ns8MerchantMock);
        verify(modelServiceMock).save(baseSiteMock);
    }

    @Test(expected = IllegalArgumentException.class)
    public void addMerchantToCmsSite_WhenMerchantIsNull_ShouldThrowException() {
        testObj.addMerchantToBaseSite(null, baseSiteMock);
    }

    @Test(expected = IllegalArgumentException.class)
    public void addMerchantToCmsSite_WhenSiteIsNull_ShouldThrowException() {
        testObj.addMerchantToBaseSite(ns8MerchantMock, null);
    }

    @Test
    public void deactivateMerchant_ShouldDeactivateMerchant() {
        testObj.deactivateMerchant(ns8MerchantMock);

        final InOrder inOrder = inOrder(ns8ApiServiceMock, ns8MerchantMock, modelServiceMock);
        inOrder.verify(ns8ApiServiceMock).triggerMerchantUninstallEvent(ns8MerchantMock);
        inOrder.verify(ns8MerchantMock).setEnabled(Boolean.FALSE);
        inOrder.verify(modelServiceMock).save(ns8MerchantMock);
    }

    @Test
    public void reactivateMerchant_WhenMerchantIsReactivated_ShouldSetEnabledTrue() {
        when(ns8ApiServiceMock.triggerMerchantReinstallEvent(ns8MerchantMock)).thenReturn(true);

        testObj.reactivateMerchant(ns8MerchantMock);

        verify(ns8MerchantMock).setEnabled(Boolean.TRUE);
        verify(modelServiceMock).save(ns8MerchantMock);
    }

    @Test
    public void reactivateMerchant_WhenMerchantReactivationReturnFalse_ShouldDoNothing() {
        when(ns8ApiServiceMock.triggerMerchantReinstallEvent(ns8MerchantMock)).thenReturn(false);

        testObj.reactivateMerchant(ns8MerchantMock);

        verifyZeroInteractions(ns8MerchantMock);
        verifyZeroInteractions(modelServiceMock);
    }

    @Test
    public void isMerchantActive_WhenMerchantIsEnabledAndApiKeyPresent_ShouldReturnTrue() {
        boolean result = testObj.isMerchantActive(ns8MerchantMock);

        assertThat(result).isTrue();
    }

    @Test
    public void isMerchantActive_WhenMerchantIsEnabledAndApiKeyNull_ShouldReturnFalse() {
        when(ns8MerchantMock.getApiKey()).thenReturn(null);

        boolean result = testObj.isMerchantActive(ns8MerchantMock);

        assertThat(result).isFalse();
    }

    @Test
    public void isMerchantActive_WhenMerchantIsDisabled_ShouldReturnFalse() {
        when(ns8MerchantMock.getEnabled()).thenReturn(Boolean.FALSE);

        boolean result = testObj.isMerchantActive(ns8MerchantMock);

        assertThat(result).isFalse();
    }

    @Test(expected = IllegalArgumentException.class)
    public void isMerchantActive_WhenMerchantIsNull_ShouldThrowException() {
        testObj.isMerchantActive(null);
    }
}
