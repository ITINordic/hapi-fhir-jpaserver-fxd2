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

import ca.uhn.fhir.jpa.starter.HapiProperties;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONObject;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 *
 * @author Charles Chigoriwa
 */
public class DhisTokenUtility {

    public static DhisTokenWrapper getNewDHIS2TokenWrapper(String username, String password) {
        try {
            String url = getUrl();
            String basicAuthorization = getBasicAuthorization();
            List<NameValuePair> params = new ArrayList<>();
            params.add(new BasicNameValuePair("grant_type", "password"));
            params.add(new BasicNameValuePair("username", username));
            params.add(new BasicNameValuePair("password", password));
            Map<String, String> headers = new HashMap<>();
            headers.put("Accept", "application/json");
            String responseBody = CustomHttpUtility.httpPost(url, params, basicAuthorization, headers);
            return toTokenWrapper(responseBody);
        } catch (IOException ex) {
            throw new FrismException(ex);
        }catch (ApiException ex) {
            if (ex.getStatus() != null && ex.getStatus().equals("400")) {
                throw new UnauthorizedApiException(ex);
            } else {
                throw ex;
            }
        }
    }

    public static DhisTokenWrapper getRefreshedDHIS2TokenWrapper(String refreshToken) {
        try {
            String url = getUrl();
            String basicAuthorization = getBasicAuthorization();
            List<NameValuePair> params = new ArrayList<>();
            params.add(new BasicNameValuePair("grant_type", "refresh_token"));
            params.add(new BasicNameValuePair("refresh_token", refreshToken));
            Map<String, String> headers = new HashMap<>();
            headers.put("Accept", "application/json");
            String responseBody = CustomHttpUtility.httpPost(url, params, basicAuthorization, headers);
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

    private static DhisTokenWrapper toTokenWrapper(String responseBody) {
        DhisTokenWrapper tokenWrapper = new DhisTokenWrapper();
        JSONObject jsonObject = new JSONObject(responseBody);
        tokenWrapper.setAccessToken(jsonObject.getString("access_token"));
        tokenWrapper.setRefreshToken(jsonObject.getString("refresh_token"));
        tokenWrapper.setTokenType(jsonObject.getString("token_type"));
        tokenWrapper.setScope(jsonObject.getString("scope"));
        tokenWrapper.setExpiresIn(jsonObject.getInt("expires_in"));
        return tokenWrapper;
    }
    
    public static String getAccessTokenFromSecurityContext() {
        String accessToken = "";
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            if (authentication instanceof DhisAuthentication) {
                DhisAuthentication dhis2Authentication = (DhisAuthentication) authentication;
                DhisTokenWrapper tokenWrapper = dhis2Authentication.getDhis2TokenWrapper();
                if (tokenWrapper != null) {
                    if (tokenWrapper.isExpired() || tokenWrapper.isAboutToExpire()) {
                        DhisTokenWrapper newTokenWrapper = DhisTokenUtility.getRefreshedDHIS2TokenWrapper(tokenWrapper.getRefreshToken());
                        accessToken = newTokenWrapper.getAccessToken();
                        SecurityContextHolder.getContext().setAuthentication(DhisAuthentication.valueOf(newTokenWrapper));
                    } else {
                        accessToken = tokenWrapper.getAccessToken();
                    }
                }
            }
        }
        return accessToken;
    }
}
