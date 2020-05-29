package com.ns8.hybris.notifications.services.impl;

import com.ns8.hybris.notifications.daos.Ns8QueueMessageDao;
import com.ns8.hybris.notifications.model.Ns8QueueMessageModel;
import de.hybris.bootstrap.annotations.UnitTest;
import de.hybris.platform.servicelayer.model.ModelService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import static com.ns8.hybris.notifications.enums.Ns8MessageStatus.FAILED;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@UnitTest
@RunWith(MockitoJUnitRunner.class)
public class DefaultNs8QueueMessagesCleanupServiceTest {

    private static final int QUEUE_MESSAGE_AGE_IN_DAYS = 10;

    @InjectMocks
    private DefaultNs8QueueMessagesCleanUpService testObj;

    @Mock
    private ModelService modelServiceMock;
    @Mock
    private Ns8QueueMessageDao ns8QueueMessageDaoMock;
    @Mock
    private Ns8QueueMessageModel message1Mock, message2Mock;
    @Captor
    private ArgumentCaptor<Date> dateArgumentCaptor;

    @Test
    public void doCleanUp_WhenQueueMessageStatusAndQueueMessageAgeGiven_ShouldFindQueueMessagesAndRemoveThem() {
        when(ns8QueueMessageDaoMock.findNs8QueueMessagesByStatusCreatedBeforeDate(eq(FAILED), any(Date.class))).thenReturn(Arrays.asList(message1Mock, message2Mock));

        testObj.doCleanUp(FAILED, QUEUE_MESSAGE_AGE_IN_DAYS);

        verify(modelServiceMock).remove(message1Mock);
        verify(modelServiceMock).remove(message2Mock);
        verify(ns8QueueMessageDaoMock).findNs8QueueMessagesByStatusCreatedBeforeDate(eq(FAILED), dateArgumentCaptor.capture());

        final Date dateInPast = dateArgumentCaptor.getValue();

        long diffInMillies = Math.abs(new Date().getTime() - dateInPast.getTime());
        long diff = TimeUnit.DAYS.convert(diffInMillies, TimeUnit.MILLISECONDS);

        assertThat(diff).isEqualTo(QUEUE_MESSAGE_AGE_IN_DAYS);
    }
}