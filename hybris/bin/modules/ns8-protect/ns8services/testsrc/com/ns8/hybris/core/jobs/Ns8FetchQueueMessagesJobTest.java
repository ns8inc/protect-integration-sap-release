package com.ns8.hybris.core.jobs;

import com.google.gson.JsonObject;
import com.ns8.hybris.core.constants.Ns8servicesConstants;
import com.ns8.hybris.core.data.queue.Ns8QueueMessage;
import com.ns8.hybris.core.integration.exceptions.NS8IntegrationException;
import com.ns8.hybris.core.model.NS8MerchantModel;
import com.ns8.hybris.core.model.Ns8FetchQueueMessagesCronJobModel;
import com.ns8.hybris.core.model.Ns8QueueMessageModel;
import com.ns8.hybris.core.services.Ns8QueueService;
import de.hybris.bootstrap.annotations.UnitTest;
import de.hybris.platform.basecommerce.model.site.BaseSiteModel;
import de.hybris.platform.cronjob.enums.CronJobResult;
import de.hybris.platform.cronjob.enums.CronJobStatus;
import de.hybris.platform.servicelayer.cronjob.PerformResult;
import de.hybris.platform.servicelayer.model.ModelService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.*;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;

import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@UnitTest
@RunWith(MockitoJUnitRunner.class)
public class Ns8FetchQueueMessagesJobTest {

    private static final String MERCHANT_API_KEY = "merchant api key";
    private static final String QUEUE_URL = "url";
    private static final String MESSAGE_ACTION = "some action";
    private static final String ORDER_ID = "order id";
    private static final String RECIEPT_HANDLE = "reciept handle";

    @Spy
    @InjectMocks
    private Ns8FetchQueueMessagesJob testObj;

    @Mock
    private Ns8QueueService ns8QueueServiceMock;
    @Mock
    private ModelService modelServiceMock;
    @Mock
    private Ns8FetchQueueMessagesCronJobModel cronjobMock;
    @Mock
    private BaseSiteModel siteMock;
    @Mock
    private NS8MerchantModel merchantMock;
    @Mock
    private Ns8QueueMessage messageMock;
    @Mock
    private Ns8QueueMessageModel messageModelMock;

    private String messageBody;

    @Before
    public void setUp() {
        testObj.setModelService(modelServiceMock);
        when(cronjobMock.getSite()).thenReturn(siteMock);
        when(cronjobMock.getMaxBatchSize()).thenReturn(1000);
        when(siteMock.getNs8Merchant()).thenReturn(merchantMock);
        when(merchantMock.getApiKey()).thenReturn(MERCHANT_API_KEY);
        when(ns8QueueServiceMock.getQueueUrl(MERCHANT_API_KEY)).thenReturn(QUEUE_URL);

        messageBody = buildBody(MESSAGE_ACTION, ORDER_ID);
        when(messageMock.getBody()).thenReturn(messageBody);
        when(messageMock.getReceiptHandle()).thenReturn(RECIEPT_HANDLE);

        when(modelServiceMock.create(Ns8QueueMessageModel.class)).thenReturn(messageModelMock);

        doReturn(false).when(testObj).callSuperClearAbortRequestedIfNeeded(cronjobMock);
    }

    @Test
    public void perform_ShouldSaveMessagesToDb() {
        when(ns8QueueServiceMock.receiveMessages(QUEUE_URL))
                .thenReturn(Collections.singletonList(messageMock))
                .thenReturn(Collections.emptyList());

        final PerformResult result = testObj.perform(cronjobMock);

        assertThat(result.getResult()).isEqualTo(CronJobResult.SUCCESS);
        assertThat(result.getStatus()).isEqualTo(CronJobStatus.FINISHED);
        verify(messageModelMock).setAction(MESSAGE_ACTION);
        verify(messageModelMock).setBody(messageBody);
        verify(messageModelMock).setOrderId(ORDER_ID);
        verify(modelServiceMock).save(messageModelMock);
    }

    @Test
    public void perform_ShouldDeleteMessageAfterSave() {
        when(ns8QueueServiceMock.receiveMessages(QUEUE_URL))
                .thenReturn(Collections.singletonList(messageMock))
                .thenReturn(Collections.emptyList());
        when(modelServiceMock.create(Ns8QueueMessageModel.class)).thenReturn(messageModelMock);

        testObj.perform(cronjobMock);

        InOrder inOrder = Mockito.inOrder(modelServiceMock, ns8QueueServiceMock);
        inOrder.verify(modelServiceMock).save(messageModelMock);
        inOrder.verify(ns8QueueServiceMock).deleteMessage(MERCHANT_API_KEY, RECIEPT_HANDLE);
    }

    @Test
    public void perform_WhenUserAbortsTheJob_ShouldSetTheJobsAsAborted() {
        doReturn(true).when(testObj).callSuperClearAbortRequestedIfNeeded(cronjobMock);
        when(ns8QueueServiceMock.receiveMessages(QUEUE_URL))
                .thenReturn(Collections.singletonList(messageMock));

        final PerformResult result = testObj.perform(cronjobMock);

        assertThat(result.getResult()).isEqualTo(CronJobResult.ERROR);
        assertThat(result.getStatus()).isEqualTo(CronJobStatus.ABORTED);
        verify(ns8QueueServiceMock).getQueueUrl(MERCHANT_API_KEY);
        verify(modelServiceMock, never()).create(Ns8QueueMessageModel.class);
        verify(modelServiceMock, never()).save(messageModelMock);
        verify(ns8QueueServiceMock, never()).receiveMessages(QUEUE_URL);
        verify(ns8QueueServiceMock, never()).deleteMessage(MERCHANT_API_KEY, RECIEPT_HANDLE);
        verifyNoMoreInteractions(modelServiceMock, ns8QueueServiceMock);
    }

    @Test
    public void perform_NoMoreMessages_ShouldStopReceivingMessages() {
        when(ns8QueueServiceMock.receiveMessages(QUEUE_URL))
                .thenReturn(Collections.singletonList(messageMock))
                .thenReturn(Collections.emptyList());

        final PerformResult result = testObj.perform(cronjobMock);

        assertThat(result.getResult()).isEqualTo(CronJobResult.SUCCESS);
        assertThat(result.getStatus()).isEqualTo(CronJobStatus.FINISHED);
        verify(modelServiceMock).create(Ns8QueueMessageModel.class);
        verify(modelServiceMock).save(messageModelMock);
        verify(ns8QueueServiceMock).getQueueUrl(MERCHANT_API_KEY);
        verify(ns8QueueServiceMock, times(2)).receiveMessages(QUEUE_URL);
        verify(ns8QueueServiceMock).deleteMessage(MERCHANT_API_KEY, RECIEPT_HANDLE);
        verifyNoMoreInteractions(modelServiceMock, ns8QueueServiceMock);
    }

    @Test
    public void perform_BatchSizeReached_ShouldStopReceivingMessages() {
        when(cronjobMock.getMaxBatchSize()).thenReturn(1);
        when(ns8QueueServiceMock.receiveMessages(QUEUE_URL))
                .thenReturn(Collections.singletonList(messageMock));

        final PerformResult result = testObj.perform(cronjobMock);

        assertThat(result.getResult()).isEqualTo(CronJobResult.SUCCESS);
        assertThat(result.getStatus()).isEqualTo(CronJobStatus.FINISHED);
        verify(modelServiceMock).create(Ns8QueueMessageModel.class);
        verify(modelServiceMock).save(messageModelMock);
        verify(ns8QueueServiceMock).getQueueUrl(MERCHANT_API_KEY);
        verify(ns8QueueServiceMock).receiveMessages(QUEUE_URL);
        verify(ns8QueueServiceMock).deleteMessage(MERCHANT_API_KEY, RECIEPT_HANDLE);
        verifyNoMoreInteractions(modelServiceMock);
        verifyNoMoreInteractions(ns8QueueServiceMock);
    }

    @Test
    public void perform_403IntegrationExceptionThrownInFirstRun_ShouldSetTheJobInError() {
        when(ns8QueueServiceMock.receiveMessages(QUEUE_URL))
                .thenThrow(new NS8IntegrationException("message", HttpStatus.FORBIDDEN));

        final PerformResult result = testObj.perform(cronjobMock);

        assertThat(result.getResult()).isEqualTo(CronJobResult.ERROR);
        assertThat(result.getStatus()).isEqualTo(CronJobStatus.FINISHED);
        verify(ns8QueueServiceMock).getQueueUrl(MERCHANT_API_KEY);
        verify(modelServiceMock, never()).create(Ns8QueueMessageModel.class);
        verify(modelServiceMock, never()).save(messageModelMock);
        verify(ns8QueueServiceMock).receiveMessages(QUEUE_URL);
        verify(ns8QueueServiceMock, never()).deleteMessage(MERCHANT_API_KEY, RECIEPT_HANDLE);
        verifyNoMoreInteractions(modelServiceMock, ns8QueueServiceMock);
    }

    @Test
    public void perform_403IntegrationExceptionThrown_ShouldStopReceivingMessages() {
        when(ns8QueueServiceMock.receiveMessages(QUEUE_URL))
                .thenReturn(Collections.singletonList(messageMock))
                .thenThrow(new NS8IntegrationException("message", HttpStatus.FORBIDDEN));

        final PerformResult result = testObj.perform(cronjobMock);

        assertThat(result.getResult()).isEqualTo(CronJobResult.SUCCESS);
        assertThat(result.getStatus()).isEqualTo(CronJobStatus.FINISHED);
        verify(modelServiceMock).create(Ns8QueueMessageModel.class);
        verify(modelServiceMock).save(messageModelMock);
        verify(ns8QueueServiceMock).getQueueUrl(MERCHANT_API_KEY);
        verify(ns8QueueServiceMock, times(2)).receiveMessages(QUEUE_URL);
        verify(ns8QueueServiceMock).deleteMessage(MERCHANT_API_KEY, RECIEPT_HANDLE);
        verifyNoMoreInteractions(modelServiceMock, ns8QueueServiceMock);
    }

    @Test
    public void perform_OtherIntegrationExceptionThrown_ShouldAbortJob() {
        when(ns8QueueServiceMock.receiveMessages(QUEUE_URL))
                .thenThrow(new NS8IntegrationException("message", HttpStatus.INTERNAL_SERVER_ERROR));

        final PerformResult result = testObj.perform(cronjobMock);

        assertThat(result.getResult()).isEqualTo(CronJobResult.ERROR);
        assertThat(result.getStatus()).isEqualTo(CronJobStatus.ABORTED);
        verify(ns8QueueServiceMock).getQueueUrl(MERCHANT_API_KEY);
        verify(ns8QueueServiceMock).receiveMessages(QUEUE_URL);
        verifyNoMoreInteractions(modelServiceMock, ns8QueueServiceMock);
    }

    @Test
    public void perform_NoMerhantForSite_ShouldFailJob() {
        when(siteMock.getNs8Merchant()).thenReturn(null);

        final PerformResult result = testObj.perform(cronjobMock);

        assertThat(result.getResult()).isEqualTo(CronJobResult.FAILURE);
        assertThat(result.getStatus()).isEqualTo(CronJobStatus.ABORTED);
    }

    @Test
    public void isAbortable_ShouldReturnTrue() {
        final boolean result = testObj.isAbortable();

        assertThat(result).isTrue();
    }

    private String buildBody(final String action, final String orderId) {
        final JsonObject obj = new JsonObject();
        obj.addProperty("key1", "value1");
        obj.addProperty(Ns8servicesConstants.MESSAGE_BODY_ACTION_KEY, action.toString());
        obj.addProperty(Ns8servicesConstants.MESSAGE_BODY_ORDERID_KEY, orderId);
        obj.addProperty("key2", "value2");
        return obj.toString();
    }
}
