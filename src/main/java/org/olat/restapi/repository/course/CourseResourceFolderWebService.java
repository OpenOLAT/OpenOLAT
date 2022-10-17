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

package org.olat.restapi.repository.course;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.CacheControl;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.PathSegment;
import jakarta.ws.rs.core.Request;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;
import jakarta.ws.rs.core.UriBuilder;
import jakarta.ws.rs.core.UriInfo;

import org.apache.logging.log4j.Logger;
import org.olat.core.commons.services.vfs.VFSMetadata;
import org.olat.core.commons.services.vfs.VFSRepositoryService;
import org.olat.core.commons.services.vfs.model.VFSMetadataImpl;
import org.olat.core.gui.UserRequest;
import org.olat.core.id.Identity;
import org.olat.core.id.Roles;
import org.olat.core.logging.Tracing;
import org.olat.core.util.WebappHelper;
import org.olat.core.util.vfs.VFSConstants;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.core.util.vfs.VFSItem;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.core.util.vfs.VFSLockApplicationType;
import org.olat.core.util.vfs.VFSLockManager;
import org.olat.core.util.vfs.VFSManager;
import org.olat.core.util.vfs.callbacks.ReadOnlyCallback;
import org.olat.core.util.vfs.filters.VFSSystemItemFilter;
import org.olat.course.ICourse;
import org.olat.course.config.CourseConfig;
import org.olat.modules.sharedfolder.SharedFolderManager;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntrySecurity;
import org.olat.repository.RepositoryManager;
import org.olat.repository.RepositoryService;
import org.olat.resource.OLATResource;
import org.olat.restapi.repository.SharedFolderWebService;
import org.olat.restapi.security.RestSecurityHelper;
import org.olat.restapi.support.MultipartReader;
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
 * 
 * Description:<br>
 * This will handle the resources folders in the course: the course storage folder
 * and the shared folder. The course folder has a read-write access but the shared
 * folder can only be read.
 * 
 * <P>
 * Initial Date:  26 apr. 2010 <br>
 * @author srosse, stephane.rosse@frentix.com
 */
@Tag(name = "Repo")
@Component
@Path("repo/courses/{courseId}/resourcefolders")
public class CourseResourceFolderWebService {

	private static final Logger log = Tracing.createLoggerFor(CourseResourceFolderWebService.class);

	private static final String VERSION  = "1.0";

	private static final CacheControl cc = new CacheControl();
	static {
		cc.setMaxAge(-1);
	}

	@Autowired
	private VFSLockManager vfsLockManager;
	@Autowired
	private RepositoryManager repositoryManager;
	@Autowired
	private RepositoryService repositoryService;
	@Autowired
	private VFSRepositoryService vfsRepositoryService;

	/**
	 * The version of the resources folders Web Service
	 * 
	 * @return
	 */
	@GET
	@Path("version")
	@Operation(summary = "The version of the resources folders Web Service", description = "The version of the resources folders Web Service")
	@ApiResponse(responseCode = "200", description = "The version of this specific Web Service")
	@Produces(MediaType.TEXT_PLAIN)
	public Response getVersion() {
		return Response.ok(VERSION).build();
	}

	/**
	 * This retrieves the files in the shared folder
	 * 
	 * @param courseId The course resourceable's id
	 * @param uri The uri infos
	 * @param httpRequest The HTTP request
	 * @param request The REST request
	 * @return 
	 */
	@GET
	@Path("sharedfolder")
	@Operation(summary = "This retrieves the files in the shared folder", description = "This retrieves the files in the shared folder")
	@ApiResponse(responseCode = "200", description = "The list of files", content = {
			@Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = LinkVO.class))),
			@Content(mediaType = "application/xml", array = @ArraySchema(schema = @Schema(implementation = LinkVO.class))) })
	@ApiResponse(responseCode = "401", description = "The roles of the authenticated user are not sufficient")
	@ApiResponse(responseCode = "404", description = "The course or the shared folder not found")	
	public Response getSharedFiles(@PathParam("courseId") Long courseId, @Context UriInfo uriInfo,
			@Context HttpServletRequest httpRequest, @Context Request request) {
		return getFiles(courseId, Collections.<PathSegment>emptyList(), FolderType.SHARED_FOLDER, uriInfo, httpRequest, request);
	}

	/**
	 * This retrieves the files in the shared folder
	 * 
	 * @param courseId The course resourceable's id
	 * @param path The path of the file or directory
	 * @param uri The uri infos
	 * @param httpRequest The HTTP request
	 * @param request The REST request
	 * @return 
	 */
	@GET
	@Path("sharedfolder/{path:.*}")
	@Operation(summary = "This retrieves the files in the shared folder", description = "This retrieves the files in the shared folder")
	@ApiResponse(responseCode = "200", description = "The list of files", content = {
			@Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = LinkVO.class))),
			@Content(mediaType = "application/xml", array = @ArraySchema(schema = @Schema(implementation = LinkVO.class))) })
	@ApiResponse(responseCode = "401", description = "The roles of the authenticated user are not sufficient")
	@ApiResponse(responseCode = "404", description = "The course or the shared folder not found")
	@ApiResponse(responseCode = "406", description = "The course node is not acceptable to copy a file")
	@Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, MediaType.TEXT_HTML, MediaType.APPLICATION_OCTET_STREAM})
	public Response getSharedFiles(@PathParam("courseId") Long courseId, @PathParam("path") List<PathSegment> path,
			@Context UriInfo uriInfo, @Context HttpServletRequest httpRequest, @Context Request request) {
		return getFiles(courseId, path, FolderType.SHARED_FOLDER, uriInfo, httpRequest, request);
	}


	/**
	 * This retrieves the files in the course folder
	 * 
	 * @param courseId The course resourceable's id
	 * @param uri The uri infos
	 * @param httpRequest The HTTP request
	 * @param request The REST request
	 * @return 
	 */
	@GET
	@Path("coursefolder")
	@Operation(summary = "This retrieves the files in the course folder", description = "This retrieves the files in the course folder")
	@ApiResponse(responseCode = "200", description = "The list of files", content = {
			@Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = LinkVO.class))),
			@Content(mediaType = "application/xml", array = @ArraySchema(schema = @Schema(implementation = LinkVO.class))) })
	@ApiResponse(responseCode = "401", description = "The roles of the authenticated user are not sufficient")
	@ApiResponse(responseCode = "404", description = "The course or the shared folder not found")
	public Response getCourseFiles(@PathParam("courseId") Long courseId, @Context UriInfo uriInfo,
			@Context HttpServletRequest httpRequest, @Context Request request) {
		return getFiles(courseId, Collections.<PathSegment>emptyList(), FolderType.COURSE_FOLDER, uriInfo, httpRequest, request);
	}

	/**
	 * This retrieves the files in the course folder
	 * 
	 * @param courseId The course resourceable's id
	 * @param path The path of the file or directory
	 * @param uri The uri infos
	 * @param httpRequest The HTTP request
	 * @param request The REST request
	 * @return 
	 */
	@GET
	@Path("coursefolder/{path:.*}")
	@Operation(summary = "This retrieves the files in the course folder", description = "This retrieves the files in the course folder")
	@ApiResponse(responseCode = "200", description = "The list of files", content = {
			@Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = LinkVO.class))),
			@Content(mediaType = "application/xml", array = @ArraySchema(schema = @Schema(implementation = LinkVO.class))) })
	@ApiResponse(responseCode = "401", description = "The roles of the authenticated user are not sufficient")
	@ApiResponse(responseCode = "404", description = "The course or the shared folder not found")
	@ApiResponse(responseCode = "406", description = "The course node is not acceptable to copy a file")
	@Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, MediaType.TEXT_HTML, MediaType.APPLICATION_OCTET_STREAM})
	public Response getCourseFiles(@PathParam("courseId") Long courseId, @PathParam("path") List<PathSegment> path,
			@Context UriInfo uriInfo, @Context HttpServletRequest httpRequest, @Context Request request) {
		return getFiles(courseId, path, FolderType.COURSE_FOLDER, uriInfo, httpRequest, request);
	}

	/**
	 * This attaches the uploaded file(s) to the supplied folder id.
	 * 
	 * @param courseId The course resourceable's id
	 * @param filename The filename
	 * @param file The file resource to upload
	 * @param request The HTTP request
	 * @return 
	 */
	@POST
	@Path("coursefolder")
	@Operation(summary = "This attaches the uploaded file(s) to the supplied folder id", description = "This attaches the uploaded file(s) to the supplied folder id")
	@ApiResponse(responseCode = "200", description = "The file is correctly saved")
	@ApiResponse(responseCode = "401", description = "The roles of the authenticated user are not sufficient")
	@ApiResponse(responseCode = "404", description = "The course or course node not found")
	@ApiResponse(responseCode = "406", description = "The course node is not acceptable to copy a file")
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	public Response attachFileToFolderPost(@PathParam("courseId") Long courseId,
			@Context HttpServletRequest request) {
		return attachFileToCourseFolder(courseId, Collections.<PathSegment>emptyList(), request);
	}

	/**
	 * This attaches the uploaded file(s) to the supplied folder id at the specified path.
	 * 
	 * @param courseId The course resourceable's id
	 * @param path The path of the file
	 * @param filename The filename
	 * @param file The file resource to upload
	 * @param request The HTTP request
	 * @return 
	 */
	@POST
	@Path("coursefolder/{path:.*}")
	@Operation(summary = "Attach the uploaded file(s) to the supplied folder id at the specified path", description = "This attaches the uploaded file(s) to the supplied folder id at the specified path")
	@ApiResponse(responseCode = "200", description = "The file is correctly saved")
	@ApiResponse(responseCode = "401", description = "The roles of the authenticated user are not sufficient")
	@ApiResponse(responseCode = "404", description = "The course or course node not found")
	@ApiResponse(responseCode = "406", description = "The course node is not acceptable to copy a file")
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	public Response attachFileToFolderPost(@PathParam("courseId") Long courseId, @PathParam("path") List<PathSegment> path,
			@Context HttpServletRequest request) {
		return attachFileToCourseFolder(courseId, path, request);
	}

	/**
	 * This attaches the uploaded file(s) to the supplied folder id at the root level
	 * 
	 * @param courseId The course resourceable's id
	 * @param nodeId The id for the folder that will contain the file(s)
	 * @param filename The filename
	 * @param file The file resource to upload
	 * @param request The HTTP request
	 * @return 
	 */
	@PUT
	@Path("coursefolder")
	@Operation(summary = "Attach the uploaded file(s) to the supplied folder id at the root level", description = "This attaches the uploaded file(s) to the supplied folder id at the root level")
	@ApiResponse(responseCode = "200", description = "The file is correctly saved")
	@ApiResponse(responseCode = "401", description = "The roles of the authenticated user are not sufficient")
	@ApiResponse(responseCode = "404", description = "The course or course node not found")
	@ApiResponse(responseCode = "406", description = "The course node is not acceptable to copy a file")
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	public Response attachFileToFolder(@PathParam("courseId") Long courseId,
			@Context HttpServletRequest request) {
		return attachFileToCourseFolder(courseId, Collections.<PathSegment>emptyList(), request);
	}

	/**
	 * This attaches the uploaded file(s) to the supplied folder id at the specified path
	 * 
	 * @param courseId The course resourceable's id
	 * @param nodeId The id for the folder that will contain the file(s)
	 * @param filename The filename
	 * @param file The file resource to upload
	 * @param request The HTTP request
	 * @return 
	 */	
	@PUT
	@Path("coursefolder/{path:.*}")
	@Operation(summary = "Attach the uploaded file(s) to the supplied folder id at the specified path", description = "This attaches the uploaded file(s) to the supplied folder id at the specified path")
	@ApiResponse(responseCode = "200", description = "The file is correctly saved")
	@ApiResponse(responseCode = "401", description = "The roles of the authenticated user are not sufficient")
	@ApiResponse(responseCode = "404", description = "The course or course node not found")
	@ApiResponse(responseCode = "406", description = "The course node is not acceptable to copy a file")
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	public Response attachFileToFolder(@PathParam("courseId") Long courseId, @PathParam("path") List<PathSegment> path,
			@Context HttpServletRequest request) {
		return attachFileToCourseFolder(courseId, path, request);
	}

	private Response attachFileToCourseFolder(Long courseId, List<PathSegment> path, HttpServletRequest request) {

		MultipartReader partsReader = null;
		try {
			partsReader = new MultipartReader(request);
			File tmpFile = partsReader.getFile();
			InputStream in = new FileInputStream(tmpFile);
			String filename = partsReader.getValue("filename");
			return attachFileToCourseFolder(courseId, path, filename, in, request);
		} catch (FileNotFoundException e) {
			log.error("", e);
			return Response.serverError().status(Status.INTERNAL_SERVER_ERROR).build();
		} finally {
			MultipartReader.closeQuietly(partsReader);
		}
	}

	private Response attachFileToCourseFolder(Long courseId, List<PathSegment> path, String filename, InputStream file, HttpServletRequest request) {
		ICourse course = CoursesWebService.loadCourse(courseId);
		if(course == null) {
			return Response.serverError().status(Status.NOT_FOUND).build();
		}
		if(!isAuthor(course, request)) {
			return Response.serverError().status(Status.UNAUTHORIZED).build();
		}

		VFSContainer container = course.getCourseFolderContainer();
		for(PathSegment segment:path) {
			VFSItem item = container.resolve(segment.getPath());
			if(item instanceof VFSContainer) {
				container = (VFSContainer)item;
			} else if(item == null) {
				//create the folder
				container = container.createChildContainer(segment.getPath());
			}
		}

		VFSItem newFile;
		UserRequest ureq = RestSecurityHelper.getUserRequest(request);
		if (container.resolve(filename) != null) {
			VFSItem existingVFSItem = container.resolve(filename);
			if(existingVFSItem instanceof VFSContainer) {
				//already exists
				return Response.ok().build();
			}

			//check if it's locked
			boolean locked = vfsLockManager.isLockedForMe(existingVFSItem, ureq.getIdentity(), VFSLockApplicationType.vfs, null);
			if(locked) {
				return Response.serverError().status(Status.UNAUTHORIZED).build();
			}

			if (existingVFSItem instanceof VFSLeaf && existingVFSItem.canVersion() == VFSConstants.YES) {
				VFSLeaf existingLeaf = (VFSLeaf)existingVFSItem;
				vfsRepositoryService.addVersion(existingLeaf, ureq.getIdentity(), false, "REST upload", file);
				newFile = existingLeaf;
			} else {
				existingVFSItem.delete();
				newFile = container.createChildLeaf(filename);
				VFSManager.copyContent(file, (VFSLeaf)newFile, ureq.getIdentity());
			}
		} else if (file != null) {
			newFile = container.createChildLeaf(filename);
			VFSManager.copyContent(file, (VFSLeaf)newFile, ureq.getIdentity());
		} else {
			newFile = container.createChildContainer(filename);
			VFSMetadata infos = newFile.getMetaInfo();
			if (infos instanceof VFSMetadataImpl) {
				((VFSMetadataImpl)infos).setFileInitializedBy(ureq.getIdentity());
				vfsRepositoryService.updateMetadata(infos);
			}
		}

		return Response.ok().build();
	}

	public Response getFiles(Long courseId, List<PathSegment> path, FolderType type, UriInfo uriInfo, HttpServletRequest httpRequest, Request request) {
		ICourse course = CoursesWebService.loadCourse(courseId);
		if(course == null) {
			return Response.serverError().status(Status.NOT_FOUND).build();
		}
		if(!isAuthor(course, httpRequest)) {
			return Response.serverError().status(Status.UNAUTHORIZED).build();
		}

		VFSContainer container = null;
		RepositoryEntry re = null;
		switch(type) {
			case COURSE_FOLDER:
				container = course.getCourseFolderContainer();
				break;
			case SHARED_FOLDER: {
				container = null;
				String sfSoftkey = course.getCourseConfig().getSharedFolderSoftkey();
				OLATResource sharedResource = repositoryService.loadRepositoryEntryResourceBySoftKey(sfSoftkey);
				if (sharedResource != null) {
					re = repositoryService.loadByResourceKey(sharedResource.getKey());
					container = SharedFolderManager.getInstance().getNamedSharedFolder(re, true);
					CourseConfig courseConfig = course.getCourseConfig();
					if(courseConfig.isSharedFolderReadOnlyMount()) {
						container.setLocalSecurityCallback(new ReadOnlyCallback());
					}
				}
				break;
			}
		}

		if(container == null) {
			return Response.serverError().status(Status.NOT_FOUND).build();
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
			UriBuilder repoUri = baseUriBuilder.path(CourseResourceFolderWebService.class).path("files");
			if (type.equals(FolderType.SHARED_FOLDER) && re != null) {
				repoUri = baseUriBuilder.replacePath("restapi").path(SharedFolderWebService.class).path(re.getKey().toString()).path("files");
			}
			for(PathSegment pathSegment:path) {
				repoUri.path(pathSegment.getPath());
			}
			String uri = repoUri.path(item.getName()).build(courseId).toString();
			links[count++] = new LinkVO("self", uri, item.getName());
		}

		return Response.ok(links).build();
	}
	
	private boolean isAuthor(ICourse course, HttpServletRequest httpRequest) {
		UserRequest ureq = RestSecurityHelper.getUserRequest(httpRequest);
		Identity identity = ureq.getIdentity();
		Roles roles = ureq.getUserSession().getRoles();
		RepositoryEntrySecurity reSecurity = repositoryManager.isAllowed(identity, roles,
				course.getCourseEnvironment().getCourseGroupManager().getCourseEntry());
		return reSecurity.isEntryAdmin();
	}

	public enum FolderType {
		COURSE_FOLDER,
		SHARED_FOLDER
	}
}
