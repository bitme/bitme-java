package org.jointsecurityarea.bitme;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.skyscreamer.jsonassert.JSONAssert;
import org.junit.Assert;

import java.util.logging.Level;
import java.util.logging.Logger;

public class BitmeAPITest {
    Logger logger = Logger.getLogger("BitmeAPITest");
    private BitmeAPI api;
    @Before
    public void setUp() throws Exception {
        this.api = new BitmeAPI(BitmeAPIKeys.API_KEY, BitmeAPIKeys.API_SECRET);
        logger.setLevel(Level.ALL);
    }

    @Test
    public void testVerify_credentials() throws Exception {
        JSONObject object = this.api.verify_credentials();
        // Validate
        Assert.assertNotNull(object);
        JSONObject expected = new JSONObject("{}");
        JSONAssert.assertEquals(expected, object, false);
    }
    @Test
    public void testAccounts() throws Exception {
        JSONObject object = this.api.accounts();
        // Validate
        Assert.assertNotNull(object);
        Assert.assertTrue(object.has("accounts"));
    }
    @Test
    public void testBitcoinAddress() throws Exception {
        JSONObject object = this.api.bitcoin_address();
        // Validate
        Assert.assertNotNull(object);
        Assert.assertTrue(object.has("address"));
    }
    @Test
    public void testOpenOrders() throws Exception {
        JSONObject object = this.api.open_orders();
        // Validate
        Assert.assertNotNull(object);
        Assert.assertTrue(object.has("orders"));
    }
    @Test
    public void testGetTransactions() throws Exception {
        JSONObject object = this.api.get_transactions();
        // Validate
        Assert.assertNotNull(object);
        Assert.assertTrue(object.has("transactions"));
    }
    @Test
    public void testGetTransaction() throws Exception {
        JSONObject object = this.api.get_transactions();
        Assert.assertNotNull(object);
        Assert.assertTrue(object.has("transactions"));
        JSONArray txs = object.getJSONArray("transactions");
        JSONObject member = txs.getJSONObject(0);
        JSONObject tx = this.api.get_transaction(member.getString("id"));
        // Validate
        Assert.assertNotNull(tx);
        Assert.assertTrue(tx.has("transaction"));
    }

    /* NOTE: You need at least 0.001 BTC available in your account for this to work */
    @Test
    public void testCreateQueryCancelOrders() throws Exception {
        JSONObject object = this.api.create_order("BID", 1, "0.001");
        Assert.assertNotNull(object);
        logger.info("Create Order returned:" + object.toString());
        Assert.assertTrue(object.has("order"));
        String order_uuid = object.getJSONObject("order").getString("uuid");

        // Get
        object = this.api.get_order(order_uuid);
        Assert.assertNotNull(object);
        Assert.assertTrue(object.has("order"));

        // Cancel
        object = this.api.cancel_order(order_uuid);
        Assert.assertNotNull(object);
        logger.info("Cancel Order returned:" + object.toString());
        Assert.assertTrue(object.has("order"));
    }
    @Test
    public void testGetOrderbook() throws Exception {
        JSONObject object = this.api.get_orderbook();
        Assert.assertNotNull(object);
        logger.info("Orderbook returned:" + object.toString());
        // Validate
        Assert.assertTrue(object.has("orderbook"));
    }
    @Test
    public void testGetCompatOrderbook() throws Exception {
        JSONObject object = this.api.get_compat_orderbook();
        Assert.assertNotNull(object);
        logger.info("Orderbook returned:" + object.toString());
        // Validate
        Assert.assertTrue(object.has("asks"));
    }
    @Test
    public void testGetCompatTrades() throws Exception {
        JSONObject object = this.api.get_compat_trades();
        Assert.assertNotNull(object);
        logger.info("Trades returned:" + object.toString());
        // Validate
        Assert.assertTrue(object.has("array"));
    }
}