/*
 *BSD 2-Clause License
 *
 *Copyright (c) 2019, itinordic All rights reserved.
 *
 *Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following
 *conditions are met:
 *
 *    1. Redistributions of source code must retain the above copyright notice, this list of conditions and the following
 *    disclaimer.
 *
 *    2. Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following
 *    disclaimer in the documentation and/or other materials provided with the distribution.
 *
 *THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR
 *IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 *FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR
 *CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 *DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 *DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER
 *IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF
 *THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 **/
package ca.uhn.fhir.jpa.starter.custom;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.jpa.starter.HapiProperties;
import ca.uhn.fhir.parser.JsonParser;
import ca.uhn.fhir.parser.LenientErrorHandler;
import ca.uhn.fhir.rest.api.RestOperationTypeEnum;
import ca.uhn.fhir.rest.api.server.RequestDetails;
import ca.uhn.fhir.rest.api.server.ResponseDetails;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import ca.uhn.fhir.rest.server.exceptions.AuthenticationException;
import java.io.IOException;
import java.util.Collections;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.Bundle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Charles Chigoriwa
 */
public class DHIS2DataSenderInterceptor extends CustomInterceptorAdapter {

   

    protected static Logger logger = LoggerFactory.getLogger(DHIS2DataSenderInterceptor.class);

    //This method is called just before the actual implementing server method is invoked.
    @Override
    public boolean incomingRequestPostProcessed(RequestDetails theRequestDetails, HttpServletRequest theRequest, HttpServletResponse theResponse) throws AuthenticationException {
        RestOperationTypeEnum restOperationType = theRequestDetails.getRestOperationType();
        String frismHint = theRequestDetails.getHeader(FRISM_HINT);
        if (frismHint != null && frismHint.equalsIgnoreCase(NO_DHIS_SAVE)) {
            return true;
        }

        if (restOperationType.equals(RestOperationTypeEnum.CREATE) || restOperationType.equals(RestOperationTypeEnum.UPDATE)) {
            String authorization = theRequestDetails.getHeader("Authorization");
            if (isAdapterAndItsDhisRunning(authorization)) {
                return true;
            } else {
                throw new CustomAbortException(500, "Fhir Adapter and/or its Dhis are not running");
            }
        }
        return true;
    }

    @Override
    public void incomingRequestPreHandled(RestOperationTypeEnum theOperation, ActionRequestDetails theProcessedRequest) {
        RequestDetails theRequestDetails = theProcessedRequest.getRequestDetails();
        String frismHint = theRequestDetails.getHeader(FRISM_HINT);
        if (frismHint != null && frismHint.equalsIgnoreCase(NO_DHIS_SAVE)) {
            return;
        }

        if (theOperation.equals(RestOperationTypeEnum.CREATE) || theOperation.equals(RestOperationTypeEnum.UPDATE)) {
            IBaseResource resource = theProcessedRequest.getResource();
            setDhisSavedExtension(resource, false);
        }

        if (theOperation.equals(RestOperationTypeEnum.UPDATE)) {
            String authorization = theRequestDetails.getHeader("Authorization");
            String resourceName = theRequestDetails.getResourceName();
            FhirContext fhirContext = theRequestDetails.getFhirContext();
            IBaseResource resource = theProcessedRequest.getResource();
            IGenericClient client = getFhirClient(fhirContext, authorization, Collections.singletonMap(FRISM_HINT, NO_DHIS_SAVE));
            Bundle response = client.search().byUrl(resourceName + "/" + resource.getIdElement().getIdPart())
                    .returnBundle(Bundle.class).execute();
            if (!GeneralUtility.isEmpty(response.getEntry())) {
                IBaseResource resourceBeforeUpdate = response.getEntry().get(0).getResource();
                theRequestDetails.getUserData().put(RESOURCE_BEFORE_UPDATE, resourceBeforeUpdate);
            }
        }
    }

    //This method is called after the server implementation method has been called, but before any attempt to stream the
    //response back to the client.
    @Override
    public boolean outgoingResponse(RequestDetails theRequestDetails, ResponseDetails theResponseDetails, HttpServletRequest theServletRequest, HttpServletResponse theServletResponse)
            throws AuthenticationException {

        String frismHint = theRequestDetails.getHeader(FRISM_HINT);
        if (frismHint != null && frismHint.equalsIgnoreCase(NO_DHIS_SAVE)) {
            return true;
        }

        RestOperationTypeEnum restOperationType = theRequestDetails.getRestOperationType();
        if (restOperationType.equals(RestOperationTypeEnum.CREATE) || restOperationType.equals(RestOperationTypeEnum.UPDATE)) {
            IBaseResource resource = theResponseDetails.getResponseResource();
            if (resource != null) {
                String resourceClassName = resource.getClass().getSimpleName();
                String clientResourceId = null;
                if (resourceClassName.equalsIgnoreCase("patient")) {
                    clientResourceId = "667bfa41-867c-4796-86b6-eb9f9ed4dc94";
                } else if (resourceClassName.equalsIgnoreCase("carePlan")) {
                    clientResourceId = "b28e733c-8aee-11e9-9928-4736812fb4de";
                } else if (resourceClassName.equalsIgnoreCase("questionnaireResponse")) {
                    clientResourceId = "056c3922-8e64-11e9-a6cb-6ba3fca8a311";
                }

                if (!GeneralUtility.isEmpty(clientResourceId)) {
                    boolean useUserAuthorization = true;
                    FhirContext fhirContext = theRequestDetails.getFhirContext();
                    String authorization = theRequestDetails.getHeader("Authorization");
                    JsonParser jsonParser = new JsonParser(fhirContext, new LenientErrorHandler());
                    String clientId = "73cd99c5-0ca8-42ad-a53b-1891fccce08f";
                    String resourceId = resource.getIdElement().getIdPart();
                    String resourceInString = jsonParser.encodeResourceToString(resource);
                    String resourceType = resourceClassName;
                    //String url = "/remote-fhir-rest-hook/" + clientId + "/" + clientResourceId + "/" + resourceType + "/" + resourceId;
                    String url = "/remote-fhir-express/" + clientId + "/" + clientResourceId + "/" + resourceType + "/" + resourceId;
                    String baseUrl = HapiProperties.getCustomDhisFhirAdapterBaseUrl();
                    baseUrl = GeneralUtility.isEmpty(baseUrl) ? "http://localhost:8081" : baseUrl;
                    url = baseUrl + url;
                    if (!useUserAuthorization) {
                        authorization = "Bearer jhsj832jDShf8ehShdu7ejhDhsilwmdsgs";
                    }
                    boolean savedInDhis;
                    try {
                        CustomHttpUtility.httpPost(url, resourceInString, authorization, Collections.singletonMap("Content-Type", "application/json"));
                        savedInDhis = true;
                    } catch (IOException | ApiException ex) {
                        savedInDhis=false;
                        logger.error("Error saving a resource in dhis", ex);
                        if (restOperationType.equals(RestOperationTypeEnum.CREATE)) {
                            deleteResource(theRequestDetails, theResponseDetails);
                            createBadRequestResponse(theServletResponse, "Failed to save data in dhis. FhirResource deleted.");
                        } else {
                            revertResourceToBeforeUpdate(theRequestDetails);
                            createBadRequestResponse(theServletResponse, "Failed to save data in dhis. Reverted to previous resource version.");
                        }
                    }
                    
                    if(savedInDhis){
                        saveAsDhisSaved(theRequestDetails, theResponseDetails);
                    }
                    
                    return savedInDhis;

                }

            }

        }
        return true;

    }

   

   

}
