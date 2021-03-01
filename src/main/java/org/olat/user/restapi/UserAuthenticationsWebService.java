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

import java.util.List;

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
 * 
 * Initial date: 10 août 2020<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Tag(name = "Users")
@Component
@Path("users/{identityKey}/authentications")
public class UserAuthenticationsWebService {
	
	private static final Logger log = Tracing.createLoggerFor(UserAuthenticationsWebService.class);
	
	private static final String VERSION = "2.0";
	
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
	 * @param identityKey The identity primary key of the user to retrieve the list authentication
	 * @param request The HTTP request
	 * @return A list of authentication
	 */
	@GET
	@Operation(summary = "Returns all user authentications", description = "Returns all user authentications")
	@ApiResponse(responseCode = "200", description = "The list of all users in the OLAT system", content = {
			@Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = AuthenticationVO.class))),
			@Content(mediaType = "application/xml", array = @ArraySchema(schema = @Schema(implementation = AuthenticationVO.class))) })
	@ApiResponse(responseCode = "401", description = "The roles of the authenticated user are not sufficient")
	@ApiResponse(responseCode = "404", description = "The identity not found")
	@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	public Response getAuthenticationTokenList(@PathParam("identityKey") Long identityKey, @Context HttpServletRequest request) {
		Identity identity = securityManager.loadIdentityByKey(identityKey);
		if(identityKey == null) {
			return Response.serverError().status(Status.NOT_FOUND).build();
		}
		if(!isManager(identity, request)) {
			return Response.serverError().status(Status.FORBIDDEN).build();
		}
		
		List<Authentication> authentications = securityManager.getAuthentications(identity);
		AuthenticationVO[] vos = new AuthenticationVO[authentications.size()];
		int count = 0;
		for(Authentication authentication:authentications) {
			vos[count++] = ObjectFactory.get(authentication, false);
		}
		return Response.ok(vos).build();
	}
	
	/**
	 * Creates and persists an authentication, or update it.
	 *
	 * @param identityKey The identity key of the user
	 * @param authenticationVO The authentication object to persist
	 * @param request The HTTP request
	 * @return the saved authentication
	 */
	@PUT
	@Operation(summary = "Creates and persists an authentication, or update it if it already exists.",
			description = "Creates and persists an authentication, or update it if it already exists.")
	@ApiResponse(responseCode = "200", description = "The saved authentication", content = {
			@Content(mediaType = "application/json", schema = @Schema(implementation = AuthenticationVO.class)),
			@Content(mediaType = "application/xml", schema = @Schema(implementation = AuthenticationVO.class)) })
	@ApiResponse(responseCode = "401", description = "The roles of the authenticated user are not sufficient")
	@ApiResponse(responseCode = "404", description = "The identity not found") 
	@ApiResponse(responseCode = "406", description = "Cannot create the authentication for an unkown reason")
	@ApiResponse(responseCode = "409", description = "Cannot create the authentication because the authentication username is already used by someone else within the same provider")	
	@Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	public Response create(@PathParam("identityKey") Long identityKey, AuthenticationVO authenticationVO, @Context HttpServletRequest request) {
		return createOrUpdate(identityKey, authenticationVO, request);
	}
	
	/**
	 * Creates and persists an authentication, or update it.
	 *
	 * @param identityKey The identity key of the user
	 * @param authenticationVO The authentication object to persist
	 * @param request The HTTP request
	 * @return the saved authentication
	 */
	@POST
	@Operation(summary = "Creates and persists an authentication, or update it if it already exists.",
			description = "Creates and persists an authentication, or update it if it already exists.")
	@ApiResponse(responseCode = "200", description = "The saved authentication", content = {
			@Content(mediaType = "application/json", schema = @Schema(implementation = AuthenticationVO.class)),
			@Content(mediaType = "application/xml", schema = @Schema(implementation = AuthenticationVO.class)) })
	@ApiResponse(responseCode = "401", description = "The roles of the authenticated user are not sufficient")
	@ApiResponse(responseCode = "404", description = "The identity not found") 
	@ApiResponse(responseCode = "406", description = "Cannot create the authentication for an unkown reason")
	@ApiResponse(responseCode = "409", description = "Cannot create the authentication because the authentication username is already used by someone else within the same provider")	
	@Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	public Response update(@PathParam("identityKey") Long identityKey, AuthenticationVO authenticationVO, @Context HttpServletRequest request) {
		return createOrUpdate(identityKey, authenticationVO, request);
	}

	private Response createOrUpdate(Long identityKey, AuthenticationVO authenticationVO, HttpServletRequest request) {
		Identity identity = securityManager.loadIdentityByKey(authenticationVO.getIdentityKey(), false);
		if(identity == null) {
			return Response.serverError().status(Status.NOT_FOUND).build();
		}
		if(!isManager(identity, request)) {
			return Response.serverError().status(Status.FORBIDDEN).build();
		}
		if(!identity.getKey().equals(identityKey)) {
			return Response.serverError().status(Status.CONFLICT).build();
		}
		
		String provider = authenticationVO.getProvider();
		String authUsername = authenticationVO.getAuthUsername();
		String credentials = authenticationVO.getCredential();
		
		Authentication authentication;
		if(authenticationVO.getKey() != null) {
			Authentication currentAuthentication = securityManager.findAuthenticationByKey(authenticationVO.getKey());
			if(currentAuthentication == null) {
				return Response.serverError().status(Status.NOT_FOUND).build();
			}
			if(!currentAuthentication.getIdentity().equals(identity)) {
				return notSameIdentity(currentAuthentication);
			}
			if(!currentAuthentication.getProvider().equals(provider)) {
				return Response.serverError().status(Status.CONFLICT).build();
			}
			
			currentAuthentication.setAuthusername(authUsername);
			authentication = securityManager.updateAuthentication(currentAuthentication);
			log.info(Tracing.M_AUDIT, "Authentication created for {} with provider {}", authUsername, provider);
		} else {
			Authentication currentAuthentication = securityManager.findAuthenticationByAuthusername(authUsername, provider, BaseSecurity.DEFAULT_ISSUER);
			if(currentAuthentication != null && !currentAuthentication.getIdentity().equals(identity)) {
				return notSameIdentity(currentAuthentication);
			}
		
			authentication = securityManager.createAndPersistAuthentication(identity, provider, BaseSecurity.DEFAULT_ISSUER, authUsername, credentials, null);
			if(authentication == null) {
				return Response.serverError().status(Status.NOT_ACCEPTABLE).build();
			}
			log.info(Tracing.M_AUDIT, "New authentication created for {} with provider {}", authUsername, provider);
		}
		AuthenticationVO savedAuth = ObjectFactory.get(authentication, true);
		return Response.ok(savedAuth).build();
	}
	
	private Response notSameIdentity(Authentication currentAuthentication) {
		ErrorVO error = new ErrorVO();
		error.setCode("unkown:409");
		error.setTranslation("Authentication name used by: " + currentAuthentication.getIdentity().getUser().getEmail());
		return Response.serverError().status(Status.CONFLICT).entity(error).build();
	}

	/**
	 * Deletes an authentication from the system
	 * 
	 * @param identityKey The identity key of the user
	 * @param authKey The authentication key identifier
	 * @param request The HTTP request
	 * @return <code>Response</code> object. The operation status (success or
	 *         fail)
	 */
	@DELETE
	@Path("{authKey}")
	@Operation(summary = "Deletes an authentication from the system", description = "Deletes an authentication from the system")
	@ApiResponse(responseCode = "200", description = "The authentication successfully deleted")
	@ApiResponse(responseCode = "401", description = "The roles of the authenticated user are not sufficient")
	@ApiResponse(responseCode = "404", description = "The identity not found")
	public Response delete(@PathParam("identityKey") Long identityKey, @PathParam("authKey") Long authKey, @Context HttpServletRequest request) {
		Identity identity = securityManager.loadIdentityByKey(identityKey);
		if(identity == null) {
			return Response.serverError().status(Status.NOT_FOUND).build();
		}
		if(!isManager(identity, request)) {
			return Response.serverError().status(Status.FORBIDDEN).build();
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
	 * @param username The identity key of the user to change the password
	 * @param newPassword The new password
	 * @param request The HTTP request
	 * @return <code>Response</code> object. The operation status (success or fail)
	 */
	@POST
	@Path("password")
	@Operation(summary = "Change the password of a user", description = "Change the password of a user")
	@ApiResponse(responseCode = "200", description = "The password successfully changed")
	@ApiResponse(responseCode = "304", description = "The password was not changed")
	@ApiResponse(responseCode = "401", description = "The roles of the authenticated user are not sufficient")
	@ApiResponse(responseCode = "404", description = "The identity or the authentication not found")
	public Response changePassword(@PathParam("identityKey") Long identityKey, @FormParam("newPassword") String newPassword,
			@Context HttpServletRequest request) {
		Identity doer = getIdentity(request);
		if(doer == null) {
			return Response.serverError().status(Status.FORBIDDEN).build();
		}
		Identity identity = securityManager.loadIdentityByKey(identityKey);
		if(identity == null) {
			return Response.serverError().status(Status.NOT_FOUND).build();
		}
		if(!isManager(identity, request)) {
			return Response.serverError().status(Status.FORBIDDEN).build();
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
