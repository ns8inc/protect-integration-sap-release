package com.ns8.hybris.core.fraud.services.impl;

import com.ns8.hybris.core.enums.Ns8RiskType;
import com.ns8.hybris.core.fraud.impl.Ns8FraudFactor;
import com.ns8.hybris.core.fraud.impl.Ns8FraudServiceResponse;
import com.ns8.hybris.core.fraud.impl.Ns8FraudSymptom;
import com.ns8.hybris.core.model.Ns8FraudFactorModel;
import com.ns8.hybris.fulfilmentprocess.enums.Ns8FraudReportRisk;
import com.ns8.hybris.fulfilmentprocess.enums.Ns8FraudReportStatus;
import de.hybris.bootstrap.annotations.UnitTest;
import de.hybris.platform.basecommerce.enums.FraudStatus;
import de.hybris.platform.core.model.order.OrderModel;
import de.hybris.platform.fraud.FraudService;
import de.hybris.platform.fraud.model.FraudReportModel;
import de.hybris.platform.fraud.model.FraudSymptomScoringModel;
import de.hybris.platform.servicelayer.model.ModelService;
import de.hybris.platform.servicelayer.time.TimeService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.internal.util.reflection.Whitebox;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

@UnitTest
@RunWith(MockitoJUnitRunner.class)
public class DefaultNs8FraudServiceTest {

    private static final double SCORE_VALUE = 98.33d;
    private static final String SYMPTOM_VALUE = "symptom";
    private static final String CATEGORY_1_VALUE = "category1";
    private static final String DESCRIPTION_1_VALUE = "descritpion 1";
    private static final String ID_1_VALUE = "id1";
    private static final String TYPE_1_VALUE = "type 1";
    private static final String NS8_PROVIDER_NAME_VALUE = "NS8";
    private static final String ORDER_CODE = "order_code";
    private static final Date CURRENT_TIME = new Date();
    private static final String FRAUD_DESCRIPTION_VALUE = "Fraud description";
    private static final String EXPLANATION_VALUE = "explanation";

    @Spy
    @InjectMocks
    private DefaultNs8FraudService testObj;

    @Mock
    private ModelService modelServiceMock;
    @Mock
    private TimeService timeServiceMock;
    @Mock
    private FraudService fraudServiceMock;
    @Mock
    private Ns8FraudServiceResponse fraudServiceResponseMock;
    @Mock
    private Ns8FraudSymptom fraudSymptomMock;
    @Mock
    private Ns8FraudFactor factor1Mock;
    @Mock
    private OrderModel orderMock;
    @Mock
    private FraudReportModel fraudReportMock, oldFraudReportMock, newFraudReportMock;
    @Mock
    private FraudSymptomScoringModel fraudSymptomScoringMock;
    @Mock
    private Ns8FraudFactorModel fraudFactor1Mock, fraudFactor2Mock;
    private String providerNameMock;

    @Before
    public void setUp() {
        when(orderMock.getRiskEventPayload()).thenReturn("{payload : ''}");
        Whitebox.setInternalState(testObj, "modelService", modelServiceMock);
        Whitebox.setInternalState(testObj, "timeService", timeServiceMock);
        Whitebox.setInternalState(testObj, "providerName", providerNameMock);

        when(modelServiceMock.create(FraudReportModel.class)).thenReturn(new FraudReportModel());
        when(modelServiceMock.create(FraudSymptomScoringModel.class)).thenReturn(new FraudSymptomScoringModel());
        when(modelServiceMock.create(Ns8FraudFactorModel.class)).thenReturn(new Ns8FraudFactorModel());
        when(fraudServiceMock.recognizeOrderSymptoms(providerNameMock, orderMock)).thenReturn(fraudServiceResponseMock);
        when(fraudServiceResponseMock.getStatus()).thenReturn(Ns8FraudReportStatus.MERCHANT_REVIEW);
        when(fraudServiceResponseMock.getRisk()).thenReturn(Ns8FraudReportRisk.MEDIUM);
        when(fraudServiceResponseMock.getScore()).thenReturn(SCORE_VALUE);
        when(fraudServiceResponseMock.getDescription()).thenReturn(FRAUD_DESCRIPTION_VALUE);
        when(fraudServiceResponseMock.getNs8Symptoms()).thenReturn(Collections.singletonList(fraudSymptomMock));
        when(fraudSymptomMock.getScore()).thenReturn(SCORE_VALUE);
        when(fraudSymptomMock.getSymptom()).thenReturn(SYMPTOM_VALUE);
        when(fraudSymptomMock.getExplanation()).thenReturn(EXPLANATION_VALUE);
        when(fraudSymptomMock.getFactors()).thenReturn(Collections.singletonList(factor1Mock));
        when(factor1Mock.getCategory()).thenReturn(CATEGORY_1_VALUE);
        when(factor1Mock.getDescription()).thenReturn(DESCRIPTION_1_VALUE);
        when(factor1Mock.getId()).thenReturn(ID_1_VALUE);
        when(factor1Mock.getType()).thenReturn(TYPE_1_VALUE);
        when(oldFraudReportMock.getStatus()).thenReturn(FraudStatus.CHECK);
        when(orderMock.getCode()).thenReturn(ORDER_CODE);
        when(timeServiceMock.getCurrentTime()).thenReturn(CURRENT_TIME);
    }

    @Test(expected = IllegalArgumentException.class)
    public void hasOrderBeenScored_WhenOrderNull_ShouldThrowException() {
        testObj.hasOrderBeenScored(null);
    }

    @Test
    public void hasOrderBeenScored_WhenOrderHasPayload_ShouldReturnTrue() {
        final boolean result = testObj.hasOrderBeenScored(orderMock);

        assertThat(result).isTrue();
    }

    @Test
    public void hasOrderBeenScored_WhenOrderHasNotPayload_ShouldReturnFalse() {
        when(orderMock.getRiskEventPayload()).thenReturn(null);

        final boolean result = testObj.hasOrderBeenScored(orderMock);

        assertThat(result).isFalse();
    }

    @Test(expected = IllegalArgumentException.class)
    public void isOrderFraudChecked_WhenOrderNull_ShouldThrowException() {
        testObj.isOrderFraudChecked(null);
    }

    @Test
    public void isOrderFraudChecked_WhenOrderHasFraudReport_ShouldReturnTrue() {
        when(orderMock.getFraudReports()).thenReturn(new HashSet<>(Collections.singletonList(fraudReportMock)));

        final boolean result = testObj.isOrderFraudChecked(orderMock);

        assertThat(result).isTrue();
    }

    @Test
    public void isOrderFraudChecked_WhenOrderHasNotFraudReports_ShouldReturnFalse() {
        when(orderMock.getFraudReports()).thenReturn(Collections.emptySet());

        final boolean result = testObj.isOrderFraudChecked(orderMock);

        assertThat(result).isFalse();
    }

    @Test
    public void createFraudReport_ShouldCreateTheFraudReport() {
        doReturn(Collections.singletonList(fraudSymptomScoringMock)).when(testObj).createFraudSymptomScoringList(eq(fraudServiceResponseMock), any(FraudReportModel.class));

        final FraudReportModel result = testObj.createFraudReport(NS8_PROVIDER_NAME_VALUE, fraudServiceResponseMock, orderMock, FraudStatus.CHECK);

        assertThat(result.getCode()).isEqualTo(ORDER_CODE + "_FR" + 0);
        assertThat(result.getProvider()).isEqualTo(NS8_PROVIDER_NAME_VALUE);
        assertThat(result.getScore()).isEqualTo(SCORE_VALUE);
        assertThat(result.getTimestamp()).isEqualTo(CURRENT_TIME);
        assertThat(result.getOrder()).isEqualTo(orderMock);
        assertThat(result.getStatus()).isEqualTo(FraudStatus.CHECK);
        assertThat(result.getRisk()).isEqualTo(Ns8RiskType.MEDIUM);
        assertThat(result.getExplanation()).isEqualTo(FRAUD_DESCRIPTION_VALUE);
        assertThat(result.getFraudSymptomScorings()).isEqualTo(Collections.singletonList(fraudSymptomScoringMock));
    }

    @Test
    public void createFraudSymptomScoringList_ShouldCreateTheFraudSymptomScorings() {
        doReturn(Arrays.asList(fraudFactor1Mock, fraudFactor2Mock)).when(testObj).createNs8FraudFactors(any(), any());

        final List<FraudSymptomScoringModel> result = testObj.createFraudSymptomScoringList(fraudServiceResponseMock, fraudReportMock);

        assertThat(result.size()).isEqualTo(1);
        assertThat(result.get(0).getScore()).isEqualTo(SCORE_VALUE);
        assertThat(result.get(0).getExplanation()).isEqualTo(EXPLANATION_VALUE);
        assertThat(result.get(0).getName()).isEqualTo(SYMPTOM_VALUE);
        assertThat(result.get(0).getFraudReport()).isEqualTo(fraudReportMock);
        assertThat(result.get(0).getFraudFactors().size()).isEqualTo(2);
        assertThat(result.get(0).getFraudFactors().get(0)).isEqualTo(fraudFactor1Mock);
        assertThat(result.get(0).getFraudFactors().get(1)).isEqualTo(fraudFactor2Mock);
    }

    @Test
    public void createNs8FraudFactors_ShouldCreateTheFraudFactors() {
        final List<Ns8FraudFactorModel> result = testObj.createNs8FraudFactors(fraudSymptomMock, fraudSymptomScoringMock);

        assertThat(result.size()).isEqualTo(1);
        assertThat(result.get(0).getCategory()).isEqualTo(CATEGORY_1_VALUE);
        assertThat(result.get(0).getDescription()).isEqualTo(DESCRIPTION_1_VALUE);
        assertThat(result.get(0).getId()).isEqualTo(ID_1_VALUE);
        assertThat(result.get(0).getType()).isEqualTo(TYPE_1_VALUE);
        assertThat(result.get(0).getFraudSymptomScoring()).isEqualTo(fraudSymptomScoringMock);
    }

    @Test
    public void updateOrderFraudReport_ShouldUpdateTheOrderFraudReport() {
        when(orderMock.getFraudReports()).thenReturn(new HashSet<>(Collections.singletonList(oldFraudReportMock)));
        doReturn(newFraudReportMock).when(testObj).createFraudReport(providerNameMock, fraudServiceResponseMock, orderMock, FraudStatus.CHECK);

        testObj.updateOrderFraudReport(orderMock);

        verify(orderMock, times(2)).getFraudReports();
        verify(modelServiceMock).remove(oldFraudReportMock);
        verify(fraudServiceMock).recognizeOrderSymptoms(providerNameMock, orderMock);
        verify(testObj).createFraudReport(providerNameMock, fraudServiceResponseMock, orderMock, FraudStatus.CHECK);
        verify(modelServiceMock).saveAll(newFraudReportMock, orderMock);
    }

    @Test(expected = IllegalArgumentException.class)
    public void updateOrderFraudReport_WhenNoFraudReports_ShouldThrowException() {
        when(orderMock.getFraudReports()).thenReturn(Collections.emptySet());

        testObj.updateOrderFraudReport(orderMock);
    }
}
