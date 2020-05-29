package com.ns8.hybris.addon.controllers;

import com.ns8.hybris.addon.data.Ns8OrderVerificationData;
import com.ns8.hybris.addon.facades.Ns8OrderVerificationTemplateFacade;
import com.ns8.hybris.addon.forms.Ns8OrderVerificationForm;
import com.ns8.hybris.core.data.Ns8OrderVerificationResponse;
import de.hybris.platform.acceleratorservices.urlresolver.SiteBaseUrlResolutionService;
import de.hybris.platform.acceleratorstorefrontcommons.controllers.pages.AbstractPageController;
import de.hybris.platform.cms2.exceptions.CMSItemNotFoundException;
import de.hybris.platform.cms2.model.pages.ContentPageModel;
import de.hybris.platform.servicelayer.dto.converter.Converter;
import de.hybris.platform.site.BaseSiteService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.util.UriComponentsBuilder;

import javax.annotation.Resource;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import static org.apache.commons.lang.StringUtils.isBlank;

/**
 * Controller to surface to the FE the NS8 order verification
 */
@Controller
public class Ns8OrderVerificationController extends AbstractPageController {

    private static final String REDIRECT_PREFIX = "redirect:";
    private static final String ORDER_REJECTION_REDIRECT = "/orders-reject-confirm?orderId=:orderId&token=:token&verificationId=:verificationId";

    @Resource
    protected final Ns8OrderVerificationTemplateFacade ns8OrderVerificationTemplateFacade;
    @Resource
    protected final Converter<Ns8OrderVerificationForm, Ns8OrderVerificationData> ns8OrderVerificationDataConverter;
    @Resource
    protected final SiteBaseUrlResolutionService siteBaseUrlResolutionService;
    @Resource
    protected final BaseSiteService baseSiteService;

    public Ns8OrderVerificationController(final Ns8OrderVerificationTemplateFacade ns8OrderVerificationTemplateFacade,
                                          final Converter<Ns8OrderVerificationForm, Ns8OrderVerificationData> ns8OrderVerificationDataConverter,
                                          final SiteBaseUrlResolutionService siteBaseUrlResolutionService,
                                          final BaseSiteService baseSiteService) {
        this.ns8OrderVerificationTemplateFacade = ns8OrderVerificationTemplateFacade;
        this.ns8OrderVerificationDataConverter = ns8OrderVerificationDataConverter;
        this.siteBaseUrlResolutionService = siteBaseUrlResolutionService;
        this.baseSiteService = baseSiteService;
    }

    /**
     * Gets the order verification content for orders-validate template
     *
     * @param model the model
     * @param form  the form containing the information from the page
     * @return returns the html content for the order verification page
     * @throws CMSItemNotFoundException if no order verification page is found
     */
    @GetMapping(value = "/orders-validate")
    public String orderVerification(final Model model, final Ns8OrderVerificationForm form) throws CMSItemNotFoundException {
        final Ns8OrderVerificationData orderVerificationData = createNs8OrderVerificationData(form, "orders-validate", null);

        final String orderVerificationPageContent = ns8OrderVerificationTemplateFacade.getVerificationTemplate(orderVerificationData);
        return fillOrderVerificationPage(model, orderVerificationPageContent);
    }

    /**
     * Posts the form data to ns8 order template and returns the html content for the order verification page
     *
     * @param model the model
     * @param form  the form containing the information from the page
     * @return the html template form the response to inject into the order verification page
     * @throws CMSItemNotFoundException if no order verification page is found
     */
    @PostMapping(value = "/orders-validate")
    public String postOrderVerification(final Model model, final Ns8OrderVerificationForm form) throws CMSItemNotFoundException {
        final Ns8OrderVerificationData orderVerificationData = createNs8OrderVerificationData(form, "orders-validate", "/orders-validate-code");

        final Ns8OrderVerificationResponse response = ns8OrderVerificationTemplateFacade.sendVerification(orderVerificationData);
        if (isBlank(response.getHtml())) {
            return REDIRECT_PREFIX + getRedirectUrl("/orders-validate-code", orderVerificationData);
        }
        return fillOrderVerificationPage(model, response.getHtml());
    }

    /**
     * Gets the order verification content for orders-valid template
     *
     * @param model the model
     * @param form  the form containing the information from the page
     * @return returns the html content for the order verification page
     * @throws CMSItemNotFoundException if no order verification page is found
     */
    @GetMapping(value = "/orders-validate-code")
    public String orderVerificationCode(final Model model, final Ns8OrderVerificationForm form) throws CMSItemNotFoundException {
        final Ns8OrderVerificationData orderVerificationData = createNs8OrderVerificationData(form, "orders-validate-code", null);

        final String orderVerificationPageContent = ns8OrderVerificationTemplateFacade.getVerificationTemplate(orderVerificationData);
        return fillOrderVerificationPage(model, orderVerificationPageContent);
    }

    /**
     * Gets the order verification content for orders-valid template
     *
     * @param model the model
     * @param form  the form containing the information from the page
     * @return returns the html content for the order verification page
     * @throws CMSItemNotFoundException if no order verification page is found
     */
    @PostMapping(value = "/orders-validate-code")
    public String postOrderVerificationCode(final Model model, final Ns8OrderVerificationForm form) throws CMSItemNotFoundException {
        final Ns8OrderVerificationData orderVerificationData = createNs8OrderVerificationData(form, "orders-validate-code", null);

        final Ns8OrderVerificationResponse orderVerificationResponse = ns8OrderVerificationTemplateFacade.sendVerification(orderVerificationData);
        return fillOrderVerificationPage(model, orderVerificationResponse.getHtml());
    }

    /**
     * Gets the order verification content for orders-reject template
     *
     * @param model the model
     * @param form  the form containing the information from the page
     * @return the html content for the order verification page
     * @throws CMSItemNotFoundException if no order verification page is found
     */
    @GetMapping(value = "/orders-reject")
    public String orderRejection(final Model model, final Ns8OrderVerificationForm form) throws CMSItemNotFoundException {
        final Ns8OrderVerificationData orderVerificationData = createNs8OrderVerificationData(form, "orders-reject", buildRedirectRejectionURL());
        final String orderVerificationPageContent = ns8OrderVerificationTemplateFacade.getVerificationTemplate(orderVerificationData);
        return fillOrderVerificationPage(model, orderVerificationPageContent);
    }

    /**
     * Gets the order verification content for orders-reject-confirm template
     *
     * @param model the model
     * @param form  the form containing the information from the page
     * @return the html content for the order verification page
     * @throws CMSItemNotFoundException if no order verification page is found
     */
    @GetMapping(value = "/orders-reject-confirm")
    public String orderRejectionConfirm(final Model model, final Ns8OrderVerificationForm form) throws CMSItemNotFoundException {
        final Ns8OrderVerificationData orderVerificationData = createNs8OrderVerificationData(form, "orders-reject-confirm", null);

        final String orderVerificationPageContent = ns8OrderVerificationTemplateFacade.getVerificationTemplate(orderVerificationData);
        return fillOrderVerificationPage(model, orderVerificationPageContent);
    }

    /**
     * Sets up the order verification page and returns the view
     *
     * @param model   the model
     * @param content the content to inject into the page
     * @return the view of the content page
     * @throws CMSItemNotFoundException if no order verification page is found
     */
    protected String fillOrderVerificationPage(final Model model, final String content) throws CMSItemNotFoundException {
        final ContentPageModel ns8OrderVerificationPage = getContentPageForLabelOrId("ns8OrderVerificationPage");
        storeCmsPageInModel(model, ns8OrderVerificationPage);
        model.addAttribute("ns8VerifcationContent", content);

        return getViewForPage(ns8OrderVerificationPage);
    }

    /**
     * Returns the url of the path for the current website with the given params
     *
     * @param path                  the path
     * @param orderVerificationData the order verification data containing the params
     * @return url of the path
     */
    protected String getRedirectUrl(final String path, final Ns8OrderVerificationData orderVerificationData) {
        return UriComponentsBuilder.fromUriString("")
                .path(path)
                .queryParam("orderId", orderVerificationData.getOrderId())
                .queryParam("token", orderVerificationData.getToken())
                .queryParam("verificationId", orderVerificationData.getVerificationId())
                .build().toString();
    }

    /**
     * Builds the redirection URL for order rejection
     * <p>
     * Due to the nature of NS8's middleware, the redirect rejection URL
     * has to have an specific format and be encoded twice
     * in order to retrieve all the parameters and their values
     *
     * @return Twice encoded rejection redirect URL
     */
    protected String buildRedirectRejectionURL() {
        return siteBaseUrlResolutionService.getWebsiteUrlForSite(baseSiteService.getCurrentBaseSite(), true, null)
                + URLEncoder.encode(
                URLEncoder.encode(ORDER_REJECTION_REDIRECT, StandardCharsets.UTF_8)
                , StandardCharsets.UTF_8);
    }

    /**
     * Creates the order verification data for the given form,template and URI
     *
     * @param form      the ns8 order validation form
     * @param template  the order validation template
     * @param returnURI the return uri
     * @return {@link Ns8OrderVerificationData}
     */
    protected Ns8OrderVerificationData createNs8OrderVerificationData(final Ns8OrderVerificationForm form, final String template, final String returnURI) {
        final Ns8OrderVerificationData orderVerificationData = ns8OrderVerificationDataConverter.convert(form);
        orderVerificationData.setTemplate(template);
        orderVerificationData.setReturnURI(returnURI);
        return orderVerificationData;
    }
}
