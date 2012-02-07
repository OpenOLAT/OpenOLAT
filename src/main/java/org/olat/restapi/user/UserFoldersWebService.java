/**
 * <a href=“http://www.openolat.org“>
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
 * 2011 by frentix GmbH, http://www.frentix.com
 * <p>
**/
package org.olat.restapi.user;

import static org.olat.restapi.security.RestSecurityHelper.getIdentity;
import static org.olat.restapi.security.RestSecurityHelper.getRoles;
import static org.olat.restapi.security.RestSecurityHelper.getUserRequest;
import static org.olat.restapi.security.RestSecurityHelper.isAdmin;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.olat.basesecurity.BaseSecurityManager;
import org.olat.basesecurity.IdentityShort;
import org.olat.collaboration.CollaborationTools;
import org.olat.core.CoreSpringFactory;
import org.olat.core.commons.modules.bc.BriefcaseWebDAVProvider;
import org.olat.core.commons.modules.bc.FolderConfig;
import org.olat.core.commons.modules.bc.vfs.OlatRootFolderImpl;
import org.olat.core.gui.UserRequest;
import org.olat.core.id.Identity;
import org.olat.core.id.IdentityEnvironment;
import org.olat.core.id.Roles;
import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;
import org.olat.core.util.nodes.INode;
import org.olat.core.util.notifications.NotificationsManager;
import org.olat.core.util.notifications.Subscriber;
import org.olat.core.util.tree.Visitor;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.core.util.vfs.callbacks.ReadOnlyCallback;
import org.olat.core.util.vfs.callbacks.VFSSecurityCallback;
import org.olat.core.util.vfs.restapi.VFSWebservice;
import org.olat.course.CourseFactory;
import org.olat.course.ICourse;
import org.olat.course.nodes.BCCourseNode;
import org.olat.course.nodes.CourseNode;
import org.olat.course.nodes.bc.BCWebService;
import org.olat.course.run.userview.CourseTreeVisitor;
import org.olat.group.BusinessGroup;
import org.olat.group.BusinessGroupManager;
import org.olat.group.BusinessGroupManagerImpl;
import org.olat.group.SearchBusinessGroupParams;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryManager;
import org.olat.repository.SearchRepositoryEntryParameters;
import org.olat.resource.accesscontrol.AccessResult;
import org.olat.resource.accesscontrol.manager.ACFrontendManager;
import org.olat.restapi.group.LearningGroupWebService;
import org.olat.restapi.support.vo.FolderVO;
import org.olat.restapi.support.vo.FolderVOes;

/**
 * 
 * Description:<br>
 * 
 * <P>
 * Initial Date:  16 déc. 2011 <br>
 *
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
@Path("users/{identityKey}/folders")
public class UserFoldersWebService {
	
	private static final OLog log = Tracing.createLoggerFor(UserFoldersWebService.class);
	
	@Path("personal")
	public VFSWebservice getFolder(@PathParam("identityKey") Long identityKey, @Context HttpServletRequest request) {
		Identity identity = getIdentity(request);
		if(identity == null) {
			throw new WebApplicationException(Response.serverError().status(Status.UNAUTHORIZED).build());
		}
		
		if(identityKey.equals(identity.getKey())) {
			//private and public folder
			VFSContainer myFodlers = new BriefcaseWebDAVProvider().getContainer(identity);
			return new VFSWebservice(myFodlers);
		} else {
			//only public
			IdentityShort retrievedIdentity = BaseSecurityManager.getInstance().loadIdentityShortByKey(identityKey);
			String chosenUserFolderRelPath = FolderConfig.getUserHome(retrievedIdentity.getName()) + "/" + "public";
			OlatRootFolderImpl rootFolder = new OlatRootFolderImpl(chosenUserFolderRelPath, null);
			VFSSecurityCallback secCallback = new ReadOnlyCallback();
			rootFolder.setLocalSecurityCallback(secCallback);
			return new VFSWebservice(rootFolder);
		}
	}
	
	/**
	 * Retrieves the folder of a group
	 * @response.representation.200.qname {http://www.example.com}fileVO
	 * @response.representation.200.mediaType application/xml, application/json
	 * @response.representation.200.doc The files
	 * @response.representation.200.example {@link org.olat.restapi.support.vo.Examples#SAMPLE_FILE}
	 * @response.representation.401.doc The roles of the authenticated user are not sufficient
	 * @param groupKey The key of the group
	 * @param courseNodeId The key of the node if it's a course
	 * @param httpRequest The HTTP request
	 * @param request The REST request
	 * @return The files
	 */
	@Path("group/{groupKey}")
	public VFSWebservice getGroupFolder(@PathParam("groupKey") Long groupKey, @Context HttpServletRequest request) {
		if(groupKey == null) {
			throw new WebApplicationException( Response.serverError().status(Status.NOT_FOUND).build());
		}
		return new LearningGroupWebService().getFolder(groupKey, request);
	}
	
	/**
	 * Retrieves the folder of a course building block
	 * @response.representation.200.qname {http://www.example.com}fileVO
	 * @response.representation.200.mediaType application/xml, application/json
	 * @response.representation.200.doc The files
	 * @response.representation.200.example {@link org.olat.restapi.support.vo.Examples#SAMPLE_FILE}
	 * @response.representation.401.doc The roles of the authenticated user are not sufficient
	 * @param courseKey The key of the course
	 * @param courseNodeId The key of the node
	 * @param httpRequest The HTTP request
	 * @param request The REST request
	 * @return The files
	 */
	@Path("course/{courseKey}/{courseNodeId}")
	public VFSWebservice getCourseFolder(@PathParam("courseKey") Long courseKey, @PathParam("courseNodeId") String courseNodeId,
			@Context HttpServletRequest request) {

		if(courseNodeId == null) {
			throw new WebApplicationException( Response.serverError().status(Status.NOT_FOUND).build());
		} else if (courseKey != null) {
			ICourse course = loadCourse(courseKey);
			if(course == null) {
				throw new WebApplicationException( Response.serverError().status(Status.NOT_FOUND).build());
			}
			CourseNode node =course.getEditorTreeModel().getCourseNode(courseNodeId);
			if(node == null) {
				throw new WebApplicationException( Response.serverError().status(Status.NOT_FOUND).build());
			} else if(!(node instanceof BCCourseNode)) {
				throw new WebApplicationException(Response.serverError().status(Status.NOT_ACCEPTABLE).build());
			}

			UserRequest ureq = getUserRequest(request);
			CourseTreeVisitor courseVisitor = new CourseTreeVisitor(course, ureq.getUserSession().getIdentityEnvironment());
			if(courseVisitor.isAccessible(node)) {
				BCCourseNode bcNode = (BCCourseNode)node;
				VFSContainer container = BCCourseNode.getSecurisedNodeFolderContainer(bcNode, course.getCourseEnvironment(), ureq.getUserSession().getIdentityEnvironment());
				return new VFSWebservice(container);
			} else {
				throw new WebApplicationException(Response.serverError().status(Status.UNAUTHORIZED).build());
			}
		}
		return null;
	}
	
	/**
	 * Retrieves a list of folders on a user base. All folders of groups 
	 * where the user is participant/tutor + all folders in course where
	 * the user is a participant (owner, tutor or participant)
	 * @response.representation.200.qname {http://www.example.com}folderVOes
	 * @response.representation.200.mediaType application/xml, application/json
	 * @response.representation.200.doc The folders
	 * @response.representation.200.example {@link org.olat.restapi.support.vo.Examples#SAMPLE_FOLDERVOes}
	 * @response.representation.401.doc The roles of the authenticated user are not sufficient
	 * @param identityKey The key of the user (IdentityImpl)
	 * @param httpRequest The HTTP request
	 * @param request The REST request
	 * @return The folders
	 */
	@GET
	@Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
	public Response getFolders(@PathParam("identityKey") Long identityKey,
			@Context HttpServletRequest httpRequest, @Context Request request) {
		
		Roles roles;
		Identity retrievedUser = getIdentity(httpRequest);
		if(retrievedUser == null) {
			return Response.serverError().status(Status.UNAUTHORIZED).build();
		} else if(!identityKey.equals(retrievedUser.getKey())) {
			if(isAdmin(httpRequest)) {
				retrievedUser = BaseSecurityManager.getInstance().loadIdentityByKey(identityKey);
				roles = BaseSecurityManager.getInstance().getRoles(retrievedUser);
			} else {
				return Response.serverError().status(Status.UNAUTHORIZED).build();
			}
		} else {
			roles = getRoles(httpRequest);
		}

		final Map<Long,Long> groupNotified = new HashMap<Long,Long>();
		final Map<Long,Long> courseNotified = new HashMap<Long,Long>();
		NotificationsManager man = NotificationsManager.getInstance();
		{//collect subscriptions
			List<String> notiTypes = Collections.singletonList("FolderModule");
			List<Subscriber> subs = man.getSubscribers(retrievedUser, notiTypes);
			for(Subscriber sub:subs) {
				String resName = sub.getPublisher().getResName();
				if("BusinessGroup".equals(resName)) {
					Long groupKey = sub.getPublisher().getResId();
					groupNotified.put(groupKey, sub.getPublisher().getResId());
				} else if("CourseModule".equals(resName)) {
					Long courseKey = sub.getPublisher().getResId();
					courseNotified.put(courseKey, sub.getPublisher().getResId());
				}
			}
		}

		final List<FolderVO> folderVOs = new ArrayList<FolderVO>();
		
		RepositoryManager rm = RepositoryManager.getInstance();
		ACFrontendManager acManager = (ACFrontendManager)CoreSpringFactory.getBean("acFrontendManager");
		SearchRepositoryEntryParameters repoParams = new SearchRepositoryEntryParameters(retrievedUser, roles, "CourseModule");
		repoParams.setOnlyExplicitMember(true);
		List<RepositoryEntry> entries = rm.genericANDQueryWithRolesRestriction(repoParams, 0, -1, true);
		for(RepositoryEntry entry:entries) {
			AccessResult result = acManager.isAccessible(entry, retrievedUser, false);
			if(result.isAccessible()) {
				final ICourse course = CourseFactory.loadCourse(entry.getOlatResource());
				final IdentityEnvironment ienv = new IdentityEnvironment(retrievedUser, roles);
				
				new CourseTreeVisitor(course,  ienv).visit(new Visitor() {
					@Override
					public void visit(INode node) {
						if(node instanceof BCCourseNode) {
							BCCourseNode bcNode = (BCCourseNode)node;
							FolderVO folder = BCWebService.createFolderVO(ienv, course, bcNode, courseNotified.containsKey(bcNode.getIdent()));
							folderVOs.add(folder);
						}
					}
				});
			}
		}
		
		//start found forums in groups
		BusinessGroupManager bgm = BusinessGroupManagerImpl.getInstance();
		SearchBusinessGroupParams params = new SearchBusinessGroupParams();
		params.addTypes(BusinessGroup.TYPE_BUDDYGROUP, BusinessGroup.TYPE_LEARNINGROUP);
		params.addTools(CollaborationTools.TOOL_FOLDER);
		List<BusinessGroup> groups = bgm.findBusinessGroups(params, retrievedUser, true, true, null, 0, -1);
		for(BusinessGroup group:groups) {
			FolderVO folderVo = new FolderVO();
			folderVo.setName(group.getName());
			folderVo.setGroupKey(group.getKey());
			folderVo.setSubscribed(groupNotified.containsKey(group.getKey()));
			folderVOs.add(folderVo);
		}

		FolderVOes voes = new FolderVOes();
		voes.setFolders(folderVOs.toArray(new FolderVO[folderVOs.size()]));
		voes.setTotalCount(folderVOs.size());
		return Response.ok(voes).build();
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
