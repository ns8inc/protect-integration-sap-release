package com.ns8.hybris.core.services.api.impl;

import com.ns8.hybris.core.data.*;
import com.ns8.hybris.core.integration.exceptions.Ns8IntegrationException;
import com.ns8.hybris.core.model.NS8MerchantModel;
import com.ns8.hybris.core.services.api.Ns8EndpointService;
import de.hybris.bootstrap.annotations.UnitTest;
import de.hybris.platform.core.model.order.OrderModel;
import de.hybris.platform.servicelayer.config.ConfigurationService;
import de.hybris.platform.servicelayer.dto.converter.Converter;
import de.hybris.platform.servicelayer.model.ModelService;
import org.apache.commons.lang.StringUtils;
import org.apache.http.entity.ContentType;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.*;
import org.mockito.internal.util.reflection.Whitebox;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.http.*;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Base64;

import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.mockito.Answers.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.*;

@UnitTest
@RunWith(MockitoJUnitRunner.class)
public class DefaultNs8ApiServiceTest {

    private static final String PLATFORM_NAME_VALUE = "platform";
    private static final String BASE_CLIENT_URL = "baseClientURL";
    private static final String BASE_BACKEND_URL = "baseBackendURL";
    private static final String PLATFORM_ERROR_URL = "/api/util/log-platform-error";
    private static final String API_INIT_SCRIPT = "/api/init/script/";
    private static final String PROTECT_PLATFORM_INSTALL_URL = "/protect/platform/install/";
    private static final String PROTECT_PLATFORM_REINSTALL_URL = "/protect/platform/install/reinstall/";
    private static final String NS_8_SERVICES_PLATFORM_NAME_CONFIGURATION_KEY = "ns8services.platform.name";
    private static final String ACCESS_TOKEN_FROM_RESPONSE = "accessToken";
    private static final String QUEUE_ID_FROM_RESPONSE = "queueId";
    private static final String API_KEY = "apiKey";
    private static final String BEARER = "Bearer ";
    private static final String CREATE_ORDER_ACTION = "CREATE_ORDER_ACTION";
    private static final String UPDATE_ORDER_STATUS_ACTION = "UPDATE_ORDER_STATUS_ACTION";
    private static final String UNINSTALL_MERCHANT_ACTION = "UNINSTALL_ACTION";
    private static final String API_SWITCH_EXECUTOR = "/api/switch/executor";
    private static final String API_ORDER_URL = "/api/orders/order-name/";
    private static final String ACTION_HTTP_PARAM = "action";
    private static final String TOKEN = "tokenValue";
    private static final String ORDER_ID = "orderId";
    private static final String RETURN_URI = "returnUri";
    private static final String TEMPLATE_ID = "orders-validate";
    private static final String VERIFICATION_ID = "verificationId";
    private static final String DIRTY_TEMPLATE_CONTENT = "{\"html\":\"templateContent\"}";
    private static final String API_TEMPLATE_URL = "/api/merchant/template";
    private static final String SANITIZED_TEMPLATE_CONTENT = "templateContent";
    private static final String VIEW_PARAM = "view";
    private static final String TOKEN_PARAM = "token";
    private static final String ORDER_ID_PARAM = "orderId";
    private static final String RETURN_URI_PARAM = "returnUri";
    private static final String VERIFICATION_ID_PARAM = "verificationId";
    private static final String DIRTY_NS8_ORDER = "{\"status\": \"APPROVED\",\"id\": \"orderId\"}";
    private static final String NS_8_SERVICES_ERROR_TRIGGER_PLUGIN_INSTALL_VALUE = "Plugin installation error";
    private static final String NS_8_SERVICES_ERROR_TRIGGER_PLUGIN_INSTALL_KEY = "ns8services.error.triggerPluginInstallEvent";
    private static final String NS_8_SERVICES_ERROR_TRIGGER_MERCHANT_UNINSTALL_KEY = "ns8services.error.triggerMerchantUninstallEvent";
    private static final String NS_8_SERVICES_ERROR_TRIGGER_MERCHANT_UNINSTALL_VALUE = "Uninstalling merchant error";
    private static final String NS_8_SERVICES_ERROR_TRIGGER_MERCHANT_REINSTALL_KEY = "ns8services.error.triggerMerchantReinstallEvent";
    private static final String NS_8_SERVICES_ERROR_TRIGGER_MERCHANT_REINSTALL_VALUE = "Reinstalling merchant error";
    private static final String NS_8_SERVICES_ERROR_FETCH_TRUE_STATS_SCRIPT_KEY = "ns8services.error.fetchTrueStatsScript";
    private static final String NS_8_SERVICES_ERROR_FETCH_TRUE_STATS_SCRIPT_VALUE = "Fetching TrueStats error";
    private static final String NS_8_SERVICES_ERROR_TRIGGER_CREATE_ORDER_KEY = "ns8services.error.triggerCreateOrderActionEvent";
    private static final String NS_8_SERVICES_ERROR_TRIGGER_CREATE_ORDER_VALUE = "Error sending the order";
    private static final String NS_8_SERVICES_ERROR_TRIGGER_UPDATE_ORDER_STATUS_KEY = "ns8services.error.triggerUpdateOrderStatusAction";
    private static final String NS_8_SERVICES_ERROR_TRIGGER_UPDATE_ORDER_STATUS_VALUE= "Uptdating order status error";
    private static final String NS_8_SERVICES_ERROR_VERIFICATION_TEMPLATE_KEY = "ns8services.error.getVerificationTemplate";
    private static final String NS_8_SERVICES_ERROR_VERIFICATION_TEMPLATE_VALUE = "Verification template error";
    private static final String NS_8_SERVICES_ERROR_SEND_VERIFICATION_KEY = "ns8services.error.sendVerification";
    private static final String NS_8_SERVICES_ERROR_SEND_VERIFICATION_VALUE = "Sending verification error";
    private static final String NS_8_SERVICES_ERROR_NS8_ORDER_KEY = "ns8services.error.getNs8Order";
    private static final String NS_8_SERVICES_ERROR_NS8_ORDER_VALUE = "Getting Ns8 Order error";
    private static final String ERROR_MESSAGE = "errorMessage";
    private static final String STACK_TRACE_MESSAGE = "stackTraceMessage";
    private static final String VERIFICATION_TEMPLATE_URL = "baseClientURL/api/merchant/template?orderId=orderId&token=tokenValue&verificationId=verificationId&view=orders-validate&returnUri=returnUri";

    @Spy
    @InjectMocks
    private DefaultNs8ApiService testObj;

    @Mock(answer = RETURNS_DEEP_STUBS)
    private ConfigurationService configurationServiceMock;
    @Mock
    private RestTemplate restTemplateMock;
    @Mock
    private Converter<NS8MerchantModel, Ns8PluginInstallRequest> pluginInstallRequestConverterMock;
    @Mock
    private Converter<OrderModel, Ns8OrderData> ns8OrderDataConverterMock;
    @Mock
    private Converter<OrderModel, Ns8UpdateOrderStatus> ns8UpdateOrderStatusConverterMock;
    @Mock
    private Ns8EndpointService ns8EndpointServiceMock;
    @Mock
    private ModelService modelServiceMock;

    @Mock
    private NS8MerchantModel ns8MerchantMock;
    @Mock
    private Ns8PluginInstallRequest ns8PluginInstallRequestMock;
    @Mock
    private PluginInstallResponseData pluginInstallResponseDataMock;
    @Mock(answer = RETURNS_DEEP_STUBS)
    private OrderModel orderMock;
    @Mock
    private Ns8OrderData ns8OrderDataMock;
    @Mock
    private Ns8UpdateOrderStatus ns8UpdateOrderStatusMock;
    @Mock
    private Ns8OrderVerificationRequest orderVerificationRequestMock;
    @Mock
    private MerchantReactivateResponseData merchantReactivateResponseDataMock;
    @Mock
    private ResponseEntity<Ns8PlatformErrorResponse> responseEntityMock;
    @Mock
    private Ns8PlatformErrorResponse ns8PlatformErrorResponseMock;

    @Captor
    private ArgumentCaptor<HttpEntity<?>> httpEntityCaptor;
    @Captor
    private ArgumentCaptor<String> stringCaptor;
    @Captor
    private ArgumentCaptor<HttpHeaders> headersCaptor;


    @Before
    public void setUp() {
        Whitebox.setInternalState(testObj, "ns8OrderDataConverter", ns8OrderDataConverterMock);
        Whitebox.setInternalState(testObj, "ns8PluginInstallRequestConverter", pluginInstallRequestConverterMock);
        Whitebox.setInternalState(testObj, "ns8UpdateOrderStatusConverter", ns8UpdateOrderStatusConverterMock);
        doReturn("prettyObject").when(testObj).prettyPrint(any());
        when(ns8EndpointServiceMock.getBaseBackendURL()).thenReturn(BASE_BACKEND_URL);
        when(ns8EndpointServiceMock.getBaseClientURL()).thenReturn(BASE_CLIENT_URL);
        when(configurationServiceMock.getConfiguration().getString(NS_8_SERVICES_PLATFORM_NAME_CONFIGURATION_KEY)).thenReturn(PLATFORM_NAME_VALUE);
        when(configurationServiceMock.getConfiguration().getString(NS_8_SERVICES_ERROR_TRIGGER_PLUGIN_INSTALL_KEY)).thenReturn(NS_8_SERVICES_ERROR_TRIGGER_PLUGIN_INSTALL_VALUE);
        when(configurationServiceMock.getConfiguration().getString(NS_8_SERVICES_ERROR_FETCH_TRUE_STATS_SCRIPT_KEY)).thenReturn(NS_8_SERVICES_ERROR_FETCH_TRUE_STATS_SCRIPT_VALUE);
        when(configurationServiceMock.getConfiguration().getString(NS_8_SERVICES_ERROR_TRIGGER_CREATE_ORDER_KEY)).thenReturn(NS_8_SERVICES_ERROR_TRIGGER_CREATE_ORDER_VALUE);
        when(configurationServiceMock.getConfiguration().getString(NS_8_SERVICES_ERROR_TRIGGER_UPDATE_ORDER_STATUS_KEY)).thenReturn(NS_8_SERVICES_ERROR_TRIGGER_UPDATE_ORDER_STATUS_VALUE);
        when(configurationServiceMock.getConfiguration().getString(NS_8_SERVICES_ERROR_VERIFICATION_TEMPLATE_KEY)).thenReturn(NS_8_SERVICES_ERROR_VERIFICATION_TEMPLATE_VALUE);
        when(configurationServiceMock.getConfiguration().getString(NS_8_SERVICES_ERROR_SEND_VERIFICATION_KEY)).thenReturn(NS_8_SERVICES_ERROR_SEND_VERIFICATION_VALUE);
        when(configurationServiceMock.getConfiguration().getString(NS_8_SERVICES_ERROR_TRIGGER_MERCHANT_UNINSTALL_KEY)).thenReturn(NS_8_SERVICES_ERROR_TRIGGER_MERCHANT_UNINSTALL_VALUE);
        when(configurationServiceMock.getConfiguration().getString(NS_8_SERVICES_ERROR_NS8_ORDER_KEY)).thenReturn(NS_8_SERVICES_ERROR_NS8_ORDER_VALUE);
        when(configurationServiceMock.getConfiguration().getString(NS_8_SERVICES_ERROR_TRIGGER_MERCHANT_REINSTALL_KEY)).thenReturn(NS_8_SERVICES_ERROR_TRIGGER_MERCHANT_REINSTALL_VALUE);

        when(pluginInstallRequestConverterMock.convert(ns8MerchantMock)).thenReturn(ns8PluginInstallRequestMock);
        when(ns8OrderDataConverterMock.convert(orderMock)).thenReturn(ns8OrderDataMock);
        when(ns8UpdateOrderStatusConverterMock.convert(orderMock)).thenReturn(ns8UpdateOrderStatusMock);
        when(orderMock.getSite().getNs8Merchant()).thenReturn(ns8MerchantMock);
        when(pluginInstallResponseDataMock.getAccessToken()).thenReturn(ACCESS_TOKEN_FROM_RESPONSE);
        when(pluginInstallResponseDataMock.getQueueId()).thenReturn(QUEUE_ID_FROM_RESPONSE);
        when(ns8MerchantMock.getApiKey()).thenReturn(API_KEY);
        when(ns8MerchantMock.getEnabled()).thenReturn(Boolean.TRUE);
        when(ns8MerchantMock.getEmail()).thenReturn("some@email.com");

        when(orderVerificationRequestMock.getOrderId()).thenReturn(ORDER_ID);
        when(orderVerificationRequestMock.getToken()).thenReturn(TOKEN);
        when(orderVerificationRequestMock.getVerificationId()).thenReturn(VERIFICATION_ID);
        when(orderVerificationRequestMock.getView()).thenReturn(TEMPLATE_ID);
        when(orderVerificationRequestMock.getReturnURI()).thenReturn(RETURN_URI);
        when(responseEntityMock.getBody()).thenReturn(ns8PlatformErrorResponseMock);
        when(responseEntityMock.hasBody()).thenReturn(Boolean.TRUE);
        when(ns8PlatformErrorResponseMock.getLogged()).thenReturn(Boolean.TRUE);
    }

    @Test
    public void triggerPluginInstallEvent_ShouldSendMerchantInformationToNS8UsingRestTemplate() throws Ns8IntegrationException {
        when(restTemplateMock.postForEntity(eq(BASE_BACKEND_URL + PROTECT_PLATFORM_INSTALL_URL + PLATFORM_NAME_VALUE), httpEntityCaptor.capture(), eq(PluginInstallResponseData.class)))
                .thenReturn(new ResponseEntity(pluginInstallResponseDataMock, HttpStatus.OK));

        testObj.triggerPluginInstallEvent(ns8MerchantMock);

        assertThat(httpEntityCaptor.getValue().getBody()).isEqualTo(ns8PluginInstallRequestMock);
        verify(ns8MerchantMock).setApiKey(ACCESS_TOKEN_FROM_RESPONSE);
        verify(ns8MerchantMock).setQueueId(QUEUE_ID_FROM_RESPONSE);
        verify(ns8MerchantMock).setEnabled(true);
        verify(modelServiceMock).save(ns8MerchantMock);
    }

    @Test
    public void triggerPluginInstallEvent_WhenHttpStatusCodeException_ShouldThrowNS8IntegrationException() {
        final HttpClientErrorException clientErrorException = new HttpClientErrorException(HttpStatus.BAD_REQUEST, "exception");
        final String errorStackMessage = String.format("Installation of NS8 Merchant failed. URL: [%s], payload: [%s], status code [%s], error body: [%s].",
                BASE_BACKEND_URL + PROTECT_PLATFORM_INSTALL_URL + PLATFORM_NAME_VALUE,
                "prettyObject",
                HttpStatus.BAD_REQUEST,
                StringUtils.EMPTY);

        doThrow(clientErrorException).when(restTemplateMock).postForEntity(eq(BASE_BACKEND_URL + PROTECT_PLATFORM_INSTALL_URL + PLATFORM_NAME_VALUE), any(HttpEntity.class), eq(PluginInstallResponseData.class));
        when(restTemplateMock.postForEntity(eq(BASE_CLIENT_URL + PLATFORM_ERROR_URL), any(HttpEntity.class), eq(Ns8PlatformErrorResponse.class)))
                .thenReturn(responseEntityMock);

        final Throwable thrown = catchThrowable(() -> testObj.triggerPluginInstallEvent(ns8MerchantMock));

        assertThat(thrown)
                .isInstanceOf(Ns8IntegrationException.class)
                .hasCause(clientErrorException);
        verify(testObj).sendErrorLogging(NS_8_SERVICES_ERROR_TRIGGER_PLUGIN_INSTALL_VALUE, errorStackMessage);
    }

    @Test
    public void triggerPluginInstallEvent_WhenOtherException_ShouldThrowNS8IntegrationException() {
        doThrow(ResourceAccessException.class).when(restTemplateMock).postForEntity(eq(BASE_BACKEND_URL + PROTECT_PLATFORM_INSTALL_URL + PLATFORM_NAME_VALUE), any(HttpEntity.class), eq(PluginInstallResponseData.class));

        final Throwable thrown = catchThrowable(() -> testObj.triggerPluginInstallEvent(ns8MerchantMock));

        assertThat(thrown)
                .isInstanceOf(Ns8IntegrationException.class)
                .hasCauseInstanceOf(ResourceAccessException.class);
    }

    @Test
    public void fetchTrueStatsScript_shouldGetJavascriptContent_RemovingQuotesAroundScript() {
        when(restTemplateMock.postForObject(eq(BASE_CLIENT_URL + API_INIT_SCRIPT), httpEntityCaptor.capture(), eq(String.class)))
                .thenReturn("\"var testJavaScript = function(){\\r\\nalert(1);\\r\\n}\"");

        final String result = testObj.fetchTrueStatsScript(ns8MerchantMock);

        final HttpEntity<?> httpEntity = httpEntityCaptor.getValue();

        assertThat(httpEntity.getBody()).isNull();
        assertThat(httpEntity.getHeaders()).containsExactly(entry(HttpHeaders.AUTHORIZATION, singletonList("Bearer " + API_KEY)));

        assertThat(result).isEqualTo("var testJavaScript = function(){\r\nalert(1);\r\n}");
    }

    @Test
    public void fetchTrueStatsScript_shouldGetJavascriptContent_NoNeedToRemoveQuotesAroundScript() {
        when(restTemplateMock.postForObject(eq(BASE_CLIENT_URL + API_INIT_SCRIPT), httpEntityCaptor.capture(), eq(String.class)))
                .thenReturn("var testJavaScript = function(){\\r\\nalert(1);\\r\\n}");

        final String result = testObj.fetchTrueStatsScript(ns8MerchantMock);

        final HttpEntity<?> httpEntity = httpEntityCaptor.getValue();

        assertThat(httpEntity.getBody()).isNull();
        assertThat(httpEntity.getHeaders()).containsExactly(entry(HttpHeaders.AUTHORIZATION, singletonList("Bearer " + API_KEY)));

        assertThat(result).isEqualTo("var testJavaScript = function(){\r\nalert(1);\r\n}");
    }

    @Test
    public void fetchTrueStatsScript_WhenHttpStatusCodeException_ShouldThrowNS8IntegrationException() {
        final HttpClientErrorException clientException = new HttpClientErrorException(HttpStatus.I_AM_A_TEAPOT);
        final String errorStackMessage = String.format("Reactivation of NS8 Merchant failed. URL: [%s], status code [%s], error body: [%s].",
                BASE_CLIENT_URL + API_INIT_SCRIPT,
                HttpStatus.I_AM_A_TEAPOT,
                StringUtils.EMPTY);

        when(restTemplateMock.postForObject(eq(BASE_CLIENT_URL + API_INIT_SCRIPT), httpEntityCaptor.capture(), eq(String.class)))
                .thenThrow(clientException);
        when(restTemplateMock.postForEntity(eq(BASE_CLIENT_URL + PLATFORM_ERROR_URL), any(HttpEntity.class), eq(Ns8PlatformErrorResponse.class)))
                .thenReturn(responseEntityMock);

        final Throwable thrown = catchThrowable(() -> testObj.fetchTrueStatsScript(ns8MerchantMock));

        assertThat(thrown)
                .isInstanceOf(Ns8IntegrationException.class)
                .hasCause(clientException)
                .hasFieldOrPropertyWithValue("httpStatus", HttpStatus.I_AM_A_TEAPOT);

        verify(testObj).sendErrorLogging(NS_8_SERVICES_ERROR_FETCH_TRUE_STATS_SCRIPT_VALUE, errorStackMessage);
    }

    @Test
    public void fetchTrueStatsScript_WhenOtherException_ShouldThrowNS8IntegrationException() {
        when(restTemplateMock.postForObject(eq(BASE_CLIENT_URL + API_INIT_SCRIPT), httpEntityCaptor.capture(), eq(String.class)))
                .thenThrow(ResourceAccessException.class);

        final Throwable thrown = catchThrowable(() -> testObj.fetchTrueStatsScript(ns8MerchantMock));

        assertThat(thrown)
                .isInstanceOf(Ns8IntegrationException.class)
                .hasCauseInstanceOf(ResourceAccessException.class)
                .hasFieldOrPropertyWithValue("httpStatus", HttpStatus.SERVICE_UNAVAILABLE);
    }

    @Test(expected = IllegalArgumentException.class)
    public void triggerCreateOrderActionEvent_WhenApiKeyNotProvided_ShouldThrowException() {
        when(ns8MerchantMock.getApiKey()).thenReturn(null);

        testObj.triggerCreateOrderActionEvent(orderMock);
    }

    @Test(expected = IllegalArgumentException.class)
    public void triggerCreateOrderActionEvent_WhenMerchantNull_ShouldThrowException() {
        when(orderMock.getSite().getNs8Merchant()).thenReturn(null);

        testObj.triggerCreateOrderActionEvent(orderMock);
    }

    @Test(expected = IllegalArgumentException.class)
    public void triggerCreateOrderActionEvent_WhenSiteNull_ShouldThrowException() {
        when(orderMock.getSite()).thenReturn(null);

        testObj.triggerCreateOrderActionEvent(orderMock);
    }

    @Test
    public void triggerCreateOrderActionEvent_ShouldSendOrderToNs8() {
        when(restTemplateMock.postForEntity(stringCaptor.capture(), httpEntityCaptor.capture(), eq(Void.class)))
                .thenReturn(new ResponseEntity<>(HttpStatus.OK));

        testObj.triggerCreateOrderActionEvent(orderMock);

        final String url = stringCaptor.getValue();
        assertThat(url).isEqualTo(BASE_CLIENT_URL + API_SWITCH_EXECUTOR + "?" + ACTION_HTTP_PARAM + "=" + CREATE_ORDER_ACTION);
        final HttpEntity<?> httpEntity = httpEntityCaptor.getValue();
        assertThat(httpEntity.getHeaders()).containsExactly(
                entry(HttpHeaders.AUTHORIZATION, singletonList("Bearer " + API_KEY)),
                entry(HttpHeaders.CONTENT_TYPE, singletonList(ContentType.APPLICATION_JSON.toString()))
        );
        assertThat(httpEntity.getBody()).isEqualTo(ns8OrderDataMock);
    }

    @Test
    public void triggerCreateOrderActionEvent_WhenHttpStatusCodeException_ShouldThrowNs8IntegrationException() {
        final HttpClientErrorException clientException = new HttpClientErrorException(HttpStatus.I_AM_A_TEAPOT);
        final String errorStackMessage = String.format("Sending order to NS8 failed. URL: [%s], payload: [%s], status code [%s], error body: [%s].",
                BASE_CLIENT_URL + API_SWITCH_EXECUTOR + "?" + ACTION_HTTP_PARAM + "=" + CREATE_ORDER_ACTION,
                "prettyObject",
                HttpStatus.I_AM_A_TEAPOT,
                StringUtils.EMPTY);

        when(restTemplateMock.postForEntity(eq(BASE_CLIENT_URL + PLATFORM_ERROR_URL), any(HttpEntity.class), eq(Ns8PlatformErrorResponse.class)))
                .thenReturn(responseEntityMock);
        when(restTemplateMock.postForEntity(stringCaptor.capture(), httpEntityCaptor.capture(), eq(Void.class)))
                .thenThrow(clientException);

        final Throwable thrown = catchThrowable(() -> testObj.triggerCreateOrderActionEvent(orderMock));

        final String url = stringCaptor.getValue();
        assertThat(url).isEqualTo(BASE_CLIENT_URL + API_SWITCH_EXECUTOR + "?" + ACTION_HTTP_PARAM + "=" + CREATE_ORDER_ACTION);
        assertThat(thrown)
                .isInstanceOf(Ns8IntegrationException.class)
                .hasCause(clientException)
                .hasFieldOrPropertyWithValue("httpStatus", HttpStatus.I_AM_A_TEAPOT);
        verify(testObj).sendErrorLogging(NS_8_SERVICES_ERROR_TRIGGER_CREATE_ORDER_VALUE, errorStackMessage);
    }

    @Test
    public void triggerCreateOrderActionEvent_WhenOtherException_ShouldThrowNs8IntegrationException() {
        when(restTemplateMock.postForEntity(stringCaptor.capture(), httpEntityCaptor.capture(), eq(Void.class)))
                .thenThrow(ResourceAccessException.class);

        final Throwable thrown = catchThrowable(() -> testObj.triggerCreateOrderActionEvent(orderMock));

        final String url = stringCaptor.getValue();
        assertThat(url).isEqualTo(BASE_CLIENT_URL + API_SWITCH_EXECUTOR + "?" + ACTION_HTTP_PARAM + "=" + CREATE_ORDER_ACTION);
        assertThat(thrown)
                .isInstanceOf(Ns8IntegrationException.class)
                .hasCauseInstanceOf(ResourceAccessException.class)
                .hasFieldOrPropertyWithValue("httpStatus", HttpStatus.SERVICE_UNAVAILABLE);
    }

    @Test(expected = IllegalArgumentException.class)
    public void triggerUpdateOrderStatusAction_WhenApiKeyNotProvided_ShouldThrowException() {
        when(ns8MerchantMock.getApiKey()).thenReturn(null);

        testObj.triggerUpdateOrderStatusAction(orderMock);
    }

    @Test(expected = IllegalArgumentException.class)
    public void triggerUpdateOrderStatusAction_WhenMerchantNull_ShouldThrowException() {
        when(orderMock.getSite().getNs8Merchant()).thenReturn(null);

        testObj.triggerUpdateOrderStatusAction(orderMock);
    }

    @Test(expected = IllegalArgumentException.class)
    public void triggerUpdateOrderStatusAction_WhenSiteNull_ShouldThrowException() {
        when(orderMock.getSite()).thenReturn(null);

        testObj.triggerUpdateOrderStatusAction(orderMock);
    }

    @Test
    public void triggerUpdateOrderStatusAction_ShouldSendOrderToNs8() {
        when(restTemplateMock.postForEntity(stringCaptor.capture(), httpEntityCaptor.capture(), eq(Void.class)))
                .thenReturn(new ResponseEntity<>(HttpStatus.OK));

        testObj.triggerUpdateOrderStatusAction(orderMock);

        final String url = stringCaptor.getValue();
        assertThat(url).isEqualTo(BASE_CLIENT_URL + API_SWITCH_EXECUTOR + "?" + ACTION_HTTP_PARAM + "=" + UPDATE_ORDER_STATUS_ACTION);
        final HttpEntity<?> httpEntity = httpEntityCaptor.getValue();
        assertThat(httpEntity.getHeaders()).containsExactly(
                entry(HttpHeaders.AUTHORIZATION, singletonList("Bearer " + API_KEY)),
                entry(HttpHeaders.CONTENT_TYPE, singletonList(ContentType.APPLICATION_JSON.toString()))
        );
        assertThat(httpEntity.getBody()).isEqualTo(ns8UpdateOrderStatusMock);
    }

    @Test
    public void triggerUpdateOrderStatusAction_WhenHttpStatusCodeException_ShouldThrowNs8IntegrationException() {
        final HttpClientErrorException clientException = new HttpClientErrorException(HttpStatus.I_AM_A_TEAPOT);
        final String errorStackMessage = String.format("Sending order status update to NS8 failed. URL: [{}], payload: [{}], status code [{}], error body: [{}].",
                BASE_CLIENT_URL + API_SWITCH_EXECUTOR + "?" + ACTION_HTTP_PARAM + "=" + UPDATE_ORDER_STATUS_ACTION,
                "prettyObject",
                HttpStatus.I_AM_A_TEAPOT,
                StringUtils.EMPTY);

        when(restTemplateMock.postForEntity(eq(BASE_CLIENT_URL + PLATFORM_ERROR_URL), any(HttpEntity.class), eq(Ns8PlatformErrorResponse.class)))
                .thenReturn(responseEntityMock);

        when(restTemplateMock.postForEntity(stringCaptor.capture(), httpEntityCaptor.capture(), eq(Void.class)))
                .thenThrow(clientException);

        final Throwable thrown = catchThrowable(() -> testObj.triggerUpdateOrderStatusAction(orderMock));

        final String url = stringCaptor.getValue();
        assertThat(url).isEqualTo(BASE_CLIENT_URL + API_SWITCH_EXECUTOR + "?" + ACTION_HTTP_PARAM + "=" + UPDATE_ORDER_STATUS_ACTION);
        assertThat(thrown)
                .isInstanceOf(Ns8IntegrationException.class)
                .hasCause(clientException)
                .hasFieldOrPropertyWithValue("httpStatus", HttpStatus.I_AM_A_TEAPOT);
        verify(testObj).sendErrorLogging(NS_8_SERVICES_ERROR_TRIGGER_UPDATE_ORDER_STATUS_VALUE, errorStackMessage);
    }

    @Test
    public void triggerUpdateOrderStatusAction_WhenOtherException_ShouldThrowNs8IntegrationException() {
        when(restTemplateMock.postForEntity(stringCaptor.capture(), httpEntityCaptor.capture(), eq(Void.class)))
                .thenThrow(ResourceAccessException.class);

        final Throwable thrown = catchThrowable(() -> testObj.triggerUpdateOrderStatusAction(orderMock));

        final String url = stringCaptor.getValue();
        assertThat(url).isEqualTo(BASE_CLIENT_URL + API_SWITCH_EXECUTOR + "?" + ACTION_HTTP_PARAM + "=" + UPDATE_ORDER_STATUS_ACTION);
        assertThat(thrown)
                .isInstanceOf(Ns8IntegrationException.class)
                .hasCauseInstanceOf(ResourceAccessException.class)
                .hasFieldOrPropertyWithValue("httpStatus", HttpStatus.SERVICE_UNAVAILABLE);
    }

    @Test
    public void getVerificationTemplate_ShouldRetrieveSanitizedTemplate() {
        when(restTemplateMock.getForEntity(stringCaptor.capture(), eq(String.class), headersCaptor.capture()))
                .thenReturn(new ResponseEntity<>(DIRTY_TEMPLATE_CONTENT, HttpStatus.OK));

        final String result = testObj.getVerificationTemplate(orderVerificationRequestMock, API_KEY);
        assertThat(result).isEqualTo(SANITIZED_TEMPLATE_CONTENT);

        final String url = stringCaptor.getValue();
        assertUriValuesForGet(url);

        final HttpHeaders headers = headersCaptor.getValue();
        assertThat(headers).containsExactly(entry(HttpHeaders.AUTHORIZATION, singletonList("Bearer " + API_KEY)));
    }

    @Test
    public void getVerificationTemplate_WhenHttpStatusCodeException_ShouldThrowNs8IntegrationException() {
        final HttpClientErrorException clientException = new HttpClientErrorException(HttpStatus.I_AM_A_TEAPOT);
        final String errorStackMessage = String.format("Could not retrieve template [%s]. URL: [%s], status code [%s], error body: [%s].",
                TEMPLATE_ID,
                VERIFICATION_TEMPLATE_URL,
                HttpStatus.I_AM_A_TEAPOT,
                StringUtils.EMPTY);

        when(restTemplateMock.postForEntity(eq(BASE_CLIENT_URL + PLATFORM_ERROR_URL), any(HttpEntity.class), eq(Ns8PlatformErrorResponse.class)))
                .thenReturn(responseEntityMock);
        when(restTemplateMock.getForEntity(stringCaptor.capture(), eq(String.class), headersCaptor.capture()))
                .thenThrow(clientException);

        final Throwable thrown = catchThrowable(() -> testObj.getVerificationTemplate(orderVerificationRequestMock, API_KEY));

        final String url = stringCaptor.getValue();
        assertUriValuesForGet(url);

        final HttpHeaders headers = headersCaptor.getValue();
        assertThat(headers).containsExactly(entry(HttpHeaders.AUTHORIZATION, singletonList("Bearer " + API_KEY)));

        assertThat(thrown)
                .isInstanceOf(Ns8IntegrationException.class)
                .hasCause(clientException)
                .hasFieldOrPropertyWithValue("httpStatus", HttpStatus.I_AM_A_TEAPOT);
        verify(testObj).sendErrorLogging(NS_8_SERVICES_ERROR_VERIFICATION_TEMPLATE_VALUE, errorStackMessage);
    }

    @Test
    public void getVerificationTemplate_WhenOtherException_ShouldThrowNs8IntegrationException() {
        when(restTemplateMock.getForEntity(stringCaptor.capture(), eq(String.class), headersCaptor.capture()))
                .thenThrow(ResourceAccessException.class);

        final Throwable thrown = catchThrowable(() -> testObj.getVerificationTemplate(orderVerificationRequestMock, API_KEY));

        final String url = stringCaptor.getValue();
        assertUriValuesForGet(url);

        assertThat(thrown)
                .isInstanceOf(Ns8IntegrationException.class)
                .hasCauseInstanceOf(ResourceAccessException.class)
                .hasFieldOrPropertyWithValue("httpStatus", HttpStatus.SERVICE_UNAVAILABLE);

        final HttpHeaders headers = headersCaptor.getValue();
        assertThat(headers).containsExactly(entry(HttpHeaders.AUTHORIZATION, singletonList("Bearer " + API_KEY)));
    }

    @Test
    public void sendVerification_WhenSendingOrderVerificationRequest_ThenTemplateContentIsReturned() {
        when(restTemplateMock.postForEntity(stringCaptor.capture(), httpEntityCaptor.capture(), eq(String.class)))
                .thenReturn(new ResponseEntity(DIRTY_TEMPLATE_CONTENT, HttpStatus.OK));

        final Ns8OrderVerificationResponse result = testObj.sendVerification(orderVerificationRequestMock, API_KEY);
        assertThat(result.getHtml()).isEqualTo(SANITIZED_TEMPLATE_CONTENT);

        final String url = stringCaptor.getValue();
        assertUrlForUriValues(url);

        final HttpEntity<?> httpEntity = httpEntityCaptor.getValue();
        assertEquals(orderVerificationRequestMock, httpEntity.getBody());
        assertThat(httpEntity.getHeaders()).containsExactly(entry(HttpHeaders.AUTHORIZATION, singletonList("Bearer " + API_KEY)));
    }

    @Test
    public void sendVerification_WhenHttpStatusCodeException_ShouldThrowNs8IntegrationException() {
        final HttpClientErrorException clientException = new HttpClientErrorException(HttpStatus.I_AM_A_TEAPOT);
        final String errorStackMessage = String.format("Could not send verification template [%s]. URL: [%s], status code [%s], error body: [%s].",
                TEMPLATE_ID,
                BASE_CLIENT_URL + API_TEMPLATE_URL,
                HttpStatus.I_AM_A_TEAPOT,
                StringUtils.EMPTY);

        when(restTemplateMock.postForEntity(eq(BASE_CLIENT_URL + PLATFORM_ERROR_URL), any(HttpEntity.class), eq(Ns8PlatformErrorResponse.class)))
                .thenReturn(responseEntityMock);
        when(restTemplateMock.postForEntity(stringCaptor.capture(), httpEntityCaptor.capture(), eq(String.class)))
                .thenThrow(clientException);

        final Throwable thrown = catchThrowable(() -> testObj.sendVerification(orderVerificationRequestMock, API_KEY));

        final String url = stringCaptor.getValue();
        assertUrlForUriValues(url);

        final HttpEntity<?> httpEntity = httpEntityCaptor.getValue();
        assertThat(httpEntity.getHeaders()).containsExactly(entry(HttpHeaders.AUTHORIZATION, singletonList("Bearer " + API_KEY)));

        assertThat(thrown)
                .isInstanceOf(Ns8IntegrationException.class)
                .hasCause(clientException)
                .hasFieldOrPropertyWithValue("httpStatus", HttpStatus.I_AM_A_TEAPOT);
        verify(testObj).sendErrorLogging(NS_8_SERVICES_ERROR_SEND_VERIFICATION_VALUE, errorStackMessage);
    }

    @Test
    public void sendVerification_WhenResourceAccessException_ShouldThrowNs8IntegrationException() {
        when(restTemplateMock.postForEntity(stringCaptor.capture(), httpEntityCaptor.capture(), eq(String.class)))
                .thenThrow(ResourceAccessException.class);

        final Throwable thrown = catchThrowable(() -> testObj.sendVerification(orderVerificationRequestMock, API_KEY));

        final String url = stringCaptor.getValue();
        assertUrlForUriValues(url);

        assertThat(thrown)
                .isInstanceOf(Ns8IntegrationException.class)
                .hasCauseInstanceOf(ResourceAccessException.class)
                .hasFieldOrPropertyWithValue("httpStatus", HttpStatus.SERVICE_UNAVAILABLE);

        final HttpEntity<?> httpEntity = httpEntityCaptor.getValue();
        assertThat(httpEntity.getHeaders()).containsExactly(entry(HttpHeaders.AUTHORIZATION, singletonList("Bearer " + API_KEY)));
    }

    @Test
    public void triggerMerchantUninstallEvent_ShouldCallApiToDisableMerchant() {
        when(restTemplateMock.postForEntity(stringCaptor.capture(), httpEntityCaptor.capture(), eq(Void.class)))
                .thenReturn(new ResponseEntity<>(HttpStatus.OK));

        testObj.triggerMerchantUninstallEvent(ns8MerchantMock);

        final String url = stringCaptor.getValue();
        assertThat(url).isEqualTo(BASE_CLIENT_URL + API_SWITCH_EXECUTOR + "?" + ACTION_HTTP_PARAM + "=" + UNINSTALL_MERCHANT_ACTION);

        final HttpEntity<?> httpEntity = httpEntityCaptor.getValue();
        assertThat(httpEntity.getHeaders()).containsOnly(entry(HttpHeaders.AUTHORIZATION, singletonList("Bearer " + API_KEY)));
    }

    @Test
    public void triggerMerchantUninstallEvent_WhenHttpStatusCodeException_ShouldThrowNs8IntegrationException() {
        final HttpClientErrorException clientException = new HttpClientErrorException(HttpStatus.I_AM_A_TEAPOT);
        final String errorStackMessage = String.format("Deactivation of NS8 Merchant failed. URL: [%s], status code [%s], error body: [%s].",
                BASE_CLIENT_URL + API_SWITCH_EXECUTOR + "?" + ACTION_HTTP_PARAM + "=" + UNINSTALL_MERCHANT_ACTION,
                HttpStatus.I_AM_A_TEAPOT,
                StringUtils.EMPTY);

        when(restTemplateMock.postForEntity(eq(BASE_CLIENT_URL + PLATFORM_ERROR_URL), any(HttpEntity.class), eq(Ns8PlatformErrorResponse.class)))
                .thenReturn(responseEntityMock);
        when(restTemplateMock.postForEntity(stringCaptor.capture(), httpEntityCaptor.capture(), eq(Void.class)))
                .thenThrow(clientException);

        final Throwable thrown = catchThrowable(() -> testObj.triggerMerchantUninstallEvent(ns8MerchantMock));

        final String url = stringCaptor.getValue();
        assertThat(url).isEqualTo(BASE_CLIENT_URL + API_SWITCH_EXECUTOR + "?" + ACTION_HTTP_PARAM + "=" + UNINSTALL_MERCHANT_ACTION);
        assertThat(thrown)
                .isInstanceOf(Ns8IntegrationException.class)
                .hasCause(clientException)
                .hasFieldOrPropertyWithValue("httpStatus", HttpStatus.I_AM_A_TEAPOT);
        verify(testObj).sendErrorLogging(NS_8_SERVICES_ERROR_TRIGGER_MERCHANT_UNINSTALL_VALUE, errorStackMessage);
    }

    @Test
    public void triggerMerchantUninstallEvent_WhenResourceAccessException_ShouldThrowNs8IntegrationException() {
        when(restTemplateMock.postForEntity(stringCaptor.capture(), httpEntityCaptor.capture(), eq(Void.class)))
                .thenThrow(ResourceAccessException.class);

        final Throwable thrown = catchThrowable(() -> testObj.triggerMerchantUninstallEvent(ns8MerchantMock));

        final String url = stringCaptor.getValue();
        assertThat(url).isEqualTo(BASE_CLIENT_URL + API_SWITCH_EXECUTOR + "?" + ACTION_HTTP_PARAM + "=" + UNINSTALL_MERCHANT_ACTION);
        assertThat(thrown)
                .isInstanceOf(Ns8IntegrationException.class)
                .hasCauseInstanceOf(ResourceAccessException.class)
                .hasFieldOrPropertyWithValue("httpStatus", HttpStatus.SERVICE_UNAVAILABLE);
    }

    @Test
    public void getNs8Order_WhenOrderIsCorrectlyDefined_ShouldTReturnNS8OrderDetails() {
        final HttpHeaders headers = new HttpHeaders();
        headers.put(HttpHeaders.AUTHORIZATION, singletonList(BEARER + API_KEY));
        final HttpEntity request = new HttpEntity(headers);
        when(orderMock.getCode()).thenReturn(ORDER_ID_PARAM);
        when(restTemplateMock.exchange(BASE_CLIENT_URL + API_ORDER_URL + Base64.getEncoder().encodeToString(ORDER_ID_PARAM.getBytes()), HttpMethod.GET, request, String.class))
                .thenReturn(new ResponseEntity(DIRTY_NS8_ORDER, HttpStatus.OK));

        testObj.fetchAndSaveNs8OrderPayload(orderMock);

        verify(orderMock).setNs8OrderPayload(DIRTY_NS8_ORDER);
        verify(modelServiceMock).save(orderMock);
    }

    @Test
    public void getNs8Order_WhenHttpStatusCodeException_ShouldThrowNs8IntegrationException() {
        final HttpHeaders headers = new HttpHeaders();
        headers.put(HttpHeaders.AUTHORIZATION, singletonList(BEARER + API_KEY));
        final HttpEntity request = new HttpEntity(headers);
        final HttpClientErrorException clientException = new HttpClientErrorException(HttpStatus.I_AM_A_TEAPOT);
        final String errorStackMessage = String.format("Could not retrieve order information from Ns8. URL: [%s], orderId[%s], status code [%s], error body: [%s].",
                BASE_CLIENT_URL + API_ORDER_URL + Base64.getEncoder().encodeToString(ORDER_ID_PARAM.getBytes()),
                ORDER_ID_PARAM,
                HttpStatus.I_AM_A_TEAPOT,
                StringUtils.EMPTY);

        when(restTemplateMock.postForEntity(eq(BASE_CLIENT_URL + PLATFORM_ERROR_URL), any(HttpEntity.class), eq(Ns8PlatformErrorResponse.class)))
                .thenReturn(responseEntityMock);
        when(orderMock.getCode()).thenReturn(ORDER_ID_PARAM);
        when(restTemplateMock.exchange(BASE_CLIENT_URL + API_ORDER_URL + Base64.getEncoder().encodeToString(ORDER_ID_PARAM.getBytes()), HttpMethod.GET, request, String.class))
                .thenThrow(clientException);

        final Throwable thrown = catchThrowable(() -> testObj.fetchAndSaveNs8OrderPayload(orderMock));

        assertThat(thrown)
                .isInstanceOf(Ns8IntegrationException.class)
                .hasCause(clientException)
                .hasFieldOrPropertyWithValue("httpStatus", HttpStatus.I_AM_A_TEAPOT);
        verify(testObj).sendErrorLogging(NS_8_SERVICES_ERROR_NS8_ORDER_VALUE, errorStackMessage);
    }

    @Test
    public void getNs8Order_WhenResourceAccessException_ShouldThrowNs8IntegrationException() {
        final HttpHeaders headers = new HttpHeaders();
        headers.put(HttpHeaders.AUTHORIZATION, singletonList(BEARER + API_KEY));
        final HttpEntity request = new HttpEntity(headers);
        when(orderMock.getCode()).thenReturn(ORDER_ID_PARAM);
        when(restTemplateMock.exchange(BASE_CLIENT_URL + API_ORDER_URL + Base64.getEncoder().encodeToString(ORDER_ID_PARAM.getBytes()), HttpMethod.GET, request, String.class))
                .thenThrow(ResourceAccessException.class);

        final Throwable thrown = catchThrowable(() -> testObj.fetchAndSaveNs8OrderPayload(orderMock));

        assertThat(thrown)
                .isInstanceOf(Ns8IntegrationException.class)
                .hasCauseInstanceOf(ResourceAccessException.class)
                .hasFieldOrPropertyWithValue("httpStatus", HttpStatus.SERVICE_UNAVAILABLE);
    }

    @Test
    public void triggerMerchantReinstallEvent_ShouldSendMerchantInformationToNS8AndEnableMerchant() throws Ns8IntegrationException {
        when(restTemplateMock.postForEntity(eq(BASE_BACKEND_URL + PROTECT_PLATFORM_REINSTALL_URL + PLATFORM_NAME_VALUE), httpEntityCaptor.capture(), eq(MerchantReactivateResponseData.class)))
                .thenReturn(new ResponseEntity(merchantReactivateResponseDataMock, HttpStatus.OK));
        when(merchantReactivateResponseDataMock.getSuccess()).thenReturn(true);

        boolean result = testObj.triggerMerchantReinstallEvent(ns8MerchantMock);

        assertThat(httpEntityCaptor.getValue().getBody()).isEqualTo(ns8PluginInstallRequestMock);
        assertThat(result).isEqualTo(true);
    }

    @Test
    public void triggerMerchantReinstallEvent_WhenResourceAccessException_ShouldThrowNs8IntegrationException() throws Ns8IntegrationException {
        when(restTemplateMock.postForEntity(stringCaptor.capture(), httpEntityCaptor.capture(), eq(MerchantReactivateResponseData.class)))
                .thenThrow(ResourceAccessException.class);

        final Throwable thrown = catchThrowable(() -> testObj.triggerMerchantReinstallEvent(ns8MerchantMock));

        final String url = stringCaptor.getValue();
        assertThat(url).isEqualTo(BASE_BACKEND_URL + PROTECT_PLATFORM_REINSTALL_URL + PLATFORM_NAME_VALUE);
        assertThat(thrown)
                .isInstanceOf(Ns8IntegrationException.class)
                .hasCauseInstanceOf(ResourceAccessException.class)
                .hasFieldOrPropertyWithValue("httpStatus", HttpStatus.SERVICE_UNAVAILABLE);
    }

    @Test
    public void triggerMerchantReinstallEvent_WhenHttpStatusCodeException_ShouldThrowNs8IntegrationException() {
        final HttpClientErrorException clientException = new HttpClientErrorException(HttpStatus.I_AM_A_TEAPOT);
        final String errorStackMessage = String.format("Reactivation of NS8 Merchant failed. URL: [%s], status code [%s], error body: [%s].",
                BASE_BACKEND_URL + PROTECT_PLATFORM_REINSTALL_URL + PLATFORM_NAME_VALUE,
                HttpStatus.I_AM_A_TEAPOT,
                StringUtils.EMPTY);

        when(restTemplateMock.postForEntity(eq(BASE_CLIENT_URL + PLATFORM_ERROR_URL), any(HttpEntity.class), eq(Ns8PlatformErrorResponse.class)))
                .thenReturn(responseEntityMock);
        when(restTemplateMock.postForEntity(stringCaptor.capture(), httpEntityCaptor.capture(), eq(MerchantReactivateResponseData.class)))
                .thenThrow(clientException);

        final Throwable thrown = catchThrowable(() -> testObj.triggerMerchantReinstallEvent(ns8MerchantMock));

        final String url = stringCaptor.getValue();
        assertThat(url).isEqualTo(BASE_BACKEND_URL + PROTECT_PLATFORM_REINSTALL_URL + PLATFORM_NAME_VALUE);
        assertThat(thrown)
                .isInstanceOf(Ns8IntegrationException.class)
                .hasCause(clientException)
                .hasFieldOrPropertyWithValue("httpStatus", HttpStatus.I_AM_A_TEAPOT);
        verify(testObj).sendErrorLogging(NS_8_SERVICES_ERROR_TRIGGER_MERCHANT_REINSTALL_VALUE, errorStackMessage);
    }

    @Test
    public void sendErrorToNs8Platform_WhenLogIsSuccesfullySent_ShouldReturnTrue() {
        when(restTemplateMock.postForEntity(eq(BASE_CLIENT_URL + PLATFORM_ERROR_URL), any(HttpEntity.class), eq(Ns8PlatformErrorResponse.class)))
                .thenReturn(responseEntityMock);

        final Boolean result = testObj.sendErrorLogging(ERROR_MESSAGE, STACK_TRACE_MESSAGE);

        assertThat(result).isEqualTo(Boolean.TRUE);
    }

    @Test
    public void sendErrorToNs8Platform_WhenNs8ReurnsAnError_ShouldReturnFalse() {
        when(responseEntityMock.getBody().getLogged()).thenReturn(Boolean.FALSE);
        when(restTemplateMock.postForEntity(eq(BASE_CLIENT_URL + PLATFORM_ERROR_URL), any(HttpEntity.class), eq(Ns8PlatformErrorResponse.class)))
                .thenReturn(responseEntityMock);

        final Boolean result = testObj.sendErrorLogging(ERROR_MESSAGE, STACK_TRACE_MESSAGE);

        assertThat(result).isEqualTo(Boolean.FALSE);
    }


    private void assertUrlForUriValues(final String uri) {
        final UriComponents components = UriComponentsBuilder.fromUriString(uri).build();
        assertThat(components.getPath()).isEqualTo(BASE_CLIENT_URL + API_TEMPLATE_URL);
    }

    private void assertUriValuesForGet(final String uri) {
        final UriComponents components = UriComponentsBuilder.fromUriString(uri).build();
        assertUrlForUriValues(uri);

        final MultiValueMap<String, String> params = components.getQueryParams();
        assertThat(params).contains(
                entry(ORDER_ID_PARAM, singletonList(ORDER_ID)),
                entry(TOKEN_PARAM, singletonList(TOKEN)),
                entry(VERIFICATION_ID_PARAM, singletonList(VERIFICATION_ID)),
                entry(VIEW_PARAM, singletonList(TEMPLATE_ID)),
                entry(RETURN_URI_PARAM, singletonList(RETURN_URI)));
    }

}
