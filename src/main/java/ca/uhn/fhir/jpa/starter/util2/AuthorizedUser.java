package ca.uhn.fhir.jpa.starter.util2;

import java.io.Serializable;
import java.util.Set;
import javax.annotation.Nonnull;
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
/**
 *
 * @author developer
 */
public class AuthorizedUser implements Serializable {

    public static final String ATTRIBUTE_NAME = AuthorizedUser.class.getName();

    private static final long serialVersionUID = -2003663083946137408L;

    private final DhisUser dhisUser;

    private final Set<String> organizationIds;

    private final boolean admin;

    public AuthorizedUser(@Nonnull DhisUser dhisUser, @Nonnull Set<String> organizationIds, boolean admin) {
        this.dhisUser = dhisUser;
        this.organizationIds = organizationIds;
        this.admin = admin;
    }

    @Nonnull
    public DhisUser getDhisUser() {
        return dhisUser;
    }

    @Nonnull
    public Set<String> getOrganizationIds() {
        return organizationIds;
    }

    public boolean isAdmin() {
        return admin;
    }
}
