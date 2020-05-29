package com.ns8.hybris.addon.facades;

import com.ns8.hybris.addon.data.Ns8OrderVerificationData;
import com.ns8.hybris.core.data.Ns8OrderVerificationResponse;

/**
 * Exposes a method to retrieve a verification template for an order
 */
public interface Ns8OrderVerificationTemplateFacade {

    /**
     * Retrieves a verification template for an order
     *
     * @param verificationData the verificationData containing the information from the page
     * @return Verification template for the given order in HTML format
     */
    String getVerificationTemplate(Ns8OrderVerificationData verificationData);

    /**
     * Posts information about the verification form contained in a template
     * and retrieves a template in the shape of an HTML code or a URL to
     * which the user will be redirected
     *
     * @param form      the form containing the information from the page
     * @return Verification template for the given order in HTML format or redirection URL
     */
    Ns8OrderVerificationResponse sendVerification(Ns8OrderVerificationData form);

}
