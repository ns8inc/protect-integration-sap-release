package com.ns8.hybris.backoffice.actions.site.merchant.activate;

import com.hybris.cockpitng.actions.ActionContext;
import com.hybris.cockpitng.actions.ActionResult;
import com.hybris.cockpitng.actions.CockpitAction;
import com.hybris.cockpitng.engine.impl.AbstractComponentWidgetAdapterAware;
import de.hybris.platform.basecommerce.model.site.BaseSiteModel;

/**
 * NS8 action to activate the current CmsSite with NS8
 */
public class NS8MerchantActivationAction extends AbstractComponentWidgetAdapterAware implements CockpitAction<Object, Object> {

    protected static final String SOCKET_OUT_NS8_CMSSITE = "currentSiteOutput";
    protected static final String ACTION_RESULT_SUCCESS_CODE = "success";
    protected static final String ACTION_RESULT_ERROR_CODE = "error";

    /**
     * {@inheritDoc}
     */
    @Override
    public ActionResult<Object> perform(final ActionContext<Object> actionContext) {
        ActionResult<Object> result;
        if (actionContext.getData() instanceof BaseSiteModel) {
            result = new ActionResult(ACTION_RESULT_SUCCESS_CODE);
            this.sendOutput(SOCKET_OUT_NS8_CMSSITE, actionContext.getData());
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
            final BaseSiteModel baseSite = (BaseSiteModel) ctx.getData();
            return baseSite.getNs8Merchant() == null || !baseSite.getNs8Merchant().getEnabled();
        }
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean needsConfirmation(final ActionContext<Object> ctx) {
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getConfirmationMessage(final ActionContext<Object> ctx) {
        return null;
    }
}
