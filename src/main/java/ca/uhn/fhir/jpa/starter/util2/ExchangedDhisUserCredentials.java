package ca.uhn.fhir.jpa.starter.util2;

import java.io.Serializable;
import java.util.List;

/**
 *
 * @author Charles Chigoriwa
 */
public class ExchangedDhisUserCredentials implements Serializable{
    
    private static final long serialVersionUID = 1236821511497623609L;

        private String username;

        private List<DhisUserRole> userRoles;

        public String getUsername()
        {
            return username;
        }

        public void setUsername( String username )
        {
            this.username = username;
        }

        public List<DhisUserRole> getUserRoles()
        {
            return userRoles;
        }

        public void setUserRoles( List<DhisUserRole> userRoles )
        {
            this.userRoles = userRoles;
        }
    
}
