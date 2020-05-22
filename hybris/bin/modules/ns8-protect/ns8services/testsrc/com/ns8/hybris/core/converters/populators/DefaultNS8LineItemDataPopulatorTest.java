package com.ns8.hybris.core.converters.populators;

import com.ns8.hybris.core.data.NS8LineItemData;
import de.hybris.bootstrap.annotations.UnitTest;
import de.hybris.platform.core.model.order.OrderEntryModel;
import de.hybris.platform.core.model.product.ProductModel;
import de.hybris.platform.util.DiscountValue;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.Answers.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.when;

@UnitTest
@RunWith(MockitoJUnitRunner.class)
public class DefaultNS8LineItemDataPopulatorTest {

    private static final String EAN = "ean13perhaps";
    private static final String MANUFACTURER_NAME = "name of the manufacturer";
    private static final String PRODUCT_NAME = "product name";
    private static final String PRODUCT_CODE = "product code";
    private static final String ORDER_CODE = "order code";
    private static final double TOTAL_PRICE = 12.4d;
    private static final long QUANTITY = 13L;
    private static final int ENTRY_NUMBER = 1;
    private static final String MANUFACTURER_AID = "manufacturerAid";
    private static final String PRODUCT_DESCRIPTION = "lovely description";
    private static final double DISCOUNT_1_VALUE = 12.3d;
    private static final double DISCOUNT_2_VALUE = 2.5d;

    private DefaultNS8LineItemDataPopulator testObj = new DefaultNS8LineItemDataPopulator();

    @Mock(answer = RETURNS_DEEP_STUBS)
    private OrderEntryModel orderEntryMock;
    @Mock
    private ProductModel productMock;
    @Mock
    private DiscountValue discountValue1Mock, discountValue2Mock;

    private NS8LineItemData ns8LineItemData;


    @Before
    public void setUp() {
        ns8LineItemData = new NS8LineItemData();

        when(orderEntryMock.getProduct()).thenReturn(productMock);
        when(orderEntryMock.getOrder().getCode()).thenReturn(ORDER_CODE);
        when(orderEntryMock.getEntryNumber()).thenReturn(ENTRY_NUMBER);
        when(orderEntryMock.getTotalPrice()).thenReturn(TOTAL_PRICE);
        when(orderEntryMock.getQuantity()).thenReturn(QUANTITY);
        when(orderEntryMock.getDiscountValues()).thenReturn(Arrays.asList(discountValue1Mock, discountValue2Mock));

        when(discountValue1Mock.getAppliedValue()).thenReturn(DISCOUNT_1_VALUE);
        when(discountValue2Mock.getAppliedValue()).thenReturn(DISCOUNT_2_VALUE);

        when(productMock.getEan()).thenReturn(EAN);
        when(productMock.getManufacturerName()).thenReturn(MANUFACTURER_NAME);
        when(productMock.getName()).thenReturn(PRODUCT_NAME);
        when(productMock.getDescription()).thenReturn(PRODUCT_DESCRIPTION);
        when(productMock.getCode()).thenReturn(PRODUCT_CODE);
        when(productMock.getManufacturerAID()).thenReturn(MANUFACTURER_AID);
    }

    @Test
    public void populate_shouldPopulateAllFields() {
        testObj.populate(orderEntryMock, ns8LineItemData);

        assertThat(ns8LineItemData.getEan13()).isEqualTo(EAN);
        assertThat(ns8LineItemData.getManufacturer()).isEqualTo(MANUFACTURER_NAME);
        assertThat(ns8LineItemData.getName()).isEqualTo(PRODUCT_NAME);
        assertThat(ns8LineItemData.getPlatformProductId()).isEqualTo(PRODUCT_CODE);
        assertThat(ns8LineItemData.getPlatformId()).isEqualTo(ORDER_CODE + "-" + ENTRY_NUMBER);
        assertThat(ns8LineItemData.getPrice()).isEqualTo(TOTAL_PRICE);
        assertThat(ns8LineItemData.getQuantity()).isEqualTo(QUANTITY);
        assertThat(ns8LineItemData.getSku()).isEqualTo(MANUFACTURER_AID);
        assertThat(ns8LineItemData.getTitle()).isEqualTo(PRODUCT_DESCRIPTION);
        assertThat(ns8LineItemData.getTotalDiscount()).isEqualTo(DISCOUNT_1_VALUE + DISCOUNT_2_VALUE);
        assertThat(ns8LineItemData.getVariantId()).isNullOrEmpty();
        assertThat(ns8LineItemData.getVariantTitle()).isNullOrEmpty();
        assertThat(ns8LineItemData.getUpc()).isNullOrEmpty();
        assertThat(ns8LineItemData.getIsGiftCard()).isNull();
        assertThat(ns8LineItemData.getIsbn()).isNull();
    }

    @Test
    public void populate_noProductAttachedToEntry_shouldThrowException() {
        when(orderEntryMock.getProduct()).thenReturn(null);

        final Throwable thrown = catchThrowable(() -> testObj.populate(orderEntryMock, ns8LineItemData));

        assertThat(thrown).isInstanceOf(IllegalArgumentException.class);
    }
}
