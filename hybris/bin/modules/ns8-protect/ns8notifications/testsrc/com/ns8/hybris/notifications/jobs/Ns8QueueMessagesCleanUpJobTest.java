package com.ns8.hybris.notifications.jobs;

import com.ns8.hybris.notifications.model.Ns8QueueMessagesCleanUpCronJobModel;
import com.ns8.hybris.notifications.services.Ns8QueueMessagesCleanUpService;
import de.hybris.bootstrap.annotations.UnitTest;
import de.hybris.platform.cronjob.enums.CronJobResult;
import de.hybris.platform.cronjob.enums.CronJobStatus;
import de.hybris.platform.servicelayer.cronjob.PerformResult;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static com.ns8.hybris.notifications.enums.Ns8MessageStatus.FAILED;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@UnitTest
@RunWith(MockitoJUnitRunner.class)
public class Ns8QueueMessagesCleanUpJobTest {

    private static final int AGE_IN_DAYS = 10;

    @InjectMocks
    private Ns8QueueMessagesCleanUpJob testObj;

    @Mock
    private Ns8QueueMessagesCleanUpService ns8QueueMessagesCleanUpServiceMock;
    @Mock
    private Ns8QueueMessagesCleanUpCronJobModel ns8QueueMessagesCleanUpCronJobMock;

    @Test
    public void perform_WhenCronJobStarts_ShouldCallCleanUpServiceAndFinishWithSuccess() {
        when(ns8QueueMessagesCleanUpCronJobMock.getNs8MessageStatus()).thenReturn(FAILED);
        when(ns8QueueMessagesCleanUpCronJobMock.getAgeInDaysBeforeDeletion()).thenReturn(AGE_IN_DAYS);

        final PerformResult result = testObj.perform(ns8QueueMessagesCleanUpCronJobMock);

        assertThat(result.getResult()).isEqualTo(CronJobResult.SUCCESS);
        assertThat(result.getStatus()).isEqualTo(CronJobStatus.FINISHED);

        verify(ns8QueueMessagesCleanUpServiceMock).doCleanUp(FAILED, AGE_IN_DAYS);
    }
}