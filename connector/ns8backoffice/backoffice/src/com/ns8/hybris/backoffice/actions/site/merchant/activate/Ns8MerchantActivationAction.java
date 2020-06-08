package com.ns8.hybris.backoffice.actions.site.merchant.activate;

import com.hybris.cockpitng.actions.ActionContext;
import com.hybris.cockpitng.actions.ActionResult;
import com.hybris.cockpitng.actions.CockpitAction;
import com.hybris.cockpitng.core.events.CockpitEventQueue;
import com.hybris.cockpitng.core.events.impl.DefaultCockpitEvent;
import com.hybris.cockpitng.dataaccess.facades.object.ObjectFacade;
import com.hybris.cockpitng.engine.impl.AbstractComponentWidgetAdapterAware;
import com.hybris.cockpitng.util.notifications.NotificationService;
import com.ns8.hybris.core.integration.exceptions.Ns8IntegrationException;
import com.ns8.hybris.core.merchant.services.Ns8MerchantService;
import com.ns8.hybris.core.model.NS8MerchantModel;
import de.hybris.platform.basecommerce.model.site.BaseSiteModel;
import org.zkoss.zhtml.Messagebox;

import javax.annotation.Resource;

import static com.hybris.cockpitng.util.notifications.event.NotificationEvent.Level;
import static org.zkoss.zul.Messagebox.ERROR;

/**
 * NS8 action to activate the current CmsSite with NS8
 */
public class Ns8MerchantActivationAction extends AbstractComponentWidgetAdapterAware implements CockpitAction<Object, Object> {

    protected static final String SOCKET_OUT_NS8_CMSSITE = "currentSiteOutput";
    protected static final String ACTION_RESULT_SUCCESS_CODE = "success";
    protected static final String ACTION_RESULT_ERROR_CODE = "error";
    protected static final String REACTIVATE_MERCHANT_ERROR_ACTION = "ns8.reactivate.merchant.error.action";
    protected static final String REACTIVATE_MERCHANT_SUCCESS_ACTION = "ns8.reactivate.merchant.confirm.action";
    protected static final String MERCHANT_CONFIRM_REACTIVATION = "ns8.reactivate.merchant.confirm.reactivation";

    @Resource
    private Ns8MerchantService ns8MerchantService;
    @Resource
    private CockpitEventQueue cockpitEventQueue;
    @Resource
    private NotificationService notificationService;

    /**
     * {@inheritDoc}
     */
    @Override
    public ActionResult<Object> perform(final ActionContext<Object> actionContext) {
        ActionResult<Object> result;
        final Object data = actionContext.getData();

        if (data instanceof BaseSiteModel) {
            final BaseSiteModel baseSite = (BaseSiteModel) data;
            final NS8MerchantModel ns8Merchant = baseSite.getNs8Merchant();

            if (ns8Merchant != null && !ns8MerchantService.isMerchantActive(ns8Merchant)) {
                try {
                    ns8MerchantService.reactivateMerchant(ns8Merchant);
                    notificationService.notifyUser(actionContext, "JustMessage", Level.SUCCESS, actionContext.getLabel(REACTIVATE_MERCHANT_SUCCESS_ACTION));
                    cockpitEventQueue.publishEvent(new DefaultCockpitEvent(ObjectFacade.OBJECTS_UPDATED_EVENT, baseSite, null));
                    result = new ActionResult(ACTION_RESULT_SUCCESS_CODE);
                } catch (final Ns8IntegrationException | IllegalArgumentException e) {
                    showMessageBoxFromErrorMessage(actionContext, e.getMessage());
                    result = new ActionResult(ACTION_RESULT_ERROR_CODE);
                }

            } else {
                result = new ActionResult(ACTION_RESULT_SUCCESS_CODE);
                this.sendOutput(SOCKET_OUT_NS8_CMSSITE, actionContext.getData());
            }
        } else {
            result = new ActionResult(ACTION_RESULT_ERROR_CODE);
        }

        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean canPerform(final ActionContext<Object> ctx) {
        if (ctx.getData() instanceof BaseSiteModel) {
            final NS8MerchantModel ns8Merchant = ((BaseSiteModel) ctx.getData()).getNs8Merchant();
            return ns8Merchant == null || !ns8MerchantService.isMerchantActive(ns8Merchant);
        }
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean needsConfirmation(final ActionContext<Object> ctx) {
        final Object data = ctx.getData();
        if (data instanceof BaseSiteModel) {
            final BaseSiteModel baseSite = (BaseSiteModel) data;
            final NS8MerchantModel ns8Merchant = baseSite.getNs8Merchant();
            return ns8Merchant != null && !ns8MerchantService.isMerchantActive(ns8Merchant);
        }
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getConfirmationMessage(final ActionContext<Object> ctx) {
        return ctx.getLabel(MERCHANT_CONFIRM_REACTIVATION);
    }

    /**
     * Shows error message to backoffice user
     *
     * @param errorMessage the error message to show
     */
    protected void showMessageBoxFromErrorMessage(final ActionContext<Object> ctx, final String errorMessage) {
        Messagebox.show(errorMessage, ctx.getLabel(REACTIVATE_MERCHANT_ERROR_ACTION), 1, ERROR);
    }

}
