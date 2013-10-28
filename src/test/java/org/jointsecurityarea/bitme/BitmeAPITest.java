package org.jointsecurityarea.bitme;

import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.skyscreamer.jsonassert.JSONAssert;
import sun.jvm.hotspot.utilities.Assert;

public class BitmeAPITest {
    private BitmeAPI api;
    @Before
    public void setUp() throws Exception {
        this.api = new BitmeAPI(BitmeAPIKeys.API_KEY, BitmeAPIKeys.API_SECRET);
    }

    @Test
    public void testVerify_credentials() throws Exception {
        JSONObject object = this.api.verify_credentials();
        // Validate
        JSONObject expected = new JSONObject("{}");
        JSONAssert.assertEquals(expected, object, false);
    }
    @Test
    public void testAccounts() throws Exception {
        JSONObject object = this.api.accounts();
        // Validate
        Assert.that(object.has("accounts"), "Accounts not present");
    }
    @Test
    public void testBitcoinAddress() throws Exception {
        JSONObject object = this.api.bitcoin_address();
        // Validate
        Assert.that(object.has("address"), "Address not present");
    }
    @Test
    public void testOpenOrders() throws Exception {
        JSONObject object = this.api.open_orders();
        // Validate
        Assert.that(object.has("orders"), "Orders not present");
    }
    @Test
    public void testGetTransactions() throws Exception {
        JSONObject object = this.api.get_transactions();
        // Validate
        Assert.that(object.has("transactions"), "Orders not present");
    }
}