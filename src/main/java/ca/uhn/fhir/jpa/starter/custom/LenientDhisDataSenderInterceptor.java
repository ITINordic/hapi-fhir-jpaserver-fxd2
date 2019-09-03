package ca.uhn.fhir.jpa.starter.custom;

import ca.uhn.fhir.rest.api.server.RequestDetails;
import ca.uhn.fhir.rest.api.server.ResponseDetails;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 *
 * @author Charles Chigoriwa
 */
public class LenientDhisDataSenderInterceptor extends DhisDataSenderInterceptor{
    
    
    @Override
    protected boolean handleAdapterError(AdapterResource adapterResource, RequestDetails theRequestDetails, ResponseDetails theResponseDetails, HttpServletRequest theServletRequest, HttpServletResponse theServletResponse) {
        return lenientErrorHandling(adapterResource,theRequestDetails, theResponseDetails, theServletRequest, theServletResponse);
    }

    @Override
    protected boolean checkIfAdapterAndDhisAreRunning() {
        return false;
    }
    
}
