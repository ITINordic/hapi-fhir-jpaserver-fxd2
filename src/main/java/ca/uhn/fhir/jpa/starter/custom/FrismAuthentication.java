package ca.uhn.fhir.jpa.starter.custom;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.apache.http.auth.BasicUserPrincipal;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

/**
 *
 * @author developer
 */
public class FrismAuthentication implements Authentication {

    private Collection<? extends GrantedAuthority> authorities;
    private Object credentials;
    private Object details;
    private Object Principal;
    private boolean authenticated;
    private String name;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    public void setAuthorities(Collection<? extends GrantedAuthority> authorities) {
        this.authorities = authorities;
    }

    @Override
    public Object getCredentials() {
        return credentials;
    }

    public void setCredentials(Object credentials) {
        this.credentials = credentials;
    }

    @Override
    public Object getDetails() {
        return details;
    }

    public void setDetails(Object details) {
        this.details = details;
    }

    @Override
    public Object getPrincipal() {
        return Principal;
    }

    public void setPrincipal(Object Principal) {
        this.Principal = Principal;
    }

    @Override
    public boolean isAuthenticated() {
        return authenticated;
    }

    @Override
    public void setAuthenticated(boolean authenticated) {
        this.authenticated = authenticated;
    }

    @Override
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public static FrismAuthentication valueOf(String accessToken, String refreshToken) {
        String username = accessToken + ":" + refreshToken;
        FrismAuthentication auth = new FrismAuthentication();
        auth.setAuthorities(getDefaultAuthorities());
        auth.setAuthenticated(true);
        auth.setName(username);
        auth.setPrincipal(new BasicUserPrincipal(username));
        auth.setCredentials(username);
        return auth;
    }

    private static List<GrantedAuthority> getDefaultAuthorities() {
        List<GrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority("ROLE_DEFAULT"));
        return authorities;
    }

}
