package ca.uhn.fhir.jpa.starter.alternative;

import ca.uhn.fhir.rest.api.QualifiedParamList;
import ca.uhn.fhir.rest.api.RestOperationTypeEnum;
import ca.uhn.fhir.rest.api.server.RequestDetails;
import ca.uhn.fhir.rest.param.ParameterUtil;
import ca.uhn.fhir.rest.server.exceptions.AuthenticationException;
import ca.uhn.fhir.rest.server.interceptor.auth.AuthorizedList;
import ca.uhn.fhir.rest.server.interceptor.auth.SearchNarrowingInterceptor;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.collections4.ListUtils;
import org.hl7.fhir.instance.model.Patient;

/**
 *
 * @author developer
 */
public class DhisSearchNarrowingInterceptor extends SearchNarrowingInterceptor {

    @Override
    protected AuthorizedList buildAuthorizedList(RequestDetails theRequestDetails) {
        if ("Organization".equalsIgnoreCase(theRequestDetails.getResourceName())) {
            return new AuthorizedList().addResources(
                    getRestrictedOrganizations(theRequestDetails).toArray(new String[0]));
        }

        return null;
    }

    @Override
    public boolean incomingRequestPostProcessed(RequestDetails theRequestDetails, HttpServletRequest theRequest, HttpServletResponse theResponse) throws AuthenticationException {
        if (getAuthorizedUser(theRequestDetails).isAdmin()) {
            return true;
        }

        if (!super.incomingRequestPostProcessed(theRequestDetails, theRequest, theResponse)) {
            return false;
        }

        if (theRequestDetails.getRestOperationType() != RestOperationTypeEnum.SEARCH_TYPE) {
            return true;
        }

        final Map<String, List<String>> parameterToOrValues = new HashMap<>();

        if ("Patient".equalsIgnoreCase(theRequestDetails.getResourceName())) {
            processReferencedResources(theRequestDetails, parameterToOrValues,
                    Patient.SP_ORGANIZATION, getRestrictedOrganizations(theRequestDetails));
        }

        if (!parameterToOrValues.isEmpty()) {
            theRequestDetails.setParameters(addReferencedResources(parameterToOrValues,
                    new HashMap<>(theRequestDetails.getParameters())));
        }

        return true;
    }

    @Nonnull
    private Map<String, String[]> addReferencedResources(@Nonnull Map<String, List<String>> parameterToOrValues, @Nonnull Map<String, String[]> newParameters) {
        parameterToOrValues.forEach((nextParamName, nextAllowedValues)
                -> {
            if (newParameters.containsKey(nextParamName)) {
                String[] existingValues = newParameters.get(nextParamName);
                boolean restrictedExistingList = false;

                for (int i = 0; i < existingValues.length; i++) {
                    String nextExistingValue = existingValues[i];
                    List<String> nextRequestedValues = QualifiedParamList.splitQueryStringByCommasIgnoreEscape(null, nextExistingValue);
                    List<String> nextPermittedValues = ListUtils.intersection(nextRequestedValues, nextAllowedValues);

                    if (nextPermittedValues.size() > 0) {
                        restrictedExistingList = true;
                        existingValues[i] = ParameterUtil.escapeAndJoinOrList(nextPermittedValues);
                    }
                }

                if (!restrictedExistingList) {
                    String[] newValues = Arrays.copyOf(existingValues, existingValues.length + 1);
                    newValues[existingValues.length] = ParameterUtil.escapeAndJoinOrList(nextAllowedValues);
                    newParameters.put(nextParamName, newValues);
                }
            } else {
                String nextValuesJoined = ParameterUtil.escapeAndJoinOrList(nextAllowedValues);
                String[] paramValues = {nextValuesJoined};
                newParameters.put(nextParamName, paramValues);
            }
        });

        return newParameters;
    }

    private void processReferencedResources(@Nonnull RequestDetails requestDetails, @Nonnull Map<String, List<String>> parameterToOrValues, @Nonnull String searchParamName, @Nonnull Collection<String> resourceReferences) {
        if (!resourceReferences.isEmpty()) {
            final List<String> orValues = parameterToOrValues.computeIfAbsent(searchParamName, spn -> new ArrayList<>());
            orValues.addAll(resourceReferences);
        }
    }

    @Nonnull
    private Collection<String> getRestrictedOrganizations(@Nonnull RequestDetails requestDetails) {
        final AuthorizedUser authorizedUser = getAuthorizedUser(requestDetails);

        return authorizedUser.getOrganizationIds().stream()
                .map(id -> "Organization/" + id).collect(Collectors.toList());
    }

    @Nonnull
    private AuthorizedUser getAuthorizedUser(@Nonnull RequestDetails requestDetails) {
        final AuthorizedUser authorizedUser = (AuthorizedUser) requestDetails.getUserData().get(AuthorizedUser.ATTRIBUTE_NAME);

        if (authorizedUser == null) {
            throw new IllegalStateException("Authorized user has not been set.");
        }

        return authorizedUser;
    }
}
