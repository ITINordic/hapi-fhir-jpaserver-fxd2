package ca.uhn.fhir.jpa.starter.oslo;

import java.io.Serializable;
import java.util.List;

/**
 *
 * @author Oslo
 */
public class ExchangedDhisUser implements Serializable {

    private static final long serialVersionUID = 2254795657744423934L;

    private String id;

    private ExchangedDhisUserCredentials userCredentials;

    private List<DhisOrganisationUnit> organisationUnits;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public ExchangedDhisUserCredentials getUserCredentials() {
        return userCredentials;
    }

    public void setUserCredentials(ExchangedDhisUserCredentials userCredentials) {
        this.userCredentials = userCredentials;
    }

    public List<DhisOrganisationUnit> getOrganisationUnits() {
        return organisationUnits;
    }

    public void setOrganisationUnits(List<DhisOrganisationUnit> organisationUnits) {
        this.organisationUnits = organisationUnits;
    }

}
