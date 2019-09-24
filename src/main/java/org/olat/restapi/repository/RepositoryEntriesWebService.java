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
* Copyright (c) since 2004 at Multimedia- & E-Learning Services (MELS),<br>
* University of Zurich, Switzerland.
* <hr>
* <a href="http://www.openolat.org">
* OpenOLAT - Online Learning and Training</a><br>
* This file has been modified by the OpenOLAT community. Changes are licensed
* under the Apache 2.0 license as the original file.
*/
package org.olat.restapi.repository;


import static org.olat.restapi.security.RestSecurityHelper.getIdentity;
import static org.olat.restapi.security.RestSecurityHelper.getRoles;
import static org.olat.restapi.security.RestSecurityHelper.getUserRequest;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.apache.logging.log4j.Logger;
import org.olat.basesecurity.OrganisationRoles;
import org.olat.basesecurity.OrganisationService;
import org.olat.basesecurity.model.OrganisationRefImpl;
import org.olat.core.CoreSpringFactory;
import org.olat.core.id.Identity;
import org.olat.core.id.Organisation;
import org.olat.core.id.Roles;
import org.olat.core.logging.Tracing;
import org.olat.core.util.StringHelper;
import org.olat.core.util.i18n.I18nModule;
import org.olat.fileresource.types.ResourceEvaluation;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryStatusEnum;
import org.olat.repository.RepositoryManager;
import org.olat.repository.RepositoryService;
import org.olat.repository.handlers.RepositoryHandler;
import org.olat.repository.handlers.RepositoryHandlerFactory;
import org.olat.repository.model.SearchRepositoryEntryParameters;
import org.olat.restapi.security.RestSecurityHelper;
import org.olat.restapi.support.MediaTypeVariants;
import org.olat.restapi.support.MultipartReader;
import org.olat.restapi.support.vo.RepositoryEntryVO;
import org.olat.restapi.support.vo.RepositoryEntryVOes;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.servers.Server;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * Description:<br>
 * This handles the repository entries
 * 
 * <P>
 * Initial Date: 19.05.2009 <br>
 * 
 * @author patrickb, srosse, stephane.rosse@frentix.com
 */
@Tag(name = "Repo")
@Component
@OpenAPIDefinition(
	info = @Info(
			title = "OpenOlat REST API",
			description = "This is the documentation of the OpenOlat REST API.",
			contact = @Contact(
					name=  "OpenOlat",
					url = "https://www.openolat.org"
				),
			license = @License(
					name = "Apache 2.0",
					url = "https://github.com/OpenOLAT/OpenOLAT/blob/master/LICENSE"
				)
		),
	servers = { @Server(url = "/restapi") }
)
@Path("repo/entries")
public class RepositoryEntriesWebService {
	
	private static final Logger log = Tracing.createLoggerFor(RepositoryEntriesWebService.class);
	private static final String VERSION = "1.0";
	
	@Autowired
	private RepositoryService repositoryService;
	@Autowired
	private RepositoryManager repositoryManager;
	@Autowired
	private OrganisationService organisationService;
	@Autowired
	private RepositoryHandlerFactory handlerFactory;
	
	/**
	 * The version number of this web service
	 * @return
	 */
	@GET
	@Path("version")
	@Produces(MediaType.TEXT_PLAIN)
	public Response getVersion() {
		return Response.ok(VERSION).build();
	}
	
	/**
	 * List all entries in the OLAT repository
	 * @response.representation.200.qname {http://www.example.com}repositoryEntryVO
	 * @response.representation.200.mediaType text/plain, text/html, application/xml, application/json
	 * @response.representation.200.doc List all entries in the repository
	 * @response.representation.200.example {@link org.olat.restapi.support.vo.Examples#SAMPLE_REPOENTRYVOes}
	 * @param start (optional)
	 * @param limit (optional)
	 * @param managed (optional)
	 * @param externalId External ID (optional)
	 * @param externalRef External reference number (optional)
	 * @param resourceType The resource type (CourseModule) (optional)
	 * @param httpRequest The HTTP request
	 * @param request The RESt request
	 * @return
	 */
	@GET
	@Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
	@ApiResponses(value = {
		@ApiResponse(responseCode = "200",
			description = "JVM system properties of a particular host.",
			content = {
				@Content(mediaType = "application/json", schema = @Schema(implementation = RepositoryEntryVO[].class)),
				@Content(mediaType = "application/xml", schema = @Schema(implementation = RepositoryEntryVO[].class))
			}
		)}
	)
	@Operation(summary = "List all entries in the repository",
		description = "List all entries in the OpenOLAT repository.")
	public Response getEntries(@QueryParam("start") @DefaultValue("0") Integer start,
			@QueryParam("limit") @DefaultValue("25") Integer limit,
			@QueryParam("managed") Boolean managed, @QueryParam("externalId") String externalId,
			@QueryParam("externalRef") String externalRef, @QueryParam("resourceType") String resourceType,
			@Context HttpServletRequest httpRequest, @Context Request request) {
		try {
			// list of courses open for everybody
			Roles roles = getRoles(httpRequest);
			Identity identity = getIdentity(httpRequest);
			RepositoryManager rm = RepositoryManager.getInstance();
			SearchRepositoryEntryParameters params = new SearchRepositoryEntryParameters(identity, roles);
			params.setManaged(managed);
			if(StringHelper.containsNonWhitespace(externalId)) {
				params.setExternalId(externalId);
			}
			if(StringHelper.containsNonWhitespace(externalRef)) {
				params.setExternalRef(externalRef);
			}
			if(StringHelper.containsNonWhitespace(resourceType)) {
				params.setResourceTypes(Collections.singletonList(resourceType));
			}
			
			if(MediaTypeVariants.isPaged(httpRequest, request)) {
				int totalCount = rm.countGenericANDQueryWithRolesRestriction(params);
				List<RepositoryEntry> res = rm.genericANDQueryWithRolesRestriction(params, start, limit, true);
				RepositoryEntryVOes voes = new RepositoryEntryVOes();
				voes.setRepositoryEntries(toArrayOfVOes(res));
				voes.setTotalCount(totalCount);
				return Response.ok(voes).build();
			} else {
				List<RepositoryEntry> res = rm.genericANDQueryWithRolesRestriction(params, 0, -1, false);
				RepositoryEntryVO[] voes = toArrayOfVOes(res);
				return Response.ok(voes).build();
			}
		} catch(Exception e) {
			throw new WebApplicationException(e);
		}
	}
	
	private RepositoryEntryVO[] toArrayOfVOes(List<RepositoryEntry> coursRepos) {
		int i=0;
		RepositoryEntryVO[] entryVOs = new RepositoryEntryVO[coursRepos.size()];
		for (RepositoryEntry repoE : coursRepos) {
			entryVOs[i++] = RepositoryEntryVO.valueOf(repoE);
		}
		return entryVOs;
	}

	/**
	 * Search for repository entries, possible search attributes are name, author and type
	 * @response.representation.mediaType multipart/form-data
	 * @response.representation.doc Search for repository entries
	 * @response.representation.200.qname {http://www.example.com}repositoryEntryVO
	 * @response.representation.200.mediaType application/xml, application/json
	 * @response.representation.200.doc Search for repository entries
	 * @response.representation.200.example {@link org.olat.restapi.support.vo.Examples#SAMPLE_REPOENTRYVO}
	 * @response.representation.401.doc The roles of the authenticated user are not sufficient
	 * @param type Filter by the file resource type of the repository entry
	 * @param author Filter by the author's username
	 * @param name Filter by name of repository entry
	 * @param myEntries Only search entries the requester owns
	 * @param httpRequest The HTTP request
	 * @return
	 */
	@GET
	@Path("search")
	@Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
	public Response searchEntries(@QueryParam("type") String type, @QueryParam("author") @DefaultValue("*") String author,
			@QueryParam("name") @DefaultValue("*") String name, @QueryParam("myentries") @DefaultValue("false") boolean myEntries,
			@Context HttpServletRequest httpRequest) {
		RepositoryManager rm = RepositoryManager.getInstance();
		try {
			List<RepositoryEntry> reposFound = new ArrayList<>();
			Identity identity = getIdentity(httpRequest);
			boolean restrictedType = type != null && !type.isEmpty();
			
			// list of courses open for everybody
			Roles roles = getRoles(httpRequest);
			
			if(myEntries) {
				List<RepositoryEntry> lstRepos = rm.queryByOwner(identity, true, restrictedType ? new String[] {type} : null);
				boolean restrictedName = !name.equals("*");
				boolean restrictedAuthor = !author.equals("*");
				if(restrictedName || restrictedAuthor) {
					// filter by search conditions
					for(RepositoryEntry re : lstRepos) {
						boolean nameOk = restrictedName ? re.getDisplayname().toLowerCase().contains(name.toLowerCase()) : true;
						boolean authorOk = restrictedAuthor ? re.getInitialAuthor().equalsIgnoreCase(author) : true;
						if(nameOk && authorOk) {
							reposFound.add(re);
						}
					}
				} else {
					if(!lstRepos.isEmpty()) reposFound.addAll(lstRepos);
				}
			} else {
				List<String> types = new ArrayList<>(1);
				if(restrictedType) types.add(type);

				SearchRepositoryEntryParameters params = new SearchRepositoryEntryParameters(name, author, null, restrictedType ? types : null, identity, roles);
				List<RepositoryEntry> lstRepos = rm.genericANDQueryWithRolesRestriction(params, 0, -1, false);
				if(!lstRepos.isEmpty()) reposFound.addAll(lstRepos);
			}
			
			int i=0;
			RepositoryEntryVO[] reVOs = new RepositoryEntryVO[reposFound.size()];
			for (RepositoryEntry re : reposFound) {
				reVOs[i++] = RepositoryEntryVO.valueOf(re);
			}
			return Response.ok(reVOs).build();
		} catch(Exception e) {
			throw new WebApplicationException(e);
		}
	}
	
	/**
	 * Import a resource in the repository
	 * @response.representation.mediaType multipart/form-data
	 * @response.representation.doc The file, its name and the resourcename
	 * @response.representation.200.qname {http://www.example.com}repositoryEntryVO
	 * @response.representation.200.mediaType application/xml, application/json
	 * @response.representation.200.doc Import the resource and return the repository entry
	 * @response.representation.200.example {@link org.olat.restapi.support.vo.Examples#SAMPLE_REPOENTRYVO}
	 * @response.representation.401.doc The roles of the authenticated user are not sufficient
	 * @param filename The name of the imported file
	 * @param file The file input stream
	 * @param resourcename The name of the resource
	 * @param displayname The display name
	 * @param softkey The soft key (can be null)
	 * @param request The HTTP request
	 * @return
	 */
	@PUT
	@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	@Consumes({MediaType.MULTIPART_FORM_DATA})
	public Response putResource(@Context HttpServletRequest request) {
		Roles roles = getRoles(request);
		if(!roles.isAdministrator() && !roles.isLearnResourceManager() && !roles.isAuthor()) {
			return Response.serverError().status(Status.UNAUTHORIZED).build();
		}
		
		MultipartReader partsReader = null;
		try {
			Identity identity = getUserRequest(request).getIdentity();
			partsReader = new MultipartReader(request);
			File tmpFile = partsReader.getFile();
			long length = tmpFile.length();
			if(length > 0) {
				boolean guests = false;
				boolean allUsers = false;
				RepositoryEntryStatusEnum status = RepositoryEntryStatusEnum.preparation;
				
				Long accessRaw = partsReader.getLongValue("access");
				String statusRaw = partsReader.getValue("status");
				String allUsersRaw = partsReader.getValue("allUsers");
				String guestsRaw = partsReader.getValue("guests");
				
				if(RepositoryEntryStatusEnum.isValid(statusRaw)) {
					status = RepositoryEntryStatusEnum.valueOf(statusRaw);
					allUsers = "true".equals(allUsersRaw);
					guests = "true".equals(guestsRaw);
				} else if(accessRaw != null) {
					status = RestSecurityHelper.convertToEntryStatus(accessRaw.intValue(), false);
					allUsers = accessRaw.longValue() >= 3;
					guests = accessRaw.longValue() >= 4;
				}

				String softkey = partsReader.getValue("softkey");
				String resourcename = partsReader.getValue("resourcename");
				String displayname = partsReader.getValue("displayname");	
				String organisationKey = partsReader.getValue("organisationkey");
				Organisation organisation = null;
				if(StringHelper.containsNonWhitespace(organisationKey)) {
					organisation = organisationService.getOrganisation(new OrganisationRefImpl(Long.valueOf(organisationKey)));
				} else {
					organisation = organisationService.getDefaultOrganisation();
				}
				
				boolean hasAdminRights = roles.hasSomeRoles(organisation,
						OrganisationRoles.administrator, OrganisationRoles.learnresourcemanager,
						OrganisationRoles.author);
				if(hasAdminRights) {
					RepositoryEntry re = importFileResource(identity, tmpFile, resourcename, displayname, softkey, status, allUsers, guests, organisation);
					RepositoryEntryVO vo = RepositoryEntryVO.valueOf(re);
					return Response.ok(vo).build();
				} else {
					return Response.serverError().status(Status.UNAUTHORIZED).build();
				}
			}
			return Response.serverError().status(Status.NO_CONTENT).build();
		} catch (Exception e) {
			log.error("Error while importing a file",e);
		} finally {
			MultipartReader.closeQuietly(partsReader);
		}
		return Response.serverError().status(Status.INTERNAL_SERVER_ERROR).build();
	}
	
	private RepositoryEntry importFileResource(Identity identity, File fResource, String resourcename,
			String displayname, String softkey, RepositoryEntryStatusEnum status, boolean allUsers, boolean guests, Organisation organisation) {
		try {
			RepositoryHandler handler = null;
			for(String type:handlerFactory.getSupportedTypes()) {
				RepositoryHandler h = handlerFactory.getRepositoryHandler(type);
				ResourceEvaluation eval = h.acceptImport(fResource, fResource.getName());
				if(eval != null && eval.isValid()) {
					handler = h;
					break;
				}
			}
			
			RepositoryEntry addedEntry = null;
			if(handler != null) {
				Locale locale = I18nModule.getDefaultLocale();
				
				addedEntry = handler.importResource(identity, null, displayname,
						"", true, organisation, locale, fResource, fResource.getName());
				
				if(StringHelper.containsNonWhitespace(resourcename)) {
					addedEntry.setResourcename(resourcename);
				}
				if(StringHelper.containsNonWhitespace(softkey)) {
					addedEntry.setSoftkey(softkey);
				}
				addedEntry.setEntryStatus(status);
				addedEntry.setAllUsers(allUsers);
				addedEntry.setGuests(guests);
				addedEntry = repositoryService.update(addedEntry);
			}
			return addedEntry;
		} catch(Exception e) {
			log.error("Fail to import a resource", e);
			throw new WebApplicationException(e);
		}
	}
	
	@Path("{repoEntryKey}")
	public RepositoryEntryWebService getRepositoryEntryResource(@PathParam("repoEntryKey")String repoEntryKey)
	throws WebApplicationException {
		RepositoryEntry re = lookupRepositoryEntry(repoEntryKey);
	    if(re == null) {
	      throw new WebApplicationException(Status.NOT_FOUND);
	    }
		
		RepositoryEntryWebService entrySW = new RepositoryEntryWebService(re);
		CoreSpringFactory.autowireObject(entrySW);
		return entrySW;
	}
	
	private RepositoryEntry lookupRepositoryEntry(String key) {
		RepositoryEntry re = null;
		if (StringHelper.isLong(key)) {// looks like a primary key
			try {
				re = repositoryManager.lookupRepositoryEntry(Long.valueOf(key));
			} catch (NumberFormatException e) {
				log.warn("", e);
			}
		}
		if (re == null) {// perhaps a soft key
			re = repositoryManager.lookupRepositoryEntryBySoftkey(key, false);
		}
		return re;
	}
}