package com.ns8.hybris.backoffice.widgets.controllers.ns8navigationcontext;

import com.hybris.cockpitng.annotations.ViewEvent;
import com.hybris.cockpitng.util.DefaultWidgetController;
import com.ns8.hybris.backoffice.data.context.Ns8NavigationContextData;
import de.hybris.platform.cms2.model.site.CMSSiteModel;
import de.hybris.platform.cms2.servicelayer.services.CMSSiteService;
import de.hybris.platform.servicelayer.i18n.I18NService;
import de.hybris.platform.servicelayer.session.SessionExecutionBody;
import de.hybris.platform.servicelayer.session.SessionService;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.event.SelectEvent;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zk.ui.select.annotation.WireVariable;
import org.zkoss.zul.Combobox;
import org.zkoss.zul.Comboitem;
import org.zkoss.zul.ListModelList;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import static java.util.stream.Collectors.toList;

/**
 * Ns8 Navigation context controller to handle the navigation context values
 */
public class Ns8NavigationContextController extends DefaultWidgetController {

    protected static final String CLEAR_NS8_TREE_EVENT = "clearNs8Tree";
    protected static final String NS8_NAVIGATION_CONTEXT = "ns8NavigationContext";
    protected static final String CMS_SITE_UID_EVENT = "cmsSiteUId";

    @WireVariable
    protected transient SessionService sessionService;
    @WireVariable
    protected transient I18NService i18nService;
    @WireVariable
    protected transient CMSSiteService cmsSiteService;
    @Wire
    protected Combobox cmsSiteSelector;

    protected ListModelList<CMSSiteModel> cmsSiteModels = new ListModelList();

    /**
     * {@inheritDoc}
     */
    @Override
    public void initialize(final Component component) {
        initializeSelectors();
        component.addEventListener(Events.ON_CREATE, event -> {
            final Ns8NavigationContextData ns8NavigationContext = getNs8NavigationContext();
            updateSelectors(ns8NavigationContext);
            sendCmsSiteUId(ns8NavigationContext);
            this.sendOutput(CLEAR_NS8_TREE_EVENT, null);
        });
    }

    /**
     * When the user change the site from combo box, it receives the event notification about the
     * selection and populates selected site id in the Ns8NavigationContextData
     *
     * @param siteChangedEvent the site id event
     */
    @ViewEvent(
            componentID = "cmsSiteSelector",
            eventName = "onSelect"
    )
    public void onCmsSiteChanged(final SelectEvent<Comboitem, String> siteChangedEvent) {
        final Ns8NavigationContextData ns8NavigationContext = getNs8NavigationContext();
        final String newCmsSiteUId = siteChangedEvent.getReference().getValue();
        if (ns8NavigationContext != null && !Objects.equals(ns8NavigationContext.getCmsSite(), newCmsSiteUId)) {
            ns8NavigationContext.setCmsSite(newCmsSiteUId);
            updateSelectors(ns8NavigationContext);
            sendCmsSiteUId(ns8NavigationContext);
        }
    }

    /**
     * Initializes the combo bow selector
     */
    protected void initializeSelectors() {
        final Ns8NavigationContextData ns8NavigationContext = new Ns8NavigationContextData();
        ns8NavigationContext.setCmsSite(null);
        setNs8NavigationContext(ns8NavigationContext);
        this.cmsSiteSelector.setModel(this.cmsSiteModels);
    }

    /**
     * Updates the selected value in the context
     *
     * @param ns8NavigationContext the navigation context data
     */
    protected void updateSelectors(final Ns8NavigationContextData ns8NavigationContext) {
        sessionService.executeInLocalView(new SessionExecutionBody() {
            public void executeWithoutResult() {
                Ns8NavigationContextController.this.i18nService.setLocalizationFallbackEnabled(true);
                Ns8NavigationContextController.this.updateCmsSite(ns8NavigationContext);
            }
        });
    }

    /**
     * Populates the combo box list as well as the selection. Then updates the navigation context data
     *
     * @param ns8NavigationContext the navigation context data
     */
    protected void updateCmsSite(final Ns8NavigationContextData ns8NavigationContext) {
        final Set<CMSSiteModel> selectedSite = this.cmsSiteModels.getSelection();
        final CMSSiteModel selectedCmsSite = selectedSite.isEmpty() ? null : selectedSite.iterator().next();
        final List<CMSSiteModel> allSystemCmsSites = cmsSiteService.getSites().stream().collect(toList());
        allSystemCmsSites.add(0, null);
        if (!CollectionUtils.isEqualCollection(this.cmsSiteModels.getInnerList(), allSystemCmsSites)) {
            this.cmsSiteModels.clear();
            this.cmsSiteModels.addAll(allSystemCmsSites);
        }

        if (CollectionUtils.isEmpty(this.cmsSiteModels.getInnerList()) || (this.cmsSiteModels.getInnerList().size() == 1 && this.cmsSiteModels.getInnerList().contains(null))) {
            ns8NavigationContext.setCmsSite(null);
            this.cmsSiteModels.setSelection(allSystemCmsSites);
            this.cmsSiteSelector.setDisabled(true);
        } else if (ns8NavigationContext.getCmsSite() == null || !this.cmsSiteModels.contains(selectedCmsSite)) {
            ns8NavigationContext.setCmsSite(selectedCmsSite == null || StringUtils.isBlank(selectedCmsSite.getUid()) ? null : selectedCmsSite.getUid());
            this.cmsSiteModels.setSelection(Collections.singletonList(selectedCmsSite));
            this.cmsSiteSelector.setDisabled(false);
        }

    }

    protected void sendCmsSiteUId(final Ns8NavigationContextData ns8NavigationContext) {
        this.sendOutput(CMS_SITE_UID_EVENT, ns8NavigationContext.getCmsSite());
    }

    public Ns8NavigationContextData getNs8NavigationContext() {
        return this.getModel().getValue(NS8_NAVIGATION_CONTEXT, Ns8NavigationContextData.class);
    }

    public void setNs8NavigationContext(final Ns8NavigationContextData ns8NavigationContext) {
        this.getModel().put(NS8_NAVIGATION_CONTEXT, ns8NavigationContext);
    }

    public ListModelList<CMSSiteModel> getCmsSiteModels() {
        return cmsSiteModels;
    }
}
