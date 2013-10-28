package org.jointsecurityarea.bitme;

import org.apache.commons.codec.binary.Base64;
import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.bouncycastle.crypto.digests.SHA512Digest;
import org.bouncycastle.crypto.macs.HMac;
import org.bouncycastle.crypto.params.KeyParameter;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;

/**
 * BitmeAPI
 * Author: Erik Gregg
 */
public class BitmeAPI {
    private static final String API_URLROOT = "https://bitme.com/rest";
    private final String apiSecret;
    private final String apiKey;
    private CloseableHttpClient httpClient;

    public BitmeAPI(String apiKey, String apiSecret) {
        this.apiKey = apiKey;
        this.apiSecret = apiSecret;
        this.httpClient = HttpClients.createDefault();
    }

    private JSONObject query(String url, boolean auth, List<NameValuePair> params)
    {
        JSONObject object = null;
        HttpGet get = new HttpGet(BitmeAPI.API_URLROOT + url);
        if(params == null) params = new ArrayList<NameValuePair>();

        if(auth)
        {
            // Must compute nonce and signature
            HMac hmac = new HMac(new SHA512Digest());
            hmac.init(new KeyParameter(Base64.decodeBase64(this.apiSecret)));
            byte[] resBuf = new byte[hmac.getMacSize()];
            Date date = new Date();
            long nonce = date.getTime() * 1000 + 1000000;
            params.add(new BasicNameValuePair("nonce", (new Long(nonce)).toString()));
            String to_hash = URLEncodedUtils.format((Iterable<? extends NameValuePair>) params, null);
            hmac.update(to_hash.getBytes(), 0, to_hash.getBytes().length);
            hmac.doFinal(resBuf, 0);
            get.addHeader("Rest-Key", this.apiKey);
            get.addHeader("Rest-Sign", new String(Base64.encodeBase64(resBuf)));
            try {
                get.setURI(new URI(BitmeAPI.API_URLROOT + url + "?" + to_hash));
            } catch (URISyntaxException e) {
                e.printStackTrace();
            }
        }

        CloseableHttpResponse response = null;
        try {
            response = this.httpClient.execute(get);
            HttpEntity entity = response.getEntity();
            object = new JSONObject(EntityUtils.toString(entity));
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
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
        return query("/verify-credentials", true, null);
    }
}
