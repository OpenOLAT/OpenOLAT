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

import static org.olat.course.nodes.co.COEditController.CONFIG_KEY_EMAILTOADRESSES;
import static org.olat.course.nodes.co.COEditController.CONFIG_KEY_EMAILTOAREAS;
import static org.olat.course.nodes.co.COEditController.CONFIG_KEY_EMAILTOCOACHES;
import static org.olat.course.nodes.co.COEditController.CONFIG_KEY_EMAILTOGROUPS;
import static org.olat.course.nodes.co.COEditController.CONFIG_KEY_EMAILTOPARTICIPANTS;
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
import org.olat.group.BusinessGroup;
import org.olat.group.BusinessGroupService;
import org.olat.modules.ModuleConfiguration;
import org.olat.restapi.repository.course.AbstractCourseNodeWebService;

/**
 * 
 * Description:<br>
 * This handles the contact building block.
 * 
 * <P>
 * Initial Date:  10 mai 2010 <br>
 * @author srosse, stephane.rosse@frentix.com
 */
@Path("repo/courses/{courseId}/elements/contact")
public class COWebService extends AbstractCourseNodeWebService {
	
	/**
	 * This attaches a contact element onto a given course, the element will be
	 * inserted underneath the supplied parentNodeId
   * @response.representation.mediaType application/x-www-form-urlencoded
   * @response.representation.doc The course node metadatas
	 * @response.representation.200.qname {http://www.example.com}courseNodeVO
   * @response.representation.200.mediaType application/xml, application/json
   * @response.representation.200.doc The course node metadatas
   * @response.representation.200.example {@link org.olat.restapi.support.vo.Examples#SAMPLE_COURSENODEVO}
	 * @response.representation.401.doc The roles of the authenticated user are not sufficient
   * @response.representation.404.doc The course or parentNode not found
	 * @param courseId The course resourceable's id
	 * @param parentNodeId The node's id which will be the parent of this structure
	 * @param position The node's position relative to its sibling nodes (optional)
	 * @param shortTitle The node short title
	 * @param longTitle The node long title
	 * @param objectives The node learning objectives
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
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	public Response attachContact(@PathParam("courseId") Long courseId, @QueryParam("parentNodeId") String parentNodeId,
			@QueryParam("position") Integer position, @QueryParam("shortTitle") @DefaultValue("undefined") String shortTitle,
			@QueryParam("longTitle") @DefaultValue("undefined") String longTitle, @QueryParam("objectives") @DefaultValue("undefined") String objectives,
			@QueryParam("visibilityExpertRules") String visibilityExpertRules, @QueryParam("accessExpertRules") String accessExpertRules,
			@QueryParam("coaches") @DefaultValue("false") boolean coaches, @QueryParam("participants") @DefaultValue("false") boolean participants,
			@QueryParam("groups") String groups, @QueryParam("areas") String areas, @QueryParam("to") String to,
			@QueryParam("defaultSubject") String defaultSubject, @QueryParam("defaultBody") String defaultBody,
			@Context HttpServletRequest request) {
		
		ContactConfigDelegate config = new ContactConfigDelegate(coaches, participants, groups, areas, to, defaultSubject, defaultBody);
		return attach(courseId, parentNodeId, "co", position, shortTitle, longTitle, objectives, visibilityExpertRules, accessExpertRules, config, request);
	}
	
	/**
	 * This attaches a contact element onto a given course, the element will be
	 * inserted underneath the supplied parentNodeId
   * @response.representation.mediaType application/x-www-form-urlencoded
   * @response.representation.doc The course node metadatas
	 * @response.representation.200.qname {http://www.example.com}courseNodeVO
   * @response.representation.200.mediaType application/xml, application/json
   * @response.representation.200.doc The course node metadatas
   * @response.representation.200.example {@link org.olat.restapi.support.vo.Examples#SAMPLE_COURSENODEVO}
	 * @response.representation.401.doc The roles of the authenticated user are not sufficient
   * @response.representation.404.doc The course or parentNode not found
	 * @param courseId The course resourceable's id
	 * @param parentNodeId The node's id which will be the parent of this
	 *          structure
	 * @param position The node's position relative to its sibling nodes (optional)
	 * @param shortTitle The node short title
	 * @param longTitle The node long title
	 * @param objectives The node learning objectives
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
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	public Response attachContactPost(@PathParam("courseId") Long courseId, @FormParam("parentNodeId") String parentNodeId,
			@FormParam("position") Integer position, @FormParam("shortTitle") @DefaultValue("undefined") String shortTitle,
			@FormParam("longTitle") @DefaultValue("undefined") String longTitle, @FormParam("objectives") @DefaultValue("undefined") String objectives,
			@FormParam("visibilityExpertRules") String visibilityExpertRules, @FormParam("accessExpertRules") String accessExpertRules,
			@FormParam("coaches") @DefaultValue("false") boolean coaches, @FormParam("participants") @DefaultValue("false") boolean participants,
			@FormParam("groups") String groups, @FormParam("areas") String areas, @FormParam("to") String to,
			@FormParam("defaultSubject") String defaultSubject, @FormParam("defaultBody") String defaultBody,
			@Context HttpServletRequest request) {
		ContactConfigDelegate config = new ContactConfigDelegate(coaches, participants, groups, areas, to, defaultSubject, defaultBody);
		return attach(courseId, parentNodeId, "co", position, shortTitle, longTitle, objectives, visibilityExpertRules, accessExpertRules, config, request);
	}
	
	private class ContactConfigDelegate implements CustomConfigDelegate {
		private Boolean coaches;
		private Boolean participants;
		private List<String> groups;
		private List<String> areas;
		private List<String> tos;
		private String defaultSubject;
		private String defaultBody;
		
		public ContactConfigDelegate(Boolean coaches, Boolean participants, String groups, String areas, String to, String defaultSubject, String defaultBody) {
			this.coaches = coaches;
			this.participants = participants;
			this.groups = getGroupNames(groups);
			this.areas = getGroupNames(areas);
			this.tos = getEmails(to);
			this.defaultSubject = defaultSubject;
			this.defaultBody = defaultBody;
		}
		
		@Override
		public boolean isValid() {
			boolean ok = false;
			ok = ok || coaches;
			ok = ok || participants;
			ok = ok || (areas != null && !areas.isEmpty());
			ok = ok || (groups != null && !groups.isEmpty());
			ok = ok || (tos != null && !tos.isEmpty());
			
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
			moduleConfig.set(CONFIG_KEY_EMAILTOGROUPS, getGroupNamesToString(groups));
			moduleConfig.set(CONFIG_KEY_EMAILTOAREAS, getGroupNamesToString(areas));
			moduleConfig.setBooleanEntry(CONFIG_KEY_EMAILTOCOACHES, coaches == null ? false : coaches.booleanValue());
			moduleConfig.setBooleanEntry(CONFIG_KEY_EMAILTOPARTICIPANTS, participants == null ? false : participants.booleanValue());
			moduleConfig.set(CONFIG_KEY_EMAILTOADRESSES, tos);
			moduleConfig.set(CONFIG_KEY_MSUBJECT_DEFAULT, defaultSubject);
			moduleConfig.set(CONFIG_KEY_MBODY_DEFAULT, defaultBody);
		}
		
		private List<String> getEmails(String to) {
			List<String> eList = new ArrayList<String>();
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
		
		// fxdiff
		private String getGroupNamesToString(List<String> groupNames) {
			StringBuffer buffer = new StringBuffer();
			for(String groupName:groupNames) {
				if(buffer.length() > 0) {
					buffer.append(',');
				}
				buffer.append(groupName);
			}
			return buffer.toString();
		}
		
		private List<String> getGroupNames(String groupIds) {
			List<String> groupNames = new ArrayList<String>();
			
			if(StringHelper.containsNonWhitespace(groupIds)) {
				String[] groupIdArr = groupIds.split(";");
				BusinessGroupService bgm = CoreSpringFactory.getImpl(BusinessGroupService.class);
				
				List<Long> keys = new ArrayList<Long>();
				for(String groupId:groupIdArr) {
					Long groupKey = new Long(groupId);
					keys.add(groupKey);
				}
				List<BusinessGroup> groups = bgm.loadBusinessGroups(keys);
				for(BusinessGroup bg:groups) {
					groupNames.add(bg.getName());
				}
			}
			
			return groupNames;
		}
	}
}
