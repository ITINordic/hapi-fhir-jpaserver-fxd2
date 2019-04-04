package ca.uhn.fhir.jpa.starter.custom;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClients;
/**
 *
 * @author Charles Chigoriwa
 */
public class DHIS2Utility {

    public static String httpPost(String url, String body,String authorization) throws UnsupportedEncodingException, IOException {
        HttpClient httpClient = HttpClients.createDefault();
        HttpPost httppost = new HttpPost(url);
        httppost.addHeader("Authorization", authorization);
        httppost.setEntity(new StringEntity(body, "UTF-8"));
        HttpResponse response = httpClient.execute(httppost);
        return parseStringContent(response);
    }

    public static String httpGet(String url, String authorization) throws UnsupportedEncodingException, IOException {
        HttpClient httpClient = HttpClients.createDefault();
        HttpGet httpGet = new HttpGet(url);
        httpGet.addHeader("Authorization",authorization);
        HttpResponse response = httpClient.execute(httpGet);
        return parseStringContent(response);
    }

    private static String parseStringContent(HttpResponse response) throws IOException {
        String content = "";
        HttpEntity entity = response.getEntity();
        if (entity != null) {
            try (InputStream instream = entity.getContent()) {
                content = IOUtils.toString(instream, "UTF-8");
                System.out.println("DHIS2 Content=" + content);
            }
        }
        int statusCode = response.getStatusLine().getStatusCode();
        System.out.println("DHIS2 StatusCode=" + statusCode);
        if (statusCode == 401) {
            throw new UnauthorizedApiException(String.valueOf(statusCode), content);
        } else if (statusCode >= 300) {
            throw new ApiException(String.valueOf(statusCode), content);
        }
        return content;
    }


}
