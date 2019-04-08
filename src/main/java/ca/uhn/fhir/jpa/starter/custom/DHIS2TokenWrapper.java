package ca.uhn.fhir.jpa.starter.custom;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.io.Serializable;
import java.util.Date;
import org.joda.time.DateTime;
import org.joda.time.Minutes;

/**
 *
 * @author Charles Chigoriwa
 */
public class DHIS2TokenWrapper implements Serializable{

    private long creationTime = new Date().getTime();
    private String accessToken;
    private String refreshToken;
    private String tokenType;
    private String scope;
    private int expiresIn;

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public String getTokenType() {
        return tokenType;
    }

    public void setTokenType(String tokenType) {
        this.tokenType = tokenType;
    }

    public String getScope() {
        return scope;
    }

    public void setScope(String scope) {
        this.scope = scope;
    }

    public int getExpiresIn() {
        return expiresIn;
    }

    public void setExpiresIn(int expiresIn) {
        this.expiresIn = expiresIn;
    }

    public long getCreationTime() {
        return creationTime;
    }

    public void setCreationTime(long creationTime) {
        this.creationTime = creationTime;
    }

    @JsonIgnore
    public DateTime getExpiryDateTime() {
        DateTime creationDateTime = new DateTime(creationTime);
        DateTime expiryDateTime = creationDateTime.plusSeconds(expiresIn);
        return expiryDateTime;
    }

    @JsonIgnore
    public boolean isExpired() {
        DateTime expiryDateTime = getExpiryDateTime();
        return !DateTime.now().isBefore(expiryDateTime);
    }

    @JsonIgnore
    public boolean isAboutToExpire() {
        DateTime expiryDateTime = getExpiryDateTime();
        DateTime todayDateTime = DateTime.now();
        int minutesLeft = Minutes.minutesBetween(todayDateTime, expiryDateTime).getMinutes();
        return minutesLeft <= 3;
    }

}
