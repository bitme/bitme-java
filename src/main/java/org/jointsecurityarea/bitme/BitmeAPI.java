package org.jointsecurityarea.bitme;

import ch.boye.httpclientandroidlib.HttpEntity;
import ch.boye.httpclientandroidlib.HttpResponse;
import ch.boye.httpclientandroidlib.NameValuePair;
import ch.boye.httpclientandroidlib.client.HttpClient;
import ch.boye.httpclientandroidlib.client.entity.UrlEncodedFormEntity;
import ch.boye.httpclientandroidlib.client.methods.HttpGet;
import ch.boye.httpclientandroidlib.client.methods.HttpPost;
import ch.boye.httpclientandroidlib.client.methods.HttpUriRequest;
import ch.boye.httpclientandroidlib.client.utils.URLEncodedUtils;
import ch.boye.httpclientandroidlib.conn.ClientConnectionManager;
import ch.boye.httpclientandroidlib.conn.scheme.Scheme;
import ch.boye.httpclientandroidlib.conn.ssl.SSLSocketFactory;
import ch.boye.httpclientandroidlib.impl.client.DefaultHttpClient;
import ch.boye.httpclientandroidlib.impl.conn.tsccm.ThreadSafeClientConnManager;
import ch.boye.httpclientandroidlib.message.BasicNameValuePair;
import ch.boye.httpclientandroidlib.params.HttpParams;
import ch.boye.httpclientandroidlib.util.EntityUtils;
import org.bouncycastle.crypto.digests.SHA512Digest;
import org.bouncycastle.crypto.macs.HMac;
import org.bouncycastle.crypto.params.KeyParameter;
import org.bouncycastle.util.encoders.Base64;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import javax.net.ssl.SSLContext;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * BitmeAPI
 * @author Erik Gregg
 */
public class BitmeAPI {
    Logger logger = Logger.getLogger("BitmeAPI");
    private static final String API_URLROOT = "https://bitme.com/rest";
    private final String apiSecret;
    private final String apiKey;
    private HttpClient httpClient;

    public BitmeAPI(String apiKey, String apiSecret) {
        this.apiKey = apiKey;
        this.apiSecret = apiSecret;
        this.httpClient = getThreadSafeClient();
        // Production
        this.logger.setLevel(Level.OFF);
        // Debug
        // this.logger.setLevel(Level.ALL);

    }

    /**
     * Verifies that the configuration used to instantiate the class (API key and secret) are valid.
     @return On success, an empty JSON object.  On failure, null.
     */
    public JSONObject verify_credentials()
    {
        return query("/verify-credentials", true);
    }

    /**
     * Lists accounts belonging to the authenticated user
     * @return JSONObject containing account information
     */
    public JSONObject accounts()
    {
        return query("/accounts", true);
    }

    /**
     * Lists bitcoin addresses belonging to the authenticated user
     * @return JSONObject containing the bitcoin addresses
     */
    public JSONObject bitcoin_address()
    {
        return query("/bitcoin-address", true);
    }

    /**
     * Withdraws bitcoins to a given bitcoin address
     * @param amount Amount in BTC to move
     * @param address Address to move BTC to
     * @return JSONObject containing the status
     */
    public JSONObject bitcoin_withdraw(String amount, String address)
    {
        List<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair("amount", amount));
        params.add(new BasicNameValuePair("address", address));
        return query("/bitcoin-withdraw", true, params, "POST");
    }

    /**
     * Lists open orders for the authenticated user
     * @return JSONObject containing open orders
     */
    public JSONObject open_orders()
    {
        return query("/orders/open", true);
    }

    /**
     * Retrieves information about a transaction with a specified ID
     * @param id ID of transaction, generally obtained with get_transactions()
     * @return  JSONObject containing transaction
     */
    public JSONObject get_transaction(String id)
    {
        return query("/transaction/" + id, true);
    }

    /**
     * Get BTC transactions associated with authenticated user with default params (limit 10, descending order, page 1)
     * @return JSONObject containing transactions
     */
    public JSONObject get_transactions()
    { return get_transactions("BTC"); }
    /**
     * Get transactions for the specified currency associated with authenticated user with default params (limit 10, descending order, page 1)
     * @return JSONObject containing transactions
     */
    public JSONObject get_transactions(String currency_cd)
    { return get_transactions(currency_cd, 10); }
    /**
     * Get some number of transactions for the specified currency associated with authenticated user with default params (descending order, page 1)
     * @return JSONObject containing transactions
     */
    public JSONObject get_transactions(String currency_cd, int limit)
    { return get_transactions(currency_cd, limit, "DESC"); }
    /**
     * Get some number of ordered transactions for the specified currency associated with authenticated user with default params (page 1)
     * @return JSONObject containing transactions
     */
    public JSONObject get_transactions(String currency_cd, int limit, String order_by)
    { return get_transactions(currency_cd, limit, order_by, 1); }
    /**
     * Get some number of ordered transactions for the specified currency associated with authenticated user with a given page offset
     * @return JSONObject containing transactions
     */
    public JSONObject get_transactions(String currency_cd, int limit, String order_by, int page)
    {
        List<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair("currency_cd", currency_cd));
        params.add(new BasicNameValuePair("limit", (new Integer(limit)).toString()));
        params.add(new BasicNameValuePair("order_by", order_by));
        params.add(new BasicNameValuePair("page", (new Integer(page)).toString()));
        return query("/transactions/" + currency_cd, true, params);
    }

    /**
     * Create an BTCLTC order with the given properties
     * @param order_type_cd 'BID' or 'ASK'
     * @param quantity Number of units to transact
     * @param rate Rate at which to transact (Minimum amount is 0.001 BTC)
     * @return JSONObject containing order information
     */
    public JSONObject create_order(String order_type_cd,
                                   String quantity,
                                   String rate)
    { return create_order(order_type_cd, quantity, rate, "BTCLTC"); }

    /**
     * Create an order with the given properties
     * @param order_type_cd 'BID' or 'ASK'
     * @param quantity Number of units to transact
     * @param rate Rate at which to transact (Minimum amount is 0.001 BTC)
     * @param currency_pair Currency pair to use
     * @return JSONObject containing order information
     */
    public JSONObject create_order(String order_type_cd,
                                   String quantity,
                                   String rate,
                                   String currency_pair)
    {
        List<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair("order_type_cd", order_type_cd));
        params.add(new BasicNameValuePair("quantity", quantity));
        params.add(new BasicNameValuePair("rate", rate));
        params.add(new BasicNameValuePair("currency_pair", currency_pair));
        return query("/order/create", true, params, "POST");
    }

    /**
     * Cancel an order
     * @param uuid UUID of order, generally from open_orders()
     * @return JSONObject containing order information
     */
    public JSONObject cancel_order(String uuid)
    {
        List<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair("uuid", uuid));
        return query("/order/cancel", true, params, "POST");
    }

    /**
     * Gets information about an order with a given UUID
     * @param uuid UUID of order, generally from open_orders()
     * @return JSONObject containing order information
     */
    public JSONObject get_order(String uuid)
    {
        return query("/order/" + uuid, true);
    }

    /**
     * Get orderbook (data about open orders) for BTCLTC
     * @return JSONObject containing orderbook data
     */
    public JSONObject get_orderbook()
    { return get_orderbook("BTCLTC"); }
    /**
     * Get orderbook (data about open orders) for given currency pair
     * @return JSONObject containing orderbook data
     */
    public JSONObject get_orderbook(String currency_pair)
    {
        return query("/orderbook/" + currency_pair);
    }

    /**
     * Get bitcoincharts-compatible orderbook (data about open orders) for BTCLTC
     * @return JSONObject containing orderbook data
     */
    public JSONObject get_compat_orderbook()
    { return get_compat_orderbook("BTCLTC"); }
    /**
     * Get bitcoincharts-compatible orderbook (data about open orders) for given currency pair
     * @return JSONObject containing orderbook data
     */
    public JSONObject get_compat_orderbook(String currency_pair)
    {
        return query("/compat/orderbook/" + currency_pair);
    }

    /**
     * Get bitcoincharts-compatible trade array for BTCLTC
     * @return JSONObject containing trade array
     */
    public JSONObject get_compat_trades()
    { return get_compat_trades("BTCLTC"); }
    /**
     * Get bitcoincharts-compatible trade array for given currency pair
     * @return JSONObject containing trade array
     */
    public JSONObject get_compat_trades(String currency_pair)
    {
        return query("/compat/trades/" + currency_pair);
    }

    /**
     * Queries a URL with default parameters (no auth, no params, HTTP GET)
     * @param url URL suffix to query
     * @return JSON result of API call
     */
    private JSONObject query(String url)
    { return query(url, false); }
    /**
     * Queries a URL with default parameters (no params, HTTP GET)
     * @param url URL to query
     * @param auth Whether authentication should be used
     * @return JSON result of API call
     */
    private JSONObject query(String url, boolean auth)
    { return query(url, auth, null); }
    /**
     * Queries a URL with GET method
     * @param url URL to query
     * @param auth Whether authentication should be used
     * @param params Params for either GET or POST
     * @return JSON result of API call
     */
    private JSONObject query(String url, boolean auth, List<NameValuePair> params)
        { return query(url, auth, params, "GET"); }

    /**
     * Queries a url with given parameters
     * @param url URL suffix to query
     * @param auth Whether authentication should be used
     * @param params Params for either GET or POST
     * @param method GET or POST
     * @return JSON result of API call
     */
    private JSONObject query(String url, boolean auth, List<NameValuePair> params, String method)
    {
        JSONObject object = null;
        HttpUriRequest req;
        if(this.apiKey == null || this.apiSecret == null) return null;

        if(method == null || method.equals("GET"))
        {
            req = new HttpGet(BitmeAPI.API_URLROOT + url);
        }
        else
        {
            req = new HttpPost(BitmeAPI.API_URLROOT + url);
        }
        if(params == null) params = new ArrayList<NameValuePair>();

        if(auth)
        {
            // Must compute nonce and signature
            HMac hmac = new HMac(new SHA512Digest());
            logger.info("API Secret: " + this.apiSecret);
            byte[] decoded = Base64.decode(this.apiSecret);
            if(decoded == null) return null;
            hmac.init(new KeyParameter(decoded));
            byte[] resBuf = new byte[hmac.getMacSize()];
            Date date = new Date();
            long nonce = date.getTime() * 1000 + 1000000;
            params.add(0, new BasicNameValuePair("nonce", (new Long(nonce)).toString()));
            String to_hash = URLEncodedUtils.format(params, Charset.defaultCharset());
            hmac.update(to_hash.getBytes(), 0, to_hash.getBytes().length);
            hmac.doFinal(resBuf, 0);
            req.addHeader("Rest-Key", this.apiKey);
            logger.info("Rest-Key: " + this.apiKey);
            req.addHeader("Rest-Sign", new String(Base64.encode(resBuf)));
            logger.info("Rest-Sign: " + new String(Base64.encode(resBuf)));
            logger.info("Params: " + to_hash);
            if(req instanceof HttpGet)
            {
                try {
                    ((HttpGet)req).setURI(new URI(BitmeAPI.API_URLROOT + url + "?" + to_hash));
                } catch (URISyntaxException e) {
                    e.printStackTrace();
                }
            } else
            {
                try {
                    ((HttpPost)req).setEntity(new UrlEncodedFormEntity(params));
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
            }
        }

        HttpResponse response = null;
        try {
            logger.info("Final URL: " + req.getURI().toString());
            response = this.httpClient.execute(req);
            HttpEntity entity = response.getEntity();
            String str_entity = EntityUtils.toString(entity);
            logger.info("Response Content: " + str_entity);
            try {
                object = new JSONObject(str_entity);
            } catch (JSONException e) {
                try {
                    JSONArray array = new JSONArray(str_entity);
                    object = new JSONObject();
                    object.put("array", array);
                } catch (JSONException inner) {
                    inner.printStackTrace();
                }
            } finally {
                entity.consumeContent();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return object;
    }
    public static DefaultHttpClient getThreadSafeClient()  {
        SSLSocketFactory sf = null;
        SSLContext sslContext;
        try {
            sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null,null,null);
        } catch (NoSuchAlgorithmException e) {
            return null;
        } catch (KeyManagementException e){
            return null;
        }
        sf = new SSLSocketFactory(
                sslContext,
                new BitmeSSLVerifier());

        Scheme sch = new Scheme("https", 443, sf);
        DefaultHttpClient client = new DefaultHttpClient();
        client.getConnectionManager().getSchemeRegistry().register(sch);
        ClientConnectionManager mgr = client.getConnectionManager();
        HttpParams params = client.getParams();
        client = new DefaultHttpClient(new ThreadSafeClientConnManager(params,
                mgr.getSchemeRegistry()), params);
        return client;
    }
}
