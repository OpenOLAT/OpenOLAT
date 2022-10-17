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
package org.olat.user.restapi;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Request;
import jakarta.ws.rs.core.Response;

import org.apache.logging.log4j.Logger;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.olat.core.logging.Tracing;
import org.olat.course.CourseFactory;
import org.olat.course.ICourse;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryOrder;
import org.olat.repository.RepositoryManager;
import org.olat.restapi.support.MediaTypeVariants;
import org.olat.restapi.support.ObjectFactory;
import org.olat.restapi.support.vo.CourseVO;
import org.olat.restapi.support.vo.CourseVOes;
import org.springframework.beans.factory.annotation.Autowired;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;

/**
 * 
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class UserCoursesWebService {
	
	private static final Logger log = Tracing.createLoggerFor(UserCoursesWebService.class);
	
	private final Identity identity;
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private RepositoryManager repositoryManager;
	
	public UserCoursesWebService(Identity identity) {
		this.identity = identity;
	}
	
	/**
	 * Retrieves the list of "My entries" but limited to courses.
	 * 
	 * @param start The first result
	 * @param limit Max result
	 * @param httpRequest The HTTP request
	 * @param request The REST request
	 * @return The list of my entries
	 */
	@GET
	@Path("my")
	@Operation(summary = "Retrieves the list of \"My entries\"", description = "Retrieves the list of \"My entries\" but limited to courses")
	@ApiResponse(responseCode = "200", description = "The courses", content = {
			@Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = CourseVO.class))),
			@Content(mediaType = "application/xml", array = @ArraySchema(schema = @Schema(implementation = CourseVO.class))) })
	@ApiResponse(responseCode = "401", description = "The roles of the authenticated user are not sufficient")
	@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	public Response getMyCourses(@QueryParam("start") @DefaultValue("0") Integer start,
			@QueryParam("limit") @DefaultValue("25") Integer limit, @Context HttpServletRequest httpRequest,
			@Context Request request) {
		
		if(MediaTypeVariants.isPaged(httpRequest, request)) {
			List<RepositoryEntry> repoEntries = repositoryManager.getLearningResourcesAsStudent(identity, "CourseModule", start, limit, RepositoryEntryOrder.nameAsc);
			int totalCount= repositoryManager.countLearningResourcesAsStudent(identity, "CourseModule");

			CourseVO[] vos = toCourseVo(repoEntries);
			CourseVOes voes = new CourseVOes();
			voes.setCourses(vos);
			voes.setTotalCount(totalCount);
			return Response.ok(voes).build();
		} else {
			List<RepositoryEntry> repoEntries = repositoryManager.getLearningResourcesAsStudent(identity, "CourseModule", 0, -1, RepositoryEntryOrder.nameAsc);
			CourseVO[] vos = toCourseVo(repoEntries);
			return Response.ok(vos).build();
		}
	}

	/**
	 * Retrieves the list of "My supervised courses" but limited to courses.
	 * 
	 * @param start The first result
	 * @param limit Max result
	 * @param httpRequest The HTTP request
	 * @param request The REST request
	 * @return The list of my supervised entries
	 */
	@GET
	@Path("teached")
	@Operation(summary = "Retrieves the list of \"My supervised courses\"", description = "Retrieves the list of \"My supervised courses\" but limited to courses")
	@ApiResponse(responseCode = "200", description = "The courses", content = {
			@Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = CourseVO.class))),
			@Content(mediaType = "application/xml", array = @ArraySchema(schema = @Schema(implementation = CourseVO.class))) })
	@ApiResponse(responseCode = "401", description = "The roles of the authenticated user are not sufficient")
	@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	public Response getTeachedCourses(@QueryParam("start") @DefaultValue("0") Integer start,
			@QueryParam("limit") @DefaultValue("25") Integer limit, @Context HttpServletRequest httpRequest,
			@Context Request request) {
		
		if(MediaTypeVariants.isPaged(httpRequest, request)) {
			List<RepositoryEntry> repoEntries = repositoryManager.getLearningResourcesAsTeacher(identity, start, limit, RepositoryEntryOrder.nameAsc);
			int totalCount= repositoryManager.countLearningResourcesAsTeacher(identity);

			CourseVO[] vos = toCourseVo(repoEntries);
			CourseVOes voes = new CourseVOes();
			voes.setCourses(vos);
			voes.setTotalCount(totalCount);
			return Response.ok(voes).build();
		} else {
			List<RepositoryEntry> repoEntries = repositoryManager.getLearningResourcesAsTeacher(identity, 0, -1);
			CourseVO[] vos = toCourseVo(repoEntries);
			return Response.ok(vos).build();
		}
	}
	
    /**
     * Retrieves the list of "My owned courses" but limited to courses.
     * 
     * @param start The first result
     * @param limit Max result
     * @param httpRequest The HTTP request
     * @param request The REST request
     * @return The list of my supervised entries
     */
    @GET
    @Path("owned")
    @Operation(summary = "Retrieves the list of \"My owned courses\"", description = "Retrieves the list of \"My owned courses\" but limited to courses")
    @ApiResponse(responseCode = "200", description = "The courses", content = {
            @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = CourseVO.class))),
            @Content(mediaType = "application/xml", array = @ArraySchema(schema = @Schema(implementation = CourseVO.class))) })
    @ApiResponse(responseCode = "401", description = "The roles of the authenticated user are not sufficient")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response getOwnedCourses(@QueryParam("start") @DefaultValue("0") Integer start,
            @QueryParam("limit") @DefaultValue("25") Integer limit, @Context HttpServletRequest httpRequest,
            @Context Request request) {
        
        if(MediaTypeVariants.isPaged(httpRequest, request)) {
            List<RepositoryEntry> repoEntries = repositoryManager.getLearningResourcesAsOwner(identity, start, limit, RepositoryEntryOrder.nameAsc);
            int totalCount= repositoryManager.countLearningResourcesAsOwner(identity);

            CourseVO[] vos = toCourseVo(repoEntries);
            CourseVOes voes = new CourseVOes();
            voes.setCourses(vos);
            voes.setTotalCount(totalCount);
            return Response.ok(voes).build();
        } else {
            List<RepositoryEntry> repoEntries = repositoryManager.getLearningResourcesAsOwner(identity, 0, -1);
            CourseVO[] vos = toCourseVo(repoEntries);
            return Response.ok(vos).build();
        }
    }
	
	/**
	 * Retrieves the list of my favorite courses.
	 * 
	 * @param start The first result
	 * @param limit Max result
	 * @param httpRequest The HTTP request
	 * @param request The REST request
	 * @return The list of my favorite courses
	 */
	@GET
	@Path("favorite")
	@Operation(summary = "Retrieves the list of my favorite courses", description = "Retrieves the list of my favorite courses")
	@ApiResponse(responseCode = "200", description = "The courses", content = {
			@Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = CourseVO.class))),
			@Content(mediaType = "application/xml", array = @ArraySchema(schema = @Schema(implementation = CourseVO.class))) })
	@ApiResponse(responseCode = "401", description = "The roles of the authenticated user are not sufficient")
	@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	public Response getFavoritCourses(@QueryParam("start") @DefaultValue("0") Integer start,
			@QueryParam("limit") @DefaultValue("25") Integer limit, @Context HttpServletRequest httpRequest,
			@Context Request request) {
		
		List<String> courseType = Collections.singletonList("CourseModule");
		if(MediaTypeVariants.isPaged(httpRequest, request)) {
			List<RepositoryEntry> repoEntries = repositoryManager.getFavoritLearningResourcesAsTeacher(identity, courseType, start, limit, RepositoryEntryOrder.nameAsc);
			
			int totalCount;
			if(repoEntries.size() < limit) {
				totalCount = repoEntries.size();
			} else {
				totalCount = repositoryManager.countFavoritLearningResourcesAsTeacher(identity, courseType);
			}
			CourseVO[] vos = toCourseVo(repoEntries);
			CourseVOes voes = new CourseVOes();
			voes.setCourses(vos);
			voes.setTotalCount(totalCount);
			return Response.ok(voes).build();
		} else {
			List<RepositoryEntry> repoEntries = repositoryManager.getFavoritLearningResourcesAsTeacher(identity, courseType, 0, -1, RepositoryEntryOrder.nameAsc);
			CourseVO[] vos = toCourseVo(repoEntries);
			return Response.ok(vos).build();
		}
	}
	
	private CourseVO[] toCourseVo(List<RepositoryEntry> repoEntries) {
		List<CourseVO> voList = new ArrayList<>(repoEntries.size());

		int count=0;
		for (RepositoryEntry repoEntry : repoEntries) {
			try {
				ICourse course = CourseFactory.loadCourse(repoEntry);
				if(course != null) {
					voList.add(ObjectFactory.get(repoEntry, course));
				}
				if(count % 33 == 0) {
					dbInstance.commitAndCloseSession();
				}
			} catch (Exception e) {
				log.error("Cannot load the course with this repository entry: {}", repoEntry, e);
			}
		}
		return voList.toArray(new CourseVO[voList.size()]);
	}
}
