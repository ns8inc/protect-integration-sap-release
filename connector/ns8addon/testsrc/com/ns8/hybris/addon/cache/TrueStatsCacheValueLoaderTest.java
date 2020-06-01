package com.ns8.hybris.addon.cache;

import de.hybris.bootstrap.annotations.UnitTest;
import de.hybris.platform.regioncache.key.CacheKey;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;

@UnitTest
@RunWith(MockitoJUnitRunner.class)
public class TrueStatsCacheValueLoaderTest {

    private static final String SCRIPT_CONTENT = "scriptContent";

    @Mock
    private CacheKey cacheKeyMock;

    @Test
    public void load_shouldRetrieveValueByGivenKey() {
        final TrueStatsCacheValueLoader cacheValueLoader = new TrueStatsCacheValueLoader(SCRIPT_CONTENT);

        final String result = cacheValueLoader.load(cacheKeyMock);

        assertThat(result).isEqualTo(SCRIPT_CONTENT);
    }
}
