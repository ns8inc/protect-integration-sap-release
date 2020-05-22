package com.ns8.hybris.core.merchant.services.impl;

import com.ns8.hybris.core.merchant.parameter.builder.MerchantParameters;
import com.ns8.hybris.core.merchant.services.NS8MerchantService;
import com.ns8.hybris.core.model.NS8MerchantModel;
import com.ns8.hybris.core.services.api.NS8APIService;
import de.hybris.platform.basecommerce.model.site.BaseSiteModel;
import de.hybris.platform.servicelayer.model.ModelService;

import java.util.Optional;

import static de.hybris.platform.servicelayer.util.ServicesUtil.validateParameterNotNull;

/**
 * Default implementation of {@link NS8MerchantService}
 */
public class DefaultNS8MerchantService implements NS8MerchantService {

    protected final ModelService modelService;
    protected final NS8APIService ns8APIService;

    public DefaultNS8MerchantService(final ModelService modelService, final NS8APIService ns8APIService) {
        this.modelService = modelService;
        this.ns8APIService = ns8APIService;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<NS8MerchantModel> createMerchant(final MerchantParameters merchantParameters) {
        validateParameterNotNull(merchantParameters, "MerchantParameters cannot be null.");

        final NS8MerchantModel ns8Merchant = modelService.create(NS8MerchantModel.class);

        ns8Merchant.setEmail(merchantParameters.getEmail());
        ns8Merchant.setStoreUrl(merchantParameters.getStoreUrl());
        ns8Merchant.setFirstName(merchantParameters.getMerchantFirstName());
        ns8Merchant.setLastName(merchantParameters.getMerchantLastName());
        ns8Merchant.setPhone(merchantParameters.getPhone());

        ns8APIService.triggerPluginInstallEvent(ns8Merchant);
        return Optional.of(ns8Merchant);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addMerchantToBaseSite(final NS8MerchantModel ns8Merchant, final BaseSiteModel baseSite) {
        validateParameterNotNull(ns8Merchant, "NS8 Merchant cannot be null.");
        validateParameterNotNull(baseSite, "Base site cannot be null");

        baseSite.setNs8Merchant(ns8Merchant);
        modelService.save(baseSite);
    }
}
