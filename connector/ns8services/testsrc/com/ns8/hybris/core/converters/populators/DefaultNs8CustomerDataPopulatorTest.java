package com.ns8.hybris.core.converters.populators;

import com.ns8.hybris.core.data.Ns8CustomerData;
import de.hybris.bootstrap.annotations.UnitTest;
import de.hybris.platform.core.model.order.OrderModel;
import de.hybris.platform.core.model.user.AddressModel;
import de.hybris.platform.core.model.user.CustomerModel;
import de.hybris.platform.core.model.user.UserModel;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.Mockito.when;

@UnitTest
@RunWith(MockitoJUnitRunner.class)
public class DefaultNs8CustomerDataPopulatorTest {

    private static final String CUSTOMER_EMAIL = "customer@lol.com";
    private static final String CUSTOMER_UID = "customer uid";
    private static final String FIRST_NAME = "name1";
    private static final String LAST_NAME = "name1 name2 name3";
    private static final String COMPANY = "company name";
    private static final String PHONE_1 = "phone 1";
    private static final String PHONE_2 = "phone 2";

    private DefaultNs8CustomerDataPopulator testObj = new DefaultNs8CustomerDataPopulator();

    @Mock
    private CustomerModel customerMock;
    @Mock
    private OrderModel orderMock;
    @Mock
    private AddressModel paymentAddressMock;

    private Ns8CustomerData ns8Customer;
    private Date creationTime;

    @Before
    public void setUp() {
        creationTime = new Date();
        ns8Customer = new Ns8CustomerData();

        when(orderMock.getUser()).thenReturn(customerMock);
        when(orderMock.getPaymentAddress()).thenReturn(paymentAddressMock);
        when(paymentAddressMock.getCompany()).thenReturn(COMPANY);
        when(paymentAddressMock.getPhone1()).thenReturn(PHONE_1);
        when(paymentAddressMock.getPhone2()).thenReturn(PHONE_2);
        when(customerMock.getContactEmail()).thenReturn(CUSTOMER_EMAIL);
        when(customerMock.getName()).thenReturn(FIRST_NAME + " " + LAST_NAME);
        when(customerMock.getCreationtime()).thenReturn(creationTime);
        when(customerMock.getUid()).thenReturn(CUSTOMER_UID);
    }

    @Test
    public void populate_shouldPopulateAllValues() {
        testObj.populate(orderMock, ns8Customer);

        assertThat(ns8Customer.getFirstName()).isEqualTo(FIRST_NAME);
        assertThat(ns8Customer.getLastName()).isEqualTo(LAST_NAME);
        assertThat(ns8Customer.getCompany()).isEqualTo(COMPANY);
        assertThat(ns8Customer.getPhone()).isEqualTo(PHONE_1);
        assertThat(ns8Customer.getEmail()).isEqualTo(CUSTOMER_EMAIL);
        assertThat(ns8Customer.getPlatformCreatedAt()).isEqualTo(creationTime);
        assertThat(ns8Customer.getPlatformId()).isEqualTo(CUSTOMER_UID);
        assertThat(ns8Customer.getGender()).isNullOrEmpty();
        assertThat(ns8Customer.getBirthday()).isNull();
        assertThat(ns8Customer.getIsEmailVerified()).isNull();
        assertThat(ns8Customer.getIsPayingCustomer()).isNull();
        assertThat(ns8Customer.getTotalSpent()).isNull();
    }

    @Test
    public void populate_WhenPhone1NotPresent_ShouldUsePhone2() {
        when(paymentAddressMock.getPhone1()).thenReturn(null);

        testObj.populate(orderMock, ns8Customer);

        assertThat(ns8Customer.getPhone()).isEqualTo(PHONE_2);
    }

    @Test
    public void populate_WhenLastNameNotPresent_ShouldFirstName() {
        when(customerMock.getName()).thenReturn(FIRST_NAME);

        testObj.populate(orderMock, ns8Customer);

        assertThat(ns8Customer.getFirstName()).isEqualTo(FIRST_NAME);
        assertThat(ns8Customer.getLastName()).isEqualTo(FIRST_NAME);
    }

    @Test
    public void populate_WhenContainingMultipleSpace_ShouldReturnFirstAndLastNameWithoutExtraSpaces() {
        when(customerMock.getName()).thenReturn("   " + FIRST_NAME + "    " + " Last  Name     Test           ");

        testObj.populate(orderMock, ns8Customer);

        assertThat(ns8Customer.getFirstName()).isEqualTo(FIRST_NAME);
        assertThat(ns8Customer.getLastName()).isEqualTo("Last Name Test");
    }

    @Test
    public void populate_WhenContainingMultipleSpaceAndNameDuplicated_ShouldReturnFirstAndLastNameCorrectly() {
        when(customerMock.getName()).thenReturn("   " + FIRST_NAME + "    " + FIRST_NAME + " Last  Name");

        testObj.populate(orderMock, ns8Customer);

        assertThat(ns8Customer.getFirstName()).isEqualTo(FIRST_NAME);
        assertThat(ns8Customer.getLastName()).isEqualTo(FIRST_NAME + " " + "Last Name");
    }

    @Test
    public void populate_WhenNameHasLeadingSpacesAndNoLastName_ShouldSetFirstNameInBothCases() {
        when(customerMock.getName()).thenReturn("   " + FIRST_NAME + " ");

        testObj.populate(orderMock, ns8Customer);

        assertThat(ns8Customer.getFirstName()).isEqualTo(FIRST_NAME);
        assertThat(ns8Customer.getLastName()).isEqualTo(FIRST_NAME);
    }

    @Test
    public void populate_WhenUserNotInstanceOfCustomer_ShouldThrowException() {
        when(orderMock.getUser()).thenReturn(Mockito.mock(UserModel.class));

        final Throwable thrown = catchThrowable(() -> testObj.populate(orderMock, ns8Customer));

        assertThat(thrown).isInstanceOf(IllegalArgumentException.class);
    }
}
