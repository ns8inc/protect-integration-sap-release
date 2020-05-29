package com.ns8.hybris.core.converters.populators;

import com.ns8.hybris.core.data.Ns8CreditCardData;
import com.ns8.hybris.core.data.Ns8CreditCardTransactionType;
import com.ns8.hybris.core.services.Ns8PaymentTransactionService;
import de.hybris.bootstrap.annotations.UnitTest;
import de.hybris.platform.core.enums.CreditCardType;
import de.hybris.platform.core.model.order.payment.CreditCardPaymentInfoModel;
import de.hybris.platform.core.model.order.payment.PaymentInfoModel;
import de.hybris.platform.payment.enums.PaymentTransactionType;
import de.hybris.platform.payment.model.PaymentTransactionEntryModel;
import de.hybris.platform.payment.model.PaymentTransactionModel;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Map;

import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.Mockito.when;

@UnitTest
@RunWith(MockitoJUnitRunner.class)
public class DefaultNs8CreditCardDataPopulatorTest {

    private static final String VALID_TO_MONTH = "11";
    private static final String VALID_TO_YEAR = "23";
    private static final CreditCardType CREDIT_CARD_TYPE = CreditCardType.AMEX;
    private static final String CREDIT_CARD_NUMBER = "**********221111";
    private static final String FIRST_NAME = "jens";
    private static final String LAST_NAME = "hansen";

    @InjectMocks
    private DefaultNs8CreditCardDataPopulator testObj;

    @Mock
    protected Ns8PaymentTransactionService Ns8PaymentTransactionServiceMock;


    @Mock
    private PaymentTransactionModel paymentTransactionMock;
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private CreditCardPaymentInfoModel ccPaymentInfoMock;
    @Mock
    private PaymentTransactionEntryModel transactionEntryMock;

    private Ns8CreditCardData ns8CreditCardData;

    @Before
    public void setUp() {
        testObj.setTransactionTypeMapping(Map.of(
                PaymentTransactionType.AUTHORIZATION, Ns8CreditCardTransactionType.AUTHORIZATION,
                PaymentTransactionType.CAPTURE, Ns8CreditCardTransactionType.CAPTURE));
        ns8CreditCardData = new Ns8CreditCardData();

        when(Ns8PaymentTransactionServiceMock.getApplicableEntry(paymentTransactionMock)).thenReturn(transactionEntryMock);
        when(paymentTransactionMock.getInfo()).thenReturn(ccPaymentInfoMock);
        when(transactionEntryMock.getType()).thenReturn(PaymentTransactionType.AUTHORIZATION);

        when(ccPaymentInfoMock.getValidToMonth()).thenReturn(VALID_TO_MONTH);
        when(ccPaymentInfoMock.getValidToYear()).thenReturn(VALID_TO_YEAR);
        when(ccPaymentInfoMock.getType()).thenReturn(CREDIT_CARD_TYPE);
        when(ccPaymentInfoMock.getNumber()).thenReturn(CREDIT_CARD_NUMBER);
        when(ccPaymentInfoMock.getBillingAddress().getFirstname()).thenReturn(FIRST_NAME);
        when(ccPaymentInfoMock.getBillingAddress().getLastname()).thenReturn(LAST_NAME);
    }

    @Test
    public void populate_shouldPopulateAllValues() {
        when(paymentTransactionMock.getEntries()).thenReturn(singletonList(transactionEntryMock));

        testObj.populate(paymentTransactionMock, ns8CreditCardData);

        assertThat(ns8CreditCardData.getCardExpiration()).isEqualTo(VALID_TO_MONTH + "/" + VALID_TO_YEAR);
        assertThat(ns8CreditCardData.getCardHolder()).isEqualTo(FIRST_NAME + " " + LAST_NAME);
        assertThat(ns8CreditCardData.getCreditCardCompany()).isEqualTo(CREDIT_CARD_TYPE.toString());
        assertThat(ns8CreditCardData.getCreditCardNumber()).isEqualTo(CREDIT_CARD_NUMBER);
        assertThat(ns8CreditCardData.getTransactionType()).isEqualTo(Ns8CreditCardTransactionType.AUTHORIZATION);
        assertThat(ns8CreditCardData.getCvvResultCode()).isNull();
        assertThat(ns8CreditCardData.getAvsResultCode()).isNull();
        assertThat(ns8CreditCardData.getCreditCardBin()).isNull();
        assertThat(ns8CreditCardData.getGateway()).isNullOrEmpty();
    }

    @Test
    public void populate_noPaymentInfo_shouldThrowException() {
        when(paymentTransactionMock.getInfo()).thenReturn(null);

        final Throwable thrown = catchThrowable(() -> testObj.populate(paymentTransactionMock, ns8CreditCardData));

        assertThat(thrown).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    public void populate_paymentInfoNotCreditCard_shouldThrowException() {
        when(paymentTransactionMock.getInfo()).thenReturn(Mockito.mock(PaymentInfoModel.class));

        final Throwable thrown = catchThrowable(() -> testObj.populate(paymentTransactionMock, ns8CreditCardData));

        assertThat(thrown).isInstanceOf(IllegalArgumentException.class);
    }
}
