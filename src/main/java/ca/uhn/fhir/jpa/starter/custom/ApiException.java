package ca.uhn.fhir.jpa.starter.custom;

/**
 *
 * @author Charles Chigoriwa
 */
public class ApiException extends FrismException {

    private String status;
    private String content;

    public ApiException() {
    }

    public ApiException(String status, String content) {
        super("Status=" + status + ", Content=" + content);
        this.status = status;
        this.content = content;
    }

    public ApiException(String status, String content, Throwable cause) {
        super("Status=" + status + ", Content=" + content, cause);
        this.status = status;
        this.content = content;
    }

    public ApiException(String message) {
        super(message);
    }

    public ApiException(String message, Throwable cause) {
        super(message, cause);
    }

    public ApiException(Throwable cause) {
        super(cause);
    }

    public ApiException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

}
