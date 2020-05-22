package com.ns8.hybris.core.services.api.impl;

import com.ns8.hybris.core.data.NS8MerchantData;
import com.ns8.hybris.core.data.NS8OrderData;
import com.ns8.hybris.core.data.PluginInstallResponseData;
import com.ns8.hybris.core.integration.exceptions.NS8IntegrationException;
import com.ns8.hybris.core.model.NS8MerchantModel;
import com.ns8.hybris.core.services.api.NS8EndpointService;
import de.hybris.bootstrap.annotations.UnitTest;
import de.hybris.platform.core.model.order.OrderModel;
import de.hybris.platform.servicelayer.config.ConfigurationService;
import de.hybris.platform.servicelayer.dto.converter.Converter;
import de.hybris.platform.servicelayer.model.ModelService;
import org.apache.http.entity.ContentType;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.*;
import org.mockito.internal.util.reflection.Whitebox;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.net.ConnectException;

import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Answers.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.*;

@UnitTest
@RunWith(MockitoJUnitRunner.class)
public class DefaultNS8APIServiceTest {

    private static final String PLATFORM_NAME_VALUE = "platform";
    private static final String BASE_CLIENT_URL = "baseClientURL";
    private static final String BASE_BACKEND_URL = "baseBackendURL";
    private static final String API_INIT_SCRIPT = "/api/init/script/";
    private static final String PROTECT_PLATFORM_INSTALL_URL = "/protect/platform/install/";
    private static final String NS_8_SERVICES_PLATFORM_NAME_CONFIGURATION_KEY = "ns8services.platform.name";
    private static final String ACCESS_TOKEN_FROM_RESPONSE = "accessToken";
    private static final String QUEUE_ID_FROM_RESPONSE = "queueId";
    private static final String API_KEY = "apiKey";
    private static final String CREATE_ORDER_ACTION = "CREATE_ORDER_ACTION";
    private static final String API_SWITCH_EXECUTOR = "/api/switch/executor";
    private static final String ACTION_HTTP_PARAM = "action";

    @Spy
    @InjectMocks
    private DefaultNS8APIService testObj;

    @Mock(answer = RETURNS_DEEP_STUBS)
    private ConfigurationService configurationServiceMock;
    @Mock
    private RestTemplate restTemplateMock;
    @Mock
    private Converter<NS8MerchantModel, NS8MerchantData> merchantConverterMock;
    @Mock
    private Converter<OrderModel, NS8OrderData> ns8OrderDataConverterMock;
    @Mock
    private NS8EndpointService ns8EndpointServiceMock;
    @Mock
    private ModelService modelServiceMock;

    @Mock
    private NS8MerchantModel ns8MerchantMock;
    @Mock
    private NS8MerchantData convertedMerchantMock;
    @Mock
    private PluginInstallResponseData pluginInstallResponseDataMock;
    @Mock(answer = RETURNS_DEEP_STUBS)
    private OrderModel orderMock;
    @Mock
    private NS8OrderData ns8OrderDataMock;

    @Captor
    private ArgumentCaptor<HttpEntity<?>> httpEntityCaptor;
    @Captor
    private ArgumentCaptor<String> stringCaptor;

    @Before
    public void setUp() {
        Whitebox.setInternalState(testObj, "ns8OrderDataConverter", ns8OrderDataConverterMock);
        Whitebox.setInternalState(testObj, "ns8MerchantModelNS8MerchantDataConverter", merchantConverterMock);
        doReturn("prettyObject").when(testObj).prettyPrint(any());
        when(ns8EndpointServiceMock.getBaseBackendURL()).thenReturn(BASE_BACKEND_URL);
        when(ns8EndpointServiceMock.getBaseClientURL()).thenReturn(BASE_CLIENT_URL);
        when(configurationServiceMock.getConfiguration().getString(NS_8_SERVICES_PLATFORM_NAME_CONFIGURATION_KEY)).thenReturn(PLATFORM_NAME_VALUE);

        when(merchantConverterMock.convert(ns8MerchantMock)).thenReturn(convertedMerchantMock);
        when(ns8OrderDataConverterMock.convert(orderMock)).thenReturn(ns8OrderDataMock);
        when(orderMock.getSite().getNs8Merchant()).thenReturn(ns8MerchantMock);
        when(pluginInstallResponseDataMock.getAccessToken()).thenReturn(ACCESS_TOKEN_FROM_RESPONSE);
        when(pluginInstallResponseDataMock.getQueueId()).thenReturn(QUEUE_ID_FROM_RESPONSE);
        when(ns8MerchantMock.getApiKey()).thenReturn(API_KEY);
    }

    @Test
    public void triggerPluginInstallEvent_ShouldSendMerchantInformationToNS8UsingRestTemplate() throws NS8IntegrationException {
        when(restTemplateMock.postForEntity(eq(BASE_BACKEND_URL + PROTECT_PLATFORM_INSTALL_URL + PLATFORM_NAME_VALUE), httpEntityCaptor.capture(), eq(PluginInstallResponseData.class)))
                .thenReturn(new ResponseEntity(pluginInstallResponseDataMock, HttpStatus.OK));

        testObj.triggerPluginInstallEvent(ns8MerchantMock);

        assertThat(httpEntityCaptor.getValue().getBody()).isEqualTo(convertedMerchantMock);
        verify(ns8MerchantMock).setApiKey(ACCESS_TOKEN_FROM_RESPONSE);
        verify(ns8MerchantMock).setQueueId(QUEUE_ID_FROM_RESPONSE);
        verify(ns8MerchantMock).setEnabled(true);
        verify(modelServiceMock).save(ns8MerchantMock);
    }

    @Test
    public void triggerPluginInstallEvent_WhenHttpStatusCodeException_ShouldThrowNS8IntegrationException() {
        final HttpClientErrorException clientErrorException = new HttpClientErrorException(HttpStatus.BAD_REQUEST, "exception");
        doThrow(clientErrorException).when(restTemplateMock).postForEntity(eq(BASE_BACKEND_URL + PROTECT_PLATFORM_INSTALL_URL + PLATFORM_NAME_VALUE), any(HttpEntity.class), eq(PluginInstallResponseData.class));

        final Throwable thrown = catchThrowable(() -> testObj.triggerPluginInstallEvent(ns8MerchantMock));

        assertThat(thrown)
                .isInstanceOf(NS8IntegrationException.class)
                .hasCause(clientErrorException);
    }

    @Test
    public void triggerPluginInstallEvent_WhenOtherException_ShouldThrowNS8IntegrationException() {
        doThrow(ConnectException.class).when(restTemplateMock).postForEntity(eq(BASE_BACKEND_URL + PROTECT_PLATFORM_INSTALL_URL + PLATFORM_NAME_VALUE), any(HttpEntity.class), eq(PluginInstallResponseData.class));

        final Throwable thrown = catchThrowable(() -> testObj.triggerPluginInstallEvent(ns8MerchantMock));

        assertThat(thrown)
                .isInstanceOf(NS8IntegrationException.class)
                .hasCauseInstanceOf(ConnectException.class);
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
        when(restTemplateMock.postForObject(eq(BASE_CLIENT_URL + API_INIT_SCRIPT), httpEntityCaptor.capture(), eq(String.class)))
                .thenThrow(clientException);

        final Throwable thrown = catchThrowable(() -> testObj.fetchTrueStatsScript(ns8MerchantMock));

        assertThat(thrown)
                .isInstanceOf(NS8IntegrationException.class)
                .hasCause(clientException)
                .hasFieldOrPropertyWithValue("httpStatus", HttpStatus.I_AM_A_TEAPOT);
    }

    @Test
    public void fetchTrueStatsScript_WhenOtherException_ShouldThrowNS8IntegrationException() {
        when(restTemplateMock.postForObject(eq(BASE_CLIENT_URL + API_INIT_SCRIPT), httpEntityCaptor.capture(), eq(String.class)))
                .thenThrow(ConnectException.class);

        final Throwable thrown = catchThrowable(() -> testObj.fetchTrueStatsScript(ns8MerchantMock));

        assertThat(thrown)
                .isInstanceOf(NS8IntegrationException.class)
                .hasCauseInstanceOf(ConnectException.class)
                .hasFieldOrPropertyWithValue("httpStatus", HttpStatus.SERVICE_UNAVAILABLE);
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
        when(restTemplateMock.postForEntity(stringCaptor.capture(), httpEntityCaptor.capture(), eq(Void.class)))
                .thenThrow(clientException);

        final Throwable thrown = catchThrowable(() -> testObj.triggerCreateOrderActionEvent(orderMock));

        final String url = stringCaptor.getValue();
        assertThat(url).isEqualTo(BASE_CLIENT_URL + API_SWITCH_EXECUTOR + "?" + ACTION_HTTP_PARAM + "=" + CREATE_ORDER_ACTION);
        assertThat(thrown)
                .isInstanceOf(NS8IntegrationException.class)
                .hasCause(clientException)
                .hasFieldOrPropertyWithValue("httpStatus", HttpStatus.I_AM_A_TEAPOT);
    }

    @Test
    public void triggerCreateOrderActionEvent_WhenOtherException_ShouldThrowNs8IntegrationException() {
        when(restTemplateMock.postForEntity(stringCaptor.capture(), httpEntityCaptor.capture(), eq(Void.class)))
                .thenThrow(ConnectException.class);

        final Throwable thrown = catchThrowable(() -> testObj.triggerCreateOrderActionEvent(orderMock));

        final String url = stringCaptor.getValue();
        assertThat(url).isEqualTo(BASE_CLIENT_URL + API_SWITCH_EXECUTOR + "?" + ACTION_HTTP_PARAM + "=" + CREATE_ORDER_ACTION);
        assertThat(thrown)
                .isInstanceOf(NS8IntegrationException.class)
                .hasCauseInstanceOf(ConnectException.class)
                .hasFieldOrPropertyWithValue("httpStatus", HttpStatus.SERVICE_UNAVAILABLE);
    }
}
