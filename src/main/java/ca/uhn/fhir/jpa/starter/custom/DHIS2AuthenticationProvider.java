package ca.uhn.fhir.jpa.starter.custom;

import ca.uhn.fhir.jpa.starter.HapiProperties;
import java.io.IOException;
import java.util.ArrayList;
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
        String url = HapiProperties.getCustomDhis2BaseUrl()+"/api/me";
        boolean loggedIn = false;
        try {
            DHIS2Utility.httpGet(url, name, password);
            loggedIn = true;
        } catch (IOException ex) {
            throw new FrismException(ex);
        } catch (UnauthorizedApiException ex) {
            loggedIn = false;
        }

        if (loggedIn) {
            return new UsernamePasswordAuthenticationToken(
                    name, password, new ArrayList<>());
        } else {
            throw new BadCredentialsException("DHIS2  authentication failed");
        }
    }

    @Override
    public boolean supports(Class<?> authentication) {
       return (UsernamePasswordAuthenticationToken.class.isAssignableFrom(authentication));
    }
}
