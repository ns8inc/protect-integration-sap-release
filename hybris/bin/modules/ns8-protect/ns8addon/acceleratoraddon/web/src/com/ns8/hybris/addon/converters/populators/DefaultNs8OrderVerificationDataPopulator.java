package com.ns8.hybris.addon.converters.populators;

import com.ns8.hybris.addon.data.Ns8OrderVerificationData;
import com.ns8.hybris.addon.forms.Ns8OrderVerificationForm;
import de.hybris.platform.converters.Populator;
import de.hybris.platform.servicelayer.dto.converter.ConversionException;

/**
 * Populates the information of the {@link Ns8OrderVerificationForm} into a {@link Ns8OrderVerificationData}
 */
public class DefaultNs8OrderVerificationDataPopulator implements Populator<Ns8OrderVerificationForm, Ns8OrderVerificationData> {

    /**
     * {@inheritDoc}
     */
    @Override
    public void populate(final Ns8OrderVerificationForm source, final Ns8OrderVerificationData target) throws ConversionException {
        target.setOrderId(source.getOrderId());
        target.setToken(source.getToken());
        target.setVerificationId(source.getVerificationId());
        target.setPhone(source.getPhone());
        target.setCode(source.getCode());
    }
}
