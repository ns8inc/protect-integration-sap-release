package com.ns8.hybris.fulfilmentprocess.actions.order;

import com.ns8.hybris.core.enums.Ns8RiskType;
import com.ns8.hybris.core.model.Ns8FraudFactorModel;
import com.ns8.hybris.fulfilmentprocess.fraud.impl.Ns8FraudFactor;
import com.ns8.hybris.fulfilmentprocess.fraud.impl.Ns8FraudServiceResponse;
import com.ns8.hybris.fulfilmentprocess.fraud.impl.Ns8FraudSymptom;
import de.hybris.platform.basecommerce.enums.FraudStatus;
import de.hybris.platform.core.model.order.OrderModel;
import de.hybris.platform.fraud.model.FraudReportModel;
import de.hybris.platform.fraud.model.FraudSymptomScoringModel;
import de.hybris.platform.orderprocessing.model.OrderProcessModel;
import org.apache.commons.collections4.CollectionUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Defines the logic to create the FraudReport information related to the given order
 */
public abstract class AbstractNs8FraudCheckAction<T extends OrderProcessModel> extends AbstractFraudCheckAction<T> {

    /**
     * Creates the FraudReport for given Ns8FraudServiceResponse, provider name, order and fraud status
     *
     * @param providerName the current provider name
     * @param response     the response with fraud data
     * @param order        the current order
     * @param status       the fraud status
     * @return the {@link FraudReportModel}
     */
    protected FraudReportModel createFraudReport(final String providerName, final Ns8FraudServiceResponse response,
                                                 final OrderModel order, final FraudStatus status) {
        final FraudReportModel fraudReport = modelService.create(FraudReportModel.class);
        fraudReport.setOrder(order);
        fraudReport.setStatus(status);
        fraudReport.setProvider(providerName);
        fraudReport.setTimestamp(timeService.getCurrentTime());
        fraudReport.setExplanation(response.getDescription());
        fraudReport.setRisk(Ns8RiskType.valueOf(response.getRisk().name()));
        fraudReport.setScore(response.getScore());

        int reportNumber = 0;
        if (CollectionUtils.isNotEmpty(order.getFraudReports())) {
            reportNumber = order.getFraudReports().size();
        }
        fraudReport.setCode(order.getCode() + "_FR" + reportNumber);
        fraudReport.setFraudSymptomScorings(createFraudSymptomScoringList(response, fraudReport));
        return fraudReport;
    }

    /**
     * Creates the list of FraudSymptomScoring based on the Ns8FraudServiceResponse
     *
     * @param response    the response with fraud data
     * @param fraudReport the related FraudReport
     * @return the list of {@link FraudSymptomScoringModel}
     */
    protected List<FraudSymptomScoringModel> createFraudSymptomScoringList(final Ns8FraudServiceResponse response, final FraudReportModel fraudReport) {
        List<FraudSymptomScoringModel> symptoms = null;
        for (final Ns8FraudSymptom symptom : response.getNs8Symptoms()) {
            if (symptoms == null) {
                symptoms = new ArrayList<>();
            }
            final FraudSymptomScoringModel symptomScoring = modelService.create(FraudSymptomScoringModel.class);
            symptomScoring.setFraudReport(fraudReport);
            symptomScoring.setName(symptom.getSymptom());
            symptomScoring.setExplanation(symptom.getExplanation());
            symptomScoring.setScore(symptom.getScore());

            if (CollectionUtils.isNotEmpty(symptom.getFactors())) {
                symptomScoring.setFraudFactors(createNs8FraudFactors(symptom, symptomScoring));
            }

            symptoms.add(symptomScoring);
        }
        return symptoms;
    }

    /**
     * Creates the list of Ns8FraudFactorModel based on the Ns8FraudSymptom
     *
     * @param symptom              the source Ns8FraudSymptom
     * @param parentSymptomScoring the related FraudSymptomScorin
     * @return the list of {@link Ns8FraudFactorModel}
     */
    protected List<Ns8FraudFactorModel> createNs8FraudFactors(final Ns8FraudSymptom symptom, final FraudSymptomScoringModel parentSymptomScoring) {
        List<Ns8FraudFactorModel> ns8FraudFactors = null;
        for (Ns8FraudFactor factor : symptom.getFactors()) {
            if (ns8FraudFactors == null) {
                ns8FraudFactors = new ArrayList<>();
            }
            final Ns8FraudFactorModel ns8FraudFactor = modelService.create(Ns8FraudFactorModel.class);
            ns8FraudFactor.setId(factor.getId());
            ns8FraudFactor.setType(factor.getType());
            ns8FraudFactor.setDescription(factor.getDescription());
            ns8FraudFactor.setCategory(factor.getCategory());
            ns8FraudFactor.setFraudSymptomScoring(parentSymptomScoring);
            ns8FraudFactors.add(ns8FraudFactor);
        }
        return ns8FraudFactors;
    }
}
