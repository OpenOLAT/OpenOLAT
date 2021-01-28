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
package org.olat.user.restapi;

import static org.olat.restapi.security.RestSecurityHelper.getIdentity;
import static org.olat.restapi.security.RestSecurityHelper.getRoles;

import java.util.ArrayList;
import java.util.Collection;
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
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.olat.basesecurity.BaseSecurityManager;
import org.olat.basesecurity.OrganisationRoles;
import org.olat.collaboration.CollaborationTools;
import org.olat.collaboration.CollaborationToolsFactory;
import org.olat.core.CoreSpringFactory;
import org.olat.core.commons.modules.bc.BriefcaseWebDAVProvider;
import org.olat.core.commons.modules.bc.FolderConfig;
import org.olat.core.commons.services.notifications.NotificationsManager;
import org.olat.core.commons.services.notifications.Subscriber;
import org.olat.core.commons.services.vfs.restapi.VFSWebservice;
import org.olat.core.id.Identity;
import org.olat.core.id.IdentityEnvironment;
import org.olat.core.id.Roles;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.core.util.vfs.VFSManager;
import org.olat.core.util.vfs.callbacks.ReadOnlyCallback;
import org.olat.core.util.vfs.callbacks.VFSSecurityCallback;
import org.olat.course.CourseFactory;
import org.olat.course.ICourse;
import org.olat.course.nodes.BCCourseNode;
import org.olat.course.nodes.bc.BCWebService;
import org.olat.course.run.userview.CourseTreeVisitor;
import org.olat.group.BusinessGroup;
import org.olat.group.BusinessGroupService;
import org.olat.group.model.SearchBusinessGroupParams;
import org.olat.restapi.group.LearningGroupWebService;
import org.olat.restapi.support.vo.FileVO;
import org.olat.restapi.support.vo.FolderVO;
import org.olat.restapi.support.vo.FolderVOes;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;

/**
 * 
 * Initial Date:  16 déc. 2011 <br>
 *
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class UserFoldersWebService {
	
	private final Identity identity;
	
	public UserFoldersWebService(Identity identity) {
		this.identity = identity;
	}
	
	@Path("personal")
	public VFSWebservice getFolder(@Context HttpServletRequest request) {
		Identity ureqIdentity = getIdentity(request);
		if(identity.getKey().equals(ureqIdentity.getKey())) {
			//private and public folder
			Roles roles = getRoles(request);
			VFSContainer myFodlers = new BriefcaseWebDAVProvider().getContainer(ureqIdentity, roles);
			return new VFSWebservice(myFodlers);
		} else {
			//only public
			String chosenUserFolderRelPath = FolderConfig.getUserHome(identity) + "/" + "public";
			VFSContainer rootFolder = VFSManager.olatRootContainer(chosenUserFolderRelPath, null);
			VFSSecurityCallback secCallback = new ReadOnlyCallback();
			rootFolder.setLocalSecurityCallback(secCallback);
			return new VFSWebservice(rootFolder);
		}
	}
	
	/**
	 * Retrieves the folder of a group
	 * 
	 * @param groupKey The key of the group
	 * @param courseNodeId The key of the node if it's a course
	 * @param httpRequest The HTTP request
	 * @param request The REST request
	 * @return The files
	 */
	@Path("group/{groupKey}") 
	@Operation(summary = "Retrieve the folder of a group", description = "Retrieves the folder of a group")
	@ApiResponse(responseCode = "200", description = "The files", content = {
			@Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = FileVO.class))),
			@Content(mediaType = "application/xml", array = @ArraySchema(schema = @Schema(implementation = FileVO.class))) })
	@ApiResponse(responseCode = "401", description = "The roles of the authenticated user are not sufficient")
	public VFSWebservice getGroupFolder(@PathParam("groupKey") Long groupKey, @Context HttpServletRequest request) {
		if(groupKey == null) {
			throw new WebApplicationException( Response.serverError().status(Status.NOT_FOUND).build());
		}
		
		LearningGroupWebService groupWebService = new LearningGroupWebService();
		CoreSpringFactory.autowireObject(groupWebService);
		return groupWebService.getFolder(groupKey, request);
	}
	
	/**
	 * Retrieves the folder of a course building block
	 * 
	 * @param courseKey The key of the course
	 * @param courseNodeId The key of the node
	 * @param httpRequest The HTTP request
	 * @param request The REST request
	 * @return The files
	 */
	@Path("course/{courseKey}/{courseNodeId}")
	@Operation(summary = "Retrieves the folder of a course building block", description = "Retrieves the folder of a course building block")
	@ApiResponse(responseCode = "200", description = "The files", content = {
			@Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = FileVO.class))),
			@Content(mediaType = "application/xml", array = @ArraySchema(schema = @Schema(implementation = FileVO.class))) })
	@ApiResponse(responseCode = "401", description = "The roles of the authenticated user are not sufficient")
	public VFSWebservice getCourseFolder(@PathParam("courseKey") Long courseKey, @PathParam("courseNodeId") String courseNodeId,
			@Context HttpServletRequest request) {
		return new BCWebService().getVFSWebService(courseKey, courseNodeId, request);
	}
	
	/**
	 * Retrieves a list of folders on a user base. All folders of groups 
	 * where the user is participant/tutor + all folders in course where
	 * the user is a participant (owner, tutor or participant)
	 * 
	 * @param identityKey The key of the user (IdentityImpl)
	 * @param httpRequest The HTTP request
	 * @return The folders
	 */
	@GET
	@Operation(summary = "Retrieves a list of folders on a user base", description = "Retrieves a list of folders on a user base. All folders of groups \n" + 
			" where the user is participant/tutor + all folders in course where\n" + 
			" the user is a participant (owner, tutor or participant)")
	@ApiResponse(responseCode = "200", description = "The folders", content = {
			@Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = FolderVO.class))),
			@Content(mediaType = "application/xml", array = @ArraySchema(schema = @Schema(implementation = FolderVO.class))) })
	@ApiResponse(responseCode = "401", description = "The roles of the authenticated user are not sufficient")
	@Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
	public Response getFolders(@Context HttpServletRequest httpRequest) {
		
		Roles roles;
		Identity ureqIdentity = getIdentity(httpRequest);
		if(ureqIdentity == null) {
			return Response.serverError().status(Status.UNAUTHORIZED).build();
		} else if(!identity.getKey().equals(ureqIdentity.getKey())) {
			roles = BaseSecurityManager.getInstance().getRoles(identity);
			if(isAdminOf(roles, httpRequest)) {
				ureqIdentity = identity;
			} else {
				return Response.serverError().status(Status.UNAUTHORIZED).build();
			}
		} else {
			roles = getRoles(httpRequest);
		}

		final Map<Long,Long> groupNotified = new HashMap<>();
		final Map<Long,Collection<String>> courseNotified = new HashMap<>();
		NotificationsManager man = CoreSpringFactory.getImpl(NotificationsManager.class);
		{//collect subscriptions
			List<String> notiTypes = Collections.singletonList("FolderModule");
			List<Subscriber> subs = man.getSubscribers(ureqIdentity, notiTypes, true);
			for(Subscriber sub:subs) {
				String resName = sub.getPublisher().getResName();
				if("BusinessGroup".equals(resName)) {
					Long groupKey = sub.getPublisher().getResId();
					groupNotified.put(groupKey, sub.getPublisher().getResId());
				} else if("CourseModule".equals(resName)) {
					Long courseKey = sub.getPublisher().getResId();
					if(!courseNotified.containsKey(courseKey)) {
						courseNotified.put(courseKey,new ArrayList<>());
					}
					courseNotified.get(courseKey).add(sub.getPublisher().getSubidentifier());
				}
			}
		}

		final List<FolderVO> folderVOs = new ArrayList<>();
		final IdentityEnvironment ienv = new IdentityEnvironment(ureqIdentity, roles);
		for(Map.Entry<Long, Collection<String>> e:courseNotified.entrySet()) {
			final Long courseKey = e.getKey();
			final Collection<String> nodeKeys = e.getValue();
			final ICourse course = CourseFactory.loadCourse(courseKey);
			new CourseTreeVisitor(course, ienv).visit(node -> {
				if(node instanceof BCCourseNode) {
					BCCourseNode bcNode = (BCCourseNode)node;
					if(nodeKeys.contains(bcNode.getIdent())) {
						FolderVO folder = BCWebService.createFolderVO(ienv, course, bcNode, courseNotified.get(course.getResourceableId()));
						folderVOs.add(folder);
					}
				}
			});
		}
		
		//start found forums in groups
		BusinessGroupService bgs = CoreSpringFactory.getImpl(BusinessGroupService.class);
		SearchBusinessGroupParams params = new SearchBusinessGroupParams(ureqIdentity, true, true);
		params.addTools(CollaborationTools.TOOL_FOLDER);
		List<BusinessGroup> groups = bgs.findBusinessGroups(params, null, 0, -1);
		for(BusinessGroup group:groups) {
			CollaborationTools tools = CollaborationToolsFactory.getInstance().getOrCreateCollaborationTools(group);
			VFSContainer container = tools.getSecuredFolder(group, null, ureqIdentity, false);

			FolderVO folderVo = new FolderVO();
			folderVo.setName(group.getName());
			folderVo.setGroupKey(group.getKey());
			folderVo.setSubscribed(groupNotified.containsKey(group.getKey()));
			folderVo.setRead(container.getLocalSecurityCallback().canRead());
			folderVo.setList(container.getLocalSecurityCallback().canList());
			folderVo.setWrite(container.getLocalSecurityCallback().canWrite());
			folderVo.setDelete(container.getLocalSecurityCallback().canDelete());

			folderVOs.add(folderVo);
		}

		FolderVOes voes = new FolderVOes();
		voes.setFolders(folderVOs.toArray(new FolderVO[folderVOs.size()]));
		voes.setTotalCount(folderVOs.size());
		return Response.ok(voes).build();
	}
	
	private boolean isAdminOf(Roles identityRoles, HttpServletRequest httpRequest) {
		Roles managerRoles = getRoles(httpRequest);
		return managerRoles.isManagerOf(OrganisationRoles.usermanager, identityRoles)
				|| managerRoles.isManagerOf(OrganisationRoles.rolesmanager, identityRoles)
				|| managerRoles.isManagerOf(OrganisationRoles.administrator, identityRoles);
	}
}
