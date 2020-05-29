package com.ns8.hybris.addon.converters.populators;

import com.ns8.hybris.addon.data.Ns8OrderVerificationData;
import com.ns8.hybris.addon.forms.Ns8OrderVerificationForm;
import de.hybris.bootstrap.annotations.UnitTest;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

@UnitTest
public class DefaultNs8OrderVerificationDataPopulatorTest {

    private DefaultNs8OrderVerificationDataPopulator testObj = new DefaultNs8OrderVerificationDataPopulator();

    private Ns8OrderVerificationForm source = new Ns8OrderVerificationForm();
    private Ns8OrderVerificationData target = new Ns8OrderVerificationData();

    @Test
    public void populate_ShouldPopulateFields() {
        source.setOrderId("orderId");
        source.setToken("token");
        source.setVerificationId("verificationId");
        source.setPhone("phone");
        source.setCode("code");

        testObj.populate(source, target);

        assertEquals("orderId", target.getOrderId());
        assertEquals("token", target.getToken());
        assertEquals("verificationId", target.getVerificationId());
        assertEquals("phone", target.getPhone());
        assertEquals("code", target.getCode());
    }
}