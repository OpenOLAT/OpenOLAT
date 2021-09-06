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
* Copyright (c) since 2004 at Multimedia- & E-Learning Services (MELS),<br>
* University of Zurich, Switzerland.
* <hr>
* <a href="http://www.openolat.org">
* OpenOLAT - Online Learning and Training</a><br>
* This file has been modified by the OpenOLAT community. Changes are licensed
* under the Apache 2.0 license as the original file.
*/
package org.olat.modules.fo.restapi;

import static org.olat.restapi.security.RestSecurityHelper.getUserRequest;

import java.util.ArrayList;
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
import org.olat.core.commons.services.notifications.NotificationsManager;
import org.olat.core.commons.services.notifications.Subscriber;
import org.olat.core.gui.UserRequest;
import org.olat.core.util.StringHelper;
import org.olat.course.ICourse;
import org.olat.course.nodes.CourseNode;
import org.olat.course.nodes.FOCourseNode;
import org.olat.course.properties.CoursePropertyManager;
import org.olat.course.run.userview.CourseTreeVisitor;
import org.olat.modules.ModuleConfiguration;
import org.olat.modules.fo.Forum;
import org.olat.modules.fo.manager.ForumManager;
import org.olat.properties.Property;
import org.olat.restapi.repository.course.AbstractCourseNodeWebService;
import org.olat.restapi.repository.course.CourseWebService;
import org.olat.restapi.repository.course.CoursesWebService;
import org.olat.restapi.support.vo.CourseNodeVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * 
 * Description:<br>
 * REST API implementation for forum course node 
 * 
 * <P>
 * Initial Date:  20.12.2010 <br>
 * @author skoeber
 */
@Tag(name = "Repo")
@Component
@Path("repo/courses/{courseId}/elements/forum")
public class ForumCourseNodeWebService extends AbstractCourseNodeWebService {
	
	@Autowired
	private ForumManager forumManager;
	@Autowired
	private NotificationsManager notificationsManager;
	
	/**
	 * Retrieves metadata of the published course node
	 * 
	 * @param courseId The course resourceable's id
	 * @param httpRequest The HTTP request
	 * @return The persisted structure element (fully populated)
	 */
	@GET
	@Operation(summary = "Retrieves metadata of the published course node",
		description = "Retrieves metadata of the published course node")
	@ApiResponse(responseCode = "200", description = "The course node metadatas",
			content = {
					@Content(mediaType = "application/json", schema = @Schema(implementation = ForumVO.class)),
					@Content(mediaType = "application/xml", schema = @Schema(implementation = ForumVO.class))
				})
	@ApiResponse(responseCode = "401", description = "The roles of the authenticated user are not sufficient")
	@ApiResponse(responseCode = "404", description = "The course or parentNode not found")
	@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	public Response getForums(@PathParam("courseId") Long courseId, @Context HttpServletRequest httpRequest) {
		final ICourse course = CoursesWebService.loadCourse(courseId);
		if(course == null) {
			return Response.serverError().status(Status.NOT_FOUND).build();
		}
		if (!CourseWebService.isCourseAccessible(course, httpRequest)) {
			return Response.serverError().status(Status.UNAUTHORIZED).build();
		}

		UserRequest ureq = getUserRequest(httpRequest);

		final Set<Long> subcribedForums = new HashSet<>();
		List<String> notiTypes = Collections.singletonList("Forum");
		List<Subscriber> subs = notificationsManager.getSubscribers(ureq.getIdentity(), notiTypes, true);
		for(Subscriber sub:subs) {
			Long forumKey = Long.parseLong(sub.getPublisher().getData());
			subcribedForums.add(forumKey);
		}

		final List<ForumVO> forumVOs = new ArrayList<>();
		new CourseTreeVisitor(course, ureq.getUserSession().getIdentityEnvironment()).visit(node -> {
			if(node instanceof FOCourseNode) {
				FOCourseNode forumNode = (FOCourseNode)node;
				ForumVO forum = createForumVO(course, forumNode, subcribedForums);
				forumVOs.add(forum);
			}
		});

		ForumVOes voes = new ForumVOes();
		voes.setForums(forumVOs.toArray(new ForumVO[forumVOs.size()]));
		voes.setTotalCount(forumVOs.size());
		return Response.ok(voes).build();
	}

	/**
	 * This attaches a Forum Element onto a given course. The element will be
	 * inserted underneath the supplied parentNodeId.
	 * 
	 * @param courseId The course resourceable's id
	 * @param parentNodeId The node's id which will be the parent of this single
	 *          page
	 * @param position The node's position relative to its sibling nodes (optional)
	 * @param shortTitle The node short title
	 * @param longTitle The node long title
	 * @param description The node description
	 * @param objectives The node learning objectives
	 * @param instruction The node instruction
	 * @param instructionalDesign The node instructional design
	 * @param visibilityExpertRules The rules to view the node (optional)
	 * @param accessExpertRules The rules to access the node (optional)
	 * @param request The HTTP request
	 * @return The persisted Forum Element (fully populated)
	 */
	@POST
	@Operation(summary = "attach a Forum Element onto a given course",
		description = "This attaches a Forum Element onto a given course. The element will be\n" + 
			" inserted underneath the supplied parentNodeId")
	@ApiResponse(responseCode = "200", description = "The course node metadatas",
			content = {
					@Content(mediaType = "application/json", schema = @Schema(implementation = CourseNodeVO.class)),
					@Content(mediaType = "application/xml", schema = @Schema(implementation = CourseNodeVO.class))
				})
	@ApiResponse(responseCode = "401", description = "The roles of the authenticated user are not sufficient")
	@ApiResponse(responseCode = "404", description = "The course or parentNode not found")
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	public Response attachForumPost(@PathParam("courseId") Long courseId,
			@FormParam("parentNodeId") String parentNodeId, @FormParam("position") Integer position,
			@FormParam("shortTitle") String shortTitle,
			@FormParam("longTitle") @DefaultValue("undefined") String longTitle,
			@FormParam("description") String description,
			@FormParam("objectives") String objectives, @FormParam("instruction") String instruction,
			@FormParam("instructionalDesign") String instructionalDesign,
			@FormParam("visibilityExpertRules") String visibilityExpertRules,
			@FormParam("accessExpertRules") String accessExpertRules,
			@FormParam("moderatorExpertRules") String moderatorExpertRules,
			@FormParam("posterExpertRules") String posterExpertRules,
			@FormParam("readerExpertRules") String readerExpertRules, @Context HttpServletRequest request) {
		return attachForum(courseId, parentNodeId, position, shortTitle, longTitle, description, objectives,
				instruction, instructionalDesign, visibilityExpertRules, accessExpertRules, moderatorExpertRules,
				posterExpertRules, readerExpertRules, request);
	}
	
	/**
	 * This attaches a Forum Element onto a given course. The element will be
	 * inserted underneath the supplied parentNodeId.
	 * 
	 * @param courseId The course resourceable id
	 * @param parentNodeId The node's id which will be the parent of this single
	 *          page
	 * @param position The node's position relative to its sibling nodes (optional)
	 * @param shortTitle The node short title
	 * @param longTitle The node long title
	 * @param description The node description
	 * @param description The node description
	 * @param objectives The node learning objectives
	 * @param instruction The node instruction
	 * @param instructionalDesign The node instructional design
	 * @param instruction The node instruction
	 * @param instructionalDesign The node instructional design
	 * @param visibilityExpertRules The rules to view the node (optional)
	 * @param accessExpertRules The rules to access the node (optional)
	 * @param request The HTTP request
	 * @return The persisted Forum Element (fully populated)
	 */
	@PUT
	@Operation(summary = "attach a Forum Element onto a given course",
		description = "This attaches a Forum Element onto a given course. The element will be\n" + 
			" inserted underneath the supplied parentNodeId")
	@ApiResponse(responseCode = "200", description = "The course node metadatas",
			content = {
					@Content(mediaType = "application/json", schema = @Schema(implementation = CourseNodeVO.class)),
					@Content(mediaType = "application/xml", schema = @Schema(implementation = CourseNodeVO.class))
				})
	@ApiResponse(responseCode = "401", description = "The roles of the authenticated user are not sufficient")
	@ApiResponse(responseCode = "404", description = "The course or parentNode not found")
	@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	public Response attachForum(@PathParam("courseId") Long courseId,
			@QueryParam("parentNodeId") @Parameter(description = "The node's id which will be the parent of this single page") String parentNodeId,
			@QueryParam("position") @Parameter(description = "The node's position relative to its sibling nodes (optional)") Integer position,
			@QueryParam("shortTitle") @Parameter(description = "The node short title") String shortTitle,
			@QueryParam("longTitle") @Parameter(description = "The node long title") @DefaultValue("undefined") String longTitle,
			@QueryParam("description") @Parameter(description = "The node description") String description,
			@QueryParam("objectives") @Parameter(description = "The node learning instruction") String objectives,
			@QueryParam("instruction") @Parameter(description = "The node learning objectives") String instruction,
			@QueryParam("instructionalDesign") @Parameter(description = "The node instructional designs") String instructionalDesign,
			@QueryParam("visibilityExpertRules") @Parameter(description = "The rules to view the node (optional)") String visibilityExpertRules,
			@QueryParam("accessExpertRules") @Parameter(description = "The rules to access the node (optional)") String accessExpertRules,
			@QueryParam("moderatorExpertRules") @Parameter(description = "The rules to moderate the node (optional)") String moderatorExpertRules,
			@QueryParam("posterExpertRules") @Parameter(description = "The rules to post the node (optional)") String posterExpertRules,
			@QueryParam("readerExpertRules") @Parameter(description = "The rules to read the node (optional)") String readerExpertRules,
			@Context HttpServletRequest request) {
		ForumCustomConfig config = new ForumCustomConfig(moderatorExpertRules, posterExpertRules, readerExpertRules);
		return attach(courseId, parentNodeId, "fo", position, shortTitle, longTitle, description, objectives,
				instruction, instructionalDesign, visibilityExpertRules, accessExpertRules, config, request);
	}
	
	/**
	 * Retrieves metadata of the published course node
	 * 
	 * @param courseId The course resourceable's id
	 * @param nodeId The node's id
	 * @param httpRequest The HTTP request
	 * @return The persisted structure element (fully populated)
	 */
	@GET
	@Path("{nodeId}")
	@Operation(summary = "Retrieve metadata of the published course node",
		description = "Retrieves metadata of the published course node")
	@ApiResponse(responseCode = "200", description = "The course node metadatas",
			content = {
					@Content(mediaType = "application/json", schema = @Schema(implementation = ForumVO.class)),
					@Content(mediaType = "application/xml", schema = @Schema(implementation = ForumVO.class))
				})
	@ApiResponse(responseCode = "401", description = "The roles of the authenticated user are not sufficient")
	@ApiResponse(responseCode = "404", description = "The course or parentNode not found")
	@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	public Response getForum(@PathParam("courseId") Long courseId, @PathParam("nodeId") String nodeId, @Context HttpServletRequest httpRequest) {
		ICourse course = CoursesWebService.loadCourse(courseId);
		if(course == null) {
			return Response.serverError().status(Status.NOT_FOUND).build();
		} else if (!CourseWebService.isCourseAccessible(course, httpRequest)) {
			return Response.serverError().status(Status.UNAUTHORIZED).build();
		}

		CourseNode courseNode = course.getRunStructure().getNode(nodeId);
		if(!(courseNode instanceof FOCourseNode)) {
			return Response.serverError().status(Status.NOT_FOUND).build();
		}

		UserRequest ureq = getUserRequest(httpRequest);
		CourseTreeVisitor courseVisitor = new CourseTreeVisitor(course, ureq.getUserSession().getIdentityEnvironment());
		if(courseVisitor.isAccessible(courseNode)) {
			FOCourseNode forumNode = (FOCourseNode)courseNode;

			Set<Long> subscriptions = new HashSet<>();
			List<String> notiTypes = Collections.singletonList("Forum");
			List<Subscriber> subs = notificationsManager.getSubscribers(ureq.getIdentity(), notiTypes, true);
			for(Subscriber sub:subs) {
				Long forumKey = Long.parseLong(sub.getPublisher().getData());
				subscriptions.add(forumKey);
			}

			ForumVO forumVo = createForumVO(course, forumNode, subscriptions);
			return Response.ok(forumVo).build();
		} else {
			return Response.serverError().status(Status.UNAUTHORIZED).build();
		}
	}
	
	@Path("{nodeId}/forum")
	public ForumWebService getForumContent(@PathParam("courseId") Long courseId, @PathParam("nodeId") String nodeId, @Context HttpServletRequest request) {
		ICourse course = CoursesWebService.loadCourse(courseId);
		if(course == null) {
			throw new WebApplicationException(Response.serverError().status(Status.NOT_FOUND).build());
		}
		if (!CourseWebService.isCourseAccessible(course, request)) {
			throw new WebApplicationException(Response.serverError().status(Status.UNAUTHORIZED).build());
		}

		CourseNode courseNode = course.getRunStructure().getNode(nodeId);
		if(!(courseNode instanceof FOCourseNode)) {
			throw new WebApplicationException(Response.serverError().status(Status.NOT_FOUND).build());
		}
		
		UserRequest ureq = getUserRequest(request);
		CourseTreeVisitor courseVisitor = new CourseTreeVisitor(course, ureq.getUserSession().getIdentityEnvironment());
		if(courseVisitor.isAccessible(courseNode)) {
			FOCourseNode forumNode = (FOCourseNode)courseNode;
			Forum forum = forumNode.loadOrCreateForum(course.getCourseEnvironment());	
			ForumWebService ws = new ForumWebService(forum);
			CoreSpringFactory.autowireObject(ws);
			return ws;
		} else {
			throw new WebApplicationException(Response.serverError().status(Status.UNAUTHORIZED).build());
		}
	}
	
	private class ForumCustomConfig implements CustomConfigDelegate {
		
		private final String preConditionModerator;
		private final String preConditionPoster;
		private final String preConditionReader;
		
		public ForumCustomConfig(String preConditionModerator, String preConditionPoster, String preConditionReader) {
			this.preConditionModerator = preConditionModerator;
			this.preConditionPoster = preConditionPoster;
			this.preConditionReader = preConditionReader;
		}
		
		@Override
		public boolean isValid() {
			return true;
		}

		@Override
		public void configure(ICourse course, CourseNode newNode, ModuleConfiguration moduleConfig) {
			// create the forum
			CoursePropertyManager cpm = course.getCourseEnvironment().getCoursePropertyManager();
			Forum forum = forumManager.addAForum();
			Long forumKey = forum.getKey();
			Property forumKeyProperty = cpm.createCourseNodePropertyInstance(newNode, null, null, FOCourseNode.CONFIG_FORUM_KEY, null, forumKey, null, null);
			cpm.saveProperty(forumKeyProperty);
			
			// special rules
			if(StringHelper.containsNonWhitespace(preConditionModerator)) {
				((FOCourseNode)newNode).setPreConditionModerator(createExpertCondition("moderator", preConditionModerator));
			}
			if(StringHelper.containsNonWhitespace(preConditionPoster)) {
				((FOCourseNode)newNode).setPreConditionPoster(createExpertCondition("poster", preConditionPoster));
			}
			if(StringHelper.containsNonWhitespace(preConditionReader)) {
				((FOCourseNode)newNode).setPreConditionReader(createExpertCondition("reader", preConditionReader));
			}
		}
	}
	
	public static ForumVO createForumVO(ICourse course, FOCourseNode forumNode, Set<Long> subscribed) {
		Forum forum = forumNode.loadOrCreateForum(course.getCourseEnvironment());	
		
		ForumVO forumVo = new ForumVO();
		forumVo.setName(course.getCourseTitle());
		forumVo.setDetailsName(forumNode.getShortTitle());
		if(subscribed != null && subscribed.contains(forum.getKey())) {
			forumVo.setSubscribed(true);
		} else {
			forumVo.setSubscribed(false);
		}
		
		forumVo.setCourseKey(course.getResourceableId());
		forumVo.setCourseNodeId(forumNode.getIdent());
		forumVo.setForumKey(forum.getKey());
		
		return forumVo;
	}
}