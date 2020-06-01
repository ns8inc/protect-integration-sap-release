package com.ns8.hybris.core.order.dynamic.handlers;

import com.google.gson.Gson;
import de.hybris.platform.core.model.order.OrderModel;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.Map;

/**
 * Abstract dynamic attribute handler
 */
public abstract class Ns8AbstractOrderAttributeHandler {

    /**
     * Gets the attribute for the given attribute name from the order risk event payload
     *
     * @param order         the order model
     * @param attributeName the attribute to get
     * @return the attribute value
     */
    protected Object getDynamicAttribute(final OrderModel order, final String attributeName) {
        if (StringUtils.isBlank(order.getRiskEventPayload())) {
            return null;
        }

        final Map<String, Object> riskEventBodyMap = new Gson().fromJson(order.getRiskEventPayload(), Map.class);
        if (MapUtils.isEmpty(riskEventBodyMap) || !riskEventBodyMap.containsKey(attributeName)) {
            return null;
        }

        return riskEventBodyMap.get(attributeName);

    }
}
