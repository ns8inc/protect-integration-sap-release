package com.ns8.hybris.addon.controllers.pages;

import com.ns8.hybris.addon.controllers.NS8TrueStatsController;
import com.ns8.hybris.addon.facades.NS8TrueStatsFacade;
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
public class NS8TrueStatsControllerTest {

    private static final String JAVASCRIPT_CONTENT = "javascriptContent";

    @InjectMocks
    private NS8TrueStatsController testObj;

    @Mock
    private NS8TrueStatsFacade trueStatsFacadeMock;

    @Test
    public void fetchTrueStatsScript() {
        when(trueStatsFacadeMock.fetchTrueStatsContent()).thenReturn(JAVASCRIPT_CONTENT);

        final String result = testObj.fetchTrueStatsScript();

        assertThat(result).isEqualTo(JAVASCRIPT_CONTENT);
    }
}
