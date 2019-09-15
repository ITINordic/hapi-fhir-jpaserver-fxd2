package ca.uhn.fhir.jpa.starter.custom;

import ca.uhn.fhir.rest.api.server.RequestDetails;
import ca.uhn.fhir.rest.api.server.ResponseDetails;
import java.io.IOException;
import java.util.Collections;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 *
 * @author Charles Chigoriwa
 */
public class LenientDhisDataSenderInterceptor extends AbstractDhisDataSenderInterceptor {

    @Override
    protected boolean handleAdapterError(AdapterResource adapterResource, RequestDetails theRequestDetails, ResponseDetails theResponseDetails, HttpServletRequest theServletRequest, HttpServletResponse theServletResponse) {
        String clientId = adapterResource.getClientId();
        String clientResourceId = adapterResource.getClientResourceId();
        String resourceId = adapterResource.getResourceId();
        String resourceInString = adapterResource.getResourceInString();
        String resourceType = adapterResource.getResourceType();
        String url = "/remote-fhir-rest-hook/" + clientId + "/" + clientResourceId + "/" + resourceType + "/" + resourceId;
        String baseUrl = adapterResource.getBaseUrl();
        url = baseUrl + url;
        String authorization = "Bearer jhsj832jDShf8ehShdu7ejhDhsilwmdsgs";
        try {
            CustomHttpUtility.httpPost(url, resourceInString, authorization, Collections.singletonMap("Content-Type", "application/json"));
        } catch (IOException | ApiException ex) {
            //Ignore this error
        }
        return true;
    }

    @Override
    protected boolean checkIfAuthorizedByAdapter() {
        return false;
    }

    @Override
    protected boolean storeResourceBeforeUpdate() {
        return false;
    }

    @Override
    protected boolean checkIfAdapterIsRunning() {
        return true;
    }

}
