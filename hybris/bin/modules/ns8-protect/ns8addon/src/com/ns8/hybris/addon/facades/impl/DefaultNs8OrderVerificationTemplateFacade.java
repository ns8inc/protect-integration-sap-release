package com.ns8.hybris.addon.facades.impl;

import com.ns8.hybris.addon.data.Ns8OrderVerificationData;
import com.ns8.hybris.addon.facades.Ns8OrderVerificationTemplateFacade;
import com.ns8.hybris.core.data.Ns8OrderVerificationRequest;
import com.ns8.hybris.core.data.Ns8OrderVerificationResponse;
import com.ns8.hybris.core.model.NS8MerchantModel;
import com.ns8.hybris.core.services.api.NS8APIService;
import de.hybris.platform.basecommerce.model.site.BaseSiteModel;
import de.hybris.platform.servicelayer.dto.converter.Converter;
import de.hybris.platform.site.BaseSiteService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Optional;

/**
 * Default implementation of {@link Ns8OrderVerificationTemplateFacade}
 */
public class DefaultNs8OrderVerificationTemplateFacade implements Ns8OrderVerificationTemplateFacade {

    protected static final Logger LOG = LogManager.getLogger(DefaultNs8OrderVerificationTemplateFacade.class);

    protected final NS8APIService ns8APIService;
    protected final BaseSiteService baseSiteService;
    protected final Converter<Ns8OrderVerificationData, Ns8OrderVerificationRequest> ns8OrderVerificationRequestConverter;

    public DefaultNs8OrderVerificationTemplateFacade(final NS8APIService ns8APIService, final BaseSiteService baseSiteService,
                                                     final Converter<Ns8OrderVerificationData, Ns8OrderVerificationRequest> ns8OrderVerificationRequestConverter) {
        this.ns8APIService = ns8APIService;
        this.baseSiteService = baseSiteService;
        this.ns8OrderVerificationRequestConverter = ns8OrderVerificationRequestConverter;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getVerificationTemplate(final Ns8OrderVerificationData verificationData) {
        return ns8APIService.getVerificationTemplate(ns8OrderVerificationRequestConverter.convert(verificationData), getMerchantApiKey());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Ns8OrderVerificationResponse sendVerification(final Ns8OrderVerificationData verificationData) {
        return ns8APIService.sendVerification(ns8OrderVerificationRequestConverter.convert(verificationData), getMerchantApiKey());
    }

    /**
     * Gets the API key for the current base site
     *
     * @return API key of the NS8 merchant
     * @throws IllegalArgumentException if the current site does not have an NS8 merchant
     */
    protected String getMerchantApiKey() {
        return Optional.ofNullable(baseSiteService.getCurrentBaseSite())
                .map(BaseSiteModel::getNs8Merchant)
                .map(NS8MerchantModel::getApiKey)
                .orElseThrow(() -> new IllegalArgumentException("Could not retrieve verification template for current site as it has no NS8 Merchant"));
    }

}
