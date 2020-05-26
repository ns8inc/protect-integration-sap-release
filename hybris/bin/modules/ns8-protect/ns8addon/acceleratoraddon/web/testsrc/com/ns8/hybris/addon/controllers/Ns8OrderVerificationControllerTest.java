package com.ns8.hybris.addon.controllers;

import com.ns8.hybris.addon.data.Ns8OrderVerificationData;
import com.ns8.hybris.addon.facades.Ns8OrderVerificationTemplateFacade;
import com.ns8.hybris.addon.forms.Ns8OrderVerificationForm;
import com.ns8.hybris.core.data.Ns8OrderVerificationResponse;
import de.hybris.bootstrap.annotations.UnitTest;
import de.hybris.platform.acceleratorservices.storefront.util.PageTitleResolver;
import de.hybris.platform.acceleratorservices.urlresolver.SiteBaseUrlResolutionService;
import de.hybris.platform.basecommerce.model.site.BaseSiteModel;
import de.hybris.platform.cms2.data.PagePreviewCriteriaData;
import de.hybris.platform.cms2.exceptions.CMSItemNotFoundException;
import de.hybris.platform.cms2.model.pages.ContentPageModel;
import de.hybris.platform.cms2.model.pages.PageTemplateModel;
import de.hybris.platform.cms2.servicelayer.services.CMSPageService;
import de.hybris.platform.cms2.servicelayer.services.CMSPreviewService;
import de.hybris.platform.servicelayer.dto.converter.Converter;
import de.hybris.platform.site.BaseSiteService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.internal.util.reflection.Whitebox;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.ui.Model;
import org.springframework.web.util.UriComponentsBuilder;

import static java.util.Map.entry;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@UnitTest
@RunWith(MockitoJUnitRunner.class)
public class Ns8OrderVerificationControllerTest {

    private static final String CONTENT = "content";
    private static final String PAGE_ROOT = "pages/";
    private static final String PAGE_VIEW = "pageView";
    private static final String PAGE_MODEL = "cmsPage";
    private static final String PAGE_TITLE = "pageTitle";
    private static final String TOKEN_VALUE = "tokenValue";
    private static final String REDIRECT_PREFIX = "redirect:";
    private static final String ORDER_ID_VALUE = "orderIdValue";
    private static final String CMS_PAGE_TITLE = "cmsPageTitle";
    private static final String VERIFICATION_ID_VALUE = "verificationIdValue";
    private static final String RESOLVED_CMS_PAGE_TITLE = "resolvedCmsPageTitle";
    private static final String NS8_VERIFICATION_CONTENT = "ns8VerifcationContent";
    private static final String ORDERS_VALIDATE_CODE_URL = "/orders-validate-code";
    private static final String NS8_ORDER_VERIFICATION_PAGE = "ns8OrderVerificationPage";
    private static final String WEBSITE_URL = "websiteUrl";

    @Spy
    @InjectMocks
    private Ns8OrderVerificationController testObj;

    @Mock
    private Ns8OrderVerificationTemplateFacade ns8VerificationTemplateFacadeMock;
    @Mock
    private PageTitleResolver pageTitleResolverMock;
    @Mock
    private CMSPageService cmsPageServiceMock;
    @Mock
    private CMSPreviewService cmsPreviewServiceMock;
    @Mock
    private Converter<Ns8OrderVerificationForm, Ns8OrderVerificationData> ns8OrderVerificationDataConverterMock;
    @Mock
    private SiteBaseUrlResolutionService siteBaseUrlResolutionServiceMock;
    @Mock
    private BaseSiteService baseSiteServiceMock;

    @Mock
    private Ns8OrderVerificationForm formMock;
    @Mock
    private ContentPageModel contentPageMock;
    @Mock
    private PagePreviewCriteriaData pagePreviewCriteriaDataMock;
    @Mock
    private Ns8OrderVerificationData verificationDataMock;
    @Mock
    private PageTemplateModel pageTemplateMock;
    @Mock
    private Ns8OrderVerificationResponse orderVerificationResponseMock;
    @Mock
    private BaseSiteModel baseSiteMock;

    private Model model = new ExtendedModelMap();

    @Before
    public void setUp() throws CMSItemNotFoundException {
        Whitebox.setInternalState(testObj, "cmsPageService", cmsPageServiceMock);
        Whitebox.setInternalState(testObj, "cmsPreviewService", cmsPreviewServiceMock);
        Whitebox.setInternalState(testObj, "pageTitleResolver", pageTitleResolverMock);
        Whitebox.setInternalState(testObj, "ns8OrderVerificationDataConverter", ns8OrderVerificationDataConverterMock);

        when(contentPageMock.getTitle()).thenReturn(CMS_PAGE_TITLE);
        when(contentPageMock.getMasterTemplate()).thenReturn(pageTemplateMock);
        when(cmsPageServiceMock.getPageForLabelOrId(NS8_ORDER_VERIFICATION_PAGE, pagePreviewCriteriaDataMock)).thenReturn(contentPageMock);
        when(cmsPageServiceMock.getFrontendTemplateName(pageTemplateMock)).thenReturn(PAGE_VIEW);
        when(cmsPreviewServiceMock.getPagePreviewCriteria()).thenReturn(pagePreviewCriteriaDataMock);
        when(pageTitleResolverMock.resolveContentPageTitle(CMS_PAGE_TITLE)).thenReturn(RESOLVED_CMS_PAGE_TITLE);
        when(ns8OrderVerificationDataConverterMock.convert(formMock)).thenReturn(verificationDataMock);
        when(baseSiteServiceMock.getCurrentBaseSite()).thenReturn(baseSiteMock);
        when(siteBaseUrlResolutionServiceMock.getWebsiteUrlForSite(baseSiteMock, true, null)).thenReturn(WEBSITE_URL);
    }

    @Test
    public void orderVerification_shouldFillPageWithReceivedContent() throws CMSItemNotFoundException {
        when(ns8VerificationTemplateFacadeMock.getVerificationTemplate(verificationDataMock)).thenReturn(CONTENT);

        final String result = testObj.orderVerification(model, formMock);

        assertCommonPath(result);
    }

    @Test
    public void postOrderVerification_WhenContentIsReturned_shouldFillPageWithReceivedContent() throws CMSItemNotFoundException {
        when(ns8VerificationTemplateFacadeMock.sendVerification(verificationDataMock)).thenReturn(orderVerificationResponseMock);
        when(orderVerificationResponseMock.getHtml()).thenReturn(CONTENT);

        final String result = testObj.postOrderVerification(model, formMock);

        assertCommonPath(result);
    }

    @Test
    public void postOrderVerification_WhenURLIsReturned_shouldRedirectToUrlWithParameters() throws CMSItemNotFoundException {
        populateValidateRequest();

        when(ns8VerificationTemplateFacadeMock.sendVerification(verificationDataMock)).thenReturn(orderVerificationResponseMock);
        when(orderVerificationResponseMock.getHtml()).thenReturn(null);

        final String result = testObj.postOrderVerification(model, formMock);

        verify(testObj, never()).fillOrderVerificationPage(model, CONTENT);
        assertThat(result).isEqualTo(REDIRECT_PREFIX + getRedirectUrlForForm());
    }

    @Test
    public void orderVerificationCode_shouldFillPageWithReceivedContent() throws CMSItemNotFoundException {
        when(ns8VerificationTemplateFacadeMock.getVerificationTemplate(verificationDataMock)).thenReturn(CONTENT);

        final String result = testObj.orderVerificationCode(model, formMock);

        assertCommonPath(result);
    }

    @Test
    public void postOrderVerificationCode_shouldFillPageWithReceivedContent() throws CMSItemNotFoundException {
        when(ns8VerificationTemplateFacadeMock.sendVerification(verificationDataMock)).thenReturn(orderVerificationResponseMock);
        when(orderVerificationResponseMock.getHtml()).thenReturn(CONTENT);

        final String result = testObj.postOrderVerificationCode(model, formMock);

        assertCommonPath(result);
    }

    @Test
    public void orderRejection_shouldFillPageWithReceivedContent() throws CMSItemNotFoundException {
        when(ns8VerificationTemplateFacadeMock.getVerificationTemplate(verificationDataMock)).thenReturn(CONTENT);

        final String result = testObj.orderRejection(model, formMock);

        assertCommonPath(result);
    }

    @Test
    public void orderRejectionConfirm_shouldFillPageWithReceivedContent() throws CMSItemNotFoundException {
        when(ns8VerificationTemplateFacadeMock.getVerificationTemplate(verificationDataMock)).thenReturn(CONTENT);

        final String result = testObj.orderRejectionConfirm(model, formMock);

        assertCommonPath(result);
    }

    private void assertCommonPath(final String result) throws CMSItemNotFoundException {
        verify(testObj).fillOrderVerificationPage(model, CONTENT);
        assertThat(result).isEqualTo(PAGE_ROOT + PAGE_VIEW);

        assertThat(model.asMap()).containsExactly(
                entry(PAGE_MODEL, contentPageMock),
                entry(PAGE_TITLE, RESOLVED_CMS_PAGE_TITLE),
                entry(NS8_VERIFICATION_CONTENT, CONTENT)
        );
    }

    private void populateValidateRequest() {
        when(verificationDataMock.getOrderId()).thenReturn(ORDER_ID_VALUE);
        when(verificationDataMock.getToken()).thenReturn(TOKEN_VALUE);
        when(verificationDataMock.getVerificationId()).thenReturn(VERIFICATION_ID_VALUE);
        when(verificationDataMock.getTemplate()).thenReturn("orders-validate");
        when(verificationDataMock.getReturnURI()).thenReturn("/orders-validate-code");
    }

    private String getRedirectUrlForForm() {
        return UriComponentsBuilder.fromUriString("")
                .path(ORDERS_VALIDATE_CODE_URL)
                .queryParam("orderId", ORDER_ID_VALUE)
                .queryParam("token", TOKEN_VALUE)
                .queryParam("verificationId", VERIFICATION_ID_VALUE)
                .build().toString();
    }
}
