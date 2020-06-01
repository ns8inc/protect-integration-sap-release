package com.ns8.hybris.core.fraud.services.impl;

import com.google.common.base.Preconditions;
import com.ns8.hybris.core.enums.Ns8RiskType;
import com.ns8.hybris.core.fraud.impl.Ns8FraudFactor;
import com.ns8.hybris.core.fraud.impl.Ns8FraudServiceResponse;
import com.ns8.hybris.core.fraud.impl.Ns8FraudSymptom;
import com.ns8.hybris.core.fraud.services.Ns8FraudService;
import com.ns8.hybris.core.model.Ns8FraudFactorModel;
import de.hybris.platform.basecommerce.enums.FraudStatus;
import de.hybris.platform.core.model.order.OrderModel;
import de.hybris.platform.fraud.FraudService;
import de.hybris.platform.fraud.model.FraudReportModel;
import de.hybris.platform.fraud.model.FraudSymptomScoringModel;
import de.hybris.platform.servicelayer.model.ModelService;
import de.hybris.platform.servicelayer.time.TimeService;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

import static de.hybris.platform.servicelayer.util.ServicesUtil.validateParameterNotNull;

/**
 * Default implementation of {@link Ns8FraudService}
 */
public class DefaultNs8FraudService implements Ns8FraudService {

    protected final ModelService modelService;
    protected final TimeService timeService;
    protected final FraudService fraudService;
    protected final String providerName;

    public DefaultNs8FraudService(final ModelService modelService,
                                  final TimeService timeService,
                                  final FraudService fraudService,
                                  final String providerName) {
        this.modelService = modelService;
        this.timeService = timeService;
        this.fraudService = fraudService;
        this.providerName = providerName;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean hasOrderBeenScored(final OrderModel order) {
        validateParameterNotNull(order, "Order cannot be null");

        return StringUtils.isNotBlank(order.getRiskEventPayload());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isOrderFraudChecked(final OrderModel order) {
        validateParameterNotNull(order, "Order cannot be null");

        return CollectionUtils.isNotEmpty(order.getFraudReports());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public FraudReportModel createFraudReport(final String providerName, final Ns8FraudServiceResponse response,
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

    /**
     * {@inheritDoc}
     */
    @Override
    public void updateOrderFraudReport(final OrderModel order) {
        Preconditions.checkArgument(CollectionUtils.isNotEmpty(order.getFraudReports()), "The order Fraud Report list cannot be empty for an update.");

        final FraudReportModel oldFraudReport = order.getFraudReports().stream().findFirst().get();
        final FraudStatus fraudStatus = oldFraudReport.getStatus();
        modelService.remove(oldFraudReport);
        final Ns8FraudServiceResponse response = (Ns8FraudServiceResponse) fraudService.recognizeOrderSymptoms(providerName, order);
        final FraudReportModel updatedFraudReport = createFraudReport(providerName, response, order, fraudStatus);
        modelService.saveAll(updatedFraudReport, order);
    }
}
