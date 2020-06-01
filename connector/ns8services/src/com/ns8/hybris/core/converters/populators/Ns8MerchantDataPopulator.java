package com.ns8.hybris.core.converters.populators;

import com.ns8.hybris.core.data.Ns8MerchantData;
import com.ns8.hybris.core.model.NS8MerchantModel;
import de.hybris.platform.converters.Populator;

/**
 * Populates the information of the {@link NS8MerchantModel} into a {@link Ns8MerchantData}
 */
public class Ns8MerchantDataPopulator implements Populator<NS8MerchantModel, Ns8MerchantData> {

    /**
     * {@inheritDoc}
     */
    @Override
    public void populate(final NS8MerchantModel source, final Ns8MerchantData target) {
        target.setEmail(source.getEmail());
        target.setFirstName(source.getFirstName());
        target.setLastName(source.getLastName());
        target.setPhone(source.getPhone());
        target.setStoreUrl(source.getStoreUrl());
    }
}
