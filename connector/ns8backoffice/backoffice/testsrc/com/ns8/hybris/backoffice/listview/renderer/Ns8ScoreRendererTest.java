package com.ns8.hybris.backoffice.listview.renderer;

import com.google.gson.Gson;
import com.hybris.cockpitng.core.config.impl.jaxb.listview.ListColumn;
import com.hybris.cockpitng.dataaccess.facades.permissions.PermissionFacade;
import com.hybris.cockpitng.dataaccess.facades.type.DataType;
import com.hybris.cockpitng.dataaccess.services.PropertyValueService;
import com.hybris.cockpitng.engine.WidgetInstanceManager;
import de.hybris.bootstrap.annotations.UnitTest;
import de.hybris.platform.core.model.order.OrderModel;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.zkoss.zul.Listcell;

import java.util.Map;

import static org.mockito.Mockito.*;
import static org.mockito.Mockito.when;

@UnitTest
@RunWith(MockitoJUnitRunner.class)
public class Ns8ScoreRendererTest {

    private static final String SCORE_KEY = "score";
    private static final String NOT_AVAILABLE_MESSAGE = "N/A";
    private static final Double SCORE_VALUE = 100d;
    private static final Integer SCORE_INTEGER_VALUE = SCORE_VALUE.intValue();
    private static final String SCORE_VALUE_LABEL = String.valueOf(SCORE_INTEGER_VALUE);
    private static final String ORDER_TYPE = "order";

    @InjectMocks
    private Ns8ScoreRenderer testObj;

    @Mock
    private Listcell listCellMock;
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private OrderModel orderModelMock;
    @Mock
    private ListColumn columnConfigurationMock;
    @Mock
    private DataType dataTypeMock;
    @Mock
    private PermissionFacade permissionFacadeMock;
    @Mock
    private PropertyValueService propertyValueServiceMock;
    @Mock
    private WidgetInstanceManager widgetInstanceManagerMock;

    @Before
    public void setUp() {
        when(dataTypeMock.getCode()).thenReturn(ORDER_TYPE);
        when(permissionFacadeMock.canReadProperty(ORDER_TYPE, SCORE_KEY)).thenReturn(true);
        when(orderModelMock.getRiskEventPayload()).thenReturn(new Gson().toJson(createEventBody()));
        when(propertyValueServiceMock.readValue(orderModelMock, SCORE_KEY)).thenReturn(SCORE_VALUE);
    }

    @Test
    public void render_shouldSetLabelWithAmountValue_WhenRiskEventPayloadIsNotEmpty() {
        testObj.render(listCellMock, columnConfigurationMock, orderModelMock, dataTypeMock, widgetInstanceManagerMock);
        verify(listCellMock).setLabel(SCORE_VALUE_LABEL);
    }

    @Test
    public void render_shouldSetLabelWithAmountValue_WhenRiskEventPayloadIsEmptyButNs8OrderPayloadIsNot() {
        when(orderModelMock.getRiskEventPayload()).thenReturn(null);
        when(orderModelMock.getNs8OrderPayload()).thenReturn(new Gson().toJson(createEventBody()));
        testObj.render(listCellMock, columnConfigurationMock, orderModelMock, dataTypeMock, widgetInstanceManagerMock);
        verify(listCellMock).setLabel(SCORE_VALUE_LABEL);
    }

    @Test
    public void render_shouldSetNotAvailable_WhenScoreIsEmpty() {
        when(propertyValueServiceMock.readValue(orderModelMock, SCORE_KEY)).thenReturn(null);
        testObj.render(listCellMock, columnConfigurationMock, orderModelMock, dataTypeMock, widgetInstanceManagerMock);
        verify(listCellMock).setLabel(NOT_AVAILABLE_MESSAGE);
    }

    @Test
    public void render_shouldSetNotAvailable_WhenRiskEventPayloadIsEmpty() {
        when(orderModelMock.getRiskEventPayload()).thenReturn(null);
        testObj.render(listCellMock, columnConfigurationMock, orderModelMock, dataTypeMock, widgetInstanceManagerMock);
        verify(listCellMock).setLabel(NOT_AVAILABLE_MESSAGE);
    }

    @Test
    public void render_shouldSetNotAvailable_WhenRiskEventPayloadAndNs8OrderPayloadAreEmpty() {
        when(orderModelMock.getRiskEventPayload()).thenReturn(null);
        when(orderModelMock.getNs8OrderPayload()).thenReturn(null);
        testObj.render(listCellMock, columnConfigurationMock, orderModelMock, dataTypeMock, widgetInstanceManagerMock);
        verify(listCellMock).setLabel(NOT_AVAILABLE_MESSAGE);
    }

    private Map<String, Object> createEventBody() {
        final StringBuilder eventBody = new StringBuilder();
        eventBody.append("{");
        eventBody.append("  \"score\": \"100\",");
        eventBody.append("  \"status\": \"MERCHANT_REVIEW\"");
        eventBody.append("}");

        return new Gson().fromJson(eventBody.toString(), Map.class);
    }

}
