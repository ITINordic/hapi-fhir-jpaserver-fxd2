package ca.uhn.fhir.jpa.starter.custom;

import ca.uhn.fhir.jpa.starter.HapiProperties;
import static ca.uhn.fhir.jpa.starter.custom.DHIS2TokenUtility.getAccessTokenFromSecurityContext;
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
public class DHIS2AuthInterceptor extends AuthorizationInterceptor {

    private final Cache<String, Boolean> authorizationStore = Caffeine.newBuilder()
            .expireAfterWrite(60, TimeUnit.SECONDS)
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
            DHIS2HttpUtility.httpGet(url, authorization);
            return true;
        } catch (UnauthorizedApiException ex) {
            return false;
        } catch (IOException ex) {
            throw new FrismException(ex);
        }

    }

}
