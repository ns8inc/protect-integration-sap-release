package com.ns8.hybris.fulfilmentprocess.fraud.impl;

/**
 * NS8 fraud factor related to a {@link Ns8FraudSymptom}
 */
public class Ns8FraudFactor {

    private final String id;
    private final String type;
    private final String category;
    private final String description;

    public Ns8FraudFactor(final String id, final String type, final String category, final String description) {
        this.id = id;
        this.type = type;
        this.category = category;
        this.description = description;
    }

    public String getId() {
        return id;
    }

    public String getType() {
        return type;
    }

    public String getCategory() {
        return category;
    }

    public String getDescription() {
        return description;
    }
}
