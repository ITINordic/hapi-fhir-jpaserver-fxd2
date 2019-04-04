package ca.uhn.fhir.jpa.starter.oslo;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 *
 * @author developer
 */
public class DhisOrganisationUnit {

    private static final long serialVersionUID = -5436046484829233941L;

    private final String id;

    private final String code;

    @JsonCreator
    public DhisOrganisationUnit(@JsonProperty("id") String id, @JsonProperty("code") String code) {
        this.id = id;
        this.code = code;
    }

    public String getId() {
        return id;
    }

    public String getCode() {
        return code;
    }
}
