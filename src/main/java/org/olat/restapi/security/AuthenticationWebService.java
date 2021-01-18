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
package org.olat.restapi.security;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.olat.basesecurity.AuthHelper;
import org.olat.basesecurity.BaseSecurity;
import org.olat.basesecurity.BaseSecurityModule;
import org.olat.core.gui.UserRequest;
import org.olat.core.id.Identity;
import org.olat.core.util.StringHelper;
import org.olat.login.auth.OLATAuthManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;

/**
 * 
 * Description:<br>
 * Authenticate against OLAT Provider
 * 
 * <P>
 * Initial Date:  7 apr. 2010 <br>
 * @author srosse, stephane.rosse@frentix.com
 */
@Deprecated
@Path("auth")
@Component
public class AuthenticationWebService {
	
	private static final String VERSION = "1.0";
	
	@Autowired
	private BaseSecurity securityManager;
	@Autowired
	private RestSecurityBean securityBean;
	@Autowired
	private OLATAuthManager olatAuthenticationSpi;
	
	/**
	 * Retrieves the version of the User Authentication Web Service
	 * 
	 * @return
	 */
	@GET
	@Path("version")
	@Operation(summary = "Retrieves the version of the User Authentication Web Service",
	description = "Retrieves the version of the User Authentication Web Service")
	@ApiResponse(responseCode = "200", description = "Retrieves the version of the User Authentication Web Service")	
	@Produces(MediaType.TEXT_PLAIN)
	public Response getVersion() {
		return Response.ok(VERSION).build();
	}
	
	/**
	 * Authenticates against OLAT Provider and provides a security token if
	 * authentication is successful. The security token is returned as a header
	 * named X-OLAT-TOKEN. Given that the password is sent in clear text and not
	 * encrypted, it is not advisable to use this service over a none secure
	 * connection (https).
	 * 
	 * This authentication method should only be used if basic authentication is
	 * not possible.
	 * 
	 * When using the REST API, best-practice is to use basic authentication and
	 * activate cookies in your HTTP client for automatic session management.
	 * 
	 * @param username The username
	 * @param password The password (the password is in clear text, not encrypted)
	 * @param httpRequest The HTTP request
	 * @return
	 */
	@GET
	@Path("{username}")
	@Operation(summary = "Authenticates against OLAT Provider", description = "Authenticates against OLAT Provider and provides a security token if\n" + 
			" authentication is successful. The security token is returned as a header\n" + 
			" named X-OLAT-TOKEN. Given that the password is sent in clear text and not\n" + 
			" encrypted, it is not advisable to use this service over a none secure\n" + 
			" connection (https).\n" + 
			" \n" + 
			" This authentication method should only be used if basic authentication is\n" + 
			" not possible.\n" + 
			" \n" + 
			" When using the REST API, best-practice is to use basic authentication and\n" + 
			" activate cookies in your HTTP client for automatic session management.", deprecated=true)
	@ApiResponse(responseCode = "200", description = "Say hello to the authenticated user, and\n" + 
			" *                                  give it a security token\n" + 
			" *                                  &lt;hello&gt;Hello john&lt;/hello&gt;")
	@ApiResponse(responseCode = "401", description = "The authentication has failed")
	@ApiResponse(responseCode = "404", description = "The identity not found")
	@Produces({MediaType.TEXT_PLAIN, MediaType.APPLICATION_XML})
	public Response login(@PathParam("username") String username,
			@QueryParam("password") String password,
			@QueryParam("x-olat-token") String secToken,
			@Context HttpServletRequest httpRequest) {
		
		if(StringHelper.containsNonWhitespace(password)) {
			return loginWithPassword(username, password, httpRequest);
		} else if (StringHelper.containsNonWhitespace(secToken)) {
			return loginWithToken(username, secToken, httpRequest);
		}
		return Response.serverError().status(Response.Status.BAD_REQUEST).build();
	}
	
	private Response loginWithToken(String username, String secToken, HttpServletRequest httpRequest) {
		Identity identity = securityManager.findIdentityByLogin(username);
		if(identity == null) {
			return Response.serverError().status(Status.UNAUTHORIZED).build();
		}
		
		org.olat.basesecurity.Authentication auth = securityManager.findAuthentication(identity, RestSecurityBeanImpl.REST_AUTH_PROVIDER);
		if(auth == null) {
			return Response.serverError().status(Status.UNAUTHORIZED).build();
		}
		
		if(auth.getCredential() != null && auth.getCredential().equals(secToken)) {
			UserRequest ureq = RestSecurityHelper.getUserRequest(httpRequest);
			int loginStatus = AuthHelper.doHeadlessLogin(identity, RestSecurityBeanImpl.REST_AUTH_PROVIDER, ureq, true);
			if (loginStatus == AuthHelper.LOGIN_OK) {
				return Response.ok("<hello identityKey=\"" + identity.getKey() + "\">Hello " + username + "</hello>", MediaType.APPLICATION_XML)
						.header(RestSecurityHelper.SEC_TOKEN, secToken).build();
			}
		}
		return Response.serverError().status(Status.UNAUTHORIZED).build();
	}
	
	private Response loginWithPassword(String username, String password, HttpServletRequest httpRequest) {
		UserRequest ureq = RestSecurityHelper.getUserRequest(httpRequest);
		Identity identity = olatAuthenticationSpi.authenticate(username, password);
		if(identity == null) {
			return Response.serverError().status(Status.UNAUTHORIZED).build();
		}
		
		int loginStatus = AuthHelper.doHeadlessLogin(identity, BaseSecurityModule.getDefaultAuthProviderIdentifier(), ureq, true);
		if (loginStatus == AuthHelper.LOGIN_OK) {
			securityManager.setIdentityLastLogin(identity);
			//Forge a new security token
			String token = securityBean.generateToken(identity, httpRequest.getSession(true));
			return Response.ok("<hello identityKey=\"" + identity.getKey() + "\">Hello " + username + "</hello>", MediaType.APPLICATION_XML)
				.header(RestSecurityHelper.SEC_TOKEN, token).build();
		}
		return Response.serverError().status(Status.UNAUTHORIZED).build();
	}
}
