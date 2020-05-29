package com.ns8.hybris.addon.facades.impl;

import com.ns8.hybris.core.merchant.services.Ns8MerchantService;
import com.ns8.hybris.core.model.NS8MerchantModel;
import de.hybris.bootstrap.annotations.UnitTest;
import de.hybris.platform.cms2.model.site.CMSSiteModel;
import de.hybris.platform.cms2.servicelayer.services.CMSSiteService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@UnitTest
@RunWith(MockitoJUnitRunner.class)
public class DefaultNs8MerchantFacadeTest {

    @InjectMocks
    private DefaultNs8MerchantFacade testObj;

    @Mock
    private CMSSiteService cmsSiteServiceMock;
    @Mock
    private CMSSiteModel currentSiteMock;
    @Mock
    private NS8MerchantModel ns8MerchantMock;
    @Mock
    private Ns8MerchantService ns8MerchantServiceMock;

    @Test
    public void isMerchantActive_ShouldCallNs8MerchantService() {
        when(cmsSiteServiceMock.getCurrentSite()).thenReturn(currentSiteMock);
        when(currentSiteMock.getNs8Merchant()).thenReturn(ns8MerchantMock);

        testObj.isMerchantActive();

        verify(ns8MerchantServiceMock).isMerchantActive(ns8MerchantMock);
    }
}