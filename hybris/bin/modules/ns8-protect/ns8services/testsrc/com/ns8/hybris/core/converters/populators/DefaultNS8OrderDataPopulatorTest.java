package com.ns8.hybris.core.converters.populators;

import com.ns8.hybris.core.data.*;
import com.ns8.hybris.core.model.NS8MerchantModel;
import de.hybris.bootstrap.annotations.UnitTest;
import de.hybris.platform.basecommerce.model.site.BaseSiteModel;
import de.hybris.platform.core.model.c2l.CurrencyModel;
import de.hybris.platform.core.model.order.OrderEntryModel;
import de.hybris.platform.core.model.order.OrderModel;
import de.hybris.platform.core.model.user.AddressModel;
import de.hybris.platform.payment.model.PaymentTransactionModel;
import de.hybris.platform.servicelayer.dto.converter.Converter;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.internal.util.reflection.Whitebox;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@UnitTest
@RunWith(MockitoJUnitRunner.class)
public class DefaultNS8OrderDataPopulatorTest {

    private static final String CURRENCY_ISO_CODE = "EN";
    private static final String ORDER_CODE = "order code";
    private static final double ORDER_TOTAL_PRICE = 12.3d;
    private static final String QUEUE_ID = "queue id";
    private static final String ORDER_NAME = "order name";

    @InjectMocks
    private DefaultNS8OrderDataPopulator testObj;

    @Mock
    private Converter<OrderModel, List<NS8AddressData>> ns8AddressDatasConverterMock;
    @Mock
    private Converter<OrderModel, NS8CustomerData> ns8CustomerDataConverterMock;
    @Mock
    private Converter<OrderEntryModel, NS8LineItemData> ns8LineItemDataConverterMock;
    @Mock
    private Converter<PaymentTransactionModel, NS8TransactionData> ns8TransactionDataConverterMock;
    @Mock
    private Converter<OrderModel, NS8SessionData> ns8SessionDataConverterMock;

    @Mock
    private OrderModel orderMock;
    @Mock
    private CurrencyModel currencyMock;
    @Mock
    private AddressModel deliveryAddressMock, paymentAddressMock;
    @Mock
    private OrderEntryModel orderEntry1Mock, orderEntry2Mock;
    @Mock
    private PaymentTransactionModel paymentTransaction1Mock, paymentTransaction2Mock;
    @Mock
    private BaseSiteModel baseSiteMock;
    @Mock
    private NS8MerchantModel ns8MerchantMock;

    @Mock
    private NS8AddressData ns8DeliveryAddressDataMock, ns8BillingAddressDataMock;
    @Mock
    private NS8CustomerData ns8CustomerDataMock;
    @Mock
    private NS8LineItemData ns8LineItem1Mock, ns8LineItem2Mock;
    @Mock
    private NS8TransactionData ns8TransactionData1Mock, ns8TransactionData2Mock;
    @Mock
    private NS8SessionData ns8SessionDataMock;

    private NS8OrderData target;
    private Date creationTime, modifiedTime;

    @Before
    public void setUp() {
        Whitebox.setInternalState(testObj, "ns8AddressDatasConverter", ns8AddressDatasConverterMock);
        Whitebox.setInternalState(testObj, "ns8CustomerDataConverter", ns8CustomerDataConverterMock);
        Whitebox.setInternalState(testObj, "ns8LineItemDataConverter", ns8LineItemDataConverterMock);
        Whitebox.setInternalState(testObj, "ns8TransactionDataConverter", ns8TransactionDataConverterMock);
        Whitebox.setInternalState(testObj, "ns8SessionDataConverter", ns8SessionDataConverterMock);
        target = new NS8OrderData();
        creationTime = new Date();
        modifiedTime = new Date();

        when(orderMock.getCreationtime()).thenReturn(creationTime);
        when(orderMock.getModifiedtime()).thenReturn(modifiedTime);
        when(orderMock.getCurrency()).thenReturn(currencyMock);
        when(orderMock.getDeliveryAddress()).thenReturn(deliveryAddressMock);
        when(orderMock.getPaymentAddress()).thenReturn(paymentAddressMock);
        when(orderMock.getEntries()).thenReturn(asList(orderEntry1Mock, orderEntry2Mock));
        when(orderMock.getCode()).thenReturn(ORDER_CODE);
        when(orderMock.getTotalPrice()).thenReturn(ORDER_TOTAL_PRICE);
        when(orderMock.getPaymentTransactions()).thenReturn(Arrays.asList(paymentTransaction1Mock, paymentTransaction2Mock));
        when(orderMock.getSite()).thenReturn(baseSiteMock);
        when(orderMock.getName()).thenReturn(ORDER_NAME);
        when(baseSiteMock.getNs8Merchant()).thenReturn(ns8MerchantMock);
        when(ns8MerchantMock.getQueueId()).thenReturn(QUEUE_ID);

        when(currencyMock.getIsocode()).thenReturn(CURRENCY_ISO_CODE);
        when(ns8AddressDatasConverterMock.convert(orderMock)).thenReturn(Arrays.asList(ns8DeliveryAddressDataMock, ns8BillingAddressDataMock));
        when(ns8CustomerDataConverterMock.convert(orderMock)).thenReturn(ns8CustomerDataMock);
        when(ns8LineItemDataConverterMock.convert(orderEntry1Mock)).thenReturn(ns8LineItem1Mock);
        when(ns8LineItemDataConverterMock.convert(orderEntry2Mock)).thenReturn(ns8LineItem2Mock);
        when(ns8TransactionDataConverterMock.convertAll(Arrays.asList(paymentTransaction1Mock, paymentTransaction2Mock))).thenReturn(Arrays.asList(ns8TransactionData1Mock, ns8TransactionData2Mock));
        when(ns8SessionDataConverterMock.convert(orderMock)).thenReturn(ns8SessionDataMock);
    }

    @Test
    public void populate_shouldPopulateAllFields() {
        testObj.populate(orderMock, target);

        assertThat(target.getAddresses()).containsExactlyInAnyOrder(ns8DeliveryAddressDataMock, ns8BillingAddressDataMock);
        assertThat(target.getCustomer()).isEqualTo(ns8CustomerDataMock);
        assertThat(target.getLineItems()).containsExactlyInAnyOrder(ns8LineItem1Mock, ns8LineItem2Mock);
        assertThat(target.getCreatedAt()).isEqualTo(creationTime);
        assertThat(target.getPlatformCreatedAt()).isEqualTo(creationTime);
        assertThat(target.getCurrency()).isEqualTo(CURRENCY_ISO_CODE);
        assertThat(target.getMerchantId()).isEqualTo(QUEUE_ID);
        assertThat(target.getName()).isEqualTo(ORDER_NAME);
        assertThat(target.getPlatformId()).isEqualTo(ORDER_CODE);
        assertThat(target.getTotalPrice()).isEqualTo(ORDER_TOTAL_PRICE);
        assertThat(target.getTransactions()).containsExactlyInAnyOrder(ns8TransactionData1Mock, ns8TransactionData2Mock);
        assertThat(target.getUpdatedAt()).isEqualTo(modifiedTime);
        assertThat(target.getPlatformStatus()).isNull();
        assertThat(target.getStatus()).isNull();
        assertThat(target.getHasGiftCard()).isNull();
        assertThat(target.getSession()).isEqualTo(ns8SessionDataMock);
    }

    @Test
    public void populate_WhenOrderNameIsNotPresent_ShouldSetOrderCode() {
        when(orderMock.getName()).thenReturn(null);

        testObj.populate(orderMock, target);

        assertThat(target.getName()).isEqualTo(ORDER_CODE);
    }

    @Test
    public void populate_WhenOrderNameIsBlank_ShouldSetOrderCode() {
        when(orderMock.getName()).thenReturn("");

        testObj.populate(orderMock, target);

        assertThat(target.getName()).isEqualTo(ORDER_CODE);
    }

}
