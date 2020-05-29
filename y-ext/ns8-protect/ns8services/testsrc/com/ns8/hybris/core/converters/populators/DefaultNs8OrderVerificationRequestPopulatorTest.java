package com.ns8.hybris.core.converters.populators;

import com.ns8.hybris.addon.data.Ns8OrderVerificationData;
import com.ns8.hybris.core.data.Ns8OrderVerificationRequest;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class DefaultNs8OrderVerificationRequestPopulatorTest {

    private DefaultNs8OrderVerificationRequestPopulator testObj = new DefaultNs8OrderVerificationRequestPopulator();

    private Ns8OrderVerificationData source = new Ns8OrderVerificationData();
    private Ns8OrderVerificationRequest target = new Ns8OrderVerificationRequest();

    @Test
    public void populate_ShouldPopulateFields() {
        source.setOrderId("orderId");
        source.setToken("token");
        source.setVerificationId("verificationId");
        source.setPhone("phone");
        source.setCode("code");
        source.setReturnURI("/return-uri");
        source.setTemplate("orders-confirm");

        testObj.populate(source, target);

        assertEquals("orderId", target.getOrderId());
        assertEquals("token", target.getToken());
        assertEquals("verificationId", target.getVerificationId());
        assertEquals("phone", target.getPhone());
        assertEquals("code", target.getCode());
        assertEquals("/return-uri", target.getReturnURI());
        assertEquals("orders-confirm", target.getView());
    }
}