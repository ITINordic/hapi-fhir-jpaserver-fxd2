package ca.uhn.fhir.jpa.starter.util2;

import ca.uhn.fhir.jpa.rp.dstu3.OrganizationResourceProvider;
import ca.uhn.fhir.rest.api.RestOperationTypeEnum;
import ca.uhn.fhir.rest.api.SearchTotalModeEnum;
import ca.uhn.fhir.rest.api.SortOrderEnum;
import ca.uhn.fhir.rest.api.SortSpec;
import ca.uhn.fhir.rest.api.SummaryEnum;
import ca.uhn.fhir.rest.api.server.IBundleProvider;
import ca.uhn.fhir.rest.api.server.RequestDetails;
import ca.uhn.fhir.rest.param.TokenAndListParam;
import ca.uhn.fhir.rest.param.TokenOrListParam;
import ca.uhn.fhir.rest.server.exceptions.AuthenticationException;
import ca.uhn.fhir.rest.server.exceptions.ForbiddenOperationException;
import ca.uhn.fhir.rest.server.exceptions.UnprocessableEntityException;
import ca.uhn.fhir.rest.server.interceptor.auth.AuthorizationInterceptor;
import ca.uhn.fhir.rest.server.interceptor.auth.AuthorizationInterceptor.Verdict;
import ca.uhn.fhir.rest.server.interceptor.auth.IAuthRule;
import ca.uhn.fhir.rest.server.interceptor.auth.RuleBuilder;
import ca.uhn.fhir.rest.server.servlet.ServletRequestDetails;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.lang3.StringUtils;
import org.hl7.fhir.instance.model.IdType;
import org.hl7.fhir.instance.model.Patient;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.instance.model.api.IIdType;
import org.hl7.fhir.r4.model.Organization;

/**
 *
 * @author Oslo
 */
public class DhisAuthorizationInterceptor extends AuthorizationInterceptor {

    private final DhisUserService dhisUserService;

    private final OrganizationResourceProvider organizationResourceProvider;

    private final Cache<String, DhisUser> dhisUserStore = Caffeine.newBuilder()
            .expireAfterWrite(30, TimeUnit.SECONDS)
            .maximumSize(1000L).build();

    private final Cache<List<String>, List<String>> organizationIdStore = Caffeine.newBuilder()
            .expireAfterWrite(30, TimeUnit.SECONDS)
            .maximumSize(1000L).build();

    public DhisAuthorizationInterceptor(@Nonnull DhisUserService dhisUserService,
            @Nonnull OrganizationResourceProvider organizationResourceProvider) {
        this.dhisUserService = dhisUserService;
        this.organizationResourceProvider = organizationResourceProvider;
    }

    @Override
    public boolean incomingRequestPostProcessed(RequestDetails theRequestDetails, HttpServletRequest theRequest, HttpServletResponse theResponse) throws AuthenticationException {
        final String authorization = theRequest.getHeader("Authorization");

        if (StringUtils.isAllBlank(authorization) || authorization.indexOf(' ') <= 0) {
            throwAuthenticationException("Authorization header is missing or invalid.");
        }

        DhisUser dhisUser = dhisUserStore.getIfPresent(authorization);

        if (dhisUser == null) {
            // thrown exception must be handled (e.g. error 403)
            dhisUser = dhisUserService.authenticateUser(authorization);
            dhisUserStore.put(authorization, dhisUser);
        }

        final boolean admin = dhisUser.getUserRoles().stream().anyMatch(ug -> "Superuser".equals(ug.getName()));
        final boolean nurse = dhisUser.getUserRoles().stream().anyMatch(ug -> "Nurse".equals(ug.getName()));

        if (!admin && !nurse) {
            throwAuthenticationException("User is unauthorized to access this application.");
        }

        if (admin && (theRequestDetails instanceof ServletRequestDetails)) {
            validateAdminAccess((ServletRequestDetails) theRequestDetails);
        }

        final List<String> organizationIds = getFhirOrganizationIds(dhisUser.getOrganisationUnits());

        if (!admin && organizationIds.isEmpty()) {
            throwAuthenticationException("User has no access to any facility.");
        }

        theRequestDetails.getUserData().put(AuthorizedUser.ATTRIBUTE_NAME,
                new AuthorizedUser(dhisUser, new HashSet<>(organizationIds), admin));

        return true;
    }

    @Override
    public Verdict applyRulesAndReturnDecision(RestOperationTypeEnum theOperation, RequestDetails theRequestDetails, IBaseResource theInputResource, IIdType theInputResourceId, IBaseResource theOutputResource) {
        final AuthorizedUser authorizedUser = getAuthorizedUser(theRequestDetails);

        if (!authorizedUser.isAdmin()) {
            if (theInputResource instanceof Patient) {
                final Patient patient = (Patient) theInputResource;

                if (patient.getManagingOrganization() == null) {
                    throw new UnprocessableEntityException("Patient required a managing organization.");
                }
                if (!authorizedUser.getOrganizationIds().contains(patient.getManagingOrganization().getReferenceElement().getIdPart())) {
                    throw new ForbiddenOperationException("Access denied to selected managing organization.");
                }
            }
            if (theOutputResource instanceof Patient) {
                final Patient patient = (Patient) theOutputResource;

                if (patient.getManagingOrganization() == null || !authorizedUser.getOrganizationIds().contains(
                        patient.getManagingOrganization().getReferenceElement().getIdPart())) {
                    throw new ForbiddenOperationException("Access denied to selected patient.");
                }
            }
        }

        return super.applyRulesAndReturnDecision(theOperation, theRequestDetails, theInputResource, theInputResourceId, theOutputResource);
    }

    @Override
    public List<IAuthRule> buildRuleList(RequestDetails theRequestDetails) {
        final AuthorizedUser authorizedUser = getAuthorizedUser(theRequestDetails);

        if (authorizedUser.isAdmin()) {
            return new RuleBuilder().allowAll().build();
        } else {
            return new RuleBuilder().allow().metadata()
                    .andThen().allow().read().resourcesOfType(Patient.class).withAnyId()
                    .andThen().allow().write().resourcesOfType(Patient.class).withAnyId()
                    .andThen().allow().read().instances(
                            authorizedUser.getOrganizationIds().stream()
                                    .map(id -> new IdType("Organization", id))
                                    .collect(Collectors.toList())).build();
        }
    }

    @Nonnull
    private AuthorizedUser getAuthorizedUser(@Nonnull RequestDetails requestDetails) {
        final AuthorizedUser authorizedUser = (AuthorizedUser) requestDetails.getUserData().get(AuthorizedUser.ATTRIBUTE_NAME);

        if (authorizedUser == null) {
            throw new IllegalStateException("Authorized user has not been set.");
        }

        return authorizedUser;
    }

    private void validateAdminAccess(@Nonnull ServletRequestDetails theRequestDetails) {
        final String remoteIp = theRequestDetails.getServletRequest().getRemoteAddr();

        // admin access is just allowed from specific IP addresses
        if (!"127.0.0.1".equals(remoteIp) && !"0:0:0:0:0:0:0:1".equals(remoteIp)) {
            throw new AuthenticationException("Connected client not appropriate for admin access.");
        }
    }

    private List<String> getFhirOrganizationIds(@Nonnull Collection<DhisOrganisationUnit> organisationUnits) {
        if (organisationUnits.isEmpty()) {
            return Collections.emptyList();
        }

        final List<String> codes = organisationUnits.stream().filter(ou -> ou.getCode() != null)
                .map(DhisOrganisationUnit::getCode).sorted().collect(Collectors.toList());

        if (codes.isEmpty()) {
            return Collections.emptyList();
        }

        List<String> ids = organizationIdStore.getIfPresent(codes);

        if (ids != null) {
            return ids;
        }

        final TokenOrListParam identifiers = new TokenOrListParam();
        codes.forEach(c -> identifiers.add("http://example.zw/organizations", c));

        final IBundleProvider bundleProvider = organizationResourceProvider.search(null, null, null,
                null, null, null, null, null, null,
                null, null, null, null, null, null, null, null, null, null,
                new TokenAndListParam().addAnd(identifiers), null, null, null, null, null, null, null, null,
                new SortSpec().setOrder(SortOrderEnum.ASC).setParamName("_id"), null, SummaryEnum.FALSE, SearchTotalModeEnum.NONE);

        final List<IBaseResource> resources = bundleProvider.getResources(0, Integer.MAX_VALUE);

        final List<String> resultingIds = new ArrayList<>();
        resources.stream().map(r -> (Organization) r).forEach(r -> resultingIds.add(r.getIdElement().getIdPart()));

        if (resultingIds.isEmpty()) {
            return Collections.emptyList();
        }

        organizationIdStore.put(codes, resultingIds);

        return resultingIds;
    }

    private void throwAuthenticationException(@Nonnull String message) {
        throw new AuthenticationException(message)
                .addResponseHeader("WWW-Authenticate", "Basic realm=\"DHIS2\"")
                .addResponseHeader("WWW-Authenticate", "Bearer realm=\"dhis2/oauth2\"");
    }
}
