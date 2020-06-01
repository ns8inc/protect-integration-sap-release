package com.ns8.hybris.addon.services.impl;

import com.ns8.hybris.addon.cache.Ns8TrueStatsCacheKeyProvider;
import com.ns8.hybris.addon.cache.TrueStatsCacheValueLoader;
import de.hybris.bootstrap.annotations.UnitTest;
import de.hybris.platform.basecommerce.model.site.BaseSiteModel;
import de.hybris.platform.regioncache.CacheController;
import de.hybris.platform.regioncache.key.CacheKey;
import de.hybris.platform.servicelayer.config.ConfigurationService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.*;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@UnitTest
@RunWith(MockitoJUnitRunner.class)
public class DefaultNs8TrueStatsCacheServiceTest {

    private static final String SCRIPT_CONTENT = "scriptContent";
    private static final String NS8_TRUESTATS_CACHE_ENABLED_KEY = "ns8.truestats.cache.enabled";

    @InjectMocks
    private DefaultNs8TrueStatsCacheService testObj;

    @Mock
    private Ns8TrueStatsCacheKeyProvider cacheKeyProviderMock;
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ConfigurationService configurationServiceMock;
    @Mock
    private CacheController cacheControllerMock;
    @Mock
    private BaseSiteModel baseSiteModelMock;
    @Mock
    private CacheKey cacheKeyMock;
    @Captor
    private ArgumentCaptor<TrueStatsCacheValueLoader> trueStatsCacheValueLoaderCaptor;

    @Before
    public void setUp() {
        when(configurationServiceMock.getConfiguration().getBoolean(NS8_TRUESTATS_CACHE_ENABLED_KEY, false)).thenReturn(true);
    }

    @Test
    public void getKey_shouldReturnKeyFromCacheProviderUsingBaseSiteModelAndScriptContent() {
        when(cacheKeyProviderMock.getKey(baseSiteModelMock)).thenReturn(cacheKeyMock);

        final CacheKey result = testObj.getKey(baseSiteModelMock);

        assertThat(result).isEqualTo(cacheKeyMock);
    }

    @Test
    public void get_shouldReturnCachedScriptFromCacheControllerByGivenKey() {
        when(cacheControllerMock.get(cacheKeyMock)).thenReturn(SCRIPT_CONTENT);

        final String result = testObj.get(cacheKeyMock);

        assertThat(result).isEqualTo(SCRIPT_CONTENT);
    }

    @Test
    public void put_shouldStoreANewVersionOfScriptContentForGivenKey() {

        testObj.put(cacheKeyMock, SCRIPT_CONTENT);

        verify(cacheControllerMock).getWithLoader(eq(cacheKeyMock), trueStatsCacheValueLoaderCaptor.capture());

        final TrueStatsCacheValueLoader value = trueStatsCacheValueLoaderCaptor.getValue();
        assertThat(value.load(cacheKeyMock)).isEqualTo(SCRIPT_CONTENT);
    }

    @Test
    public void useCache_shouldReturnTRUE_whenInternalCacheIsActiveAndBaseSiteModelIsNotNull() {

        final boolean result = testObj.useCache(baseSiteModelMock);

        assertThat(result).isTrue();
    }

    @Test
    public void useCache_shouldReturnFALSE_whenInternalCacheIsActiveAndBaseSiteModelIsNull() {

        final boolean result = testObj.useCache(null);

        assertThat(result).isFalse();
    }

    @Test
    public void useCache_shouldReturnFALSE_whenInternalCacheIsDisabledAndBaseSiteModelIsNotNull() {
        when(configurationServiceMock.getConfiguration().getBoolean(NS8_TRUESTATS_CACHE_ENABLED_KEY, false)).thenReturn(false);

        final boolean result = testObj.useCache(baseSiteModelMock);

        assertThat(result).isFalse();
    }

    @Test
    public void useCache_shouldReturnFALSE_whenInternalCacheIsDisabledAndBaseSiteModelIsNull() {
        when(configurationServiceMock.getConfiguration().getBoolean(NS8_TRUESTATS_CACHE_ENABLED_KEY, false)).thenReturn(false);

        final boolean result = testObj.useCache(null);

        assertThat(result).isFalse();
    }
}
