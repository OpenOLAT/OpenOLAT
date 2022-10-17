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
package org.olat.restapi.repository;

import static org.olat.repository.ui.catalog.CatalogNodeManagerController.LOCK_TOKEN;
import static org.olat.repository.ui.catalog.CatalogNodeManagerController.lockRes;
import static org.olat.restapi.security.RestSecurityHelper.getRoles;
import static org.olat.restapi.security.RestSecurityHelper.getUserRequest;
import static org.olat.restapi.support.CatalogVOFactory.get;
import static org.olat.restapi.support.CatalogVOFactory.link;

import java.util.List;
import java.util.Locale;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.FormParam;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.PathSegment;
import jakarta.ws.rs.core.Request;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;
import jakarta.ws.rs.core.UriInfo;

import org.olat.basesecurity.BaseSecurity;
import org.olat.basesecurity.manager.SecurityGroupDAO;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.translator.Translator;
import org.olat.core.id.Identity;
import org.olat.core.id.Roles;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.core.util.coordinate.CoordinatorManager;
import org.olat.core.util.coordinate.LockResult;
import org.olat.core.util.i18n.I18nModule;
import org.olat.dispatcher.LocaleNegotiator;
import org.olat.repository.CatalogEntry;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryManager;
import org.olat.repository.manager.CatalogManager;
import org.olat.repository.ui.catalog.CatalogNodeManagerController;
import org.olat.restapi.support.MediaTypeVariants;
import org.olat.restapi.support.vo.CatalogEntryVO;
import org.olat.restapi.support.vo.CatalogEntryVOes;
import org.olat.restapi.support.vo.ErrorVO;
import org.olat.user.UserManager;
import org.olat.user.restapi.UserVO;
import org.olat.user.restapi.UserVOFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * Description:<br>
 * A web service for the catalog
 * 
 * <P>
 * Initial Date:  5 may 2010 <br>
 * @author srosse, stephane.rosse@frentix.com
 */
@Tag(name = "Catalog")
@Component
@Path("catalog")
public class CatalogWebService {
	
	private static final String VERSION = "1.0";
	
	@Autowired
	private UserManager userManager;
	@Autowired
	private BaseSecurity securityManager;
	@Autowired
	private CatalogManager catalogManager;
	@Autowired
	private SecurityGroupDAO securityGroupDao;
	@Autowired
	private RepositoryManager repositoryManager;

	
	/**
	 * Retrieves the version of the Catalog Web Service.
	 * 
	 * @return
	 */
	@GET
	@Path("version")
	@Operation(summary = "Retrieves the version of the Catalog Web Service", description = "Retrieves the version of the Catalog Web Service")
	@ApiResponse(responseCode = "200", description = "The version of this specific Web Service")
	@Produces(MediaType.TEXT_PLAIN)
	public Response getVersion() {
		return Response.ok(VERSION).build();
	}
	
	/**
	 * Returns the list of root catalog entries.
	 * 
	 * @return The response
	 */
	@GET
	@Operation(summary = "Returns the list of root catalog entries", description = "Returns the list of root catalog entries")
	@ApiResponse(responseCode = "200", description = "Array of results for the whole the course", content = {
			@Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = CatalogEntryVO.class))),
			@Content(mediaType = "application/xml", array = @ArraySchema(schema = @Schema(implementation = CatalogEntryVO.class))) })
	@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	public Response getRoots(@Context HttpServletRequest httpRequest, @Context Request request) {
		List<CatalogEntry> rootEntries = catalogManager.getRootCatalogEntries();
		CatalogEntryVO[] entryVOes = toArray(rootEntries);
		if(MediaTypeVariants.isPaged(httpRequest, request)) {
			CatalogEntryVOes voes = new CatalogEntryVOes();
			voes.setCatalogEntries(entryVOes);
			voes.setTotalCount(1);
			return Response.ok(voes).build();
		} else {
			return Response.ok(entryVOes).build();
		}
	}
	
	/**
	 * Returns the metadata of the catalog entry.
	 * 
	 * @param path The path
	 * @param uriInfo The URI informations
	 * @param httpRequest The HTTP request
	 * @param request The REST request
	 * @return The response
	 */
	@GET
	@Path("{path:.*}")
	@Operation(summary = "Returns the metadata of the catalog entry", description = "Returns the metadata of the catalog entry")
	@ApiResponse(responseCode = "200", description = "The catalog entry", content = {
			@Content(mediaType = "application/json", schema = @Schema(implementation = CatalogEntryVO.class)),
			@Content(mediaType = "application/xml", schema = @Schema(implementation = CatalogEntryVO.class)) })
	@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	public Response getCatalogEntry(@PathParam("path") List<PathSegment> path, @Context UriInfo uriInfo,
			@Context HttpServletRequest httpRequest, @Context Request request) {
		if(path.isEmpty()) {
			return getRoots(httpRequest, request);
		}
		
		Long ceKey = getCatalogEntryKeyFromPath(path);
		if(ceKey == null) {
			return Response.serverError().status(Status.NOT_ACCEPTABLE).build();
		}

		CatalogEntry ce = catalogManager.loadCatalogEntry(ceKey);
		if(ce == null) {
			return Response.serverError().status(Status.NOT_FOUND).build();
		}
		
		CatalogEntryVO vo = link(get(ce), uriInfo);
		return Response.ok(vo).build();
	}
	
	/**
	 * Returns a list of catalog entries.
	 * 
	 * @param path The path
	 * @param start
	 * @param limit
	 * @param httpRequest The HTTP request
	 * @param request The REST request
	 * @return The response
	 */
	@GET
	@Path("{path:.*}/children")
	@Operation(summary = "Returns a list of catalog entries", description = "Returns a list of catalog entries")
	@ApiResponse(responseCode = "200", description = "The list of catalog entries", content = {
			@Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = CatalogEntryVO.class))),
			@Content(mediaType = "application/xml", array = @ArraySchema(schema = @Schema(implementation = CatalogEntryVO.class))) })
	@ApiResponse(responseCode = "404", description = "The path could not be resolved to a valid catalog entry")
	@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	public Response getChildren(@PathParam("path") List<PathSegment> path, @QueryParam("start") @DefaultValue("0") Integer start,
			@QueryParam("limit") @DefaultValue("25") Integer limit, @Context HttpServletRequest httpRequest, @Context Request request) {
		if(path.isEmpty()) {
			return getRoots(httpRequest, request);
		}

		Long ceKey = getCatalogEntryKeyFromPath(path);
		if(ceKey == null) {
			return Response.serverError().status(Status.NOT_ACCEPTABLE).build();
		}
		
		CatalogEntry ce = catalogManager.loadCatalogEntry(ceKey);
		if(ce == null) {
			return Response.serverError().status(Status.NOT_FOUND).build();
		}
		
		if(MediaTypeVariants.isPaged(httpRequest, request)) {
			int totalCount = catalogManager.countChildrenOf(ce, -1);
			List<CatalogEntry> entries = catalogManager.getChildrenOf(ce, start, limit, CatalogEntry.OrderBy.name, true);
			CatalogEntryVO[] entryVOes = toArray(entries);
			CatalogEntryVOes voes = new CatalogEntryVOes();
			voes.setTotalCount(totalCount);
			voes.setCatalogEntries(entryVOes);
			return Response.ok(voes).build();
		} else {
			List<CatalogEntry> entries = catalogManager.getChildrenOf(ce);
			CatalogEntryVO[] entryVOes = toArray(entries);
			return Response.ok(entryVOes).build();
		}
	}
	
	private CatalogEntryVO[] toArray(List<CatalogEntry> entries) {
		int count = 0;
		CatalogEntryVO[] entryVOes = new CatalogEntryVO[entries.size()];
		for(CatalogEntry entry:entries) {
			entryVOes[count++] = get(entry);
		}
		return entryVOes;
	}
	
	/**
	 * Adds a catalog entry under the path specified in the URL.
	 * 
	 * @param path The path
	 * @param name The name
	 * @param description The description
	 * @param type The type (leaf or node)
	 * @param repoEntryKey The id of the repository entry
	 * @param httpRquest The HTTP request
	 * @param uriInfo The URI informations
	 * @return The response
	 */
	@PUT
	@Path("{path:.*}")
	@Operation(summary = "Add a catalog", description = "Adds a catalog entry under the path specified in the URL")
	@ApiResponse(responseCode = "200", description = "The catalog entry", content = {
			@Content(mediaType = "application/json", schema = @Schema(implementation = CatalogEntryVO.class)),
			@Content(mediaType = "application/xml", schema = @Schema(implementation = CatalogEntryVO.class)) })
	@ApiResponse(responseCode = "401", description = "Not authorized")
	@ApiResponse(responseCode = "404", description = "The path could not be resolved to a valid catalog entry")
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	public Response addCatalogEntry(@PathParam("path") List<PathSegment> path,
			@QueryParam("name") String name, @QueryParam("description") String description,
			@QueryParam("type") Integer type, @QueryParam("repoEntryKey") Long repoEntryKey,
			@Context HttpServletRequest httpRequest,
			@Context UriInfo uriInfo) {
		
		CatalogEntryVO entryVo = new CatalogEntryVO();
		entryVo.setName(name);
		entryVo.setDescription(description);
		if(type != null) {
			entryVo.setType(type);
		}
		entryVo.setRepositoryEntryKey(repoEntryKey);
		return addCatalogEntry(path, entryVo, httpRequest, uriInfo);
	}
	
	/**
	 * Adds a catalog entry under the path specified in the URL.
	 * 
	 * @param path The path
	 * @param entryVo The catalog entry
	 * @param httpRquest The HTTP request
	 * @param uriInfo The URI informations
	 * @return The response
	 */
	@PUT
	@Path("{path:.*}")
	@Operation(summary = "Add a catalog", description = "Adds a catalog entry under the path specified in the URL")
	@ApiResponse(responseCode = "200", description = "The catalog entry", content = {
			@Content(mediaType = "application/json", schema = @Schema(implementation = CatalogEntryVO.class)),
			@Content(mediaType = "application/xml", schema = @Schema(implementation = CatalogEntryVO.class)) })
	@ApiResponse(responseCode = "401", description = "Not authorized")
	@ApiResponse(responseCode = "404", description = "The path could not be resolved to a valid catalog entry")
	@Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	public Response addCatalogEntry(@PathParam("path") List<PathSegment> path, CatalogEntryVO entryVo,
			@Context HttpServletRequest httpRequest, @Context UriInfo uriInfo) {
		if(!isCatalogManager(httpRequest)) {
			return Response.serverError().status(Status.UNAUTHORIZED).build();
		}

		Long parentKey = getCatalogEntryKeyFromPath(path);
		if(parentKey == null) {
			return Response.serverError().status(Status.NOT_ACCEPTABLE).build();
		}
		
		CatalogEntry parent = catalogManager.loadCatalogEntry(parentKey);
		if(parent == null) {
			return Response.serverError().status(Status.NOT_FOUND).build();
		}

		int type = guessType(entryVo);
		if(type == CatalogEntry.TYPE_NODE && !canAdminSubTree(parent, httpRequest)) {
			return Response.serverError().status(Status.UNAUTHORIZED).build();
		}
		
		RepositoryEntry re = null;
		if(entryVo.getRepositoryEntryKey() != null) {
			re = repositoryManager.lookupRepositoryEntry(entryVo.getRepositoryEntryKey());
			if(re == null) {
				return Response.serverError().status(Status.NOT_FOUND).build();
			}
		}
		
		Identity id = getUserRequest(httpRequest).getIdentity();
		LockResult lock = CoordinatorManager.getInstance().getCoordinator().getLocker().acquireLock(lockRes, id, LOCK_TOKEN, null);
		if (!lock.isSuccess()) {
			return getLockedResponse(lock, httpRequest);
		}
		
		CatalogEntry ce = null;
		try {
			ce = catalogManager.createCatalogEntry();
			ce.setType(guessType(entryVo));
			ce.setName(entryVo.getName());
			ce.setDescription(entryVo.getDescription());
			if(re != null) {
				ce.setRepositoryEntry(re);
			}
			catalogManager.addCatalogEntry(parent, ce);
		} catch (Exception e) {
			throw new WebApplicationException(e);
		} finally {
			CoordinatorManager.getInstance().getCoordinator().getLocker().releaseLock(lock);
		}

		CatalogEntryVO newEntryVo = link(get(ce), uriInfo);
		return Response.ok(newEntryVo).build();
	}
	
	/**
	 * Updates the catalog entry under the path specified in the URL.
	 * 
	 * @param path The path
	 * @param name The name
	 * @param description The description
	 * @param newParentKey The parent key to move the entry (optional)
	 * @param httpRquest The HTTP request
	 * @param uriInfo The URI informations
	 * @return The response
	 */
	@POST
	@Path("{path:.*}")
	@Operation(summary = "Update a catalog", description = "Updates the catalog entry under the path specified in the URL")
	@ApiResponse(responseCode = "200", description = "The catalog entry", content = {
			@Content(mediaType = "application/json", schema = @Schema(implementation = CatalogEntryVO.class)),
			@Content(mediaType = "application/xml", schema = @Schema(implementation = CatalogEntryVO.class)) })
	@ApiResponse(responseCode = "401", description = "Not authorized")
	@ApiResponse(responseCode = "404", description = "The path could not be resolved to a valid catalog entry")
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	public Response updatePostCatalogEntry(@PathParam("path") List<PathSegment> path,
			@FormParam("name") String name, @FormParam("description") String description,
			@FormParam("newParentKey") Long newParentKey,//fxdiff FXOLAT-122: course management
			@Context HttpServletRequest httpRequest, @Context UriInfo uriInfo) {
		
		CatalogEntryVO entryVo = new CatalogEntryVO();
		entryVo.setName(name);
		entryVo.setDescription(description);
		return updateCatalogEntry(path, entryVo, newParentKey, httpRequest, uriInfo);
	}
	
	/**
	 * Updates the catalog entry with the path specified in the URL.
	 * 
	 * @param path The path
	 * @param id The id of the catalog entry
	 * @param name The name
	 * @param description The description
	 * @param newParentKey The parent key to move the entry (optional)
	 * @param httpRquest The HTTP request
	 * @param uriInfo The URI informations
	 * @return The response
	 */
	@POST
	@Path("{path:.*}")
	@Operation(summary = "Update a catalog", description = "Updates the catalog entry with the path specified in the URL")
	@ApiResponse(responseCode = "200", description = "The catalog entry", content = {
			@Content(mediaType = "application/json", schema = @Schema(implementation = CatalogEntryVO.class)),
			@Content(mediaType = "application/xml", schema = @Schema(implementation = CatalogEntryVO.class)) })
	@ApiResponse(responseCode = "401", description = "Not authorized")
	@ApiResponse(responseCode = "404", description = "The path could not be resolved to a valid catalog entry")
	@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	public Response updateCatalogEntry(@PathParam("path") List<PathSegment> path,
			@QueryParam("name") String name, @QueryParam("description") String description,
			@QueryParam("newParentKey") Long newParentKey,//fxdiff FXOLAT-122: course management
			@Context HttpServletRequest httpRequest, @Context UriInfo uriInfo) {
		
		CatalogEntryVO entryVo = new CatalogEntryVO();
		entryVo.setName(name);
		entryVo.setDescription(description);
		return updateCatalogEntry(path, entryVo, newParentKey, httpRequest, uriInfo);
	}
	
	/**
	 * Updates the catalog entry with the path specified in the URL.
	 * 
	 * @param path The path
	 * @param entryVo The catalog entry
	 * @param newParentKey The parent key to move the entry (optional)
	 * @param httpRquest The HTTP request
	 * @param uriInfo The URI informations
	 * @return The response
	 */
	@POST
	@Path("{path:.*}")
	@Operation(summary = "Update a catalog", description = "Updates the catalog entry with the path specified in the URL")
	@ApiResponse(responseCode = "200", description = "The catalog entry", content = {
			@Content(mediaType = "application/json", schema = @Schema(implementation = CatalogEntryVO.class)),
			@Content(mediaType = "application/xml", schema = @Schema(implementation = CatalogEntryVO.class)) })
	@ApiResponse(responseCode = "401", description = "Not authorized")
	@ApiResponse(responseCode = "404", description = "The path could not be resolved to a valid catalog entry")
	@Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	public Response updateCatalogEntry(@PathParam("path") List<PathSegment> path,
			CatalogEntryVO entryVo, @QueryParam("newParentKey") Long newParentKey, @Context HttpServletRequest httpRequest, @Context UriInfo uriInfo) {
		
		if(!isCatalogManager(httpRequest)) {
			return Response.serverError().status(Status.UNAUTHORIZED).build();
		}
		
		Long key = getCatalogEntryKeyFromPath(path);
		if(key == null) {
			return Response.serverError().status(Status.NOT_ACCEPTABLE).build();
		}
		
		CatalogEntry ce = catalogManager.loadCatalogEntry(key);
		if(ce.getType() == CatalogEntry.TYPE_NODE
				&& !canAdminSubTree(ce, httpRequest)) {
			return Response.serverError().status(Status.UNAUTHORIZED).build();
		}
		
		CatalogEntry newParent = null;
		if(newParentKey != null) {
			newParent = catalogManager.loadCatalogEntry(newParentKey);
			if(newParent.getType() == CatalogEntry.TYPE_NODE
					&& !canAdminSubTree(newParent, httpRequest)) {
				return Response.serverError().status(Status.UNAUTHORIZED).build();
			}
		}
		
		Identity id = getUserRequest(httpRequest).getIdentity();
		LockResult lock = CoordinatorManager.getInstance().getCoordinator().getLocker().acquireLock(lockRes, id, LOCK_TOKEN, null);
		if (!lock.isSuccess()) {
			return getLockedResponse(lock, httpRequest);
		}
		
		try {
			ce = catalogManager.loadCatalogEntry(ce);
			if(ce == null) {
				return Response.serverError().status(Status.NOT_FOUND).build();
			}
			
			//only update if needed
			if(StringHelper.containsNonWhitespace(entryVo.getName())) {
				ce.setName(entryVo.getName());
			}
			if(StringHelper.containsNonWhitespace(entryVo.getDescription())) {
				ce.setDescription(entryVo.getDescription());
			}
			if(entryVo.getType() != null) {
				ce.setType(guessType(entryVo));
			}
			catalogManager.updateCatalogEntry(ce);
			if(newParent != null) {
				catalogManager.moveCatalogEntry(ce, newParent);
			}
		} catch (Exception e) {
			throw new WebApplicationException(e);
		} finally {
			CoordinatorManager.getInstance().getCoordinator().getLocker().releaseLock(lock);
		}
		
		CatalogEntryVO newEntryVo = link(get(ce), uriInfo);
		return Response.ok(newEntryVo).build();
	}
	
	/**
	 * Deletes the catalog entry with the path specified in the URL.
	 * 
	 * @param path The path
	 * @param httpRquest The HTTP request
	 * @return The response
	 */
	@DELETE
	@Path("{path:.*}")
	@Operation(summary = "Delete a catalog", description = "Deletes the catalog entry with the path specified in the URL")
	@ApiResponse(responseCode = "200", description = "The catalog entry")
	@ApiResponse(responseCode = "401", description = "Not authorized")
	@ApiResponse(responseCode = "404", description = "The path could not be resolved to a valid catalog entry")
	@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	public Response deleteCatalogEntry(@PathParam("path") List<PathSegment> path, @Context HttpServletRequest httpRequest) {
		Long key = getCatalogEntryKeyFromPath(path);
		if(key == null) {
			return Response.serverError().status(Status.NOT_ACCEPTABLE).build();
		}
		
		CatalogEntry ce = catalogManager.loadCatalogEntry(key);
		if(ce == null) {
			return Response.serverError().status(Status.NOT_FOUND).build();
		}
		
		if(!canAdminSubTree(ce, httpRequest)) {
			return Response.serverError().status(Status.UNAUTHORIZED).build();
		}
		
		Identity id = getUserRequest(httpRequest).getIdentity();
		LockResult lock = CoordinatorManager.getInstance().getCoordinator().getLocker().acquireLock(lockRes, id, LOCK_TOKEN, null);
		if (!lock.isSuccess()) {
			return getLockedResponse(lock, httpRequest);
		}
		
		try {
			catalogManager.deleteCatalogEntry(ce);
		} catch(Exception e) {
			throw new WebApplicationException(e);
		} finally {
			CoordinatorManager.getInstance().getCoordinator().getLocker().releaseLock(lock);
		}
		return Response.ok().build();
	}
	
	/**
	 * Get the owners of the local sub tree
	 * 
	 * @param path The path
	 * @param httpRquest The HTTP request
	 * @return The response
	 */
	@GET
	@Path("{path:.*}/owners")
	@Operation(summary = "Get the owners of the local sub tree", description = "Get the owners of the local sub tree")
	@ApiResponse(responseCode = "200", description = "The catalog entry", content = {
			@Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = CatalogEntryVO.class))),
			@Content(mediaType = "application/xml", array = @ArraySchema(schema = @Schema(implementation = CatalogEntryVO.class))) })
	@ApiResponse(responseCode = "401", description = "Not authorized")
	@ApiResponse(responseCode = "404", description = "The path could not be resolved to a valid catalog entry")
	@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	public Response getOwners(@PathParam("path") List<PathSegment> path, @Context HttpServletRequest httpRequest) {
		Long key = getCatalogEntryKeyFromPath(path);
		if(key == null) {
			return Response.serverError().status(Status.NOT_ACCEPTABLE).build();
		}
		
		CatalogEntry ce = catalogManager.loadCatalogEntry(key);
		if(ce == null) {
			return Response.serverError().status(Status.NOT_FOUND).build();
		}

		if(!isCatalogManager(httpRequest) && !canAdminSubTree(ce, httpRequest)) {
			return Response.serverError().status(Status.UNAUTHORIZED).build();
		}

		List<Identity> ids = catalogManager.getOwners(ce);
		int count = 0;
		UserVO[] voes = new UserVO[ids.size()];
		for(Identity id:ids) {
			voes[count++] = UserVOFactory.get(id);
		}
		return Response.ok(voes).build();
	}
	
	/**
	 * Retrieves data of an owner of the local sub tree
	 * 
	 * @param path The path
	 * @Param identityKey The id of the user 
	 * @param httpRquest The HTTP request
	 * @return The response
	 */
	@GET
	@Path("{path:.*}/owners/{identityKey}")
	@Operation(summary = "Retrieves data of an owner of the local sub tree", description = "Retrieves data of an owner of the local sub tree")
	@ApiResponse(responseCode = "200", description = "The catalog entry", content = {
			@Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = CatalogEntryVO.class))),
			@Content(mediaType = "application/xml", array = @ArraySchema(schema = @Schema(implementation = CatalogEntryVO.class))) })
	@ApiResponse(responseCode = "401", description = "Not authorized")
	@ApiResponse(responseCode = "404", description = "The path could not be resolved to a valid catalog entry")
	public Response getOwner(@PathParam("path") List<PathSegment> path, @PathParam("identityKey") Long identityKey,
			@Context HttpServletRequest httpRequest) {
		Long key = getCatalogEntryKeyFromPath(path);
		if(key == null) {
			return Response.serverError().status(Status.NOT_ACCEPTABLE).build();
		}
		
		CatalogEntry ce = catalogManager.loadCatalogEntry(key);
		if(ce == null) {
			return Response.serverError().status(Status.NOT_FOUND).build();
		}

		if(!isCatalogManager(httpRequest) && !canAdminSubTree(ce, httpRequest)) {
			return Response.serverError().status(Status.UNAUTHORIZED).build();
		}

		List<Identity> ids = catalogManager.getOwners(ce);
		UserVO vo = null;
		for(Identity id:ids) {
			if(id.getKey().equals(identityKey)) {
				vo = UserVOFactory.get(id);
				break;
			}
		}
		if(vo == null) {
			return Response.serverError().status(Status.NOT_FOUND).build();
		}
		return Response.ok(vo).build();
	}
	
	/**
	 * Add an owner of the local sub tree
	 * 
	 * @param path The path
	 * @param identityKey The id of the user
	 * @param httpRquest The HTTP request
	 * @return The response
	 */
	@PUT
	@Path("{path:.*}/owners/{identityKey}")
	@Operation(summary = "Add an owner of the local sub tree", description = "Add an owner of the local sub tree")
	@ApiResponse(responseCode = "200", description = "The catalog entry", content = {
			@Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = CatalogEntryVO.class))),
			@Content(mediaType = "application/xml", array = @ArraySchema(schema = @Schema(implementation = CatalogEntryVO.class))) })
	@ApiResponse(responseCode = "401", description = "Not authorized")
	@ApiResponse(responseCode = "404", description = "The path could not be resolved to a valid catalog entry")
	public Response addOwner(@PathParam("path") List<PathSegment> path, @PathParam("identityKey") Long identityKey,
			@Context HttpServletRequest httpRequest) {
		Long key = getCatalogEntryKeyFromPath(path);
		if(key == null) {
			return Response.serverError().status(Status.NOT_ACCEPTABLE).build();
		}
		CatalogEntry ce = catalogManager.loadCatalogEntry(key);
		if(ce == null) {
			return Response.serverError().status(Status.NOT_FOUND).build();
		}

		if(!isCatalogManager(httpRequest) && !canAdminSubTree(ce, httpRequest)) {
			return Response.serverError().status(Status.UNAUTHORIZED).build();
		}
		
		Identity identity = securityManager.loadIdentityByKey(identityKey, false);
		if(identity == null) {
			return Response.serverError().status(Status.NOT_FOUND).build();
		}
		
		Identity id = getUserRequest(httpRequest).getIdentity();
		LockResult lock = CoordinatorManager.getInstance().getCoordinator().getLocker().acquireLock(lockRes, id, LOCK_TOKEN, null);
		if (!lock.isSuccess()) {
			return getLockedResponse(lock, httpRequest);
		}
		
		try {
			securityGroupDao.addIdentityToSecurityGroup(identity, ce.getOwnerGroup());
		} catch(Exception e) {
			throw new WebApplicationException(e);
		} finally {
			CoordinatorManager.getInstance().getCoordinator().getLocker().releaseLock(lock);
		}
		return Response.ok().build();
	}
	
	/**
	 * Remove an owner of the local sub tree
	 * @
	 * @param path The path
	 * @param identityKey The id of the user
	 * @param httpRquest The HTTP request
	 * @return The response
	 */
	@DELETE
	@Path("{path:.*}/owners/{identityKey}")
	@Operation(summary = "Remove an owner of the local sub tree", description = "Remove an owner of the local sub tree")
	@ApiResponse(responseCode = "200", description = "The catalog entry")
	@ApiResponse(responseCode = "401", description = "Not authorized")
	@ApiResponse(responseCode = "404", description = "The path could not be resolved to a valid catalog entry")
	public Response removeOwner(@PathParam("path") List<PathSegment> path, @PathParam("identityKey") Long identityKey,
			@Context HttpServletRequest httpRequest) {
		Long key = getCatalogEntryKeyFromPath(path);
		if(key == null) {
			return Response.serverError().status(Status.NOT_ACCEPTABLE).build();
		}
		
		CatalogEntry ce = catalogManager.loadCatalogEntry(key);
		if(ce == null) {
			return Response.serverError().status(Status.NOT_FOUND).build();
		}

		if(!isCatalogManager(httpRequest) && !canAdminSubTree(ce, httpRequest)) {
			return Response.serverError().status(Status.UNAUTHORIZED).build();
		}
		
		Identity identity = securityManager.loadIdentityByKey(identityKey, false);
		if(identity == null) {
			return Response.ok().build();
		}
		
		Identity id = getUserRequest(httpRequest).getIdentity();
		LockResult lock = CoordinatorManager.getInstance().getCoordinator().getLocker().acquireLock(lockRes, id, LOCK_TOKEN, null);
		if (!lock.isSuccess()) {
			return getLockedResponse(lock, httpRequest);
		}
		
		try {
			catalogManager.removeOwner(ce, identity);
		} catch(Exception e) {
			throw new WebApplicationException(e);
		} finally {
			CoordinatorManager.getInstance().getCoordinator().getLocker().releaseLock(lock);
		}
		return Response.ok().build();
	}
	
	private Response getLockedResponse(LockResult lock, HttpServletRequest request) {
		Locale locale = null;
		UserRequest ureq = getUserRequest(request);
		if(ureq != null) {
			locale = LocaleNegotiator.getPreferedLocale(ureq);
		} 
		if(locale == null) {
			locale = I18nModule.getDefaultLocale();
		}
		
		Translator translator = Util.createPackageTranslator(CatalogNodeManagerController.class, locale);

		String ownerName = userManager.getUserDisplayName(lock.getOwner());
		String translation = translator.translate("catalog.locked.by", new String[]{ ownerName });
		ErrorVO vo = new ErrorVO("org.olat.catalog.ui","catalog.locked.by",translation);
		ErrorVO[] voes = new ErrorVO[]{vo};
		return Response.ok(voes).status(Status.UNAUTHORIZED).build();
	}
	
	private Long getCatalogEntryKeyFromPath(List<PathSegment> path) {
		PathSegment lastPath = path.get(path.size() - 1);
		Long key = null;
		try {
			key = Long.valueOf(lastPath.getPath());
		} catch (NumberFormatException e) {
			key = null;
		}
		return key;
	}
	
	private boolean canAdminSubTree(CatalogEntry ce, HttpServletRequest httpRequest) {
		if(isCatalogManager(httpRequest)) {
			return true;
		}
		
		Identity identity = getUserRequest(httpRequest).getIdentity();
		return catalogManager.isOwner(ce, identity);
	}
	
	private int guessType(CatalogEntryVO vo) {
		Integer type = vo.getType();
		if(type == null) {
			if(vo.getRepositoryEntryKey() == null) {
				return CatalogEntry.TYPE_NODE;
			}
			return CatalogEntry.TYPE_LEAF;
		}
		return type.intValue();
	}
	
	private boolean isCatalogManager(HttpServletRequest request) {
		try {
			Roles roles = getRoles(request);
			return roles.isAdministrator() || roles.isLearnResourceManager();
		} catch (Exception e) {
			return false;
		}
	}
}
