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
package org.olat.course.nodes.co;

// depricated configvalues for configversion 2
import static org.olat.course.nodes.co.COEditController.CONFIG_KEY_EMAILTOADRESSES;
import static org.olat.course.nodes.co.COEditController.CONFIG_KEY_EMAILTOAREAS;
import static org.olat.course.nodes.co.COEditController.CONFIG_KEY_EMAILTOCOACHES;
// values for configversion 3
import static org.olat.course.nodes.co.COEditController.CONFIG_KEY_EMAILTOCOACHES_ALL;
import static org.olat.course.nodes.co.COEditController.CONFIG_KEY_EMAILTOCOACHES_AREA;
import static org.olat.course.nodes.co.COEditController.CONFIG_KEY_EMAILTOCOACHES_COURSE;
import static org.olat.course.nodes.co.COEditController.CONFIG_KEY_EMAILTOCOACHES_GROUP;
import static org.olat.course.nodes.co.COEditController.CONFIG_KEY_EMAILTOGROUPS;
import static org.olat.course.nodes.co.COEditController.CONFIG_KEY_EMAILTOPARTICIPANTS;
import static org.olat.course.nodes.co.COEditController.CONFIG_KEY_EMAILTOPARTICIPANTS_ALL;
import static org.olat.course.nodes.co.COEditController.CONFIG_KEY_EMAILTOPARTICIPANTS_AREA;
import static org.olat.course.nodes.co.COEditController.CONFIG_KEY_EMAILTOPARTICIPANTS_COURSE;
import static org.olat.course.nodes.co.COEditController.CONFIG_KEY_EMAILTOPARTICIPANTS_GROUP;
import static org.olat.course.nodes.co.COEditController.CONFIG_KEY_MBODY_DEFAULT;
import static org.olat.course.nodes.co.COEditController.CONFIG_KEY_MSUBJECT_DEFAULT;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.FormParam;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.olat.core.CoreSpringFactory;
import org.olat.core.util.StringHelper;
import org.olat.core.util.mail.MailHelper;
import org.olat.course.ICourse;
import org.olat.course.nodes.CourseNode;
import org.olat.group.BusinessGroupService;
import org.olat.group.BusinessGroupShort;
import org.olat.modules.ModuleConfiguration;
import org.olat.restapi.repository.course.AbstractCourseNodeWebService;
import org.olat.restapi.support.vo.CourseNodeVO;
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
 * This handles the contact building block.
 * 
 * <P>
 * Initial Date:  10 mai 2010 <br>
 * @author srosse, stephane.rosse@frentix.com
 * @author Dirk Furrer
 */
@Tag(name = "Repo")
@Component
@Path("repo/courses/{courseId}/elements/contact")
public class COWebService extends AbstractCourseNodeWebService {
	
	/**
	 * This attaches a contact element onto a given course, the element will be
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
	 * @param coaches Send to coaches (true/false)
	 * @param participants Send to participants (true/false)
	 * @param groups A list of learning groups (list of keys)
	 * @param areas A list of learning areas (list of keys)
	 * @param to The list of e-mail address
	 * @param defaultSubject The default subject
	 * @param defaultBody The default body text
	 * @param request The HTTP request
	 * @return The persisted contact element (fully populated)
	 */
	@PUT
	@Operation(summary = "attach a contact element onto a given course",
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
	public Response attachContact(@PathParam("courseId") Long courseId,
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
			@QueryParam("coaches") @Parameter(description = "Send to coaches (true/false)") @DefaultValue("false") boolean coaches,
			@QueryParam("participants") @Parameter(description = "Send to participants (true/false)") @DefaultValue("false") boolean participants,
			@QueryParam("groups") @Parameter(description = "A list of learning groups (list of keys)") String groups,
			@QueryParam("areas") @Parameter(description = "A list of learning areas (list of keys)") String areas,
			@QueryParam("to") String to,
			@QueryParam("defaultSubject") @Parameter(description = "The default subject") String defaultSubject,
			@QueryParam("defaultBody") @Parameter(description = "The default body text") String defaultBody,
			@Context HttpServletRequest request) {
		
		ContactConfigDelegate config = new ContactConfigDelegate(coaches, participants, groups, areas, to, defaultSubject, defaultBody);
		return attach(courseId, parentNodeId, "co", position, shortTitle, longTitle, description, objectives,
				instruction, instructionalDesign, visibilityExpertRules, accessExpertRules, config, request);
	}
	
	/**
	 * This attaches a contact element onto a given course, the element will be
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
	 * @param instructionalDesign The node instructional design
	 * @param visibilityExpertRules The rules to view the node (optional)
	 * @param accessExpertRules The rules to access the node (optional)
	 * @param coaches send the message to coaches
	 * @param participants send the message to participants
	 * @param groups send the message to the specified groups
	 * @param areas send the message to the specified learning areas
	 * @param to send the message to the e-mail address
	 * @param defaultSubject default subject of the message
	 * @param defaultBody default body text of the message
	 * @param request The HTTP request
	 * @return The persisted contact element (fully populated)
	 */
	@POST
	@Operation(summary = "attach a contact element onto a given course",
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
	public Response attachContactPost(@PathParam("courseId") Long courseId, @FormParam("parentNodeId") String parentNodeId,
			@FormParam("position") Integer position, 
			@FormParam("shortTitle") String shortTitle,
			@FormParam("longTitle") @DefaultValue("undefined") String longTitle, 
			@FormParam("description") String description,
			@FormParam("objectives") String objectives,
			@FormParam("instruction") String instruction,
			@FormParam("instructionalDesign") String instructionalDesign,
			@FormParam("visibilityExpertRules") String visibilityExpertRules,
			@FormParam("accessExpertRules") String accessExpertRules,
			@FormParam("coachesAll") @DefaultValue("false") boolean coachesAll,
			@FormParam("courseCoaches") @DefaultValue("false") boolean coachesCourse,
			@FormParam("assignedCoaches") @DefaultValue("false") boolean coachesAssigned,
			@FormParam("coachesGroup") String coachesGroups,
			@FormParam("coachesArea") String coachesAreas,
			@FormParam("participantsAll") @DefaultValue("false") boolean participantsAll,
			@FormParam("courseParticipants") @DefaultValue("false") boolean participantsCourse,
			@FormParam("participantsGroup") String participantsGroups,
			@FormParam("participantsArea") String participantsAreas,
			@FormParam("to") String to,
			@FormParam("defaultSubject") String defaultSubject, 
			@FormParam("defaultBody") String defaultBody,
			@Context HttpServletRequest request) {
		ContactConfigDelegate config = new ContactConfigDelegate(coachesAll,coachesCourse, coachesAssigned, coachesGroups, coachesAreas, participantsAll, participantsCourse, participantsGroups, participantsAreas,to, defaultSubject, defaultBody);
		return attach(courseId, parentNodeId, "co", position, shortTitle, longTitle, description, objectives, instruction, instructionalDesign, visibilityExpertRules, accessExpertRules, config, request);
	}
	
	private class ContactConfigDelegate implements CustomConfigDelegate {
		/**
		 * Deprecated Configvalues
		 */
		private Boolean coaches;
		private Boolean participants;
		private List<String> groups;
		private List<String> areas;
		
		
		private Boolean coachesAll;
		private Boolean coachesCourse;
		private Boolean coachesAssigned;
		private List<String> coachesGroups; 
		private List<String> coachesAreas; 
		private Boolean participantsAll; 
		private Boolean participantsCourse;
		private List<String> participantsGroups;
		private List<String> participantsAreas;
		
		private List<String> tos;
		private String defaultSubject;
		private String defaultBody;
		
		/**
		 * DepricatedConfig constructor
		 * @param coaches
		 * @param participants
		 * @param groups
		 * @param areas
		 * @param to
		 * @param defaultSubject
		 * @param defaultBody
		 */
		public ContactConfigDelegate(Boolean coaches, Boolean participants, String groups, String areas, String to, String defaultSubject, String defaultBody) {
			this.coaches = coaches;
			this.participants = participants;
			this.groups = getGroupNames(groups);
			this.areas = getGroupNames(areas);
			this.tos = getEmails(to);
			this.defaultSubject = defaultSubject;
			this.defaultBody = defaultBody;
		}
		
		public ContactConfigDelegate(Boolean coachesAll, Boolean coachesCourse, Boolean coachesAssigned, String coachesGroups, String coachesAreas, Boolean participantsAll, Boolean participantsCourse, String participantsGroups, String participantsAreas, String to, String defaultSubject, String defaultBody){
			this.coachesAll = coachesAll;
			this.coachesCourse = coachesCourse;
			this.coachesAssigned = coachesAssigned;
			this.coachesGroups = getGroupNames(coachesGroups);
			this.coachesAreas = getGroupNames(coachesAreas);
			this.participantsAll = participantsAll;
			this.participantsCourse = participantsCourse;
			this.participantsGroups = getGroupNames(participantsGroups);
			this.participantsAreas =  getGroupNames(participantsAreas);
			this.tos = getEmails(to);
			this.defaultSubject = defaultSubject;
			this.defaultBody = defaultBody;
		}
		
		@Override
		public boolean isValid() {
			boolean ok = false;
			/**
			 * if depricatedconfig is used
			 */
			if(participants != null){
				ok = ok || coaches;
				ok = ok || participants;
				ok = ok || (areas != null && !areas.isEmpty());
				ok = ok || (groups != null && !groups.isEmpty());
				ok = ok || (tos != null && !tos.isEmpty());
			}else{
				ok = ok || coachesAll;
				ok = ok || coachesCourse;
				ok = ok || coachesAssigned;
				ok = ok || participantsAll;
				ok = ok || participantsCourse;
				ok = ok || (participantsGroups != null && !participantsGroups.isEmpty());
				ok = ok || (participantsAreas != null && !participantsAreas.isEmpty());
				ok = ok || (coachesGroups != null && !coachesGroups.isEmpty());
				ok = ok || (coachesAreas != null && !coachesAreas.isEmpty());
				ok = ok || (tos != null && !tos.isEmpty());
			}

			
			/*
			 * check validity of manually provided e-mails
			 */
			if(tos != null && !tos.isEmpty()) {
				for (String eAd:tos) {
					ok = ok && MailHelper.isValidEmailAddress(eAd);
				}
			}
			return true;
		}

		@Override
		public void configure(ICourse course, CourseNode newNode, ModuleConfiguration moduleConfig) {
			/**
			 * if deprecated config is used
			 */
			if(participants != null){
				moduleConfig.set(CONFIG_KEY_EMAILTOGROUPS, getGroupNamesToString(groups));
				moduleConfig.set(CONFIG_KEY_EMAILTOAREAS, getGroupNamesToString(areas));
				moduleConfig.setBooleanEntry(CONFIG_KEY_EMAILTOCOACHES, coaches == null ? false : coaches.booleanValue());
				moduleConfig.setBooleanEntry(CONFIG_KEY_EMAILTOPARTICIPANTS, participants == null ? false : participants.booleanValue());
			}else{
				moduleConfig.setBooleanEntry(CONFIG_KEY_EMAILTOCOACHES_ALL, coachesAll.booleanValue());
				moduleConfig.setBooleanEntry(CONFIG_KEY_EMAILTOCOACHES_COURSE, coachesCourse.booleanValue());
				moduleConfig.setBooleanEntry(COEditController.CONFIG_KEY_EMAILTOCOACHES_ASSIGNED, coachesAssigned.booleanValue());
				moduleConfig.set(CONFIG_KEY_EMAILTOCOACHES_GROUP, getGroupNamesToString(coachesGroups));
				moduleConfig.set(CONFIG_KEY_EMAILTOCOACHES_AREA, getGroupNamesToString(coachesAreas));
				moduleConfig.set(CONFIG_KEY_EMAILTOPARTICIPANTS_ALL, participantsAll.booleanValue());
				moduleConfig.set(CONFIG_KEY_EMAILTOPARTICIPANTS_COURSE, participantsCourse.booleanValue());
				moduleConfig.set(CONFIG_KEY_EMAILTOPARTICIPANTS_GROUP, getGroupNamesToString(participantsGroups));
				moduleConfig.set(CONFIG_KEY_EMAILTOPARTICIPANTS_AREA, getGroupNamesToString(participantsAreas));
			}
			moduleConfig.set(CONFIG_KEY_EMAILTOADRESSES, tos);
			moduleConfig.set(CONFIG_KEY_MSUBJECT_DEFAULT, defaultSubject);
			moduleConfig.set(CONFIG_KEY_MBODY_DEFAULT, defaultBody);
		}
		
		
		private List<String> getEmails(String to) {
			List<String> eList = new ArrayList<>();
			if(StringHelper.containsNonWhitespace(to)) {
				String[] emailAdress = to.split(";");
				if ((emailAdress != null) && (emailAdress.length > 0) && (!"".equals(emailAdress[0]))) {
					for (String eAd : emailAdress) {
						eAd = eAd.trim();
						if (MailHelper.isValidEmailAddress(eAd)) {
							eList.add(eAd);
						}
					}
				}
			}
			return eList;
		}
		
		private String getGroupNamesToString(List<String> groupNames) {
			StringBuilder buffer = new StringBuilder();
			for(String groupName:groupNames) {
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
				BusinessGroupService bgm = CoreSpringFactory.getImpl(BusinessGroupService.class);
				
				List<Long> keys = new ArrayList<>();
				for(String groupId:groupIdArr) {
					Long groupKey = Long.valueOf(groupId);
					keys.add(groupKey);
				}
				List<BusinessGroupShort> businessGroups = bgm.loadShortBusinessGroups(keys);
				for(BusinessGroupShort bg:businessGroups) {
					groupNames.add(bg.getName());
				}
			}
			
			return groupNames;
		}
	}
}
