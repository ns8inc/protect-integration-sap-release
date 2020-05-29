package com.ns8.hybris.addon.controllers.pages;

import com.ns8.hybris.addon.controllers.Ns8TrueStatsController;
import com.ns8.hybris.addon.facades.Ns8MerchantFacade;
import com.ns8.hybris.addon.facades.Ns8TrueStatsFacade;
import de.hybris.bootstrap.annotations.UnitTest;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.OK;

@UnitTest
@RunWith(MockitoJUnitRunner.class)
public class Ns8TrueStatsControllerTest {

    private static final String JAVASCRIPT_CONTENT = "javascriptContent";

    @InjectMocks
    private Ns8TrueStatsController testObj;

    @Mock
    private Ns8TrueStatsFacade trueStatsFacadeMock;
    @Mock
    private Ns8MerchantFacade ns8MerchantFacadeMock;

    @Test
    public void fetchTrueStatsScript_WhenMerchantActive_ShouldReturnJavaScriptContent() {
        when(ns8MerchantFacadeMock.isMerchantActive()).thenReturn(Boolean.TRUE);
        when(trueStatsFacadeMock.fetchTrueStatsContent()).thenReturn(JAVASCRIPT_CONTENT);

        final ResponseEntity result = testObj.fetchTrueStatsScript();

        assertThat(result.getBody()).isEqualTo(JAVASCRIPT_CONTENT);
        assertThat(result.getStatusCode()).isEqualTo(OK);
    }

    @Test
    public void fetchTrueStatsScript_WhenMerchantIsNotActive_ShouldReturnBadRequest() {
        when(ns8MerchantFacadeMock.isMerchantActive()).thenReturn(Boolean.FALSE);

        final ResponseEntity result = testObj.fetchTrueStatsScript();

        assertThat(result.getBody()).isNull();
        assertThat(result.getStatusCode()).isEqualTo(BAD_REQUEST);
    }
}
