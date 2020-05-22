package com.ns8.hybris.core.converters.populators;

import com.ns8.hybris.core.data.NS8CreditCardData;
import com.ns8.hybris.core.data.NS8TransactionData;
import com.ns8.hybris.core.data.NS8TransactionMethod;
import com.ns8.hybris.core.data.NS8TransactionStatus;
import com.ns8.hybris.core.services.NS8PaymentTransactionService;
import de.hybris.bootstrap.annotations.UnitTest;
import de.hybris.platform.payment.dto.TransactionStatus;
import de.hybris.platform.payment.model.PaymentTransactionEntryModel;
import de.hybris.platform.payment.model.PaymentTransactionModel;
import de.hybris.platform.servicelayer.dto.converter.Converter;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.math.BigDecimal;
import java.util.Date;
import java.util.Map;

import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.Mockito.when;

@UnitTest
@RunWith(MockitoJUnitRunner.class)
public class DefaultNS8TransactionDataPopulatorTest {

    private static final double AMOUNT = 12.6d;
    private static final String CURRENCY_ISO = "iso";
    private static final String TRANSACTION_ID = "transactionId";
    private static final String TRANSACTION_STATUS_DETAIL_AUTH = "transaction status detail AUTH";

    @InjectMocks
    private DefaultNS8TransactionDataPopulator testObj;

    @Mock
    private Converter<PaymentTransactionModel, NS8CreditCardData> ns8CreditCardDataConverter;
    @Mock
    private NS8PaymentTransactionService NS8PaymentTransactionServiceMock;

    @Mock
    private PaymentTransactionModel paymentTransactionMock;
    @Mock
    private NS8CreditCardData ns8CreditCardDataMock;
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private PaymentTransactionEntryModel transactionEntryMock;

    private NS8TransactionData ns8TransactionData;
    private Date creationDate;

    @Before
    public void setUp() {
        testObj.setTransactionStatusMapping(Map.of(
                "ACCEPTED", NS8TransactionStatus.SUCCESS,
                "REJECTED", NS8TransactionStatus.FAILURE));

        ns8TransactionData = new NS8TransactionData();
        creationDate = new Date();
        when(NS8PaymentTransactionServiceMock.getApplicableEntry(paymentTransactionMock)).thenReturn(transactionEntryMock);
        when(ns8CreditCardDataConverter.convert(paymentTransactionMock)).thenReturn(ns8CreditCardDataMock);
        when(paymentTransactionMock.getPlannedAmount()).thenReturn(BigDecimal.valueOf(AMOUNT));
        when(paymentTransactionMock.getCode()).thenReturn(TRANSACTION_ID);
        when(paymentTransactionMock.getCreationtime()).thenReturn(creationDate);
        when(transactionEntryMock.getCurrency().getIsocode()).thenReturn(CURRENCY_ISO);
        when(transactionEntryMock.getTransactionStatusDetails()).thenReturn(TRANSACTION_STATUS_DETAIL_AUTH);
        when(transactionEntryMock.getTransactionStatus()).thenReturn(TransactionStatus.ACCEPTED.name());
    }

    @Test
    public void populate_shouldPopulateAllValues() {
        when(paymentTransactionMock.getEntries()).thenReturn(singletonList(transactionEntryMock));

        testObj.populate(paymentTransactionMock, ns8TransactionData);

        assertThat(ns8TransactionData.getAmount()).isEqualTo(AMOUNT);
        assertThat(ns8TransactionData.getCreditCard()).isEqualTo(ns8CreditCardDataMock);
        assertThat(ns8TransactionData.getCurrency()).isEqualTo(CURRENCY_ISO);
        assertThat(ns8TransactionData.getMethod()).isEqualTo(NS8TransactionMethod.CC);
        assertThat(ns8TransactionData.getPlatformId()).isEqualTo(TRANSACTION_ID);
        assertThat(ns8TransactionData.getProcessedAt()).isEqualTo(creationDate);
        assertThat(ns8TransactionData.getStatus()).isEqualTo(NS8TransactionStatus.SUCCESS);
        assertThat(ns8TransactionData.getStatusDetails()).isEqualTo(TRANSACTION_STATUS_DETAIL_AUTH);
    }

    @Test
    public void populate_noCurrencyOnTransactionEntry_shouldThrowException() {
        when(transactionEntryMock.getCurrency()).thenReturn(null);

        final Throwable thrown = catchThrowable(() -> testObj.populate(paymentTransactionMock, ns8TransactionData));

        assertThat(thrown).isInstanceOf(IllegalArgumentException.class);
    }
}
