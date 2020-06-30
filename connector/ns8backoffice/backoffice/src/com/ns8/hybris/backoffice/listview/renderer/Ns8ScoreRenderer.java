package com.ns8.hybris.backoffice.listview.renderer;

import com.hybris.cockpitng.core.config.impl.jaxb.listview.ListColumn;
import com.hybris.cockpitng.dataaccess.facades.permissions.PermissionFacade;
import com.hybris.cockpitng.dataaccess.facades.type.DataType;
import com.hybris.cockpitng.dataaccess.services.PropertyValueService;
import com.hybris.cockpitng.engine.WidgetInstanceManager;
import com.hybris.cockpitng.widgets.common.AbstractWidgetComponentRenderer;
import de.hybris.platform.core.model.order.OrderModel;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.zkoss.zul.Listcell;

import javax.annotation.Resource;

/**
 * Renderer for Ns8 Score column in orders table.
 */
public class Ns8ScoreRenderer extends AbstractWidgetComponentRenderer<Listcell, ListColumn, Object> {

    protected static final Logger LOG = LogManager.getLogger(Ns8ScoreRenderer.class);
    protected static final String SCORE_KEY = "score";
    protected static final String NOT_AVAILABLE_MESSAGE = "N/A";

    @Resource
    protected PermissionFacade permissionFacade;
    @Resource
    protected PropertyValueService propertyValueService;

    /**
     * Render the Ns8 Score.
     * If there is a score for the order, it returns the value.
     * Otherwise Not Available (N/A) is shown.
     *
     * @param listcell
     * @param configuration
     * @param object
     * @param dataType
     * @param widgetInstanceManager
     */
    @Override
    public void render
    (final Listcell listcell, final ListColumn configuration, final Object object, final DataType dataType, final WidgetInstanceManager widgetInstanceManager) {
        if (object instanceof OrderModel) {
            final OrderModel order = (OrderModel) object;
            if (StringUtils.isBlank(order.getRiskEventPayload()) && StringUtils.isBlank(order.getNs8OrderPayload())) {
                listcell.setLabel(NOT_AVAILABLE_MESSAGE);
            } else {
                final Double amount = (Double) propertyValueService.readValue(object, SCORE_KEY);
                if (amount != null && permissionFacade.canReadProperty(dataType.getCode(), SCORE_KEY)) {
                    listcell.setLabel(String.valueOf(amount.intValue()));
                } else {
                    listcell.setLabel(NOT_AVAILABLE_MESSAGE);
                }
            }
        } else {
            LOG.error("Given object is not of OrderModel type, please check your editor configuration!");
        }
    }
}
