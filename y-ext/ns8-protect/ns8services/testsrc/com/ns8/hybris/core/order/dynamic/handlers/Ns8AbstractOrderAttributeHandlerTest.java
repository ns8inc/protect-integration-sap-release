package com.ns8.hybris.core.order.dynamic.handlers;

import com.google.gson.Gson;
import de.hybris.bootstrap.annotations.UnitTest;
import de.hybris.platform.core.model.order.OrderModel;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@UnitTest
@RunWith(MockitoJUnitRunner.class)
public class Ns8AbstractOrderAttributeHandlerTest {

    protected static final String RISK_BODY_KEY = "risk";

    @Spy
    private Ns8AbstractOrderAttributeHandler testObj;

    @Mock
    private OrderModel orderMock;

    @Test
    public void getDynamicAttribute_ShouldGetTheAttributeValue() {
        when(orderMock.getRiskEventPayload()).thenReturn(new Gson().toJson(createEventBody()));

        final Object result = testObj.getDynamicAttribute(orderMock, RISK_BODY_KEY);

        assertThat((String) result).isEqualTo("MEDIUM");
    }

    @Test
    public void getDynamicAttribute_WhenRiskEventPayloadNotPopulated_ShouldReturnNull() {
        when(orderMock.getRiskEventPayload()).thenReturn(null);

        final Object result = testObj.getDynamicAttribute(orderMock, RISK_BODY_KEY);

        assertThat(result).isNull();
    }

    @Test
    public void getDynamicAttribute_WhenRiskEventPayloadHasNotValues_ShouldReturnNull() {
        when(orderMock.getRiskEventPayload()).thenReturn("{}");

        final Object result = testObj.getDynamicAttribute(orderMock, RISK_BODY_KEY);

        assertThat(result).isNull();
    }

    @Test
    public void getDynamicAttribute_WhenAttributeNotFound_ShouldReturnNull() {
        final Map<String, Object> eventBody = createEventBody();
        eventBody.remove(RISK_BODY_KEY);
        when(orderMock.getRiskEventPayload()).thenReturn(new Gson().toJson(eventBody));

        final Object result = testObj.getDynamicAttribute(orderMock, RISK_BODY_KEY);

        assertThat(result).isNull();
    }

    private Map<String, Object> createEventBody() {
        final StringBuilder eventBody = new StringBuilder();
        eventBody.append("{");
        eventBody.append("  \"risk\": \"MEDIUM\",");
        eventBody.append("  \"score\": \"0\",");
        eventBody.append("  \"status\": \"MERCHANT_REVIEW\"");
        eventBody.append("}");

        return new Gson().fromJson(eventBody.toString(), Map.class);
    }
}
