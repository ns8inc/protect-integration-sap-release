package com.ns8.hybris.backoffice.actions.site.merchant.deactivate;

import com.hybris.cockpitng.actions.ActionContext;
import com.hybris.cockpitng.actions.ActionResult;
import com.hybris.cockpitng.actions.CockpitAction;
import com.hybris.cockpitng.core.events.CockpitEventQueue;
import com.hybris.cockpitng.core.events.impl.DefaultCockpitEvent;
import com.hybris.cockpitng.dataaccess.facades.object.ObjectFacade;
import com.hybris.cockpitng.engine.impl.AbstractComponentWidgetAdapterAware;
import com.ns8.hybris.core.integration.exceptions.Ns8IntegrationException;
import com.ns8.hybris.core.merchant.services.Ns8MerchantService;
import de.hybris.platform.basecommerce.model.site.BaseSiteModel;
import org.zkoss.zhtml.Messagebox;

import javax.annotation.Resource;

import static org.zkoss.zul.Messagebox.ERROR;

/**
 * NS8 action to deactivate the current ns8 merchant
 */
public class Ns8MerchantDeactivationAction extends AbstractComponentWidgetAdapterAware implements CockpitAction<Object, Object> {

    protected static final String ACTION_RESULT_SUCCESS_CODE = "success";
    protected static final String ACTION_RESULT_ERROR_CODE = "error";
    protected static final String DEACTIVATE_MERCHANT_SUCCESS_ACTION = "ns8.deactivate.merchant.confirm.action";
    protected static final String DEACTIVATE_MERCHANT_ERROR_ACTION = "ns8.deactivate.merchant.error.action";
    protected static final String DEACTIVATE_MERCHANT_UNSUPPORTED_ACTION = "ns8.deactivate.merchant.unsupported.action";

    @Resource
    private Ns8MerchantService ns8MerchantService;
    @Resource
    private CockpitEventQueue cockpitEventQueue;

    /**
     * {@inheritDoc}
     */
    @Override
    public ActionResult<Object> perform(final ActionContext<Object> ctx) {
        final Object data = ctx.getData();

        if (data instanceof BaseSiteModel) {
            final BaseSiteModel baseSite = (BaseSiteModel) data;

            try {
                ns8MerchantService.deactivateMerchant(baseSite.getNs8Merchant());
                cockpitEventQueue.publishEvent(new DefaultCockpitEvent(ObjectFacade.OBJECTS_UPDATED_EVENT, baseSite, null));
                showMessageToUser(ctx.getLabel(DEACTIVATE_MERCHANT_SUCCESS_ACTION));
                return new ActionResult(ACTION_RESULT_SUCCESS_CODE);
            } catch (final Ns8IntegrationException e) {
                showMessageBoxFromErrorMessage(ctx, e.getMessage());
                return new ActionResult(ACTION_RESULT_ERROR_CODE);
            }
        } else {
            showMessageToUser(ctx.getLabel(DEACTIVATE_MERCHANT_UNSUPPORTED_ACTION));
            return new ActionResult(ACTION_RESULT_ERROR_CODE);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean canPerform(final ActionContext<Object> ctx) {
        if (ctx.getData() instanceof BaseSiteModel) {
            final BaseSiteModel baseSite = (BaseSiteModel) ctx.getData();
            return baseSite.getNs8Merchant() != null && baseSite.getNs8Merchant().getEnabled();
        }
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean needsConfirmation(final ActionContext<Object> ctx) {
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getConfirmationMessage(final ActionContext<Object> ctx) {
        return ctx.getLabel("ns8.deactivate.merchant.confirm.deactivation");
    }

    /**
     * Shows message to backoffice user
     */
    protected void showMessageToUser(final String message) {
        Messagebox.show(message);
    }

    /**
     * Shows error message to backoffice user
     *
     * @param errorMessage the error message to show
     */
    protected void showMessageBoxFromErrorMessage(final ActionContext<Object> ctx, final String errorMessage) {
        Messagebox.show(errorMessage, ctx.getLabel(DEACTIVATE_MERCHANT_ERROR_ACTION), 1, ERROR);
    }
}
