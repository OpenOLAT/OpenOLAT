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


import java.util.Collections;
import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.CacheControl;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.PathSegment;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

import org.olat.core.commons.services.vfs.restapi.VFSWebservice;
import org.olat.core.util.WebappHelper;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.core.util.vfs.VFSItem;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.core.util.vfs.callbacks.ReadOnlyCallback;
import org.olat.core.util.vfs.filters.VFSSystemItemFilter;
import org.olat.modules.sharedfolder.SharedFolderManager;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntrySecurity;
import org.olat.repository.RepositoryManager;
import org.olat.restapi.security.RestSecurityHelper;
import org.olat.restapi.support.vo.LinkVO;
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
 * Initial Date:  5 may 2017 <br>
 * @author Stephan Clemenz, VCRP
 */
@Tag(name = "Repo")
@Component
@Path("repo/sharedfolder")
public class SharedFolderWebService {

	private static final String VERSION = "1.0";
	public static final CacheControl cc = new CacheControl();
	static {
		cc.setMaxAge(-1);
	}
	
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
	@ApiResponse(responseCode = "200", description = "Return the version number")	
	@Produces(MediaType.TEXT_PLAIN)
	public Response getVersion() {
		return Response.ok(VERSION).build();
	}

	/**
	 * This retrieves the files in the shared folder
	 * 
	 * @param repoEntryKey The course resourceable's id
	 * @param uri The uri infos
	 * @param httpRequest The HTTP request
	 * @param request The REST request
	 * @return 
	 */
	@GET
	@Path("{repoEntryKey}")
	@Operation(summary = "This retrieves the files in the shared folder", description = "This retrieves the files in the shared folder")
	@ApiResponse(responseCode = "200", description = "The files", content = {
			@Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = LinkVO.class))),
			@Content(mediaType = "application/xml", array = @ArraySchema(schema = @Schema(implementation = LinkVO.class))) })
	@ApiResponse(responseCode = "401", description = "The roles of the authenticated user are not sufficient")
	@ApiResponse(responseCode = "404", description = "The shared folder is not found")
	public Response getSharedFiles(@PathParam("repoEntryKey") Long repoEntryKey, @Context UriInfo uriInfo,
			@Context HttpServletRequest httpRequest, @Context Request request) {
		return getFiles(repoEntryKey, Collections.<PathSegment>emptyList(), uriInfo, httpRequest, request);
	}

	/**
	 * This retrieves the files in the shared folder and give full access to
	 * the folder, read, write, delete.
	 * 
	 * @param repoEntryKey The course resourceable's id
	 * @param httpRequest The HTTP request
	 * @return 
	 */
	@Path("{repoEntryKey}/files")
	@Operation(summary = "This retrieves the files in the shared folder", description = "This retrieves the files in the shared folder and give full access to\n" + 
			" the folder, read, write, delete.")
	@ApiResponse(responseCode = "200", description = "The files", content = {
			@Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = LinkVO.class))),
			@Content(mediaType = "application/xml", array = @ArraySchema(schema = @Schema(implementation = LinkVO.class))) })
	@ApiResponse(responseCode = "401", description = "The roles of the authenticated user are not sufficient")
	@ApiResponse(responseCode = "404", description = "The shared folder is not found")
	@Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, MediaType.TEXT_HTML, MediaType.APPLICATION_OCTET_STREAM})
	public VFSWebservice getVFSWebservice(@PathParam("repoEntryKey") Long repoEntryKey, @Context HttpServletRequest httpRequest) {
		RepositoryEntry re = repositoryManager.lookupRepositoryEntry(repoEntryKey);
		if(re == null) {
			throw new WebApplicationException( Response.serverError().status(Status.NOT_FOUND).build());
		}

		VFSContainer container = SharedFolderManager.getInstance().getNamedSharedFolder(re, true);
		if(container == null) {
			throw new WebApplicationException( Response.serverError().status(Status.NOT_FOUND).build());
		}
	
		RepositoryEntrySecurity reSecurity = repositoryManager
			.isAllowed(RestSecurityHelper.getIdentity(httpRequest), RestSecurityHelper.getRoles(httpRequest), re);
		if(reSecurity.isEntryAdmin()) {
			// all ok
		} else if(reSecurity.isMember()) {
			container.setLocalSecurityCallback(new ReadOnlyCallback());
		} else {
			throw new WebApplicationException( Response.serverError().status(Status.UNAUTHORIZED).build());
		}
		return new VFSWebservice(container);
	}

	public Response getFiles(Long repoEntryKey, List<PathSegment> path, UriInfo uriInfo, HttpServletRequest httpRequest, Request request) {
		RepositoryEntry re = repositoryManager.lookupRepositoryEntry(repoEntryKey);
		if(re == null) {
			return Response.serverError().status(Status.NOT_FOUND).build();
		}
		VFSContainer container = SharedFolderManager.getInstance().getNamedSharedFolder(re, true);
		if(container == null) {
			return Response.serverError().status(Status.NOT_FOUND).build();
		}
		if (!repositoryManager.isAllowedToLaunch(RestSecurityHelper.getIdentity(httpRequest), RestSecurityHelper.getRoles(httpRequest), re)) {
			return Response.serverError().status(Status.UNAUTHORIZED).build();
		}

		VFSLeaf leaf = null;
		for(PathSegment seg:path) {
			VFSItem item = container.resolve(seg.getPath());
			if(item instanceof VFSLeaf) {
				leaf = (VFSLeaf)item;
				break;
			} else if (item instanceof VFSContainer) {
				container = (VFSContainer)item;
			}
		}

		if(leaf != null) {
			Date lastModified = new Date(leaf.getLastModified());
			Response.ResponseBuilder response = request.evaluatePreconditions(lastModified);
			if(response == null) {
				String mimeType = WebappHelper.getMimeType(leaf.getName());
				if (mimeType == null) mimeType = MediaType.APPLICATION_OCTET_STREAM;
				response = Response.ok(leaf.getInputStream(), mimeType).lastModified(lastModified).cacheControl(cc);
			}
			return response.build();
		} 

		List<VFSItem> items = container.getItems(new VFSSystemItemFilter());
		int count=0;
		LinkVO[] links = new LinkVO[items.size()];
		for(VFSItem item:items) {
			UriBuilder baseUriBuilder = uriInfo.getBaseUriBuilder();
			UriBuilder repoUri = baseUriBuilder.path(SharedFolderWebService.class).path(repoEntryKey.toString()).path("files");
			for(PathSegment pathSegment:path) {
				repoUri.path(pathSegment.getPath());
			}
			String uri = repoUri.path(item.getName()).build().toString();
			links[count++] = new LinkVO("self", uri, item.getName());
		}

		return Response.ok(links).build();
	}
}
