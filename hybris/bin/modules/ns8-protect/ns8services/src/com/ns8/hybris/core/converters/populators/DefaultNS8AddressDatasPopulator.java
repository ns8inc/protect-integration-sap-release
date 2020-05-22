package com.ns8.hybris.core.converters.populators;

import com.ns8.hybris.core.data.NS8AddressData;
import com.ns8.hybris.core.data.NS8AddressType;
import de.hybris.platform.converters.Populator;
import de.hybris.platform.core.model.c2l.CountryModel;
import de.hybris.platform.core.model.c2l.RegionModel;
import de.hybris.platform.core.model.order.OrderModel;
import de.hybris.platform.core.model.user.AddressModel;
import de.hybris.platform.servicelayer.dto.converter.ConversionException;
import org.springframework.util.Assert;

import java.util.List;

/**
 * Populates the information of the {@link OrderModel} into a {@link NS8AddressData}
 */
public class DefaultNS8AddressDatasPopulator implements Populator<OrderModel, List<NS8AddressData>> {

    /**
     * {@inheritDoc}
     */
    @Override
    public void populate(final OrderModel source, final List<NS8AddressData> target) throws ConversionException {
        final AddressModel sourcePaymentAddress = source.getPaymentAddress();
        final AddressModel sourceDeliveryAddress = source.getDeliveryAddress();
        Assert.notNull(sourcePaymentAddress, "order must have a payment address");

        final NS8AddressData billingAddress = createAddress(sourcePaymentAddress);
        billingAddress.setType(NS8AddressType.BILLING);
        target.add(billingAddress);
        if (sourceDeliveryAddress != null) {
            final NS8AddressData shippingAddress = createAddress(sourceDeliveryAddress);
            shippingAddress.setType(NS8AddressType.SHIPPING);
            target.add(shippingAddress);
        }
    }

    /**
     * Created a {@link NS8AddressData} from the address model
     *
     * @param source the address model
     * @return the NS8 address data
     */
    protected NS8AddressData createAddress(final AddressModel source) {
        final NS8AddressData address = new NS8AddressData();
        address.setAddress1(source.getLine1());
        address.setAddress2(source.getLine2());
        address.setCity(source.getTown());
        address.setCompany(source.getCompany());

        final CountryModel country = source.getCountry();
        if (country != null) {
            address.setCountry(country.getName());
            address.setCountryCode(country.getIsocode());
        }

        final RegionModel region = source.getRegion();
        if (region != null) {
            address.setRegion(region.getName());
            address.setRegionCode(region.getIsocode());
        }
        address.setZip(source.getPostalcode());
        address.setName(String.format("%s %s", source.getFirstname(), source.getLastname()));
        return address;
    }
}
