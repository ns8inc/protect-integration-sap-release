package com.ns8.hybris.core.services.api.impl;

import com.google.common.base.Preconditions;
import com.google.gson.*;
import com.ns8.hybris.core.data.*;
import com.ns8.hybris.core.integration.exceptions.Ns8IntegrationException;
import com.ns8.hybris.core.model.NS8MerchantModel;
import com.ns8.hybris.core.services.api.Ns8ApiService;
import com.ns8.hybris.core.services.api.Ns8EndpointService;
import de.hybris.platform.core.model.order.OrderModel;
import de.hybris.platform.servicelayer.config.ConfigurationService;
import de.hybris.platform.servicelayer.dto.converter.Converter;
import de.hybris.platform.servicelayer.model.ModelService;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.http.entity.ContentType;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.*;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Base64;
import java.util.Map;

import static com.ns8.hybris.core.constants.Ns8servicesConstants.*;
import static java.util.Collections.singletonList;

/**
 * Default implementation of {@link Ns8ApiService}
 */
public class DefaultNs8ApiService implements Ns8ApiService {

    protected static final Logger LOG = LogManager.getLogger(DefaultNs8ApiService.class);

    protected static final String NS_8_SERVICES_PLATFORM_NAME_CONFIGURATION_KEY = "ns8services.platform.name";
    protected static final String ERROR_KEY = "error";
    protected static final String BEARER = "Bearer ";

    protected final RestTemplate restTemplate;
    protected final Converter<NS8MerchantModel, Ns8PluginInstallRequest> ns8PluginInstallRequestConverter;
    protected final Converter<OrderModel, Ns8UpdateOrderStatus> ns8UpdateOrderStatusConverter;
    protected final Converter<OrderModel, Ns8OrderData> ns8OrderDataConverter;
    protected final ConfigurationService configurationService;
    protected final Ns8EndpointService ns8EndpointService;
    protected final ModelService modelService;

    public DefaultNs8ApiService(final RestTemplate restTemplate,
                                final Converter<NS8MerchantModel, Ns8PluginInstallRequest> ns8PluginInstallRequestConverter,
                                final Converter<OrderModel, Ns8UpdateOrderStatus> ns8UpdateOrderStatusConverter,
                                final Converter<OrderModel, Ns8OrderData> ns8OrderDataConverter,
                                final ConfigurationService configurationService,
                                final Ns8EndpointService ns8EndpointService,
                                final ModelService modelService) {
        this.restTemplate = restTemplate;
        this.ns8PluginInstallRequestConverter = ns8PluginInstallRequestConverter;
        this.ns8UpdateOrderStatusConverter = ns8UpdateOrderStatusConverter;
        this.ns8OrderDataConverter = ns8OrderDataConverter;
        this.configurationService = configurationService;
        this.ns8EndpointService = ns8EndpointService;
        this.modelService = modelService;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void triggerPluginInstallEvent(final NS8MerchantModel ns8Merchant) {
        final String platform = configurationService.getConfiguration().getString(NS_8_SERVICES_PLATFORM_NAME_CONFIGURATION_KEY);
        final String backendAPIURL = ns8EndpointService.getBaseBackendURL();

        final Ns8PluginInstallRequest ns8PluginInstallRequest = ns8PluginInstallRequestConverter.convert(ns8Merchant);
        final String requestUrl = backendAPIURL + PROTECT_PLATFORM_INSTALL_URL + platform;

        final ResponseEntity<PluginInstallResponseData> responseEntity;

        try {
            responseEntity = restTemplate.postForEntity(requestUrl, new HttpEntity(ns8PluginInstallRequest), PluginInstallResponseData.class);
            updateMerchantFromNS8Response(ns8Merchant, responseEntity.getBody());
        } catch (final HttpStatusCodeException e) {
            LOG.error("Installation of NS8 Merchant failed. URL: [{}], payload: [{}], status code [{}], error body: [{}].", () -> requestUrl, () -> prettyPrint(ns8PluginInstallRequest), e::getStatusCode, e::getResponseBodyAsString);
            final String errorDetails = getErrorDetails(e);
            throw new Ns8IntegrationException("Installation of NS8 Merchant failed. " + errorDetails, e.getStatusCode(), e);
        } catch (final ResourceAccessException e) {
            LOG.error("Installation of NS8 Merchant failed due to connection issues. URL: [{}], payload: [{}], status code [{}], error body: [{}].",
                    () -> requestUrl, () -> prettyPrint(ns8PluginInstallRequest), () -> HttpStatus.SERVICE_UNAVAILABLE, e::getMessage);
            throw new Ns8IntegrationException("Installation of NS8 Merchant failed due to connection issues.", HttpStatus.SERVICE_UNAVAILABLE, e);
        }

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void triggerMerchantUninstallEvent(final NS8MerchantModel ns8Merchant) {
        final String requestUrl = buildUriComponents(UNINSTALL_MERCHANT_ACTION).toString();
        final HttpHeaders headers = buildHeadersWithAuthorization(ns8Merchant.getApiKey());

        try {
            final ResponseEntity<Void> responseEntity = restTemplate.postForEntity(requestUrl, new HttpEntity<>(headers), Void.class);
            LOG.debug("Merchant [{}] is deactivated successfully - Response code: [{}]", ns8Merchant.getEmail(), responseEntity.getStatusCode());
        } catch (final HttpStatusCodeException e) {
            LOG.error("Deactivation of NS8 Merchant failed. URL: [{}], status code [{}], error body: [{}].", () -> requestUrl, e::getStatusCode, e::getResponseBodyAsString);
            throw new Ns8IntegrationException("NS8 merchant uninstall failed.", e.getStatusCode(), e);
        } catch (final ResourceAccessException e) {
            LOG.error("Deactivation of NS8 Merchant failed due to connection issues. URL: [{}], status code [{}], error body: [{}].",
                    () -> requestUrl, () -> HttpStatus.SERVICE_UNAVAILABLE, e::getMessage);
            throw new Ns8IntegrationException("NS8 merchant uninstall failed due to connection issues.", HttpStatus.SERVICE_UNAVAILABLE, e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean triggerMerchantReinstallEvent(final NS8MerchantModel ns8Merchant) {
        final Ns8PluginInstallRequest ns8PluginInstallRequest = ns8PluginInstallRequestConverter.convert(ns8Merchant);
        final String backendAPIURL = ns8EndpointService.getBaseBackendURL();
        final String platform = configurationService.getConfiguration().getString(NS_8_SERVICES_PLATFORM_NAME_CONFIGURATION_KEY);
        final String requestUrl = backendAPIURL + PROTECT_PLATFORM_REINSTALL_URL + platform;

        final HttpEntity<Object> request = buildHttpEntity(ns8Merchant.getApiKey(), ns8PluginInstallRequest);
        final ResponseEntity<MerchantReactivateResponseData> responseEntity;

        try {
            responseEntity = restTemplate.postForEntity(requestUrl, request, MerchantReactivateResponseData.class);
            LOG.debug("Merchant [{}] is reactivated successfully - Response code: [{}]", ns8Merchant.getEmail(), responseEntity.getStatusCode());
        } catch (final HttpStatusCodeException e) {
            LOG.error("Reactivation of NS8 Merchant failed. URL: [{}], status code [{}], error body: [{}].", () -> requestUrl, e::getStatusCode, e::getResponseBodyAsString);
            throw new Ns8IntegrationException("NS8 merchant reinstall failed.", e.getStatusCode(), e);
        } catch (final ResourceAccessException e) {
            LOG.error("Reactivation of NS8 Merchant failed due to connection issues. URL: [{}], status code [{}], error body: [{}].",
                    () -> requestUrl, () -> HttpStatus.SERVICE_UNAVAILABLE, e::getMessage);
            throw new Ns8IntegrationException("NS8 merchant reinstall failed due to connection issues.", HttpStatus.SERVICE_UNAVAILABLE, e);
        }
        return responseEntity.getBody().getSuccess();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String fetchTrueStatsScript(final NS8MerchantModel ns8Merchant) {
        final String clientAPIURL = ns8EndpointService.getBaseClientURL();
        final HttpHeaders headers = buildHeadersWithAuthorization(ns8Merchant.getApiKey());
        final HttpEntity<String> entity = new HttpEntity<>(headers);

        try {
            final String response = restTemplate.postForObject(clientAPIURL + "/api/init/script/", entity, String.class);
            return this.sanitiseTrueStatsScript(response);
        } catch (final HttpStatusCodeException e) {
            LOG.error("Fetching true stats failed. Status code [{}], error body: [{}].", e::getStatusCode, e::getResponseBodyAsString);
            throw new Ns8IntegrationException("Fetching true stats failed.", e.getStatusCode(), e);
        } catch (final ResourceAccessException ex) {
            LOG.error("Fetching true stats failed due to connection issues. Status code [{}], error body: [{}].", () -> HttpStatus.SERVICE_UNAVAILABLE, ex::getMessage);
            throw new Ns8IntegrationException("Fetching true stats failed due to connection issues.", HttpStatus.SERVICE_UNAVAILABLE, ex);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void triggerCreateOrderActionEvent(final OrderModel order) {
        final UriComponents uri = buildUriComponents(CREATE_ORDER_ACTION);
        final Ns8OrderData ns8OrderData = ns8OrderDataConverter.convert(order);

        Preconditions.checkArgument(order.getSite() != null && order.getSite().getNs8Merchant() != null
                && StringUtils.isNotBlank(order.getSite().getNs8Merchant().getApiKey()), "The merchant api key is mandatory to trigger the order creation.");
        final HttpEntity<Object> request = buildHttpEntity(order.getSite().getNs8Merchant().getApiKey(), ns8OrderData);

        try {
            LOG.info("Sending order [{}] to NS8 for fraud check", order::getCode);
            final ResponseEntity<Void> responseEntity = restTemplate.postForEntity(uri.toString(), request, Void.class);
            LOG.debug("Order [{}] sent successfully - Response code: [{}]", order.getCode(), responseEntity.getStatusCode());
        } catch (final HttpStatusCodeException e) {
            LOG.error("Sending order to NS8 failed. URL: [{}], payload: [{}], status code [{}], error body: [{}].", uri::toString, () -> prettyPrint(ns8OrderData), e::getStatusCode, e::getResponseBodyAsString);
            throw new Ns8IntegrationException("Installation of NS8 Merchant failed.", e.getStatusCode(), e);
        } catch (final ResourceAccessException ex) {
            LOG.error("Sending order to NS8 failed due to connection issues. URL: [{}], payload: [{}], status code [{}], error body: [{}].",
                    uri::toString, () -> prettyPrint(ns8OrderData), () -> HttpStatus.SERVICE_UNAVAILABLE, ex::getMessage);
            throw new Ns8IntegrationException("Sending order to NS8 failed due to connection issues.", HttpStatus.SERVICE_UNAVAILABLE, ex);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void triggerUpdateOrderStatusAction(final OrderModel order) {
        final UriComponents uri = buildUriComponents(UPDATE_ORDER_STATUS_ACTION);
        final Ns8UpdateOrderStatus ns8UpdateOrderStatus = ns8UpdateOrderStatusConverter.convert(order);

        Preconditions.checkArgument(order.getSite() != null && order.getSite().getNs8Merchant() != null
                && StringUtils.isNotBlank(order.getSite().getNs8Merchant().getApiKey()), "The merchant api key is mandatory to trigger the update order status.");
        final HttpEntity<Object> request = buildHttpEntity(order.getSite().getNs8Merchant().getApiKey(), ns8UpdateOrderStatus);

        try {
            LOG.info("Sending order status update [{}] to NS8 for fraud check", order::getCode);
            LOG.debug("Payload: [{}]", () -> prettyPrint(ns8UpdateOrderStatus));
            final ResponseEntity<Void> responseEntity = restTemplate.postForEntity(uri.toString(), request, Void.class);
            LOG.debug("Order status update [{}] sent successfully - Response code: [{}]", order.getCode(), responseEntity.getStatusCode());
        } catch (final HttpStatusCodeException e) {
            LOG.error("Sending order status update to NS8 failed. URL: [{}], payload: [{}], status code [{}], error body: [{}].", uri::toString, () -> prettyPrint(ns8UpdateOrderStatus), e::getStatusCode, e::getResponseBodyAsString);
            throw new Ns8IntegrationException("NS8 order status update failed.", e.getStatusCode(), e);
        } catch (final ResourceAccessException ex) {
            LOG.error("Update status event could not be completed due to connection issues. URL: [{}], payload: [{}], status code [{}], error body: [{}].",
                    uri::toString, () -> prettyPrint(ns8UpdateOrderStatus), () -> HttpStatus.SERVICE_UNAVAILABLE, ex::getMessage);
            throw new Ns8IntegrationException("Update status event failed due to connection issues.", HttpStatus.SERVICE_UNAVAILABLE, ex);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getVerificationTemplate(final Ns8OrderVerificationRequest orderVerificationRequest, final String apiKey) {
        final UriComponents uri = getGetTemplateCallUriComponents(orderVerificationRequest);
        final HttpHeaders headers = buildHeadersWithAuthorization(apiKey);

        final String template = orderVerificationRequest.getView();
        try {
            LOG.debug("Getting verification template [{}] from NS8", template);
            final ResponseEntity<String> responseEntity = restTemplate.getForEntity(uri.toString(), String.class, headers);
            return getNs8OrderVerificationContent(responseEntity);
        } catch (final HttpStatusCodeException e) {
            LOG.error("Could not retrieve template [{}]. URL: [{}], status code [{}], error body: [{}].", () -> template, uri::toString, e::getStatusCode, e::getResponseBodyAsString);
            throw new Ns8IntegrationException("Could not retrieve verification template", e.getStatusCode(), e);
        } catch (final ResourceAccessException ex) {
            LOG.error("Could not retrieve template [{}] due to connection issues. URL: [{}], status code [{}], error body: [{}].",
                    () -> template, uri::toString, () -> HttpStatus.SERVICE_UNAVAILABLE, ex::getMessage);
            throw new Ns8IntegrationException("Could not retrieve template from NS8 due to connection issues.", HttpStatus.SERVICE_UNAVAILABLE, ex);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Ns8OrderVerificationResponse sendVerification(final Ns8OrderVerificationRequest orderVerificationRequest, final String apiKey) {
        final UriComponents uri = getPostTemplateCallUriComponents();
        final HttpEntity<Object> request = buildHttpEntityForVerificationCall(orderVerificationRequest, apiKey);

        final String template = orderVerificationRequest.getView();
        try {
            LOG.debug("Getting verification template [{}] from NS8", template);
            final ResponseEntity<String> responseEntity = restTemplate.postForEntity(uri.toString(), request, String.class);
            LOG.debug("Template [{}] retrieved successfully - Response code: [{}]", template, responseEntity.getStatusCode());
            return new GsonBuilder().create().fromJson(responseEntity.getBody(), Ns8OrderVerificationResponse.class);
        } catch (final HttpStatusCodeException e) {
            LOG.error("Could not retrieve template [{}]. URL: [{}], status code [{}], error body: [{}].", () -> template, uri::toString, e::getStatusCode, e::getResponseBodyAsString);
            throw new Ns8IntegrationException("Could not retrieve verification template", e.getStatusCode(), e);
        } catch (final ResourceAccessException ex) {
            LOG.error("Could not retrieve template [{}] due to connection issues. URL: [{}], status code [{}], error body: [{}].",
                    () -> template, uri::toString, () -> HttpStatus.SERVICE_UNAVAILABLE, ex::getMessage);
            throw new Ns8IntegrationException("Could not retrieve template from NS8 due to connection issues.", HttpStatus.SERVICE_UNAVAILABLE, ex);
        }
    }

    /**
     * Generates the URI components with a given set of parameters
     *
     * @param orderVerificationRequest the order verification request object
     */
    protected UriComponents getGetTemplateCallUriComponents(final Ns8OrderVerificationRequest orderVerificationRequest) {
        return UriComponentsBuilder.fromUriString(ns8EndpointService.getBaseClientURL())
                .path(API_TEMPLATE_URL)
                .queryParam("orderId", orderVerificationRequest.getOrderId())
                .queryParam("token", orderVerificationRequest.getToken())
                .queryParam("verificationId", orderVerificationRequest.getVerificationId())
                .queryParam("view", orderVerificationRequest.getView())
                .queryParam("returnUri", orderVerificationRequest.getReturnURI())
                .build();
    }

    /**
     * Generates the URI components of a call
     */
    protected UriComponents getPostTemplateCallUriComponents() {
        return UriComponentsBuilder.fromUriString(ns8EndpointService.getBaseClientURL())
                .path(API_TEMPLATE_URL)
                .build();
    }

    /**
     * Updates the NS8 merchant from the response data
     *
     * @param ns8Merchant  the NS8 merchant to be updated
     * @param responseData the response data
     */
    protected void updateMerchantFromNS8Response(final NS8MerchantModel ns8Merchant, final PluginInstallResponseData responseData) {
        ns8Merchant.setApiKey(responseData.getAccessToken());
        ns8Merchant.setQueueId(responseData.getQueueId());
        ns8Merchant.setEnabled(true);
        modelService.save(ns8Merchant);
    }

    /**
     * Checks if the exception contains an error details, and if found returns the error message
     *
     * @param e the HttpStatusCodeException
     * @return the error details
     */
    protected String getErrorDetails(final HttpStatusCodeException e) {
        final Map<String, Object> errorBody = new Gson().fromJson(e.getResponseBodyAsString(), Map.class);
        String errorDetails = StringUtils.EMPTY;
        if (MapUtils.isNotEmpty(errorBody) && errorBody.containsKey(ERROR_KEY)) {
            errorDetails = (String) errorBody.get(ERROR_KEY);
        }
        return errorDetails;
    }

    protected String prettyPrint(final Object requestBody) {
        final Gson gson = new GsonBuilder().setPrettyPrinting().create();
        return gson.toJson(requestBody).replace("\\n", "\n").replace("\\\"", "\"");
    }

    private String sanitiseTrueStatsScript(final String rawScript) {
        String sanitisedScript = StringEscapeUtils.unescapeJavaScript(rawScript);
        /* In order to parse the requested JavaScript as a script and not a string we must remove quotes
         * from the start and end of string. In order to future proof the API call we check to see if it is
         * surrounded before removing the quotes */
        if (sanitisedScript.charAt(0) == '"') {
            sanitisedScript = sanitisedScript.substring(1, sanitisedScript.length() - 1);
        }
        return sanitisedScript;
    }

    /**
     * Generates URI components for action event calls. Includes the action code
     * of the operation that will be performed
     *
     * @param actionCode the action code of the operation
     * @return the Uri components
     */
    protected UriComponents buildUriComponents(final String actionCode) {
        final String clientAPIURL = ns8EndpointService.getBaseClientURL();
        return UriComponentsBuilder.fromUriString(clientAPIURL)
                .path(API_SWITCH_EXECUTOR_URL)
                .queryParam("action", actionCode)
                .build();
    }

    /**
     * Creates an HTTP entity with the given merchant API Key
     *
     * @param apiKey the merchant api key
     * @param object the object that is added to the HTTP entity model
     * @return the http headers
     */
    protected HttpEntity<Object> buildHttpEntity(final String apiKey, final Object object) {
        final HttpHeaders headers = buildHeadersWithAuthorization(apiKey);
        headers.put(HttpHeaders.CONTENT_TYPE, singletonList(ContentType.APPLICATION_JSON.toString()));
        return new HttpEntity<>(object, headers);
    }

    /**
     * Creates an HTTP entity that holds a form and uses an order's merchant API Key
     *
     * @param orderVerificationRequest the order verification request object
     * @param apiKey                   The API key for the header
     * @return the http headers
     */
    protected HttpEntity<Object> buildHttpEntityForVerificationCall(final Ns8OrderVerificationRequest orderVerificationRequest, final String apiKey) {
        final HttpHeaders headers = buildHeadersWithAuthorization(apiKey);

        return new HttpEntity<>(orderVerificationRequest, headers);
    }

    /**
     * Creates HTTP headers containing the authorization needed for an API key
     *
     * @param apiKey The API key of the merchant
     */
    protected HttpHeaders buildHeadersWithAuthorization(final String apiKey) {
        final HttpHeaders headers = new HttpHeaders();
        headers.put(HttpHeaders.AUTHORIZATION, singletonList(BEARER + apiKey));
        return headers;
    }

    protected String getNs8OrderVerificationContent(final ResponseEntity<String> responseEntity) {
        return new GsonBuilder().create().fromJson(responseEntity.getBody(), Ns8OrderVerificationResponse.class).getHtml();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void getNs8Order(final OrderModel orderModel) {

        final String orderCode = orderModel.getCode();
        final UriComponents uri = getGetOrderUriComponents(Base64.getEncoder().encodeToString(orderCode.getBytes()));
        Preconditions.checkArgument(orderModel.getSite() != null && orderModel.getSite().getNs8Merchant() != null
                && StringUtils.isNotBlank(orderModel.getSite().getNs8Merchant().getApiKey()), "The merchant api key is mandatory to trigger the update order status.");
        final HttpHeaders headers = buildHeadersWithAuthorization(orderModel.getSite().getNs8Merchant().getApiKey());
        final HttpEntity request = new HttpEntity(headers);

        try {
            LOG.debug("Getting order [{}] from NS8", orderCode);
            final ResponseEntity<String> responseEntity = restTemplate.exchange(uri.toString(), HttpMethod.GET, request, String.class);
            orderModel.setNs8OrderPayload(responseEntity.getBody());
            modelService.save(orderModel);
        } catch (final HttpStatusCodeException e) {
            LOG.error("Could not retrieve order [{}]. URL: [{}], status code [{}], error body: [{}].", () -> orderCode, uri::toString, e::getStatusCode, e::getResponseBodyAsString);
            throw new Ns8IntegrationException("Could not retrieve verification template", e.getStatusCode(), e);
        } catch (final ResourceAccessException ex) {
            LOG.error("Could not retrieve order [{}] due to connection issues. URL: [{}], status code [{}], error body: [{}].",
                    () -> orderCode, uri::toString, () -> HttpStatus.SERVICE_UNAVAILABLE, ex::getMessage);
            throw new Ns8IntegrationException("Could not retrieve order from NS8 due to connection issues.", HttpStatus.SERVICE_UNAVAILABLE, ex);
        }
    }

    /**
     * Generates the URI components with a given set of parameters
     *
     * @param orderId the order id with base64 codification
     */
      protected UriComponents getGetOrderUriComponents(final String orderId) {
        return UriComponentsBuilder.fromUriString(ns8EndpointService.getBaseClientURL())
                .path(API_ORDER_URL + orderId)
                .build();
    }
}
