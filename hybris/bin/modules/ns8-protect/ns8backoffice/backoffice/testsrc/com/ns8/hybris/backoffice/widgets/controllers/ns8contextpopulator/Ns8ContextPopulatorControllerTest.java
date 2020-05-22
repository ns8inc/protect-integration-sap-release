package com.ns8.hybris.backoffice.widgets.controllers.ns8contextpopulator;

import com.hybris.cockpitng.core.model.WidgetModel;
import com.hybris.cockpitng.engine.impl.DefaultWidgetInstanceManager;
import com.ns8.hybris.backoffice.data.context.Ns8NavigationContextData;
import de.hybris.bootstrap.annotations.UnitTest;
import de.hybris.platform.servicelayer.i18n.I18NService;
import de.hybris.platform.servicelayer.session.SessionExecutionBody;
import de.hybris.platform.servicelayer.session.SessionService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.event.Events;

import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;

@UnitTest
@RunWith(MockitoJUnitRunner.class)
public class Ns8ContextPopulatorControllerTest {

    private static final String DASHBOARD_VIEW = "DASHBOARD";
    private static final String SUSPICIOUS_ORDERS_VIEW = "SUSPICIOUS_ORDERS";
    private static final String ORDER_RULES_VIEW = "ORDER_RULES";
    private static final String ELECTRONICS_SITE_ID = "site_id";
    private static final String APPAREL_UK_SITE_ID = "apparel-uk";

    @Spy
    @InjectMocks
    private Ns8ContextPopulatorController testObj;

    @Mock
    private SessionService sessionServiceMock;
    @Mock
    private I18NService i18nServiceMock;
    @Mock
    private Component componentMock;
    @Mock
    private WidgetModel widgetMock;
    @Mock
    private DefaultWidgetInstanceManager widgetInstanceManagerMock;
    @Mock
    private Ns8NavigationContextData ns8NavigationContextMock;

    @Before
    public void setUp() {
        when(sessionServiceMock.executeInLocalView(any(SessionExecutionBody.class))).thenAnswer(invocation -> {
            final SessionExecutionBody args = (SessionExecutionBody) invocation.getArguments()[0];
            return args.execute();
        });
        doReturn(widgetInstanceManagerMock).when(testObj).getWidgetInstanceManager();
        when(widgetInstanceManagerMock.getModel()).thenReturn(widgetMock);
        doNothing().when(widgetInstanceManagerMock).sendOutput(anyString(), anyObject());
        when(ns8NavigationContextMock.getCmsSite()).thenReturn(ELECTRONICS_SITE_ID);
    }

    @Test
    public void initialize_ShouldInitializeTheWidget() {
        testObj.initialize(componentMock);

        final InOrder inOrder = inOrder(testObj, componentMock);
        inOrder.verify(testObj).initializeNs8NavigationContext();
        inOrder.verify(componentMock).addEventListener(eq(Events.ON_CREATE), any());
    }

    @Test
    public void populateViewName_WhenMainDashboardEventIsTrueAndViewNameNotSame_ShouldPopulateTheDashBoardName() {
        doReturn(ns8NavigationContextMock).when(testObj).getNs8NavigationContextData();
        when(ns8NavigationContextMock.getViewName()).thenReturn(SUSPICIOUS_ORDERS_VIEW);

        testObj.populateViewName(true, DASHBOARD_VIEW);

        final InOrder inOrder = inOrder(testObj);
        inOrder.verify(testObj).setViewName(DASHBOARD_VIEW);
        inOrder.verify(testObj).updateNs8NavigationContext(ns8NavigationContextMock);
        inOrder.verify(testObj).setNs8NavigationContextData(ns8NavigationContextMock);
        inOrder.verify(testObj).sendNs8NavigationContextData(ns8NavigationContextMock);
    }

    @Test
    public void populateViewName_WhenMainDashboardEventIsFalseAndViewNameNotSame_ShouldNotPopulateTheDashBoardName() {
        doReturn(ns8NavigationContextMock).when(testObj).getNs8NavigationContextData();
        when(ns8NavigationContextMock.getViewName()).thenReturn(SUSPICIOUS_ORDERS_VIEW);

        testObj.populateViewName(false, DASHBOARD_VIEW);

        verify(testObj, never()).setViewName(DASHBOARD_VIEW);
        verify(testObj, never()).updateNs8NavigationContext(ns8NavigationContextMock);
        verify(testObj, never()).setNs8NavigationContextData(ns8NavigationContextMock);
        verify(testObj, never()).sendNs8NavigationContextData(ns8NavigationContextMock);
    }

    @Test
    public void populateViewName_WhenMainDashboardEventIsTrueAndViewNameSame_ShouldNotPopulateTheDashBoardName() {
        doReturn(ns8NavigationContextMock).when(testObj).getNs8NavigationContextData();
        when(ns8NavigationContextMock.getViewName()).thenReturn(DASHBOARD_VIEW);

        testObj.populateViewName(true, DASHBOARD_VIEW);

        verify(testObj, never()).setViewName(DASHBOARD_VIEW);
        verify(testObj, never()).updateNs8NavigationContext(ns8NavigationContextMock);
        verify(testObj, never()).setNs8NavigationContextData(ns8NavigationContextMock);
        verify(testObj, never()).sendNs8NavigationContextData(ns8NavigationContextMock);
    }

    @Test
    public void populateViewName_WhenNavigationContextNull_ShouldNotPopulateTheDashBoardName() {
        doReturn(null).when(testObj).getNs8NavigationContextData();

        testObj.populateViewName(true, DASHBOARD_VIEW);

        verify(testObj, never()).setViewName(DASHBOARD_VIEW);
        verify(testObj, never()).updateNs8NavigationContext(ns8NavigationContextMock);
        verify(testObj, never()).setNs8NavigationContextData(ns8NavigationContextMock);
        verify(testObj, never()).sendNs8NavigationContextData(ns8NavigationContextMock);
    }

    @Test
    public void onMainDashboardSelected_ShouldCallThePopulateViewName() {
        testObj.onMainDashboardSelected(true);

        verify(testObj).populateViewName(true, DASHBOARD_VIEW);
    }

    @Test
    public void onSuspiciousOrdersSelected_ShouldCallThePopulateViewName() {
        testObj.onSuspiciousOrdersSelected(false);

        verify(testObj).populateViewName(false, SUSPICIOUS_ORDERS_VIEW);
    }

    @Test
    public void onOrderRulesViewSelected_ShouldCallThePopulateViewName() {
        testObj.onOrderRulesViewSelected(true);

        verify(testObj).populateViewName(true, ORDER_RULES_VIEW);
    }

    @Test
    public void onCmsSiteChanged_WhenSiteNotSame_ShouldPopulateTheSiteUId() {
        doReturn(ns8NavigationContextMock).when(testObj).getNs8NavigationContextData();

        testObj.onCmsSiteChanged(APPAREL_UK_SITE_ID);

        final InOrder inOrder = inOrder(testObj);
        inOrder.verify(testObj).setCmsSiteUid(APPAREL_UK_SITE_ID);
        inOrder.verify(testObj).updateNs8NavigationContext(ns8NavigationContextMock);
        inOrder.verify(testObj).setNs8NavigationContextData(ns8NavigationContextMock);
        inOrder.verify(testObj).sendNs8NavigationContextData(ns8NavigationContextMock);
    }

    @Test
    public void onCmsSiteChanged_WhenSiteIsSame_ShouldPopulateTheSiteUId() {
        doReturn(ns8NavigationContextMock).when(testObj).getNs8NavigationContextData();

        testObj.onCmsSiteChanged(ELECTRONICS_SITE_ID);

        verify(testObj, never()).setViewName(ELECTRONICS_SITE_ID);
        verify(testObj, never()).updateNs8NavigationContext(ns8NavigationContextMock);
        verify(testObj, never()).setNs8NavigationContextData(ns8NavigationContextMock);
        verify(testObj, never()).sendNs8NavigationContextData(ns8NavigationContextMock);
    }

    @Test
    public void onCmsSiteChanged_WhenNavigationContextNull_ShouldPopulateTheSiteUId() {
        doReturn(null).when(testObj).getNs8NavigationContextData();

        testObj.onCmsSiteChanged(ELECTRONICS_SITE_ID);

        verify(testObj, never()).setViewName(ELECTRONICS_SITE_ID);
        verify(testObj, never()).updateNs8NavigationContext(ns8NavigationContextMock);
        verify(testObj, never()).setNs8NavigationContextData(ns8NavigationContextMock);
        verify(testObj, never()).sendNs8NavigationContextData(ns8NavigationContextMock);
    }
}
