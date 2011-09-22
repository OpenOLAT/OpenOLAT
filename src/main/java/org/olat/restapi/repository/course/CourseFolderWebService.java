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

package org.olat.restapi.repository.course;

import static org.olat.restapi.security.RestSecurityHelper.isAuthor;

import java.io.InputStream;
import java.io.OutputStream;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
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

import org.olat.core.commons.modules.bc.vfs.OlatNamedContainerImpl;
import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;
import org.olat.core.util.FileUtils;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.course.CourseFactory;
import org.olat.course.ICourse;
import org.olat.course.nodes.BCCourseNode;
import org.olat.course.nodes.CourseNode;

/**
 * 
 * Description:<br>
 * This handles the folder building blocks within a course.
 * 
 * <P>
 * Initial Date:  22 apr. 2010 <br>
 * @author srosse, stephane.rosse@frentix.com
 */
@Path("repo/courses/{courseId}/elements/folders/{nodeId}")
public class CourseFolderWebService {
	
	private static final OLog log = Tracing.createLoggerFor(CourseFolderWebService.class);

	private static final String VERSION  = "1.0";
	
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
	 * This attaches the uploaded file(s) to the supplied folder id.
   * @response.representation.mediaType multipart/form-data
   * @response.representation.doc The file
   * @response.representation.200.doc The file is correctly saved
	 * @response.representation.401.doc The roles of the authenticated user are not sufficient
   * @response.representation.404.doc The course or course node not found
   * @response.representation.406.doc The course node is not acceptable to copy a file
	 * @param courseId The course resourceable's id
	 * @param nodeId The id for the folder that will contain the file(s)
	 * @param filename The filename
	 * @param file The file resource to upload
	 * @param The HTTP request
	 * @return 
	 */
	@POST
	@Path("files")
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	public Response attachFileToFolderPost(@PathParam("courseId") Long courseId, @PathParam("nodeId") String nodeId,
			@FormParam("filename") String filename, @FormParam("file") InputStream file, @Context HttpServletRequest request) {
		return attachFileToFolder(courseId, nodeId, filename, file, request);
	}
	
	/**
	 * This attaches the uploaded file(s) to the supplied folder id.
   * @response.representation.mediaType multipart/form-data
   * @response.representation.doc The file
   * @response.representation.200.doc The file is correctly saved
	 * @response.representation.401.doc The roles of the authenticated user are not sufficient
   * @response.representation.404.doc The course or course node not found
   * @response.representation.406.doc The course node is not acceptable to copy a file
	 * @param courseId The course resourceable's id
	 * @param nodeId The id for the folder that will contain the file(s)
	 * @param filename The filename
	 * @param file The file resource to upload
	 * @param request The HTTP request
	 * @return 
	 */
	@PUT
	@Path("files")
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	public Response attachFileToFolder(@PathParam("courseId") Long courseId, @PathParam("nodeId") String nodeId,
			@FormParam("filename") String filename, @FormParam("file") InputStream file, @Context HttpServletRequest request) {
		if(!isAuthor(request)) {
			return Response.serverError().status(Status.UNAUTHORIZED).build();
		}
		
		ICourse course = loadCourse(courseId);
		if(course == null) {
			return Response.serverError().status(Status.NOT_FOUND).build();
		}
		CourseNode node =course.getEditorTreeModel().getCourseNode(nodeId);
		if(node == null) {
			return Response.serverError().status(Status.NOT_FOUND).build();
		} else if(!(node instanceof BCCourseNode)) {
			return Response.serverError().status(Status.NOT_ACCEPTABLE).build();
		}
		
		BCCourseNode bcNode = (BCCourseNode)node;
		OlatNamedContainerImpl container = BCCourseNode.getNodeFolderContainer(bcNode, course.getCourseEnvironment());
		if (container.resolve(filename) != null) {
			//already exist
		} else {
			VFSLeaf newFile = container.createChildLeaf(filename);
			OutputStream out = newFile.getOutputStream(false);
			FileUtils.copy(file, out);
			FileUtils.closeSafely(out);
			FileUtils.closeSafely(file);
		}
		return Response.ok().build();
	}
	
	private ICourse loadCourse(Long courseId) {
		try {
			ICourse course = CourseFactory.loadCourse(courseId);
			return course;
		} catch(Exception ex) {
			log.error("cannot load course with id: " + courseId, ex);
			return null;
		}
	}
}
