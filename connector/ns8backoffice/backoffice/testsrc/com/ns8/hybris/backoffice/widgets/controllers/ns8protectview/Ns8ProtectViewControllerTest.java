package com.ns8.hybris.backoffice.widgets.controllers.ns8protectview;

import com.hybris.backoffice.widgets.notificationarea.event.NotificationEvent;
import com.hybris.cockpitng.components.Widgetslot;
import com.hybris.cockpitng.core.model.WidgetModel;
import com.hybris.cockpitng.engine.impl.DefaultWidgetInstanceManager;
import com.hybris.cockpitng.util.notifications.NotificationService;
import com.ns8.hybris.backoffice.data.context.Ns8NavigationContextData;
import com.ns8.hybris.core.model.NS8MerchantModel;
import com.ns8.hybris.core.services.api.Ns8EndpointService;
import de.hybris.bootstrap.annotations.UnitTest;
import de.hybris.platform.cms2.model.site.CMSSiteModel;
import de.hybris.platform.cms2.servicelayer.services.CMSSiteService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Collections;
import java.util.Optional;

import static io.netty.util.internal.StringUtil.EMPTY_STRING;
import static java.util.Arrays.asList;
import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;

@UnitTest
@RunWith(MockitoJUnitRunner.class)
public class Ns8ProtectViewControllerTest {

    private static final String API_KEY = "apiKey";
    private static final String ELECTRONICS_SITE_ID = "electronics";
    private static final String APPAREL_UK_SITE_ID = "apparel-uk";
    private static final String NS8_PROTECT_VIEW_VISIBLE_KEY = "ns8ProtectViewVisible";
    private static final String DASHBOARD_VIEW = "DASHBOARD";
    private static final String ACCESS_TOKEN_KEY = "accessToken";
    private static final String VIEW_NAME_KEY = "viewName";
    private static final String CLIENT_API_URL = "https://test.com";

    @Spy
    @InjectMocks
    private Ns8ProtectViewController testObj;

    @Mock
    private Ns8EndpointService ns8EndpointServiceMock;
    @Mock
    private NotificationService notificationServiceMock;
    @Mock
    private CMSSiteService cmsSiteServiceMock;
    @Mock
    private NS8MerchantModel ns8merchantMock;
    @Mock
    private CMSSiteModel cmsSite1Mock, cmsSite2Mock;
    @Mock
    private WidgetModel widgetMock;
    @Mock
    private DefaultWidgetInstanceManager widgetInstanceManagerMock;
    @Mock
    private Ns8NavigationContextData ns8NavigationContextMock;
    @Mock
    private Widgetslot widgetSlotMock;

    @Before
    public void setUp() {
        doReturn(widgetInstanceManagerMock).when(testObj).getWidgetInstanceManager();
        when(widgetInstanceManagerMock.getModel()).thenReturn(widgetMock);
        when(widgetInstanceManagerMock.getWidgetslot()).thenReturn(widgetSlotMock);
        doNothing().when(widgetInstanceManagerMock).sendOutput(anyString(), anyObject());
        when(cmsSite1Mock.getNs8Merchant()).thenReturn(ns8merchantMock);
        when(ns8merchantMock.getEnabled()).thenReturn(true);
        when(ns8merchantMock.getApiKey()).thenReturn(API_KEY);
        when(cmsSiteServiceMock.getSites()).thenReturn(asList(cmsSite1Mock, cmsSite2Mock));
        when(cmsSite1Mock.getUid()).thenReturn(ELECTRONICS_SITE_ID);
        when(cmsSite2Mock.getUid()).thenReturn(APPAREL_UK_SITE_ID);
        when(ns8NavigationContextMock.getAccessToken()).thenReturn(API_KEY);
        when(ns8NavigationContextMock.getViewName()).thenReturn(DASHBOARD_VIEW);
        when(ns8NavigationContextMock.getCmsSite()).thenReturn(APPAREL_UK_SITE_ID);
    }

    @Test
    public void onNavigationContextDataEvent_WhenViewNameAndTokenEmpty_ShouldSetViewNotVisible() {
        doNothing().when(testObj).updateProtectViewContext(ns8NavigationContextMock);
        when(ns8NavigationContextMock.getAccessToken()).thenReturn(EMPTY_STRING);
        when(ns8NavigationContextMock.getViewName()).thenReturn(EMPTY_STRING);

        testObj.onNavigationContextDataEvent(ns8NavigationContextMock);

        final InOrder inOrder = inOrder(testObj, widgetMock, widgetSlotMock);
        inOrder.verify(testObj).updateProtectViewContext(ns8NavigationContextMock);
        inOrder.verify(widgetMock).setValue(NS8_PROTECT_VIEW_VISIBLE_KEY, false);
        inOrder.verify(widgetSlotMock).updateView();
    }

    @Test
    public void onNavigationContextDataEvent_WhenViewNameAndTokenPopulated_ShouldSetViewVisibleAndSetValuesInModel() {
        doNothing().when(testObj).updateProtectViewContext(ns8NavigationContextMock);

        testObj.onNavigationContextDataEvent(ns8NavigationContextMock);

        final InOrder inOrder = inOrder(testObj, widgetMock, widgetSlotMock);
        inOrder.verify(testObj).updateProtectViewContext(ns8NavigationContextMock);
        inOrder.verify(widgetMock).setValue(NS8_PROTECT_VIEW_VISIBLE_KEY, true);
        inOrder.verify(widgetMock).setValue(VIEW_NAME_KEY, DASHBOARD_VIEW);
        inOrder.verify(widgetMock).setValue(ACCESS_TOKEN_KEY, API_KEY);
        inOrder.verify(widgetSlotMock).updateView();
    }

    @Test
    public void getSelectedCmsSite_WhenNoSites_ShouldReturnOptionalEmpty() {
        when(cmsSiteServiceMock.getSites()).thenReturn(Collections.emptyList());

        final Optional<CMSSiteModel> result = testObj.getSelectedCmsSite(APPAREL_UK_SITE_ID);

        assertThat(result).isEmpty();
    }

    @Test
    public void getSelectedCmsSite_WhenNoSiteMatch_ShouldReturnOptionalEmpty() {
        when(cmsSiteServiceMock.getSites()).thenReturn(asList(cmsSite1Mock));

        final Optional<CMSSiteModel> result = testObj.getSelectedCmsSite(APPAREL_UK_SITE_ID);

        assertThat(result).isEmpty();
    }

    @Test
    public void getSelectedCmsSite_WhenSiteMatch_ShouldReturnOptionalSite() {
        when(cmsSiteServiceMock.getSites()).thenReturn(asList(cmsSite1Mock, cmsSite2Mock));

        final Optional<CMSSiteModel> result = testObj.getSelectedCmsSite(APPAREL_UK_SITE_ID);

        assertThat(result).isNotEmpty();
        assertThat(result.get().getUid()).isEqualTo(APPAREL_UK_SITE_ID);
    }

    @Test
    public void getClientApiUrl_ShouldReturnClientApiUrl() {
        when(ns8EndpointServiceMock.getBaseClientURL()).thenReturn(CLIENT_API_URL);

        final String result = testObj.getClientApiUrl();

        assertThat(result).isEqualTo(CLIENT_API_URL);
    }

    @Test
    public void updateProtectViewContext_WhenContextNull_ShouldDoNothing() {
        testObj.updateProtectViewContext(null);

        verify(ns8NavigationContextMock, never()).setCmsSite(any());
        verify(ns8NavigationContextMock, never()).setAccessToken(any());
        verify(ns8NavigationContextMock, never()).setViewName(any());
    }

    @Test
    public void updateProtectViewContext_WhenViewNameBlank_ShouldDoNothing() {
        when(ns8NavigationContextMock.getViewName()).thenReturn(EMPTY_STRING);

        testObj.updateProtectViewContext(ns8NavigationContextMock);

        verify(ns8NavigationContextMock).setCmsSite(null);
        verify(ns8NavigationContextMock).setAccessToken(null);
        verify(ns8NavigationContextMock).setViewName(null);
    }

    @Test
    public void updateProtectViewContext_WhenSiteIdBlank_ShouldSetNullValues() {
        when(ns8NavigationContextMock.getCmsSite()).thenReturn(EMPTY_STRING);

        testObj.updateProtectViewContext(ns8NavigationContextMock);

        verify(ns8NavigationContextMock).setCmsSite(null);
        verify(ns8NavigationContextMock).setAccessToken(null);
        verify(ns8NavigationContextMock).setViewName(null);
    }

    @Test
    public void updateProtectViewContext_WhenValuesPopulatedButSiteFound_ShouldSetNullValues() {
        when(ns8NavigationContextMock.getCmsSite()).thenReturn("random");

        testObj.updateProtectViewContext(ns8NavigationContextMock);

        verify(ns8NavigationContextMock).setCmsSite(null);
        verify(ns8NavigationContextMock).setAccessToken(null);
        verify(ns8NavigationContextMock).setViewName(null);
    }

    @Test
    public void updateProtectViewContext_WhenValuesPopulatedButNoMerchant_ShouldSetNullAccessTokenAndNotifyError() {
        when(cmsSite1Mock.getNs8Merchant()).thenReturn(null);

        testObj.updateProtectViewContext(ns8NavigationContextMock);

        verify(ns8NavigationContextMock).setAccessToken(null);
        verify(notificationServiceMock).notifyUser(eq(widgetInstanceManagerMock), eq("JustMessage"),
                eq(NotificationEvent.Level.FAILURE), anyCollection());
    }

    @Test
    public void updateProtectViewContext_WhenValuesPopulatedButMerchantNotActive_ShouldSetNullAccessTokenAndNotifyError() {
        when(ns8merchantMock.getEnabled()).thenReturn(false);

        testObj.updateProtectViewContext(ns8NavigationContextMock);

        verify(ns8NavigationContextMock).setAccessToken(null);
        verify(notificationServiceMock).notifyUser(eq(widgetInstanceManagerMock), eq("JustMessage"),
                eq(NotificationEvent.Level.FAILURE), anyCollection());
    }

    @Test
    public void updateProtectViewContext_WhenValuesPopulatedButMerchantWithoutApiKey_ShouldSetNullAccessTokenAndNotifyError() {
        when(ns8merchantMock.getApiKey()).thenReturn(null);

        testObj.updateProtectViewContext(ns8NavigationContextMock);

        verify(ns8NavigationContextMock).setAccessToken(null);
        verify(notificationServiceMock).notifyUser(eq(widgetInstanceManagerMock), eq("JustMessage"),
                eq(NotificationEvent.Level.FAILURE), anyCollection());
    }

    @Test
    public void updateProtectViewContext_WhenValuesPopulatedAndMerchantValid_ShouldSetTheAccessToken() {
        when(ns8NavigationContextMock.getCmsSite()).thenReturn(ELECTRONICS_SITE_ID);

        testObj.updateProtectViewContext(ns8NavigationContextMock);

        verify(ns8NavigationContextMock).setAccessToken(API_KEY);
    }
}
