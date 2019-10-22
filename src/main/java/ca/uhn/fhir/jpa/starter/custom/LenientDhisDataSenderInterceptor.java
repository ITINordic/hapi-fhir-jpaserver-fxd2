package ca.uhn.fhir.jpa.starter.custom;

import ca.uhn.fhir.context.FhirContext;
import static ca.uhn.fhir.jpa.starter.custom.CustomInterceptorAdapter.AUTHORIZATION_HEADER;
import ca.uhn.fhir.rest.api.server.RequestDetails;
import ca.uhn.fhir.rest.api.server.ResponseDetails;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.hl7.fhir.instance.model.api.IBaseExtension;
import org.hl7.fhir.instance.model.api.IBaseHasExtensions;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.StringType;

/**
 *
 * @author Charles Chigoriwa
 */
public class LenientDhisDataSenderInterceptor extends AbstractDhisDataSenderInterceptor {

    
    
    protected final static String SUBSCRIPTION_EXTENSION_URL = "adapter/subscription/status";
    
    //private final ForkJoinPool dhisSaveForkJoinPool=new ForkJoinPool();
    
    
    /*@Override
     protected boolean saveInDhisViaAdapter(RequestDetails theRequestDetails, ResponseDetails theResponseDetails, HttpServletRequest theServletRequest, HttpServletResponse theServletResponse){
         dhisSaveForkJoinPool.submit(()->{             
             super.saveInDhisViaAdapter(theRequestDetails, theResponseDetails, theServletRequest, theServletResponse);         
         });
         return true;
     }*/
    
    
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
            saveForSubscription(theRequestDetails, theResponseDetails);            
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
        return false;
    }
    
    protected void setSubscriptionExtension(IBaseResource resource, String status) {
        IBaseHasExtensions resourceWithExtension = (IBaseHasExtensions) resource;
        IBaseExtension extension = getSubscriptionExtension(resource);
        if (extension == null) {
            extension = resourceWithExtension.addExtension();
            extension.setUrl(SUBSCRIPTION_EXTENSION_URL);
        }
        extension.setValue(new StringType(status));

    }

    protected IBaseExtension getSubscriptionExtension(IBaseResource resource) {
        IBaseHasExtensions resourceWithExtension = (IBaseHasExtensions) resource;
        List<? extends IBaseExtension<?, ?>> extensions = resourceWithExtension.getExtension();
        for (IBaseExtension extension : extensions) {
            if (extension.getUrl().equals(SUBSCRIPTION_EXTENSION_URL)) {
                return extension;
            }
        }
        return null;
    }
    
    protected void saveForSubscription(RequestDetails theRequestDetails, ResponseDetails theResponseDetails) {
        IBaseResource resource = theResponseDetails.getResponseResource();
        if (resource != null) {
            setSubscriptionExtension(resource, "active");
            String authorization = theRequestDetails.getHeader(AUTHORIZATION_HEADER);
            FhirContext fhirContext = theRequestDetails.getFhirContext();
            IGenericClient client = getFhirClient(fhirContext, authorization, Collections.singletonMap(FRISM_HINT, NO_DHIS_SAVE));
            resource.setId(client.update().resource(resource).execute().getId());
        }
    }
}
