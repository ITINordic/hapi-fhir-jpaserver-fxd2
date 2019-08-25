/*
 *BSD 2-Clause License
 *
 *Copyright (c) 2019, itinordic All rights reserved.
 *
 *Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following
 *conditions are met:
 *
 *    1. Redistributions of source code must retain the above copyright notice, this list of conditions and the following
 *    disclaimer.
 *
 *    2. Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following
 *    disclaimer in the documentation and/or other materials provided with the distribution.
 *
 *THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR
 *IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 *FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR
 *CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 *DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 *DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER
 *IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF
 *THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 **/
package ca.uhn.fhir.jpa.starter.custom;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.Map;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClients;

/**
 *
 * @author Charles Chigoriwa
 */
public class DHIS2HttpUtility {

    public static String httpPost(String url, String body, String authorization, Map<String, String> headers) throws UnsupportedEncodingException, IOException {
        HttpClient httpClient = HttpClients.createDefault();
        HttpPost httppost = new HttpPost(url);
        if (!GeneralUtility.isEmpty(authorization)) {
            httppost.addHeader("Authorization", authorization);
        }

        if (!GeneralUtility.isEmpty(headers)) {
            headers.keySet().forEach((key) -> {
                httppost.addHeader(key, headers.get(key));
            });
        }

        if (!GeneralUtility.isEmpty(body)) {
            httppost.setEntity(new StringEntity(body, "UTF-8"));
        }

        HttpResponse response = httpClient.execute(httppost);
        return parseStringContent(response);
    }

    public static String httpPost(String url, List<NameValuePair> params, String authorization, Map<String, String> headers) throws UnsupportedEncodingException, IOException {
        HttpClient httpClient = HttpClients.createDefault();
        HttpPost httppost = new HttpPost(url);
        if (!GeneralUtility.isEmpty(authorization)) {
            httppost.addHeader("Authorization", authorization);
        }

        if (!GeneralUtility.isEmpty(headers)) {
            headers.keySet().forEach((key) -> {
                httppost.addHeader(key, headers.get(key));
            });
        }
        if (!GeneralUtility.isEmpty(params)) {
            httppost.setEntity(new UrlEncodedFormEntity(params, "UTF-8"));
        }
        HttpResponse response = httpClient.execute(httppost);
        return parseStringContent(response);
    }

    public static String httpGet(String url, String authorization, Map<String, String> headers) throws UnsupportedEncodingException, IOException {
        HttpClient httpClient = HttpClients.createDefault();
        HttpGet httpGet = new HttpGet(url);
        if (!GeneralUtility.isEmpty(authorization)) {
            httpGet.addHeader("Authorization", authorization);
        }
        
        if (!GeneralUtility.isEmpty(headers)) {
            headers.keySet().forEach((key) -> {
                httpGet.addHeader(key, headers.get(key));
            });
        }
        HttpResponse response = httpClient.execute(httpGet);
        return parseStringContent(response);
    }

    public static String httpPost(String url, List<NameValuePair> params, String authorization) throws UnsupportedEncodingException, IOException {
        return httpPost(url, params, authorization, null);
    }

    public static String httpPost(String url, String body, String authorization) throws UnsupportedEncodingException, IOException {
        return httpPost(url, body, authorization, null);
    }

    public static String httpGet(String url, String authorization) throws UnsupportedEncodingException, IOException {
        return httpGet(url, authorization, null);
    }

    private static String parseStringContent(HttpResponse response) throws IOException {
        String content = "";
        HttpEntity entity = response.getEntity();
        if (entity != null) {
            try (InputStream instream = entity.getContent()) {
                content = IOUtils.toString(instream, "UTF-8");
                System.out.println("Content=" + content);
            }
        }
        int statusCode = response.getStatusLine().getStatusCode();
        System.out.println("StatusCode=" + statusCode);
        if (statusCode == 401 || statusCode == 403) {
            throw new UnauthorizedApiException(String.valueOf(statusCode), content);
        } else if (statusCode >= 300) {
            throw new ApiException(String.valueOf(statusCode), content);
        }
        return content;
    }

}
