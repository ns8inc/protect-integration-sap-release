package com.ns8.hybris.core.merchant.services.impl;

import com.ns8.hybris.core.merchant.parameter.builder.MerchantParameters;
import com.ns8.hybris.core.merchant.services.Ns8MerchantService;
import com.ns8.hybris.core.model.NS8MerchantModel;
import com.ns8.hybris.core.services.api.Ns8ApiService;
import de.hybris.platform.basecommerce.model.site.BaseSiteModel;
import de.hybris.platform.servicelayer.model.ModelService;
import org.apache.commons.lang.StringUtils;

import java.util.Optional;

import static de.hybris.platform.servicelayer.util.ServicesUtil.validateParameterNotNull;

/**
 * Default implementation of {@link Ns8MerchantService}
 */
public class DefaultNs8MerchantService implements Ns8MerchantService {

    protected final ModelService modelService;
    protected final Ns8ApiService ns8ApiService;

    public DefaultNs8MerchantService(final ModelService modelService, final Ns8ApiService ns8ApiService) {
        this.modelService = modelService;
        this.ns8ApiService = ns8ApiService;
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

        ns8ApiService.triggerPluginInstallEvent(ns8Merchant);
        return Optional.of(ns8Merchant);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void deactivateMerchant(final NS8MerchantModel ns8Merchant) {
        ns8ApiService.triggerMerchantUninstallEvent(ns8Merchant);

        ns8Merchant.setEnabled(Boolean.FALSE);
        modelService.save(ns8Merchant);
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

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isMerchantActive(final NS8MerchantModel ns8Merchant) {
        validateParameterNotNull(ns8Merchant, "NS8 Merchant cannot be null.");

        return ns8Merchant.getEnabled() && StringUtils.isNotBlank(ns8Merchant.getApiKey());
    }
}
