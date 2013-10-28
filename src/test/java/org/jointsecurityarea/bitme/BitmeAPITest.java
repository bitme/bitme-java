package org.jointsecurityarea.bitme;

import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runners.JUnit4;
import org.skyscreamer.jsonassert.JSONAssert;

public class BitmeAPITest {
    private BitmeAPI api;
    @Before
    public void setUp() throws Exception {
        // Read APIKEY and APISECRET from the src/test dir

        this.api = new BitmeAPI(BitmeAPIKeys.API_KEY, BitmeAPIKeys.API_SECRET);
    }

    @Test
    public void testVerify_credentials() throws Exception {
        JSONObject object = this.api.verify_credentials();
        // Validate
        JSONObject expected = new JSONObject("{}");
        JSONAssert.assertEquals(expected, object, false);
    }
}
