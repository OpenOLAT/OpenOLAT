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
 * Copyright (c) frentix GmbH<br>
 * http://www.frentix.com<br>
 * <p>
 */
package org.olat.core.util.vfs.restapi;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.Normalizer;
import java.util.Collections;
import java.util.Date;
import java.util.List;

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
import org.olat.core.util.FileUtils;
import org.olat.core.util.StringHelper;
import org.olat.core.util.WebappHelper;
import org.olat.core.util.vfs.VFSConstants;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.core.util.vfs.VFSItem;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.core.util.vfs.VFSStatus;
import org.olat.restapi.support.vo.FileVO;

public class VFSWebservice {

	private static final String VERSION  = "1.0";
	
	public static CacheControl cc = new CacheControl();
	static {
		cc.setMaxAge(-1);
	}
	
	private final VFSContainer container;

	public VFSWebservice(VFSContainer container) {
		this.container = container;
	}
	
	/**
	 * Retrieves the version of the Folder Course Node Web Service.
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
	 * This retrieves the files or a specific file in the root folder
	 * @response.representation.200.doc The list of files
	 * @response.representation.200.qname {http://www.example.com}linkVOes
	 * @param uriInfo The uri infos
	 * @param request The REST request
	 * @return 
	 */
	@GET
	@Produces({"*/*", MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, MediaType.TEXT_HTML, MediaType.APPLICATION_OCTET_STREAM})
	public Response listFiles(@Context UriInfo uriInfo, @Context Request request) {
		return get(Collections.<PathSegment>emptyList(), uriInfo, request);
	}
	
	/**
	 * This retrieves the files or a specific file in a folder
	 * @response.representation.200.doc The list of files or the file
	 * @response.representation.200.qname {http://www.example.com}linkVOes
	 * @param path the path to the folder
	 * @param uriInfo The uri infos
	 * @param request The REST request
	 * @return 
	 */
	@GET
	@Path("{path:.*}")
	@Produces({"*/*", MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, MediaType.TEXT_HTML, MediaType.APPLICATION_OCTET_STREAM})
	public Response listFiles(@PathParam("path") List<PathSegment> path, @Context UriInfo uriInfo, @Context Request request) {
		return get(path, uriInfo, request);
	}
	
	/**
	 * Upload a file to the root folder or create a new folder. One of the two sets
	 * of parameters must be set: foldername to create
	 * @response.representation.200.doc The link to the created file
	 * @response.representation.200.qname {http://www.example.com}linkVO
	 * @param foldername The name of the new folder (optional)
	 * @param filename The name of the file (optional)
	 * @param file The content of the file (optional)
	 * @param uriInfo The uri infos
	 * @return The link to the created file
	 */
	@POST
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	@Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
	public Response postFileToRoot(@FormParam("foldername") String foldername, @FormParam("filename") String filename,
			@FormParam("file") InputStream file, @Context UriInfo uriInfo) {
		return putFile(foldername, filename, file, uriInfo, Collections.<PathSegment>emptyList());
	}
	
	/**
	 * Upload a file to the root folder or create a new folder. One of the two sets
	 * of parameters must be set: foldername to create
	 * @response.representation.200.doc The link to the created file
	 * @response.representation.200.qname {http://www.example.com}linkVO
	 * @param foldername The name of the new folder (optional)
	 * @param filename The name of the file (optional)
	 * @param file The content of the file (encoded with Base64)
	 * @param uriInfo The uri infos
	 * @return The link to the created file
	 */
	@POST
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	@Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
	public Response postFile64ToRoot(@FormParam("foldername") String foldername, @FormParam("filename") String filename,
			@FormParam("file") String file, @Context UriInfo uriInfo) {
		byte[] fileAsBytes = Base64.decodeBase64(file);
		InputStream in = new ByteArrayInputStream(fileAsBytes);
		return putFile(foldername, filename, in, uriInfo, Collections.<PathSegment>emptyList());
	}
	
	/**
	 * Upload a file to the specified folder or create a new folder
	 * @response.representation.200.doc The link to the created file
	 * @response.representation.200.qname {http://www.example.com}linkVO
	 * @param foldername The name of the new folder (optional)
	 * @param filename The name of the file (optional)
	 * @param file The content of the file (optional)
	 * @param uriInfo The uri infos
	 * @param path The path to the folder
	 * @return The link to the created file
	 */
	@POST
	@Path("{path:.*}")
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	@Produces({"*/*", MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
	public Response postFileToFolder(@FormParam("foldername") String foldername, @FormParam("filename") String filename,
			@FormParam("file") InputStream file, @Context UriInfo uriInfo, @PathParam("path") List<PathSegment> path) {
		return putFile(foldername, filename, file, uriInfo, path);
	}
	
	/**
	 * Upload a file to the specified folder or create a new folder
	 * @response.representation.200.doc The link to the created file
	 * @response.representation.200.qname {http://www.example.com}linkVO
	 * @param foldername The name of the new folder (optional)
	 * @param filename The name of the file (optional)
	 * @param file The content of the file (encoded with Base64)
	 * @param uriInfo The uri infos
	 * @param path The path to the folder
	 * @return The link to the created file
	 */
	@POST
	@Path("{path:.*}")
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	@Produces({"*/*", MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
	public Response postFile64ToFolder(@FormParam("foldername") String foldername, @FormParam("filename") String filename,
			@FormParam("file") String file, @Context UriInfo uriInfo, @PathParam("path") List<PathSegment> path) {
		byte[] fileAsBytes = Base64.decodeBase64(file);
		InputStream in = new ByteArrayInputStream(fileAsBytes);
		return putFile(foldername, filename, in, uriInfo, path);
	}
	
	/**
	 * Upload a file to the root folder or create a new folder
	 * @response.representation.200.doc The link to the created file
	 * @response.representation.200.qname {http://www.example.com}linkVO
	 * @param foldername The name of the new folder (optional)
	 * @param filename The name of the file (optional)
	 * @param file The content of the file (optional)
	 * @param uriInfo The uri infos
	 * @return The link to the created file
	 */
	@PUT
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	@Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
	public Response putFileToRoot(@FormParam("foldername") String foldername, @FormParam("filename") String filename,
			@FormParam("file") InputStream file, @Context UriInfo uriInfo) {
		return putFile(foldername, filename, file, uriInfo, Collections.<PathSegment>emptyList());
	}
	
	/**
	 * Upload a file to the specified folder or create a new folder
	 * @response.representation.200.doc The link to the created file
	 * @response.representation.200.qname {http://www.example.com}linkVO
	 * @param foldername The name of the new folder (optional)
	 * @param filename The name of the file (optional)
	 * @param file The content of the file (optional)
	 * @param uriInfo The uri infos
	 * @param path The path to the folder
	 * @return The link to the created file
	 */
	@PUT
	@Path("{path:.*}")
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	@Produces({"*/*", MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
	public Response putFileToFolder(@FormParam("foldername") String foldername, @FormParam("filename") String filename,
			@FormParam("file") InputStream file, @Context UriInfo uriInfo, @PathParam("path") List<PathSegment> path) {
		return putFile(foldername, filename, file, uriInfo, path);
	}
	
	/**
	 * Create folders
	 * @response.representation.200.doc The link to the created file
	 * @response.representation.200.qname {http://www.example.com}linkVO
	 * @param uriInfo The uri infos
	 * @param path The path to the folder
	 * @return The link to the created file
	 */
	@PUT
	@Path("{path:.*}")
	@Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
	public Response putFolders(@Context UriInfo uriInfo, @PathParam("path") List<PathSegment> path) {
		return createFolders(uriInfo, path);
	}
	
	@DELETE
	@Path("{path:.*}")
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
		
		VFSContainer directory = resolveContainer(path, true);
		if(directory == null) {
			return Response.serverError().status(Status.NOT_FOUND).build();
		}
		return Response.ok(createFileVO(directory, uriInfo)).build();
	}
	
	protected Response putFile(String foldername, String filename, InputStream file, UriInfo uriInfo, List<PathSegment> path) {
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

		OutputStream out = newFile.getOutputStream(false);
		FileUtils.copy(file, out);
		FileUtils.closeSafely(out);
		FileUtils.closeSafely(file);

		return Response.ok(createFileVO(newFile, uriInfo)).build();
	}

	protected Response get(List<PathSegment> path, UriInfo uriInfo, Request request) {
		VFSItem vItem = resolveFile(path);
		if(vItem == null) {
			return Response.serverError().status(Status.NOT_FOUND).build();
		} else if (vItem instanceof VFSContainer) {
			VFSContainer directory = (VFSContainer)vItem;
			List<VFSItem> items = directory.getItems(new SystemItemFilter());
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
	
	protected VFSContainer resolveContainer(List<PathSegment> path, boolean create) {
		VFSContainer directory = container;
		boolean notFound = false;
		
		//remove trailing segment if a trailing / is used
		if(path.size() > 0 && !StringHelper.containsNonWhitespace(path.get(path.size() - 1).getPath())) {
			path = path.subList(0, path.size() -1);
		}
		
		a_a:
		for(PathSegment seg:path) {
			String segPath = seg.getPath();
			for(VFSItem item : directory.getItems(new SystemItemFilter())) {
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
		if(path.size() > 0 && !StringHelper.containsNonWhitespace(path.get(path.size() - 1).getPath())) {
			path = path.subList(0, path.size() -1);
		}
		
		a_a:
		for(PathSegment seg:path) {
			String segPath = seg.getPath();
			for(VFSItem item : directory.getItems(new SystemItemFilter())) {
				if(normalize(item.getName()).equals(segPath)) {
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
