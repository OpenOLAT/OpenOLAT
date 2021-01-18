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
package org.olat.core.commons.services.vfs.restapi;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.Normalizer;
import java.util.Collections;
import java.util.Date;
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
import javax.ws.rs.core.CacheControl;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.PathSegment;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.Logger;
import org.olat.core.logging.Tracing;
import org.olat.core.util.FileUtils;
import org.olat.core.util.StringHelper;
import org.olat.core.util.WebappHelper;
import org.olat.core.util.vfs.VFSConstants;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.core.util.vfs.VFSItem;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.core.util.vfs.VFSStatus;
import org.olat.core.util.vfs.filters.VFSSystemItemFilter;
import org.olat.restapi.support.MultipartReader;
import org.olat.restapi.support.vo.File64VO;
import org.olat.restapi.support.vo.FileMetadataVO;
import org.olat.restapi.support.vo.FileVO;
import org.olat.restapi.support.vo.LinkVO;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;

public class VFSWebservice {

	private static final String VERSION  = "1.0";
	
	private static final Logger log = Tracing.createLoggerFor(VFSWebservice.class);
	private static final int MAX_FOLDER_DEPTH = 20;
	
	private static final CacheControl cc = new CacheControl();
	static {
		cc.setMaxAge(-1);
	}
	
	private final VFSContainer container;

	public VFSWebservice(VFSContainer container) {
		this.container = container;
	}
	
	/**
	 * Retrieves the version of the Folder Course Node Web Service.
	 * 
	 * @return
	 */
	@GET
	@Path("version")
	@Operation(summary = "Retrieve version",
	description = "Retrieves the version of the Folder Course Node Web Service")
	@ApiResponse(responseCode = "200", description = "The version of this specific Web Service")
	@Produces(MediaType.TEXT_PLAIN)
	public Response getVersion() {
		return Response.ok(VERSION).build();
	}
	
	/**
	 * This retrieves the files or a specific file in the root folder.
	 * 
	 * @param uriInfo The uri infos
	 * @param request The REST request
	 * @return 
	 */
	@GET
	@Operation(summary = "Retrieve files",
	description = "This retrieves the files or a specific file in the root folder")
	@ApiResponse(responseCode = "200", description = "The list of files", content = {
			@Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = LinkVO.class))),
			@Content(mediaType = "application/xml", array = @ArraySchema(schema = @Schema(implementation = LinkVO.class)))
		} )
	@Produces({"*/*", MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, MediaType.TEXT_HTML, MediaType.APPLICATION_OCTET_STREAM})
	public Response listFiles(@Context UriInfo uriInfo, @Context Request request) {
		return get(Collections.<PathSegment>emptyList(), uriInfo, request);
	}
	
	/**
	 * This retrieves the files or a specific file in a folder.
	 * 
	 * @param path the path to the folder
	 * @param uriInfo The uri infos
	 * @param request The REST request
	 * @return 
	 */
	@GET
	@Path("{path:.*}")
	@Operation(summary = "Retrieve files",
	description = "This retrieves the files or a specific file in a folder")
	@ApiResponse(responseCode = "200", description = "The list of files or the file", content = {
			@Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = LinkVO.class))),
			@Content(mediaType = "application/xml", array = @ArraySchema(schema = @Schema(implementation = LinkVO.class)))
		} )
	@Produces({"*/*", MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, MediaType.TEXT_HTML, MediaType.APPLICATION_OCTET_STREAM})
	public Response listFiles(@PathParam("path") List<PathSegment> path, @Context UriInfo uriInfo, @Context Request request) {
		return get(path, uriInfo, request);
	}
	
	/**
	 * This retrieves some metadata of a specific file in a folder
	 * The metadata are: filename, size, date of last modification,
	 * MIME-type and file href for downloading via REST.
	 * 
	 * @param path the path to the file
	 * @param uriInfo The uri infos
	 * @return 
	 */
	@GET
	@Path("metadata/{path:.*}")
	@Operation(summary = "Retrieve metadata",
	description = "This retrieves some metadata of a specific file in a folder.\n" + 
			" The metadata are: filename, size, date of last modification, MIME-type and file href for downloading via REST")
	@ApiResponse(responseCode = "200", description = "The list of files or the file", content = {
			@Content(mediaType = "application/json", schema = @Schema(implementation = FileMetadataVO.class)),
			@Content(mediaType = "application/xml", schema = @Schema(implementation = FileMetadataVO.class))
		} )
	@Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
	public Response getFileMetadata(@PathParam("path") @Parameter(description = "the path to the file List<PathSegment>") List<PathSegment> path, @Context UriInfo uriInfo) {
		return getFMetadata(path, uriInfo);
	}
	
	/**
	 * Upload a file to the root folder or create a new folder. One of the two sets
	 * of parameters must be set: foldername to create.
	 * 
	 * @param foldername The name of the new folder (optional)
	 * @param filename The name of the file (optional)
	 * @param file The content of the file (optional)
	 * @param uriInfo The uri infos
	 * @return The link to the created file
	 */
	@POST
	@Operation(summary = "Upload a file",
	description = "Upload a file to the root folder or create a new folder. One of the two sets\n" + 
			" of parameters must be set: foldername to create")
	@ApiResponse(responseCode = "200", description = "The link to the created file", content = {
			@Content(mediaType = "application/json", schema = @Schema(implementation = LinkVO.class)),
			@Content(mediaType = "application/xml", schema = @Schema(implementation = LinkVO.class))
		} )
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	@Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
	public Response postFileToRoot(@Context UriInfo uriInfo, @Context HttpServletRequest request) {
		return addFileToFolder(uriInfo, Collections.<PathSegment>emptyList(), request);
	}
	
	/**
	 * Upload a file to the root folder or create a new folder. One of the two sets
	 * of parameters must be set: foldername to create.
	 * 
	 * @param foldername The name of the new folder (optional)
	 * @param filename The name of the file (optional)
	 * @param file The content of the file (encoded with Base64)
	 * @param uriInfo The uri infos
	 * @return The link to the created file
	 */
	@POST
	@Operation(summary = "Upload a file",
	description = "Upload a file to the root folder or create a new folder. One of the two sets\n" + 
			"of parameters must be set: foldername to create")
	@ApiResponse(responseCode = "200", description = "The link to the created file", content = {
			@Content(mediaType = "application/json", schema = @Schema(implementation = LinkVO.class)),
			@Content(mediaType = "application/xml", schema = @Schema(implementation = LinkVO.class))
		} )
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	@Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
	public Response postFile64ToRoot(@FormParam("foldername")String foldername, @FormParam("filename") String filename,
			@FormParam("file")String file, @Context UriInfo uriInfo) {
		byte[] fileAsBytes = Base64.decodeBase64(file);
		try(InputStream in = new ByteArrayInputStream(fileAsBytes)) {
			return putFile(foldername, filename, in, uriInfo, Collections.<PathSegment>emptyList());
		} catch (VFSDepthException e) {
			return Response.serverError().status(Status.NOT_ACCEPTABLE).build();
		} catch (IOException e) {
			return Response.serverError().status(Status.INTERNAL_SERVER_ERROR).build();
		}
	}
	
	/**
	 * Upload a file to the specified folder or create a new folder.
	 * 
	 * @param foldername The name of the new folder (optional)
	 * @param filename The name of the file (optional)
	 * @param file The content of the file (optional)
	 * @param uriInfo The uri infos
	 * @param path The path to the folder
	 * @return The link to the created file
	 */
	@POST
	@Path("{path:.*}")
	@Operation(summary = "Upload a file",
	description = "Upload a file to the specified folder or create a new folder")
	@ApiResponse(responseCode = "200", description = "The link to the created file", content = {
			@Content(mediaType = "application/json", schema = @Schema(implementation = LinkVO.class)),
			@Content(mediaType = "application/xml", schema = @Schema(implementation = LinkVO.class))
		} )
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	@Produces({"*/*", MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
	public Response postFileToFolder(@Context UriInfo uriInfo, @PathParam("path") List<PathSegment> path,
			@Context HttpServletRequest request) {
			return addFileToFolder(uriInfo, path, request);
	}
	
	/**
	 * Upload a file to the specified folder or create a new folder.
	 * 
	 * @param foldername The name of the new folder (optional)
	 * @param filename The name of the file (optional)
	 * @param file The content of the file (encoded with Base64)
	 * @param uriInfo The uri infos
	 * @param path The path to the folder
	 * @return The link to the created file
	 */
	@POST
	@Path("{path:.*}")
	@Operation(summary = "Upload a file",
	description = "Upload a file to the specified folder or create a new folder")
	@ApiResponse(responseCode = "200", description = "The link to the created file", content = {
			@Content(mediaType = "application/json", schema = @Schema(implementation = LinkVO.class)),
			@Content(mediaType = "application/xml", schema = @Schema(implementation = LinkVO.class))
		} )
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	@Produces({"*/*", MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
	public Response postFile64ToFolder(@FormParam("foldername") String foldername, @FormParam("filename") String filename,
			@FormParam("file") String file, @Context UriInfo uriInfo, @PathParam("path") List<PathSegment> path) {
		byte[] fileAsBytes = Base64.decodeBase64(file);
		try(InputStream in = new ByteArrayInputStream(fileAsBytes)) {
			return putFile(foldername, filename, in, uriInfo, path);
		} catch (VFSDepthException e) {
			return Response.serverError().status(Status.NOT_ACCEPTABLE).build();
		} catch (IOException e) {
			return Response.serverError().status(Status.INTERNAL_SERVER_ERROR).build();
		}
	}
	
	/**
	 * Upload a file to the root folder or create a new folder.
	 * 
	 * @param foldername The name of the new folder (optional)
	 * @param filename The name of the file (optional)
	 * @param file The content of the file (optional)
	 * @param uriInfo The uri infos
	 * @return The link to the created file
	 */
	@PUT
	@Operation(summary = "Upload a file",
	description = "Upload a file to the specified folder or create a new folder")
	@ApiResponse(responseCode = "200", description = "The link to the created file", content = {
			@Content(mediaType = "application/json", schema = @Schema(implementation = LinkVO.class)),
			@Content(mediaType = "application/xml", schema = @Schema(implementation = LinkVO.class))
		} )
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	@Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
	public Response putFileToRoot(@Context UriInfo uriInfo, @Context HttpServletRequest request) {
		return addFileToFolder(uriInfo, Collections.<PathSegment>emptyList(), request);
	}
	
	/**
	 * Upload a file to the root folder or create a new folder.
	 * 
	 * @param foldername The name of the new folder (optional)
	 * @param filename The name of the file (optional)
	 * @param file The content of the file (optional)
	 * @param uriInfo The uri infos
	 * @return The link to the created file
	 */
	@PUT
	@Operation(summary = "Upload a file",
	description = "Upload a file to the specified folder or create a new folder")
	@ApiResponse(responseCode = "200", description = "The link to the created file", content = {
			@Content(mediaType = "application/json", schema = @Schema(implementation = LinkVO.class)),
			@Content(mediaType = "application/xml", schema = @Schema(implementation = LinkVO.class))
		} )
	@Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
	@Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
	public Response putFile64VOToRoot(File64VO file, @Context UriInfo uriInfo) {
		byte[] fileAsBytes = Base64.decodeBase64(file.getFile());
		try(InputStream in = new ByteArrayInputStream(fileAsBytes)) {
			return putFile(null, file.getFilename(), in, uriInfo, Collections.<PathSegment>emptyList());
		} catch (VFSDepthException e) {
			return Response.serverError().status(Status.NOT_ACCEPTABLE).build();
		} catch (IOException e) {
			return Response.serverError().status(Status.INTERNAL_SERVER_ERROR).build();
		}
	}
	
	/**
	 * Upload a file to the specified folder or create a new folder.
	 * 
	 * @param foldername The name of the new folder (optional)
	 * @param filename The name of the file (optional)
	 * @param file The content of the file (optional)
	 * @param uriInfo The uri infos
	 * @param path The path to the folder
	 * @return The link to the created file
	 */
	@PUT
	@Operation(summary = "Upload a file",
	description = "Upload a file to the specified folder or create a new folder")
	@ApiResponse(responseCode = "200", description = "The link to the created file", content = {
			@Content(mediaType = "application/json", schema = @Schema(implementation = LinkVO.class)),
			@Content(mediaType = "application/xml", schema = @Schema(implementation = LinkVO.class))
		} )
	@Path("{path:.*}")
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	@Produces({"*/*", MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
	public Response putFileToFolder(@Context UriInfo uriInfo, @PathParam("path") List<PathSegment> path,
			@Context HttpServletRequest request) {
		return addFileToFolder(uriInfo, path, request);
	}
	
	private Response addFileToFolder(UriInfo uriInfo, List<PathSegment> path,
			 HttpServletRequest request) {
		InputStream in = null;
		MultipartReader partsReader = null;
		try {
			partsReader = new MultipartReader(request);
			File tmpFile = partsReader.getFile();
			if(tmpFile != null) {
				in = new FileInputStream(tmpFile);
			}
			String filename = partsReader.getValue("filename");
			String foldername = partsReader.getValue("foldername");
			return putFile(foldername, filename, in, uriInfo, path);
		} catch (FileNotFoundException e) {
			log.error("", e);
			return Response.serverError().status(Status.INTERNAL_SERVER_ERROR).build();
		}  catch (VFSDepthException e) {
			log.error("", e);
			return Response.serverError().status(Status.NOT_ACCEPTABLE).build();
		} finally {
			MultipartReader.closeQuietly(partsReader);
			IOUtils.closeQuietly(in);
		}
	}
	
	/**
	 * Upload a file to the specified folder or create a new folder.
	 * 
	 * @param foldername The name of the new folder (optional)
	 * @param filename The name of the file (optional)
	 * @param file The content of the file (optional)
	 * @param uriInfo The uri infos
	 * @param path The path to the folder
	 * @return The link to the created file
	 */
	@PUT
	@Path("{path:.*}")
	@Operation(summary = "Upload a file",
	description = "Upload a file to the specified folder or create a new folder")
	@ApiResponse(responseCode = "200", description = "The link to the created file", content = {
			@Content(mediaType = "application/json", schema = @Schema(implementation = LinkVO.class)),
			@Content(mediaType = "application/xml", schema = @Schema(implementation = LinkVO.class))
		} )
	@Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
	@Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
	public Response putFile64ToFolder(File64VO file, @Context UriInfo uriInfo, @PathParam("path") List<PathSegment> path) {
		byte[] fileAsBytes = Base64.decodeBase64(file.getFile());
		try(InputStream in = new ByteArrayInputStream(fileAsBytes)) {
			return putFile(null, file.getFilename(), in, uriInfo, path);
		} catch (VFSDepthException e) {
			return Response.serverError().status(Status.NOT_ACCEPTABLE).build();
		} catch(IOException e) {
			return Response.serverError().status(Status.INTERNAL_SERVER_ERROR).build();
		}
	}
	
	/**
	 * Create folders.
	 * 
	 * @param uriInfo The uri infos
	 * @param path The path to the folder
	 * @return The link to the created file
	 */
	@PUT
	@Path("{path:.*}")
	@Operation(summary = "Create folders",
	description = "Create fodlers")
	@ApiResponse(responseCode = "200", description = "The link to the created folder", content = {
			@Content(mediaType = "application/json", schema = @Schema(implementation = LinkVO.class)),
			@Content(mediaType = "application/xml", schema = @Schema(implementation = LinkVO.class))
		} )
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	@Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
	public Response putFolders(@Context UriInfo uriInfo, @PathParam("path") List<PathSegment> path) {
		return createFolders(uriInfo, path);
	}
	
	@DELETE
	@Path("{path:.*}")
	@Operation(summary = "Delete folders",
	description = "Delete")
	@ApiResponse(responseCode = "200", description = "Ok", content = {
			@Content(mediaType = "application/json", schema = @Schema(implementation = LinkVO.class)),
			@Content(mediaType = "application/xml", schema = @Schema(implementation = LinkVO.class))
		} )
	@Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
	public Response deleteItem(@PathParam("path") List<PathSegment> path) {
		if(container.getLocalSecurityCallback() != null && !container.getLocalSecurityCallback().canDelete()) {
			return Response.serverError().status(Status.UNAUTHORIZED).build();
		}
		
		VFSItem item = resolveFile(path);
		if(item == null) {
			return Response.serverError().status(Status.NOT_FOUND).build();
		}
		
		VFSStatus status = item.delete();
		if(status == VFSConstants.YES) {
			return Response.ok().build();
		}
		//need something better
		return Response.ok().build();
	}
	
	protected Response createFolders(UriInfo uriInfo, List<PathSegment> path) {
		if(container.getLocalSecurityCallback() != null && !container.getLocalSecurityCallback().canWrite()) {
			return Response.serverError().status(Status.UNAUTHORIZED).build();
		}
		if(path.size() >= MAX_FOLDER_DEPTH) {
			return Response.serverError().status(Status.NOT_ACCEPTABLE).build();
		}
		
		try {
			VFSContainer directory = resolveContainer(path, true);
			if(directory == null) {
				return Response.serverError().status(Status.NOT_FOUND).build();
			}
			return Response.ok(createFileVO(directory, uriInfo)).build();
		} catch (VFSDepthException e) {
			return Response.serverError().status(Status.NOT_ACCEPTABLE).build();
		}
	}
	
	private Response putFile(String foldername, String filename, InputStream file, UriInfo uriInfo, List<PathSegment> path)
	throws VFSDepthException {
		if(container.getLocalSecurityCallback() != null && !container.getLocalSecurityCallback().canWrite()) {
			return Response.serverError().status(Status.UNAUTHORIZED).build();
		}
		
		VFSContainer directory = resolveContainer(path, true);
		if(directory == null) {
			return Response.serverError().status(Status.NOT_FOUND).build();
		}
		
		if(filename == null && file == null) {
			//only create folders
			if(foldername != null) {
				VFSItem newFolder = directory.resolve(foldername);
				if(newFolder instanceof VFSContainer) {
					return Response.ok(createFileVO(newFolder, uriInfo)).build();
				} else if (newFolder == null) {
					newFolder = directory.createChildContainer(foldername);
					if(newFolder != null) {
						return Response.ok(createFileVO(newFolder, uriInfo)).build();
					}
				}
			}
			return Response.serverError().status(Status.CONFLICT).build();
		}
		
		VFSItem newItem = directory.resolve(filename);
		VFSLeaf newFile;
		if(newItem == null) {
			newFile = directory.createChildLeaf(filename);
		} else if (newItem instanceof VFSLeaf) {
			newFile = (VFSLeaf)newItem;
		} else {
			return Response.serverError().status(Status.FORBIDDEN).build();
		}

		try(OutputStream out = newFile.getOutputStream(false)) {
			FileUtils.cpio(file, out, "Copy");
			FileUtils.closeSafely(file);
			return Response.ok(createFileVO(newFile, uriInfo)).build();
		} catch (IOException e) {
			return Response.serverError().status(Status.INTERNAL_SERVER_ERROR).build();
		}
	}

	protected Response get(List<PathSegment> path, UriInfo uriInfo, Request request) {
		VFSItem vItem = resolveFile(path);
		if(vItem == null) {
			return Response.serverError().status(Status.NOT_FOUND).build();
		} else if (vItem instanceof VFSContainer) {
			VFSContainer directory = (VFSContainer)vItem;
			List<VFSItem> items = directory.getItems(new VFSSystemItemFilter());
			int count=0;
			FileVO[] links = new FileVO[items.size()];
			for(VFSItem item:items) {
				UriBuilder builder = uriInfo.getAbsolutePathBuilder();
				String uri = builder.path(normalize(item.getName())).build().toString();
				if(item instanceof VFSLeaf) {
					links[count++] = new FileVO("self", uri, item.getName(), ((VFSLeaf)item).getSize());
				} else {
					links[count++] = new FileVO("self", uri, item.getName());
				}
			}
			
			return Response.ok(links).build();
		} else if (vItem instanceof VFSLeaf) {
			VFSLeaf leaf = (VFSLeaf)vItem;
			Date lastModified = new Date(leaf.getLastModified());
			Response.ResponseBuilder response = request.evaluatePreconditions(lastModified);
			if(response == null) {
				String mimeType = WebappHelper.getMimeType(leaf.getName());
				if (mimeType == null) {
					mimeType = MediaType.APPLICATION_OCTET_STREAM;
				}
				response = Response.ok(leaf.getInputStream(), mimeType).lastModified(lastModified).cacheControl(cc);
			}
			return response.build();
			
		}
		return Response.serverError().status(Status.BAD_REQUEST).build();
	}
	
	protected Response getFMetadata(List<PathSegment> path, UriInfo uriInfo) {
		VFSItem vItem = resolveFile(path);
		if(vItem == null) {
			return Response.serverError().status(Status.NOT_FOUND).build();
		} else if (vItem instanceof VFSContainer) {
			return Response.serverError().status(Status.NOT_ACCEPTABLE).build();
		} else if (vItem instanceof VFSLeaf) {
			VFSLeaf leaf = (VFSLeaf)vItem;
			UriBuilder builder = uriInfo.getAbsolutePathBuilder();
			String uri = builder.build().toString();
			String[] uriArray = uri.split("metadata/");
			uri = uriArray[0] + uriArray[1];
			FileMetadataVO metaVo = new FileMetadataVO(uri, leaf);
			return Response.ok(metaVo).build();
		}
		return Response.serverError().status(Status.BAD_REQUEST).build();
	}
	
	protected VFSContainer resolveContainer(List<PathSegment> path, boolean create)
	throws VFSDepthException {
		VFSContainer directory = container;
		boolean notFound = false;
		
		//remove trailing segment if a trailing / is used
		if(!path.isEmpty() && !StringHelper.containsNonWhitespace(path.get(path.size() - 1).getPath())) {
			path = path.subList(0, path.size() -1);
		}
		
		if(create && path.size() >= MAX_FOLDER_DEPTH) {
			throw new VFSDepthException();
		}
		
		a_a:
		for(PathSegment seg:path) {
			String segPath = seg.getPath();
			for(VFSItem item : directory.getItems(new VFSSystemItemFilter())) {
				if(item instanceof VFSLeaf) {
					//
				} else if (item instanceof VFSContainer && normalize(item.getName()).equals(segPath)) {
					directory = (VFSContainer)item;
					continue a_a;
				}
			}
			if(create) {
				directory = directory.createChildContainer(segPath);
			} else if(path.get(path.size() - 1) == seg) {
				break a_a;
			} else {
				notFound = true;
			}
		}
			
		if(notFound) {
			return null;
		}
		return directory;
	}
	
	protected VFSItem resolveFile(List<PathSegment> path) {
		VFSContainer directory = container;
		VFSItem resolvedItem = directory;
		boolean notFound = false;
		
		//remove trailing segment if a trailing / is used
		if(!path.isEmpty() && !StringHelper.containsNonWhitespace(path.get(path.size() - 1).getPath())) {
			path = path.subList(0, path.size() -1);
		}
		
		a_a:
		for(PathSegment seg:path) {
			String segPath = seg.getPath();
			for(VFSItem item : directory.getItems(new VFSSystemItemFilter())) {
				if(item.getName().equals(segPath) || normalize(item.getName()).equals(segPath)) {
					if(item instanceof VFSLeaf) {
						if(path.get(path.size() - 1) == seg) {
							resolvedItem = item;
							break a_a;
						}
					} else if (item instanceof VFSContainer) {
						resolvedItem = directory = (VFSContainer)item;
						continue a_a;
					}
				}
			}
			notFound = true;
		}
			
		if(notFound) {
			return null;
		}
		return resolvedItem;
	}
	
	public static FileVO createFileVO(VFSItem item, UriInfo uriInfo) {
		UriBuilder builder = uriInfo.getAbsolutePathBuilder();
		String uri = builder.path(normalize(item.getName())).build().toString();
		FileVO link = new FileVO("self", uri, item.getName());
		if(item instanceof VFSLeaf) {
			link.setSize(((VFSLeaf)item).getSize());
		}
		return link;
	}
	
	public static String normalize(String segment) {
		segment = segment.replace(" ", "_");
		segment = Normalizer.normalize(segment, Normalizer.Form.NFD).replaceAll("[^\\p{ASCII}]", "");
		return segment;
	}
}
