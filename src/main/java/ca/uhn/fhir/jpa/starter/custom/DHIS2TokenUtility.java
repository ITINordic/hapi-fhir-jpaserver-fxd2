package ca.uhn.fhir.jpa.starter.custom;

import ca.uhn.fhir.jpa.starter.HapiProperties;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONObject;

/**
 *
 * @author Charles Chigoriwa
 */
public class DHIS2TokenUtility {

    public static DHIS2TokenWrapper getNewDHIS2TokenWrapper(String username, String password) {
        try {
            String url = getUrl();
            String basicAuthorization = getBasicAuthorization();
            List<NameValuePair> params = new ArrayList<>();
            params.add(new BasicNameValuePair("grant_type", "password"));
            params.add(new BasicNameValuePair("username", username));
            params.add(new BasicNameValuePair("password", password));
            Map<String, String> headers = new HashMap<>();
            headers.put("Accept", "application/json");
            String responseBody = DHIS2HttpUtility.httpPost(url, params, basicAuthorization, headers);
            return toTokenWrapper(responseBody);
        } catch (IOException ex) {
            throw new FrismException(ex);
        }
    }

    public static DHIS2TokenWrapper getRefreshedDHIS2TokenWrapper(String refreshToken) {
        try {
            String url = getUrl();
            String basicAuthorization = getBasicAuthorization();
            List<NameValuePair> params = new ArrayList<>();
            params.add(new BasicNameValuePair("grant_type", "refresh_token"));
            params.add(new BasicNameValuePair("refresh_token", refreshToken));
            Map<String, String> headers = new HashMap<>();
            headers.put("Accept", "application/json");
            String responseBody = DHIS2HttpUtility.httpPost(url, params, basicAuthorization, headers);
            return toTokenWrapper(responseBody);
        } catch (IOException ex) {
            throw new FrismException(ex);
        }
    }

    private static String getUrl() {
        return HapiProperties.getCustomDhis2BaseUrl() + "/uaa/oauth/token";
    }

    private static String getBasicAuthorization() {
        String cid = HapiProperties.getCustomDhis2ClientCid();
        String secret = HapiProperties.getCustomDhis2ClientSecret();
        String basicAuthorization = GeneralUtility.getBasicAuthorization(cid, secret);
        return basicAuthorization;
    }

    private static DHIS2TokenWrapper toTokenWrapper(String responseBody) {
        DHIS2TokenWrapper tokenWrapper = new DHIS2TokenWrapper();
        JSONObject jsonObject = new JSONObject(responseBody);
        tokenWrapper.setAccessToken(jsonObject.getString("access_token"));
        tokenWrapper.setRefreshToken(jsonObject.getString("refresh_token"));
        tokenWrapper.setTokenType(jsonObject.getString("token_type"));
        tokenWrapper.setScope(jsonObject.getString("scope"));
        tokenWrapper.setExpiresIn(jsonObject.getInt("expires_in"));
        return tokenWrapper;
    }
}
