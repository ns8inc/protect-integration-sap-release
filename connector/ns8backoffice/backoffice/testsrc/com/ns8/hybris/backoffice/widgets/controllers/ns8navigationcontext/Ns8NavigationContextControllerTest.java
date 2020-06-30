package com.ns8.hybris.backoffice.widgets.controllers.ns8navigationcontext;

import com.hybris.cockpitng.core.model.WidgetModel;
import com.hybris.cockpitng.engine.impl.DefaultWidgetInstanceManager;
import com.ns8.hybris.backoffice.data.context.Ns8NavigationContextData;
import de.hybris.bootstrap.annotations.UnitTest;
import de.hybris.platform.cms2.model.site.CMSSiteModel;
import de.hybris.platform.cms2.servicelayer.services.CMSSiteService;
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
import org.mockito.internal.util.reflection.Whitebox;
import org.mockito.runners.MockitoJUnitRunner;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.event.SelectEvent;
import org.zkoss.zul.Combobox;
import org.zkoss.zul.Comboitem;
import org.zkoss.zul.ListModelList;

import java.util.Collections;

import static java.util.Arrays.asList;
import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;

@UnitTest
@RunWith(MockitoJUnitRunner.class)
public class Ns8NavigationContextControllerTest {

    private static final String ELECTRONICS_SITE_ID = "electronics";
    private static final String APPAREL_UK_SITE_ID = "apparel-uk";

    @Spy
    @InjectMocks
    private Ns8NavigationContextController testObj;

    @Mock
    private SessionService sessionServiceMock;
    @Mock
    private I18NService i18nServiceMock;
    @Mock
    private CMSSiteService cmsSiteServiceMock;
    @Mock
    private Component componentMock;
    @Mock
    private WidgetModel widgetMock;
    @Mock
    private DefaultWidgetInstanceManager widgetInstanceManagerMock;
    @Mock
    private Ns8NavigationContextData ns8NavigationContextMock;
    @Mock
    private Combobox cmsSiteSelectorMock;
    @Mock
    private SelectEvent<Comboitem, String> siteChangedEventMock;
    @Mock
    private Comboitem comboItemMock;
    @Mock
    private CMSSiteModel cmsSite1Mock, cmsSite2Mock;
    private ListModelList listModelList;

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
        when(siteChangedEventMock.getReference()).thenReturn(comboItemMock);
        when(comboItemMock.getValue()).thenReturn(APPAREL_UK_SITE_ID);
        when(cmsSiteServiceMock.getSites()).thenReturn(asList(cmsSite1Mock, cmsSite2Mock));
        when(cmsSite1Mock.getUid()).thenReturn(ELECTRONICS_SITE_ID);
        when(cmsSite2Mock.getUid()).thenReturn(APPAREL_UK_SITE_ID);
        listModelList = new ListModelList(asList(cmsSite1Mock, cmsSite2Mock));
        Whitebox.setInternalState(testObj, "cmsSiteModels", listModelList);
        Whitebox.setInternalState(testObj, "cmsSiteSelector", cmsSiteSelectorMock);
    }

    @Test
    public void initialize_ShouldInitializeTheWidget() {
        testObj.initialize(componentMock);

        final InOrder inOrder = inOrder(testObj, componentMock);
        inOrder.verify(testObj).initializeSelectors();
        inOrder.verify(componentMock).addEventListener(eq(Events.ON_CREATE), any());
    }

    @Test
    public void onCmsSiteChanged_WhenContextNotNullAndSiteChanged_ShouldUpdateContextAndSelector() {
        doReturn(ns8NavigationContextMock).when(testObj).getNs8NavigationContext();
        when(ns8NavigationContextMock.getCmsSite()).thenReturn(ELECTRONICS_SITE_ID);

        testObj.onCmsSiteChanged(siteChangedEventMock);

        final InOrder inOrder = inOrder(ns8NavigationContextMock, testObj);
        inOrder.verify(ns8NavigationContextMock).setCmsSite(APPAREL_UK_SITE_ID);
        inOrder.verify(testObj).updateSelectors(ns8NavigationContextMock);
        inOrder.verify(testObj).sendCmsSiteUId(ns8NavigationContextMock);
    }

    @Test
    public void onCmsSiteChanged_WhenContextNullAndSiteChanged_ShouldNotUpdateContextAndSelector() {
        doReturn(null).when(testObj).getNs8NavigationContext();

        testObj.onCmsSiteChanged(siteChangedEventMock);

        verify(ns8NavigationContextMock, never()).setCmsSite(APPAREL_UK_SITE_ID);
        verify(testObj, never()).updateSelectors(ns8NavigationContextMock);
        verify(testObj, never()).sendCmsSiteUId(ns8NavigationContextMock);
    }

    @Test
    public void onCmsSiteChanged_WhenContextNotNullAndSameSite_ShouldNotUpdateContextAndSelector() {
        doReturn(ns8NavigationContextMock).when(testObj).getNs8NavigationContext();
        when(ns8NavigationContextMock.getCmsSite()).thenReturn(APPAREL_UK_SITE_ID);

        testObj.onCmsSiteChanged(siteChangedEventMock);

        verify(ns8NavigationContextMock, never()).setCmsSite(APPAREL_UK_SITE_ID);
        verify(testObj, never()).updateSelectors(ns8NavigationContextMock);
        verify(testObj, never()).sendCmsSiteUId(ns8NavigationContextMock);
    }

    @Test
    public void updateSelectors_ShouldTriggerTheUpdateSiteForTheContext() {
        testObj.updateSelectors(ns8NavigationContextMock);

        final InOrder inOrder = inOrder(sessionServiceMock, i18nServiceMock, testObj);
        inOrder.verify(sessionServiceMock).executeInLocalView(any(SessionExecutionBody.class));
        inOrder.verify(i18nServiceMock).setLocalizationFallbackEnabled(true);
        inOrder.verify(testObj).updateCmsSite(ns8NavigationContextMock);
    }

    @Test
    public void updateCmsSite_WhenSystemSitesAndListSitesAreDifferentButNotSelection_ShouldJustUpdateTheComboList() {
        Whitebox.setInternalState(testObj, "cmsSiteModels", new ListModelList(Collections.singletonList(cmsSite1Mock)));

        testObj.updateCmsSite(ns8NavigationContextMock);

        assertThat(testObj.getCmsSiteModels()).isEqualTo(asList(cmsSite1Mock, cmsSite2Mock));
        verify(testObj, never()).sendCmsSiteUId(ns8NavigationContextMock);
    }

    @Test
    public void updateCmsSite_WhenSystemSitesAndListSitesAreSameButNotSelection_ShouldDoNothing() {
        testObj.updateCmsSite(ns8NavigationContextMock);

        assertThat(testObj.getCmsSiteModels()).isEqualTo(asList(cmsSite1Mock, cmsSite2Mock));
        verify(testObj, never()).sendCmsSiteUId(ns8NavigationContextMock);
    }

    @Test
    public void updateCmsSite_WhenSystemSitesAndListSitesAreNotSameAndSiteSelectedNotContainedAfterUpdate_ShouldUpdateTheListSetTheSelectedSiteAndActivateSelector() {
        when(cmsSiteServiceMock.getSites()).thenReturn(Collections.singletonList(cmsSite1Mock));
        listModelList.setSelection(Collections.singletonList(cmsSite2Mock));

        testObj.updateCmsSite(ns8NavigationContextMock);

        assertThat(testObj.getCmsSiteModels()).isEqualTo(asList(cmsSite1Mock));
        verify(ns8NavigationContextMock).setCmsSite(APPAREL_UK_SITE_ID);
        verify(cmsSiteSelectorMock).setDisabled(false);
    }

    @Test
    public void updateCmsSite_WhenSystemSitesAreNotAvailable_ShouldUpdateTheComboListToEmptyDeleteTheSelectionAndDisableTheSelector() {
        when(cmsSiteServiceMock.getSites()).thenReturn(Collections.emptyList());
        Whitebox.setInternalState(testObj, "cmsSiteModels", new ListModelList(0));

        testObj.updateCmsSite(ns8NavigationContextMock);

        assertThat(testObj.getCmsSiteModels()).isEqualTo(Collections.emptyList());
        verify(cmsSiteSelectorMock).setDisabled(true);
    }
}
