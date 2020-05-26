package com.ns8.hybris.addon.facades.impl;

import com.ns8.hybris.addon.data.Ns8OrderVerificationData;
import com.ns8.hybris.core.data.Ns8OrderVerificationRequest;
import com.ns8.hybris.core.model.NS8MerchantModel;
import com.ns8.hybris.core.services.api.NS8APIService;
import de.hybris.bootstrap.annotations.UnitTest;
import de.hybris.platform.basecommerce.model.site.BaseSiteModel;
import de.hybris.platform.servicelayer.dto.converter.Converter;
import de.hybris.platform.site.BaseSiteService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.internal.util.reflection.Whitebox;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@UnitTest
@RunWith(MockitoJUnitRunner.class)
public class DefaultNs8OrderVerificationTemplateFacadeTest {

    private static final String TOKEN = "token";
    private static final String VERIFICATION_ID = "verificationId";
    private static final String API_KEY = "apiKey";
    private static final String RETURN_URI = "returnUri";
    private static final String TEMPLATE_ID = "orders-validate";
    private static final String ORDER_ID = "orderId";

    @InjectMocks
    private DefaultNs8OrderVerificationTemplateFacade testObj;

    @Mock
    private NS8APIService ns8APIServiceMock;
    @Mock
    private BaseSiteService baseSiteServiceMock;
    @Mock
    private Converter<Ns8OrderVerificationData, Ns8OrderVerificationRequest> ns8OrderVerificationRequestConverter;

    @Mock
    private BaseSiteModel baseSiteMock;
    @Mock
    private NS8MerchantModel ns8MerchantMock;
    @Mock
    private Ns8OrderVerificationData verificationDataMock;
    @Mock
    private Ns8OrderVerificationRequest verificationRequestMock;

    @Before
    public void setUp() {
        Whitebox.setInternalState(testObj, "ns8OrderVerificationRequestConverter", ns8OrderVerificationRequestConverter);
        when(baseSiteServiceMock.getCurrentBaseSite()).thenReturn(baseSiteMock);
        when(baseSiteMock.getNs8Merchant()).thenReturn(ns8MerchantMock);
        when(ns8MerchantMock.getApiKey()).thenReturn(API_KEY);
        when(ns8OrderVerificationRequestConverter.convert(verificationDataMock)).thenReturn(verificationRequestMock);
        when(verificationRequestMock.getOrderId()).thenReturn(ORDER_ID);
        when(verificationRequestMock.getToken()).thenReturn(TOKEN);
        when(verificationRequestMock.getVerificationId()).thenReturn(VERIFICATION_ID);
        when(verificationRequestMock.getView()).thenReturn(TEMPLATE_ID);
        when(verificationRequestMock.getReturnURI()).thenReturn(RETURN_URI);
    }

    @Test
    public void getVerificationTemplate_WhenSiteHasMerchant_ShouldCallNs8APIService() {
        testObj.getVerificationTemplate(verificationDataMock);

        verify(ns8APIServiceMock).getVerificationTemplate(verificationRequestMock, API_KEY);
    }

    @Test
    public void getVerificationTemplate_WhenSiteHasNoMerchant_ShouldThrowIllegalArgumentException() {
        when(baseSiteMock.getNs8Merchant()).thenReturn(null);

        final Throwable thrown = catchThrowable(() -> testObj.getVerificationTemplate(verificationDataMock));

        assertThat(thrown).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    public void sendVerification_WhenSiteHasMerchant_ShouldCallNs8APIService() {
        testObj.sendVerification(verificationDataMock);

        verify(ns8APIServiceMock).sendVerification(verificationRequestMock, API_KEY);
    }


    @Test
    public void sendVerification_WhenSiteHasNoMerchant_ShouldThrowIllegalArgumentException() {
        when(baseSiteMock.getNs8Merchant()).thenReturn(null);

        final Throwable thrown = catchThrowable(() -> testObj.sendVerification(verificationDataMock));

        assertThat(thrown).isInstanceOf(IllegalArgumentException.class);
    }
}
