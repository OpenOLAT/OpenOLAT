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
package org.olat.course.nodes.bc;

import static org.olat.restapi.security.RestSecurityHelper.getUserRequest;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
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
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.olat.core.CoreSpringFactory;
import org.olat.core.commons.modules.bc.vfs.OlatNamedContainerImpl;
import org.olat.core.commons.services.notifications.NotificationsManager;
import org.olat.core.commons.services.notifications.Subscriber;
import org.olat.core.gui.UserRequest;
import org.olat.core.id.IdentityEnvironment;
import org.olat.core.util.StringHelper;
import org.olat.core.util.nodes.INode;
import org.olat.core.util.tree.Visitor;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.core.util.vfs.callbacks.VFSSecurityCallback;
import org.olat.core.util.vfs.restapi.VFSWebservice;
import org.olat.course.ICourse;
import org.olat.course.condition.Condition;
import org.olat.course.nodes.BCCourseNode;
import org.olat.course.nodes.CourseNode;
import org.olat.course.run.environment.CourseEnvironment;
import org.olat.course.run.userview.CourseTreeVisitor;
import org.olat.course.run.userview.NodeEvaluation;
import org.olat.course.run.userview.TreeEvaluation;
import org.olat.course.run.userview.UserCourseEnvironmentImpl;
import org.olat.course.run.userview.VisibleTreeFilter;
import org.olat.modules.ModuleConfiguration;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryManager;
import org.olat.repository.model.RepositoryEntrySecurity;
import org.olat.resource.accesscontrol.ACService;
import org.olat.resource.accesscontrol.AccessResult;
import org.olat.restapi.repository.course.AbstractCourseNodeWebService;
import org.olat.restapi.repository.course.CourseWebService;
import org.olat.restapi.repository.course.CoursesWebService;
import org.olat.restapi.support.vo.FolderVO;
import org.olat.restapi.support.vo.FolderVOes;
import org.springframework.stereotype.Component;

/**
 * 
 * Description:<br>
 * 
 * <P>
 * Initial Date:  6 févr. 2012 <br>
 *
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
@Component
@Path("repo/courses/{courseId}/elements/folder")
public class BCWebService extends AbstractCourseNodeWebService {
	
	
	/**
	 * Retrieves metadata of the course node
	 * @response.representation.200.qname {http://www.example.com}folderVOes
	 * @response.representation.200.mediaType application/xml, application/json
	 * @response.representation.200.doc The course node metadatas
	 * @response.representation.200.example {@link org.olat.restapi.support.vo.Examples#SAMPLE_FOLDERVOes}
	 * @response.representation.401.doc The roles of the authenticated user are not sufficient
	 * @response.representation.404.doc The course or parentNode not found
	 * @param courseId The course resourceable's id
	 * @param nodeId The node's id
	 * @param httpRequest The HTTP request
	 * @return The persisted structure element (fully populated)
	 */
	@GET
	@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	public Response getFolders(@PathParam("courseId") Long courseId, @Context HttpServletRequest httpRequest) {
		final ICourse course = CoursesWebService.loadCourse(courseId);
		if(course == null) {
			return Response.serverError().status(Status.NOT_FOUND).build();
		} else if (!CourseWebService.isCourseAccessible(course, httpRequest)) {
			return Response.serverError().status(Status.UNAUTHORIZED).build();
		}

		final UserRequest ureq = getUserRequest(httpRequest);
		
		RepositoryEntry entry = RepositoryManager.getInstance().lookupRepositoryEntry(course, true);
		ACService acManager = CoreSpringFactory.getImpl(ACService.class);
		AccessResult result = acManager.isAccessible(entry, ureq.getIdentity(), false);
		if(!result.isAccessible()) {
			return Response.serverError().status(Status.UNAUTHORIZED).build();
		}

		final Set<String> subscribed = new HashSet<String>();
		NotificationsManager man = NotificationsManager.getInstance();
		List<String> notiTypes = Collections.singletonList("FolderModule");
		List<Subscriber> subs = man.getSubscribers(ureq.getIdentity(), notiTypes);
		for(Subscriber sub:subs) {
			Long courseKey = sub.getPublisher().getResId();
			if(courseId.equals(courseKey)) {
				subscribed.add(sub.getPublisher().getSubidentifier());
				break;
			}
		}
		
		final List<FolderVO> folderVOs = new ArrayList<>();
		new CourseTreeVisitor(course, ureq.getUserSession().getIdentityEnvironment()).visit(new Visitor() {
			@Override
			public void visit(INode node) {
				if(node instanceof BCCourseNode) {
					BCCourseNode bcNode = (BCCourseNode)node;
					FolderVO folder = createFolderVO(ureq.getUserSession().getIdentityEnvironment(), course, bcNode, subscribed);
					folderVOs.add(folder);
				}
			}
		}, new VisibleTreeFilter());

		FolderVOes voes = new FolderVOes();
		voes.setFolders(folderVOs.toArray(new FolderVO[folderVOs.size()]));
		voes.setTotalCount(folderVOs.size());
		return Response.ok(voes).build();
	}
	
	/**
	 * This attaches a Folder Element onto a given course. The element will be
	 * inserted underneath the supplied parentNodeId.
	 * @response.representation.mediaType application/x-www-form-urlencoded
	 * @response.representation.200.qname {http://www.example.com}courseNodeVO
	 * @response.representation.200.mediaType application/xml, application/json
	 * @response.representation.200.doc The folder node metadatas
	 * @response.representation.200.example {@link org.olat.restapi.support.vo.Examples#SAMPLE_COURSENODEVO}
	 * @response.representation.401.doc The roles of the authenticated user are not sufficient
	 * @response.representation.404.doc The course or parentNode not found
	 * @param courseId The course resourceable id
	 * @param parentNodeId The node's id which will be the parent of this folder
	 * @param position The node's position relative to its sibling nodes (optional)
	 * @param shortTitle The node short title
	 * @param longTitle The node long title
	 * @param objectives The node learning objectives
	 * @param visibilityExpertRules The rules to view the node (optional)
	 * @param downloadExpertRules The rules to download files (optional)
	 * @param uploadExpertRules The rules to upload files (optional)
	 * @param request The HTTP request
	 * @return The persisted folder element (fully populated)
	 */
	@PUT
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	public Response attachFolder(@PathParam("courseId") Long courseId, @QueryParam("parentNodeId") String parentNodeId,
			@QueryParam("position") Integer position, @QueryParam("shortTitle") @DefaultValue("undefined") String shortTitle,
			@QueryParam("longTitle") @DefaultValue("undefined") String longTitle, @QueryParam("objectives") @DefaultValue("undefined") String objectives,
			@QueryParam("visibilityExpertRules") String visibilityExpertRules, @QueryParam("downloadExpertRules") String downloadExpertRules,
			@QueryParam("uploadExpertRules") String uploadExpertRules, @Context HttpServletRequest request) {
		
		FolderCustomConfig config = new FolderCustomConfig(downloadExpertRules, uploadExpertRules);
		return attach(courseId, parentNodeId, "bc", position, shortTitle, longTitle, objectives, visibilityExpertRules, null, config, request);
	}
	
	/**
	 * This attaches a Folder Element onto a given course. The element will be
	 * inserted underneath the supplied parentNodeId.
	 * @response.representation.mediaType application/x-www-form-urlencoded
	 * @response.representation.200.qname {http://www.example.com}courseNodeVO
	 * @response.representation.200.mediaType application/xml, application/json
	 * @response.representation.200.doc The folder node metadatas
	 * @response.representation.200.example {@link org.olat.restapi.support.vo.Examples#SAMPLE_COURSENODEVO}
	 * @response.representation.401.doc The roles of the authenticated user are not sufficient
	 * @response.representation.404.doc The course or parentNode not found
	 * @param courseId The course resourceable's id
	 * @param parentNodeId The node's id which will be the parent of this folder
	 * @param position The node's position relative to its sibling nodes (optional)
	 * @param shortTitle The node short title
	 * @param longTitle The node long title
	 * @param objectives The node learning objectives
	 * @param visibilityExpertRules The rules to view the node (optional)
	 * @param downloadExpertRules The rules to download files (optional)
	 * @param uploadExpertRules The rules to upload files (optional)
	 * @param request The HTTP request
	 * @return The persisted folder element (fully populated)
	 */
	@POST
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	public Response attachFolderPost(@PathParam("courseId") Long courseId, @FormParam("parentNodeId") String parentNodeId,
			@FormParam("position") Integer position, @FormParam("shortTitle") @DefaultValue("undefined") String shortTitle,
			@FormParam("longTitle") @DefaultValue("undefined") String longTitle, @FormParam("objectives") @DefaultValue("undefined") String objectives,
			@FormParam("visibilityExpertRules") String visibilityExpertRules, @FormParam("downloadExpertRules") String downloadExpertRules,
			@FormParam("uploadExpertRules") String uploadExpertRules, @Context HttpServletRequest request) {
		return attachFolder(courseId, parentNodeId, position, shortTitle, longTitle, objectives, visibilityExpertRules, downloadExpertRules, uploadExpertRules, request);
	}
	
	/**
	 * This updates a Folder Element onto a given course.
	 * @response.representation.mediaType application/x-www-form-urlencoded
	 * @response.representation.200.qname {http://www.example.com}courseNodeVO
	 * @response.representation.200.mediaType application/xml, application/json
	 * @response.representation.200.doc The folder node metadatas
	 * @response.representation.200.example {@link org.olat.restapi.support.vo.Examples#SAMPLE_COURSENODEVO}
	 * @response.representation.401.doc The roles of the authenticated user are not sufficient
	 * @response.representation.404.doc The course or parentNode not found
	 * @param courseId The course resourceable's id
	 * @param nodeId The node's id of this folder
	 * @param shortTitle The node short title
	 * @param longTitle The node long title
	 * @param objectives The node learning objectives
	 * @param visibilityExpertRules The rules to view the node (optional)
	 * @param downloadExpertRules The rules to download files (optional)
	 * @param uploadExpertRules The rules to upload files (optional)
	 * @param request The HTTP request
	 * @return The persisted folder element (fully populated)
	 */
	@POST
	@Path("{nodeId}")
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	public Response updateFolder(@PathParam("courseId") Long courseId, @PathParam("nodeId") String nodeId,
			@FormParam("shortTitle") @DefaultValue("undefined") String shortTitle,
			@FormParam("longTitle") @DefaultValue("undefined") String longTitle, @FormParam("objectives") @DefaultValue("undefined") String objectives,
			@FormParam("visibilityExpertRules") String visibilityExpertRules, @FormParam("downloadExpertRules") String downloadExpertRules,
			@FormParam("uploadExpertRules") String uploadExpertRules, @Context HttpServletRequest request) {
		FolderCustomConfig config = new FolderCustomConfig(downloadExpertRules, uploadExpertRules);
		return update(courseId, nodeId, shortTitle, longTitle, objectives, visibilityExpertRules, null, config, request);
	}
	
	/**
	 * Retrieves metadata of the course node
	 * @response.representation.200.qname {http://www.example.com}folderVO
	 * @response.representation.200.mediaType application/xml, application/json
	 * @response.representation.200.doc The course node metadatas
	 * @response.representation.200.example {@link org.olat.restapi.support.vo.Examples#SAMPLE_FOLDERVO}
	 * @response.representation.401.doc The roles of the authenticated user are not sufficient
	 * @response.representation.404.doc The course or parentNode not found
	 * @param courseId The course resourceable's id
	 * @param nodeId The node's id
	 * @param httpRequest The HTTP request
	 * @return The persisted structure element (fully populated)
	 */
	@GET
	@Path("{nodeId}")
	@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	public Response getFolder(@PathParam("courseId") Long courseId, @PathParam("nodeId") String nodeId, @Context HttpServletRequest httpRequest) {
		ICourse course = CoursesWebService.loadCourse(courseId);
		if(course == null) {
			return Response.serverError().status(Status.NOT_FOUND).build();
		} else if (!CourseWebService.isCourseAccessible(course, httpRequest)) {
			return Response.serverError().status(Status.UNAUTHORIZED).build();
		}

		CourseNode courseNode = course.getRunStructure().getNode(nodeId);
		if(!(courseNode instanceof BCCourseNode)) {
			return Response.serverError().status(Status.NOT_FOUND).build();
		}

		UserRequest ureq = getUserRequest(httpRequest);
		boolean accessible = (new CourseTreeVisitor(course, ureq.getUserSession().getIdentityEnvironment())).isAccessible(courseNode, new VisibleTreeFilter());
		if(accessible) {
			Set<String> subscribed = new HashSet<>();
			NotificationsManager man = NotificationsManager.getInstance();
			List<String> notiTypes = Collections.singletonList("FolderModule");
			List<Subscriber> subs = man.getSubscribers(ureq.getIdentity(), notiTypes);
			for(Subscriber sub:subs) {
				Long courseKey = sub.getPublisher().getResId();
				if(courseId.equals(courseKey)) {
					subscribed.add(sub.getPublisher().getSubidentifier());
				}
			}

			FolderVO folderVo = createFolderVO(ureq.getUserSession().getIdentityEnvironment(), course, (BCCourseNode)courseNode, subscribed);
			return Response.ok(folderVo).build();
		} else {
			return Response.serverError().status(Status.UNAUTHORIZED).build();
		}
	}
	
	/**
	 * Return the FX implementation to manage a folder.
	 * @param courseId
	 * @param nodeId
	 * @param request
	 * @return
	 */
	@Path("{nodeId}/files")
	public VFSWebservice getVFSWebService(@PathParam("courseId") Long courseId, @PathParam("nodeId") String nodeId, @Context HttpServletRequest request) {
		ICourse course = CoursesWebService.loadCourse(courseId);
		if(course == null) {
			throw new WebApplicationException( Response.serverError().status(Status.NOT_FOUND).build());
		}
		
		boolean author = isAuthorEditor(course, request);
		if (!author && !CourseWebService.isCourseAccessible(course, request)) {
			throw new WebApplicationException( Response.serverError().status(Status.UNAUTHORIZED).build());
		}
		
		CourseNode node;
		if(author) {
			node = course.getEditorTreeModel().getCourseNode(nodeId);
		} else {
			node = course.getRunStructure().getNode(nodeId);
		}
		
		if(node == null) {
			throw new WebApplicationException( Response.serverError().status(Status.NOT_FOUND).build());
		} else if(!(node instanceof BCCourseNode)) {
			throw new WebApplicationException(Response.serverError().status(Status.NOT_ACCEPTABLE).build());
		}
		
		BCCourseNode bcNode = (BCCourseNode)node;
		UserRequest ureq = getUserRequest(request);
		VFSContainer container = getSecurisedNodeFolderContainer(bcNode, course.getCourseEnvironment(), ureq.getUserSession().getIdentityEnvironment());
		return new VFSWebservice(container);
	}

	public class FolderCustomConfig implements CustomConfigDelegate {
		private final String downloadExpertRules;
		private final String uploadExpertRules;
		
		public FolderCustomConfig(String downloadExpertRules, String uploadExpertRules) {
			this.downloadExpertRules = downloadExpertRules;
			this.uploadExpertRules = uploadExpertRules;
		}

		@Override
		public boolean isValid() {
			return true;
		}

		@Override
		public void configure(ICourse course, CourseNode newNode, ModuleConfiguration moduleConfig) {
			BCCourseNode bcCourseNode = (BCCourseNode)newNode;
			
			if(StringHelper.containsNonWhitespace(downloadExpertRules)) {
				Condition downloadCond = createExpertCondition("downloaders", downloadExpertRules);
				bcCourseNode.setPreConditionDownloaders(downloadCond);
			}

			if(StringHelper.containsNonWhitespace(uploadExpertRules)) {
				Condition uploadCond = createExpertCondition("uploaders", uploadExpertRules);
				bcCourseNode.setPreConditionUploaders(uploadCond);
			}
		}	
	}
	
	public static FolderVO createFolderVO(IdentityEnvironment ienv, ICourse course, BCCourseNode bcNode, Collection<String> subscribed) {
		OlatNamedContainerImpl container = getSecurisedNodeFolderContainer(bcNode, course.getCourseEnvironment(), ienv);
		VFSSecurityCallback secCallback = container.getLocalSecurityCallback();
		
		FolderVO folderVo = new FolderVO();
		folderVo.setName(course.getCourseTitle());
		folderVo.setDetailsName(bcNode.getShortTitle());
		if(subscribed != null && subscribed.contains(bcNode.getIdent())) {
			folderVo.setSubscribed(true);
		} else {
			folderVo.setSubscribed(false);
		}
		folderVo.setCourseKey(course.getResourceableId());
		folderVo.setCourseNodeId(bcNode.getIdent());
		folderVo.setWrite(secCallback.canWrite());
		folderVo.setRead(secCallback.canRead());
		folderVo.setDelete(secCallback.canDelete());
		folderVo.setList(secCallback.canList());
		return folderVo;
	}
	
	public static OlatNamedContainerImpl getSecurisedNodeFolderContainer(BCCourseNode node, CourseEnvironment courseEnv, IdentityEnvironment ienv) {
		RepositoryEntry entry = courseEnv.getCourseGroupManager().getCourseEntry();
		RepositoryEntrySecurity reSecurity = CoreSpringFactory.getImpl(RepositoryManager.class).isAllowed(ienv.getIdentity(), ienv.getRoles(), entry);
		
		UserCourseEnvironmentImpl uce = new UserCourseEnvironmentImpl(ienv, courseEnv);
		NodeEvaluation ne = node.eval(uce.getConditionInterpreter(), new TreeEvaluation(), new VisibleTreeFilter());

		OlatNamedContainerImpl container = BCCourseNode.getNodeFolderContainer(node, courseEnv);
		VFSSecurityCallback secCallback = new FolderNodeCallback(container.getRelPath(), ne, reSecurity.isEntryAdmin(), ienv.getRoles().isGuestOnly(), null);
		container.setLocalSecurityCallback(secCallback);
		return container;
	}
}