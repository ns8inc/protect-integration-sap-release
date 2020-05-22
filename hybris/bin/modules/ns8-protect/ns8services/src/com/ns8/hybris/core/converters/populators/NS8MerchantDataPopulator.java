package com.ns8.hybris.core.converters.populators;

import com.ns8.hybris.core.data.NS8MerchantData;
import com.ns8.hybris.core.model.NS8MerchantModel;
import de.hybris.platform.converters.Populator;

/**
 * Populates the information of the {@link NS8MerchantModel} into a {@link NS8MerchantData}
 */
public class NS8MerchantDataPopulator implements Populator<NS8MerchantModel, NS8MerchantData> {

    /**
     * {@inheritDoc}
     */
    @Override
    public void populate(final NS8MerchantModel source, final NS8MerchantData target) {
        target.setEmail(source.getEmail());
        target.setFirstName(source.getFirstName());
        target.setLastName(source.getLastName());
        target.setPhone(source.getPhone());
        target.setStoreUrl(source.getStoreUrl());
    }
}
