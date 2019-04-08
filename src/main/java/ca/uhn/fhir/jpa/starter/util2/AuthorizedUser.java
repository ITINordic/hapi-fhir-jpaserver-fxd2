package ca.uhn.fhir.jpa.starter.util2;

import java.io.Serializable;
import java.util.Set;
import javax.annotation.Nonnull;

/**
 *
 * @author developer
 */
public class AuthorizedUser implements Serializable {

    public static final String ATTRIBUTE_NAME = AuthorizedUser.class.getName();

    private static final long serialVersionUID = -2003663083946137408L;

    private final DhisUser dhisUser;

    private final Set<String> organizationIds;

    private final boolean admin;

    public AuthorizedUser(@Nonnull DhisUser dhisUser, @Nonnull Set<String> organizationIds, boolean admin) {
        this.dhisUser = dhisUser;
        this.organizationIds = organizationIds;
        this.admin = admin;
    }

    @Nonnull
    public DhisUser getDhisUser() {
        return dhisUser;
    }

    @Nonnull
    public Set<String> getOrganizationIds() {
        return organizationIds;
    }

    public boolean isAdmin() {
        return admin;
    }
}
