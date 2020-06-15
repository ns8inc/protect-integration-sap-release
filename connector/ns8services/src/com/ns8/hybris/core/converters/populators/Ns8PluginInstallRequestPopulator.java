package com.ns8.hybris.core.converters.populators;

import com.ns8.hybris.core.data.Ns8PluginInstallRequest;
import com.ns8.hybris.core.model.NS8MerchantModel;
import de.hybris.platform.converters.Populator;
import de.hybris.platform.servicelayer.config.ConfigurationService;

/**
 * Populates the information of the {@link NS8MerchantModel} into a {@link Ns8PluginInstallRequest}
 */
public class Ns8PluginInstallRequestPopulator implements Populator<NS8MerchantModel, Ns8PluginInstallRequest> {

    protected static final String NS_8_SERVICES_CONNECTOR_VERSION_CONFIG_KEY = "ns8services.connector.version";
    protected static final String BUILD_VERSION_CONFIG_KEY = "build.version";
    protected static final String DEFAULT_BUILD_VERSION = "develop";

    protected final ConfigurationService configurationService;

    public Ns8PluginInstallRequestPopulator(final ConfigurationService configurationService) {
        this.configurationService = configurationService;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void populate(final NS8MerchantModel source, final Ns8PluginInstallRequest target) {
        target.setEmail(source.getEmail());
        target.setFirstName(source.getFirstName());
        target.setLastName(source.getLastName());
        target.setPhone(source.getPhone());
        target.setStoreUrl(source.getStoreUrl());

        populateVersionMetadata(target);
    }

    /**
     * Gets the generic metadata for install / reinstall the NS8 plugin request
     *
     * @return the generic metadata map
     */
    private void populateVersionMetadata(Ns8PluginInstallRequest target) {
        final String platformVersion = configurationService.getConfiguration().getString(BUILD_VERSION_CONFIG_KEY);
        final String moduleVersion = configurationService.getConfiguration().getString(NS_8_SERVICES_CONNECTOR_VERSION_CONFIG_KEY, DEFAULT_BUILD_VERSION);

        target.setPlatformVersion(platformVersion);
        target.setModuleVersion(moduleVersion);
    }

}
