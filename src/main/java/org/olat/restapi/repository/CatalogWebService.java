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
import static org.olat.restapi.security.RestSecurityHelper.getUserRequest;
import static org.olat.restapi.security.RestSecurityHelper.isAdmin;
import static org.olat.restapi.security.RestSecurityHelper.isAuthor;
import static org.olat.restapi.support.CatalogVOFactory.get;
import static org.olat.restapi.support.CatalogVOFactory.link;

import java.util.List;
import java.util.Locale;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.PathSegment;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;

import org.olat.basesecurity.BaseSecurity;
import org.olat.basesecurity.manager.SecurityGroupDAO;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.translator.Translator;
import org.olat.core.id.Identity;
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

/**
 * Description:<br>
 * A web service for the catalog
 * 
 * <P>
 * Initial Date:  5 may 2010 <br>
 * @author srosse, stephane.rosse@frentix.com
 */
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
   * @response.representation.200.mediaType text/plain
   * @response.representation.200.doc The version of this specific Web Service
   * @response.representation.200.example 1.0
	 * @return
	 */
	@GET
	@Path("version")
	@Produces(MediaType.TEXT_PLAIN)
	public Response getVersion() {
		return Response.ok(VERSION).build();
	}
	
	/**
	 * Returns the list of root catalog entries.
	 * @response.representation.200.qname {http://www.example.com}catalogEntryVO
   * @response.representation.200.mediaType application/xml, application/json
   * @response.representation.200.doc The list of roots catalog entries
   * @response.representation.200.example {@link org.olat.restapi.support.vo.Examples#SAMPLE_CATALOGENTRYVOes}
	 * @return The response
	 */
	@GET
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
	 * @response.representation.200.qname {http://www.example.com}catalogEntryVO
   * @response.representation.200.mediaType application/xml, application/json
   * @response.representation.200.doc The catalog entry
   * @response.representation.200.example {@link org.olat.restapi.support.vo.Examples#SAMPLE_CATALOGENTRYVO}
   * @response.representation.401.doc The path could not be resolved to a valid catalog entry
   * @param path The path
   * @param uriInfo The URI informations
   * @param httpRequest The HTTP request
   * @param request The REST request
	 * @return The response
	 */
	@GET
	@Path("{path:.*}")
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
	 * @response.representation.200.qname {http://www.example.com}catalogEntryVO
   * @response.representation.200.mediaType application/xml, application/json
   * @response.representation.200.doc The list of catalog entries
   * @response.representation.200.example {@link org.olat.restapi.support.vo.Examples#SAMPLE_CATALOGENTRYVOes}
   * @response.representation.404.doc The path could not be resolved to a valid catalog entry
   * @param path The path
   * @param start
   * @param limit
   * @param httpRequest The HTTP request
   * @param request The REST request
	 * @return The response
	 */
	@GET
	@Path("{path:.*}/children")
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
	 * @response.representation.200.qname {http://www.example.com}catalogEntryVO
   * @response.representation.200.mediaType application/xml, application/json
   * @response.representation.200.doc The catalog entry
   * @response.representation.200.example {@link org.olat.restapi.support.vo.Examples#SAMPLE_CATALOGENTRYVO}
   * @response.representation.401.doc Not authorized
   * @response.representation.404.doc The path could not be resolved to a valid catalog entry
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
	 * @response.representation.qname {http://www.example.com}catalogEntryVO
   * @response.representation.mediaType application/xml, application/json
   * @response.representation.doc The catalog entry
   * @response.representation.example {@link org.olat.restapi.support.vo.Examples#SAMPLE_CATALOGENTRYVO}
	 * @response.representation.200.qname {http://www.example.com}catalogEntryVO
   * @response.representation.200.mediaType application/xml, application/json
   * @response.representation.200.doc The list of catalog entry
   * @response.representation.200.example {@link org.olat.restapi.support.vo.Examples#SAMPLE_CATALOGENTRYVO}
   * @response.representation.401.doc Not authorized
   * @response.representation.404.doc The path could not be resolved to a valid catalog entry
   * @param path The path
   * @param entryVo The catalog entry
   * @param httpRquest The HTTP request
   * @param uriInfo The URI informations
	 * @return The response
	 */
	@PUT
	@Path("{path:.*}")
	@Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	public Response addCatalogEntry(@PathParam("path") List<PathSegment> path, CatalogEntryVO entryVo,
			@Context HttpServletRequest httpRequest, @Context UriInfo uriInfo)
	throws WebApplicationException {
		if(!isAuthor(httpRequest)) {
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
		LockResult lock = CoordinatorManager.getInstance().getCoordinator().getLocker().acquireLock(lockRes, id, LOCK_TOKEN);
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
	 * @response.representation.200.qname {http://www.example.com}catalogEntryVO
   * @response.representation.200.mediaType application/xml, application/json
   * @response.representation.200.doc The catalog entry
   * @response.representation.200.example {@link org.olat.restapi.support.vo.Examples#SAMPLE_CATALOGENTRYVO}
   * @response.representation.401.doc Not authorized
   * @response.representation.404.doc The path could not be resolved to a valid catalog entry
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
	 * @response.representation.200.qname {http://www.example.com}catalogEntryVO
   * @response.representation.200.mediaType application/xml, application/json
   * @response.representation.200.doc The catalog entry
   * @response.representation.200.example {@link org.olat.restapi.support.vo.Examples#SAMPLE_CATALOGENTRYVO}
   * @response.representation.401.doc Not authorized
   * @response.representation.404.doc The path could not be resolved to a valid catalog entry
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
	 * @response.representation.200.qname {http://www.example.com}catalogEntryVO
   * @response.representation.200.mediaType application/xml, application/json
   * @response.representation.200.doc The catalog entry
   * @response.representation.200.example {@link org.olat.restapi.support.vo.Examples#SAMPLE_CATALOGENTRYVO}
   * @response.representation.401.doc Not authorized
   * @response.representation.404.doc The path could not be resolved to a valid catalog entry
   * @param path The path
   * @param entryVo The catalog entry
   * @param newParentKey The parent key to move the entry (optional)
   * @param httpRquest The HTTP request
   * @param uriInfo The URI informations
	 * @return The response
	 */
	@POST
	@Path("{path:.*}")
	@Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	public Response updateCatalogEntry(@PathParam("path") List<PathSegment> path,
			CatalogEntryVO entryVo, @QueryParam("newParentKey") Long newParentKey, @Context HttpServletRequest httpRequest, @Context UriInfo uriInfo) {
		
		if(!isAuthor(httpRequest)) {
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
		LockResult lock = CoordinatorManager.getInstance().getCoordinator().getLocker().acquireLock(lockRes, id, LOCK_TOKEN);
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
	 * @response.representation.200.qname {http://www.example.com}catalogEntryVO
	 * @response.representation.200.mediaType application/xml, application/json
	 * @response.representation.200.doc The catalog entry
	 * @response.representation.200.example {@link org.olat.restapi.support.vo.Examples#SAMPLE_CATALOGENTRYVO}
	 * @response.representation.401.doc Not authorized
	 * @response.representation.404.doc The path could not be resolved to a valid catalog entry
	 * @param path The path
	 * @param httpRquest The HTTP request
	 * @return The response
	 */
	@DELETE
	@Path("{path:.*}")
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
		LockResult lock = CoordinatorManager.getInstance().getCoordinator().getLocker().acquireLock(lockRes, id, LOCK_TOKEN);
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
	 * @response.representation.200.qname {http://www.example.com}userVO
   * @response.representation.200.mediaType application/xml, application/json
   * @response.representation.200.doc The catalog entry
   * @response.representation.200.example {@link org.olat.user.restapi.Examples#SAMPLE_USERVOes}
   * @response.representation.401.doc Not authorized
   * @response.representation.404.doc The path could not be resolved to a valid catalog entry
   * @param path The path
   * @param httpRquest The HTTP request
	 * @return The response
	 */
	@GET
	@Path("{path:.*}/owners")
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

		if(!isAuthor(httpRequest) && !canAdminSubTree(ce, httpRequest)) {
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
	 * @response.representation.200.qname {http://www.example.com}userVO
   * @response.representation.200.mediaType application/xml, application/json
   * @response.representation.200.doc The catalog entry
   * @response.representation.200.example {@link org.olat.user.restapi.Examples#SAMPLE_USERVOes}
   * @response.representation.401.doc Not authorized
   * @response.representation.404.doc The path could not be resolved to a valid catalog entry
   * @param path The path
   * @Param identityKey The id of the user 
   * @param httpRquest The HTTP request
	 * @return The response
	 */
	@GET
	@Path("{path:.*}/owners/{identityKey}")
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

		if(!isAuthor(httpRequest) && !canAdminSubTree(ce, httpRequest)) {
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
	 * @response.representation.200.qname {http://www.example.com}userVO
   * @response.representation.200.mediaType application/xml, application/json
   * @response.representation.200.doc The catalog entry
   * @response.representation.200.example {@link org.olat.user.restapi.Examples#SAMPLE_USERVOes}
   * @response.representation.401.doc Not authorized
   * @response.representation.404.doc The path could not be resolved to a valid catalog entry
   * @param path The path
   * @param identityKey The id of the user
   * @param httpRquest The HTTP request
	 * @return The response
	 */
	@PUT
	@Path("{path:.*}/owners/{identityKey}")
	public Response addOwner(@PathParam("path") List<PathSegment> path, @PathParam("identityKey") Long identityKey,
			@Context HttpServletRequest httpRequest)
	throws WebApplicationException {
		
		Long key = getCatalogEntryKeyFromPath(path);
		if(key == null) {
			return Response.serverError().status(Status.NOT_ACCEPTABLE).build();
		}
		
		CatalogEntry ce = catalogManager.loadCatalogEntry(key);
		if(ce == null) {
			return Response.serverError().status(Status.NOT_FOUND).build();
		}

		if(!isAuthor(httpRequest) && !canAdminSubTree(ce, httpRequest)) {
			return Response.serverError().status(Status.UNAUTHORIZED).build();
		}
		
		Identity identity = securityManager.loadIdentityByKey(identityKey, false);
		if(identity == null) {
			return Response.serverError().status(Status.NOT_FOUND).build();
		}
		
		Identity id = getUserRequest(httpRequest).getIdentity();
		LockResult lock = CoordinatorManager.getInstance().getCoordinator().getLocker().acquireLock(lockRes, id, LOCK_TOKEN);
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
	 * @response.representation.200.qname {http://www.example.com}userVO
   * @response.representation.200.mediaType application/xml, application/json
   * @response.representation.200.doc The catalog entry
   * @response.representation.200.example {@link org.olat.user.restapi.Examples#SAMPLE_USERVOes}
   * @response.representation.401.doc Not authorized
   * @response.representation.404.doc The path could not be resolved to a valid catalog entry
   * @param path The path
   * @param identityKey The id of the user
   * @param httpRquest The HTTP request
	 * @return The response
	 */
	@DELETE
	@Path("{path:.*}/owners/{identityKey}")
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

		if(!isAuthor(httpRequest) && !canAdminSubTree(ce, httpRequest)) {
			return Response.serverError().status(Status.UNAUTHORIZED).build();
		}
		
		Identity identity = securityManager.loadIdentityByKey(identityKey, false);
		if(identity == null) {
			return Response.ok().build();
		}
		
		Identity id = getUserRequest(httpRequest).getIdentity();
		LockResult lock = CoordinatorManager.getInstance().getCoordinator().getLocker().acquireLock(lockRes, id, LOCK_TOKEN);
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
			key = new Long(lastPath.getPath());
		} catch (NumberFormatException e) {
			key = null;
		}
		return key;
	}
	
	private boolean canAdminSubTree(CatalogEntry ce, HttpServletRequest httpRequest) {
		if(isAdmin(httpRequest)) {
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
}
