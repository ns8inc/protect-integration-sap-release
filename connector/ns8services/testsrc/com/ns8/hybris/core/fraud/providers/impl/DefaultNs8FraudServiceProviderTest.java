package com.ns8.hybris.core.fraud.providers.impl;


import com.google.gson.Gson;
import com.ns8.hybris.core.fraud.impl.Ns8FraudFactor;
import com.ns8.hybris.core.fraud.impl.Ns8FraudServiceResponse;
import com.ns8.hybris.core.fraud.impl.Ns8FraudSymptom;
import com.ns8.hybris.fulfilmentprocess.enums.Ns8FraudReportRisk;
import com.ns8.hybris.fulfilmentprocess.enums.Ns8FraudReportStatus;
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

import static org.assertj.core.api.Assertions.assertThat;
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
    private static final String AD_HOC_SCORE_ACTION = "AD_HOC_SCORE";

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
        assertThat(fraudSymptomResult.getFactors()).hasSize(6);

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
    public void recognizeOrderFraudSymptoms_WhenValidEventBodyIsEmptyButNs8PayloadIsNot_ShouldGetFraudServiceResponse() {
        when(orderMock.getRiskEventPayload()).thenReturn(null);
        when(orderMock.getNs8OrderPayload()).thenReturn(new Gson().toJson(createNs8Body()));
        final Ns8FraudServiceResponse response = (Ns8FraudServiceResponse) testObj.recognizeOrderFraudSymptoms(orderMock);

        assertThat(response.getRisk()).isEqualTo(Ns8FraudReportRisk.HIGH);
        assertThat(response.getScore()).isEqualTo(14d);
        assertThat(response.getStatus()).isEqualTo(Ns8FraudReportStatus.APPROVED);
        assertThat(response.getDescription()).isEqualTo(AD_HOC_SCORE_ACTION);
        assertThat(response.getProviderName()).isEqualTo(NS8_FRAUD_SERVICE_PROVIDER);

        assertThat(response.getNs8Symptoms()).hasSize(1);
        final Ns8FraudSymptom fraudSymptomResult = response.getNs8Symptoms().get(0);
        assertThat(fraudSymptomResult.getScore()).isEqualTo(SYMPTOM_SCORE);
        assertThat(fraudSymptomResult.getSymptom()).isEqualTo(SYMPTOM);
        assertThat(fraudSymptomResult.getFactors().size()).isEqualTo(1);

        final Ns8FraudFactor fraudFactorResult1 = fraudSymptomResult.getFactors().get(0);
        assertThat(fraudFactorResult1.getCategory()).isEqualTo(FACTOR_CATEGORY_1);
        assertThat(fraudFactorResult1.getId()).isEqualTo(FACTOR_ID_1);
        assertThat(fraudFactorResult1.getType()).isEqualTo(FACTOR_TYPE_1);
        assertThat(fraudFactorResult1.getDescription()).isEqualTo(FACTOR_DESCRIPTION_1);
    }

    @Test
    public void recognizeOrderFraudSymptoms_WhenValidEventBodyIsEmptyButNs8PayloadHasFraudAssestmentEmpty_ShouldGetFraudServiceResponse() {
        when(orderMock.getRiskEventPayload()).thenReturn(null);
        when(orderMock.getNs8OrderPayload()).thenReturn(new Gson().toJson(createNs8BodyWithoutFraudAssestments()));
        var response = testObj.recognizeOrderFraudSymptoms(orderMock);

        assertThat(response).isNull();
    }

    @Test
    public void recognizeOrderFraudSymptoms_WhenMapEmpty_ShouldReturnNull() {
        when(orderMock.getRiskEventPayload()).thenReturn("{}");

        var response = testObj.recognizeOrderFraudSymptoms(orderMock);

        assertThat(response).isNull();
    }

    @Test
    public void recognizeOrderFraudSymptoms_WhenRiskNotPresent_ShouldReturnNull() {
        final Map<String, Object> eventBody = createEventBody();
        eventBody.remove("risk");
        when(orderMock.getRiskEventPayload()).thenReturn(new Gson().toJson(eventBody));

        var response = testObj.recognizeOrderFraudSymptoms(orderMock);

        assertThat(response).isNull();
    }

    @Test
    public void recognizeOrderFraudSymptoms_WhenScoreNotPresent_ShouldReturnNull() {
        final Map<String, Object> eventBody = createEventBody();
        eventBody.remove("score");
        when(orderMock.getRiskEventPayload()).thenReturn(new Gson().toJson(eventBody));

        var response = testObj.recognizeOrderFraudSymptoms(orderMock);

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

    private Map<String, Object> createNs8Body() {
        final StringBuilder eventBody = new StringBuilder();
        eventBody.append("{");
        eventBody.append("    \"status\": \"APPROVED\",");
        eventBody.append("    \"createdAt\": \"2020-06-05T11:22:58.816Z\",");
        eventBody.append("    \"updatedAt\": \"2020-06-05T11:23:03.321Z\",");
        eventBody.append("    \"id\": \"2913ddfe-4205-4946-a0ee-81cef582a97f\",");
        eventBody.append("    \"merchantId\": \"7e6d4cf9-00dc-4554-9ffb-8ac2c4ba953b\",");
        eventBody.append("    \"verificationHistory\": null,");
        eventBody.append("    \"platformId\": \"77b9717f-bafb-4093-845c-f773b4fdabf1\",");
        eventBody.append("    \"name\": \"77b9717f-bafb-4093-845c-f773b4fdabf1\",");
        eventBody.append("    \"platformCreatedAt\": \"2020-06-05T11:22:40.222Z\",");
        eventBody.append("    \"currency\": \"USD\",");
        eventBody.append("    \"totalPrice\": 193.57,");
        eventBody.append("    \"risk\": \"HIGH\",");
        eventBody.append("    \"hasGiftCard\": false,");
        eventBody.append("       \"platformStatus\": null,");
        eventBody.append("       \"addresses\":");
        eventBody.append("       [");
        eventBody.append("           {  ");
        eventBody.append("               \"id\": \"459cd372-4975-4fd8-9a34-90ac9eb8a714\",");
        eventBody.append("               \"type\": \"SHIPPING\",");
        eventBody.append("               \"name\": \"Charlie Brown\",");
        eventBody.append("               \"company\": null,");
        eventBody.append("               \"address1\": \"Calle hermosita\", ");
        eventBody.append("               \"address2\": null,");
        eventBody.append("               \"city\": \"Valencia\",");
        eventBody.append("               \"zip\": \"46001\",");
        eventBody.append("               \"region\": null,");
        eventBody.append("               \"regionCode\": null,");
        eventBody.append("               \"country\": \"Spain\",");
        eventBody.append("               \"countryCode\": \"ES\",");
        eventBody.append("               \"latitude\": 39.474176,");
        eventBody.append("               \"longitude\": -0.380721");
        eventBody.append("           },");
        eventBody.append("           {  ");
        eventBody.append("               \"id\": \"a52dccc8-2122-4008-a117-87bdf5ae0a23\", ");
        eventBody.append("               \"type\": \"DEVICE\",");
        eventBody.append("               \"name\": null,");
        eventBody.append("               \"company\": null,");
        eventBody.append("               \"address1\": null,");
        eventBody.append("               \"address2\": null,");
        eventBody.append("               \"city\": \"Vilamarxant\",");
        eventBody.append("               \"zip\": \"46191\",");
        eventBody.append("               \"region\": \"Valencia\",");
        eventBody.append("               \"regionCode\": \"VC\",");
        eventBody.append("               \"country\": \"Spain\",");
        eventBody.append("               \"countryCode\": \"ES\",");
        eventBody.append("               \"latitude\": 39.5667,");
        eventBody.append("               \"longitude\": -0.6167");
        eventBody.append("           },");
        eventBody.append("           {  ");
        eventBody.append("               \"id\": \"ecb824c1-ab54-4f73-976f-f376dc325d8f\",");
        eventBody.append("               \"type\": \"BILLING\",");
        eventBody.append("               \"name\": \"Charlie Brown\",");
        eventBody.append("               \"company\": null,");
        eventBody.append("               \"address1\": \"Calle hermosita\",");
        eventBody.append("               \"address2\": null,");
        eventBody.append("               \"city\": \"Valencia\",");
        eventBody.append("               \"zip\": \"46001\",");
        eventBody.append("               \"region\": null,");
        eventBody.append("               \"regionCode\": null,");
        eventBody.append("               \"country\": \"Spain\",");
        eventBody.append("               \"countryCode\": \"ES\",");
        eventBody.append("               \"latitude\": 39.474176,");
        eventBody.append("               \"longitude\": -0.380721 ");
        eventBody.append("          }");
        eventBody.append("       ],");
        eventBody.append("      \"customer\":");
        eventBody.append("      { ");
        eventBody.append("          \"id\": \"d59b589b-013f-4823-957f-998545c0abb1\",");
        eventBody.append("          \"firstName\": \"Raul\",");
        eventBody.append("          \"lastName\": \"Gutierrez\",");
        eventBody.append("          \"email\": \"raul.gutierrez@e2ycommerce.com\",");
        eventBody.append("          \"platformId\": \"raul.gutierrez@e2ycommerce.com\",");
        eventBody.append("          \"platformCreatedAt\": \"2020-06-04T09:11:44.850Z\",");
        eventBody.append("          \"phone\": null,");
        eventBody.append("          \"gender\": \"U\",");
        eventBody.append("          \"birthday\": null,");
        eventBody.append("          \"company\": null,");
        eventBody.append("          \"totalSpent\": null,");
        eventBody.append("          \"isEmailVerified\": null,");
        eventBody.append("          \"isPayingCustomer\": null");
        eventBody.append("      },");
        eventBody.append("      \"fraudAssessments\":");
        eventBody.append("      [ ");
        eventBody.append("        {  ");
        eventBody.append("          \"createdAt\": \"2020-06-05T11:23:02.286Z\",");
        eventBody.append("          \"updatedAt\": null,");
        eventBody.append("          \"id\": \"23f98e74-4322-457a-a4ee-15bdac4ffafc\",");
        eventBody.append("          \"providerType\": \"EQ8\",");
        eventBody.append("          \"score\": 14, ");
        eventBody.append("          \"grade\": \"F\", ");
        eventBody.append("          \"providerRawResponse\":");
        eventBody.append("          {");
        eventBody.append("               \"session\":");
        eventBody.append("                 {");
        eventBody.append("                    \"id\": \"569026223869722631\",");
        eventBody.append("                    \"location\":");
        eventBody.append("                     {");
        eventBody.append("                        \"continentCode\": \"EU\", ");
        eventBody.append("                        \"countryCode\": \"ES\",");
        eventBody.append("                        \"countryName\": \"Spain\",");
        eventBody.append("                        \"latitude\": 39.5667,");
        eventBody.append("                        \"longitude\": -0.6167,");
        eventBody.append("                        \"region\": \"VC\",");
        eventBody.append("                        \"regionName\": \"Valencia\",");
        eventBody.append("                        \"city\": \"Vilamarxant\",");
        eventBody.append("                        \"postalCode\": \"46191\"");
        eventBody.append("                     }");
        eventBody.append("                 }");
        eventBody.append("            },");
        eventBody.append("            \"factors\":");
        eventBody.append("              [  ");
        eventBody.append("                {  ");
        eventBody.append("                  \"category\": \"DEFAULT\",");
        eventBody.append("                  \"id\": \"29b0f4c7-bd67-47b3-80f7-f14064d5e1b7\",");
        eventBody.append("                  \"type\": \"NONE\",");
        eventBody.append("                  \"description\": \"No issues found\"");
        eventBody.append("                }");
        eventBody.append("              ]");
        eventBody.append("        },  ");
        eventBody.append("        {  ");
        eventBody.append("          \"createdAt\": \"2020-06-05T11:23:02.286Z\",");
        eventBody.append("          \"updatedAt\": null,");
        eventBody.append("          \"id\": \"bbf8c60f-913d-40dd-a03c-3088a583e0f9\",");
        eventBody.append("          \"providerType\": \"MIN_FRAUD\",");
        eventBody.append("          \"score\": 89.3,");
        eventBody.append("          \"grade\": null,");
        eventBody.append("          \"providerRawResponse\": null,");
        eventBody.append("          \"factors\":");
        eventBody.append("            [  ");
        eventBody.append("              {  ");
        eventBody.append("                 \"category\": \"PAYMENT\",");
        eventBody.append("                 \"id\": \"d9e8ff67-962a-40c3-92db-d63c3d117a80\",");
        eventBody.append("                 \"type\": \"EXTREME_RISK_TRANSACTION\",");
        eventBody.append("                 \"description\": \"The payment is rated as an extreme risk (more than a 50% chance of fraud)\"");
        eventBody.append("              }");
        eventBody.append("            ]");
        eventBody.append("        }");
        eventBody.append("      ],");
        eventBody.append("      \"customerVerification\": null,  ");
        eventBody.append("      \"transactions\": [  ");
        eventBody.append("           {  ");
        eventBody.append("               \"id\": \"1c1f6936-b561-4e91-aa79-7151deb1e02e\",");
        eventBody.append("               \"amount\": 193.57,");
        eventBody.append("               \"currency\": \"USD\",");
        eventBody.append("               \"status\": \"SUCCESS\",");
        eventBody.append("               \"statusDetails\": \"SUCCESFULL\",");
        eventBody.append("               \"platformId\": \"raul.gutierrez@e2ycommerce.com-da29fbba-45fd-4edb-9afe-f25a65157ac3\",");
        eventBody.append("               \"method\": \"CC\",");
        eventBody.append("               \"processedAt\": \"2020-06-05T11:22:40.116Z\",");
        eventBody.append("               \"creditCard\": {  ");
        eventBody.append("                   \"id\": \"41628187-fba9-4407-a980-09919eb5e486\",");
        eventBody.append("                   \"transactionType\": \"AUTHORIZATION\",");
        eventBody.append("                   \"creditCardNumber\": \"************5678\",");
        eventBody.append("                   \"creditCardCompany\": \"VISA\",");
        eventBody.append("                   \"cardExpiration\": \"3/2023\",");
        eventBody.append("                   \"cardHolder\": \"Charlie Brown\",");
        eventBody.append("                   \"avsResultCode\": null,");
        eventBody.append("                   \"cvvResultCode\": null, ");
        eventBody.append("                   \"creditCardBin\": null,");
        eventBody.append("                   \"gateway\": null  ");
        eventBody.append("               }  ");
        eventBody.append("           }  ");
        eventBody.append("       ],  ");
        eventBody.append("       \"lineItems\": [  ");
        eventBody.append("           {  ");
        eventBody.append("               \"id\": \"1827501b-4587-425d-947f-f025f3a21a95\",  ");
        eventBody.append("               \"name\": \"InfoLITHIUM™ H Series Battery\",");
        eventBody.append("               \"quantity\": 1,");
        eventBody.append("               \"price\": 184.58,");
        eventBody.append("               \"platformId\": \"77b9717f-bafb-4093-845c-f773b4fdabf1-0\",");
        eventBody.append("               \"title\": \"-Accessory value kit for Handycam models using H or P series batteries. <br/>-An affordable ready made solution that includes the essential accessories for every camcorder user. <br/>-Carrying case f\",");
        eventBody.append("               \"sku\": \"NP-FH70\",");
        eventBody.append("               \"isbn\": null,");
        eventBody.append("               \"ean13\": \"0490552438251\",");
        eventBody.append("               \"upc\": null,");
        eventBody.append("               \"variantId\": null,");
        eventBody.append("               \"variantTitle\": null,");
        eventBody.append("               \"vendor\": null,");
        eventBody.append("               \"platformProductId\": \"861175\",");
        eventBody.append("               \"isGiftCard\": null,");
        eventBody.append("               \"totalDiscount\": null,");
        eventBody.append("               \"manufacturer\": \"Sony\"");
        eventBody.append("           }  ");
        eventBody.append("       ]  ");
        eventBody.append("  }  ");


        return new Gson().fromJson(eventBody.toString(), Map.class);
    }

    private Map<String, Object> createNs8BodyWithoutFraudAssestments() {
        final StringBuilder eventBody = new StringBuilder();
        eventBody.append("{");
        eventBody.append("    \"status\": \"APPROVED\",");
        eventBody.append("    \"createdAt\": \"2020-06-05T11:22:58.816Z\",");
        eventBody.append("    \"updatedAt\": \"2020-06-05T11:23:03.321Z\",");
        eventBody.append("    \"id\": \"2913ddfe-4205-4946-a0ee-81cef582a97f\",");
        eventBody.append("    \"merchantId\": \"7e6d4cf9-00dc-4554-9ffb-8ac2c4ba953b\",");
        eventBody.append("    \"verificationHistory\": null,");
        eventBody.append("    \"platformId\": \"77b9717f-bafb-4093-845c-f773b4fdabf1\",");
        eventBody.append("    \"name\": \"77b9717f-bafb-4093-845c-f773b4fdabf1\",");
        eventBody.append("    \"platformCreatedAt\": \"2020-06-05T11:22:40.222Z\",");
        eventBody.append("    \"currency\": \"USD\",");
        eventBody.append("    \"totalPrice\": 193.57,");
        eventBody.append("    \"risk\": \"HIGH\",");
        eventBody.append("    \"hasGiftCard\": false,");
        eventBody.append("       \"platformStatus\": null,");
        eventBody.append("       \"addresses\":");
        eventBody.append("       [");
        eventBody.append("           {  ");
        eventBody.append("               \"id\": \"459cd372-4975-4fd8-9a34-90ac9eb8a714\",");
        eventBody.append("               \"type\": \"SHIPPING\",");
        eventBody.append("               \"name\": \"Charlie Brown\",");
        eventBody.append("               \"company\": null,");
        eventBody.append("               \"address1\": \"Calle hermosita\", ");
        eventBody.append("               \"address2\": null,");
        eventBody.append("               \"city\": \"Valencia\",");
        eventBody.append("               \"zip\": \"46001\",");
        eventBody.append("               \"region\": null,");
        eventBody.append("               \"regionCode\": null,");
        eventBody.append("               \"country\": \"Spain\",");
        eventBody.append("               \"countryCode\": \"ES\",");
        eventBody.append("               \"latitude\": 39.474176,");
        eventBody.append("               \"longitude\": -0.380721");
        eventBody.append("           },");
        eventBody.append("           {  ");
        eventBody.append("               \"id\": \"a52dccc8-2122-4008-a117-87bdf5ae0a23\", ");
        eventBody.append("               \"type\": \"DEVICE\",");
        eventBody.append("               \"name\": null,");
        eventBody.append("               \"company\": null,");
        eventBody.append("               \"address1\": null,");
        eventBody.append("               \"address2\": null,");
        eventBody.append("               \"city\": \"Vilamarxant\",");
        eventBody.append("               \"zip\": \"46191\",");
        eventBody.append("               \"region\": \"Valencia\",");
        eventBody.append("               \"regionCode\": \"VC\",");
        eventBody.append("               \"country\": \"Spain\",");
        eventBody.append("               \"countryCode\": \"ES\",");
        eventBody.append("               \"latitude\": 39.5667,");
        eventBody.append("               \"longitude\": -0.6167");
        eventBody.append("           },");
        eventBody.append("           {  ");
        eventBody.append("               \"id\": \"ecb824c1-ab54-4f73-976f-f376dc325d8f\",");
        eventBody.append("               \"type\": \"BILLING\",");
        eventBody.append("               \"name\": \"Charlie Brown\",");
        eventBody.append("               \"company\": null,");
        eventBody.append("               \"address1\": \"Calle hermosita\",");
        eventBody.append("               \"address2\": null,");
        eventBody.append("               \"city\": \"Valencia\",");
        eventBody.append("               \"zip\": \"46001\",");
        eventBody.append("               \"region\": null,");
        eventBody.append("               \"regionCode\": null,");
        eventBody.append("               \"country\": \"Spain\",");
        eventBody.append("               \"countryCode\": \"ES\",");
        eventBody.append("               \"latitude\": 39.474176,");
        eventBody.append("               \"longitude\": -0.380721 ");
        eventBody.append("          }");
        eventBody.append("       ],");
        eventBody.append("      \"customer\":");
        eventBody.append("      { ");
        eventBody.append("          \"id\": \"d59b589b-013f-4823-957f-998545c0abb1\",");
        eventBody.append("          \"firstName\": \"Raul\",");
        eventBody.append("          \"lastName\": \"Gutierrez\",");
        eventBody.append("          \"email\": \"raul.gutierrez@e2ycommerce.com\",");
        eventBody.append("          \"platformId\": \"raul.gutierrez@e2ycommerce.com\",");
        eventBody.append("          \"platformCreatedAt\": \"2020-06-04T09:11:44.850Z\",");
        eventBody.append("          \"phone\": null,");
        eventBody.append("          \"gender\": \"U\",");
        eventBody.append("          \"birthday\": null,");
        eventBody.append("          \"company\": null,");
        eventBody.append("          \"totalSpent\": null,");
        eventBody.append("          \"isEmailVerified\": null,");
        eventBody.append("          \"isPayingCustomer\": null");
        eventBody.append("      },");
        eventBody.append("      \"fraudAssessments\":");
        eventBody.append("      [ ");
        eventBody.append("      ],");
        eventBody.append("      \"customerVerification\": null,  ");
        eventBody.append("      \"transactions\": [  ");
        eventBody.append("           {  ");
        eventBody.append("               \"id\": \"1c1f6936-b561-4e91-aa79-7151deb1e02e\",");
        eventBody.append("               \"amount\": 193.57,");
        eventBody.append("               \"currency\": \"USD\",");
        eventBody.append("               \"status\": \"SUCCESS\",");
        eventBody.append("               \"statusDetails\": \"SUCCESFULL\",");
        eventBody.append("               \"platformId\": \"raul.gutierrez@e2ycommerce.com-da29fbba-45fd-4edb-9afe-f25a65157ac3\",");
        eventBody.append("               \"method\": \"CC\",");
        eventBody.append("               \"processedAt\": \"2020-06-05T11:22:40.116Z\",");
        eventBody.append("               \"creditCard\": {  ");
        eventBody.append("                   \"id\": \"41628187-fba9-4407-a980-09919eb5e486\",");
        eventBody.append("                   \"transactionType\": \"AUTHORIZATION\",");
        eventBody.append("                   \"creditCardNumber\": \"************5678\",");
        eventBody.append("                   \"creditCardCompany\": \"VISA\",");
        eventBody.append("                   \"cardExpiration\": \"3/2023\",");
        eventBody.append("                   \"cardHolder\": \"Charlie Brown\",");
        eventBody.append("                   \"avsResultCode\": null,");
        eventBody.append("                   \"cvvResultCode\": null, ");
        eventBody.append("                   \"creditCardBin\": null,");
        eventBody.append("                   \"gateway\": null  ");
        eventBody.append("               }  ");
        eventBody.append("           }  ");
        eventBody.append("       ],  ");
        eventBody.append("       \"lineItems\": [  ");
        eventBody.append("           {  ");
        eventBody.append("               \"id\": \"1827501b-4587-425d-947f-f025f3a21a95\",  ");
        eventBody.append("               \"name\": \"InfoLITHIUM™ H Series Battery\",");
        eventBody.append("               \"quantity\": 1,");
        eventBody.append("               \"price\": 184.58,");
        eventBody.append("               \"platformId\": \"77b9717f-bafb-4093-845c-f773b4fdabf1-0\",");
        eventBody.append("               \"title\": \"-Accessory value kit for Handycam models using H or P series batteries. <br/>-An affordable ready made solution that includes the essential accessories for every camcorder user. <br/>-Carrying case f\",");
        eventBody.append("               \"sku\": \"NP-FH70\",");
        eventBody.append("               \"isbn\": null,");
        eventBody.append("               \"ean13\": \"0490552438251\",");
        eventBody.append("               \"upc\": null,");
        eventBody.append("               \"variantId\": null,");
        eventBody.append("               \"variantTitle\": null,");
        eventBody.append("               \"vendor\": null,");
        eventBody.append("               \"platformProductId\": \"861175\",");
        eventBody.append("               \"isGiftCard\": null,");
        eventBody.append("               \"totalDiscount\": null,");
        eventBody.append("               \"manufacturer\": \"Sony\"");
        eventBody.append("           }  ");
        eventBody.append("       ]  ");
        eventBody.append("  }  ");


        return new Gson().fromJson(eventBody.toString(), Map.class);
    }
}
