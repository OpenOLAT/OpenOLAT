/**
 * OLAT - Online Learning and Training<br>
 * http://www.olat.org
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing,<br>
 * software distributed under the License is distributed on an "AS IS" BASIS, <br>
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
 * See the License for the specific language governing permissions and <br>
 * limitations under the License.
 * <p>
 * Copyright (c) frentix GmbH<br>
 * http://www.frentix.com<br>
 * <p>
 */
package org.olat.registration.restapi;

import static org.olat.restapi.security.RestSecurityHelper.getRoles;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;

import org.olat.basesecurity.BaseSecurity;
import org.olat.basesecurity.OrganisationRoles;
import org.olat.basesecurity.model.IdentityRefImpl;
import org.olat.core.id.Identity;
import org.olat.core.id.Roles;
import org.olat.core.id.UserConstants;
import org.olat.login.LoginModule;
import org.olat.registration.RegistrationManager;
import org.olat.registration.TemporaryKey;
import org.olat.user.UserModule;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;


/**
 * Webservice to create a temporary key to change the password
 * 
 * Initial date: 15.10.2013<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Component
@Path("pwchange")
public class ChangePasswordWebService {
	
	@Autowired
	private UserModule userModule;
	@Autowired
	private BaseSecurity securityManager;
	@Autowired
	private RegistrationManager rm;
	@Autowired
	private LoginModule loginModule;
	
	/**
	 * 
	 * @param identityKey
	 * @param request
	 * @return
	 */
	@PUT
	@Operation(summary = "Change password", description = "Change password")
	@ApiResponse(responseCode = "200", description = "Password has been changed")
	@ApiResponse(responseCode = "401", description = "The roles of the authenticated user are not sufficient")	
	@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	public Response register(@QueryParam("identityKey") Long identityKey, @Context HttpServletRequest request) {
		if(!isUserManagerOf(identityKey, request)) {
			return Response.serverError().status(Status.UNAUTHORIZED).build();
		}

		Identity identity = securityManager.loadIdentityByKey(identityKey);
		if(identity == null) {
			return Response.serverError().status(Status.NOT_FOUND).build();
		}
		if(!userModule.isPwdChangeAllowed(identity)) {
			return Response.serverError().status(Status.FORBIDDEN).build();
		}

		String emailAdress = identity.getUser().getProperty(UserConstants.EMAIL, null); 
		String ip = request.getRemoteAddr();
		TemporaryKey tk = rm.createAndDeleteOldTemporaryKey(identity.getKey(), emailAdress, ip,
				RegistrationManager.PW_CHANGE, loginModule.getValidUntilHoursRest());
		return Response.ok(new TemporaryKeyVO(tk)).build();
	}
	
	private boolean isUserManagerOf(Long identityKey, HttpServletRequest request) {
		if(identityKey == null) return false;
		
		Roles managerRoles = getRoles(request);
		if(!managerRoles.isUserManager() && !managerRoles.isRolesManager() && !managerRoles.isAdministrator()) {
			return false;
		}
		Roles identityRoles = securityManager.getRoles(new IdentityRefImpl(identityKey));
		return managerRoles.isManagerOf(OrganisationRoles.administrator, identityRoles)
				|| managerRoles.isManagerOf(OrganisationRoles.usermanager, identityRoles)
				|| managerRoles.isManagerOf(OrganisationRoles.rolesmanager, identityRoles);
		
	}
}
