package com.ns8.hybris.backoffice.widgets.controllers.ns8contextpopulator;

import com.hybris.cockpitng.annotations.SocketEvent;
import com.hybris.cockpitng.util.DefaultWidgetController;
import com.ns8.hybris.backoffice.data.context.Ns8NavigationContextData;
import de.hybris.platform.servicelayer.i18n.I18NService;
import de.hybris.platform.servicelayer.session.SessionExecutionBody;
import de.hybris.platform.servicelayer.session.SessionService;
import org.apache.commons.lang.StringUtils;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.select.annotation.WireVariable;

import java.util.Objects;

/**
 * Receives the events with information about user navigation and populate the result in the Ns8NavigationContextData
 * in order to show the correct view
 */
public class Ns8ContextPopulatorController extends DefaultWidgetController {

    protected static final String DASHBOARD_VIEW_NAME = "DASHBOARD";
    protected static final String SUSPICIOUS_ORDERS_VIEW_NAME = "SUSPICIOUS_ORDERS";
    protected static final String ORDER_RULES_VIEW_NAME = "ORDER_RULES";
    protected static final String NS8_NAVIGATION_CONTEXT_DATA = "ns8NavigationContextData";

    @WireVariable
    protected transient SessionService sessionService;
    @WireVariable
    protected transient I18NService i18nService;

    protected String cmsSiteUid;
    protected String viewName;

    /**
     * {@inheritDoc}
     */
    @Override
    public void initialize(final Component component) {
        initializeNs8NavigationContext();
        component.addEventListener(Events.ON_CREATE, event -> {
            final Ns8NavigationContextData ns8NavigationContext = getNs8NavigationContextData();
            updateNs8NavigationContext(ns8NavigationContext);
            sendNs8NavigationContextData(ns8NavigationContext);
        });
    }

    /**
     * When the user selects the main dashboard node from the related tree, it receives the event notification about the
     * selection and populates the view DASHBOARD in the Ns8NavigationContextData
     *
     * @param mainDashboardSelected the event notification
     */
    @SocketEvent(
            socketId = "mainDashboardSelected"
    )
    public void onMainDashboardSelected(final Boolean mainDashboardSelected) {
        populateViewName(mainDashboardSelected, DASHBOARD_VIEW_NAME);
    }

    /**
     * When the user selects the main dashboard node from the related tree, it receives the event notification about the
     * selection and populates the view SUSPICIOUS_ORDERS in the Ns8NavigationContextData
     *
     * @param suspiciousOrdersSelected the event notification
     */
    @SocketEvent(
            socketId = "suspiciousOrdersSelected"
    )
    public void onSuspiciousOrdersSelected(final Boolean suspiciousOrdersSelected) {
        populateViewName(suspiciousOrdersSelected, SUSPICIOUS_ORDERS_VIEW_NAME);
    }

    /**
     * When the user selects the main dashboard node from the related tree, it receives the event notification about the
     * selection and populates the view ORDER_RULES in the Ns8NavigationContextData
     *
     * @param orderRulesViewSelected the event notification
     */
    @SocketEvent(
            socketId = "orderRulesViewSelected"
    )
    public void onOrderRulesViewSelected(final Boolean orderRulesViewSelected) {
        populateViewName(orderRulesViewSelected, ORDER_RULES_VIEW_NAME);
    }

    /**
     * When the user selects the site from the navigation context, it receives the event notification about the
     * selection and populates selected site id in the Ns8NavigationContextData
     *
     * @param cmsSiteUId the site id event
     */
    @SocketEvent(
            socketId = "cmsSiteUId"
    )
    public void onCmsSiteChanged(final String cmsSiteUId) {
        final Ns8NavigationContextData ns8NavigationContext = getNs8NavigationContextData();
        if (ns8NavigationContext != null && !Objects.equals(ns8NavigationContext.getCmsSite(), cmsSiteUId)) {
            setCmsSiteUid(cmsSiteUId);
            updateNs8NavigationContext(ns8NavigationContext);
            setNs8NavigationContextData(ns8NavigationContext);
            sendNs8NavigationContextData(ns8NavigationContext);
        }
    }

    /**
     * Populates the view name in the Ns8NavigationContextData if has been selected
     *
     * @param isViewSelected tells if the view has been selected
     * @param viewName       the view name
     */
    protected void populateViewName(final Boolean isViewSelected, final String viewName) {
        final Ns8NavigationContextData ns8NavigationContext = getNs8NavigationContextData();
        if (isViewSelected && ns8NavigationContext != null && !viewName.equalsIgnoreCase(ns8NavigationContext.getViewName())) {
            setViewName(viewName);
            updateNs8NavigationContext(ns8NavigationContext);
            setNs8NavigationContextData(ns8NavigationContext);
            sendNs8NavigationContextData(ns8NavigationContext);
        }
    }

    /**
     * Updates the context with the session data
     *
     * @param ns8NavigationContext the navigation context data
     */
    protected void updateNs8NavigationContext(final Ns8NavigationContextData ns8NavigationContext) {
        sessionService.executeInLocalView(new SessionExecutionBody() {
            public void executeWithoutResult() {
                Ns8ContextPopulatorController.this.i18nService.setLocalizationFallbackEnabled(true);
                Ns8ContextPopulatorController.this.updateCmsSite(ns8NavigationContext);
                Ns8ContextPopulatorController.this.updateViewName(ns8NavigationContext);
            }
        });
    }

    /**
     * Updates the site id in the Ns8NavigationContextData
     *
     * @param ns8NavigationContextData the navigation context data
     */
    protected void updateCmsSite(final Ns8NavigationContextData ns8NavigationContextData) {
        if (StringUtils.isBlank(getCmsSiteUid())) {
            ns8NavigationContextData.setCmsSite(null);
        } else {
            ns8NavigationContextData.setCmsSite(getCmsSiteUid());
        }
    }

    /**
     * Updates the view name in the Ns8NavigationContextData
     *
     * @param ns8NavigationContextData the navigation context data
     */
    protected void updateViewName(final Ns8NavigationContextData ns8NavigationContextData) {
        if (StringUtils.isBlank(getViewName())) {
            ns8NavigationContextData.setViewName(null);
        } else {
            ns8NavigationContextData.setViewName(getViewName());
        }
    }

    /**
     * Initializes the context with null values
     */
    protected void initializeNs8NavigationContext() {
        final Ns8NavigationContextData ns8NavigationContext = new Ns8NavigationContextData();
        setNs8NavigationContextData(ns8NavigationContext);
    }

    /**
     * Sends the output to the view widget
     *
     * @param ns8NavigationContextData the navigation context data
     */
    protected void sendNs8NavigationContextData(final Ns8NavigationContextData ns8NavigationContextData) {
        this.sendOutput(NS8_NAVIGATION_CONTEXT_DATA, ns8NavigationContextData);
    }

    protected Ns8NavigationContextData getNs8NavigationContextData() {
        return this.getModel().getValue(NS8_NAVIGATION_CONTEXT_DATA, Ns8NavigationContextData.class);
    }

    protected void setNs8NavigationContextData(final Ns8NavigationContextData ns8NavigationContextData) {
        this.getModel().put(NS8_NAVIGATION_CONTEXT_DATA, ns8NavigationContextData);
    }

    protected String getCmsSiteUid() {
        return cmsSiteUid;
    }

    protected void setCmsSiteUid(final String cmsSiteUid) {
        this.cmsSiteUid = cmsSiteUid;
    }

    protected String getViewName() {
        return viewName;
    }

    protected void setViewName(final String viewName) {
        this.viewName = viewName;
    }
}
