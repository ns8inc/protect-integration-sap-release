package com.ns8.hybris.core.converters.populators;

import com.ns8.hybris.core.data.NS8MerchantData;
import com.ns8.hybris.core.model.NS8MerchantModel;
import de.hybris.bootstrap.annotations.UnitTest;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@UnitTest
@RunWith(MockitoJUnitRunner.class)
public class NS8MerchantDataPopulatorTest {

    private static final String EMAIL = "email";
    private static final String FIRST_NAME = "firstName";
    private static final String LAST_NAME = "lastName";
    private static final String PHONE_NUMBER = "phoneNumber";
    private static final String STORE_URL = "storeURL";

    @InjectMocks
    private NS8MerchantDataPopulator testObj;

    @Mock
    private NS8MerchantModel ns8MerchantModelMock;

    @Test
    public void populate_shouldFillMerchantDataDetails() {
        var ns8MerchantData = new NS8MerchantData();

        when(ns8MerchantModelMock.getEmail()).thenReturn(EMAIL);
        when(ns8MerchantModelMock.getFirstName()).thenReturn(FIRST_NAME);
        when(ns8MerchantModelMock.getLastName()).thenReturn(LAST_NAME);
        when(ns8MerchantModelMock.getPhone()).thenReturn(PHONE_NUMBER);
        when(ns8MerchantModelMock.getStoreUrl()).thenReturn(STORE_URL);

        testObj.populate(ns8MerchantModelMock, ns8MerchantData);

        assertThat(ns8MerchantData.getEmail()).isEqualTo(EMAIL);
        assertThat(ns8MerchantData.getFirstName()).isEqualTo(FIRST_NAME);
        assertThat(ns8MerchantData.getLastName()).isEqualTo(LAST_NAME);
        assertThat(ns8MerchantData.getPhone()).isEqualTo(PHONE_NUMBER);
        assertThat(ns8MerchantData.getStoreUrl()).isEqualTo(STORE_URL);
    }
}
