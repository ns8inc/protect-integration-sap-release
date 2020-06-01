package com.ns8.hybris.core.converters.populators;

import com.ns8.hybris.core.data.Ns8LineItemData;
import de.hybris.platform.converters.Populator;
import de.hybris.platform.core.model.order.OrderEntryModel;
import de.hybris.platform.core.model.product.ProductModel;
import de.hybris.platform.servicelayer.dto.converter.ConversionException;
import de.hybris.platform.util.DiscountValue;
import org.springframework.util.Assert;

/**
 * Populates the information of the {@link OrderEntryModel} into a {@link Ns8LineItemData}
 */
public class DefaultNs8LineItemDataPopulator implements Populator<OrderEntryModel, Ns8LineItemData> {

    /**
     * {@inheritDoc}
     */
    @Override
    public void populate(final OrderEntryModel source, final Ns8LineItemData target) throws ConversionException {
        final ProductModel product = source.getProduct();
        Assert.notNull(product, "orderEntry.product must not be null");

        target.setEan13(product.getEan());
        target.setManufacturer(product.getManufacturerName());
        target.setName(product.getName());
        target.setPlatformProductId(product.getCode());
        target.setTitle(product.getDescription());
        target.setSku(getSku(source));

        target.setTotalDiscount(getTotalDiscount(source));
        target.setPlatformId(getEntryPlatformId(source));
        target.setPrice(source.getTotalPrice());
        target.setQuantity(source.getQuantity());
    }

    /**
     * Gets the Product Sku from the order entry
     *
     * @param source the order entry
     * @return product sku
     */
    protected String getSku(final OrderEntryModel source) {
        return source.getProduct().getManufacturerAID();
    }

    /**
     * Gets the order entry total discount
     *
     * @param source the order entry
     * @return sum of all discounts
     */
    protected Double getTotalDiscount(final OrderEntryModel source) {
        return source.getDiscountValues().stream().mapToDouble(DiscountValue::getAppliedValue).sum();
    }

    /**
     * Gets the order entry platformId
     *
     * @param source the order entry
     * @return the platform id
     */
    protected String getEntryPlatformId(final OrderEntryModel source) {
        return String.format("%s-%s", source.getOrder().getCode(), source.getEntryNumber());
    }
}
