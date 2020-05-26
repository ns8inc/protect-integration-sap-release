package com.ns8.hybris.fulfilmentprocess.fraud.impl;

import com.ns8.hybris.fulfilmentprocess.enums.Ns8FraudReportRisk;
import com.ns8.hybris.fulfilmentprocess.enums.Ns8FraudReportStatus;
import de.hybris.platform.fraud.impl.FraudServiceResponse;

import java.util.List;

/**
 * Exended version of {@link FraudServiceResponse} to accommodate additional data from NS8
 */
public class Ns8FraudServiceResponse extends FraudServiceResponse {

    private final double score;
    private final Ns8FraudReportRisk risk;
    private final Ns8FraudReportStatus status;
    private List<Ns8FraudSymptom> ns8Symptoms;

    public Ns8FraudServiceResponse(final String providerName, final double score, final Ns8FraudReportStatus status, final Ns8FraudReportRisk risk,
                                   final String description, final String externalDescription, final List<Ns8FraudSymptom> ns8Symptoms) {
        super(description, providerName, externalDescription, null);
        this.score = score;
        this.status = status;
        this.risk = risk;
        this.ns8Symptoms = ns8Symptoms;
    }

    @Override
    public double getScore() {
        return score;
    }

    public Ns8FraudReportRisk getRisk() {
        return risk;
    }

    public Ns8FraudReportStatus getStatus() {
        return status;
    }

    public List<Ns8FraudSymptom> getNs8Symptoms() {
        return ns8Symptoms;
    }

    public void setNs8Symptoms(final List<Ns8FraudSymptom> ns8Symptoms) {
        this.ns8Symptoms = ns8Symptoms;
    }
}
