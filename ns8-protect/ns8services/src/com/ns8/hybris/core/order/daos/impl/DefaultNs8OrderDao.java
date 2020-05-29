package com.ns8.hybris.core.order.daos.impl;

import com.google.common.base.Preconditions;
import com.ns8.hybris.core.order.daos.Ns8OrderDao;
import de.hybris.platform.core.model.order.OrderModel;
import de.hybris.platform.order.daos.impl.DefaultOrderDao;
import de.hybris.platform.servicelayer.search.FlexibleSearchQuery;
import de.hybris.platform.servicelayer.search.FlexibleSearchService;
import de.hybris.platform.servicelayer.search.SearchResult;
import org.apache.commons.lang.StringUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static java.util.Optional.empty;

/**
 * Default implementation of {@link Ns8OrderDao}
 */
public class DefaultNs8OrderDao extends DefaultOrderDao implements Ns8OrderDao {

    protected static final String ORDER_CODE_QUERY_PARAM = "orderCode";

    protected static final String FIND_ORDER_BY_CODE_QUERY = "SELECT {" + OrderModel.PK + "} AS PK FROM {" +
            OrderModel._TYPECODE + "!} WHERE {" + OrderModel.CODE + "} = ?" + ORDER_CODE_QUERY_PARAM +
            " AND {" + OrderModel.ORIGINALVERSION + "} IS NULL";

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<OrderModel> findOrderForCode(final String orderCode) {
        Preconditions.checkArgument(StringUtils.isNotBlank(orderCode), "The order code cannot be null.");

        final Map<String, Object> queryParams = new HashMap<>();
        queryParams.put(ORDER_CODE_QUERY_PARAM, orderCode);

        final FlexibleSearchQuery fQuery = new FlexibleSearchQuery(FIND_ORDER_BY_CODE_QUERY);
        fQuery.addQueryParameters(queryParams);

        final SearchResult<OrderModel> searchResult = getSuperFlexibleSearchService().search(fQuery);
        return searchResult.getResult().isEmpty() ? empty() : Optional.of(searchResult.getResult().get(0));
    }

    protected FlexibleSearchService getSuperFlexibleSearchService() {
        return getFlexibleSearchService();
    }

}
