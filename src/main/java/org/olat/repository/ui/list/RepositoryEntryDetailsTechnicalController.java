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
package org.olat.repository.ui.list;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.olat.NewControllerFactory;
import org.olat.basesecurity.GroupRoles;
import org.olat.basesecurity.OrganisationModule;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.id.OLATResourceable;
import org.olat.core.id.Organisation;
import org.olat.core.id.Roles;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.core.util.coordinate.CoordinatorManager;
import org.olat.core.util.resource.OresHelper;
import org.olat.course.nodeaccess.NodeAccessService;
import org.olat.course.nodeaccess.NodeAccessType;
import org.olat.course.run.RunMainController;
import org.olat.modules.curriculum.Curriculum;
import org.olat.modules.curriculum.CurriculumElement;
import org.olat.modules.curriculum.CurriculumModule;
import org.olat.modules.curriculum.CurriculumService;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryRelationType;
import org.olat.repository.RepositoryService;
import org.olat.repository.ui.RepositoyUIFactory;
import org.olat.resource.references.ReferenceManager;
import org.olat.user.UserManager;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 4 Jun 2022<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class RepositoryEntryDetailsTechnicalController extends FormBasicController {

	private final RepositoryEntry entry;
	private final boolean isOwner;

	@Autowired
	private UserManager userManager;
	@Autowired
	private RepositoryService repositoryService;
	@Autowired
	private ReferenceManager referenceManager;
	@Autowired
	private NodeAccessService nodeAccessService;
	@Autowired
	private OrganisationModule organisationModule;
	@Autowired
	private CurriculumModule curriculumModule;
	@Autowired
	private CurriculumService curriculumService;
	@Autowired
	private CoordinatorManager coordinatorManager;
	
	public RepositoryEntryDetailsTechnicalController(UserRequest ureq, WindowControl wControl, RepositoryEntry entry, boolean isOwner) {
		super(ureq, wControl, Util.getPackageVelocityRoot(RepositoryEntryDetailsController.class) + "/details_technical.html");
		setTranslator(Util.createPackageTranslator(RepositoryService.class, getLocale(), getTranslator()));
		this.entry = entry;
		this.isOwner = isOwner;
		
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		if (formLayout instanceof FormLayoutContainer) {
			FormLayoutContainer layoutCont = (FormLayoutContainer)formLayout;
			
			layoutCont.contextPut("v", entry);
			
			Roles roles = ureq.getUserSession().getRoles();
			layoutCont.contextPut("roles", roles);
			layoutCont.contextPut("isEntryAuthor", Boolean.valueOf(isOwner));
			
			if (StringHelper.containsNonWhitespace(entry.getTechnicalType())) {
				String technicalType = nodeAccessService.getNodeAccessTypeName(NodeAccessType.of(entry.getTechnicalType()), getLocale());
				layoutCont.contextPut("technicalType", technicalType);
			}
			
			// Owners
			List<Long> authorKeys = repositoryService.getMemberKeys(entry, RepositoryEntryRelationType.all, GroupRoles.owner.name());
			List<String> authorLinkNames = new ArrayList<>(authorKeys.size());
			Map<Long, String> authorNames = userManager.getUserDisplayNamesByKey(authorKeys);
			int counter = 0;
			for(Map.Entry<Long, String> author:authorNames.entrySet()) {
				Long authorKey = author.getKey();
				String authorName = StringHelper.escapeHtml(author.getValue());
				
				FormLink authorLink = uifactory.addFormLink("owner-" + ++counter, "owner", authorName, null, formLayout, Link.NONTRANSLATED | Link.LINK);
				authorLink.setUserObject(authorKey);
				authorLinkNames.add(authorLink.getComponent().getComponentName());
			}
			layoutCont.contextPut("authorlinknames", authorLinkNames);
			
			// Organisations 
			if(organisationModule.isEnabled()) {
				String organisations = getOrganisationsToString();
				layoutCont.contextPut("organisations", organisations);
			}
			
			// Curricula
			if(curriculumModule.isEnabled()) {
				List<String> curriculums = getCurriculumsToString();
				layoutCont.contextPut("curriculums", curriculums);
			}
			
			// References
			if (isOwner || roles.isAdministrator() || roles.isLearnResourceManager()) {
				List<RepositoryEntry> refs = referenceManager.getRepositoryReferencesTo(entry.getOlatResource());
				if (!refs.isEmpty()) {
					List<String> refLinks = new ArrayList<>(refs.size());
					int count = 0;
					for (RepositoryEntry ref:refs) {
						String name = "ref-" + count++;
						FormLink refLink = uifactory
								.addFormLink(name, "ref", ref.getDisplayname(), null, formLayout, Link.NONTRANSLATED);
						refLink.setUserObject(ref.getKey());
						refLink.setIconLeftCSS("o_icon o_icon-fw " + RepositoyUIFactory.getIconCssClass(ref));
						refLinks.add(name);
					}
					layoutCont.contextPut("referenceLinks", refLinks);
				}
			}
			
			// Number of current users
			OLATResourceable courseRunOres = OresHelper.createOLATResourceableInstance(RunMainController.ORES_TYPE_COURSE_RUN, entry.getOlatResource().getResourceableId());
			int numUsers = coordinatorManager.getCoordinator().getEventBus().getListeningIdentityCntFor(courseRunOres);
			layoutCont.contextPut("numUsers",  String.valueOf(numUsers));
		}
	}
	
	private List<String> getCurriculumsToString() {
		List<CurriculumElement> curriculumElements = curriculumService.getCurriculumElements(entry);
		Map<Curriculum, StringBuilder> curriculumToElementsMap = new HashMap<>();
		for (CurriculumElement curriculumElement:curriculumElements) {
			Curriculum curriculum = curriculumElement.getCurriculum();
			StringBuilder sc = curriculumToElementsMap.computeIfAbsent(curriculum, c -> {
				StringBuilder sb = new StringBuilder(64);
				sb.append(StringHelper.escapeHtml(c.getDisplayName())).append(" (");
				return sb;
			});
			sc.append(StringHelper.escapeHtml(curriculumElement.getDisplayName())).append(", ");
		}
		
		List<String> curriculumList = new ArrayList<>(curriculumToElementsMap.size());
		for (StringBuilder sb:curriculumToElementsMap.values()) {
			String line = sb.toString().substring(0, sb.length() -2).concat(")");
			curriculumList.add(line);
		}
		return curriculumList;
	}
	
	private String getOrganisationsToString() {
		List<Organisation> organisations = repositoryService.getOrganisations(entry);
		StringBuilder sb = new StringBuilder(64);
		for (Organisation organisation:organisations) {
			if (sb.length() > 0) sb.append(", ");
			sb.append(StringHelper.escapeHtml(organisation.getDisplayName()));
		}
		return sb.toString();
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(source instanceof FormLink) {
			FormLink link = (FormLink)source;
			String cmd = link.getCmd();
			if("owner".equals(cmd)) {
				doOpenVisitCard(ureq, (Long)link.getUserObject());
			} else if("ref".equals(cmd)) {
				doOpenReference(ureq, (Long)link.getUserObject());
			}
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}
	
	private void doOpenReference(UserRequest ureq, Long entryKey) {
		String businessPath = "[RepositoryEntry:" + entryKey + "]";
		NewControllerFactory.getInstance().launch(businessPath, ureq, getWindowControl());
	}
	
	private void doOpenVisitCard(UserRequest ureq, Long ownerKey) {
		String businessPath = "[HomePage:" + ownerKey + "]";
		NewControllerFactory.getInstance().launch(businessPath, ureq, getWindowControl());
	}
	
}
