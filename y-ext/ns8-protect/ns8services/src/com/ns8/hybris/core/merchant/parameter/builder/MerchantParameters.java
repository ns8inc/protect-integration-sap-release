package com.ns8.hybris.core.merchant.parameter.builder;

/**
 * Class to build the NS8 Merchant parameters.
 */
public final class MerchantParameters {

    private String email;
    private String storeUrl;
    private String merchantFirstName;
    private String merchantLastName;
    private String phone;
    private String apiKey;
    private String queueId;
    private boolean enabled;

    private MerchantParameters() {
        // empty constructor
    }

    public String getEmail() {
        return email;
    }

    public String getStoreUrl() {
        return storeUrl;
    }

    public String getMerchantFirstName() {
        return merchantFirstName;
    }

    public String getMerchantLastName() {
        return merchantLastName;
    }

    public String getPhone() {
        return phone;
    }

    public String getApiKey() {
        return apiKey;
    }

    public String getQueueId() {
        return queueId;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public interface ParamEmail {
        ParamStoreUrl withEmail(String shopper);
    }

    public interface ParamStoreUrl {
        ParamMerchantFirstName withStoreUrl(String storeUrl);
    }

    public interface ParamMerchantFirstName {
        ParamMerchantLastName withMerchantFirstName(String merchantFirstName);
    }

    public interface ParamMerchantLastName {
        ParamPhone withMerchantLastName(String merchantLastName);
    }

    public interface ParamPhone {
        MerchantParametersCreator withPhone(String phone);
    }

    public interface MerchantParametersCreator {
        MerchantParametersCreator withApiKey(String apiKey);

        MerchantParametersCreator withQueueId(String queueId);

        MerchantParametersCreator withEnabled(boolean enabled);

        MerchantParameters build();
    }

    public static class MerchantParametersBuilder implements ParamEmail, ParamStoreUrl, ParamMerchantFirstName, ParamMerchantLastName, ParamPhone, MerchantParametersCreator {

        private String email;
        private String storeUrl;
        private String merchantFirstName;
        private String merchantLastName;
        private String phone;
        private String apiKey;
        private String queueId;
        private boolean enabled;

        private MerchantParametersBuilder() {
        }

        /**
         * Getting the instance method
         *
         * @return
         */
        public static ParamEmail getInstance() {
            return new MerchantParametersBuilder();
        }

        @Override
        public ParamStoreUrl withEmail(final String email) {
            this.email = email;
            return this;
        }

        @Override
        public ParamMerchantFirstName withStoreUrl(final String storeUrl) {
            this.storeUrl = storeUrl;
            return this;
        }

        @Override
        public ParamMerchantLastName withMerchantFirstName(final String merchantFirstName) {
            this.merchantFirstName = merchantFirstName;
            return this;
        }

        @Override
        public ParamPhone withMerchantLastName(final String merchantLastName) {
            this.merchantLastName = merchantLastName;
            return this;
        }

        @Override
        public MerchantParametersCreator withPhone(final String phone) {
            this.phone = phone;
            return this;
        }

        @Override
        public MerchantParametersCreator withApiKey(final String apiKey) {
            this.apiKey = apiKey;
            return this;
        }

        @Override
        public MerchantParametersCreator withQueueId(final String queueId) {
            this.queueId = queueId;
            return this;
        }

        @Override
        public MerchantParametersCreator withEnabled(final boolean enabled) {
            this.enabled = enabled;
            return this;
        }

        @Override
        public MerchantParameters build() {
            final MerchantParameters parameters = new MerchantParameters();

            parameters.email = email;
            parameters.storeUrl = storeUrl;
            parameters.merchantFirstName = merchantFirstName;
            parameters.merchantLastName = merchantLastName;
            parameters.phone = phone;
            parameters.apiKey = apiKey;
            parameters.queueId = queueId;
            parameters.enabled = enabled;

            return parameters;
        }
    }
}
