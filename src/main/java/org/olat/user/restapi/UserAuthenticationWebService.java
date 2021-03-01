/**
 * <a href="http://www.openolat.org">
 * OpenOLAT - Online Learning and Training</a><br>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at the
 * <a href="http://www.apache.org/licenses/LICENSE-2.0">Apache homepage</a>
 * <p>
 * Unless required by applicable law or agreed to in writing,<br>
 * software distributed under the License is distributed on an "AS IS" BASIS, <br>
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
 * See the License for the specific language governing permissions and <br>
 * limitations under the License.
 * <p>
 * Initial code contributed and copyrighted by<br>
 * frentix GmbH, http://www.frentix.com
 * <p>
 */
package org.olat.user.restapi;

import static org.olat.restapi.security.RestSecurityHelper.getIdentity;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.apache.logging.log4j.Logger;
import org.olat.basesecurity.Authentication;
import org.olat.basesecurity.BaseSecurity;
import org.olat.basesecurity.OrganisationRoles;
import org.olat.core.id.Identity;
import org.olat.core.id.Roles;
import org.olat.core.logging.Tracing;
import org.olat.login.auth.OLATAuthManager;
import org.olat.restapi.security.RestSecurityHelper;
import org.olat.restapi.support.ObjectFactory;
import org.olat.restapi.support.vo.AuthenticationVO;
import org.olat.restapi.support.vo.ErrorVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * This web service handles functionalities related to authentication credentials of users.
 * 
 * @author srosse, stephane.rosse@frentix.com
 */
@Deprecated
@Tag(name = "Users")
@Component
@Path("users/{username}/auth")
public class UserAuthenticationWebService {
	
	private static final Logger log = Tracing.createLoggerFor(UserAuthenticationWebService.class);
	
	private static final String VERSION = "1.0";
	
	@Autowired
	private BaseSecurity securityManager;
	@Autowired
	private OLATAuthManager authManager;
	
	/**
	 * The version of the User Authentication Web Service
	 * 
	 * @return The version number
	 */
	@GET
	@Path("version")
	@Operation(summary = "The version of the User Authentication Web Service", description = "The version of the User Authentication Web Service")
	@ApiResponse(responseCode = "200", description = "The version of this specific Web Service")
	@Produces(MediaType.TEXT_PLAIN)
	public Response getVersion() {
		return Response.ok(VERSION).build();
	}

	/**
	 * Returns all user authentications
	 * 
	 * @param username The username of the user to retrieve authentication
	 * @param request The HTTP request
	 * @return
	 */
	@GET
	@Operation(summary = "Returns all user authentications", description = "Returns all user authentications", deprecated=true)
	@ApiResponse(responseCode = "200", description = "The list of all users in the OLAT system", content = {
			@Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = AuthenticationVO.class))),
			@Content(mediaType = "application/xml", array = @ArraySchema(schema = @Schema(implementation = AuthenticationVO.class))) })
	@ApiResponse(responseCode = "401", description = "The roles of the authenticated user are not sufficient")
	@ApiResponse(responseCode = "404", description = "The identity not found")
	@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	public Response getAuthenticationTokenList(@PathParam("username") String username, @Context HttpServletRequest request) {
		List<Authentication> identities = securityManager.findAuthenticationsByAuthusername(username, null);
		if(identities.isEmpty()) {
			return Response.serverError().status(Status.NOT_FOUND).build();
		}
		Set<Identity> identitiesSet = new HashSet<>();
		for(Authentication identity:identities) {
			if(!isManager(identity.getIdentity(), request)) {
				return Response.serverError().status(Status.UNAUTHORIZED).build();
			}
			identitiesSet.add(identity.getIdentity());
		}
		if(identitiesSet.size() > 1) {
			return Response.serverError().status(Status.CONFLICT).build();
		}
		
		Identity identity = identitiesSet.iterator().next();
		List<Authentication> authentications = securityManager.getAuthentications(identity);
		AuthenticationVO[] vos = new AuthenticationVO[authentications.size()];
		int count = 0;
		for(Authentication authentication:authentications) {
			vos[count++] = ObjectFactory.get(authentication, false);
		}
		return Response.ok(vos).build();
	}
	
	/**
	 * Creates and persists an authentication
	 *
	 * @param username The username of the user
	 * @param authenticationVO The authentication object to persist
	 * @param request The HTTP request
	 * @return the saved authentication
	 */
	@PUT
	@Operation(summary = "Creates and persists an authentication", description = "Creates and persists an authentication", deprecated=true)
	@ApiResponse(responseCode = "200", description = "The saved authentication", content = {
			@Content(mediaType = "application/json", schema = @Schema(implementation = AuthenticationVO.class)),
			@Content(mediaType = "application/xml", schema = @Schema(implementation = AuthenticationVO.class)) })
	@ApiResponse(responseCode = "401", description = "The roles of the authenticated user are not sufficient")
	@ApiResponse(responseCode = "404", description = "The identity not found") 
	@ApiResponse(responseCode = "406", description = "Cannot create the authentication for an unkown reason")
	@ApiResponse(responseCode = "409", description = "Cannot create the authentication because the authentication username is already used by someone else within the same provider")	
	@Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	public Response create(@PathParam("username") String username, AuthenticationVO authenticationVO, @Context HttpServletRequest request) {
		Identity identity = securityManager.loadIdentityByKey(authenticationVO.getIdentityKey(), false);
		if(identity == null) {
			return Response.serverError().status(Status.NOT_FOUND).build();
		}
		if(!isManager(identity, request)) {
			return Response.serverError().status(Status.UNAUTHORIZED).build();
		}
		
		String provider = authenticationVO.getProvider();
		String authUsername = authenticationVO.getAuthUsername();
		String credentials = authenticationVO.getCredential();
		
		Authentication currentAuthentication = securityManager.findAuthenticationByAuthusername(authUsername, provider, BaseSecurity.DEFAULT_ISSUER);
		if(currentAuthentication != null && !currentAuthentication.getIdentity().equals(identity)) {
			ErrorVO error = new ErrorVO();
			error.setCode("unkown:409");
			error.setTranslation("Authentication name used by: " + currentAuthentication.getIdentity().getUser().getEmail());
			return Response.serverError().status(Status.CONFLICT).entity(error).build();
		}
		
		Authentication authentication = securityManager.createAndPersistAuthentication(identity, provider, BaseSecurity.DEFAULT_ISSUER, authUsername, credentials, null);
		if(authentication == null) {
			return Response.serverError().status(Status.NOT_ACCEPTABLE).build();
		}
		log.info(Tracing.M_AUDIT, "New authentication created for {} with provider {}", authUsername, provider);
		AuthenticationVO savedAuth = ObjectFactory.get(authentication, true);
		return Response.ok(savedAuth).build();
	}

	/**
	 * Deletes an authentication from the system
	 * 
	 * @param username The username of the user
	 * @param authKey The authentication key identifier
	 * @param request The HTTP request
	 * @return <code>Response</code> object. The operation status (success or
	 *         fail)
	 */
	@DELETE
	@Path("{authKey}")
	@Operation(summary = "Deletes an authentication from the system", description = "Deletes an authentication from the system", deprecated=true)
	@ApiResponse(responseCode = "200", description = "The authentication successfully deleted")
	@ApiResponse(responseCode = "401", description = "The roles of the authenticated user are not sufficient")
	@ApiResponse(responseCode = "404", description = "The identity not found")
	public Response delete(@PathParam("username") String username, @PathParam("authKey") Long authKey, @Context HttpServletRequest request) {
		Identity identity = securityManager.findIdentityByLogin(username);
		if(identity == null) {
			return Response.serverError().status(Status.NOT_FOUND).build();
		}
		if(!isManager(identity, request)) {
			return Response.serverError().status(Status.UNAUTHORIZED).build();
		}
		
		List<Authentication> authentications = securityManager.getAuthentications(identity);
		for(Authentication authentication:authentications) {
			if(authKey.equals(authentication.getKey())) {
				securityManager.deleteAuthentication(authentication);
				return Response.ok().build();
			}
		}
		return Response.serverError().status(Status.NOT_FOUND).build();
	}
	
	/**
	 * Change the password of a user.
	 * 
	 * @param username The username of the user to change the password
	 * @param newPassword The new password
	 * @param request The HTTP request
	 * @return <code>Response</code> object. The operation status (success or fail)
	 */
	@POST
	@Path("password")
	@Operation(summary = "Change the password of a user", description = "Change the password of a user", deprecated=true)
	@ApiResponse(responseCode = "200", description = "The password successfully changed")
	@ApiResponse(responseCode = "304", description = "The password was not changed")
	@ApiResponse(responseCode = "401", description = "The roles of the authenticated user are not sufficient")
	@ApiResponse(responseCode = "404", description = "The identity or the authentication not found")
	public Response changePassword(@PathParam("username") String username, @FormParam("newPassword") String newPassword,
			@Context HttpServletRequest request) {
		Identity doer = getIdentity(request);
		if(doer == null) {
			return Response.serverError().status(Status.UNAUTHORIZED).build();
		}
		Identity identity = securityManager.findIdentityByName(username);
		if(identity == null) {
			return Response.serverError().status(Status.NOT_FOUND).build();
		}
		if(!isManager(identity, request)) {
			return Response.serverError().status(Status.UNAUTHORIZED).build();
		}
		
		boolean ok = authManager.changePassword(doer, identity, newPassword);
		return (ok ? Response.ok() : Response.notModified()).build();
	}
	
	private boolean isManager(Identity identity, HttpServletRequest request) {
		Roles managerRoles = RestSecurityHelper.getRoles(request);
		Roles identityRoles = securityManager.getRoles(identity);
		return managerRoles.isManagerOf(OrganisationRoles.usermanager, identityRoles)
				|| managerRoles.isManagerOf(OrganisationRoles.rolesmanager, identityRoles)
				|| managerRoles.isManagerOf(OrganisationRoles.administrator, identityRoles);
		
	}
}
