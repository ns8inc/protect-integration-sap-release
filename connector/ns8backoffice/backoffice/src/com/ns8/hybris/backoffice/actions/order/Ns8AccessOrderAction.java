package com.ns8.hybris.backoffice.actions.order;

import com.hybris.cockpitng.actions.ActionContext;
import com.hybris.cockpitng.actions.ActionResult;
import com.hybris.cockpitng.actions.CockpitAction;
import com.hybris.cockpitng.core.events.CockpitEventQueue;
import com.hybris.cockpitng.core.events.impl.DefaultCockpitEvent;
import com.hybris.cockpitng.dataaccess.facades.object.ObjectFacade;
import com.hybris.cockpitng.engine.impl.AbstractComponentWidgetAdapterAware;
import com.ns8.hybris.core.integration.exceptions.Ns8IntegrationException;
import com.ns8.hybris.core.services.api.Ns8ApiService;
import de.hybris.platform.core.model.order.OrderModel;
import de.hybris.platform.processengine.BusinessProcessService;
import org.apache.commons.lang.StringUtils;
import org.zkoss.zhtml.Messagebox;

import javax.annotation.Resource;
import java.util.Optional;

import static org.zkoss.zul.Messagebox.INFORMATION;

/**
 * NS8 action to show the ns8 order details into a widget
 */
public class Ns8AccessOrderAction extends AbstractComponentWidgetAdapterAware implements CockpitAction<Object, Object> {

    @Resource
    protected BusinessProcessService businessProcessService;

    @Resource
    protected CockpitEventQueue cockpitEventQueue;

    @Resource
    protected Ns8ApiService ns8ApiService;


    protected static final String ACTION_RESULT_SUCCESS_CODE = "success";
    protected static final String ACTION_RESULT_ERROR_CODE = "error";
    protected static final String CURRENT_ORDER_OUTPUT = "currentOrderOutput";
    protected static final String NS8_SCORE_RECEIVED_EVENT = "_NS8ScoreReceived";
    protected static final String NS8_ORDER_SCORE_MESSAGEBOX_TITLE = "ns8.order.score.messagebox.title";
    protected static final String NS8_ORDER_SCORE_MESSAGEBOX_MESSAGE_MERCHANT_INACTIVE = "ns8.order.score.messagebox.message.merchant.inactive";

    /**
     * {@inheritDoc}
     */
    @Override
    public ActionResult<Object> perform(final ActionContext<Object> actionContext) {
        ActionResult<Object> result;
        if (actionContext.getData() instanceof OrderModel) {
            final OrderModel order = (OrderModel) actionContext.getData();
            try {
                if (StringUtils.isEmpty(order.getRiskEventPayload())) {
                    ns8ApiService.fetchAndSaveNs8OrderPayload(order);
                }
                triggerOrderEvent(order);
                this.sendOutput(CURRENT_ORDER_OUTPUT, actionContext.getData());
                cockpitEventQueue.publishEvent(new DefaultCockpitEvent(ObjectFacade.OBJECTS_UPDATED_EVENT, order, null));
                result = new ActionResult(ACTION_RESULT_SUCCESS_CODE);
            } catch (Ns8IntegrationException ex) {
                if (!order.getMerchantEnabled()) {
                    showInformationMessageBoxFromMessage(actionContext, actionContext.getLabel(NS8_ORDER_SCORE_MESSAGEBOX_MESSAGE_MERCHANT_INACTIVE));
                }
                result = new ActionResult(ACTION_RESULT_ERROR_CODE);
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
        if (ctx.getData() instanceof OrderModel) {
            final OrderModel order = (OrderModel) ctx.getData();
            return Optional.ofNullable(order.getMerchantEnabled()).orElse(false);
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

    /**
     * Unblocks the order process triggering the business process event
     *
     * @param order the order
     */
    protected void triggerOrderEvent(final OrderModel order) {
        final String ns8ScoreReceivedEventName = order.getCode() + NS8_SCORE_RECEIVED_EVENT;
        businessProcessService.triggerEvent(ns8ScoreReceivedEventName);
    }

    /**
     * Shows error message to backoffice user
     *
     * @param errorMessage the error message to show
     */
    protected void showInformationMessageBoxFromMessage(final ActionContext<Object> ctx, final String errorMessage) {
        Messagebox.show(errorMessage, ctx.getLabel(NS8_ORDER_SCORE_MESSAGEBOX_TITLE), 1, INFORMATION);
    }
}
