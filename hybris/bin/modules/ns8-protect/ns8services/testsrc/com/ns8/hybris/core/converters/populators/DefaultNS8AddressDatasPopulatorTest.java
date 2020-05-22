package com.ns8.hybris.core.converters.populators;

import com.ns8.hybris.core.data.NS8AddressData;
import com.ns8.hybris.core.data.NS8AddressType;
import de.hybris.bootstrap.annotations.UnitTest;
import de.hybris.platform.core.model.order.OrderModel;
import de.hybris.platform.core.model.user.AddressModel;
import de.hybris.platform.core.model.user.UserModel;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.Answers.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.when;

@UnitTest
@RunWith(MockitoJUnitRunner.class)
public class DefaultNS8AddressDatasPopulatorTest {

    private static final String PAYMENT_LINE_1 = "payment line 1";
    private static final String PAYMENT_LINE_2 = "payment Line 2";
    private static final String PAYMENT_POSTAL_CODE = "payment postal code";
    private static final String PAYMENT_COMPANY = "payment company";
    private static final String PAYMENT_TOWN = "payment town";
    private static final String PAYMENT_REGION_ISO = "payment region_iso";
    private static final String PAYMENT_COUNTRY_ISO = "payment country_iso";
    private static final String PAYMENT_REGION_NAME = "payment region name";
    private static final String PAYMENT_COUNTRY_NAME = "payment country name";
    private static final String DELIVERY_LINE_1 = "delivery line 1";
    private static final String DELIVERY_LINE_2 = "delivery Line 2";
    private static final String DELIVERY_POSTAL_CODE = "delivery postal code";
    private static final String DELIVERY_COMPANY = "delivery company";
    private static final String DELIVERY_TOWN = "delivery town";
    private static final String DELIVERY_REGION_ISO = "delivery region_iso";
    private static final String DELIVERY_COUNTRY_ISO = "delivery country_iso";
    private static final String DELIVERY_REGION_NAME = "delivery region name";
    private static final String DELIVERY_COUNTRY_NAME = "delivery country name";
    private static final String PAYMENT_FIRST_NAME = "bob";
    private static final String PAYMENT_LAST_NAME = "hansen";
    private static final String DELIVERY_FIRST_NAME = "otto";
    private static final String DELIVERY_LAST_NAME = "jensen";

    private DefaultNS8AddressDatasPopulator testObj = new DefaultNS8AddressDatasPopulator();

    @Mock
    private OrderModel orderMock;
    @Mock(answer = RETURNS_DEEP_STUBS)
    private AddressModel paymentAddressMock, deliveryAddressMock;
    @Mock
    private UserModel customerMock;

    private List<NS8AddressData> target;

    @Before
    public void setUp() {
        target = new ArrayList<>();
        when(orderMock.getUser()).thenReturn(customerMock);
        when(orderMock.getPaymentAddress()).thenReturn(paymentAddressMock);
        when(orderMock.getDeliveryAddress()).thenReturn(deliveryAddressMock);
        when(paymentAddressMock.getLine1()).thenReturn(PAYMENT_LINE_1);
        when(paymentAddressMock.getLine2()).thenReturn(PAYMENT_LINE_2);
        when(paymentAddressMock.getTown()).thenReturn(PAYMENT_TOWN);
        when(paymentAddressMock.getCompany()).thenReturn(PAYMENT_COMPANY);
        when(paymentAddressMock.getPostalcode()).thenReturn(PAYMENT_POSTAL_CODE);
        when(paymentAddressMock.getRegion().getIsocode()).thenReturn(PAYMENT_REGION_ISO);
        when(paymentAddressMock.getRegion().getName()).thenReturn(PAYMENT_REGION_NAME);
        when(paymentAddressMock.getCountry().getIsocode()).thenReturn(PAYMENT_COUNTRY_ISO);
        when(paymentAddressMock.getCountry().getName()).thenReturn(PAYMENT_COUNTRY_NAME);
        when(paymentAddressMock.getFirstname()).thenReturn(PAYMENT_FIRST_NAME);
        when(paymentAddressMock.getLastname()).thenReturn(PAYMENT_LAST_NAME);

        when(deliveryAddressMock.getLine1()).thenReturn(DELIVERY_LINE_1);
        when(deliveryAddressMock.getLine2()).thenReturn(DELIVERY_LINE_2);
        when(deliveryAddressMock.getTown()).thenReturn(DELIVERY_TOWN);
        when(deliveryAddressMock.getCompany()).thenReturn(DELIVERY_COMPANY);
        when(deliveryAddressMock.getPostalcode()).thenReturn(DELIVERY_POSTAL_CODE);
        when(deliveryAddressMock.getRegion().getIsocode()).thenReturn(DELIVERY_REGION_ISO);
        when(deliveryAddressMock.getRegion().getName()).thenReturn(DELIVERY_REGION_NAME);
        when(deliveryAddressMock.getCountry().getIsocode()).thenReturn(DELIVERY_COUNTRY_ISO);
        when(deliveryAddressMock.getCountry().getName()).thenReturn(DELIVERY_COUNTRY_NAME);
        when(deliveryAddressMock.getFirstname()).thenReturn(DELIVERY_FIRST_NAME);
        when(deliveryAddressMock.getLastname()).thenReturn(DELIVERY_LAST_NAME);
    }

    @Test
    public void populate() {
        testObj.populate(orderMock, target);

        final Optional<NS8AddressData> billingAddressOpt = target.stream().filter(address -> address.getType().equals(NS8AddressType.BILLING)).findFirst();
        final Optional<NS8AddressData> deliveryAddressOpt = target.stream().filter(address -> address.getType().equals(NS8AddressType.SHIPPING)).findFirst();

        assertThat(billingAddressOpt).isPresent();
        final NS8AddressData billingAddress = billingAddressOpt.get();
        assertThat(billingAddress.getAddress1()).isEqualTo(PAYMENT_LINE_1);
        assertThat(billingAddress.getAddress2()).isEqualTo(PAYMENT_LINE_2);
        assertThat(billingAddress.getCity()).isEqualTo(PAYMENT_TOWN);
        assertThat(billingAddress.getCompany()).isEqualTo(PAYMENT_COMPANY);
        assertThat(billingAddress.getCountry()).isEqualTo(PAYMENT_COUNTRY_NAME);
        assertThat(billingAddress.getCountryCode()).isEqualTo(PAYMENT_COUNTRY_ISO);
        assertThat(billingAddress.getRegion()).isEqualTo(PAYMENT_REGION_NAME);
        assertThat(billingAddress.getRegionCode()).isEqualTo(PAYMENT_REGION_ISO);
        assertThat(billingAddress.getZip()).isEqualTo(PAYMENT_POSTAL_CODE);
        assertThat(billingAddress.getName()).isEqualTo(PAYMENT_FIRST_NAME + " " + PAYMENT_LAST_NAME);
        assertThat(billingAddress.getLatitude()).isNull();
        assertThat(billingAddress.getLongitude()).isNull();

        assertThat(deliveryAddressOpt).isPresent();
        final NS8AddressData deliveryAddress = deliveryAddressOpt.get();
        assertThat(deliveryAddress.getAddress1()).isEqualTo(DELIVERY_LINE_1);
        assertThat(deliveryAddress.getAddress2()).isEqualTo(DELIVERY_LINE_2);
        assertThat(deliveryAddress.getCity()).isEqualTo(DELIVERY_TOWN);
        assertThat(deliveryAddress.getCompany()).isEqualTo(DELIVERY_COMPANY);
        assertThat(deliveryAddress.getCountry()).isEqualTo(DELIVERY_COUNTRY_NAME);
        assertThat(deliveryAddress.getCountryCode()).isEqualTo(DELIVERY_COUNTRY_ISO);
        assertThat(deliveryAddress.getRegion()).isEqualTo(DELIVERY_REGION_NAME);
        assertThat(deliveryAddress.getRegionCode()).isEqualTo(DELIVERY_REGION_ISO);
        assertThat(deliveryAddress.getZip()).isEqualTo(DELIVERY_POSTAL_CODE);
        assertThat(deliveryAddress.getName()).isEqualTo(DELIVERY_FIRST_NAME + " " + DELIVERY_LAST_NAME);
        assertThat(deliveryAddress.getLatitude()).isNull();
        assertThat(deliveryAddress.getLongitude()).isNull();
    }

    @Test
    public void populate_noPaymentAddress_shouldThrowException() {
        when(orderMock.getPaymentAddress()).thenReturn(null);

        final Throwable thrown = catchThrowable(() -> testObj.populate(orderMock, target));

        assertThat(thrown).isInstanceOf(IllegalArgumentException.class);
    }
}
