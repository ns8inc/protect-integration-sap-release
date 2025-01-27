package com.ns8.hybris.notifications.services.impl;

import com.ns8.hybris.core.data.Ns8PlatformErrorResponse;
import com.ns8.hybris.core.integration.exceptions.Ns8IntegrationException;
import com.ns8.hybris.core.services.api.Ns8ApiService;
import com.ns8.hybris.ns8notifications.data.queue.Ns8QueueMessage;
import com.ns8.hybris.ns8notifications.data.queue.Ns8ReceiveMessageResponse;
import com.ns8.hybris.ns8notifications.data.queue.Ns8ReceiveMessageWrapper;
import com.ns8.hybris.core.services.api.Ns8EndpointService;
import de.hybris.bootstrap.annotations.UnitTest;
import de.hybris.platform.servicelayer.config.ConfigurationService;
import org.apache.commons.lang.StringUtils;
import org.apache.http.entity.ContentType;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.*;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import java.net.SocketTimeoutException;
import java.net.URI;
import java.util.List;
import java.util.Map;

import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.Answers.RETURNS_DEEP_STUBS;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

@UnitTest
@RunWith(MockitoJUnitRunner.class)
public class DefaultNs8QueueServiceTest {

    private static final String QUEUE_URL = "queueUrl";
    private static final String BASE_CLIENT_URL = "baseClientURL";
    private static final String PLATFORM_ERROR_URL = "/api/util/log-platform-error";
    private static final String RECEIPT_HANDLE = "receiptHandle";
    private static final String BASE_CLIENT_API_URL = "https://client-url.me";
    private static final String MERCHANT_API_KEY = "merchantApiKey";
    private static final String NS_8_NOTIFICATIONS_ERROR_RECEIVING_MESSAGES_KEY = "ns8notifications.error.receiveMessages";
    private static final String NS_8_NOTIFICATIONS_ERROR_RECEIVING_MESSAGES_VALUE = "Error receiving messages from Ns8";

    @Spy
    @InjectMocks
    private DefaultNs8QueueService testObj;

    @Mock
    private RestTemplate restTemplateMock;
    @Mock
    private ResponseEntity<Ns8ReceiveMessageWrapper> receiveMessageResponseEntityMock;
    @Mock
    private ResponseEntity<Map> deleteMessageResponseEntityMock;
    @Mock
    private ResponseEntity<Map> getQueueUrlResponseEntityMock;
    @Mock
    private Ns8ReceiveMessageWrapper receiveMessageWrapperMock;
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private Ns8ReceiveMessageResponse receiveMessageResponseMock;
    @Mock
    private Ns8QueueMessage ns8QueueMessageMock;
    @Mock
    private Ns8EndpointService ns8EndpointServiceMock;
    @Mock(answer = RETURNS_DEEP_STUBS)
    private ConfigurationService configurationServiceMock;
    @Mock
    private Ns8ApiService ns8ApiServiceMock;
    @Mock
    private ResponseEntity<Ns8PlatformErrorResponse> responseEntityMock;
    @Mock
    private Map<String, Object> deleteMessageResponseMock;
    @Mock
    private Map<String, String> getQueueUrlResponseMock;
    @Captor
    private ArgumentCaptor<HttpEntity> httpEntityArgumentCaptor;
    @Captor
    private ArgumentCaptor<String> uriArgumentCaptor;

    @Before
    public void setUp() {
        when(ns8EndpointServiceMock.getBaseClientURL()).thenReturn(BASE_CLIENT_API_URL);
        when(restTemplateMock.exchange(eq(URI.create(QUEUE_URL)), eq(HttpMethod.GET), httpEntityArgumentCaptor.capture(), eq(Ns8ReceiveMessageWrapper.class))).thenReturn(receiveMessageResponseEntityMock);
        when(receiveMessageResponseEntityMock.getBody()).thenReturn(receiveMessageWrapperMock);
        when(deleteMessageResponseEntityMock.getBody()).thenReturn(deleteMessageResponseMock);
        when(getQueueUrlResponseEntityMock.getBody()).thenReturn(getQueueUrlResponseMock);
        when(receiveMessageWrapperMock.getReceiveMessageResponse()).thenReturn(receiveMessageResponseMock);
        when(configurationServiceMock.getConfiguration().getString(NS_8_NOTIFICATIONS_ERROR_RECEIVING_MESSAGES_KEY)).thenReturn(NS_8_NOTIFICATIONS_ERROR_RECEIVING_MESSAGES_VALUE);
    }

    @Test
    public void receiveMessage_WhenMerchantKeyGiven_ShouldRetrieveMessages() {
        doReturn(QUEUE_URL).when(testObj).getQueueUrl(MERCHANT_API_KEY);
        when(receiveMessageResponseMock.getReceiveMessageResult().getMessages()).thenReturn(singletonList(ns8QueueMessageMock));

        final List<Ns8QueueMessage> result = testObj.receiveMessages(QUEUE_URL);

        assertThat(result).containsExactly(ns8QueueMessageMock);

        verify(restTemplateMock).exchange(eq(URI.create(QUEUE_URL)), eq(HttpMethod.GET), httpEntityArgumentCaptor.capture(), eq(Ns8ReceiveMessageWrapper.class));

        final HttpEntity httpEntity = httpEntityArgumentCaptor.getValue();
        assertThat(httpEntity.getHeaders().get(HttpHeaders.ACCEPT)).containsExactly(ContentType.APPLICATION_JSON.toString());
    }

    @Test
    public void receiveMessage_WhenHttpStatusCodeException_ShouldThrowNs8IntegrationException() {
        final HttpClientErrorException exception = new HttpClientErrorException(HttpStatus.BAD_REQUEST, "exception");
        final String errorStackMessage = String.format("Receiving of messages failed. Status code [%s], error body: [%s].",
                HttpStatus.BAD_REQUEST,
                StringUtils.EMPTY);

        when(restTemplateMock.postForEntity(eq(BASE_CLIENT_URL + PLATFORM_ERROR_URL), any(HttpEntity.class), eq(Ns8PlatformErrorResponse.class)))
                .thenReturn(responseEntityMock);
        doReturn(QUEUE_URL).when(testObj).getQueueUrl(MERCHANT_API_KEY);
        doThrow(exception).when(restTemplateMock).exchange(eq(URI.create(QUEUE_URL)), eq(HttpMethod.GET), httpEntityArgumentCaptor.capture(), eq(Ns8ReceiveMessageWrapper.class));

        final Throwable thrown = catchThrowable(() -> testObj.receiveMessages(QUEUE_URL));

        assertThat(thrown)
                .isInstanceOf(Ns8IntegrationException.class)
                .hasCause(exception);
        verify(ns8ApiServiceMock).sendErrorLogging(NS_8_NOTIFICATIONS_ERROR_RECEIVING_MESSAGES_VALUE, errorStackMessage);
    }

    @Test
    public void receiveMessage_WhenResourceAccessException_ShouldThrowNs8IntegrationException() {
        final ResourceAccessException exception = new ResourceAccessException("exception", new SocketTimeoutException());

        doReturn(QUEUE_URL).when(testObj).getQueueUrl(MERCHANT_API_KEY);
        doThrow(exception).when(restTemplateMock).exchange(eq(URI.create(QUEUE_URL)), eq(HttpMethod.GET), httpEntityArgumentCaptor.capture(), eq(Ns8ReceiveMessageWrapper.class));

        final Throwable thrown = catchThrowable(() -> testObj.receiveMessages(QUEUE_URL));

        assertThat(thrown)
                .isInstanceOf(Ns8IntegrationException.class)
                .hasCause(exception);
    }

    @Test
    public void deleteMessage_WhenMerchantKeyAndReceiptHandleGiven_ShouldDeleteMessageAndReturnTrueIfSuccess() {
        when(restTemplateMock.exchange(uriArgumentCaptor.capture(), eq(HttpMethod.POST), httpEntityArgumentCaptor.capture(), any(ParameterizedTypeReference.class))).thenReturn(deleteMessageResponseEntityMock);
        when(deleteMessageResponseMock.get("success")).thenReturn(true);

        final boolean result = testObj.deleteMessage(MERCHANT_API_KEY, RECEIPT_HANDLE);

        assertThat(result).isTrue();

        verify(restTemplateMock).exchange(uriArgumentCaptor.capture(), eq(HttpMethod.POST), httpEntityArgumentCaptor.capture(), any(ParameterizedTypeReference.class));

        final HttpEntity httpEntity = httpEntityArgumentCaptor.getValue();
        assertThat(httpEntity.getHeaders().get(HttpHeaders.AUTHORIZATION)).containsExactly("Bearer " + MERCHANT_API_KEY);

        final URI uri = URI.create(uriArgumentCaptor.getValue());
        assertThat(uri.getPath()).isEqualTo("/api/polling/DeleteQueueMessage");
        assertThat(uri.getQuery()).isEqualTo("receiptHandle=receiptHandle");
    }

    @Test
    public void deleteMessage_WhenMerchantKeyAndReceiptHandleGiven_ShouldDeleteMessageAndReturnFalseIfNotSuccess() {
        when(restTemplateMock.exchange(uriArgumentCaptor.capture(), eq(HttpMethod.POST), httpEntityArgumentCaptor.capture(), any(ParameterizedTypeReference.class))).thenReturn(deleteMessageResponseEntityMock);
        when(deleteMessageResponseMock.get("success")).thenReturn(false);

        final boolean result = testObj.deleteMessage(MERCHANT_API_KEY, RECEIPT_HANDLE);

        assertThat(result).isFalse();

        verify(restTemplateMock).exchange(uriArgumentCaptor.capture(), eq(HttpMethod.POST), httpEntityArgumentCaptor.capture(), any(ParameterizedTypeReference.class));

        final HttpEntity httpEntity = httpEntityArgumentCaptor.getValue();
        assertThat(httpEntity.getHeaders().get(HttpHeaders.AUTHORIZATION)).containsExactly("Bearer " + MERCHANT_API_KEY);

        final URI uri = URI.create(uriArgumentCaptor.getValue());
        assertThat(uri.getPath()).isEqualTo("/api/polling/DeleteQueueMessage");
        assertThat(uri.getQuery()).isEqualTo("receiptHandle=receiptHandle");
    }

    @Test
    public void deleteMessage_WhenHttpStatusCodeException_ShouldThrowNs8IntegrationException() {
        final HttpClientErrorException clientErrorException = new HttpClientErrorException(HttpStatus.BAD_REQUEST, "exception");
        doThrow(clientErrorException).when(restTemplateMock).exchange(uriArgumentCaptor.capture(), eq(HttpMethod.POST), httpEntityArgumentCaptor.capture(), any(ParameterizedTypeReference.class));

        final Throwable thrown = catchThrowable(() -> testObj.deleteMessage(MERCHANT_API_KEY, RECEIPT_HANDLE));

        assertThat(thrown)
                .isInstanceOf(Ns8IntegrationException.class)
                .hasCause(clientErrorException);
    }

    @Test
    public void deleteMessage_WhenResourceException_ShouldThrowNs8IntegrationException() {
        final ResourceAccessException exception = new ResourceAccessException("exception", new SocketTimeoutException());
        doThrow(exception).when(restTemplateMock).exchange(uriArgumentCaptor.capture(), eq(HttpMethod.POST), httpEntityArgumentCaptor.capture(), any(ParameterizedTypeReference.class));

        final Throwable thrown = catchThrowable(() -> testObj.deleteMessage(MERCHANT_API_KEY, RECEIPT_HANDLE));

        assertThat(thrown)
                .isInstanceOf(Ns8IntegrationException.class)
                .hasCause(exception);
    }

    @Test
    public void getQueueUrl_WhenMerchantAKeyProvided_ShouldReturnTheUrlOfTheQueue() {
        when(restTemplateMock.exchange(uriArgumentCaptor.capture(), eq(HttpMethod.POST), httpEntityArgumentCaptor.capture(), any(ParameterizedTypeReference.class))).thenReturn(getQueueUrlResponseEntityMock);
        when(getQueueUrlResponseMock.get("url")).thenReturn(QUEUE_URL);

        final String result = testObj.getQueueUrl(MERCHANT_API_KEY);

        assertThat(result).isEqualTo(QUEUE_URL);

        verify(restTemplateMock).exchange(uriArgumentCaptor.capture(), eq(HttpMethod.POST), httpEntityArgumentCaptor.capture(), any(ParameterizedTypeReference.class));

        final HttpEntity httpEntity = httpEntityArgumentCaptor.getValue();
        assertThat(httpEntity.getHeaders().get(HttpHeaders.AUTHORIZATION)).containsExactly("Bearer " + MERCHANT_API_KEY);

        final URI uri = URI.create(uriArgumentCaptor.getValue());
        assertThat(uri.getPath()).isEqualTo("/api/polling/GetQueueUrl");
    }

    @Test
    public void getQueueUrl_WhenHttpStatusCodeException_ShouldThrowNs8IntegrationException() {
        final HttpClientErrorException clientErrorException = new HttpClientErrorException(HttpStatus.BAD_REQUEST, "exception");
        doThrow(clientErrorException).when(restTemplateMock).exchange(uriArgumentCaptor.capture(), eq(HttpMethod.POST), httpEntityArgumentCaptor.capture(), any(ParameterizedTypeReference.class));

        final Throwable thrown = catchThrowable(() -> testObj.getQueueUrl(MERCHANT_API_KEY));

        assertThat(thrown)
                .isInstanceOf(Ns8IntegrationException.class)
                .hasCause(clientErrorException);
    }

    @Test
    public void getQueueUrl_WhenResourceException_ShouldThrowNs8IntegrationException() {
        final ResourceAccessException exception = new ResourceAccessException("exception", new SocketTimeoutException());
        doThrow(exception).when(restTemplateMock).exchange(uriArgumentCaptor.capture(), eq(HttpMethod.POST), httpEntityArgumentCaptor.capture(), any(ParameterizedTypeReference.class));

        final Throwable thrown = catchThrowable(() -> testObj.getQueueUrl(MERCHANT_API_KEY));

        assertThat(thrown)
                .isInstanceOf(Ns8IntegrationException.class)
                .hasCause(exception);
    }

}
