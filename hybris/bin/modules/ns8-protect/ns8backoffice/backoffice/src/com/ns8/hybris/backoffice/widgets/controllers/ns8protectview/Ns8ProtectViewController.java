package com.ns8.hybris.backoffice.widgets.controllers.ns8protectview;

import com.hybris.backoffice.widgets.notificationarea.event.NotificationEvent;
import com.hybris.cockpitng.annotations.SocketEvent;
import com.hybris.cockpitng.util.DefaultWidgetController;
import com.hybris.cockpitng.util.notifications.NotificationService;
import com.ns8.hybris.backoffice.data.context.Ns8NavigationContextData;
import com.ns8.hybris.core.services.api.NS8EndpointService;
import de.hybris.platform.cms2.model.site.CMSSiteModel;
import de.hybris.platform.cms2.servicelayer.services.CMSSiteService;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.zkoss.zk.ui.select.annotation.WireVariable;

import java.util.Collection;
import java.util.Optional;

/**
 * Populates model for the NS8 Protect view, handles the refresh and exposes useful methods for the zul
 */
public class Ns8ProtectViewController extends DefaultWidgetController {

    protected static final String NS8_PROTECT_VIEW_MERCHANT_INACTIVE_ERROR = "ns8.protect.view.merchant.inactive.error";
    protected static final String NS8_PROTECT_VIEW_VISIBLE_KEY = "ns8ProtectViewVisible";

    @WireVariable
    protected transient NS8EndpointService ns8EndpointService;
    @WireVariable
    protected transient NotificationService notificationService;
    @WireVariable
    protected transient CMSSiteService cmsSiteService;

    /**
     * Shows the NS8 protect view based on the given cms site code and view selected and refresh the widget ui
     *
     * @param ns8NavigationContextData the navigation context populated
     */
    @SocketEvent(
            socketId = "ns8NavigationContextData"
    )
    public void onNavigationContextDataEvent(final Ns8NavigationContextData ns8NavigationContextData) {
        updateProtectViewContext(ns8NavigationContextData);

        if (StringUtils.isNotBlank(ns8NavigationContextData.getViewName()) && StringUtils.isNotBlank(ns8NavigationContextData.getAccessToken())) {
            getWidgetInstanceManager().getModel().setValue(NS8_PROTECT_VIEW_VISIBLE_KEY, true);
            getWidgetInstanceManager().getModel().setValue("viewName", ns8NavigationContextData.getViewName());
            getWidgetInstanceManager().getModel().setValue("accessToken", ns8NavigationContextData.getAccessToken());
        } else {
            getWidgetInstanceManager().getModel().setValue(NS8_PROTECT_VIEW_VISIBLE_KEY, false);
        }
        this.getWidgetslot().updateView();
    }

    /**
     * Updates the view context in order to populate the model with the correct values
     *
     * @param ns8NavigationContextData the navigation context populated
     */
    protected void updateProtectViewContext(final Ns8NavigationContextData ns8NavigationContextData) {
        if (ns8NavigationContextData != null) {
            if (StringUtils.isNotBlank(ns8NavigationContextData.getCmsSite()) &&
                    StringUtils.isNotBlank(ns8NavigationContextData.getViewName())) {

                getSelectedCmsSite(ns8NavigationContextData.getCmsSite()).ifPresentOrElse(site -> {
                            if (site.getNs8Merchant() != null && site.getNs8Merchant().getEnabled() && StringUtils.isNotBlank(site.getNs8Merchant().getApiKey())) {
                                ns8NavigationContextData.setAccessToken(site.getNs8Merchant().getApiKey());
                            } else {
                                notificationService.notifyUser(getWidgetInstanceManager(), "JustMessage",
                                        NotificationEvent.Level.FAILURE, getLabel(NS8_PROTECT_VIEW_MERCHANT_INACTIVE_ERROR));
                                ns8NavigationContextData.setAccessToken(null);
                            }
                        },
                        () -> resetContextData(ns8NavigationContextData)
                );
            } else {
                resetContextData(ns8NavigationContextData);
            }
        }
    }

    /**
     * Resets the context with null values
     *
     * @param ns8NavigationContextData the navigation context populated
     */
    protected void resetContextData(final Ns8NavigationContextData ns8NavigationContextData) {
        ns8NavigationContextData.setAccessToken(null);
        ns8NavigationContextData.setCmsSite(null);
        ns8NavigationContextData.setViewName(null);
    }

    /**
     * Gets cms site based on the uid
     *
     * @return the optional with the site model, otherwise optional empty if not found
     */
    protected Optional<CMSSiteModel> getSelectedCmsSite(final String siteUid) {
        final Collection<CMSSiteModel> systemSites = cmsSiteService.getSites();
        if (CollectionUtils.isNotEmpty(systemSites)) {
            return systemSites.stream().filter(site -> siteUid.equalsIgnoreCase(site.getUid())).findFirst();
        }

        return Optional.empty();
    }

    /**
     * Method for getting client api url from the configuration
     *
     * @return client api url
     */
    public String getClientApiUrl() {
        return ns8EndpointService.getBaseClientURL();
    }
}
