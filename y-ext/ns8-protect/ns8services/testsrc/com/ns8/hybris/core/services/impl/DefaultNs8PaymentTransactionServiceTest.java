package com.ns8.hybris.core.services.impl;

import de.hybris.bootstrap.annotations.UnitTest;
import de.hybris.platform.payment.enums.PaymentTransactionType;
import de.hybris.platform.payment.model.PaymentTransactionEntryModel;
import de.hybris.platform.payment.model.PaymentTransactionModel;
import org.assertj.core.api.Assertions;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@UnitTest
@RunWith(MockitoJUnitRunner.class)
public class DefaultNs8PaymentTransactionServiceTest {

    private DefaultNs8PaymentTransactionService testObj = new DefaultNs8PaymentTransactionService();

    @Mock
    private PaymentTransactionModel transactionMock;
    @Mock
    private PaymentTransactionEntryModel authEntryMock, captureEntryMock, cancelEntryMock;


    @Before
    public void setUp() {
        when(authEntryMock.getType()).thenReturn(PaymentTransactionType.AUTHORIZATION);
        when(captureEntryMock.getType()).thenReturn(PaymentTransactionType.CAPTURE);
        when(cancelEntryMock.getType()).thenReturn(PaymentTransactionType.CANCEL);
    }

    @Test
    public void getApplicableEntry_authAndCapture_shouldReturnCapture() {
        when(transactionMock.getEntries()).thenReturn(Arrays.asList(authEntryMock, captureEntryMock));

        final PaymentTransactionEntryModel result = testObj.getApplicableEntry(transactionMock);

        assertThat(result).isEqualTo(captureEntryMock);
    }

    @Test
    public void getApplicableEntry_onlyAuth_shouldReturnAuth() {
        when(transactionMock.getEntries()).thenReturn(Collections.singletonList(authEntryMock));

        final PaymentTransactionEntryModel result = testObj.getApplicableEntry(transactionMock);

        assertThat(result).isEqualTo(authEntryMock);
    }

    @Test
    public void getApplicableEntry_noAuthOrCapture_shouldThrowException() {
        when(transactionMock.getEntries()).thenReturn(Collections.singletonList(cancelEntryMock));

        final Throwable thrown = Assertions.catchThrowable(() -> testObj.getApplicableEntry(transactionMock));

        assertThat(thrown).isInstanceOf(IllegalArgumentException.class);
    }
}
