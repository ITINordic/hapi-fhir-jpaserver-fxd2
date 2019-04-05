package ca.uhn.fhir.jpa.starter.custom;

import ca.uhn.fhir.rest.api.server.RequestDetails;
import ca.uhn.fhir.rest.server.interceptor.auth.AuthorizationInterceptor;
import ca.uhn.fhir.rest.server.interceptor.auth.IAuthRule;
import ca.uhn.fhir.rest.server.interceptor.auth.RuleBuilder;
import java.util.List;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 *
 * @author Charles Chigoriwa
 */
public class DHIS2AuthInterceptor extends AuthorizationInterceptor {

    @Override
    public List<IAuthRule> buildRuleList(RequestDetails theRequestDetails) {
        boolean authorized;
        String bearertoken = theRequestDetails.getHeader("Authorization");
        if (bearertoken != null) {
            String token = bearertoken.split(" ")[1];
            authorized = checkToken(token);
        } else {
            authorized = true /*isLoggedIn()*/;
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
        return false;
    }

    private boolean isLoggedIn() {
        return SecurityContextHolder.getContext().getAuthentication() != null
                && SecurityContextHolder.getContext().getAuthentication().isAuthenticated()
                && //when Anonymous Authentication is enabled
                !(SecurityContextHolder.getContext().getAuthentication() instanceof AnonymousAuthenticationToken);

    }

}
