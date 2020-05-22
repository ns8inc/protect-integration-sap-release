package com.ns8.hybris.addon.services.impl;

import com.ns8.hybris.addon.cache.NS8TrueStatsCacheService;
import com.ns8.hybris.core.model.NS8MerchantModel;
import com.ns8.hybris.core.services.api.NS8APIService;
import de.hybris.bootstrap.annotations.UnitTest;
import de.hybris.platform.cms2.model.site.CMSSiteModel;
import de.hybris.platform.cms2.servicelayer.services.CMSSiteService;
import de.hybris.platform.regioncache.key.CacheKey;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

@UnitTest
@RunWith(MockitoJUnitRunner.class)
public class DefaultNS8TrueStatsServiceTest {

    private static final String JAVASCRIPT_CONTENT = "javascriptContent";

    @InjectMocks
    private DefaultNS8TrueStatsService testObj;

    @Mock
    private CMSSiteService cmsSiteServiceMock;
    @Mock
    private NS8APIService ns8APIServiceMock;
    @Mock
    private NS8TrueStatsCacheService ns8TrueStatsCacheServiceMock;
    @Mock
    private CMSSiteModel cmsSiteModelMock;
    @Mock
    private NS8MerchantModel ns8MerchantModelMock;
    @Mock
    private CacheKey cacheKeyMock;

    @Before
    public void setUp() {
        when(cmsSiteServiceMock.getCurrentSite()).thenReturn(cmsSiteModelMock);
        when(cmsSiteModelMock.getNs8Merchant()).thenReturn(ns8MerchantModelMock);
        when(ns8APIServiceMock.fetchTrueStatsScript(ns8MerchantModelMock)).thenReturn(JAVASCRIPT_CONTENT);
        when(ns8TrueStatsCacheServiceMock.getKey(cmsSiteModelMock)).thenReturn(cacheKeyMock);
        when(ns8TrueStatsCacheServiceMock.useCache(cmsSiteModelMock)).thenReturn(true);
    }

    @Test
    public void fetchTrueStatsContent_GivenCurrentCMSSiteMerchant_ShouldReturnJavascriptContent_WhenCachingIsDisabled() {
        when(ns8TrueStatsCacheServiceMock.useCache(cmsSiteModelMock)).thenReturn(false);

        final String result = testObj.fetchTrueStatsContent();

        assertThat(result).isEqualTo(JAVASCRIPT_CONTENT);
    }

    @Test
    public void fetchTrueStatsContent_GivenCurrentCMSSiteMerchant_ShouldReturnJavascriptContent_FromCacheIfHit() {
        when(ns8TrueStatsCacheServiceMock.get(cacheKeyMock)).thenReturn(JAVASCRIPT_CONTENT);

        final String result = testObj.fetchTrueStatsContent();

        assertThat(result).isEqualTo(JAVASCRIPT_CONTENT);
        verify(ns8APIServiceMock, never()).fetchTrueStatsScript(any(NS8MerchantModel.class));
    }

    @Test
    public void fetchTrueStatsContent_GivenCurrentCMSSiteMerchant_ShouldReturnJavascriptContent_AndStoreInCacheIfMiss() {
        when(ns8TrueStatsCacheServiceMock.get(cacheKeyMock)).thenReturn(null);

        final String result = testObj.fetchTrueStatsContent();

        verify(ns8TrueStatsCacheServiceMock).put(cacheKeyMock, JAVASCRIPT_CONTENT);
        assertThat(result).isEqualTo(JAVASCRIPT_CONTENT);
    }


}
