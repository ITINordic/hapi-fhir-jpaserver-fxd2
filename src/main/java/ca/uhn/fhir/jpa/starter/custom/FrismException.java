package ca.uhn.fhir.jpa.starter.custom;

/**
 *
 * @author Charles Chigoriwa
 */
public class FrismException extends RuntimeException{

    public FrismException() {
    }

    public FrismException(String message) {
        super(message);
    }

    public FrismException(String message, Throwable cause) {
        super(message, cause);
    }

    public FrismException(Throwable cause) {
        super(cause);
    }

    public FrismException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
    
    
    
}
