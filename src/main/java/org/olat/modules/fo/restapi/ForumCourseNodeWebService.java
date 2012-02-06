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
import static org.olat.restapi.security.RestSecurityHelper.isAuthor;
import static org.olat.restapi.security.RestSecurityHelper.isAuthorEditor;

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

import org.olat.basesecurity.BaseSecurity;
import org.olat.basesecurity.BaseSecurityManager;
import org.olat.core.gui.UserRequest;
import org.olat.core.id.Identity;
import org.olat.core.util.StringHelper;
import org.olat.core.util.nodes.INode;
import org.olat.core.util.notifications.NotificationsManager;
import org.olat.core.util.notifications.Subscriber;
import org.olat.course.ICourse;
import org.olat.course.Structure;
import org.olat.course.nodes.CourseNode;
import org.olat.course.nodes.FOCourseNode;
import org.olat.course.properties.CoursePropertyManager;
import org.olat.course.run.navigation.NavigationHandler;
import org.olat.course.run.userview.NodeEvaluation;
import org.olat.course.run.userview.TreeEvaluation;
import org.olat.course.run.userview.UserCourseEnvironmentImpl;
import org.olat.modules.ModuleConfiguration;
import org.olat.modules.fo.Forum;
import org.olat.modules.fo.ForumManager;
import org.olat.modules.fo.Message;
import org.olat.properties.Property;
import org.olat.restapi.repository.course.AbstractCourseNodeWebService;
import org.olat.restapi.security.RestSecurityHelper;

/**
 * 
 * Description:<br>
 * REST API implementation for forum course node 
 * 
 * <P>
 * Initial Date:  20.12.2010 <br>
 * @author skoeber
 */
@Path("repo/courses/{courseId}/elements/forum")
public class ForumCourseNodeWebService extends AbstractCourseNodeWebService {
	
	/**
	 * Retrieves metadata of the published course node
	 * @response.representation.200.qname {http://www.example.com}forumVOes
   * @response.representation.200.mediaType application/xml, application/json
   * @response.representation.200.doc The course node metadatas
   * @response.representation.200.example {@link org.olat.modules.fo.restapi.Examples#SAMPLE_FORUMVOes}
	 * @response.representation.401.doc The roles of the authenticated user are not sufficient
   * @response.representation.404.doc The course or parentNode not found
	 * @param courseId The course resourceable's id
	 * @param httpRequest The HTTP request
	 * @return The persisted structure element (fully populated)
	 */
	@GET
	@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	public Response getForums(@PathParam("courseId") Long courseId, @Context HttpServletRequest httpRequest) {
		
		ICourse course = loadCourse(courseId);
		if(course == null) {
			return Response.serverError().status(Status.NOT_FOUND).build();
		}

		UserRequest ureq = getUserRequest(httpRequest);
		UserCourseEnvironmentImpl uce = new UserCourseEnvironmentImpl(ureq.getUserSession().getIdentityEnvironment(), course.getCourseEnvironment());

		Set<Long> subcribedForums = new HashSet<Long>();
		NotificationsManager man = NotificationsManager.getInstance();
		List<String> notiTypes = Collections.singletonList("Forum");
		List<Subscriber> subs = man.getSubscribers(ureq.getIdentity(), notiTypes);
		for(Subscriber sub:subs) {
			Long forumKey = Long.parseLong(sub.getPublisher().getData());
			subcribedForums.add(forumKey);
		}
		
		List<ForumVO> forumVOs = new ArrayList<ForumVO>();
		List<FOCourseNode> forumNodes = getFOCourseNodes(course);
		for(FOCourseNode forumNode:forumNodes) {
			NodeEvaluation ne = forumNode.eval(uce.getConditionInterpreter(), new TreeEvaluation());

			boolean mayAccessWholeTreeUp = NavigationHandler.mayAccessWholeTreeUp(ne);
			if(mayAccessWholeTreeUp) {
				Forum forum = forumNode.loadOrCreateForum(course.getCourseEnvironment());	
				ForumVO forumVo = new ForumVO(forum);
				forumVo.setName(course.getCourseTitle());
				forumVo.setDetailsName(forumNode.getShortTitle());
				forumVo.setSubscribed(subcribedForums.contains(forum.getKey()));
				forumVo.setForumKey(forum.getKey());
				forumVo.setCourseKey(course.getResourceableId());
				forumVo.setCourseNodeId(forumNode.getIdent());
				forumVOs.add(forumVo);
			}
		}
		
		ForumVOes voes = new ForumVOes();
		voes.setForums(forumVOs.toArray(new ForumVO[forumVOs.size()]));
		voes.setTotalCount(forumVOs.size());
		return Response.ok(voes).build();
	}

	/**
	 * This attaches a Forum Element onto a given course. The element will be
	 * inserted underneath the supplied parentNodeId.
	 * @response.representation.200.qname {http://www.example.com}courseNodeVO
   * @response.representation.200.mediaType application/xml, application/json
   * @response.representation.200.doc The course node metadatas
   * @response.representation.200.example {@link org.olat.restapi.support.vo.Examples#SAMPLE_COURSENODEVO}
	 * @response.representation.401.doc The roles of the authenticated user are not sufficient
   * @response.representation.404.doc The course or parentNode not found
	 * @param courseId The course resourceable's id
	 * @param parentNodeId The node's id which will be the parent of this single
	 *          page
	 * @param position The node's position relative to its sibling nodes (optional)
	 * @param shortTitle The node short title
	 * @param longTitle The node long title
	 * @param objectives The node learning objectives
	 * @param visibilityExpertRules The rules to view the node (optional)
	 * @param accessExpertRules The rules to access the node (optional)
	 * @param request The HTTP request
	 * @return The persisted Forum Element (fully populated)
	 */
	@POST
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	public Response attachForumPost(@PathParam("courseId") Long courseId, @FormParam("parentNodeId") String parentNodeId,
			@FormParam("position") Integer position, @FormParam("shortTitle") @DefaultValue("undefined") String shortTitle,
			@FormParam("longTitle") @DefaultValue("undefined") String longTitle, @FormParam("objectives") @DefaultValue("undefined") String objectives,
			@FormParam("visibilityExpertRules") String visibilityExpertRules, @FormParam("accessExpertRules") String accessExpertRules,
			@FormParam("moderatorExpertRules") String moderatorExpertRules, @FormParam("posterExpertRules") String posterExpertRules,
			@FormParam("readerExpertRules") String readerExpertRules, @Context HttpServletRequest request) {
		return attachForum(courseId, parentNodeId, position, shortTitle, longTitle, objectives, visibilityExpertRules, accessExpertRules,
				moderatorExpertRules, posterExpertRules, readerExpertRules, request);
	}
	
	/**
	 * This attaches a Forum Element onto a given course. The element will be
	 * inserted underneath the supplied parentNodeId.
	 * @response.representation.200.qname {http://www.example.com}courseNodeVO
   * @response.representation.200.mediaType application/xml, application/json
   * @response.representation.200.doc The course node metadatas
   * @response.representation.200.example {@link org.olat.restapi.support.vo.Examples#SAMPLE_COURSENODEVO}
	 * @response.representation.401.doc The roles of the authenticated user are not sufficient
   * @response.representation.404.doc The course or parentNode not found
	 * @param courseId The course resourceable id
	 * @param parentNodeId The node's id which will be the parent of this single
	 *          page
	 * @param position The node's position relative to its sibling nodes (optional)
	 * @param shortTitle The node short title
	 * @param longTitle The node long title
	 * @param objectives The node learning objectives
	 * @param visibilityExpertRules The rules to view the node (optional)
	 * @param accessExpertRules The rules to access the node (optional)
	 * @param request The HTTP request
	 * @return The persisted Forum Element (fully populated)
	 */
	@PUT
	@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	public Response attachForum(@PathParam("courseId") Long courseId, @QueryParam("parentNodeId") String parentNodeId,
			@QueryParam("position") Integer position, @QueryParam("shortTitle") @DefaultValue("undefined") String shortTitle,
			@QueryParam("longTitle") @DefaultValue("undefined") String longTitle, @QueryParam("objectives") @DefaultValue("undefined") String objectives,
			@QueryParam("visibilityExpertRules") String visibilityExpertRules, @QueryParam("accessExpertRules") String accessExpertRules,
			@QueryParam("moderatorExpertRules") String moderatorExpertRules, @QueryParam("posterExpertRules") String posterExpertRules,
			@QueryParam("readerExpertRules") String readerExpertRules, @Context HttpServletRequest request) {
		ForumCustomConfig config = new ForumCustomConfig(moderatorExpertRules, posterExpertRules, readerExpertRules);
		return attach(courseId, parentNodeId, "fo", position, shortTitle, longTitle, objectives, visibilityExpertRules, accessExpertRules, config, request);
	}
	
	/**
	 * Retrieves metadata of the published course node
	 * @response.representation.200.qname {http://www.example.com}forumVO
   * @response.representation.200.mediaType application/xml, application/json
   * @response.representation.200.doc The course node metadatas
   * @response.representation.200.example {@link org.olat.modules.fo.restapi.Examples#SAMPLE_FORUMVO}
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
	public Response getForum(@PathParam("courseId") Long courseId, @PathParam("nodeId") String nodeId, @Context HttpServletRequest httpRequest) {
		ICourse course = loadCourse(courseId);
		if(course == null) {
			return Response.serverError().status(Status.NOT_FOUND).build();
		}

		CourseNode courseNode = course.getRunStructure().getNode(nodeId);
		if(courseNode == null || !(courseNode instanceof FOCourseNode)) {
			return Response.serverError().status(Status.NOT_FOUND).build();
		}

		UserRequest ureq = getUserRequest(httpRequest);
		FOCourseNode forumNode = (FOCourseNode)courseNode;
		UserCourseEnvironmentImpl uce = new UserCourseEnvironmentImpl(ureq.getUserSession().getIdentityEnvironment(), course.getCourseEnvironment());
		NodeEvaluation ne = forumNode.eval(uce.getConditionInterpreter(), new TreeEvaluation());

		boolean mayAccessWholeTreeUp = NavigationHandler.mayAccessWholeTreeUp(ne);
		if(mayAccessWholeTreeUp) {
			Forum forum = forumNode.loadOrCreateForum(course.getCourseEnvironment());	
			
			ForumVO forumVo = new ForumVO();
			forumVo.setName(course.getCourseTitle());
			forumVo.setDetailsName(forumNode.getShortTitle());

			boolean subscribed = false;
			NotificationsManager man = NotificationsManager.getInstance();
			List<String> notiTypes = Collections.singletonList("Forum");
			List<Subscriber> subs = man.getSubscribers(ureq.getIdentity(), notiTypes);
			for(Subscriber sub:subs) {
				Long forumKey = Long.parseLong(sub.getPublisher().getData());
				if(forumKey.equals(forum.getKey())) {
					subscribed = true;
					break;
				}
			}
			
			forumVo.setSubscribed(subscribed);
			forumVo.setCourseKey(course.getResourceableId());
			forumVo.setCourseNodeId(forumNode.getIdent());
			return Response.ok(forumVo).build();
		} else {
			return Response.serverError().status(Status.UNAUTHORIZED).build();
		}
	}
	
	@Path("{nodeId}/forum")
	public ForumWebService getForumContent(@PathParam("courseId") Long courseId, @PathParam("nodeId") String nodeId, @Context HttpServletRequest request) {
		ICourse course = loadCourse(courseId);
		if(course == null) {
			throw new WebApplicationException(Response.serverError().status(Status.NOT_FOUND).build());
		}

		CourseNode courseNode = course.getRunStructure().getNode(nodeId);
		if(courseNode == null || !(courseNode instanceof FOCourseNode)) {
			throw new WebApplicationException(Response.serverError().status(Status.NOT_FOUND).build());
		}

		UserRequest ureq = getUserRequest(request);
		FOCourseNode forumNode = (FOCourseNode)courseNode;
		UserCourseEnvironmentImpl uce = new UserCourseEnvironmentImpl(ureq.getUserSession().getIdentityEnvironment(), course.getCourseEnvironment());
		NodeEvaluation ne = forumNode.eval(uce.getConditionInterpreter(), new TreeEvaluation());

		boolean mayAccessWholeTreeUp = NavigationHandler.mayAccessWholeTreeUp(ne);
		if(mayAccessWholeTreeUp) {
			Forum forum = forumNode.loadOrCreateForum(course.getCourseEnvironment());	
			return new ForumWebService(forum);
		} else {
			throw new WebApplicationException(Response.serverError().status(Status.UNAUTHORIZED).build());
		}
	}
	
	/**
	 * Creates a new thread in the forum of the course node
	 * @response.representation.200.qname {http://www.example.com}messageVO
   * @response.representation.200.mediaType application/xml, application/json
   * @response.representation.200.doc The root message of the thread
   * @response.representation.200.example {@link org.olat.modules.fo.restapi.Examples#SAMPLE_MESSAGEVO}
	 * @response.representation.401.doc The roles of the authenticated user are not sufficient
   * @response.representation.404.doc The author, forum or message not found
   * @param courseId The id of the course.
   * @param nodeId The id of the course node.
	 * @param title The title for the first post in the thread
	 * @param body The body for the first post in the thread
	 * @param identityName The author identity name (optional)
	 * @param sticky Creates sticky thread.
	 * @param request The HTTP request
	 * @return The new thread
	 * 
	 * @deprecated use the {nodeId}/forum/threads instead
	 */
	@PUT
	@Path("{nodeId}/thread")
	@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	public Response newThreadToForum(@PathParam("courseId") Long courseId, @PathParam("nodeId") String nodeId, @QueryParam("title") String title,
			@QueryParam("body") String body, @QueryParam("identityName") String identityName, @QueryParam("sticky") Boolean isSticky,
			@Context HttpServletRequest request) {
		
		return addMessage(courseId, nodeId, null, title, body, identityName, isSticky, request);
	}
	
	/**
	 * Creates a new forum message in the forum of the course node
	 * @response.representation.200.qname {http://www.example.com}messageVO
   * @response.representation.200.mediaType application/xml, application/json
   * @response.representation.200.doc The root message of the thread
   * @response.representation.200.example {@link org.olat.modules.fo.restapi.Examples#SAMPLE_MESSAGEVO}
	 * @response.representation.401.doc The roles of the authenticated user are not sufficient
   * @response.representation.404.doc The author, forum or message not found
   * @param courseId The id of the course.
   * @param nodeId The id of the course node.
   * @param parentMessageId The id of the parent message.
	 * @param title The title for the first post in the thread
	 * @param body The body for the first post in the thread
	 * @param identityName The author identity name (optional)
	 * @param request The HTTP request
	 * @return The new thread
	 * 
	 * @deprecated use the {nodeId}/forum/messages instead
	 */
	@PUT
	@Path("{nodeId}/message")
	@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	public Response newMessageToForum(@PathParam("courseId") Long courseId, @PathParam("nodeId") String nodeId, @QueryParam("parentMessageId") Long parentMessageId, @QueryParam("title") String title,
			@QueryParam("body") String body, @QueryParam("identityName") String identityName, @Context HttpServletRequest request) {
		
		if(parentMessageId == null || parentMessageId == 0L) {
			return Response.serverError().status(Status.NOT_FOUND).build();
		}
		
		return addMessage(courseId, nodeId, parentMessageId, title, body, identityName, false, request);
	}
	
	/**
	 * Internal helper method to add a message to a forum.
	 * @param courseId
	 * @param nodeId
	 * @param parentMessageId can be null (will lead to new thread)
	 * @param title
	 * @param body
	 * @param identityName
	 * @param isSticky only necessary when adding new thread
	 * @param request
	 * @return
	 */
	private Response addMessage(Long courseId, String nodeId, Long parentMessageId, String title, String body, String identityName, Boolean isSticky, HttpServletRequest request) {
		if(!isAuthor(request)) {
			return Response.serverError().status(Status.UNAUTHORIZED).build();
		}
		
		BaseSecurity securityManager = BaseSecurityManager.getInstance();
		
		Identity identity;
		if (identityName != null) {
			identity = securityManager.findIdentityByName(identityName);
		} else {
			identity = RestSecurityHelper.getIdentity(request);
		}
		
		if(identity == null) {
			return Response.serverError().status(Status.NOT_FOUND).build();
		}
		
		//load forum
		ICourse course = loadCourse(courseId);
		if(course == null) {
			return Response.serverError().status(Status.NOT_FOUND).build();
		} else if (!isAuthorEditor(course, request)) {
			return Response.serverError().status(Status.UNAUTHORIZED).build();
		}
		
		CourseNode courseNode = getParentNode(course, nodeId);
		if(courseNode == null) {
			return Response.serverError().status(Status.NOT_FOUND).build();
		}
		
		CoursePropertyManager cpm = course.getCourseEnvironment().getCoursePropertyManager();
		Property forumKeyProp = cpm.findCourseNodeProperty(courseNode, null, null, FOCourseNode.FORUM_KEY);
		Forum forum = null;
		ForumManager fom = ForumManager.getInstance();
		if(forumKeyProp!=null) {
      // Forum does already exist, load forum with key from properties
		  Long forumKey = forumKeyProp.getLongValue();
		  forum = fom.loadForum(forumKey);
		}
		
		if(forum == null) {
			return Response.serverError().status(Status.NOT_FOUND).build();
		}
		
		MessageVO vo;
		
		if(parentMessageId == null || parentMessageId == 0L) {
			// creating the thread (a message without a parent message)
			Message newThread = fom.createMessage();
			if (isSticky != null && isSticky.booleanValue()) {
				// set sticky
				org.olat.modules.fo.Status status = new org.olat.modules.fo.Status();
				status.setSticky(true);
				newThread.setStatusCode(org.olat.modules.fo.Status.getStatusCode(status));
			}
			newThread.setTitle(title);
			newThread.setBody(body);
			// open a new thread
			fom.addTopMessage(identity, forum, newThread);
			
			vo = new MessageVO(newThread);
		} else {
			// adding response message (a message with a parent message)
			Message threadMessage = fom.loadMessage(parentMessageId);
			
			if(threadMessage == null) {
				return Response.serverError().status(Status.NOT_FOUND).build();
			}
			// create new message
			Message message = fom.createMessage();
			message.setTitle(title);
			message.setBody(body);
			
			fom.replyToMessage(message, identity, threadMessage);
			
			vo = new MessageVO(message);
		}
		
		return Response.ok(vo).build();
	}
	//fxdiff: RESTAPI add special expert rules for forums
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
			ForumManager fom = ForumManager.getInstance();
			CoursePropertyManager cpm = course.getCourseEnvironment().getCoursePropertyManager();
			Forum forum = fom.addAForum();
			Long forumKey = forum.getKey();
			Property forumKeyProperty = cpm.createCourseNodePropertyInstance(newNode, null, null, FOCourseNode.FORUM_KEY, null, forumKey, null, null);
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
	
	private List<FOCourseNode> getFOCourseNodes(ICourse course) {
		List<FOCourseNode> bcNodes = new ArrayList<FOCourseNode>();
		Structure courseStruct = course.getRunStructure();
		getFOCourseNodes(courseStruct.getRootNode(), bcNodes);
		return bcNodes;
	}

	/**
	 * Recursive step used by <code>getBCCourseNodes(ICourse)</code>.<br>
	 * <br>
	 * <b>PRE CONDITIONS</b>
	 * <ul>
	 * <li> <code>course != null</code>
	 * <li> <code>result != null</code>
	 * </ul>
	 * 
	 * @see #getBCCourseNodes(ICourse)
	 */
	private void getFOCourseNodes(INode node, List<FOCourseNode> result) {
		if (node != null) {
			if (node instanceof FOCourseNode) {
				result.add((FOCourseNode) node);
			}

			for (int i = 0; i < node.getChildCount(); i++) {
				getFOCourseNodes(node.getChildAt(i), result);
			}
		}
	}
}