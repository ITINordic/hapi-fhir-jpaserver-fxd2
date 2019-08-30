package ca.uhn.fhir.jpa.starter.custom;

import ca.uhn.fhir.rest.client.api.IClientInterceptor;
import ca.uhn.fhir.rest.client.api.IHttpRequest;
import ca.uhn.fhir.rest.client.api.IHttpResponse;
import java.io.IOException;
import java.util.Map;

/**
 *
 * @author Charles Chigoriwa
 */
public class CustomClientHeaderInterceptor implements IClientInterceptor {
    
    private final Map<String, String> headers;
    
    public CustomClientHeaderInterceptor(Map<String, String> headers) {
        this.headers = headers;
    }
    
    @Override
    public void interceptRequest(IHttpRequest theRequest) {
        for (String key : headers.keySet()) {
            theRequest.addHeader(key, headers.get(key));
        }
    }
    
    @Override
    public void interceptResponse(IHttpResponse theResponse) throws IOException {
        //Do nothing
    }
    
}
