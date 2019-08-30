package ca.uhn.fhir.jpa.starter.custom;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.jpa.starter.HapiProperties;
import ca.uhn.fhir.parser.IParser;
import ca.uhn.fhir.parser.JsonParser;
import ca.uhn.fhir.parser.LenientErrorHandler;
import ca.uhn.fhir.parser.XmlParser;
import ca.uhn.fhir.rest.api.RequestTypeEnum;
import ca.uhn.fhir.rest.api.server.RequestDetails;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import ca.uhn.fhir.rest.server.exceptions.AuthenticationException;
import ca.uhn.fhir.rest.server.interceptor.InterceptorAdapter;
import java.io.IOException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.hl7.fhir.instance.model.api.IBaseResource;

/**
 *
 * @author Charles Chigoriwa
 */
public class FhirDataValidationInterceptor extends InterceptorAdapter {

    //This method is called just before the actual implementing server method is invoked.
    @Override
    public boolean incomingRequestPostProcessed(RequestDetails theRequestDetails, HttpServletRequest theRequest, HttpServletResponse theResponse) throws AuthenticationException {
        RequestTypeEnum requestType = theRequestDetails.getRequestType();
        if (requestType.equals(RequestTypeEnum.POST) || requestType.equals(RequestTypeEnum.PUT) || requestType.equals(RequestTypeEnum.DELETE)) {
            String authorization = theRequestDetails.getHeader("Authorization");
            checkIfAdapterAndItsDhisAreRunning(authorization);
        }
        return true;
    }

    private void checkIfAdapterAndItsDhisAreRunning(String authorization) {
        String url = "/remote-fhir-express/authenticated";
        String baseUrl = HapiProperties.getCustomDhisFhirAdapterBaseUrl();
        baseUrl = GeneralUtility.isEmpty(baseUrl) ? "http://localhost:8081" : baseUrl;
        url = baseUrl + url;
        try {
            CustomHttpUtility.httpGet(url, authorization);
        } catch (IOException | ApiException ex) {
            throw new AuthenticationException();
        }
    }

    protected IBaseResource getInputResource(RequestDetails theRequestDetails) {
        RequestTypeEnum requestType = theRequestDetails.getRequestType();
        FhirContext fhirContext = theRequestDetails.getFhirContext();
        String contentType = theRequestDetails.getHeader("Content-Type");
        if (requestType.equals(RequestTypeEnum.POST) || requestType.equals(RequestTypeEnum.PUT)) {
            try {
                IParser parser;
                if (contentType.toLowerCase().contains("json")) {
                    parser = new JsonParser(fhirContext, new LenientErrorHandler());
                } else {
                    parser = new XmlParser(fhirContext, new LenientErrorHandler());
                }
                return parser.parseResource(theRequestDetails.getInputStream());

            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        }
        return null;
    }

    public IGenericClient getFhirClient(FhirContext fhirContext,String authorization) {
        IGenericClient client = fhirContext.newRestfulGenericClient(HapiProperties.getCustomLocalServerAddress());
        client.registerInterceptor(new CustomClientAuthInterceptor(authorization));
        return client;
    }

}
