package com.ns8.hybris.core.order.dynamic.handlers;

import de.hybris.bootstrap.annotations.UnitTest;
import de.hybris.platform.core.model.order.AbstractOrderModel;
import de.hybris.platform.core.model.order.OrderModel;
import de.hybris.platform.fraud.model.FraudReportModel;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Collections;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@UnitTest
@RunWith(MockitoJUnitRunner.class)
public class Ns8ScoreAttributeHandlerTest {

    @InjectMocks
    private Ns8ScoreAttributeHandler testObj;
    @Mock
    private OrderModel orderMock;
    @Mock
    private AbstractOrderModel abstractOrderModelMock;
    @Mock
    private FraudReportModel fraudReportMock;

    @Test
    public void get_ShouldGetTheNs8Score() {
        when(orderMock.getFraudReports()).thenReturn(Set.of(fraudReportMock));
        when(fraudReportMock.getScore()).thenReturn(100d);

        final Object result = testObj.get(orderMock);

        assertThat(result).isEqualTo(100d);
    }

    @Test
    public void get_WhenAbstractOrder_ShouldReturnNull() {
        final Object result = testObj.get(abstractOrderModelMock);

        assertThat(result).isNull();
    }

    @Test
    public void get_WhenNoFraudReport_ShouldReturnNull() {
        when(orderMock.getFraudReports()).thenReturn(Collections.emptySet());

        final Object result = testObj.get(orderMock);

        assertThat(result).isNull();
    }

    @Test
    public void get_WhenNoScoreOnFraudReport_ShouldReturnNull() {
        when(orderMock.getFraudReports()).thenReturn(Set.of(fraudReportMock));
        when(fraudReportMock.getScore()).thenReturn(0D);

        final Object result = testObj.get(abstractOrderModelMock);

        assertThat(result).isNull();
    }

    @Test
    public void get_WhenScoreIs0D_ShouldGetTheNs8Score() {
        when(orderMock.getFraudReports()).thenReturn(Set.of(fraudReportMock));
        when(fraudReportMock.getScore()).thenReturn(0d);

        final Object result = testObj.get(orderMock);

        assertThat(result).isEqualTo(0d);
    }
}
