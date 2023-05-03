/**
 * <a href="https://www.openolat.org">
 * OpenOLAT - Online Learning and Training</a><br>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at the
 * <a href="https://www.apache.org/licenses/LICENSE-2.0">Apache homepage</a>
 * <p>
 * Unless required by applicable law or agreed to in writing,<br>
 * software distributed under the License is distributed on an "AS IS" BASIS, <br>
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
 * See the License for the specific language governing permissions and <br>
 * limitations under the License.
 * <p>
 * Initial code contributed and copyrighted by<br>
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.modules.fo.archiver;

import java.util.Date;
import java.util.List;

import org.olat.basesecurity.OrganisationModule;
import org.olat.basesecurity.OrganisationRoles;
import org.olat.basesecurity.OrganisationService;
import org.olat.core.CoreSpringFactory;
import org.olat.core.id.Identity;
import org.olat.core.id.Organisation;
import org.olat.core.util.openxml.OpenXMLWorkbookStyles;
import org.olat.core.util.openxml.OpenXMLWorksheet;
import org.olat.course.ICourse;
import org.olat.course.nodes.FOCourseNode;
import org.olat.modules.fo.Forum;
import org.olat.modules.fo.Message;
import org.olat.modules.fo.manager.ForumManager;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * creating dataRows for forum report export
 * <p>
 * Initial date: Apr 18, 2023
 *
 * @author Sumit Kapoor, sumit.kapoor@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
public class ForumReportExportData {

	private final FOCourseNode forumNode;
	private final ICourse course;
	private final Date beginDate;
	private final Date endDate;
	private final List<String> selectedOrgaKeys;

	@Autowired
	private ForumManager forumManager;
	@Autowired
	private OrganisationService organisationService;
	@Autowired
	private OrganisationModule organisationModule;

	public ForumReportExportData(FOCourseNode forumNode, ICourse course,
								 Date beginDate, Date endDate, List<String> selectedOrgaKeys) {
		CoreSpringFactory.autowireObject(this);
		this.forumNode = forumNode;
		this.course = course;
		this.beginDate = beginDate;
		this.endDate = endDate;
		this.selectedOrgaKeys = selectedOrgaKeys;
	}

	public void createExportData(OpenXMLWorksheet sheet) {
		createData(sheet);
	}

	private void createData(OpenXMLWorksheet worksheet) {
		// load messages
		Forum forum = forumNode.loadOrCreateForum(course.getCourseEnvironment());
		List<Message> messages = forumManager.getMessagesByForumAndDateRange(forum, beginDate, endDate);

		for (Message message : messages) {
			Identity creator = message.getCreator();

			// filter organisations if necessary and put displayNames into a string
			String organisation = "";
			if (organisationModule.isEnabled()) {
				List<Organisation> organisations = organisationService.getOrganisations(creator, OrganisationRoles.user);
				if (isValidOrganisation(organisations)) {
					organisation = getMultipleOrganisationNames(organisations);
				} else {
					continue;
				}
			}

			OpenXMLWorksheet.Row dataRow = worksheet.newRow();
			int col = 0;

			// retrieve data and assign them
			String foName = forumNode.getLongTitle();
			String courseElementID = course.getResourceableId().toString();
			String thread = message.getThreadtop() == null ? message.getTitle() : message.getThreadtop().getTitle();
			String title = message.getParent() == null ? "" : message.getTitle();
			Date creationDate = message.getCreationDate();
			Date lastModifiedDate = message.getLastModified();
			String creatorFirstName = creator.getUser().getFirstName();
			String creatorLastName = creator.getUser().getLastName();
			String creatorNickName = creator.getUser().getNickName();
			int wordCount = message.getNumOfWords();
			int charCount = message.getNumOfCharacters();

			// fill rows by assigned data
			dataRow.addCell(col++, foName);
			dataRow.addCell(col++, courseElementID, null);
			dataRow.addCell(col++, thread);
			dataRow.addCell(col++, title);
			dataRow.addCell(col++, creationDate, new OpenXMLWorkbookStyles().getDateTimeStyle());
			dataRow.addCell(col++, lastModifiedDate, new OpenXMLWorkbookStyles().getDateTimeStyle());
			dataRow.addCell(col++, creatorFirstName);
			dataRow.addCell(col++, creatorLastName);
			dataRow.addCell(col++, creatorNickName);
			dataRow.addCell(col++, organisation);
			dataRow.addCell(col++, wordCount, null);
			dataRow.addCell(col, charCount, null);
		}
	}

	/**
	 * check if filter for organisations is applied
	 * && check if creator of message has at least one organisation of the selected ones
	 *
	 * @param organisationsOfCreator
	 * @return true if creator of message is in a selected organisation or no filter is applied
	 */
	private boolean isValidOrganisation(List<Organisation> organisationsOfCreator) {
		if (selectedOrgaKeys == null || selectedOrgaKeys.isEmpty()) {
			return true;
		} else {
			return organisationsOfCreator.stream().anyMatch(o -> selectedOrgaKeys.contains(o.getKey().toString()));
		}
	}

	/**
	 * If message creator has multiple organisations, build up a String with comma separated organisations
	 * If not just return the displayName of organisation where creator has role 'user'
	 *
	 * @param organisations
	 * @return organisation(s) displayName(s)
	 */
	private String getMultipleOrganisationNames(List<Organisation> organisations) {
		StringBuilder orgaNames = new StringBuilder();
		if (!organisations.isEmpty()) {
			for (Organisation organisation : organisations) {
				if (orgaNames.toString().equals("")) {
					orgaNames.append(organisation.getDisplayName());
				} else {
					orgaNames.append(", ");
					orgaNames.append(organisation.getDisplayName());
				}
			}
		}
		return orgaNames.toString();
	}
}
