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
import static org.olat.restapi.security.RestSecurityHelper.getLocale;
import static org.olat.restapi.security.RestSecurityHelper.getRoles;
import static org.olat.restapi.security.RestSecurityHelper.getUserRequest;
import static org.olat.restapi.security.RestSecurityHelper.itself;
import static org.olat.user.restapi.UserVOFactory.formatDbUserProperty;
import static org.olat.user.restapi.UserVOFactory.get;
import static org.olat.user.restapi.UserVOFactory.getManaged;
import static org.olat.user.restapi.UserVOFactory.parseUserProperty;
import static org.olat.user.restapi.UserVOFactory.post;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.HEAD;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.CacheControl;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;

import org.apache.logging.log4j.Logger;
import org.olat.admin.user.UserShortDescription;
import org.olat.basesecurity.Authentication;
import org.olat.basesecurity.BaseSecurity;
import org.olat.basesecurity.BaseSecurityModule;
import org.olat.basesecurity.IdentityPowerSearchQueries;
import org.olat.basesecurity.OrganisationRoles;
import org.olat.basesecurity.OrganisationService;
import org.olat.basesecurity.SearchIdentityParams;
import org.olat.basesecurity.model.IdentityRefImpl;
import org.olat.core.CoreSpringFactory;
import org.olat.core.commons.persistence.DB;
import org.olat.core.gui.components.form.ValidationError;
import org.olat.core.gui.translator.PackageTranslator;
import org.olat.core.gui.translator.Translator;
import org.olat.core.id.Identity;
import org.olat.core.id.Organisation;
import org.olat.core.id.Preferences;
import org.olat.core.id.Roles;
import org.olat.core.id.RolesByOrganisation;
import org.olat.core.id.User;
import org.olat.core.id.UserConstants;
import org.olat.core.logging.Tracing;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.login.auth.AuthenticationProviderSPI;
import org.olat.restapi.group.MyGroupWebService;
import org.olat.restapi.support.MultipartReader;
import org.olat.restapi.support.vo.ErrorVO;
import org.olat.user.DisplayPortraitManager;
import org.olat.user.UserLifecycleManager;
import org.olat.user.UserManager;
import org.olat.user.propertyhandlers.UserPropertyHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * This web service handles functionalities related to <code>User</code>.
 * 
 * @author srosse, stephane.rosse@frentix.com
 */

@Tag (name = "Users")
@Path("users")
@Component
public class UserWebService {
	
	private static final Logger log = Tracing.createLoggerFor(UserWebService.class);
	
	private static final String VERSION = "1.0";
	
	public static final String PROPERTY_HANDLER_IDENTIFIER = UserWebService.class.getName();
	
	public static final CacheControl cc = new CacheControl();
	static {
		cc.setMaxAge(-1);
	}
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private UserManager userManager;
	@Autowired
	private BaseSecurity securityManager;
	@Autowired
	private BaseSecurityModule securityModule;
	@Autowired
	private DisplayPortraitManager portraitManager;
	@Autowired
	private OrganisationService organisationService;
	@Autowired
	private UserLifecycleManager userLifecycleManager;
	@Autowired
	private IdentityPowerSearchQueries identitySearchQueries;
	
	/**
	 * The version of the User Web Service
	 * 
	 * @return The version number
	 */
	@GET
	@Path("version")
	@Operation(summary = "The version of the User Web Service", description = "The version of the User Web Service")
	@ApiResponse(responseCode = "200", description = "The version of this specific Web Service")
	@Produces(MediaType.TEXT_PLAIN)
	public Response getVersion() {
		return Response.ok(VERSION).build();
	}
	
	/**
	 * Search users and return them in a simple form (without user properties). User properties
	 * can be added two the query parameters. If the authUsername and the authProvider are set,
	 * the search is made only with these two parameters because they are sufficient to return
	 * a single user.<br>
	 * The search with login and user properties are made default with wild cards. If an exact
	 * match is needed, the parameter msut be quoted:<br>
	 * users?login="username"<br>
	 * Don't forget the right escaping in the URL!<br>
	 * You can make a search with the user properties like this:<br>
	 * users?telMobile=39847592&login=test
	 * <br >/ The lookup is possible for authors, usermanagers and system administrators. Normal
	 * users are not allowed to use the lookup service.
	 * 
	 * @param login The login (search with like)
	 * @param authProvider An authentication provider (optional)
	 * @param authUsername An specific username from the authentication provider
	 * @param uriInfo The URI infos
	 * @param httpRequest The HTTP request
	 * @return An array of users
	 */
	@GET
	@Operation(summary = "Search users and return them in a simple form ", description = "Search users and return them in a simple form (without user properties). User properties\n" + 
			" can be added two the query parameters. If the authUsername and the authProvider are set,\n" + 
			" the search is made only with these two parameters because they are sufficient to return\n" + 
			" a single user.<br>\n" + 
			" The search with login and user properties are made default with wild cards. If an exact\n" + 
			" match is needed, the parameter msut be quoted:<br>\n" + 
			" users?login=\"username\"<br>\n" + 
			" Don't forget the right escaping in the URL!<br>\n" + 
			" You can make a search with the user properties like this:<br>\n" + 
			" users?telMobile=39847592&login=test\n" + 
			" <br> The lookup is possible for authors, usermanagers and system administrators. Normal\n" + 
			" users are not allowed to use the lookup service.")
	@ApiResponse(responseCode = "200", description = "The list of all users in the OLAT system", content = {
			@Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = UserVO.class))),
			@Content(mediaType = "application/xml", array = @ArraySchema(schema = @Schema(implementation = UserVO.class))) })
	@ApiResponse(responseCode = "401", description = "The roles of the authenticated user are not sufficient")
	@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	public Response getUserListQuery(@QueryParam("login") String login, @QueryParam("externalId") String externalId,
			@QueryParam("authProvider") String authProvider, @QueryParam("authUsername") String authUsername,
			@QueryParam("statusVisibleLimit") String statusVisibleLimit,
			@Context UriInfo uriInfo, @Context HttpServletRequest httpRequest) {

		// User lookup allowed for authors, usermanagers and admins. For
		// usernamanger and up are considered "administrative" when it comes to
		// lookup of the user properties
		Roles roles = getRoles(httpRequest);
		if(!roles.isAdministrator() && !roles.isUserManager() && !roles.isRolesManager() && !roles.isAuthor()) {
			return Response.serverError().status(Status.UNAUTHORIZED).build();
		}
		
		MultivaluedMap<String,String> params = uriInfo.getQueryParameters();
		List<Identity> identities;
		//make only a search by authUsername
		if(StringHelper.containsNonWhitespace(authProvider) && StringHelper.containsNonWhitespace(authUsername)) {
			Authentication auth = securityManager.findAuthenticationByAuthusername(authUsername, authProvider, BaseSecurity.DEFAULT_ISSUER);
			if(auth == null) {
				identities = Collections.emptyList();
			} else {
				identities = Collections.singletonList(auth.getIdentity());
			}
		} else {

			boolean isAdministrativeUser = securityModule.isUserAllowedAdminProps(roles);
			
			String[] authProviders = null;
			if(StringHelper.containsNonWhitespace(authProvider)) {
				authProviders = new String[]{authProvider};
			}
			
			//retrieve and convert the parameters value
			Map<String,String> userProps = new HashMap<>();
			if(!params.isEmpty()) {
				Locale locale = getLocale(httpRequest);
				List<UserPropertyHandler> propertyHandlers = userManager
						.getUserPropertyHandlersFor(PROPERTY_HANDLER_IDENTIFIER, isAdministrativeUser);
				for(UserPropertyHandler handler:propertyHandlers) {
					if(!params.containsKey(handler.getName())) continue;
					
					List<String> values = params.get(handler.getName());
					if(!values.isEmpty()) {
						String value = formatDbUserProperty(values.get(0), handler, locale);
						userProps.put(handler.getName(), value);
					}
				}
			}
			
			Integer status = Identity.STATUS_VISIBLE_LIMIT;
			if(isAdministrativeUser && "all".equalsIgnoreCase(statusVisibleLimit)) {
				status = null;
			}
			
			SearchIdentityParams searchParams = new SearchIdentityParams(login, userProps, true,
					null, null, authProviders, null, null, null, null, status);
			if(StringHelper.containsNonWhitespace(externalId)) {
				searchParams.setExternalId(externalId);
			}
			identities = securityManager.getIdentitiesByPowerSearch(searchParams, 0, -1);
		}
		
		int count = 0;
		UserVO[] userVOs = new UserVO[identities.size()];
		for(Identity identity:identities) {
			userVOs[count++] = get(identity);
		}
		return Response.ok(userVOs).build();
	}
	
	@GET
	@Path("managed")
	@Operation(summary = "Get managed Users", description = "Get managed Users")
	@ApiResponse(responseCode = "200", description = "List of all managed users", content = {
			@Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = ManagedUserVO.class))),
			@Content(mediaType = "application/xml", array = @ArraySchema(schema = @Schema(implementation = ManagedUserVO.class))) })
	@ApiResponse(responseCode = "401", description = "The roles of the authenticated user are not sufficient")
	@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	public Response getManagedUsers(@Context HttpServletRequest httpRequest) {
		if(!isUserManager(httpRequest)) {
			return Response.serverError().status(Status.UNAUTHORIZED).build();
		}
		
		SearchIdentityParams params = new SearchIdentityParams();
		params.setManaged(Boolean.TRUE);
		List<Identity> identities = identitySearchQueries.getIdentitiesByPowerSearch(params, 0, -1);
		int count = 0;
		ManagedUserVO[] userVOs = new ManagedUserVO[identities.size()];
		for(Identity identity:identities) {
			userVOs[count++] = getManaged(identity);
		}
		return Response.ok(userVOs).build();
	}
	
	@GET
	@Path("me")
	@Operation(summary = "Informations of the logged in user", description = "Get the user informations of the currenntly log in user")
	@ApiResponse(responseCode = "200", description = "Informations of the logged in user", content = {
			@Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = UserVO.class))),
			@Content(mediaType = "application/xml", array = @ArraySchema(schema = @Schema(implementation = UserVO.class))) })
	@ApiResponse(responseCode = "403", description = "The roles of the authenticated user are not sufficient")
	@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	public Response getMe(@Context HttpServletRequest httpRequest) {
		Identity identity = getIdentity(httpRequest);
		if(identity == null) {
			return Response.serverError().status(Status.FORBIDDEN).build();
		}
		return Response.ok(get(identity, true, false)).build();
	}
	
	/**
	 * Creates and persists a new user entity
	 * 
	 * @param user The user to persist
	 * @param request The HTTP request
	 * @return the new persisted <code>User</code>
	 */
	@PUT
	@Operation(summary = "Creates and persists a new user entity", description = "Creates and persists a new user entity")
	@ApiResponse(responseCode = "200", description = "The persisted user", content = {
			@Content(mediaType = "application/json", schema = @Schema(implementation = UserVO.class)),
			@Content(mediaType = "application/xml", schema = @Schema(implementation = UserVO.class)) })
	@ApiResponse(responseCode = "401", description = "The roles of the authenticated user are not sufficient")
	@ApiResponse(responseCode = "406", description = "The list of errors", content = {
			@Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = ErrorVO.class))),
			@Content(mediaType = "application/xml", array = @ArraySchema(schema = @Schema(implementation = ErrorVO.class))) })
	@Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	public Response create(UserVO user, @Context HttpServletRequest request) {
		if(!isUserManager(request)) {
			return Response.serverError().status(Status.UNAUTHORIZED).build();
		}
		
		if (!syntaxCheckOlatLogin(user)) {
			Locale locale = getLocale(request);
			Translator translator = Util.createPackageTranslator(UserShortDescription.class, locale);
			String translation = translator.translate("new.error.loginname.empty");
			ErrorVO[] errorVos = new ErrorVO[]{
				new ErrorVO("org.olat.admin.user", "new.error.loginname.empty", translation)
			};
			return Response.ok(errorVos).status(Status.NOT_ACCEPTABLE).build();
		}
			
		// Check if login and nickname are still available (DB constraints on both)
		Identity identityLogin = securityManager.findIdentityByLogin(user.getLogin());
		Identity identityNickname = securityManager.findIdentityByNickName(user.getLogin());
		if (identityLogin != null || identityNickname != null) {
			Locale locale = getLocale(request);
			Translator translator = Util.createPackageTranslator(UserShortDescription.class, locale);
			String translation = translator.translate("new.error.loginname.choosen");
			ErrorVO[] errorVos = new ErrorVO[]{
				new ErrorVO("org.olat.admin.user", "new.error.loginname.choosen", translation)
			};
			return Response.ok(errorVos).status(Status.NOT_ACCEPTABLE).build();
		}
		
		List<ErrorVO> errors = validateUser(null, user, request);
		if(errors.isEmpty()) {
			User newUser = userManager.createUser(user.getFirstName(), user.getLastName(), user.getEmail());
			
			String identityName = securityModule.isIdentityNameAutoGenerated() ? null : user.getLogin();
			String provider = StringHelper.containsNonWhitespace(user.getPassword()) ? BaseSecurityModule.getDefaultAuthProviderIdentifier() : null;
			Identity id = securityManager
					.createAndPersistIdentityAndUserWithOrganisation(identityName, user.getLogin(), user.getExternalId(), newUser,
							provider, BaseSecurity.DEFAULT_ISSUER, user.getLogin(), user.getPassword(), null, null);
			post(newUser, user, getLocale(request));
			userManager.updateUser(id, newUser);
			return Response.ok(get(id)).build();
		}
		
		//content not ok
		ErrorVO[] errorVos = new ErrorVO[errors.size()];
		errors.toArray(errorVos);
		return Response.ok(errorVos).status(Status.NOT_ACCEPTABLE).build();
	}
	
	/**
	 * This a minimal syntax check specific to the REST API.
	 * 
	 * @param user
	 * @return
	 */
	private boolean syntaxCheckOlatLogin(UserVO user) {
		String login = user.getLogin();
		if(!StringHelper.containsNonWhitespace(login)) {
			return false;
		}
		
		char[] charArr = login.toCharArray();
		for(char ch:charArr) {
			if(ch == '/') {
				return false;
			}
			if(ch < 32) {
				return false;
			}
			
		}
		if(login.indexOf('/') >= 0) {
			return false;
		}
		return login.length() >= 3;
	}
	
	/**
	 * Retrieves the roles of a user given its unique key identifier
	 * 
	 * @param identityKey The user key identifier of the user being searched
	 * @param httpRequest The HTTP request
	 * @return an xml or json representation of a the roles being search.
	 */
	@GET
	@Path("{identityKey}/roles")
	@Operation(summary = "Retrieve the roles", description = "Retrieves the roles of a user given its unique key identifier")
	@ApiResponse(responseCode = "200", description = "The user", content = {
			@Content(mediaType = "application/json", schema = @Schema(implementation = RolesVO.class)),
			@Content(mediaType = "application/xml", schema = @Schema(implementation = RolesVO.class)) })
	@ApiResponse(responseCode = "401", description = "The roles of the authenticated user are not sufficient")
	@ApiResponse(responseCode = "404", description = "The identity not found")
	@Produces({MediaType.APPLICATION_XML ,MediaType.APPLICATION_JSON})
	public Response getIdentityRoles(@PathParam("identityKey") Long identityKey, @Context HttpServletRequest request) {
		if(!isUserManagerOf(identityKey, request) && !itself(identityKey, request)) {
			return Response.serverError().status(Status.FORBIDDEN).build();
		}
		Identity identity = securityManager.loadIdentityByKey(identityKey, false);
		if(identity == null) {
			return Response.serverError().status(Status.NOT_FOUND).build();
		}

		Roles roles = securityManager.getRoles(identity);
		return Response.ok(new RolesVO(roles)).build();
	}
	
	/**
	 * Update the roles of a user given its unique key identifier
	 * 
	 * @param identityKey The user key identifier of the user being searched
	 * @param roles The updated roles
	 * @param httpRequest The HTTP request
	 * @return an xml or json representation of a the roles being search.
	 */
	@POST
	@Path("{identityKey}/roles")
	@Operation(summary = "Update the roles", description = "Update the roles of a user given its unique key identifier")
	@ApiResponse(responseCode = "200", description = "The user", content = {
			@Content(mediaType = "application/json", schema = @Schema(implementation = RolesVO.class)),
			@Content(mediaType = "application/xml", schema = @Schema(implementation = RolesVO.class)) })
	@ApiResponse(responseCode = "401", description = "The roles of the authenticated user are not sufficient")
	@ApiResponse(responseCode = "404", description = "The identity not found")
	@Consumes({MediaType.APPLICATION_XML ,MediaType.APPLICATION_JSON})
	@Produces({MediaType.APPLICATION_XML ,MediaType.APPLICATION_JSON})
	public Response updateRoles(@PathParam("identityKey") Long identityKey, RolesVO roles, @Context HttpServletRequest request) {
		boolean isUserManager = isUserManagerOf(identityKey, request);
		if(!isUserManager) {
			return Response.serverError().status(Status.FORBIDDEN).build();
		}
		Identity identity = securityManager.loadIdentityByKey(identityKey, false);
		if(identity == null) {
			return Response.serverError().status(Status.NOT_FOUND).build();
		}
		
		Identity actingIdentity = getIdentity(request);
		Organisation defOrganisation = organisationService.getDefaultOrganisation();
		boolean userRole = !roles.isGuestOnly() && !roles.isInvitee();
		RolesByOrganisation modifiedRoles = RolesByOrganisation.roles(defOrganisation,
				roles.isGuestOnly(), roles.isInvitee(), userRole, roles.isAuthor(),
				roles.isGroupManager(), roles.isPoolAdmin(), roles.isCurriculumManager(),
				roles.isUserManager(), roles.isInstitutionalResourceManager(), roles.isOlatAdmin());
		securityManager.updateRoles(actingIdentity, identity, modifiedRoles);
		dbInstance.commit();// make sure all is committed before loading the roles again
		Roles updatedRoles = securityManager.getRoles(identity);
		return Response.ok(new RolesVO(updatedRoles)).build();
	}
	
	/**
	 * Retrieves the status of a user given its unique key identifier
	 * 
	 * @param identityKey The user key identifier of the user being searched
	 * @param httpRequest The HTTP request
	 * @return an xml or json representation of a the status being search.
	 */
	@GET
	@Path("{identityKey}/status")
	@Operation(summary = "Retrieve the status", description = "Retrieves the status of a user given its unique key identifier")
	@ApiResponse(responseCode = "200", description = "The user", content = {
			@Content(mediaType = "application/json", schema = @Schema(implementation = StatusVO.class)),
			@Content(mediaType = "application/xml", schema = @Schema(implementation = StatusVO.class)) })
	@ApiResponse(responseCode = "401", description = "The roles of the authenticated user are not sufficient")
	@ApiResponse(responseCode = "404", description = "The identity not found")
	@Produces({MediaType.APPLICATION_XML ,MediaType.APPLICATION_JSON})
	public Response getStatus(@PathParam("identityKey") Long identityKey, @Context HttpServletRequest request) {
		boolean isUserManager = isUserManagerOf(identityKey, request);
		if(!isUserManager) {
			return Response.serverError().status(Status.FORBIDDEN).build();
		}
		Identity identity = securityManager.loadIdentityByKey(identityKey, false);
		if(identity == null) {
			return Response.serverError().status(Status.NOT_FOUND).build();
		}

		StatusVO status = new StatusVO();
		status.setStatus(identity.getStatus());
		return Response.ok(status).build();
	}
	
	/**
	 * Update the roles of a user given its unique key identifier:
	 * <ul>
	 * 	<li>1: Permanent user</li> 
	 * 	<li>2: activ</li> 
	 *  <li>101: login denied</li> 
	 *  <li>199: deleted</li> 
	 * </ul>
	 * 
	 * @param identityKey The user key identifier of the user being searched
	 * @param status The status to update
	 * @param httpRequest The HTTP request
	 * @return An xml or json representation of a the status after update.
	 */
	@POST
	@Path("{identityKey}/status")
	@Operation(summary = "Update the roles of a user", description = "Update the roles of a user given its unique key identifier:\n" + 
			" <ul>\n" + 
			"  <li>1: Permanent user</li> \n" + 
			"  <li>2: activ</li> \n" + 
			"  <li>101: login denied</li> \n" + 
			"  <li>199: deleted</li> \n" + 
			" </ul>\n")
	@ApiResponse(responseCode = "200", description = "The user", content = {
			@Content(mediaType = "application/json", schema = @Schema(implementation = StatusVO.class)),
			@Content(mediaType = "application/xml", schema = @Schema(implementation = StatusVO.class)) })
	@ApiResponse(responseCode = "401", description = "The roles of the authenticated user are not sufficient")
	@ApiResponse(responseCode = "404", description = "The identity not found")
	@Consumes({MediaType.APPLICATION_XML ,MediaType.APPLICATION_JSON})
	@Produces({MediaType.APPLICATION_XML ,MediaType.APPLICATION_JSON})
	public Response updateStatus(@PathParam("identityKey") Long identityKey, StatusVO status, @Context HttpServletRequest request) {
		Identity actingIdentity = getIdentity(request);
		boolean isUserManager = isUserManagerOf(identityKey, request);
		if(actingIdentity == null || !isUserManager) {
			return Response.serverError().status(Status.FORBIDDEN).build();
		}
		Identity identity = securityManager.loadIdentityByKey(identityKey, false);
		if(identity == null) {
			return Response.serverError().status(Status.NOT_FOUND).build();
		}
		
		Integer newStatus = status.getStatus();
		identity = securityManager.saveIdentityStatus(identity, newStatus, actingIdentity);
		StatusVO reloadedStatus = new StatusVO();
		reloadedStatus.setStatus(identity.getStatus());
		return Response.ok(reloadedStatus).build();
	}
	
	/**
	 * Retrieves the preferences of a user given its unique key identifier
	 * 
	 * @param identityKey The user key identifier of the user being searched
	 * @param httpRequest The HTTP request
	 * @return an xml or json representation of a the roles being search.
	 */
	@GET
	@Path("{identityKey}/preferences")
	@Operation(summary = "Retrieve the preferences", description = "Retrieves the preferences of a user given its unique key identifier")
	@ApiResponse(responseCode = "200", description = "The preferences", content = {
			@Content(mediaType = "application/json", schema = @Schema(implementation = PreferencesVO.class)),
			@Content(mediaType = "application/xml", schema = @Schema(implementation = PreferencesVO.class)) })
	@ApiResponse(responseCode = "401", description = "The roles of the authenticated user are not sufficient")
	@ApiResponse(responseCode = "404", description = "The identity not found")
	@Produces({MediaType.APPLICATION_XML ,MediaType.APPLICATION_JSON})
	public Response getUserPreferences(@PathParam("identityKey") Long identityKey, @Context HttpServletRequest request) {
		boolean isUserManager = isUserManagerOf(identityKey, request);
		if(!isUserManager) {
			return Response.serverError().status(Status.FORBIDDEN).build();
		}
		
		Identity identity = securityManager.loadIdentityByKey(identityKey, false);
		if(identity == null) {
			return Response.serverError().status(Status.NOT_FOUND).build();
		}
		
		Preferences prefs = identity.getUser().getPreferences();
		return Response.ok(new PreferencesVO(prefs)).build();
	}
	
	/**
	 * Update the preferences of a user given its unique key identifier
	 * 
	 * @param identityKey The user key identifier of the user being searched
	 * @param preferences The updated preferences
	 * @param httpRequest The HTTP request
	 * @return an xml or json representation of a the roles being search.
	 */
	@POST
	@Path("{identityKey}/preferences")
	@Operation(summary = "Update the preferences", description = "Update the preferences of a user given its unique key identifier")
	@ApiResponse(responseCode = "200", description = "The user", content = {
			@Content(mediaType = "application/json", schema = @Schema(implementation = PreferencesVO.class)),
			@Content(mediaType = "application/xml", schema = @Schema(implementation = PreferencesVO.class)) })
	@ApiResponse(responseCode = "401", description = "The roles of the authenticated user are not sufficient")
	@ApiResponse(responseCode = "404", description = "The identity not found")
	@Consumes({MediaType.APPLICATION_XML ,MediaType.APPLICATION_JSON})
	@Produces({MediaType.APPLICATION_XML ,MediaType.APPLICATION_JSON})
	public Response updatePreferences(@PathParam("identityKey") Long identityKey, PreferencesVO preferences, @Context HttpServletRequest request) {
		boolean isUserManager = isUserManagerOf(identityKey, request);
		if(!isUserManager) {
			return Response.serverError().status(Status.FORBIDDEN).build();
		}
		Identity identity = securityManager.loadIdentityByKey(identityKey, false);
		if(identity == null) {
			return Response.serverError().status(Status.NOT_FOUND).build();
		}

		Preferences prefs = identity.getUser().getPreferences();
		prefs.setLanguage(preferences.getLanguage());
		userManager.updateUserFromIdentity(identity);
		return Response.ok(new PreferencesVO(prefs)).build();
	}

	@Path("{identityKey}/relations")
	public IdentityToIdentityRelationsWebService getRelations(@PathParam("identityKey") Long identityKey, @Context HttpServletRequest request) {
		boolean isUserManager = isUserManagerOf(identityKey, request);
		if(!isUserManager) {
			throw new WebApplicationException(Status.FORBIDDEN);
		}
		Identity identity = securityManager.loadIdentityByKey(identityKey, false);
		if(identity == null) {
			throw new WebApplicationException(Status.NOT_FOUND);
		}
		return new IdentityToIdentityRelationsWebService(identity);
	}

	/**
	 * Retrieves an user given its unique key identifier
	 * 
	 * @param identityKey The user key identifier of the user being searched
	 * @param withPortrait If true return the portrait as Base64 (default false)
	 * @param httpRequest The HTTP request
	 * @return an xml or json representation of a the user being search. The xml
	 *         correspond to a <code>UserVO</code>. <code>UserVO</code> is a
	 *         simplified representation of the <code>User</code> and <code>Identity</code>
	 */
	@GET
	@Path("{identityKey}")
	@Operation(summary = "Retrieve a user", description = "Retrieves an user given its unique key identifier")
	@ApiResponse(responseCode = "200", description = "The user", content = {
			@Content(mediaType = "application/json", schema = @Schema(implementation = UserVO.class)),
			@Content(mediaType = "application/xml", schema = @Schema(implementation = UserVO.class)) })
	@ApiResponse(responseCode = "401", description = "The roles of the authenticated user are not sufficient")
	@ApiResponse(responseCode = "404", description = "The identity not found")
	@Produces({MediaType.APPLICATION_XML ,MediaType.APPLICATION_JSON})
	public Response findById(@PathParam("identityKey") Long identityKey, @QueryParam("withPortrait") @DefaultValue("false") Boolean withPortrait,
			@Context HttpServletRequest httpRequest) {
		Identity identity = securityManager.loadIdentityByKey(identityKey, false);
		if(identity == null) {
			return Response.serverError().status(Status.NOT_FOUND).build();
		}
		
		boolean isUserManager = isUserManagerOf(identityKey, httpRequest);
		UserVO userVO = get(identity, null, true, isUserManager, withPortrait);
		return Response.ok(userVO).build();
	}
	
	@GET
	@Path("{identityKey}/lifecycle")
	@Operation(summary = "Get lifecycle information of user", description = "Get lifecycle information of user")
	@ApiResponse(responseCode = "200", description = "The user", content = {
			@Content(mediaType = "application/json", schema = @Schema(implementation = UserLifecycleVO.class)),
			@Content(mediaType = "application/xml", schema = @Schema(implementation = UserLifecycleVO.class)) })
	@ApiResponse(responseCode = "403", description = "The roles of the authenticated user are not sufficient")
	@ApiResponse(responseCode = "404", description = "The identity not found")
	@Produces({MediaType.APPLICATION_XML ,MediaType.APPLICATION_JSON})
	public Response getUserLifecycle(@PathParam("identityKey") Long identityKey,
			@Context HttpServletRequest httpRequest) {
		Identity identity = securityManager.loadIdentityByKey(identityKey, false);
		if(identity == null) {
			return Response.serverError().status(Status.NOT_FOUND).build();
		}
		if(!isUserManagerOf(identityKey, httpRequest)) {
			return Response.serverError().status(Status.FORBIDDEN).build();
		}
		UserLifecycleVO lifecycleVO = UserLifecycleVO.valueOf(identity);
		return Response.ok(lifecycleVO).build();
	}
	
	/**
	 * Update a user expiration date and only the expiration date.
	 * 
	 * @param identityKey The user key identifier
	 * @param lifecycle The user life-cycle data
	 * @param request The HTTP request
	 * @return <code>User</code> object. The operation status (success or fail)
	 */
	@POST
	@Path("{identityKey}/lifecycle")
	@Operation(summary = "Update a user's expiration date", description = "Update a user expiration date and only the expiration date.")
	@ApiResponse(responseCode = "200", description = "The user", content = {
			@Content(mediaType = "application/json", schema = @Schema(implementation = UserLifecycleVO.class)),
			@Content(mediaType = "application/xml", schema = @Schema(implementation = UserLifecycleVO.class)) })
	@ApiResponse(responseCode = "403", description = "The roles of the authenticated user are not sufficient")
	@ApiResponse(responseCode = "404", description = "The identity not found")
	@Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	public Response postLifecycle(@PathParam("identityKey") Long identityKey, UserLifecycleVO lifecycle, @Context HttpServletRequest request) {
		return update(identityKey, lifecycle, request);
	}
	
	/**
	 * Update a user expiration date and only the expiration date.
	 * 
	 * @param identityKey The user key identifier
	 * @param lifecycle The user life-cycle data
	 * @param request The HTTP request
	 * @return <code>User</code> object. The operation status (success or fail)
	 */
	@PUT
	@Path("{identityKey}/lifecycle")
	@Operation(summary = "Update a user's expiration date", description = "Update a user expiration date and only the expiration date.")
	@ApiResponse(responseCode = "200", description = "The user", content = {
			@Content(mediaType = "application/json", schema = @Schema(implementation = UserLifecycleVO.class)),
			@Content(mediaType = "application/xml", schema = @Schema(implementation = UserLifecycleVO.class)) })
	@ApiResponse(responseCode = "403", description = "The roles of the authenticated user are not sufficient")
	@ApiResponse(responseCode = "404", description = "The identity not found")
	@Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	public Response putLifecycle(@PathParam("identityKey") Long identityKey, UserLifecycleVO lifecycle, @Context HttpServletRequest request) {
		return update(identityKey, lifecycle, request);
	}
	
	private Response update(Long identityKey, UserLifecycleVO lifecycle, HttpServletRequest request) {
		if(lifecycle == null) {
			return Response.serverError().status(Status.NO_CONTENT).build();
		}
		if(!isUserManagerOf(identityKey, request)) {
			return Response.serverError().status(Status.FORBIDDEN).build();
		}

		Identity retrievedIdentity = securityManager.loadIdentityByKey(identityKey, false);
		if(retrievedIdentity == null) {
			return Response.serverError().status(Status.NOT_FOUND).build();
		}
		
		Identity updatedIdentity = securityManager.saveIdentityExpirationDate(retrievedIdentity, lifecycle.getExpirationDate(), getIdentity(request));
		UserLifecycleVO lifecycleVo = UserLifecycleVO.valueOf(updatedIdentity);
		return Response.ok(lifecycleVo).build();
	}
	
	
	@Path("{identityKey}/folders")
	@Operation(summary = "Retrieve folders", description = "Retrieves folders from a user given its unique key identifier")
	@ApiResponse(responseCode = "200", description = "The folders")
	@ApiResponse(responseCode = "401", description = "The roles of the authenticated user are not sufficient")
	@ApiResponse(responseCode = "404", description = "The identity not found")
	public UserFoldersWebService getFoldersWebService(@PathParam("identityKey") Long identityKey) {
		Identity identity = securityManager.loadIdentityByKey(identityKey, false);
		if(identity == null) {
			throw new WebApplicationException(Response.serverError().status(Status.NOT_FOUND).build());
		}
		return new UserFoldersWebService(identity);
	}
	
	@Path("{identityKey}/courses")
	@Operation(summary = "Retrieve courses", description = "Retrieves courses from a user given its unique key identifier")
	@ApiResponse(responseCode = "200", description = "The courses")
	@ApiResponse(responseCode = "401", description = "The roles of the authenticated user are not sufficient")
	@ApiResponse(responseCode = "404", description = "The identity not found")
	public UserCoursesWebService getCoursesWebService(@PathParam("identityKey") Long identityKey,
			@Context HttpServletRequest httpRequest) {
		Identity identity = securityManager.loadIdentityByKey(identityKey, false);
		if(identity == null) {
			throw new WebApplicationException(Response.serverError().status(Status.NOT_FOUND).build());
		}

		Identity ureqIdentity = getIdentity(httpRequest);
		if(ureqIdentity == null || !ureqIdentity.equals(identity)) {
			throw new WebApplicationException(Response.serverError().status(Status.UNAUTHORIZED).build());
		}
		UserCoursesWebService ws = new UserCoursesWebService(identity);
		CoreSpringFactory.autowireObject(ws);
		return ws;
	}
	
	/**
	 * Retrieves the portrait of an user
	 *
	 * @param identityKey The identity key of the user being searched
	 * @return The image
	 */
	@HEAD
	@Path("{identityKey}/portrait")
	@Operation(summary = "Retrieves the portrait of an user", description = "Retrieves the portrait of an user")
	@ApiResponse(responseCode = "200", description = "The portrait as image")
	@ApiResponse(responseCode = "404", description = "The identity or the portrait not found")
	@Produces({"image/jpeg","image/jpg",MediaType.APPLICATION_OCTET_STREAM})
	public Response getPortraitHead(@PathParam("identityKey") Long identityKey) {
		Identity identity = securityManager.loadIdentityByKey(identityKey);
		if(identity == null) {
			return Response.serverError().status(Status.NOT_FOUND).build();
		}
		
		File portrait = portraitManager.getBigPortrait(identity);
		if(portrait == null || !portrait.exists()) {
			return Response.serverError().status(Status.NOT_FOUND).build();
		}

		Date lastModified = new Date(portrait.lastModified());
		return Response.ok().lastModified(lastModified).build();
	}
	
	/**
	 * Retrieves the portrait of an user
	 * 
	 * @param identityKey The identity key of the user being searched
	 * @return The image
	 */
	@HEAD
	@Path("{identityKey}/portrait/{size}")
	@Operation(summary = "Retrieves the portrait of an user", description = "Retrieves the portrait of an user")
	@ApiResponse(responseCode = "200", description = "The portrait as image")
	@ApiResponse(responseCode = "404", description = "The identity or the portrait not found")
	@Produces({"image/jpeg","image/jpg",MediaType.APPLICATION_OCTET_STREAM})
	public Response getOriginalPortraitHead(@PathParam("identityKey") Long identityKey, @PathParam("size") String size) {
		Identity identity = securityManager.loadIdentityByKey(identityKey);
		if(identity == null) {
			return Response.serverError().status(Status.NOT_FOUND).build();
		}

		File portrait = null;
		if("master".equals(size)) {
			portrait = portraitManager.getMasterPortrait(identity);
		} else if("big".equals(size)) {
			portrait = portraitManager.getBigPortrait(identity);
		} else if("small".equals(size)) {
			portrait = portraitManager.getSmallPortrait(identity);
		}

		if(portrait == null || !portrait.exists()) {
			return Response.serverError().status(Status.NOT_FOUND).build();
		}
		Date lastModified = new Date(portrait.lastModified());
		return Response.ok().lastModified(lastModified).build();
	}
	
	/**
	 * Retrieves the portrait of an user
	 * 
	 * @param identityKey The identity key of the user being searched
	 * @param request The REST request
	 * @return The image
	 */
	@GET
	@Path("{identityKey}/portrait")
	@Operation(summary = "Retrieves the portrait of an user", description = "Retrieves the portrait of an user")
	@ApiResponse(responseCode = "200", description = "The portrait as image")
	@ApiResponse(responseCode = "404", description = "The identity or the portrait not found")
	@Produces({"image/jpeg","image/jpg",MediaType.APPLICATION_OCTET_STREAM})
	public Response getPortrait(@PathParam("identityKey") Long identityKey, @Context Request request) {
		Identity identity = securityManager.loadIdentityByKey(identityKey);
		if(identity == null) {
			return Response.serverError().status(Status.NOT_FOUND).build();
		}
		
		File portrait = portraitManager.getBigPortrait(identity);
		if(portrait == null || !portrait.exists()) {
			return Response.serverError().status(Status.NOT_FOUND).build();
		}

		Date lastModified = new Date(portrait.lastModified());
		Response.ResponseBuilder response = request.evaluatePreconditions(lastModified);
		if(response == null) {
			response = Response.ok(portrait).lastModified(lastModified).cacheControl(cc);
		}
		return response.build();
	}
	
	/**
	 * Upload the portrait of an user
	 * 
	 * @param identityKey The user key identifier of the user being searched
	 * @param file The image
	 * @param request The REST request
	 * @return The image
	 */
	@POST
	@Path("{identityKey}/portrait")
	@Operation(summary = "Upload the portrait of an user", description = "Upload the portrait of an user")
	@ApiResponse(responseCode = "200", description = "The portrait as image")
	@ApiResponse(responseCode = "401", description = "Not authorized")
	@ApiResponse(responseCode = "404", description = "The identity or the portrait not found")
	@Consumes({MediaType.MULTIPART_FORM_DATA})
	public Response postPortrait(@PathParam("identityKey") Long identityKey, @Context HttpServletRequest request) {
		MultipartReader partsReader = null;
		try {
			Identity identity = securityManager.loadIdentityByKey(identityKey);
			if(identity == null) {
				return Response.serverError().status(Status.NOT_FOUND).build();
			}
			
			Identity authIdentity = getUserRequest(request).getIdentity();
			if(!isUserManagerOf(identityKey, request) && !identity.getKey().equals(authIdentity.getKey())) {
				return Response.serverError().status(Status.UNAUTHORIZED).build();
			}
			partsReader = new MultipartReader(request);
			File tmpFile = partsReader.getFile();
			String filename = partsReader.getFilename();
			portraitManager.setPortrait(tmpFile, filename, identity);
			return Response.ok().build();
		} catch (Exception e) {
			throw new WebApplicationException(e);
		} finally {
			MultipartReader.closeQuietly(partsReader);
		}
	}
	
	/**
	 * Deletes the portrait of an user
	 * 
	 * @param identityKey The identity key identifier of the user being searched
	 * @param request The REST request
	 * @return The image
	 */
	@DELETE
	@Path("{identityKey}/portrait")
	@Operation(summary = "Deletes the portrait of an user", description = "Deletes the portrait of an user")
	@ApiResponse(responseCode = "200", description = "The portrait deleted")
	@ApiResponse(responseCode = "401", description = "Not authorized")
	public Response deletePortrait(@PathParam("identityKey") Long identityKey, @Context HttpServletRequest request) {
		Identity authIdentity = getUserRequest(request).getIdentity();
		Identity identity = securityManager.loadIdentityByKey(identityKey, false);
		if(identity == null) {
			return Response.serverError().status(Status.NOT_FOUND).build();
		} else if(!isUserManagerOf(identityKey, request) && !identity.equalsByPersistableKey(authIdentity)) {
			return Response.serverError().status(Status.UNAUTHORIZED).build();
		}
	
		portraitManager.deletePortrait(identity);
		return Response.ok().build();
	}

	@Path("{identityKey}/groups")
	public MyGroupWebService getUserGroupList(@PathParam("identityKey") Long identityKey) {
		Identity retrievedUser = securityManager.loadIdentityByKey(identityKey, false);
		if(retrievedUser == null) {
			return null;
		}
		MyGroupWebService ws = new MyGroupWebService(retrievedUser);
		CoreSpringFactory.autowireObject(ws);
		return ws;
	}

	/**
	 * Update an user
	 * 
	 * @param identityKey The user key identifier
	 * @param user The user datas
	 * @param request The HTTP request
	 * @return <code>User</code> object. The operation status (success or fail)
	 */
	@POST
	@Path("{identityKey}")
	@Operation(summary = "Update an user", description = "Update an user")
	@ApiResponse(responseCode = "200", description = "The user", content = {
			@Content(mediaType = "application/json", schema = @Schema(implementation = UserVO.class)),
			@Content(mediaType = "application/xml", schema = @Schema(implementation = UserVO.class)) })
	@ApiResponse(responseCode = "401", description = "The roles of the authenticated user are not sufficient")
	@ApiResponse(responseCode = "404", description = "The identity not found")
	@ApiResponse(responseCode = "406", description = "The list of validation errors", content = {
			@Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = ErrorVO.class))),
			@Content(mediaType = "application/xml", array = @ArraySchema(schema = @Schema(implementation = ErrorVO.class))) })
	@Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	public Response update(@PathParam("identityKey") Long identityKey, UserVO user, @Context HttpServletRequest request) {
		if(user == null) {
			return Response.serverError().status(Status.NO_CONTENT).build();
		}
		if(!isUserManagerOf(identityKey, request)) {
			return Response.serverError().status(Status.UNAUTHORIZED).build();
		}

		Identity retrievedIdentity = securityManager.loadIdentityByKey(identityKey, false);
		if(retrievedIdentity == null) {
			return Response.serverError().status(Status.NOT_FOUND).build();
		}
		
		User retrievedUser = retrievedIdentity.getUser();
		List<ErrorVO> errors = validateUser(retrievedUser, user, request);
		if(errors.isEmpty()) {
			if(StringHelper.containsNonWhitespace(user.getExternalId())
					&& !user.getExternalId().equals(retrievedIdentity.getExternalId())) {
				retrievedIdentity = securityManager.setExternalId(retrievedIdentity, user.getExternalId());
				retrievedUser = retrievedIdentity.getUser();
			}
			String oldEmail = retrievedUser.getEmail();
			post(retrievedUser, user, getLocale(request));
			userManager.updateUser(retrievedIdentity, retrievedUser);
			securityManager.deleteInvalidAuthenticationsByEmail(oldEmail);
			return Response.ok(get(retrievedIdentity, true, true)).build();
		}
		
		//content not ok
		ErrorVO[] errorVos = new ErrorVO[errors.size()];
		errors.toArray(errorVos);
		return Response.ok(errorVos).status(Status.NOT_ACCEPTABLE).build();
	}

	private List<ErrorVO> validateUser(User user, UserVO userVo, HttpServletRequest request) {
		Locale locale = getLocale(request);
		List<ErrorVO> errors = new ArrayList<>();
		List<UserPropertyHandler> propertyHandlers = userManager.getUserPropertyHandlersFor(PROPERTY_HANDLER_IDENTIFIER, false);
		validateProperty(user, UserConstants.FIRSTNAME, userVo.getFirstName(), propertyHandlers, errors, userManager, locale);
		validateProperty(user, UserConstants.LASTNAME, userVo.getLastName(), propertyHandlers, errors, userManager, locale);
		validateProperty(user, UserConstants.EMAIL, userVo.getEmail(), propertyHandlers, errors, userManager, locale);
		for (UserPropertyHandler propertyHandler : propertyHandlers) {
			if(!UserConstants.FIRSTNAME.equals(propertyHandler.getName())
					&& !UserConstants.LASTNAME.equals(propertyHandler.getName())
					&& !UserConstants.EMAIL.equals(propertyHandler.getName())) {
				validateProperty(user, userVo, propertyHandler, errors, userManager, locale);
			}
		}
		return errors;
	}
	
	private boolean validateProperty(User user, String name, String value, List<UserPropertyHandler> handlers, List<ErrorVO> errors, UserManager um, Locale locale) {
		for(UserPropertyHandler handler:handlers) {
			if(handler.getName().equals(name)) {
				return validateProperty(user, value, handler, errors, um, locale);
			}
		}
		return true;
	}
	
	private boolean validateProperty(User user, UserVO userVo, UserPropertyHandler userPropertyHandler, List<ErrorVO> errors, UserManager um, Locale locale) {
		String value = userVo.getProperty(userPropertyHandler.getName());
		return validateProperty(user, value, userPropertyHandler, errors, um, locale);
	}
	
	private boolean validateProperty(User user, String value, UserPropertyHandler userPropertyHandler, List<ErrorVO> errors, UserManager um, Locale locale) {
		ValidationError error = new ValidationError();
		if(!StringHelper.containsNonWhitespace(value) && um.isMandatoryUserProperty(PROPERTY_HANDLER_IDENTIFIER, userPropertyHandler)) {
			Translator translator = new PackageTranslator("org.olat.core", locale);
			String translation = translator.translate("new.form.mandatory");
			errors.add(new ErrorVO("org.olat.core:new.form.mandatory:" + userPropertyHandler.getName(), translation));
			return false;
		}
		
		value = parseUserProperty(value, userPropertyHandler, locale);
		
		if (!userPropertyHandler.isValidValue(user, value, error, locale)) {
			String pack = userPropertyHandler.getClass().getPackage().getName();
			Translator translator = new PackageTranslator(pack, locale);
			String translation = translator.translate(error.getErrorKey(), error.getArgs());
			errors.add(new ErrorVO(pack, error.getErrorKey(), translation));
			return false;
		} else if((userPropertyHandler.getName().equals(UserConstants.INSTITUTIONALEMAIL) && StringHelper.containsNonWhitespace(value)) 
				|| userPropertyHandler.getName().equals(UserConstants.EMAIL)) {
			if (!userManager.isEmailAllowed(value, user)) {
				String pack = userPropertyHandler.getClass().getPackage().getName();
				Translator translator = new PackageTranslator(pack, locale);
				String translation = translator.translate("form.name." + userPropertyHandler.getName() + ".error.exists", new String[] { value });
				translation += " (" + value + ")";
				errors.add(new ErrorVO("org.olat.user.propertyhandlers:new.form.name." + userPropertyHandler.getName() + ".exists", translation));
			}
		}
		
		return true;
	}

	/**
	 * Delete an user from the system
	 * 
	 * @param identityKey The user key identifier
	 * @param request The HTTP request
	 * @return <code>Response</code> object. The operation status (success or fail)
	 */
	@DELETE
	@Path("{identityKey}")
	@Operation(summary = "Delete an user from the system", description = "Delete an user from the system")
	@ApiResponse(responseCode = "200", description = "The user is removed from the group")
	@ApiResponse(responseCode = "401", description = "he roles of the authenticated user are not sufficient")
	@ApiResponse(responseCode = "404", description = "The identity not found")
	@ApiResponse(responseCode = "500", description = "Unknown problem while deleting, see olat.log")
	public Response delete(@PathParam("identityKey") Long identityKey, @Context HttpServletRequest request) {
		Identity actingIdentity = getIdentity(request);
		if(actingIdentity == null || !isUserManagerOf(identityKey, request)) {
			return Response.serverError().status(Status.UNAUTHORIZED).build();
		}
		
		Identity identity = securityManager.loadIdentityByKey(identityKey, false);
		if(identity == null) {
			return Response.serverError().status(Status.NOT_FOUND).build();
		}
		boolean success = userLifecycleManager.deleteIdentity(identity, actingIdentity);
		if (success) {
			return Response.ok().build();			
		} else {
			return Response.serverError().status(Status.INTERNAL_SERVER_ERROR).build();
		}
	}
	
	private boolean isUserManager(HttpServletRequest request) {
		Roles managerRoles = getRoles(request);
		return managerRoles.isUserManager() || managerRoles.isRolesManager() || managerRoles.isAdministrator();
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
	
	/**
	 * Search a user by user name. This is an exact match and search a unique user
	 * in the old identity name, the new nick name and the authentications user names.
	 * 
	 * @param username The user name to search for
	 * @param request The HTTP request
	 * @return A unique userVo, with all properties, inclusive administrative properties
	 */
	@GET
	@Path("username")
	@Operation(summary = "Search a user by user name", description = "Search a user by user name. This is an exact match and search a unique user in the old identity name, the new nick name and the authentications user names.")
	@ApiResponse(responseCode = "200", description = "A unique user has been successfully found")
	@ApiResponse(responseCode = "401", description = "he roles of the authenticated user are not sufficient")
	@ApiResponse(responseCode = "404", description = "The identity not found")
	@ApiResponse(responseCode = "500", description = "Unknown problem, see olat.log")
	public Response searchByUsername(@QueryParam("username") String username, @Context HttpServletRequest request) {
		Identity actingIdentity = getIdentity(request);
		if(actingIdentity == null || !isUserManager(request)) {
			return Response.serverError().status(Status.UNAUTHORIZED).build();
		}
		
		Identity identity = securityManager.findIdentityByUsernames(username);
		if(identity == null) {
			return Response.serverError().status(Status.NOT_FOUND).build();
		} else if(!isUserManagerOf(identity.getKey(), request)) {
			return Response.serverError().status(Status.UNAUTHORIZED).build();
		}
		return Response.ok(get(identity, true, true)).build();
	}
	
	@POST
	@Path("{identityKey}/username")
	@Operation(summary = "Rename an user", description = "Rename an user")
	@ApiResponse(responseCode = "200", description = "The user has been successfully renamed")
	@ApiResponse(responseCode = "401", description = "he roles of the authenticated user are not sufficient")
	@ApiResponse(responseCode = "404", description = "The identity not found")
	@ApiResponse(responseCode = "500", description = "Unknown problem, see olat.log")
	public Response renamePost(@PathParam("identityKey") Long identityKey, @QueryParam("username") String username, @Context HttpServletRequest request) {
		return renamePut(identityKey, username, request);
	}
	
	@PUT
	@Path("{identityKey}/username")
	@Operation(summary = "Rename an user", description = "Rename an user")
	@ApiResponse(responseCode = "200", description = "The user has been successfully renamed")
	@ApiResponse(responseCode = "401", description = "he roles of the authenticated user are not sufficient")
	@ApiResponse(responseCode = "404", description = "The identity not found")
	@ApiResponse(responseCode = "500", description = "Unknown problem, see olat.log")
	public Response renamePut(@PathParam("identityKey") Long identityKey, @QueryParam("username") String username, @Context HttpServletRequest request) {
		Identity actingIdentity = getIdentity(request);
		if(actingIdentity == null || !isUserManagerOf(identityKey, request)) {
			return Response.serverError().status(Status.UNAUTHORIZED).build();
		}
		
		Identity identity = securityManager.loadIdentityByKey(identityKey, false);
		if(identity == null) {
			return Response.serverError().status(Status.NOT_FOUND).build();
		}
		
		Map<String, AuthenticationProviderSPI> providers = CoreSpringFactory.getBeansOfType(AuthenticationProviderSPI.class);
		List<Authentication> authentications = securityManager.getAuthentications(identity);
		List<ErrorVO> errors = new ArrayList<>();
		List<Authentication> manageableAuthentications = new ArrayList<>();
		for(Authentication authentication:authentications) {
			AuthenticationProviderSPI provider = getProvider(authentication, providers);
			if(provider != null && provider.canChangeAuthenticationUsername(authentication.getProvider())) {
				if(provider.changeAuthenticationUsername(authentication, username)) {
					manageableAuthentications.add(authentication);
				} else {
					log.info("Cannot change authentication user name for identity:{} and provider: {}", identityKey, authentication.getProvider());
					errors.add(new ErrorVO());
				}
				
			}
		}
		
		if(!errors.isEmpty()) {
			return Response.ok(errors).status(Status.CONFLICT).build();
		}
		
		identity.getUser().setProperty(UserConstants.NICKNAME, username);
		if(userManager.updateUserFromIdentity(identity)) {
			for(Authentication authentication:manageableAuthentications) {
				AuthenticationProviderSPI provider = getProvider(authentication, providers);
				if(provider != null) {
					provider.changeAuthenticationUsername(authentication, username);
				}
			}
		} else {
			return Response.serverError()
					.status(Status.CONFLICT).build();
		}
		dbInstance.commit();
		return Response.ok().build();
	}
	
	private AuthenticationProviderSPI getProvider(Authentication authentication, Map<String, AuthenticationProviderSPI> providers) {
		for(AuthenticationProviderSPI provider:providers.values()) {
			List<String> names = provider.getProviderNames();
			if(names.contains(authentication.getProvider())) {
				return provider;
			}	
		}
		return null;
	}
}