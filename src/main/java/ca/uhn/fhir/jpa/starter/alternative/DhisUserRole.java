package ca.uhn.fhir.jpa.starter.alternative;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;

/**
 *
 * @author developer
 */
public class DhisUserRole implements Serializable {

    private static final long serialVersionUID = 980633687131779714L;

    private String id;

    private String name;

    @JsonCreator
    public DhisUserRole(@JsonProperty("id") String id, @JsonProperty("name") String name) {
        this.id = id;
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }
}
