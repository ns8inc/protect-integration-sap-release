package com.ns8.hybris.core.fraud.providers.impl;

import com.google.gson.Gson;
import com.ns8.hybris.core.fraud.impl.Ns8FraudFactor;
import com.ns8.hybris.core.fraud.impl.Ns8FraudServiceResponse;
import com.ns8.hybris.core.fraud.impl.Ns8FraudSymptom;
import com.ns8.hybris.fulfilmentprocess.enums.Ns8FraudReportRisk;
import com.ns8.hybris.fulfilmentprocess.enums.Ns8FraudReportStatus;
import de.hybris.platform.core.model.order.AbstractOrderModel;
import de.hybris.platform.core.model.user.UserModel;
import de.hybris.platform.fraud.impl.AbstractFraudServiceProvider;
import de.hybris.platform.fraud.impl.FraudServiceResponse;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.NotImplementedException;
import org.apache.commons.lang.StringUtils;

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
    protected static final String FRAUD_ASSESSTMENTS_BODY_KEY = "fraudAssessments";
    protected static final String AD_HOC_SCORE = "AD_HOC_SCORE";

    /**
     * {@inheritDoc}
     */
    @Override
    public FraudServiceResponse recognizeOrderFraudSymptoms(final AbstractOrderModel abstractOrder) {

        final String payload = StringUtils.isEmpty(abstractOrder.getRiskEventPayload()) ? abstractOrder.getNs8OrderPayload() : abstractOrder.getRiskEventPayload();
        final Map<String, Object> payloadBodyMap = new Gson().fromJson(payload, Map.class);

        if (MapUtils.isEmpty(payloadBodyMap) || (!payloadBodyMap.containsKey(RISK_BODY_KEY) || !payloadBodyMap.containsKey(SCORE_BODY_KEY) && (!payloadBodyMap.containsKey(FRAUD_ASSESSTMENTS_BODY_KEY) || getNs8Score(payloadBodyMap) == null))) {
            return null;
        }

        final String risk = (String) payloadBodyMap.get(RISK_BODY_KEY);
        final String status = (String) payloadBodyMap.get(STATUS_BODY_KEY);
        final Double score = payloadBodyMap.containsKey(SCORE_BODY_KEY) ? Double.parseDouble((String) payloadBodyMap.get(SCORE_BODY_KEY)) : getNs8Score(payloadBodyMap);
        final String action = payloadBodyMap.containsKey(ACTION_BODY_KEY) ? (String) payloadBodyMap.get(ACTION_BODY_KEY) : AD_HOC_SCORE;

        final List<Ns8FraudSymptom> symptomList = new ArrayList<>();
        final String fraudDataKey = payloadBodyMap.containsKey(FRAUD_DATA_BODY_KEY) ? FRAUD_DATA_BODY_KEY : FRAUD_ASSESSTMENTS_BODY_KEY;

        if (payloadBodyMap.containsKey(fraudDataKey)) {
            final List<Map<String, Object>> fraudDatas = (List<Map<String, Object>>) payloadBodyMap.get(fraudDataKey);

            addFraudSymptoms(symptomList, fraudDatas);
        }

        return new Ns8FraudServiceResponse(this.getProviderName(), score, Ns8FraudReportStatus.valueOf(status), Ns8FraudReportRisk.valueOf(risk), action, null, symptomList);
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
     * Obtains the Ns8 Score provided by EQ8
     *
     * @param ns8Payload the source factor list
     * @return Ns8 score provided by EQ8 as a double. Null if not exists.
     */
    protected Double getNs8Score(final Map<String, Object> ns8Payload) {
        if (!MapUtils.isEmpty(ns8Payload)) {
            final List<Map<String, Object>> fraudAssesstments = (List<Map<String, Object>>) ns8Payload.get(FRAUD_ASSESSTMENTS_BODY_KEY);
            if (CollectionUtils.isEmpty(fraudAssesstments)) {
                return null;
            } else {
                return (Double) fraudAssesstments.stream()
                        .filter(provider -> EQ8_PROVIDER_TYPE.equals(provider.get(PROVIDER_FRAUD_DATA_TYPE_BODY_KEY)))
                        .findFirst()
                        .get()
                        .get(SCORE_BODY_KEY);
            }
        }
        return null;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public FraudServiceResponse recognizeUserActivitySymptoms(final UserModel userModel) {
        throw new NotImplementedException(this.getClass());
    }
}
