package org.jointsecurityarea.bitme;

import org.apache.commons.codec.binary.Base64;
import org.apache.http.HttpEntity;
import org.apache.http.HttpRequest;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.bouncycastle.crypto.digests.SHA512Digest;
import org.bouncycastle.crypto.macs.HMac;
import org.bouncycastle.crypto.params.KeyParameter;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * BitmeAPI
 * Author: Erik Gregg
 */
public class BitmeAPI {
    Logger logger = Logger.getLogger("BitmeAPI");
    private static final String API_URLROOT = "https://bitme.com/rest";
    private final String apiSecret;
    private final String apiKey;
    private CloseableHttpClient httpClient;

    public BitmeAPI(String apiKey, String apiSecret) {
        this.apiKey = apiKey;
        this.apiSecret = apiSecret;
        this.httpClient = HttpClients.createDefault();
        this.logger.setLevel(Level.ALL);
    }
    private JSONObject query(String url)
    { return query(url, false); }
    private JSONObject query(String url, boolean auth)
    { return query(url, auth, null); }
    private JSONObject query(String url, boolean auth, List<NameValuePair> params)
        { return query(url, auth, params, "GET"); }
    private JSONObject query(String url, boolean auth, List<NameValuePair> params, String method)
    {
        JSONObject object = null;
        HttpUriRequest req;
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
            hmac.init(new KeyParameter(Base64.decodeBase64(this.apiSecret)));
            byte[] resBuf = new byte[hmac.getMacSize()];
            Date date = new Date();
            long nonce = date.getTime() * 1000 + 1000000;
            params.add(0, new BasicNameValuePair("nonce", (new Long(nonce)).toString()));
            String to_hash = URLEncodedUtils.format((Iterable<? extends NameValuePair>) params, null);
            hmac.update(to_hash.getBytes(), 0, to_hash.getBytes().length);
            hmac.doFinal(resBuf, 0);
            req.addHeader("Rest-Key", this.apiKey);
            logger.info("Rest-Key: " + this.apiKey);
            req.addHeader("Rest-Sign", new String(Base64.encodeBase64(resBuf)));
            logger.info("Rest-Sign: " + new String(Base64.encodeBase64(resBuf)));
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

        CloseableHttpResponse response = null;
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
            }

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if(response != null)
                try {
                    response.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
        }
        return object;
    }

    public JSONObject verify_credentials()
    {
        return query("/verify-credentials", true);
    }
    public JSONObject accounts()
    {
        return query("/accounts", true);
    }
    public JSONObject bitcoin_address()
    {
        return query("/bitcoin-address", true);
    }
    public JSONObject open_orders()
    {
        return query("/orders/open", true);
    }
    /* cancel_order */
    public JSONObject get_transaction(String id)
    {
        return query("/transaction/" + id, true);
    }

    public JSONObject get_transactions()
        { return get_transactions("BTC"); }
    public JSONObject get_transactions(String currency_cd)
        { return get_transactions(currency_cd, 10); }
    public JSONObject get_transactions(String currency_cd, int limit)
        { return get_transactions(currency_cd, limit, "DESC"); }
    public JSONObject get_transactions(String currency_cd, int limit, String order_by)
        { return get_transactions(currency_cd, limit, "DESC", 1); }
    public JSONObject get_transactions(String currency_cd, int limit, String order_by, int page)
    {
        List<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair("currency_cd", currency_cd));
        params.add(new BasicNameValuePair("limit", (new Integer(limit)).toString()));
        params.add(new BasicNameValuePair("order_by", order_by));
        params.add(new BasicNameValuePair("page", (new Integer(page)).toString()));
        return query("/transactions/" + currency_cd, true, params);
    }

    public JSONObject create_order(String order_type_cd,
                                   int quantity,
                                   String rate)
        { return create_order(order_type_cd, quantity, rate, "LTCBTC"); }
    public JSONObject create_order(String order_type_cd,
                                   int quantity,
                                   String rate,
                                   String currency_pair)
    {
        List<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair("order_type_cd", order_type_cd));
        params.add(new BasicNameValuePair("quantity", (new Integer(quantity)).toString()));
        params.add(new BasicNameValuePair("rate", rate));
        params.add(new BasicNameValuePair("currency_pair", currency_pair));
        return query("/order/create", true, params, "POST");
    }

    public JSONObject cancel_order(String uuid)
    {
        List<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair("uuid", uuid));
        return query("/order/cancel", true, params, "POST");
    }
    public JSONObject get_order(String uuid)
    {
        return query("/order/" + uuid, true);
    }

    public JSONObject get_orderbook()
        { return get_orderbook("LTCBTC"); }
    public JSONObject get_orderbook(String currency_pair)
    {
        return query("/orderbook/" + currency_pair);
    }

    public JSONObject get_compat_orderbook()
        { return get_compat_orderbook("LTCBTC"); }
    public JSONObject get_compat_orderbook(String currency_pair)
    {
        return query("/compat/orderbook/" + currency_pair);
    }

    public JSONObject get_compat_trades()
    { return get_compat_trades("LTCBTC"); }
    public JSONObject get_compat_trades(String currency_pair)
    {
        return query("/compat/trades/" + currency_pair);
    }
}
