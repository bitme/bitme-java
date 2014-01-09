package org.jointsecurityarea.bitme;

import ch.boye.httpclientandroidlib.conn.ssl.AbstractVerifier;

import javax.net.ssl.SSLException;

class BitmeSSLVerifier extends AbstractVerifier {

    @Override
    public void verify(String host, String[] cns, String[] subjectAlts) throws SSLException {
        if(cns[0].equals("bitme.com") ||
           cns[0].endsWith("cloudflare.com"))
            return;
        // Otherwise
        throw new SSLException("Certificate cannot be verified: CN <" + cns[0] + "> is not acceptable.");
    }
}