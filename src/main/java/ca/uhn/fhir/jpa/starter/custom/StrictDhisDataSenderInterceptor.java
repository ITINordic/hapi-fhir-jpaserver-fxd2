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
import static ca.uhn.fhir.jpa.starter.custom.CustomInterceptorAdapter.FRISM_HINT;
import ca.uhn.fhir.rest.api.RestOperationTypeEnum;
import ca.uhn.fhir.rest.api.server.RequestDetails;
import ca.uhn.fhir.rest.api.server.ResponseDetails;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import java.util.Collections;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.hl7.fhir.instance.model.api.IBaseResource;

/**
 *
 * @author Charles Chigoriwa
 */
public class StrictDhisDataSenderInterceptor extends AbstractDhisDataSenderInterceptor {

    @Override
    protected boolean handleAdapterError(AdapterResource adapterResource, RequestDetails theRequestDetails, ResponseDetails theResponseDetails, HttpServletRequest theServletRequest, HttpServletResponse theServletResponse) {
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

    @Override
    protected boolean checkIfAuthorizedByAdapter() {
        return true;
    }

    @Override
    protected boolean storeResourceBeforeUpdate() {
        return true;
    }

    protected void revertResourceToBeforeUpdate(RequestDetails theRequestDetails) {
        Object objectBeforeUpdate = theRequestDetails.getUserData().get(RESOURCE_BEFORE_UPDATE);
        if (objectBeforeUpdate != null && objectBeforeUpdate instanceof IBaseResource) {
            IBaseResource resourceBeforeUpdate = (IBaseResource) objectBeforeUpdate;
            String authorization = theRequestDetails.getHeader(AUTHORIZATION_HEADER);
            FhirContext fhirContext = theRequestDetails.getFhirContext();
            IGenericClient client = getFhirClient(fhirContext, authorization, Collections.singletonMap(FRISM_HINT, NO_DHIS_SAVE));
            client.update().resource(resourceBeforeUpdate).execute();
        }
    }

    protected void deleteResource(RequestDetails theRequestDetails, ResponseDetails theResponseDetails) {
        IBaseResource resource = theResponseDetails.getResponseResource();
        String authorization = theRequestDetails.getHeader(AUTHORIZATION_HEADER);
        FhirContext fhirContext = theRequestDetails.getFhirContext();
        IGenericClient client = getFhirClient(fhirContext, authorization, Collections.singletonMap(FRISM_HINT, NO_DHIS_SAVE));
        client.delete()
                .resource(resource)
                .execute();
    }

    @Override
    protected boolean checkIfAdapterIsRunning() {
        return true;
    }

}
