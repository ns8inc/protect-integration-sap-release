package com.ns8.hybris.fulfilmentprocess.fraud.providers.impl;

import com.google.gson.Gson;
import com.ns8.hybris.fulfilmentprocess.enums.Ns8FraudReportRisk;
import com.ns8.hybris.fulfilmentprocess.enums.Ns8FraudReportStatus;
import com.ns8.hybris.fulfilmentprocess.fraud.impl.Ns8FraudFactor;
import com.ns8.hybris.fulfilmentprocess.fraud.impl.Ns8FraudServiceResponse;
import com.ns8.hybris.fulfilmentprocess.fraud.impl.Ns8FraudSymptom;
import de.hybris.bootstrap.annotations.UnitTest;
import de.hybris.platform.core.model.order.OrderModel;
import de.hybris.platform.core.model.user.UserModel;
import org.apache.commons.lang.NotImplementedException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.internal.util.reflection.Whitebox;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Map;

import static org.assertj.core.api.Java6Assertions.assertThat;
import static org.mockito.Mockito.when;

@UnitTest
@RunWith(MockitoJUnitRunner.class)
public class DefaultNs8FraudServiceProviderTest {

    private static final String DESCRIPTION_VALUE = "UPDATE_ORDER_RISK_EVENT";
    private static final String NS8_FRAUD_SERVICE_PROVIDER = "NS8_FraudServiceProvider";
    private static final double SYMPTOM_SCORE = 89.3d;
    private static final String SYMPTOM = "MIN_FRAUD";
    private static final String FACTOR_CATEGORY_1 = "PAYMENT";
    private static final String FACTOR_DESCRIPTION_1 = "The payment is rated as an extreme risk (more than a 50% chance of fraud)";
    private static final String FACTOR_TYPE_1 = "EXTREME_RISK_TRANSACTION";
    private static final String FACTOR_ID_1 = "d9e8ff67-962a-40c3-92db-d63c3d117a80";
    private static final String FACTOR_CATEGORY_2 = "EMAIL";
    private static final String FACTOR_DESCRIPTION_2 = "The customer's email address is from a free service";
    private static final String FACTOR_TYPE_2 = "EMAIL_FREE";
    private static final String FACTOR_ID_2 = "586f5e77-7c6c-4b8c-aeea-f7c2e531203e";

    @InjectMocks
    private DefaultNs8FraudServiceProvider testObj;

    @Mock
    private OrderModel orderMock;

    @Before
    public void setUp() {
        Whitebox.setInternalState(testObj, "providerName", NS8_FRAUD_SERVICE_PROVIDER);

        when(orderMock.getRiskEventPayload()).thenReturn(new Gson().toJson(createEventBody()));
    }

    @Test
    public void recognizeOrderFraudSymptoms_WhenValidEventBody_ShouldGetFraudServiceResponse() {
        final Ns8FraudServiceResponse response = (Ns8FraudServiceResponse) testObj.recognizeOrderFraudSymptoms(orderMock);

        assertThat(response.getRisk()).isEqualTo(Ns8FraudReportRisk.MEDIUM);
        assertThat(response.getScore()).isEqualTo(0d);
        assertThat(response.getStatus()).isEqualTo(Ns8FraudReportStatus.MERCHANT_REVIEW);
        assertThat(response.getDescription()).isEqualTo(DESCRIPTION_VALUE);
        assertThat(response.getProviderName()).isEqualTo(NS8_FRAUD_SERVICE_PROVIDER);

        assertThat(response.getNs8Symptoms().size()).isEqualTo(1);
        final Ns8FraudSymptom fraudSymptomResult = response.getNs8Symptoms().get(0);
        assertThat(fraudSymptomResult.getScore()).isEqualTo(SYMPTOM_SCORE);
        assertThat(fraudSymptomResult.getSymptom()).isEqualTo(SYMPTOM);
        assertThat(fraudSymptomResult.getFactors().size()).isEqualTo(6);

        final Ns8FraudFactor fraudFactorResult1 = fraudSymptomResult.getFactors().get(0);
        assertThat(fraudFactorResult1.getCategory()).isEqualTo(FACTOR_CATEGORY_1);
        assertThat(fraudFactorResult1.getId()).isEqualTo(FACTOR_ID_1);
        assertThat(fraudFactorResult1.getType()).isEqualTo(FACTOR_TYPE_1);
        assertThat(fraudFactorResult1.getDescription()).isEqualTo(FACTOR_DESCRIPTION_1);

        final Ns8FraudFactor fraudFactorResult2 = fraudSymptomResult.getFactors().get(1);
        assertThat(fraudFactorResult2.getCategory()).isEqualTo(FACTOR_CATEGORY_2);
        assertThat(fraudFactorResult2.getId()).isEqualTo(FACTOR_ID_2);
        assertThat(fraudFactorResult2.getType()).isEqualTo(FACTOR_TYPE_2);
        assertThat(fraudFactorResult2.getDescription()).isEqualTo(FACTOR_DESCRIPTION_2);
    }

    @Test
    public void recognizeOrderFraudSymptoms_WhenMapEmpty_ShouldReturnNull() {
        when(orderMock.getRiskEventPayload()).thenReturn("{}");

        final Ns8FraudServiceResponse response = (Ns8FraudServiceResponse) testObj.recognizeOrderFraudSymptoms(orderMock);

        assertThat(response).isNull();
    }

    @Test
    public void recognizeOrderFraudSymptoms_WhenRiskNotPresent_ShouldReturnNull() {
        final Map<String, Object> eventBody = createEventBody();
        eventBody.remove("risk");
        when(orderMock.getRiskEventPayload()).thenReturn(new Gson().toJson(eventBody));

        final Ns8FraudServiceResponse response = (Ns8FraudServiceResponse) testObj.recognizeOrderFraudSymptoms(orderMock);

        assertThat(response).isNull();
    }

    @Test
    public void recognizeOrderFraudSymptoms_WhenScoreNotPresent_ShouldReturnNull() {
        final Map<String, Object> eventBody = createEventBody();
        eventBody.remove("score");
        when(orderMock.getRiskEventPayload()).thenReturn(new Gson().toJson(eventBody));

        final Ns8FraudServiceResponse response = (Ns8FraudServiceResponse) testObj.recognizeOrderFraudSymptoms(orderMock);

        assertThat(response).isNull();
    }

    @Test(expected = NotImplementedException.class)
    public void recognizeUserActivitySymptoms_ShouldThrowException() {
        testObj.recognizeUserActivitySymptoms(new UserModel());
    }

    private Map<String, Object> createEventBody() {
        final StringBuilder eventBody = new StringBuilder();
        eventBody.append("{");
        eventBody.append("  \"action\": \"UPDATE_ORDER_RISK_EVENT\",");
        eventBody.append("  \"fraudData\": [");
        eventBody.append("    {");
        eventBody.append("      \"score\": 0,");
        eventBody.append("      \"grade\": \"F\",");
        eventBody.append("      \"providerRawResponse\": {");
        eventBody.append("        \"session\": {");
        eventBody.append("          \"id\": \"7642201762106114050\",");
        eventBody.append("          \"location\": {");
        eventBody.append("            \"continentCode\": \"EU\",");
        eventBody.append("            \"countryCode\": \"UK\",");
        eventBody.append("            \"countryName\": \"United Kingdom\",");
        eventBody.append("            \"latitude\": 51.5064,");
        eventBody.append("            \"longitude\": -0.02,");
        eventBody.append("            \"region\": \"ENG\",");
        eventBody.append("            \"regionName\": \"England\",");
        eventBody.append("            \"city\": \"Poplar\",");
        eventBody.append("            \"postalCode\": \"E14\"");
        eventBody.append("          }");
        eventBody.append("        }");
        eventBody.append("      },");
        eventBody.append("      \"providerType\": \"EQ8\",");
        eventBody.append("      \"factors\": [");
        eventBody.append("        {");
        eventBody.append("          \"category\": \"DEFAULT\",");
        eventBody.append("          \"type\": \"NONE\",");
        eventBody.append("          \"id\": \"e2988690-ee2c-4366-a7de-4b00b59fe969\",");
        eventBody.append("          \"description\": \"No issues found\"");
        eventBody.append("        }");
        eventBody.append("      ],");
        eventBody.append("      \"createdAt\": \"2020-05-04T09:11:06.483Z\",");
        eventBody.append("      \"id\": \"2ee3fac2-3724-477f-957f-8df54e3c7c2a\"");
        eventBody.append("    },");
        eventBody.append("    {");
        eventBody.append("      \"score\": 89.3,");
        eventBody.append("      \"providerType\": \"MIN_FRAUD\",");
        eventBody.append("      \"factors\": [");
        eventBody.append("        {");
        eventBody.append("          \"category\": \"PAYMENT\",");
        eventBody.append("          \"type\": \"EXTREME_RISK_TRANSACTION\",");
        eventBody.append("          \"id\": \"d9e8ff67-962a-40c3-92db-d63c3d117a80\",");
        eventBody.append("          \"description\": \"The payment is rated as an extreme risk (more than a 50% chance of fraud)\"");
        eventBody.append("        },");
        eventBody.append("        {");
        eventBody.append("          \"category\": \"EMAIL\",");
        eventBody.append("          \"type\": \"EMAIL_FREE\",");
        eventBody.append("          \"id\": \"586f5e77-7c6c-4b8c-aeea-f7c2e531203e\",");
        eventBody.append("          \"description\": \"The customer's email address is from a free service\"");
        eventBody.append("        },");
        eventBody.append("        {");
        eventBody.append("          \"category\": \"SHIPPING\",");
        eventBody.append("          \"type\": \"ADDRESS_IP_PROXIMITY\",");
        eventBody.append("          \"id\": \"2568a5c7-26f0-40a7-9be4-df0606ea395b\",");
        eventBody.append("          \"description\": \"Distance from shipping address to the device exceeds 100km\"");
        eventBody.append("        },");
        eventBody.append("        {");
        eventBody.append("          \"category\": \"SHIPPING\",");
        eventBody.append("          \"type\": \"ADDRESS_IP_COUNTRY_MISMATCH\",");
        eventBody.append("          \"id\": \"dfe2a327-6aa4-4b1e-977e-cd369df432bb\",");
        eventBody.append("          \"description\": \"Shipping address is not in the country of the I.P. address the transaction originated from\"");
        eventBody.append("        },");
        eventBody.append("        {");
        eventBody.append("          \"category\": \"BILLING\",");
        eventBody.append("          \"type\": \"ADDRESS_IP_PROXIMITY\",");
        eventBody.append("          \"id\": \"a622be6d-9251-47f7-aa38-8008b44b4161\",");
        eventBody.append("          \"description\": \"Distance from billing address to the device exceeds 100km\"");
        eventBody.append("        },");
        eventBody.append("        {");
        eventBody.append("          \"category\": \"BILLING\",");
        eventBody.append("          \"type\": \"ADDRESS_IP_COUNTRY_MISMATCH\",");
        eventBody.append("          \"id\": \"3229cd1e-5e6d-4e18-a65a-7a9057e3a04d\",");
        eventBody.append("          \"description\": \"Billing address is not in the country of the I.P. address the transaction originated from\"");
        eventBody.append("        }");
        eventBody.append("      ],");
        eventBody.append("      \"createdAt\": \"2020-05-04T09:11:06.483Z\",");
        eventBody.append("      \"id\": \"6bac666f-247c-40b3-9430-f3db696d631e\"");
        eventBody.append("    }");
        eventBody.append("  ],");
        eventBody.append("  \"orderId\": \"1944f15c-e3d9-47d8-96f1-af61e47d4955\",");
        eventBody.append("  \"platformStatus\": null,");
        eventBody.append("  \"risk\": \"MEDIUM\",");
        eventBody.append("  \"score\": \"0\",");
        eventBody.append("  \"status\": \"MERCHANT_REVIEW\"");
        eventBody.append("}");

        return new Gson().fromJson(eventBody.toString(), Map.class);
    }
}
