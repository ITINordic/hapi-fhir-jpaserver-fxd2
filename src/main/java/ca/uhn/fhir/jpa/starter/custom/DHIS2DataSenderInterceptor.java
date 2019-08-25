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
import ca.uhn.fhir.rest.api.RequestTypeEnum;
import ca.uhn.fhir.rest.api.server.RequestDetails;
import ca.uhn.fhir.rest.api.server.ResponseDetails;
import ca.uhn.fhir.rest.server.exceptions.AuthenticationException;
import ca.uhn.fhir.rest.server.interceptor.InterceptorAdapter;
import java.io.IOException;
import java.util.Collections;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Charles Chigoriwa
 */
public class DHIS2DataSenderInterceptor extends InterceptorAdapter {

    protected static Logger logger = LoggerFactory.getLogger(DHIS2DataSenderInterceptor.class);

    @Override
    public boolean outgoingResponse(RequestDetails theRequestDetails, ResponseDetails theResponseDetails, HttpServletRequest theServletRequest, HttpServletResponse theServletResponse)
            throws AuthenticationException {
        RequestTypeEnum requestType = theRequestDetails.getRequestType();
        if (requestType.equals(RequestTypeEnum.POST) || requestType.equals(RequestTypeEnum.PUT)) {
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
                    FhirContext fireContext = theRequestDetails.getFhirContext();
                    JsonParser jsonParser = new JsonParser(fireContext, new LenientErrorHandler());
                    String clientId = "73cd99c5-0ca8-42ad-a53b-1891fccce08f";
                    String resourceId = resource.getIdElement().getIdPart();
                    String resourceInString = jsonParser.encodeResourceToString(resource);
                    String resourceType = resourceClassName;
                    String url = "/remote-fhir-rest-hook/" + clientId + "/" + clientResourceId + "/" + resourceType + "/" + resourceId;
                    String baseUrl = HapiProperties.getCustomDhisFhirAdapterBaseUrl();
                    baseUrl = GeneralUtility.isEmpty(baseUrl) ? "http://localhost:8081" : baseUrl;
                    url = baseUrl + url;
                    String authorization = "Bearer jhsj832jDShf8ehShdu7ejhDhsilwmdsgs";
                    try {
                        DHIS2HttpUtility.httpPost(url, resourceInString, authorization, Collections.singletonMap("Content-Type", "application/json"));
                    } catch (IOException | ApiException ex) {
                        logger.error("Error saving a resource in dhis", ex);
                    }

                }

            }

        }
        return true;

    }

}
