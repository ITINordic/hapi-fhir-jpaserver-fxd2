package ca.uhn.fhir.jpa.starter.custom;

/**
 *
 * @author Charles Chigoriwa
 */
public class AdapterResource {
    
    private String clientId;
    private String resourceClassName;
    private String resourceInString;
    private String resourceType;
    private String baseUrl;
    private String clientResourceId;
    private String resourceId;

    public AdapterResource() {
    }


    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public String getResourceClassName() {
        return resourceClassName;
    }

    public void setResourceClassName(String resourceClassName) {
        this.resourceClassName = resourceClassName;
    }

    public String getResourceInString() {
        return resourceInString;
    }

    public void setResourceInString(String resourceInString) {
        this.resourceInString = resourceInString;
    }

    public String getResourceType() {
        return resourceType;
    }

    public void setResourceType(String resourceType) {
        this.resourceType = resourceType;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public String getClientResourceId() {
        return clientResourceId;
    }

    public void setClientResourceId(String clientResourceId) {
        this.clientResourceId = clientResourceId;
    }

    public String getResourceId() {
        return resourceId;
    }

    public void setResourceId(String resourceId) {
        this.resourceId = resourceId;
    }
    
    
    
    
    
    
    
    
}
