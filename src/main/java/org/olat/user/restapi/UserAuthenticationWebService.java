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

import org.olat.basesecurity.Authentication;
import org.olat.basesecurity.BaseSecurity;
import org.olat.basesecurity.OrganisationRoles;
import org.olat.core.id.Identity;
import org.olat.core.id.Roles;
import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;
import org.olat.login.auth.OLATAuthManager;
import org.olat.restapi.security.RestSecurityHelper;
import org.olat.restapi.support.ObjectFactory;
import org.olat.restapi.support.vo.AuthenticationVO;
import org.olat.restapi.support.vo.ErrorVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * This web service handles functionalities related to authentication credentials of users.
 * 
 * @author srosse, stephane.rosse@frentix.com
 */
@Component
@Path("users/{username}/auth")
public class UserAuthenticationWebService {
	
	private static final OLog log = Tracing.createLoggerFor(UserAuthenticationWebService.class);
	
	private static final String VERSION = "1.0";
	
	@Autowired
	private BaseSecurity securityManager;
	@Autowired
	private OLATAuthManager authManager;
	
	/**
	 * The version of the User Authentication Web Service
	 * @response.representation.200.mediaType text/plain
	 * @response.representation.200.doc The version of this specific Web Service
	 * @response.representation.200.example 1.0
	 * @return The version number
	 */
	@GET
	@Path("version")
	@Produces(MediaType.TEXT_PLAIN)
	public Response getVersion() {
		return Response.ok(VERSION).build();
	}

	/**
	 * Returns all user authentications
	 * @response.representation.200.qname {http://www.example.com}authenticationVO
	 * @response.representation.200.mediaType application/xml, application/json
	 * @response.representation.200.doc The list of all users in the OLAT system
	 * @response.representation.200.example {@link org.olat.restapi.support.vo.Examples#SAMPLE_AUTHVOes}
	 * @response.representation.401.doc The roles of the authenticated user are not sufficient
	 * @response.representation.404.doc The identity not found
	 * @param username The username of the user to retrieve authentication
	 * @param request The HTTP request
	 * @return
	 */
	@GET
	@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	public Response getAuthenticationTokenList(@PathParam("username") String username, @Context HttpServletRequest request) {
		Identity identity = securityManager.findIdentityByName(username);
		if(identity == null) {
			return Response.serverError().status(Status.NOT_FOUND).build();
		}
		if(!isManager(identity, request)) {
			return Response.serverError().status(Status.UNAUTHORIZED).build();
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
	 * Creates and persists an authentication
	 * @response.representation.qname {http://www.example.com}authenticationVO
	 * @response.representation.mediaType application/xml, application/json
	 * @response.representation.doc An authentication to save
	 * @response.representation.example {@link org.olat.restapi.support.vo.Examples#SAMPLE_AUTHVO}
	 * @response.representation.200.qname {http://www.example.com}authenticationVO
	 * @response.representation.200.mediaType application/xml, application/json
	 * @response.representation.200.doc The saved authentication
	 * @response.representation.200.example {@link org.olat.restapi.support.vo.Examples#SAMPLE_AUTHVO}
	 * @response.representation.401.doc The roles of the authenticated user are not sufficient
	 * @response.representation.404.doc The identity not found
	 * @response.representation.406.doc Cannot create the authentication for an unkown reason
	 * @response.representation.409.doc Cannot create the authentication because the authentication username is already used by someone else within the same provider
	 * @param username The username of the user
	 * @param authenticationVO The authentication object to persist
	 * @param request The HTTP request
	 * @return the saved authentication
	 */
	@PUT
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
		if(!identity.getName().equals(username)) {
			return Response.serverError().status(Status.NOT_FOUND).build();
		}
		
		String provider = authenticationVO.getProvider();
		String authUsername = authenticationVO.getAuthUsername();
		String credentials = authenticationVO.getCredential();
		
		Authentication currentAuthentication = securityManager.findAuthenticationByAuthusername(authUsername, provider);
		if(currentAuthentication != null) {
			if(!currentAuthentication.getIdentity().equals(identity)) {
				ErrorVO error = new ErrorVO();
				error.setCode("unkown:409");
				error.setTranslation("Authentication name used by: " + currentAuthentication.getIdentity().getUser().getEmail());
				return Response.serverError().status(Status.CONFLICT).entity(error).build();
			}
		}
		
		Authentication authentication = securityManager.createAndPersistAuthentication(identity, provider, authUsername, credentials, null);
		if(authentication == null) {
			return Response.serverError().status(Status.NOT_ACCEPTABLE).build();
		}
		log.audit("New authentication created for " + authUsername + " with provider " + provider);
		AuthenticationVO savedAuth = ObjectFactory.get(authentication, true);
		return Response.ok(savedAuth).build();
	}

	/**
	 * Deletes an authentication from the system
	 * @response.representation.200.doc The authentication successfully deleted
	 * @response.representation.401.doc The roles of the authenticated user are not sufficient
	 * @response.representation.404.doc The identity or the authentication not found
	 * @param username The username of the user
	 * @param authKey The authentication key identifier
	 * @param request The HTTP request
	 * @return <code>Response</code> object. The operation status (success or
	 *         fail)
	 */
	@DELETE
	@Path("{authKey}")
	public Response delete(@PathParam("username") String username, @PathParam("authKey") Long authKey, @Context HttpServletRequest request) {
		Identity identity = securityManager.findIdentityByName(username);
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
	 * @response.representation.200.doc The password successfully changed
	 * @response.representation.304.doc The password was not changed
	 * @response.representation.401.doc The roles of the authenticated user are not sufficient
	 * @response.representation.404.doc The identity or the authentication not found
	 * @param username The username of the user to change the password
	 * @param newPassword The new password
	 * @param request The HTTP request
	 * @return <code>Response</code> object. The operation status (success or fail)
	 */
	@POST
	@Path("password")
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
