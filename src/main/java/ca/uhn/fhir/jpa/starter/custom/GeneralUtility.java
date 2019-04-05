package ca.uhn.fhir.jpa.starter.custom;

import java.util.Base64;

/**
 *
 * @author Charles Chigoriwa
 */
public class GeneralUtility {
    
    public static String getBasicAuthorization(String username, String password) {
        return "Basic " + Base64.getEncoder().encodeToString((username + ":" + password).getBytes());
    }

    public static String toBase64String(byte[] bytes) {
        return Base64.getEncoder().encodeToString(bytes);
    }
    
    public static boolean isEmpty(String string){
        return string==null || string.trim().isEmpty();
    }

}
