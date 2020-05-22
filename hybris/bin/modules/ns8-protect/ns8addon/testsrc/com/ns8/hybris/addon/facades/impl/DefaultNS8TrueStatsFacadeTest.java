package com.ns8.hybris.addon.facades.impl;

import com.ns8.hybris.addon.services.NS8TrueStatsService;
import de.hybris.bootstrap.annotations.UnitTest;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@UnitTest
@RunWith(MockitoJUnitRunner.class)
public class DefaultNS8TrueStatsFacadeTest {

    private static final String JAVASCRIPT_CONTENT = "javascriptContent";

    @InjectMocks
    private DefaultNS8TrueStatsFacade testObj;

    @Mock
    private NS8TrueStatsService ns8TrueStatsServiceMock;

    @Test
    public void fetchTrueStatsContent_ShouldRetrieveJavascriptContentAndReturnIt() {
        when(ns8TrueStatsServiceMock.fetchTrueStatsContent()).thenReturn(JAVASCRIPT_CONTENT);

        final String result = testObj.fetchTrueStatsContent();

        assertThat(result).isEqualTo(JAVASCRIPT_CONTENT);
    }
}
