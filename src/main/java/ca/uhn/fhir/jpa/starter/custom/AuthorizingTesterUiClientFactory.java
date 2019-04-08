package ca.uhn.fhir.jpa.starter.custom;

import ca.uhn.fhir.context.FhirContext;
import static ca.uhn.fhir.jpa.starter.custom.DHIS2TokenUtility.getAccessTokenFromSecurityContext;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import ca.uhn.fhir.rest.client.interceptor.BearerTokenAuthInterceptor;
import ca.uhn.fhir.rest.server.util.ITestingUiClientFactory;
import javax.servlet.http.HttpServletRequest;

/**
 *
 * @author Charles Chigoriwa
 */
public class AuthorizingTesterUiClientFactory implements ITestingUiClientFactory {

    @Override
    public IGenericClient newClient(FhirContext theFhirContext, HttpServletRequest theRequest, String theServerBaseUrl) {
        IGenericClient client = theFhirContext.newRestfulGenericClient(theServerBaseUrl);
        client.registerInterceptor(new BearerTokenAuthInterceptor(getAccessTokenFromSecurityContext()));
        return client;
    }

}
