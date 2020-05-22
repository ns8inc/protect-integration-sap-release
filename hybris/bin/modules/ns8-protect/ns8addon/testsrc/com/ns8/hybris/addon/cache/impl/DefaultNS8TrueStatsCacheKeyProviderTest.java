package com.ns8.hybris.addon.cache.impl;

import com.ns8.hybris.addon.cache.impl.DefaultNS8TrueStatsCacheKeyProvider.NS8TrueStatsCacheKey;
import de.hybris.bootstrap.annotations.UnitTest;
import de.hybris.platform.basecommerce.model.site.BaseSiteModel;
import de.hybris.platform.regioncache.key.CacheUnitValueType;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

@UnitTest
@RunWith(MockitoJUnitRunner.class)
public class DefaultNS8TrueStatsCacheKeyProviderTest {

    private static final String NS8_TRUESTATS_UNIT_CODE = "__NS8_TRUESTATS_CACHE__";
    private static final String BASE_SITE_MODEL_UID = "baseSiteModelUid";

    @Spy
    private DefaultNS8TrueStatsCacheKeyProvider testObj;

    @Mock
    private BaseSiteModel baseSiteModelMock;

    @Before
    public void setUp() {
        doReturn("tenantId").when(testObj).getCurrentTenantId();
    }

    @Test
    public void getKey_shouldCreateKeyWithExpectedValuesUsingGivenBaseSiteModel() {

        when(baseSiteModelMock.getUid()).thenReturn(BASE_SITE_MODEL_UID);

        final NS8TrueStatsCacheKey result = (NS8TrueStatsCacheKey) testObj.getKey(baseSiteModelMock);

        assertThat(result.getTypeCode()).isEqualTo(NS8_TRUESTATS_UNIT_CODE);
        assertThat(result.toString()).contains(BASE_SITE_MODEL_UID);
        assertThat(result.getCacheValueType()).isEqualTo(CacheUnitValueType.SERIALIZABLE);
    }
}
