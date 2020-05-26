package com.ns8.hybris.core.order.daos;

import de.hybris.platform.core.model.order.OrderModel;
import de.hybris.platform.order.daos.OrderDao;

import java.util.Optional;

/**
 * Dao interface to get Orders from DB
 */
public interface Ns8OrderDao extends OrderDao {

    /**
     * Find the Order for the given order code
     *
     * @param orderCode the order unique code
     * @return Optional<OrderModel>
     */
    Optional<OrderModel> findOrderForCode(String orderCode);
}
