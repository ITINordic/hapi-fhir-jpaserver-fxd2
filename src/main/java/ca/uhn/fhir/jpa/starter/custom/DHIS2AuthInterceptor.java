package ca.uhn.fhir.jpa.starter.custom;

import ca.uhn.fhir.jpa.starter.HapiProperties;
import static ca.uhn.fhir.jpa.starter.custom.DHIS2TokenUtility.getAccessTokenFromSecurityContext;
import ca.uhn.fhir.jpa.starter.util2.DhisUser;
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
        String bearerAuthorization = theRequestDetails.getHeader("Authorization");
        if (bearerAuthorization != null) {
            String accessToken = bearerAuthorization.split(" ")[1];
            Boolean isAuthorizedToken = authorizationStore.getIfPresent(accessToken);
            if (isAuthorizedToken == null || !isAuthorizedToken) {
                authorized = checkToken(accessToken);
                authorizationStore.put(accessToken, authorized);
            } else {
                authorized = isAuthorizedToken;
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

    private boolean checkToken(String token) {
        String bearerAuthorization = GeneralUtility.getBearerAuthorization(token);
        String url = HapiProperties.getCustomDhis2BaseUrl() + "/api/me";
        try {
            DHIS2HttpUtility.httpGet(url, bearerAuthorization);
            return true;
        } catch (UnauthorizedApiException ex) {
            return false;
        } catch (IOException ex) {
            throw new FrismException(ex);
        }

    }

}
