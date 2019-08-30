package ca.uhn.fhir.jpa.starter.custom;

import ca.uhn.fhir.rest.server.exceptions.BaseServerResponseException;
import org.hl7.fhir.instance.model.api.IBaseOperationOutcome;

/**
 *
 * @author Charles Chigoriwa
 */
public class CustomAbortException extends  BaseServerResponseException{

    public CustomAbortException(int theStatusCode, String theMessage) {
        super(theStatusCode, theMessage);
    }

    public CustomAbortException(int theStatusCode, String... theMessages) {
        super(theStatusCode, theMessages);
    }

    public CustomAbortException(int theStatusCode, String theMessage, IBaseOperationOutcome theBaseOperationOutcome) {
        super(theStatusCode, theMessage, theBaseOperationOutcome);
    }

    public CustomAbortException(int theStatusCode, String theMessage, Throwable theCause) {
        super(theStatusCode, theMessage, theCause);
    }

    public CustomAbortException(int theStatusCode, String theMessage, Throwable theCause, IBaseOperationOutcome theBaseOperationOutcome) {
        super(theStatusCode, theMessage, theCause, theBaseOperationOutcome);
    }

    public CustomAbortException(int theStatusCode, Throwable theCause) {
        super(theStatusCode, theCause);
    }

    public CustomAbortException(int theStatusCode, Throwable theCause, IBaseOperationOutcome theBaseOperationOutcome) {
        super(theStatusCode, theCause, theBaseOperationOutcome);
    }
    
}
