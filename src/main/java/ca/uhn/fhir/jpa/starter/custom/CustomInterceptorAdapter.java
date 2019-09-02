package ca.uhn.fhir.jpa.starter.custom;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.jpa.starter.HapiProperties;
import ca.uhn.fhir.model.primitive.BooleanDt;
import ca.uhn.fhir.parser.JsonParser;
import ca.uhn.fhir.parser.LenientErrorHandler;
import ca.uhn.fhir.rest.api.RestOperationTypeEnum;
import ca.uhn.fhir.rest.api.server.RequestDetails;
import ca.uhn.fhir.rest.api.server.ResponseDetails;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import ca.uhn.fhir.rest.server.interceptor.InterceptorAdapter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import javax.annotation.Nonnull;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.hl7.fhir.instance.model.api.IBaseExtension;
import org.hl7.fhir.instance.model.api.IBaseHasExtensions;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.Bundle;
import org.springframework.http.HttpStatus;

/**
 *
 * @author Charles Chigoriwa
 */
public class CustomInterceptorAdapter extends InterceptorAdapter {

    protected final static String RESOURCE_BEFORE_UPDATE = "FrismResourceBeforeUpdate";
    protected final static String FRISM_HINT = "FrismHint";
    protected final static String NO_DHIS_SAVE = "NO_DHIS_SAVE";
    protected final static String DHIS_SAVED_EXTENSION_URL = "urn:dhis:saved";

    protected IGenericClient getFhirClient(FhirContext fhirContext, String authorization) {
        String customLocalServerAddress = HapiProperties.getCustomLocalServerAddress();
        customLocalServerAddress = GeneralUtility.isEmpty(customLocalServerAddress) ? "http://localhost:8080/hapi-fhir-jpaserver/fhir/" : customLocalServerAddress;
        IGenericClient client = fhirContext.newRestfulGenericClient(customLocalServerAddress);
        client.registerInterceptor(new CustomClientAuthInterceptor(authorization));
        return client;
    }

    protected IGenericClient getFhirClient(FhirContext fhirContext, String authorization, Map<String, String> headers) {
        IGenericClient client = getFhirClient(fhirContext, authorization);
        client.registerInterceptor(new CustomClientHeaderInterceptor(headers));
        return client;
    }

    protected void deleteResource(RequestDetails theRequestDetails, ResponseDetails theResponseDetails) {
        IBaseResource resource = theResponseDetails.getResponseResource();
        String authorization = theRequestDetails.getHeader("Authorization");
        FhirContext fhirContext = theRequestDetails.getFhirContext();
        IGenericClient client = getFhirClient(fhirContext, authorization, Collections.singletonMap(FRISM_HINT, NO_DHIS_SAVE));
        client.delete()
                .resource(resource)
                .execute();
    }

    protected void revertResourceToBeforeUpdate(RequestDetails theRequestDetails) {
        Object objectBeforeUpdate = theRequestDetails.getUserData().get(RESOURCE_BEFORE_UPDATE);
        if (objectBeforeUpdate != null && objectBeforeUpdate instanceof IBaseResource) {
            IBaseResource resourceBeforeUpdate = (IBaseResource) objectBeforeUpdate;
            String authorization = theRequestDetails.getHeader("Authorization");
            FhirContext fhirContext = theRequestDetails.getFhirContext();
            IGenericClient client = getFhirClient(fhirContext, authorization, Collections.singletonMap(FRISM_HINT, NO_DHIS_SAVE));
            client.update().resource(resourceBeforeUpdate).execute();
        }
    }

    protected void saveAsDhisSaved(RequestDetails theRequestDetails, ResponseDetails theResponseDetails) {
        IBaseResource resource = theResponseDetails.getResponseResource();
        if (resource != null) {
            setDhisSavedExtension(resource, true);
            String authorization = theRequestDetails.getHeader("Authorization");
            FhirContext fhirContext = theRequestDetails.getFhirContext();
            IGenericClient client = getFhirClient(fhirContext, authorization, Collections.singletonMap(FRISM_HINT, NO_DHIS_SAVE));
            client.update().resource(resource).execute();
        }
    }

    protected void createBadRequestResponse(@Nonnull HttpServletResponse theServletResponse, @Nonnull String message) {
        try {
            theServletResponse.setStatus(HttpStatus.BAD_REQUEST.value());
            theServletResponse.setContentType("text/plain");
            theServletResponse.getWriter().append(message);
            theServletResponse.getWriter().close();
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    protected boolean isAdapterAndItsDhisRunning(String authorization) {
        String url = "/remote-fhir-express/authenticated";
        String baseUrl = HapiProperties.getCustomDhisFhirAdapterBaseUrl();
        baseUrl = GeneralUtility.isEmpty(baseUrl) ? "http://localhost:8081" : baseUrl;
        url = baseUrl + url;
        try {
            CustomHttpUtility.httpGet(url, authorization);
            return true;
        } catch (IOException | ApiException ex) {
            return false;
        }
    }

    protected IBaseResource getResourceForReset(RequestDetails theRequestDetails, ResponseDetails theResponseDetails) {
        IBaseResource resourceBeforeUpdate = (IBaseResource) theRequestDetails.getUserData().get(RESOURCE_BEFORE_UPDATE);
        String authorization = theRequestDetails.getHeader("Authorization");
        FhirContext fhirContext = theRequestDetails.getFhirContext();
        IGenericClient client = getFhirClient(fhirContext, authorization, Collections.singletonMap(FRISM_HINT, NO_DHIS_SAVE));

        Bundle response = client.history().onInstance(resourceBeforeUpdate.getIdElement())
                .andReturnBundle(Bundle.class).count(1000).since(resourceBeforeUpdate.getMeta().getLastUpdated()).execute();

        List<IBaseResource> resources = new ArrayList<>();

        if (!GeneralUtility.isEmpty(response.getEntry())) {
            response.getEntry().forEach((entry) -> {
                resources.add(entry.getResource());
            });

        }

        resources.sort((o1, o2) -> o2.getMeta().getLastUpdated().compareTo(o1.getMeta().getLastUpdated()));
        for (IBaseResource resource : resources) {
            if (isDhisSaved(resource)) {
                return resource;
            }
        }

        return resourceBeforeUpdate;
    }

    protected void setDhisSavedExtension(IBaseResource resource, boolean saved) {
        IBaseHasExtensions resourceWithExtension = (IBaseHasExtensions) resource;
        IBaseExtension extension = getDhisSavedExtension(resource);
        if (extension == null) {
            extension = resourceWithExtension.addExtension();
            extension.setUrl(DHIS_SAVED_EXTENSION_URL);
        }
        extension.setValue(new BooleanDt(saved));

    }

    protected IBaseExtension getDhisSavedExtension(IBaseResource resource) {
        IBaseHasExtensions resourceWithExtension = (IBaseHasExtensions) resource;
        List<? extends IBaseExtension<?, ?>> extensions = resourceWithExtension.getExtension();
        for (IBaseExtension extension : extensions) {
            if (extension.getUrl().equals(DHIS_SAVED_EXTENSION_URL)) {
                return extension;
            }
        }
        return null;
    }

    protected boolean isDhisSaved(IBaseResource resource) {
        IBaseExtension extension = getDhisSavedExtension(resource);
        if (extension == null) {
            return true;
        } else {
            return ((BooleanDt) extension.getValue()).getValue();
        }
    }

    protected AdapterParam getAdapterParam(RequestDetails theRequestDetails, ResponseDetails theResponseDetails) {
        AdapterParam adapterParam = new AdapterParam();
        IBaseResource resource = theResponseDetails.getResponseResource();
        String resourceClassName = resource.getClass().getSimpleName();
        String clientResourceId = null;
        if (resourceClassName.equalsIgnoreCase("patient")) {
            clientResourceId = "667bfa41-867c-4796-86b6-eb9f9ed4dc94";
        } else if (resourceClassName.equalsIgnoreCase("carePlan")) {
            clientResourceId = "b28e733c-8aee-11e9-9928-4736812fb4de";
        } else if (resourceClassName.equalsIgnoreCase("questionnaireResponse")) {
            clientResourceId = "056c3922-8e64-11e9-a6cb-6ba3fca8a311";
        }

        adapterParam.setClientResourceId(clientResourceId);
        if (!GeneralUtility.isEmpty(clientResourceId)) {
            FhirContext fhirContext = theRequestDetails.getFhirContext();
            JsonParser jsonParser = new JsonParser(fhirContext, new LenientErrorHandler());
            adapterParam.setClientId("73cd99c5-0ca8-42ad-a53b-1891fccce08f");
            adapterParam.setResourceId(resource.getIdElement().getIdPart());
            adapterParam.setResourceInString(jsonParser.encodeResourceToString(resource));
            adapterParam.setResourceType(resourceClassName);
            String baseUrl = HapiProperties.getCustomDhisFhirAdapterBaseUrl();
            baseUrl = GeneralUtility.isEmpty(baseUrl) ? "http://localhost:8081" : baseUrl;
            adapterParam.setBaseUrl(baseUrl);
        }
        return adapterParam;
    }

    protected boolean strictErrorHandling(RequestDetails theRequestDetails, ResponseDetails theResponseDetails, HttpServletRequest theServletRequest, HttpServletResponse theServletResponse) {
        RestOperationTypeEnum restOperationType = theRequestDetails.getRestOperationType();
        if (restOperationType.equals(RestOperationTypeEnum.CREATE)) {
            deleteResource(theRequestDetails, theResponseDetails);
            createBadRequestResponse(theServletResponse, "Failed to save data in dhis. FhirResource deleted.");
        } else {
            revertResourceToBeforeUpdate(theRequestDetails);
            createBadRequestResponse(theServletResponse, "Failed to save data in dhis. Reverted to previous resource version.");
        }
        return false;
    }

    protected boolean lenientErrorHandling(AdapterParam adapterParam, RequestDetails theRequestDetails, ResponseDetails theResponseDetails, HttpServletRequest theServletRequest, HttpServletResponse theServletResponse) {
        String clientId = adapterParam.getClientId();
        String clientResourceId = adapterParam.getClientResourceId();
        String resourceId = adapterParam.getResourceId();
        String resourceInString = adapterParam.getResourceInString();
        String resourceType = adapterParam.getResourceType();
        String url = "/remote-fhir-rest-hook/" + clientId + "/" + clientResourceId + "/" + resourceType + "/" + resourceId;
        String baseUrl = adapterParam.getBaseUrl();
        url = baseUrl + url;
        String authorization = "Bearer jhsj832jDShf8ehShdu7ejhDhsilwmdsgs";
        try {
            CustomHttpUtility.httpPost(url, resourceInString, authorization, Collections.singletonMap("Content-Type", "application/json"));
        } catch (IOException | ApiException ex) {
            //Ignore this error
        }
        return true;
    }

}
