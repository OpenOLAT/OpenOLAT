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

import static org.olat.restapi.security.RestSecurityHelper.getIdentity;
import static org.olat.restapi.security.RestSecurityHelper.getRoles;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.apache.logging.log4j.Logger;
import org.olat.core.CoreSpringFactory;
import org.olat.core.commons.services.notifications.NotificationsManager;
import org.olat.core.commons.services.notifications.Subscriber;
import org.olat.core.id.Identity;
import org.olat.core.id.IdentityEnvironment;
import org.olat.core.id.Roles;
import org.olat.core.logging.Tracing;
import org.olat.core.util.nodes.INode;
import org.olat.core.util.tree.Visitor;
import org.olat.course.CourseFactory;
import org.olat.course.CourseModule;
import org.olat.course.ICourse;
import org.olat.course.nodes.BCCourseNode;
import org.olat.course.nodes.FOCourseNode;
import org.olat.course.nodes.bc.BCWebService;
import org.olat.course.run.userview.CourseTreeVisitor;
import org.olat.course.run.userview.VisibleTreeFilter;
import org.olat.modules.fo.restapi.ForumCourseNodeWebService;
import org.olat.modules.fo.restapi.ForumVO;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryManager;
import org.olat.repository.model.SearchRepositoryEntryParameters;
import org.olat.resource.accesscontrol.ACService;
import org.olat.resource.accesscontrol.AccessResult;
import org.olat.restapi.support.MediaTypeVariants;
import org.olat.restapi.support.vo.CourseInfoVO;
import org.olat.restapi.support.vo.CourseInfoVOes;
import org.olat.restapi.support.vo.FolderVO;
import org.springframework.stereotype.Component;

/**
 * 
 * Description:<br>
 * 
 * <P>
 * Initial Date:  7 févr. 2012 <br>
 *
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
@Component
@Path("repo/courses/infos")
public class CoursesInfosWebService {
	
	private static final Logger log = Tracing.createLoggerFor(CoursesInfosWebService.class);
	
	/**
	 * Get courses informations viewable by the authenticated user
	 * @response.representation.200.qname {http://www.example.com}courseVO
	 * @response.representation.200.mediaType application/xml, application/json, application/json;pagingspec=1.0
	 * @response.representation.200.doc List of visible courses
	 * @response.representation.200.example {@link org.olat.restapi.support.vo.Examples#SAMPLE_COURSEINFOVOes}
	 * @param start
	 * @param limit
	 * @param httpRequest The HTTP request
	 * @param request The REST request
	 * @return
	 */
	@GET
	@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	public Response getCourseInfoList(@QueryParam("start") @DefaultValue("0") Integer start,
			@QueryParam("limit") @DefaultValue("25") Integer limit, @Context HttpServletRequest httpRequest,
			@Context Request request) {
		RepositoryManager rm = RepositoryManager.getInstance();

		//fxdiff VCRP-1,2: access control of resources
		Roles roles = getRoles(httpRequest);
		Identity identity = getIdentity(httpRequest);
		SearchRepositoryEntryParameters params = new SearchRepositoryEntryParameters(identity, roles, CourseModule.getCourseTypeName());
		if(MediaTypeVariants.isPaged(httpRequest, request)) {
			int totalCount = rm.countGenericANDQueryWithRolesRestriction(params);
			List<RepositoryEntry> repoEntries = rm.genericANDQueryWithRolesRestriction(params, start, limit, true);
			List<CourseInfoVO> infos = new ArrayList<>();

			final Set<Long> forumNotified = new HashSet<>();
			final Map<Long,Set<String>> courseNotified = new HashMap<>();
			collectSubscriptions(identity, forumNotified, courseNotified);

			for(RepositoryEntry entry:repoEntries) {
				CourseInfoVO info = collect(identity, roles, entry, forumNotified, courseNotified);
				if(info != null) {
					infos.add(info);
				}
			}

			CourseInfoVO[] vos = infos.toArray(new CourseInfoVO[infos.size()]);
			CourseInfoVOes voes = new CourseInfoVOes();
			voes.setInfos(vos);
			voes.setTotalCount(totalCount);
			return Response.ok(voes).build();
		} else {
			return Response.serverError().status(Status.FORBIDDEN).build();
		}
	}
	
	/**
	 * Get course informations viewable by the authenticated user
	 * @response.representation.200.qname {http://www.example.com}courseVO
	 * @response.representation.200.mediaType application/xml, application/json
	 * @response.representation.200.doc Course informations
	 * @response.representation.200.example {@link org.olat.restapi.support.vo.Examples#SAMPLE_COURSEINFOVO}
	 * @param courseId The course id
	 * @param httpRequest The HTTP request
	 * @return
	 */
	@GET
	@Path("{courseId}")
	@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	public Response getCourseInfo(@PathParam("courseId") Long courseId,
			@Context HttpServletRequest httpRequest) {
		
		Roles roles = getRoles(httpRequest);
		Identity identity = getIdentity(httpRequest);
		if(identity != null && roles != null) {
			Set<Long> forumNotified = new HashSet<>();
			Map<Long,Set<String>> courseNotified = new HashMap<>();
			collectSubscriptions(identity, forumNotified, courseNotified);
			
			ICourse course = CourseFactory.loadCourse(courseId);
			RepositoryEntry entry = RepositoryManager.getInstance().lookupRepositoryEntry(course, true);
			
			CourseInfoVO info = collect(identity, roles, entry, forumNotified, courseNotified);
			return Response.ok(info).build();
		} else {
			return Response.serverError().status(Status.FORBIDDEN).build();
		}
	}
	
	private void collectSubscriptions(Identity identity, Set<Long> forumNotified, Map<Long,Set<String>> courseNotified) {
		NotificationsManager man = NotificationsManager.getInstance();
		{//collect subscriptions
			List<String> notiTypes = new ArrayList<>();
			notiTypes.add("FolderModule");
			notiTypes.add("Forum");
			List<Subscriber> subs = man.getSubscribers(identity, notiTypes);
			for(Subscriber sub:subs) {
				String publisherType = sub.getPublisher().getType();
				String resName = sub.getPublisher().getResName();
				
				if("CourseModule".equals(resName)) {
					if("FolderModule".equals(publisherType)) {
						Long courseKey = sub.getPublisher().getResId();
						if(!courseNotified.containsKey(courseKey)) {
							courseNotified.put(courseKey,new HashSet<String>());
						}
						courseNotified.get(courseKey).add(sub.getPublisher().getSubidentifier());
					} else if ("Forum".equals(publisherType)) {
						Long forumKey = Long.parseLong(sub.getPublisher().getData());
						forumNotified.add(forumKey);
					}
				}
			}
		}
	}
	
	private CourseInfoVO collect(final Identity identity, final Roles roles, final RepositoryEntry entry,
			final Set<Long> forumNotified, final Map<Long,Set<String>> courseNotified) {
		
		CourseInfoVO info = new CourseInfoVO();
		info.setRepoEntryKey(entry.getKey());
		info.setSoftKey(entry.getSoftkey());
		info.setDisplayName(entry.getDisplayname());

		ACService acManager = CoreSpringFactory.getImpl(ACService.class);
		AccessResult result = acManager.isAccessible(entry, identity, false);
		if(result.isAccessible()) {
			try {
				final ICourse course = CourseFactory.loadCourse(entry);
				final List<FolderVO> folders = new ArrayList<>();
				final List<ForumVO> forums = new ArrayList<>();
				final IdentityEnvironment ienv = new IdentityEnvironment(identity, roles);

				new CourseTreeVisitor(course, ienv).visit(new Visitor() {
					@Override
					public void visit(INode node) {
						if(node instanceof BCCourseNode) {
							BCCourseNode bcNode = (BCCourseNode)node;
							folders.add(BCWebService.createFolderVO(ienv, course, bcNode, courseNotified.get(course.getResourceableId())));
						} else if (node instanceof FOCourseNode) {
							FOCourseNode forumNode = (FOCourseNode)node;
							forums.add(ForumCourseNodeWebService.createForumVO(course, forumNode, forumNotified));
						}
					}
				}, new VisibleTreeFilter());
				
				info.setKey(course.getResourceableId());
				info.setTitle(course.getCourseTitle());
				info.setFolders(folders.toArray(new FolderVO[folders.size()]));
				info.setForums(forums.toArray(new ForumVO[forums.size()]));
			} catch (Exception e) {
				log.error("", e);
			}
		}
		return info;
	}
}