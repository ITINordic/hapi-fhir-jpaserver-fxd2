package ca.uhn.fhir.jpa.starter.oslo;

import java.io.Serializable;
import java.util.List;
import javax.annotation.Nonnull;

/**
 *
 * @author developer
 */
public class DhisUser implements Serializable{

    private static final long serialVersionUID = -4030062884258712996L;

    private final String id;

    private final String username;

    private final List<DhisUserRole> userRoles;

    private final List<DhisOrganisationUnit> organisationUnits;

    public DhisUser(@Nonnull String id, @Nonnull String username, @Nonnull List<DhisUserRole> userRoles, @Nonnull List<DhisOrganisationUnit> organisationUnits) {
        this.id = id;
        this.username = username;
        this.userRoles = userRoles;
        this.organisationUnits = organisationUnits;
    }

    public String getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public List<DhisUserRole> getUserRoles() {
        return userRoles;
    }

    public List<DhisOrganisationUnit> getOrganisationUnits() {
        return organisationUnits;
    }

}
