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
package org.olat.repository.ui.settings;

import java.text.Collator;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.olat.NewControllerFactory;
import org.olat.basesecurity.GroupRoles;
import org.olat.basesecurity.OrganisationRoles;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.util.Util;
import org.olat.group.BusinessGroup;
import org.olat.group.BusinessGroupService;
import org.olat.group.model.BusinessGroupQueryParams;
import org.olat.group.model.StatisticsBusinessGroupRow;
import org.olat.ims.lti13.LTI13Service;
import org.olat.modules.curriculum.CurriculumElement;
import org.olat.modules.curriculum.CurriculumService;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryStatusEnum;
import org.olat.repository.RepositoryManager;
import org.olat.repository.RepositoryService;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 16 May 2022<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class AccessOverviewController extends BasicController {
	
	private static final String ICON_ACTIVE = "<i class=\"o_icon o_icon-fw o_icon_offer_active\"> </i> ";
	private static final String ICON_INACTIVE = "<i class=\"o_icon o_icon-fw o_icon_offer_inactive\"> </i> ";

	private final VelocityContainer mainVC;
	private final Collator collator;
	private Link openMemberManagementLink;

	private RepositoryEntry entry;
	private final BusinessGroupQueryParams bgSearchParams;
	
	@Autowired
	private RepositoryService repositoryService;
	@Autowired
	private BusinessGroupService businessGroupService;
	@Autowired
	private CurriculumService curriculumService;

	public AccessOverviewController(UserRequest ureq, WindowControl wControl, RepositoryEntry entry) {
		super(ureq, wControl);
		this.entry = entry;
		setTranslator(Util.createPackageTranslator(RepositoryManager.class, getLocale()));
		collator = Collator.getInstance(getLocale());
		
		bgSearchParams = new BusinessGroupQueryParams();
		bgSearchParams.setTechnicalTypes(List.of(BusinessGroup.BUSINESS_TYPE, LTI13Service.LTI_GROUP_TYPE));
		bgSearchParams.setRepositoryEntry(entry);
		
		mainVC = createVelocityContainer("access_overview");
		putInitialPanel(mainVC);
		
		reload();
	}
	
	public void reload() {
		entry = repositoryService.loadByKey(entry.getKey());
		Map<String, Long> roleToCountMemebers = repositoryService.getRoleToCountMemebers(entry);
		
		// Members
		Long ownersCount = roleToCountMemebers.getOrDefault(GroupRoles.owner.name(), Long.valueOf(0));
		String owners = ICON_ACTIVE + translate("access.overview.owners", ownersCount.toString());
		mainVC.contextPut("owners", owners);
		
		Long coachesCount = roleToCountMemebers.getOrDefault(GroupRoles.coach.name(), Long.valueOf(0));
		String coaches = RepositoryEntryStatusEnum.isInArray(entry.getEntryStatus(), RepositoryEntryStatusEnum.coachPublishedToClosed())
				? ICON_ACTIVE
				: ICON_INACTIVE;
		coaches += translate("access.overview.coaches", coachesCount.toString());
		mainVC.contextPut("coaches", coaches);
		
		Long participantsCount = roleToCountMemebers.getOrDefault(GroupRoles.participant.name(), Long.valueOf(0));
		String participants = RepositoryEntryStatusEnum.isInArray(entry.getEntryStatus(), RepositoryEntryStatusEnum.publishedAndClosed())
				? ICON_ACTIVE
				: ICON_INACTIVE;
		participants += translate("access.overview.participants", participantsCount.toString());
		mainVC.contextPut("participants", participants);
		
		openMemberManagementLink = LinkFactory.createLink("access.overview.open.members.management", getTranslator(), this);
		openMemberManagementLink.setIconLeftCSS("o_icon o_icon_membersmanagement");
		mainVC.put("open.members.management", openMemberManagementLink);
		
		// Admin access
		String authors = RepositoryEntryStatusEnum.isInArray(entry.getEntryStatus(), RepositoryEntryStatusEnum.reviewToClosed())
				? ICON_ACTIVE
				: ICON_INACTIVE;
		Long authorsCount = roleToCountMemebers.getOrDefault(OrganisationRoles.author.name(), Long.valueOf(0));
		List<String> authorRightsList = new ArrayList<>(3);
		if (entry.getCanReference()) {
			authorRightsList.add(translate("access.overview.right.reference"));
		}
		if (entry.getCanCopy()) {
			authorRightsList.add(translate("access.overview.right.copy"));
		}
		if (entry.getCanDownload()) {
			authorRightsList.add(translate("access.overview.right.export"));
		}
		if (authorRightsList.isEmpty()) {
			authorRightsList.add(translate("access.overview.right.none"));
		}
		String authorRights = authorRightsList.stream().collect(Collectors.joining(", "));
		authors += translate("access.overview.authors", authorsCount.toString(), translate("access.overview.right", authorRights));
		mainVC.contextPut("authors", authors);
		
		String learnresourcemanager = RepositoryEntryStatusEnum.isInArray(entry.getEntryStatus(), RepositoryEntryStatusEnum.preparationToClosed())
				? ICON_ACTIVE
				: ICON_INACTIVE;
		Long learnresourcemanagerCount = roleToCountMemebers.getOrDefault(OrganisationRoles.learnresourcemanager.name(), Long.valueOf(0));
		learnresourcemanager += translate("access.overview.learnresourcemanagers", 
				learnresourcemanagerCount.toString(),
				translate("access.overview.right", translate("access.overview.right.full")));
		mainVC.contextPut("learnresourcemanager", learnresourcemanager);
		
		Long principalsCount = roleToCountMemebers.getOrDefault(OrganisationRoles.principal.name(), Long.valueOf(0));
		String principals = ICON_ACTIVE + translate("access.overview.principals", 
				principalsCount.toString(),
				translate("access.overview.right", translate("access.overview.right.read")));
		mainVC.contextPut("principals", principals);
		
		Long administratorsCount = roleToCountMemebers.getOrDefault(OrganisationRoles.administrator.name(), Long.valueOf(0));
		String administrators = ICON_ACTIVE + translate("access.overview.administrators", 
				administratorsCount.toString(),
				translate("access.overview.right", translate("access.overview.right.full")));
		mainVC.contextPut("administrators", administrators);
		
		// Groups
		List<String> groups = businessGroupService.findBusinessGroupsFromRepositoryEntry(bgSearchParams, getIdentity(), entry).stream()
				.map(StatisticsBusinessGroupRow::getName)
				.sorted(collator)
				.collect(Collectors.toList());
		mainVC.contextPut("groups", groups);
		
		// Curricula
		List<String> curriculumElements = curriculumService.getCurriculumElements(entry).stream()
				.map(CurriculumElement::getDisplayName)
				.sorted(collator)
				.collect(Collectors.toList());
		mainVC.contextPut("curriculumElements", curriculumElements);
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if (source == openMemberManagementLink) {
			doOpenMembersManagement(ureq);
		}
	}

	private void doOpenMembersManagement(UserRequest ureq) {
		String businessPath = "[RepositoryEntry:" + entry.getKey() + "][MembersMgmt][0]";
		NewControllerFactory.getInstance().launch(businessPath, ureq, getWindowControl());
	}

}
