package com.ns8.hybris.core.converters.populators;

import com.ns8.hybris.core.data.Ns8CustomerData;
import com.ns8.hybris.core.data.Ns8TransactionData;
import de.hybris.platform.converters.Populator;
import de.hybris.platform.core.model.order.OrderModel;
import de.hybris.platform.core.model.user.AddressModel;
import de.hybris.platform.core.model.user.CustomerModel;
import de.hybris.platform.core.model.user.UserModel;
import de.hybris.platform.servicelayer.dto.converter.ConversionException;
import org.apache.commons.lang.StringUtils;
import org.springframework.util.Assert;

/**
 * Populates the information of the {@link OrderModel} into a {@link Ns8TransactionData}
 */
public class DefaultNs8CustomerDataPopulator implements Populator<OrderModel, Ns8CustomerData> {

    /**
     * {@inheritDoc}
     */
    @Override
    public void populate(final OrderModel source, final Ns8CustomerData target) throws ConversionException {
        final UserModel user = source.getUser();
        Assert.isInstanceOf(CustomerModel.class, user, "order.user must be a customer");
        final CustomerModel customer = (CustomerModel) user;

        final String[] firstNameLastName = getFirstAndLastName(customer);
        target.setFirstName(firstNameLastName[0]);
        target.setLastName(firstNameLastName[1]);
        target.setPhone(getPhone(source));
        target.setEmail(customer.getContactEmail());

        target.setPlatformCreatedAt(customer.getCreationtime());
        target.setPlatformId(customer.getUid());

        target.setCompany(source.getPaymentAddress().getCompany());
    }

    /**
     * Gets the customer's phone number from the billing address
     *
     * @param order the order model
     * @return customer's phone number
     */
    protected String getPhone(final OrderModel order) {
        final AddressModel paymentAddress = order.getPaymentAddress();
        return StringUtils.isNotBlank(paymentAddress.getPhone1()) ? paymentAddress.getPhone1() : paymentAddress.getPhone2();
    }

    /**
     * Gets the customer's first and last name
     *
     * @param customer the customer model
     * @return customer's first name and last name concatenated
     */
    protected String[] getFirstAndLastName(final CustomerModel customer) {
        final String name = customer.getName();
        final String[] firstNameLastName = new String[2];
        firstNameLastName[0] = name.split(" ")[0];
        firstNameLastName[1] = name.substring(name.indexOf(' ')).trim();
        return firstNameLastName;
    }
}
