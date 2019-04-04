package ca.uhn.fhir.jpa.starter.oslo;

import java.util.Objects;
import javax.annotation.Nonnull;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

/**
 *
 * @author developer
 */
public class DhisUserService {

    private final RestTemplate dhisRestTemplate;

    public DhisUserService(@Nonnull @Qualifier("dhisRestTemplate") RestTemplate dhisRestTemplate) {
        this.dhisRestTemplate = dhisRestTemplate;
    }

    public DhisUser authenticateUser(@Nonnull String authorizationValue) {
        final HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add(HttpHeaders.AUTHORIZATION, authorizationValue);

        final HttpEntity<Void> httpEntity = new HttpEntity<>(httpHeaders);
        final ResponseEntity<ExchangedDhisUser> responseEntity = dhisRestTemplate.exchange(
                "/me?fields=id,userCredentials[username,userRoles[id,name]],organisationUnits[id,code]",
                HttpMethod.GET, httpEntity, ExchangedDhisUser.class);
        final ExchangedDhisUser user = Objects.requireNonNull(responseEntity.getBody());

        return new DhisUser(user.getId(), user.getUserCredentials().getUsername(), user.getUserCredentials().getUserRoles(), user.getOrganisationUnits());
    }

}
