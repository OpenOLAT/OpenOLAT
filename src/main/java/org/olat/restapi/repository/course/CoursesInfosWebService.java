package org.olat.restapi.repository.course;

import static org.olat.restapi.security.RestSecurityHelper.getIdentity;
import static org.olat.restapi.security.RestSecurityHelper.getRoles;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.olat.core.CoreSpringFactory;
import org.olat.core.id.Identity;
import org.olat.core.id.IdentityEnvironment;
import org.olat.core.id.Roles;
import org.olat.core.util.nodes.INode;
import org.olat.core.util.tree.Visitor;
import org.olat.course.CourseFactory;
import org.olat.course.CourseModule;
import org.olat.course.ICourse;
import org.olat.course.nodes.BCCourseNode;
import org.olat.course.nodes.FOCourseNode;
import org.olat.course.nodes.bc.BCWebService;
import org.olat.course.run.userview.CourseTreeVisitor;
import org.olat.modules.fo.restapi.ForumCourseNodeWebService;
import org.olat.modules.fo.restapi.ForumVO;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryManager;
import org.olat.repository.SearchRepositoryEntryParameters;
import org.olat.resource.accesscontrol.AccessResult;
import org.olat.resource.accesscontrol.manager.ACFrontendManager;
import org.olat.restapi.support.MediaTypeVariants;
import org.olat.restapi.support.vo.CourseInfoVO;
import org.olat.restapi.support.vo.CourseInfoVOes;
import org.olat.restapi.support.vo.FolderVO;

@Path("repo/courses/infos")
public class CoursesInfosWebService {
	
	/**
	 * Get courses informations viewable by the authenticated user
	 * @response.representation.200.qname {http://www.example.com}courseVO
	 * @response.representation.200.mediaType application/xml, application/json, application/json;pagingspec=1.0
	 * @response.representation.200.doc List of visible courses
	 * @response.representation.200.example {@link org.olat.restapi.support.vo.Examples#SAMPLE_COURSEVOes}
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
			int totalCount = rm.countGenericANDQueryWithRolesRestriction(params, true);
			List<RepositoryEntry> repoEntries = rm.genericANDQueryWithRolesRestriction(params, start, limit, true);
			List<CourseInfoVO> infos = new ArrayList<CourseInfoVO>();

			for(RepositoryEntry entry:repoEntries) {
				CourseInfoVO info = collect(identity, roles, entry);
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
	
	private CourseInfoVO collect(Identity identity, Roles roles, RepositoryEntry entry) {
		CourseInfoVO info = new CourseInfoVO();
		info.setRepoEntryKey(entry.getKey());
		info.setSoftKey(entry.getSoftkey());
		info.setDisplayName(entry.getDisplayname());

		ACFrontendManager acManager = CoreSpringFactory.getImpl(ACFrontendManager.class);
		AccessResult result = acManager.isAccessible(entry, identity, false);
		if(result.isAccessible()) {
			final ICourse course = CourseFactory.loadCourse(entry.getOlatResource());
			final List<FolderVO> folders = new ArrayList<FolderVO>();
			final List<ForumVO> forums = new ArrayList<ForumVO>();
			final IdentityEnvironment ienv = new IdentityEnvironment(identity, roles);

			new CourseTreeVisitor(course, ienv).visit(new Visitor() {
				@Override
				public void visit(INode node) {
					if(node instanceof BCCourseNode) {
						BCCourseNode bcNode = (BCCourseNode)node;
						folders.add(BCWebService.createFolderVO(ienv, course, bcNode, false));
					} else if (node instanceof FOCourseNode) {
						FOCourseNode forumNode = (FOCourseNode)node;
						forums.add(ForumCourseNodeWebService.createForumVO(course, forumNode, null));
					}
				}
			});
			
			info.setKey(course.getResourceableId());
			info.setTitle(course.getCourseTitle());
			info.setFolders(folders.toArray(new FolderVO[folders.size()]));
			info.setForums(forums.toArray(new ForumVO[forums.size()]));
		}
		return info;
	}

}
