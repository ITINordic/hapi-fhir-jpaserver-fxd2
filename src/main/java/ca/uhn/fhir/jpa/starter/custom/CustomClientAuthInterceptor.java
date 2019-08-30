package ca.uhn.fhir.jpa.starter.custom;

import ca.uhn.fhir.rest.api.Constants;
import ca.uhn.fhir.rest.client.api.IClientInterceptor;
import ca.uhn.fhir.rest.client.api.IHttpRequest;
import ca.uhn.fhir.rest.client.api.IHttpResponse;
import java.io.IOException;

/**
 *
 * @author Charles Chigoriwa
 */
public class CustomClientAuthInterceptor implements IClientInterceptor{
    
    private final String authorization;

    public CustomClientAuthInterceptor(String authorization) {
        this.authorization = authorization;
    }

    @Override
    public void interceptRequest(IHttpRequest theRequest) {
        theRequest.addHeader(Constants.HEADER_AUTHORIZATION, authorization);
    }

    @Override
    public void interceptResponse(IHttpResponse theResponse) throws IOException {
      //Do nothing
    }
    
    
    
}
