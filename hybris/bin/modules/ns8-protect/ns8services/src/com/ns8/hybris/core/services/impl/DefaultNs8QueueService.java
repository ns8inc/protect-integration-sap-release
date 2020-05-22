package com.ns8.hybris.core.services.impl;

import com.ns8.hybris.core.data.queue.Ns8QueueMessage;
import com.ns8.hybris.core.data.queue.Ns8ReceiveMessageWrapper;
import com.ns8.hybris.core.integration.exceptions.NS8IntegrationException;
import com.ns8.hybris.core.services.Ns8QueueService;
import com.ns8.hybris.core.services.api.NS8APIService;
import com.ns8.hybris.core.services.api.NS8EndpointService;
import org.apache.http.entity.ContentType;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;

/**
 * Default implementation of {@link Ns8QueueService}
 */
public class DefaultNs8QueueService implements Ns8QueueService {

    protected static final Logger LOG = LogManager.getLogger(DefaultNs8QueueService.class);

    protected static final String API_POLLING_GET_QUEUE_URL = "/api/polling/GetQueueUrl";
    protected static final String API_POLLING_DELETE_QUEUE_MESSAGE = "/api/polling/DeleteQueueMessage";

    protected final RestTemplate restTemplate;
    protected final NS8APIService ns8APIService;
    protected final NS8EndpointService ns8EndpointService;

    public DefaultNs8QueueService(final NS8APIService ns8APIService,
                                  final RestTemplate restTemplate,
                                  final NS8EndpointService ns8EndpointService) {
        this.ns8APIService = ns8APIService;
        this.restTemplate = restTemplate;
        this.ns8EndpointService = ns8EndpointService;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Ns8QueueMessage> receiveMessages(final String queueUrl) {
        try {
            final HttpHeaders headers = new HttpHeaders();
            headers.put(HttpHeaders.ACCEPT, singletonList(ContentType.APPLICATION_JSON.toString()));

            LOG.info("Fetching messages from the queue.");
            final ResponseEntity<Ns8ReceiveMessageWrapper> response = restTemplate.exchange(URI.create(queueUrl), HttpMethod.GET, new HttpEntity<>(headers), Ns8ReceiveMessageWrapper.class);

            final Ns8ReceiveMessageWrapper body = response.getBody();

            final List<Ns8QueueMessage> messages = body.getReceiveMessageResponse().getReceiveMessageResult().getMessages();

            if (messages == null) {
                LOG.info("No Messages found.");
                return emptyList();
            }

            LOG.info("[{}] messages found.", messages::size);
            return messages;
        } catch (final HttpStatusCodeException e) {
            LOG.error("Receiving of messages failed. Status code [{}], error body: [{}].", e::getStatusCode, e::getResponseBodyAsString);
            throw new NS8IntegrationException("Receiving messages failed.", e.getStatusCode(), e);
        } catch (final ResourceAccessException e) {
            LOG.error("Receiving of messages failed due to connection issues. Status code [{}], error body: [{}].", () -> HttpStatus.SERVICE_UNAVAILABLE, e::getMessage);
            throw new NS8IntegrationException("Receiving of messages failed due to connection issues.", HttpStatus.SERVICE_UNAVAILABLE, e);
        }

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean deleteMessage(final String merchantApiKey, final String receiptHandle) {
        final HttpHeaders headers = createHttpHeaderWithAuth(merchantApiKey);

        final UriComponents uri = UriComponentsBuilder.fromUriString(ns8EndpointService.getBaseClientURL())
                .path(API_POLLING_DELETE_QUEUE_MESSAGE)
                .queryParam("receiptHandle", encodeReceiptHandle(receiptHandle))
                .build();

        LOG.debug("Delete message call url: [{}]", uri::toString);

        try {
            LOG.info("Deleting message with handle [{}] from the queue for the merchant key [{}].", () -> receiptHandle, () -> merchantApiKey);
            final ResponseEntity<Map<String, Boolean>> response = restTemplate.exchange(uri.toString(), HttpMethod.POST, new HttpEntity<>(headers), new ParameterizedTypeReference<>() {
            });
            return response.getBody().get("success");
        } catch (final HttpStatusCodeException e) {
            LOG.error("Deletion of the message failed. Status code [{}], error body: [{}].", e::getStatusCode, e::getResponseBodyAsString);
            throw new NS8IntegrationException("Deletion of the message failed.", e.getStatusCode(), e);
        } catch (final ResourceAccessException e) {
            LOG.error("Deletion of the message failed due to connection issues. Status code [{}], error body: [{}].", () -> HttpStatus.SERVICE_UNAVAILABLE, e::getMessage);
            throw new NS8IntegrationException("Deletion of the message failed due to connection issues.", HttpStatus.SERVICE_UNAVAILABLE, e);
        }

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getQueueUrl(final String merchantApiKey) {
        final String baseClientURL = ns8EndpointService.getBaseClientURL();

        final HttpHeaders headers = createHttpHeaderWithAuth(merchantApiKey);
        final HttpEntity<Object> entity = new HttpEntity<>(headers);

        try {
            LOG.info("Fetching the queue url for the merchant key [{}].", () -> merchantApiKey);
            final ResponseEntity<Map<String, String>> response = restTemplate.exchange(baseClientURL + API_POLLING_GET_QUEUE_URL, HttpMethod.POST, entity, new ParameterizedTypeReference<>() {
            });
            return response.getBody().get("url");
        } catch (final HttpStatusCodeException e) {
            LOG.error("Retrieval of the queue url failed. Status code [{}], error body: [{}].", e::getStatusCode, e::getResponseBodyAsString);
            throw new NS8IntegrationException("Retrieval of the queue url failed.", e.getStatusCode(), e);
        } catch (final ResourceAccessException e) {
            LOG.error("Retrieval of the queue url failed due to connection issues. Status code [{}], error body: [{}].", () -> HttpStatus.SERVICE_UNAVAILABLE, e::getMessage);
            throw new NS8IntegrationException("Retrieval of the queue url failed due to connection issues.", HttpStatus.SERVICE_UNAVAILABLE, e);
        }
    }

    private HttpHeaders createHttpHeaderWithAuth(final String merchantApiKey) {
        final HttpHeaders headers = new HttpHeaders();
        headers.put(HttpHeaders.AUTHORIZATION, singletonList("Bearer " + merchantApiKey));
        return headers;
    }

    private String encodeReceiptHandle(final String receiptHandle) {
        try {
            return URLEncoder.encode(receiptHandle, StandardCharsets.UTF_8.toString());
        } catch (final UnsupportedEncodingException e) {
            LOG.error("Error while encoding the message receipt handle.");
            throw new NS8IntegrationException("Deletion of the message failed.", HttpStatus.INTERNAL_SERVER_ERROR, e);
        }
    }

}
