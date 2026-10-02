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
package org.olat.course.nodes.wiki;

import java.util.ArrayList;
import java.util.List;

import org.olat.basesecurity.GroupRoles;
import org.olat.basesecurity.OrganisationRoles;
import org.olat.core.commons.services.notifications.SubscriptionContext;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.emptystate.EmptyStateConfig;
import org.olat.core.gui.components.panel.IconPanelLabelTextContent;
import org.olat.core.gui.components.stack.BreadcrumbPanel;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.id.Organisation;
import org.olat.core.id.Roles;
import org.olat.course.ICourse;
import org.olat.course.editor.CourseNodeReferenceProvider;
import org.olat.course.editor.NodeEditController;
import org.olat.course.nodes.WikiCourseNode;
import org.olat.course.run.environment.CourseEnvironment;
import org.olat.fileresource.types.WikiResource;
import org.olat.modules.ModuleConfiguration;
import org.olat.modules.wiki.DryRunAssessmentProvider;
import org.olat.modules.wiki.Wiki;
import org.olat.modules.wiki.WikiManager;
import org.olat.modules.wiki.WikiSecurityCallback;
import org.olat.modules.wiki.WikiSecurityCallbackImpl;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryService;
import org.olat.repository.ui.RepositoryEntryReferenceController;
import org.olat.repository.ui.RepositoryEntryReferenceProvider.ReferenceContentProvider;
import org.springframework.beans.factory.annotation.Autowired;

/**
 *
 * Initial date: 2 Mar 2020<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class WikiConfigController extends BasicController implements ReferenceContentProvider {

	private final VelocityContainer mainVC;
	private final IconPanelLabelTextContent iconPanelContent;
	private final RepositoryEntryReferenceController referenceCtrl;

	private Controller wikiCtrl;

	private final BreadcrumbPanel stackPanel;
	private final WikiCourseNode courseNode;
	private final ModuleConfiguration config;
	private final ICourse course;

	@Autowired
	private RepositoryService repositoryService;

	public WikiConfigController(UserRequest ureq, WindowControl wControl, BreadcrumbPanel stackPanel,
			WikiCourseNode courseNode, ICourse course) {
		super(ureq, wControl);
		this.stackPanel = stackPanel;
		this.courseNode = courseNode;
		this.course = course;
		this.config = courseNode.getModuleConfiguration();

		mainVC = createVelocityContainer("wiki_config");
		mainVC.contextPut("helpUrl", "manual_user/learningresources/Course_Element_Wiki/");

		iconPanelContent = new IconPanelLabelTextContent("content");

		RepositoryEntry wikiEntry = courseNode.getReferencedRepositoryEntry();
		EmptyStateConfig emptyStateConfig = EmptyStateConfig.builder()
				.withMessageTranslated(translate("no.wiki.resource.selected"))
				.withDescTranslated(translate("no.wiki.resource.selected.text"))
				.withIconCss("o_icon o_FileResource-WIKI_icon")
				.build();
		RepositoryEntry courseEntry = course.getCourseEnvironment().getCourseGroupManager().getCourseEntry();
		List<Organisation> defaultOrganisations = repositoryService.getOrganisations(courseEntry);
		CourseNodeReferenceProvider referenceProvider = new CourseNodeReferenceProvider(repositoryService,
				List.of(WikiResource.TYPE_NAME), defaultOrganisations, emptyStateConfig, translate("select.wiki"), this);
		referenceCtrl = new RepositoryEntryReferenceController(ureq, wControl, wikiEntry, referenceProvider);
		listenTo(referenceCtrl);
		mainVC.put("reference", referenceCtrl.getInitialComponent());

		if (wikiEntry != null) {
			updateReferenceContentUI(wikiEntry);
		}

		putInitialPanel(mainVC);
	}

	@Override
	public Component getContent(RepositoryEntry repositoryEntry) {
		return iconPanelContent;
	}

	@Override
	public void refresh(Component cmp, RepositoryEntry repositoryEntry) {
		// Refresh is handled on change event.
	}

	private void updateReferenceContentUI(RepositoryEntry wikiEntry) {
		Wiki wiki = WikiManager.getInstance().getOrLoadWiki(wikiEntry.getOlatResource());
		int numOfPages = wiki.getAllPages().size();

		List<IconPanelLabelTextContent.LabelText> labelTexts = new ArrayList<>(1);
		labelTexts.add(new IconPanelLabelTextContent.LabelText(translate("num.pages"), String.valueOf(numOfPages)));
		iconPanelContent.setLabelTexts(labelTexts);
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		//
	}

	@Override
	public void event(UserRequest ureq, Controller source, Event event) {
		if (source == referenceCtrl) {
			if (event == RepositoryEntryReferenceController.SELECTION_EVENT) {
				doChangeWiki(ureq, referenceCtrl.getRepositoryEntry());
			} else if (event == RepositoryEntryReferenceController.PREVIEW_EVENT) {
				doPreviewWiki(ureq);
			}
		}
		super.event(ureq, source, event);
	}

	private void doChangeWiki(UserRequest ureq, RepositoryEntry wikiEntry) {
		if (wikiEntry == null) {
			return;
		}
		WikiEditController.setWikiRepoReference(wikiEntry, config);
		updateReferenceContentUI(wikiEntry);
		fireEvent(ureq, NodeEditController.NODECONFIG_CHANGED_EVENT);
	}

	private void doPreviewWiki(UserRequest ureq) {
		RepositoryEntry wikiEntry = referenceCtrl.getRepositoryEntry();
		if (wikiEntry == null) {
			showError("error.repoentrymissing");
			return;
		}

		removeAsListenerAndDispose(wikiCtrl);

		Roles roles = ureq.getUserSession().getRoles();
		boolean isAdministrator = (roles.isAdministrator() || roles.isLearnResourceManager())
				&& repositoryService.hasRoleExpanded(getIdentity(), wikiEntry,
						OrganisationRoles.administrator.name(), OrganisationRoles.learnresourcemanager.name());
		boolean isResourceOwner = repositoryService.hasRole(getIdentity(), wikiEntry, GroupRoles.owner.name());

		CourseEnvironment cenv = course.getCourseEnvironment();
		SubscriptionContext subsContext = WikiManager.createTechnicalSubscriptionContextForCourse(cenv, courseNode);
		WikiSecurityCallback callback = new WikiSecurityCallbackImpl(null, isAdministrator, false, false, isResourceOwner, subsContext);
		wikiCtrl = WikiManager.getInstance().createWikiMainController(ureq, getWindowControl(), wikiEntry.getOlatResource(),
				callback, DryRunAssessmentProvider.create(), null);
		listenTo(wikiCtrl);
		stackPanel.pushController(translate("preview"), wikiCtrl);
	}
}
