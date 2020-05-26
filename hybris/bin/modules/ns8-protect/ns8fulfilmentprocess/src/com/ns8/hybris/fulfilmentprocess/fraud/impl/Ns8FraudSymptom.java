package com.ns8.hybris.fulfilmentprocess.fraud.impl;

import de.hybris.platform.fraud.impl.FraudSymptom;

import java.util.List;

/**
 * Extended version of {@link FraudSymptom} o accommodate additional data from NS8
 */
public class Ns8FraudSymptom extends FraudSymptom {

    private List<Ns8FraudFactor> factors;

    public Ns8FraudSymptom(final String symptom, final double score) {
        super(symptom, score);
    }

    public List<Ns8FraudFactor> getFactors() {
        return factors;
    }

    public void setFactors(final List<Ns8FraudFactor> factors) {
        this.factors = factors;
    }

}
