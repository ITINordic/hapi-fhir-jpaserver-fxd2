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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.apache.http.auth.BasicUserPrincipal;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

/**
 *
 * @author Charles Chigoriwa
 */
public class DHIS2Authentication implements Authentication {

    private Collection<? extends GrantedAuthority> authorities;
    private Object credentials;
    private Object details;
    private Object Principal;
    private boolean authenticated;
    private String name;
    private DHIS2TokenWrapper dhis2TokenWrapper;

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

    public DHIS2TokenWrapper getDhis2TokenWrapper() {
        return dhis2TokenWrapper;
    }

    public void setDhis2TokenWrapper(DHIS2TokenWrapper dhis2TokenWrapper) {
        this.dhis2TokenWrapper = dhis2TokenWrapper;
    }

    public static DHIS2Authentication valueOf(DHIS2TokenWrapper dhis2TokenWrapper) {
        String username = dhis2TokenWrapper.getAccessToken() + ":" + dhis2TokenWrapper.getRefreshToken();
        DHIS2Authentication auth = new DHIS2Authentication();
        auth.setAuthorities(getDefaultAuthorities());
        auth.setAuthenticated(true);
        auth.setName(username);
        auth.setPrincipal(new BasicUserPrincipal(username));
        auth.setCredentials(username);
        auth.setDhis2TokenWrapper(dhis2TokenWrapper);
        return auth;
    }

    private static List<GrantedAuthority> getDefaultAuthorities() {
        List<GrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority("ROLE_DEFAULT"));
        return authorities;
    }

}
