package ca.uhn.fhir.jpa.starter.custom;

import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;

/**
 *
 * @author Charles Chigoriwa
 */
public class DHIS2AuthenticationProvider implements AuthenticationProvider {

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        String name = authentication.getName();
        String password = authentication.getCredentials().toString();
        try {
            DHIS2TokenWrapper tokenWrapper = DHIS2TokenUtility.getNewDHIS2TokenWrapper(name, password);
            return DHIS2Authentication.valueOf(tokenWrapper);
        } catch (UnauthorizedApiException ex) {
            throw new BadCredentialsException("DHIS2  authentication failed");
        }
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return (UsernamePasswordAuthenticationToken.class.isAssignableFrom(authentication));
    }

}
