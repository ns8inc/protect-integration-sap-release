package com.ns8.hybris.core.order.daos.impl;

import de.hybris.bootstrap.annotations.UnitTest;
import de.hybris.platform.core.model.order.OrderModel;
import de.hybris.platform.servicelayer.search.FlexibleSearchQuery;
import de.hybris.platform.servicelayer.search.FlexibleSearchService;
import de.hybris.platform.servicelayer.search.SearchResult;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.*;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Collections;
import java.util.Optional;

import static org.assertj.core.api.Java6Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

@UnitTest
@RunWith(MockitoJUnitRunner.class)
public class DefaultNs8OrderDaoTest {

    private static final String ORDER_CODE_VALUE = "code";
    @Spy
    @InjectMocks
    private DefaultNs8OrderDao testObj;

    @Mock
    private FlexibleSearchService flexibleSearchServiceMock;
    @Captor
    private ArgumentCaptor<FlexibleSearchQuery> queryArgumentCaptor;
    @Mock
    private SearchResult searchResultMock;
    @Mock
    private OrderModel orderMock;

    @Before
    public void setUp() {
        doReturn(flexibleSearchServiceMock).when(testObj).getSuperFlexibleSearchService();
        when(flexibleSearchServiceMock.search(queryArgumentCaptor.capture())).thenReturn(searchResultMock);
        when(searchResultMock.getResult()).thenReturn(Collections.singletonList(orderMock));
    }

    @Test(expected = IllegalArgumentException.class)
    public void findOrderForCode_WhenOrderCodeNull_ShouldThrowException() {
        testObj.findOrderForCode(null);
    }

    @Test
    public void findOrderForCode_WhenOrderCodeValid_ShouldReturnTheOrder() {
        final Optional<OrderModel> result = testObj.findOrderForCode(ORDER_CODE_VALUE);

        verify(flexibleSearchServiceMock).search(queryArgumentCaptor.capture());
        final FlexibleSearchQuery value = queryArgumentCaptor.getValue();
        assertEquals(ORDER_CODE_VALUE, value.getQueryParameters().get("orderCode"));
        assertThat(result.isPresent()).isTrue();
        assertThat(result.get()).isSameAs(orderMock);
    }
}
