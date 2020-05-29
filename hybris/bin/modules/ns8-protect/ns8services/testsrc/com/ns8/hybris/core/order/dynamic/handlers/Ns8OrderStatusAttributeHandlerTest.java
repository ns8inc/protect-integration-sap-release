package com.ns8.hybris.core.order.dynamic.handlers;

import com.google.gson.Gson;
import de.hybris.bootstrap.annotations.UnitTest;
import de.hybris.platform.core.model.order.AbstractOrderModel;
import de.hybris.platform.core.model.order.OrderModel;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@UnitTest
@RunWith(MockitoJUnitRunner.class)
public class Ns8OrderStatusAttributeHandlerTest {

    @InjectMocks
    private Ns8OrderStatusAttributeHandler testObj;

    @Mock
    private OrderModel orderMock;

    @Test
    public void get_ShouldGetTheNs8OrderStatus() {
        when(orderMock.getRiskEventPayload()).thenReturn(new Gson().toJson(createEventBody()));

        final Object result = testObj.get(orderMock);

        assertThat(result).isEqualTo("MERCHANT_REVIEW");
    }

    @Test
    public void get_WhenAbstractOrder_ShouldReturnNull() {
        final Object result = testObj.get(new AbstractOrderModel());

        assertThat(result).isNull();
    }

    @Test(expected = UnsupportedOperationException.class)
    public void set_ShouldThrowUnsupportedOperationException() {
        testObj.set(orderMock, "something");
    }

    private Map<String, Object> createEventBody() {
        final StringBuilder eventBody = new StringBuilder();
        eventBody.append("{");
        eventBody.append("  \"score\": \"0\",");
        eventBody.append("  \"status\": \"MERCHANT_REVIEW\"");
        eventBody.append("}");

        return new Gson().fromJson(eventBody.toString(), Map.class);
    }
}
