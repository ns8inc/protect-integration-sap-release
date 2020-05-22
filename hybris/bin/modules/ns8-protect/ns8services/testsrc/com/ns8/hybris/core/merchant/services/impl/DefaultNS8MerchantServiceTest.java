package com.ns8.hybris.core.merchant.services.impl;

import com.ns8.hybris.core.integration.exceptions.NS8IntegrationException;
import com.ns8.hybris.core.merchant.parameter.builder.MerchantParameters;
import com.ns8.hybris.core.model.NS8MerchantModel;
import com.ns8.hybris.core.services.api.NS8APIService;
import de.hybris.bootstrap.annotations.UnitTest;
import de.hybris.platform.basecommerce.model.site.BaseSiteModel;
import de.hybris.platform.servicelayer.model.ModelService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;

import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

@UnitTest
@RunWith(MockitoJUnitRunner.class)
public class DefaultNS8MerchantServiceTest {

    private static final String PHONE_NUMBER = "0987654321";
    private static final String MERCHANT_EMAIL = "merchant@email.com";
    private static final String STORE_URL = "www.merchant.com";
    private static final String MERCHANT_FIRST_NAME = "merchantname";
    private static final String MERCHANT_LAST_NAME = "merchantlastname";

    @InjectMocks
    private DefaultNS8MerchantService testObj;

    @Mock
    private ModelService modelServiceMock;
    @Mock
    private NS8APIService ns8APIServiceMock;
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
    }

    @Test(expected = IllegalArgumentException.class)
    public void createMerchant_WhenMerchantParametersIsNull_ShouldThrowException() {
        testObj.createMerchant(null);
    }

    @Test
    public void createMerchant_WhenEverythingWorksFine_ShouldCreateTheNS8Merchant() {
        final Optional<NS8MerchantModel> result = testObj.createMerchant(merchantParameters);

        verify(modelServiceMock).create(NS8MerchantModel.class);
        assertTrue(result.isPresent());
        final NS8MerchantModel resultNS8Merchant = result.get();
        assertEquals(MERCHANT_EMAIL, resultNS8Merchant.getEmail());
        assertEquals(STORE_URL, resultNS8Merchant.getStoreUrl());
        assertEquals(MERCHANT_FIRST_NAME, resultNS8Merchant.getFirstName());
        assertEquals(MERCHANT_LAST_NAME, resultNS8Merchant.getLastName());
        assertEquals(PHONE_NUMBER, resultNS8Merchant.getPhone());
    }

    @Test(expected = NS8IntegrationException.class)
    public void createMerchant_WhenIntegrationError_ShouldRemoveTheModelAndReturnOptionalEmpty() throws NS8IntegrationException {
        doThrow(new NS8IntegrationException("exception", HttpStatus.SERVICE_UNAVAILABLE)).when(ns8APIServiceMock).triggerPluginInstallEvent(any(NS8MerchantModel.class));

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
}
