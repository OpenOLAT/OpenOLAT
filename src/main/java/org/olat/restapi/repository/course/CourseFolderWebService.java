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

import static org.olat.restapi.security.RestSecurityHelper.getUserRequest;
import static org.olat.restapi.security.RestSecurityHelper.isAuthor;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.olat.core.commons.modules.bc.vfs.OlatNamedContainerImpl;
import org.olat.core.gui.UserRequest;
import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;
import org.olat.core.util.vfs.callbacks.VFSSecurityCallback;
import org.olat.core.util.vfs.restapi.VFSWebservice;
import org.olat.course.CourseFactory;
import org.olat.course.ICourse;
import org.olat.course.nodes.BCCourseNode;
import org.olat.course.nodes.CourseNode;
import org.olat.course.nodes.bc.FolderNodeCallback;
import org.olat.course.run.userview.NodeEvaluation;
import org.olat.course.run.userview.TreeEvaluation;
import org.olat.course.run.userview.UserCourseEnvironmentImpl;


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
	 * Return the FX implementation to manage a folder.
	 * @param courseId
	 * @param nodeId
	 * @param request
	 * @return
	 */
	@Path("files")
	public VFSWebservice getVFSWebService(@PathParam("courseId") Long courseId, @PathParam("nodeId") String nodeId, @Context HttpServletRequest request) {
		if(!isAuthor(request)) {
			throw new WebApplicationException(Response.serverError().status(Status.UNAUTHORIZED).build());
		}
		
		ICourse course = loadCourse(courseId);
		if(course == null) {
			throw new WebApplicationException( Response.serverError().status(Status.NOT_FOUND).build());
		}
		CourseNode node =course.getEditorTreeModel().getCourseNode(nodeId);
		if(node == null) {
			throw new WebApplicationException( Response.serverError().status(Status.NOT_FOUND).build());
		} else if(!(node instanceof BCCourseNode)) {
			throw new WebApplicationException(Response.serverError().status(Status.NOT_ACCEPTABLE).build());
		}
		
		BCCourseNode bcNode = (BCCourseNode)node;
		UserRequest ureq = getUserRequest(request);
		boolean isOlatAdmin = ureq.getUserSession().getRoles().isOLATAdmin();
		boolean isGuestOnly = ureq.getUserSession().getRoles().isGuestOnly();
		
		UserCourseEnvironmentImpl uce = new UserCourseEnvironmentImpl(ureq.getUserSession().getIdentityEnvironment(), course.getCourseEnvironment());
		NodeEvaluation ne = bcNode.eval(uce.getConditionInterpreter(), new TreeEvaluation());

		OlatNamedContainerImpl container = BCCourseNode.getNodeFolderContainer(bcNode, course.getCourseEnvironment());
		VFSSecurityCallback secCallback = new FolderNodeCallback(container.getRelPath(), ne, isOlatAdmin, isGuestOnly, null);
		container.setLocalSecurityCallback(secCallback);
		
		return new VFSWebservice(container);
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
