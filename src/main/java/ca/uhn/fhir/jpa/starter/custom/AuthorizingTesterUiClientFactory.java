package ca.uhn.fhir.jpa.starter.custom;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import ca.uhn.fhir.rest.client.interceptor.BearerTokenAuthInterceptor;
import ca.uhn.fhir.rest.server.util.ITestingUiClientFactory;
import javax.servlet.http.HttpServletRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 *
 * @author Charles Chigoriwa
 */
public class AuthorizingTesterUiClientFactory implements ITestingUiClientFactory {

    @Override
    public IGenericClient newClient(FhirContext theFhirContext, HttpServletRequest theRequest, String theServerBaseUrl) {
        // Create a client
        IGenericClient client = theFhirContext.newRestfulGenericClient(theServerBaseUrl);
        // Register an interceptor which adds credentials
        client.registerInterceptor(new BearerTokenAuthInterceptor(getAccessToken()));

        return client;
    }

    private String getAccessToken() {
        String accessToken = "";
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            if (authentication instanceof DHIS2Authentication) {
                DHIS2Authentication dhis2Authentication = (DHIS2Authentication) authentication;
                DHIS2TokenWrapper tokenWrapper = dhis2Authentication.getDhis2TokenWrapper();
                if (tokenWrapper != null) {
                    if (tokenWrapper.isExpired() || tokenWrapper.isAboutToExpire()) {
                        DHIS2TokenWrapper newTokenWrapper = DHIS2TokenUtility.getRefreshedDHIS2TokenWrapper(tokenWrapper.getRefreshToken());
                        accessToken = newTokenWrapper.getAccessToken();
                        SecurityContextHolder.getContext().setAuthentication(DHIS2Authentication.valueOf(newTokenWrapper));
                    } else {
                        accessToken = tokenWrapper.getAccessToken();
                    }
                }
            }
        }
        return accessToken;
    }

}
