package ca.uhn.fhir.jpa.starter.custom;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.jpa.starter.HapiProperties;
import ca.uhn.fhir.model.primitive.BooleanDt;
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
            extension.setUrl("urn:dhis:saved");
        }
        extension.setValue(new BooleanDt(saved));

    }

    protected IBaseExtension getDhisSavedExtension(IBaseResource resource) {
        IBaseHasExtensions resourceWithExtension = (IBaseHasExtensions) resource;
        List<? extends IBaseExtension<?, ?>> extensions = resourceWithExtension.getExtension();
        for (IBaseExtension extension : extensions) {
            if (extension.getUrl().equalsIgnoreCase("urn:dhis:saved")) {
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

}
