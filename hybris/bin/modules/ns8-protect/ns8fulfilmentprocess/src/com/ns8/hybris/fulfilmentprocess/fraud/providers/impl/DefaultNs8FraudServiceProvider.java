package com.ns8.hybris.fulfilmentprocess.fraud.providers.impl;

import com.google.gson.Gson;
import com.ns8.hybris.fulfilmentprocess.enums.Ns8FraudReportRisk;
import com.ns8.hybris.fulfilmentprocess.enums.Ns8FraudReportStatus;
import com.ns8.hybris.fulfilmentprocess.fraud.impl.Ns8FraudFactor;
import com.ns8.hybris.fulfilmentprocess.fraud.impl.Ns8FraudServiceResponse;
import com.ns8.hybris.fulfilmentprocess.fraud.impl.Ns8FraudSymptom;
import de.hybris.platform.core.model.order.AbstractOrderModel;
import de.hybris.platform.core.model.user.UserModel;
import de.hybris.platform.fraud.impl.AbstractFraudServiceProvider;
import de.hybris.platform.fraud.impl.FraudServiceResponse;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.NotImplementedException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Provides the NS8 Fraud data response based on the received event
 */
public class DefaultNs8FraudServiceProvider extends AbstractFraudServiceProvider {

    protected static final String RISK_BODY_KEY = "risk";
    protected static final String STATUS_BODY_KEY = "status";
    protected static final String SCORE_BODY_KEY = "score";
    protected static final String ACTION_BODY_KEY = "action";
    protected static final String FRAUD_DATA_BODY_KEY = "fraudData";
    protected static final String PROVIDER_FRAUD_DATA_TYPE_BODY_KEY = "providerType";
    protected static final String FACTORS_FRAUD_DATA_BODY_KEY = "factors";
    protected static final String ID_FACTOR_BODY_KEY = "id";
    protected static final String TYPE_FACTOR_BODY_KEY = "type";
    protected static final String CATEGORY_FACTOR_BODY_KEY = "category";
    protected static final String DESCRIPTION_FACTOR_BODY_KEY = "description";
    protected static final String EQ8_PROVIDER_TYPE = "EQ8";

    /**
     * {@inheritDoc}
     */
    @Override
    public FraudServiceResponse recognizeOrderFraudSymptoms(final AbstractOrderModel abstractOrder) {

        final String riskEventPayload = abstractOrder.getRiskEventPayload();
        final Map<String, Object> riskEventBodyMap = new Gson().fromJson(riskEventPayload, Map.class);

        if (MapUtils.isEmpty(riskEventBodyMap) || !riskEventBodyMap.containsKey(RISK_BODY_KEY) || !riskEventBodyMap.containsKey(SCORE_BODY_KEY)) {
            return null;
        }

        final String risk = (String) riskEventBodyMap.get(RISK_BODY_KEY);
        final String status = (String) riskEventBodyMap.get(STATUS_BODY_KEY);
        final double score = Double.parseDouble((String) riskEventBodyMap.get(SCORE_BODY_KEY));

        final List<Ns8FraudSymptom> symptomList = new ArrayList<>();
        if (riskEventBodyMap.containsKey(FRAUD_DATA_BODY_KEY)) {
            final List<Map<String, Object>> fraudDatas = (List<Map<String, Object>>) riskEventBodyMap.get(FRAUD_DATA_BODY_KEY);

            addFraudSymptoms(symptomList, fraudDatas);
        }

        return new Ns8FraudServiceResponse(this.getProviderName(), score, Ns8FraudReportStatus.valueOf(status), Ns8FraudReportRisk.valueOf(risk), (String) riskEventBodyMap.get(ACTION_BODY_KEY), null, symptomList);
    }

    /**
     * Adds the fraud symptoms for the fraud report response if the event body contains fraud datas
     *
     * @param symptomList the list to populate
     * @param fraudDatas  the list of fraud data
     */
    protected void addFraudSymptoms(final List<Ns8FraudSymptom> symptomList, final List<Map<String, Object>> fraudDatas) {
        for (Map<String, Object> fraudDataMap : fraudDatas) {
            if (fraudDataMap.containsKey(PROVIDER_FRAUD_DATA_TYPE_BODY_KEY) &&
                    !((String) fraudDataMap.get(PROVIDER_FRAUD_DATA_TYPE_BODY_KEY)).equalsIgnoreCase(EQ8_PROVIDER_TYPE)) {

                final Ns8FraudSymptom ns8FraudSymptom = new Ns8FraudSymptom((String) fraudDataMap.get(PROVIDER_FRAUD_DATA_TYPE_BODY_KEY), ((Double) fraudDataMap.get(SCORE_BODY_KEY)).doubleValue());

                if (fraudDataMap.containsKey(FACTORS_FRAUD_DATA_BODY_KEY)) {
                    final List<Map<String, Object>> factors = (List<Map<String, Object>>) fraudDataMap.get(FACTORS_FRAUD_DATA_BODY_KEY);

                    ns8FraudSymptom.setFactors(createFraudFactors(factors));
                }
                symptomList.add(ns8FraudSymptom);
            }
        }
    }

    /**
     * Creates the fraud factor list based on the ns8 event body
     *
     * @param factors the source factor list
     * @return the list of {@link Ns8FraudFactor}
     */
    protected List<Ns8FraudFactor> createFraudFactors(final List<Map<String, Object>> factors) {
        final List<Ns8FraudFactor> ns8FraudFactors = new ArrayList<>();
        for (Map<String, Object> factorDataMap : factors) {
            final Ns8FraudFactor fraudFactor = new Ns8FraudFactor((String) factorDataMap.get(ID_FACTOR_BODY_KEY), (String) factorDataMap.get(TYPE_FACTOR_BODY_KEY),
                    (String) factorDataMap.get(CATEGORY_FACTOR_BODY_KEY), (String) factorDataMap.get(DESCRIPTION_FACTOR_BODY_KEY));
            ns8FraudFactors.add(fraudFactor);
        }
        return ns8FraudFactors;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public FraudServiceResponse recognizeUserActivitySymptoms(final UserModel userModel) {
        throw new NotImplementedException(this.getClass());
    }
}
