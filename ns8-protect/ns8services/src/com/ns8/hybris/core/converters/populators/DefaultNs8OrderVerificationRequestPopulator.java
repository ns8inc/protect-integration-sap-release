package com.ns8.hybris.core.converters.populators;

import com.ns8.hybris.addon.data.Ns8OrderVerificationData;
import com.ns8.hybris.core.data.Ns8OrderVerificationRequest;
import de.hybris.platform.converters.Populator;
import de.hybris.platform.servicelayer.dto.converter.ConversionException;

/**
 * Populates the information of the {@link Ns8OrderVerificationData} into a {@link Ns8OrderVerificationRequest}
 */
public class DefaultNs8OrderVerificationRequestPopulator implements Populator<Ns8OrderVerificationData, Ns8OrderVerificationRequest> {

    /**
     * {@inheritDoc}
     */
    @Override
    public void populate(final Ns8OrderVerificationData source, final Ns8OrderVerificationRequest target) throws ConversionException {
        target.setOrderId(source.getOrderId());
        target.setToken(source.getToken());
        target.setVerificationId(source.getVerificationId());
        target.setPhone(source.getPhone());
        target.setCode(source.getCode());
        target.setView(source.getTemplate());
        target.setReturnURI(source.getReturnURI());
    }
}
