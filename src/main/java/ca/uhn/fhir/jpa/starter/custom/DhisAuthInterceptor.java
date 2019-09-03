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
import static ca.uhn.fhir.jpa.starter.custom.DhisTokenUtility.getAccessTokenFromSecurityContext;
import ca.uhn.fhir.rest.api.server.RequestDetails;
import ca.uhn.fhir.rest.server.interceptor.auth.AuthorizationInterceptor;
import ca.uhn.fhir.rest.server.interceptor.auth.IAuthRule;
import ca.uhn.fhir.rest.server.interceptor.auth.RuleBuilder;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 *
 * @author Charles Chigoriwa
 */
public class DhisAuthInterceptor extends AuthorizationInterceptor {

    private final Cache<String, Boolean> authorizationStore = Caffeine.newBuilder()
            .expireAfterWrite(120, TimeUnit.SECONDS)
            .maximumSize(1000L).build();

    @Override
    public List<IAuthRule> buildRuleList(RequestDetails theRequestDetails) {
        boolean authorized;
        String authorization = theRequestDetails.getHeader("Authorization");
        if (!GeneralUtility.isEmpty(authorization)) {
            Boolean isAuthorizedToken = authorizationStore.getIfPresent(authorization);
            if (isAuthorizedToken != null) {
                authorized = isAuthorizedToken;
            } else {
                authorized = checkAuthorization(authorization);
                authorizationStore.put(authorization, authorized);
            }
        } else {
            String token = getAccessTokenFromSecurityContext();
            authorized = !GeneralUtility.isEmpty(token);
        }

        if (authorized) {
            return new RuleBuilder()
                    .allowAll()
                    .build();
        } else {
            return new RuleBuilder()
                    .denyAll()
                    .build();
        }

    }

    private boolean checkAuthorization(String authorization) {
        String url = HapiProperties.getCustomDhis2BaseUrl() + "/api/me";
        try {
            CustomHttpUtility.httpGet(url, authorization);
            return true;
        } catch (UnauthorizedApiException ex) {
            return false;
        } catch (IOException ex) {
            throw new FrismException(ex);
        }

    }

}
