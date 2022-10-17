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
package org.olat.modules.fo.restapi;

import static org.olat.collaboration.CollaborationTools.KEY_FORUM;
import static org.olat.collaboration.CollaborationTools.PROP_CAT_BG_COLLABTOOLS;
import static org.olat.restapi.security.RestSecurityHelper.getIdentity;
import static org.olat.restapi.security.RestSecurityHelper.getRoles;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;

import org.olat.basesecurity.BaseSecurityManager;
import org.olat.basesecurity.OrganisationRoles;
import org.olat.collaboration.CollaborationTools;
import org.olat.core.CoreSpringFactory;
import org.olat.core.commons.services.notifications.NotificationsManager;
import org.olat.core.commons.services.notifications.Subscriber;
import org.olat.core.id.Identity;
import org.olat.core.id.IdentityEnvironment;
import org.olat.core.id.Roles;
import org.olat.core.util.resource.OresHelper;
import org.olat.course.CourseFactory;
import org.olat.course.ICourse;
import org.olat.course.nodes.FOCourseNode;
import org.olat.course.run.userview.CourseTreeVisitor;
import org.olat.group.BusinessGroup;
import org.olat.group.BusinessGroupService;
import org.olat.group.model.SearchBusinessGroupParams;
import org.olat.properties.Property;
import org.olat.properties.PropertyManager;
import org.olat.restapi.group.LearningGroupWebService;
import org.olat.restapi.support.vo.FileVO;
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
 * 
 * <P>
 * Initial Date:  6 déc. 2011 <br>
 *
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
@Tag(name = "Users")
@Component
@Path("users/{identityKey}/forums")
public class MyForumsWebService {
	
	@Autowired
	private PropertyManager propertyManager;
	@Autowired
	private NotificationsManager notificationsManager;
	@Autowired
	private BusinessGroupService businessGroupService;
	@Autowired
	private BaseSecurityManager securityManager;

	/**
	 * Retrieves the forum of a group
	 * 
	 * @param groupKey The key of the group
	 * @param courseNodeId The key of the node if it's a course
	 * @param httpRequest The HTTP request
	 * @param request The REST request
	 * @return The files
	 */

	@Path("group/{groupKey}")
	@Operation(summary = "Retrieves the forum of a group", description = "Retrieves the forum of a group")
	@ApiResponse(responseCode = "200", description = "The forum",
			content = {
					@Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = FileVO.class))),
					@Content(mediaType = "application/xml", array = @ArraySchema(schema = @Schema(implementation = FileVO.class)))
				})
	@ApiResponse(responseCode = "401", description = "The roles of the authenticated user are not sufficient.")
	@ApiResponse(responseCode = "404", description = "Not found.")
	public ForumWebService getGroupForum(@PathParam("groupKey") Long groupKey, @Context HttpServletRequest request) {
		if(groupKey == null) {
			throw new WebApplicationException( Response.serverError().status(Status.NOT_FOUND).build());
		}
		LearningGroupWebService groupWebService = new LearningGroupWebService();
		CoreSpringFactory.autowireObject(groupWebService);
		return groupWebService.getForum(groupKey, request);
	}
	
	/**
	 * Retrieves the forum of a course building block
	 * 
	 * @param courseKey The key of the course
	 * @param courseNodeId The key of the node
	 * @param httpRequest The HTTP request
	 * @param request The REST request
	 * @return The files
	 */
	@Path("course/{courseKey}/{courseNodeId}")
	@Operation(summary = "Retrieves the forum of a course building block", description = "Retrieves the forum of a course building block")
	@ApiResponse(responseCode = "200", description = "The files",
			content = {
					@Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = FileVO.class))),
					@Content(mediaType = "application/xml", array = @ArraySchema(schema = @Schema(implementation = FileVO.class)))
				})
	@ApiResponse(responseCode = "401", description = "The roles of the authenticated user are not sufficient")
	@ApiResponse(responseCode = "404", description = "Not found")
	public ForumWebService getCourseFolder(@PathParam("courseKey") Long courseKey, @PathParam("courseNodeId") String courseNodeId,
			@Context HttpServletRequest request) {
		return new ForumCourseNodeWebService().getForumContent(courseKey, courseNodeId, request);
	}
	
	/**
	 * Retrieves a list of forums on a user base. All forums of groups 
	 * where the user is participant/tutor + all forums in course where
	 * the user is a participant (owner, tutor or participant)
	 * 
	 * @param identityKey The key of the user (IdentityImpl)
	 * @param httpRequest The HTTP request
	 * @return The forums
	 */
	@GET
	@Operation(summary = "Retrieves a list of forums on a user base", description = "Retrieves a list of forums on a user base. All forums of groups \n" + 
			"where the user is participant/tutor + all forums in course where\n" + 
			"the user is a participant (owner, tutor or participant)")
	@ApiResponse(responseCode = "200", description = "The Forums",
			content = {
					@Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = ForumVO.class))),
					@Content(mediaType = "application/xml", array = @ArraySchema(schema = @Schema(implementation = ForumVO.class)))
				})
	@ApiResponse(responseCode = "401", description = "The roles of the authenticated user are not sufficient")
	@ApiResponse(responseCode = "404", description = "Not found")
	@Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
	public Response getForums(@PathParam("identityKey") Long identityKey,
			@Context HttpServletRequest httpRequest) {
		
		Roles roles;
		Identity retrievedUser = getIdentity(httpRequest);
		if(retrievedUser == null) {
			return Response.serverError().status(Status.UNAUTHORIZED).build();
		} else if(!identityKey.equals(retrievedUser.getKey())) {
			retrievedUser = securityManager.loadIdentityByKey(identityKey);
			roles = securityManager.getRoles(retrievedUser);
			if(!isAdminOf(roles, httpRequest)) {
				return Response.serverError().status(Status.UNAUTHORIZED).build();
			}
		} else {
			roles = getRoles(httpRequest);
		}

		Map<Long,Long> groupNotified = new HashMap<>();
		Map<Long,Collection<Long>> courseNotified = new HashMap<>();
		final Set<Long> subscriptions = new HashSet<>();
		{//collect subscriptions
			List<String> notiTypes = Collections.singletonList("Forum");
			List<Subscriber> subs = notificationsManager.getSubscribers(retrievedUser, notiTypes, true);
			for(Subscriber sub:subs) {
				String resName = sub.getPublisher().getResName();
				Long forumKey = Long.parseLong(sub.getPublisher().getData());
				subscriptions.add(forumKey);
				
				if("BusinessGroup".equals(resName)) {
					Long groupKey = sub.getPublisher().getResId();
					groupNotified.put(groupKey, forumKey);
				} else if("CourseModule".equals(resName)) {
					Long courseKey = sub.getPublisher().getResId();
					if(!courseNotified.containsKey(courseKey)) {
						courseNotified.put(courseKey, new ArrayList<Long>());
					}
					courseNotified.get(courseKey).add(forumKey);
				}
			}
		}
		
		final List<ForumVO> forumVOs = new ArrayList<>();
		final IdentityEnvironment ienv = new IdentityEnvironment(retrievedUser, roles);
		for(Map.Entry<Long, Collection<Long>> e:courseNotified.entrySet()) {
			final Long courseKey = e.getKey();
			final Collection<Long> forumKeys = e.getValue();
			final ICourse course = CourseFactory.loadCourse(courseKey);
			new CourseTreeVisitor(course, ienv).visit(node -> {
				if(node instanceof FOCourseNode) {
					FOCourseNode forumNode = (FOCourseNode)node;
					ForumVO forumVo = ForumCourseNodeWebService.createForumVO(course, forumNode, subscriptions);
					if(forumKeys.contains(forumVo.getForumKey())) {
						forumVOs.add(forumVo);
					}
				}
			});
		}
		
		//start found forums in groups
		SearchBusinessGroupParams params = new SearchBusinessGroupParams(retrievedUser, true, true);
		params.addTools(CollaborationTools.TOOL_FORUM);
		List<BusinessGroup> groups = businessGroupService.findBusinessGroups(params, null, 0, -1);
		//list forum keys
		List<Long> groupIds = new ArrayList<>();
		Map<Long,BusinessGroup> groupsMap = new HashMap<>();
		for(BusinessGroup group:groups) {
			if(groupNotified.containsKey(group.getKey())) {
				ForumVO forumVo = new ForumVO();
				forumVo.setName(group.getName());
				forumVo.setGroupKey(group.getKey());
				forumVo.setForumKey(groupNotified.get(group.getKey()));
				forumVo.setSubscribed(true);
				forumVOs.add(forumVo);
				
				groupIds.remove(group.getKey());
			} else {
				groupIds.add(group.getKey());
				groupsMap.put(group.getKey(), group);
			}
		}

		List<Property> forumProperties = propertyManager.findProperties(OresHelper.calculateTypeName(BusinessGroup.class), groupIds, PROP_CAT_BG_COLLABTOOLS, KEY_FORUM);
		for(Property prop:forumProperties) {
			Long forumKey = prop.getLongValue();
			if(forumKey != null && groupsMap.containsKey(prop.getResourceTypeId())) {
				BusinessGroup group = groupsMap.get(prop.getResourceTypeId());
				
				ForumVO forumVo = new ForumVO();
				forumVo.setName(group.getName());
				forumVo.setGroupKey(group.getKey());
				forumVo.setForumKey(prop.getLongValue());
				forumVo.setSubscribed(false);
				forumVOs.add(forumVo);
			}
		}

		ForumVOes voes = new ForumVOes();
		voes.setForums(forumVOs.toArray(new ForumVO[forumVOs.size()]));
		voes.setTotalCount(forumVOs.size());
		return Response.ok(voes).build();
	}
	
	private boolean isAdminOf(Roles identityRoles, HttpServletRequest httpRequest) {
		Roles managerRoles = getRoles(httpRequest);
		return managerRoles.isManagerOf(OrganisationRoles.administrator, identityRoles)
				|| managerRoles.isManagerOf(OrganisationRoles.usermanager, identityRoles)
				|| managerRoles.isManagerOf(OrganisationRoles.rolesmanager, identityRoles);
		
	}
}