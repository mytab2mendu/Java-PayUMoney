package com.geek.payment;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

//import sun.net.www.http.HttpClient;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author root
 */
public class JavaIntegrationKit {

    private Integer error;

    public boolean empty(String s) {
        if (s == null || s.trim().equals("")) {
            return true;
        } else {
            return false;
        }
    }

    public String hashCal(String type, String str) {
        byte[] hashseq = str.getBytes();
        StringBuffer sb = new StringBuffer();// method1
        StringBuffer hexString = new StringBuffer();// method2
        try {
            MessageDigest algorithm = MessageDigest.getInstance(type);
            algorithm.reset();
            algorithm.update(hashseq);

            byte[] mdbytes = algorithm.digest();
          //convert the byte to hex format method 1
            for (int i = 0; i < mdbytes.length; i++) {
            	sb.append(Integer.toString((mdbytes[i] & 0xff) + 0x100, 16).substring(1));
            }
            System.out.println("Hex format1 : " + sb.toString());
            //convert the byte to hex format method 2
          
            for (int i=0;i<mdbytes.length;i++) {
            	hexString.append(Integer.toHexString(0xFF & mdbytes[i]));
            }
            //System.out.println("Hex format2 : " + hexString.toString());

        } catch (NoSuchAlgorithmException nsae) {
        }
        return sb.toString();
        //return hexString.toString();

    }
    
    private static String getHashCodeFromString(String str) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("SHA-512");
        md.update(str.getBytes());
        byte byteData[] = md.digest();

        //convert the byte to hex format method 1
        StringBuffer hashCodeBuffer = new StringBuffer();
        for (int i = 0; i < byteData.length; i++) {
            hashCodeBuffer.append(Integer.toString((byteData[i] & 0xff) + 0x100, 16).substring(1));
        }
        return hashCodeBuffer.toString();
    }

    
    public static void main(String...a){
    	JavaIntegrationKit ji = new JavaIntegrationKit();
    	//System.out.println(ji.get_SHA_512("gtKFFx|txnid1|10.00|testPro|Ganesh|gani247@gmail.com|||||||||||", "eCwWELxi"));
    	System.out.println(ji.hashCal("SHA-512", "gtKFFx|txnid1|10.00|testPro|Ganesh|gani247@gmail.com|||||||||||eCwWELxi"));
    	try{
    		System.out.println(ji.getHashCodeFromString("gtKFFx|txnid1|10.00|testPro|Ganesh|gani247@gmail.com|||||||||||eCwWELxi"));
    	} catch (NoSuchAlgorithmException nsae) {
        }
    }

    protected Map<String, String> hashCalMethod(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");
        String salt = "eCwWELxi";//T7DjifXJuR";//"fE0aTrjr";//ZcoOKlVupo";// eCwWELxi";
        String action1 = "";
        String base_url = "https://test.payu.in";//secure
        // base_url = "https://secure.payu.in";//
        error = 0;
        String hashString = "";
        Enumeration paramNames = request.getParameterNames();
        Map<String, String> params = new HashMap<String, String>();
        Map<String, String> urlParams = new HashMap<String, String>();
        while (paramNames.hasMoreElements()) {
            String paramName = (String) paramNames.nextElement();
            String paramValue = request.getParameter(paramName);
            params.put(paramName, paramValue);
        }
        String txnid = "";
        if (empty(params.get("txnid"))) {
            Random rand = new Random();
            String rndm = Integer.toString(rand.nextInt()) + (System.currentTimeMillis() / 1000L);
            txnid = rndm;
            params.remove("txnid");
            params.put("txnid", txnid);
            txnid = hashCal("SHA-256", rndm).substring(0, 20);
        } else {
            txnid = params.get("txnid");
        }
                
        
        String hash = "";
        String otherPostParamSeq = "phone|surl|furl|lastname|curl|address1|address2|city|state|country|zipcode|pg";
        String hashSequence = "key|txnid|amount|productinfo|firstname|email|udf1|udf2|udf3|udf4|udf5|udf6|udf7|udf8|udf9|udf10";
        if (empty(params.get("hash")) && params.size() > 0) {
            if (empty(params.get("key")) || empty(txnid) || empty(params.get("amount")) || empty(params.get("firstname")) || empty(params.get("email")) || empty(params.get("phone")) || empty(params.get("productinfo")) || empty(params.get("surl")) || empty(params.get("furl"))) {// || empty(params.get("service_provider"))) {
                error = 1;
            } else {
                
                String[] hashVarSeq = hashSequence.split("\\|");
                for (String part : hashVarSeq) {
                    if (part.equals("txnid")) {
                        hashString = hashString + txnid;
                        urlParams.put("txnid", txnid);
                    } else {
                        hashString = (empty(params.get(part))) ? hashString.concat("") : hashString.concat(params.get(part).trim());
                        urlParams.put(part, empty(params.get(part)) ? "" : params.get(part).trim());
                    }
                    hashString = hashString.concat("|");
                }
                //hash = get_SHA_512(hashString,salt);
                hashString = hashString.concat(salt);
                hash = hashCal("SHA-512", hashString);
                
                System.out.println("hashString : "+hashString);
                action1 = base_url.concat("/_payment");
                String[] otherPostParamVarSeq = otherPostParamSeq.split("\\|");
                for (String part : otherPostParamVarSeq) {
                    urlParams.put(part, empty(params.get(part)) ? "" : params.get(part).trim());
                }

            }
        } else if (!empty(params.get("hash"))) {
            hash = params.get("hash");
            action1 = base_url.concat("/_payment");
        }

        urlParams.put("hash", hash);
        urlParams.put("action", action1);
        urlParams.put("hashString", hashString);
        return urlParams;
    }

    /*public static void trustSelfSignedSSL() {
        try {
            final SSLContext ctx = SSLContext.getInstance(
                    "TLS");
            final X509TrustManager tm = new X509TrustManager() {
                //@Override
                public void checkClientTrusted(final X509Certificate[] xcs, final String string) throws CertificateException {
// do nothing
                }

                //@Override
                public void checkServerTrusted(final X509Certificate[] xcs, final String string) throws CertificateException {
// do nothing
                }

                //@Override
                public X509Certificate[] getAcceptedIssuers() {
                    return null;
                }
            };
            ctx.init(null, new TrustManager[]{tm}, null);
            SSLContext.setDefault(ctx);
        } catch (final Exception ex) {
            ex.printStackTrace();
        }
    }*/
}
