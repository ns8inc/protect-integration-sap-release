package com.ns8.hybris.notifications.daos.impl;

import com.ns8.hybris.notifications.daos.Ns8QueueMessageDao;
import com.ns8.hybris.notifications.enums.Ns8MessageStatus;
import com.ns8.hybris.notifications.model.Ns8QueueMessageModel;
import de.hybris.platform.servicelayer.search.FlexibleSearchQuery;
import de.hybris.platform.servicelayer.search.FlexibleSearchService;
import de.hybris.platform.servicelayer.search.SearchResult;

import java.util.*;

import static de.hybris.platform.servicelayer.util.ServicesUtil.validateParameterNotNull;

/**
 * Default implementation of {@link Ns8QueueMessageDao}
 */
public class DefaultNs8QueueMessageDao implements Ns8QueueMessageDao {

    protected static final String MESSAGE_STATUS_QUERY_PARAM = "status";
    protected static final String QUERY_PARAM_QUEUE_MESSAGE_STATUS = "ns8QueueMessageStatus";
    protected static final String QUERY_PARAM_CREATED_BEFORE_DATE = "createdBeforeDate";
    protected static final String MESSAGE_ID_QUERY_PARAM = "messageId";
    protected static final String FROM = "} FROM {";

    protected static final String FIND_NS8_MESSAGES_TO_PROCESS_QUERY = "SELECT {qm." + Ns8QueueMessageModel.PK +
            FROM + Ns8QueueMessageModel._TYPECODE + " as qm } WHERE {qm." + Ns8QueueMessageModel.STATUS +
            "} = ?" + MESSAGE_STATUS_QUERY_PARAM;

    protected static final String FIND_NS8_MESSAGES_BY_ID_QUERY = "SELECT {qm." + Ns8QueueMessageModel.PK +
            FROM + Ns8QueueMessageModel._TYPECODE + " as qm } WHERE {qm." + Ns8QueueMessageModel.MESSAGEID +
            "} = ?" + MESSAGE_ID_QUERY_PARAM;

    protected static final String FIND_QUEUE_MESSAGES_BY_STATUS_AND_CREATION_DATE_QUERY =
            "SELECT {msg." + Ns8QueueMessageModel.PK +
                    FROM + Ns8QueueMessageModel._TYPECODE + " as msg } WHERE {msg." + Ns8QueueMessageModel.STATUS +
                    "}= ?" + QUERY_PARAM_QUEUE_MESSAGE_STATUS +
                    " AND {msg." + Ns8QueueMessageModel.CREATIONTIME + "} < ?" + QUERY_PARAM_CREATED_BEFORE_DATE;
    protected final FlexibleSearchService flexibleSearchService;

    public DefaultNs8QueueMessageDao(final FlexibleSearchService flexibleSearchService) {
        this.flexibleSearchService = flexibleSearchService;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Ns8QueueMessageModel> findPendingNs8QueueMessages() {
        final Map<String, Object> queryParams = new HashMap<>();
        queryParams.put(MESSAGE_STATUS_QUERY_PARAM, Ns8MessageStatus.PENDING);

        final FlexibleSearchQuery fQuery = new FlexibleSearchQuery(FIND_NS8_MESSAGES_TO_PROCESS_QUERY);
        fQuery.addQueryParameters(queryParams);
        fQuery.setResultClassList(Collections.singletonList(Ns8QueueMessageModel.class));

        final SearchResult<Ns8QueueMessageModel> searchResult = flexibleSearchService.search(fQuery);
        return searchResult.getResult();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<Ns8QueueMessageModel> findNs8QueueMessageById(final String messageId) {
        final Map<String, Object> queryParams = new HashMap<>();
        queryParams.put(MESSAGE_ID_QUERY_PARAM, messageId);

        final FlexibleSearchQuery fQuery = new FlexibleSearchQuery(FIND_NS8_MESSAGES_BY_ID_QUERY);
        fQuery.addQueryParameters(queryParams);
        fQuery.setResultClassList(Collections.singletonList(Ns8QueueMessageModel.class));

        final SearchResult<Ns8QueueMessageModel> searchResult = flexibleSearchService.search(fQuery);
        return searchResult.getCount() == 0 ? Optional.empty() : Optional.of(searchResult.getResult().get(0));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Ns8QueueMessageModel> findNs8QueueMessagesByStatusCreatedBeforeDate(final Ns8MessageStatus messageStatus, final Date creationDate) {
        validateParameterNotNull(messageStatus, "Queue message Status cannot be null");
        validateParameterNotNull(creationDate, "Creation Date cannot be null");

        final FlexibleSearchQuery query = new FlexibleSearchQuery(FIND_QUEUE_MESSAGES_BY_STATUS_AND_CREATION_DATE_QUERY);
        query.addQueryParameter(QUERY_PARAM_QUEUE_MESSAGE_STATUS, messageStatus);
        query.addQueryParameter(QUERY_PARAM_CREATED_BEFORE_DATE, creationDate);

        final SearchResult<Ns8QueueMessageModel> result = flexibleSearchService.search(query);
        return result.getResult();
    }
}
