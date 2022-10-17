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
package org.olat.course.nodes.en;

import java.util.ArrayList;
import java.util.List;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.FormParam;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;

import org.olat.core.util.StringHelper;
import org.olat.course.ICourse;
import org.olat.course.nodes.CourseNode;
import org.olat.course.nodes.ENCourseNode;
import org.olat.group.BusinessGroup;
import org.olat.group.BusinessGroupService;
import org.olat.group.BusinessGroupShort;
import org.olat.modules.ModuleConfiguration;
import org.olat.restapi.repository.course.AbstractCourseNodeWebService;
import org.olat.restapi.repository.course.CoursesWebService;
import org.olat.restapi.support.vo.CourseNodeVO;
import org.olat.restapi.support.vo.GroupVO;
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
 * This handles the enrollment building block.
 * 
 * <P>
 * Initial Date:  10 mai 2010 <br>
 * @author srosse, stephane.rosse@frentix.com
 */
@Tag(name = "Repo")
@Component
@Path("repo/courses/{courseId}/elements/enrollment")
public class ENWebService extends AbstractCourseNodeWebService {
	
	@Autowired
	private BusinessGroupService businessGroupService;

	/**
	 * This attaches an enrollment element onto a given course, the element will be
	 * inserted underneath the supplied parentNodeId
	 * 
	 * @param courseId The course resourceable's id
	 * @param parentNodeId The node's id which will be the parent of this structure
	 * @param position The node's position relative to its sibling nodes (optional)
	 * @param shortTitle The node short title
	 * @param longTitle The node long title
	 * @param description The node description
	 * @param objectives The node learning objectives
	 * @param instruction The node instruction
	 * @param instructionalDesign The node instructional design
	 * @param visibilityExpertRules The rules to view the node (optional)
	 * @param accessExpertRules The rules to access the node (optional)
	 * @param groups A list of learning groups (list of keys)
	 * @param cancelEnabled cancel enrollment enabled or not
	 * @param request The HTTP request
	 * @return The persisted contact element (fully populated)
	 */
	@PUT
	@Operation(summary = "attach an enrollment element onto a given course",
		description = "This attaches a contact element onto a given course, the element will be\n" + 
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
	public Response attachEnrolmment(@PathParam("courseId") Long courseId,
			@QueryParam("parentNodeId") @Parameter(description = "The node's id which will be the parent of this structure") String parentNodeId,
			@QueryParam("position") @Parameter(description = "The node's position relative to its sibling nodes (optional)") Integer position,
			@QueryParam("shortTitle") @Parameter(description = "The node short title") String shortTitle,
			@QueryParam("longTitle") @Parameter(description = "The node long title") @DefaultValue("undefined") String longTitle,
			@QueryParam("description") @Parameter(description = "The node description") String description,
			@QueryParam("objectives") @Parameter(description = "The node learning instruction") String objectives,
			@QueryParam("instruction") @Parameter(description = "The node learning objectives") String instruction,
			@QueryParam("instructionalDesign") @Parameter(description = "The node instructional designs") String instructionalDesign,
			@QueryParam("visibilityExpertRules") @Parameter(description = "The rules to view the node (optional)") String visibilityExpertRules,
			@QueryParam("accessExpertRules") @Parameter(description = "The rules to access the node (optional)") String accessExpertRules,
			@QueryParam("groups") @Parameter(description = "A list of learning groups (list of keys)") String groups,
			@QueryParam("cancelEnabled") @Parameter(description = "cancel enrollment enabled or not") @DefaultValue("false") boolean cancelEnabled,
			@Context HttpServletRequest request) {
		
		EnrollmentConfigDelegate config = new EnrollmentConfigDelegate(groups, cancelEnabled);
		return attach(courseId, parentNodeId, "en", position, shortTitle, longTitle, description, objectives,
				instruction, instructionalDesign, visibilityExpertRules, accessExpertRules, config, request);
	}
	
	/**
	 * This attaches an enrollment element onto a given course, the element will be
	 * inserted underneath the supplied parentNodeId
	 * 
	 * @param courseId The course resourceable's id
	 * @param parentNodeId The node's id which will be the parent of this
	 *          structure
	 * @param position The node's position relative to its sibling nodes (optional)
	 * @param shortTitle The node short title
	 * @param longTitle The node long title
	 * @param description The node description
	 * @param objectives The node learning objectives
	 * @param instruction The node instruction
	 * @param instructionalDesign The node instructional desig
	 * @param visibilityExpertRules The rules to view the node (optional)
	 * @param accessExpertRules The rules to access the node (optional)
	 * @param groups send the message to the specified groups
	 * @param cancelEnabled cancel enrollment enabled or not
	 * @param request The HTTP request
	 * @return The persisted contact element (fully populated)
	 */
	@POST
	@Operation(summary = "attach an enrollment element onto a given course",
		description = "This attaches a contact element onto a given course, the element will be\n" + 
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
	public Response attachEnrollmenetPost(@PathParam("courseId") Long courseId,
			@FormParam("parentNodeId") String parentNodeId, @FormParam("position") Integer position,
			@FormParam("shortTitle") String shortTitle,
			@FormParam("longTitle") String longTitle, @FormParam("description") String description,
			@FormParam("objectives") String objectives, @FormParam("instruction") String instruction,
			@FormParam("instructionalDesign") String instructionalDesign,
			@FormParam("visibilityExpertRules") String visibilityExpertRules,
			@FormParam("accessExpertRules") String accessExpertRules, @FormParam("groups") String groups,
			@FormParam("cancelEnabled") @DefaultValue("false") boolean cancelEnabled,
			@Context HttpServletRequest request) {
		EnrollmentConfigDelegate config = new EnrollmentConfigDelegate(groups, cancelEnabled);
		return attach(courseId, parentNodeId, "en", position, shortTitle, longTitle, description, objectives,
				instruction, instructionalDesign, visibilityExpertRules, accessExpertRules, config, request);
	}
	
	/**
	 * Retrieves the groups where the enrollment happens
	 * 
	 * @param nodeId The node's id
	 * @param httpRequest The HTTP request
	 * @return An array of groups
	 */
	@GET
	@Path("{nodeId}/groups")
	@Operation(summary = "Retrieves the groups where the enrollment happens",
		description = "Retrieves the groups where the enrollment happens")
	@ApiResponse(responseCode = "200", description = "Retrieves the groups where the enrollment happens",
			content = {
					@Content(mediaType = "application/json", schema = @Schema(implementation = GroupVO.class)),
					@Content(mediaType = "application/xml", schema = @Schema(implementation = GroupVO.class))
				})
	@ApiResponse(responseCode = "401", description = "The roles of the authenticated user are not sufficient")
	@ApiResponse(responseCode = "404", description = "The course or parentNode not found")
	@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	public Response getGroups(@PathParam("courseId") Long courseId, @PathParam("nodeId") String nodeId, @Context HttpServletRequest httpRequest) {
		ICourse course = CoursesWebService.loadCourse(courseId);
		if(course == null) {
			return Response.serverError().status(Status.NOT_FOUND).build();
		}
		if (!isAuthorEditor(course, httpRequest)) {
			return Response.serverError().status(Status.UNAUTHORIZED).build();
		}

		CourseNode node = getParentNode(course, nodeId).getCourseNode();
		ModuleConfiguration config = node.getModuleConfiguration();
		String groupNames = (String)config.get(ENCourseNode.CONFIG_GROUPNAME);
		@SuppressWarnings("unchecked")
		List<Long> groupKeys = (List<Long>)config.get(ENCourseNode.CONFIG_GROUP_IDS);
		if(groupKeys == null && StringHelper.containsNonWhitespace(groupNames)) {
			groupKeys = businessGroupService.toGroupKeys(groupNames, course.getCourseEnvironment().getCourseGroupManager().getCourseEntry()); 
		}
		
		if(groupKeys == null || groupKeys.isEmpty()) {
			return Response.ok(new GroupVO[0]).build();
		}

		List<GroupVO> voes = new ArrayList<>();
		List<BusinessGroup> groups = businessGroupService.loadBusinessGroups(groupKeys);
		for(BusinessGroup group:groups) {
			voes.add(GroupVO.valueOf(group));
		}
		GroupVO[] voArr = new GroupVO[voes.size()];
		voes.toArray(voArr);
		return Response.ok(voArr).build();
	}
	
	private class EnrollmentConfigDelegate implements CustomConfigDelegate {
		private final boolean cancelEnabled;
		private final List<String> groups;
		
		public EnrollmentConfigDelegate(String groups, boolean cancelEnabled) {
			this.groups = getGroupNames(groups);
			this.cancelEnabled = cancelEnabled;
		}
		
		@Override
		public boolean isValid() {
			return groups != null && !groups.isEmpty();
		}

		@Override
		public void configure(ICourse course, CourseNode newNode, ModuleConfiguration moduleConfig) {
			moduleConfig.set(ENCourseNode.CONFIG_GROUPNAME, getGroupNamesToString());
			moduleConfig.set(ENCourseNode.CONF_CANCEL_ENROLL_ENABLED, cancelEnabled);
		}
		
		
		private String getGroupNamesToString() {
			StringBuilder buffer = new StringBuilder();
			for(String groupName:groups) {
				if(buffer.length() > 0) {
					buffer.append(',');
				}
				buffer.append(groupName);
			}
			return buffer.toString();
		}
		
		private List<String> getGroupNames(String groupIds) {
			List<String> groupNames = new ArrayList<>();
			
			if(StringHelper.containsNonWhitespace(groupIds)) {
				String[] groupIdArr = groupIds.split(";");
			
				List<Long> keys = new ArrayList<>();
				for(String groupId:groupIdArr) {
					Long groupKey = Long.valueOf(groupId);
					keys.add(groupKey);
				}
				List<BusinessGroupShort> groupsShort = businessGroupService.loadShortBusinessGroups(keys);
				for(BusinessGroupShort bg:groupsShort) {
					groupNames.add(bg.getName());
				}
			}
			
			return groupNames;
		}
	}
}
