package com.ns8.hybris.backoffice.actions.order;

import com.hybris.cockpitng.actions.ActionContext;
import com.hybris.cockpitng.actions.ActionResult;
import com.hybris.cockpitng.actions.CockpitAction;
import com.hybris.cockpitng.engine.impl.AbstractComponentWidgetAdapterAware;
import com.ns8.hybris.core.model.NS8MerchantModel;
import de.hybris.platform.basecommerce.model.site.BaseSiteModel;
import de.hybris.platform.core.model.order.OrderModel;

import java.util.Optional;

/**
 * NS8 action to show the ns8 order details into a widget
 */
public class NS8AccessOrderAction extends AbstractComponentWidgetAdapterAware implements CockpitAction<Object, Object> {

    protected static final String ACTION_RESULT_SUCCESS_CODE = "success";
    protected static final String ACTION_RESULT_ERROR_CODE = "error";
    protected static final String CURRENT_ORDER_OUTPUT = "currentOrderOutput";

    /**
     * {@inheritDoc}
     */
    @Override
    public ActionResult<Object> perform(final ActionContext<Object> actionContext) {
        ActionResult<Object> result;
        if (actionContext.getData() instanceof OrderModel) {
            this.sendOutput(CURRENT_ORDER_OUTPUT, actionContext.getData());
            result = new ActionResult(ACTION_RESULT_SUCCESS_CODE);
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
        if (ctx.getData() instanceof OrderModel) {
            final OrderModel order = (OrderModel) ctx.getData();
            return Optional.ofNullable(order.getSite()).map(BaseSiteModel::getNs8Merchant).map(NS8MerchantModel::getEnabled).orElse(false);
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
