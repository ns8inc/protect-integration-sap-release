package com.ns8.hybris.core.merchant.services;

import com.ns8.hybris.core.merchant.parameter.builder.MerchantParameters;
import com.ns8.hybris.core.model.NS8MerchantModel;
import de.hybris.platform.basecommerce.model.site.BaseSiteModel;

import java.util.Optional;

/**
 * Manages the merchant configurations
 */
public interface Ns8MerchantService {

    /**
     * Creates the NS8MerchantModel base on the given parameters
     *
     * @param merchantParameters contains all required parameters
     * @return the created NS8MerchantModel if everything worked properly
     */
    Optional<NS8MerchantModel> createMerchant(MerchantParameters merchantParameters);

    /**
     * Deactivates the  ns8 merchant
     *
     * @param ns8Merchant the ns8 merchant to deactivate
     */
    void deactivateMerchant(NS8MerchantModel ns8Merchant);

    /**
     *Reactivates the  ns8 merchant
     *
     * @param ns8Merchant the ns8 merchant to deactivate
     */
    void reactivateMerchant(NS8MerchantModel ns8Merchant);

    /**
     * Adds the given ns8 merchant to the cms site
     *
     * @param ns8Merchant the ns8 merchant
     * @param baseSite    the base site
     */
    void addMerchantToBaseSite(NS8MerchantModel ns8Merchant, BaseSiteModel baseSite);

    /**
     * Checks if the merchant is active or not
     *
     * @param ns8Merchant the merchant
     * @return true if merchant is active, false otherwise
     */
    boolean isMerchantActive(NS8MerchantModel ns8Merchant);
}
