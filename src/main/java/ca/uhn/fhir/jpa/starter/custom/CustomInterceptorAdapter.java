package ca.uhn.fhir.jpa.starter.custom;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.jpa.starter.HapiProperties;
import ca.uhn.fhir.parser.JsonParser;
import ca.uhn.fhir.parser.LenientErrorHandler;
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
import org.hl7.fhir.r4.model.BooleanType;
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
    protected final static String DHIS_SAVED_EXTENSION_URL = "dhis/resource/express/saved";
    protected final static String AUTHORIZATION_HEADER = "Authorization";
    protected final static String DEFAULT_CUSTOM_LOCAL_SERVER = "http://localhost:8080/hapi-fhir-jpaserver/fhir/";
    protected final static String DEFAULT_ADAPTER_BASE_URL = "http://localhost:8081";

    protected IGenericClient getFhirClient(FhirContext fhirContext, String authorization) {
        String customLocalServerAddress = getCustomerLocalFhirAddress();
        IGenericClient client = fhirContext.newRestfulGenericClient(customLocalServerAddress);
        client.registerInterceptor(new CustomClientAuthInterceptor(authorization));
        return client;
    }

    protected IGenericClient getFhirClient(FhirContext fhirContext, String authorization, Map<String, String> headers) {
        IGenericClient client = getFhirClient(fhirContext, authorization);
        client.registerInterceptor(new CustomClientHeaderInterceptor(headers));
        return client;
    }

    protected void saveAsDhisSaved(RequestDetails theRequestDetails, ResponseDetails theResponseDetails) {
        IBaseResource resource = theResponseDetails.getResponseResource();
        if (resource != null) {
            setDhisSavedExtension(resource, true);
            String authorization = theRequestDetails.getHeader(AUTHORIZATION_HEADER);
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

    protected boolean isAuthorizedByAdapter(String authorization) {
        String url = "/remote-fhir-express/authenticated";
        String adapterBaseUrl = getAdapterBaseUrl();
        url = adapterBaseUrl + url;
        try {
            CustomHttpUtility.httpGet(url, authorization);
            return true;
        } catch (IOException | ApiException ex) {
            return false;
        }
    }

    protected boolean isAdapterRunning() {
        String url = "/remote-fhir-express/runningAdapter";
        String adapterBaseUrl = getAdapterBaseUrl();
        url = adapterBaseUrl + url;
        try {
            CustomHttpUtility.httpGet(url);
            return true;
        } catch (IOException | ApiException ex) {
            return false;
        }
    }

    protected IBaseResource getResourceForReset(RequestDetails theRequestDetails, ResponseDetails theResponseDetails) {
        IBaseResource resourceBeforeUpdate = (IBaseResource) theRequestDetails.getUserData().get(RESOURCE_BEFORE_UPDATE);
        String authorization = theRequestDetails.getHeader(AUTHORIZATION_HEADER);
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
        extension.setValue(new BooleanType(saved));

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
            return ((BooleanType) extension.getValue()).getValue();
        }
    }

    protected AdapterResource createAdapterResource(RequestDetails theRequestDetails, ResponseDetails theResponseDetails) {
        AdapterResource adapterResource = new AdapterResource();
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

        adapterResource.setClientResourceId(clientResourceId);
        if (!GeneralUtility.isEmpty(clientResourceId)) {
            FhirContext fhirContext = theRequestDetails.getFhirContext();
            JsonParser jsonParser = new JsonParser(fhirContext, new LenientErrorHandler());
            adapterResource.setClientId("73cd99c5-0ca8-42ad-a53b-1891fccce08f");
            adapterResource.setResourceId(resource.getIdElement().getIdPart());
            adapterResource.setResourceInString(jsonParser.encodeResourceToString(resource));
            adapterResource.setResourceType(resourceClassName);
            String adapterBaseUrl = getAdapterBaseUrl();
            adapterResource.setBaseUrl(adapterBaseUrl);
        }
        return adapterResource;
    }

    protected String getAdapterBaseUrl() {
        String adapterBaseUrl = HapiProperties.getCustomDhisFhirAdapterBaseUrl();
        adapterBaseUrl = GeneralUtility.isEmpty(adapterBaseUrl) ? DEFAULT_ADAPTER_BASE_URL : adapterBaseUrl;
        return adapterBaseUrl;
    }

    protected String getCustomerLocalFhirAddress() {
        String customLocalServerAddress = HapiProperties.getCustomLocalServerAddress();
        customLocalServerAddress = GeneralUtility.isEmpty(customLocalServerAddress) ? DEFAULT_CUSTOM_LOCAL_SERVER : customLocalServerAddress;
        return customLocalServerAddress;
    }

}
