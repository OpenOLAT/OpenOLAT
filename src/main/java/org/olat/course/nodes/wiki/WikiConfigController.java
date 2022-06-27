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

import org.olat.basesecurity.GroupRoles;
import org.olat.basesecurity.OrganisationRoles;
import org.olat.core.commons.services.notifications.SubscriptionContext;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.elements.StaticTextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.stack.BreadcrumbPanel;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.id.Roles;
import org.olat.core.util.StringHelper;
import org.olat.course.ICourse;
import org.olat.course.editor.NodeEditController;
import org.olat.course.nodes.AbstractFeedCourseNode;
import org.olat.course.nodes.CourseNodeFactory;
import org.olat.course.nodes.WikiCourseNode;
import org.olat.course.run.environment.CourseEnvironment;
import org.olat.fileresource.types.WikiResource;
import org.olat.modules.ModuleConfiguration;
import org.olat.modules.wiki.DryRunAssessmentProvider;
import org.olat.modules.wiki.WikiManager;
import org.olat.modules.wiki.WikiSecurityCallback;
import org.olat.modules.wiki.WikiSecurityCallbackImpl;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryService;
import org.olat.repository.controllers.ReferencableEntriesSearchController;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 2 Mar 2020<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class WikiConfigController extends FormBasicController {
	
	private StaticTextElement wikiNotChoosenEl;
	private FormLink previewLink;
	private FormLink chooseLink;
	private FormLink replaceLink;
	private FormLink editLink;
	
	private CloseableModalController cmc;
	private ReferencableEntriesSearchController repositorySearchCtrl;
	private Controller wikiCtrl;
	
	private final BreadcrumbPanel stackPanel;
	private final WikiCourseNode courseNode;
	private final ModuleConfiguration config;
	private final ICourse course;
	private RepositoryEntry wikiEntry;
	
	@Autowired
	private RepositoryService repositoryService;
	
	public WikiConfigController(UserRequest ureq, WindowControl wControl, BreadcrumbPanel stackPanel,
			WikiCourseNode courseNode, ICourse course) {
		super(ureq, wControl);
		this.stackPanel = stackPanel;
		this.courseNode = courseNode;
		this.course = course;
		this.config = courseNode.getModuleConfiguration();
		this.wikiEntry = courseNode.getReferencedRepositoryEntry();
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		setFormTitle("header");
		setFormContextHelp("manual_user/course_elements/Communication_and_Collaboration/#wiki");
		
		wikiNotChoosenEl = uifactory.addStaticTextElement("chosenwiki", "chosenwiki", translate("no.entry.chosen"), formLayout);
		previewLink = uifactory.addFormLink("command.preview", "", translate("command.preview"), formLayout, Link.NONTRANSLATED);
		previewLink.setIconLeftCSS("o_icon o_icon-fw o_icon_preview");
		
		FormLayoutContainer buttonsCont = FormLayoutContainer.createButtonLayout("buttons", getTranslator());
		buttonsCont.setRootForm(mainForm);
		formLayout.add(buttonsCont);
		chooseLink = uifactory.addFormLink("command.create", buttonsCont, "btn btn-default o_xsmall");
		chooseLink.setElementCssClass("o_sel_wiki_choose_repofile");
		replaceLink = uifactory.addFormLink("command.change", buttonsCont, "btn btn-default o_xsmall");
		editLink = uifactory.addFormLink("edit", buttonsCont, "btn btn-default o_xsmall");
		
		updateUI();
	}
	
	private void updateUI() {
		boolean feedSelected = wikiEntry != null;
		if (feedSelected) {
			String displayname = StringHelper.escapeHtml(wikiEntry.getDisplayname());
			previewLink.setI18nKey(displayname);
			flc.setDirty(true);
		}
		wikiNotChoosenEl.setVisible(!feedSelected);
		chooseLink.setVisible(!feedSelected);
		previewLink.setVisible(feedSelected);
		replaceLink.setVisible(feedSelected);
		editLink.setVisible(feedSelected);
	}
	
	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (source == chooseLink || source == replaceLink) {
			doSelectFeed(ureq);
		} else if (source == previewLink) {
			doPreviewFeed(ureq);
		} else if (source == editLink) {
			doEditFeed(ureq);
		}
		super.formInnerEvent(ureq, source, event);
	}
	
	@Override
	public void event(UserRequest urequest, Controller source, Event event) {
		if (source == repositorySearchCtrl) {
			if (event == ReferencableEntriesSearchController.EVENT_REPOSITORY_ENTRY_SELECTED) {
				wikiEntry = repositorySearchCtrl.getSelectedEntry();
				if (wikiEntry != null) {
					AbstractFeedCourseNode.setReference(courseNode.getModuleConfiguration(), wikiEntry);
					WikiEditController.setWikiRepoReference(wikiEntry, config);
					fireEvent(urequest, NodeEditController.NODECONFIG_CHANGED_EVENT);
					updateUI();
				}
			}
			cmc.deactivate();
			cleanUp();
		} else if (source == cmc) {
			cmc.deactivate();
			cleanUp();
		}
	}

	private void cleanUp() {
		removeAsListenerAndDispose(repositorySearchCtrl);
		removeAsListenerAndDispose(wikiCtrl);
		removeAsListenerAndDispose(cmc);
		repositorySearchCtrl = null;
		wikiCtrl = null;
		cmc = null;
	}
	
	private void doSelectFeed(UserRequest ureq) {
		repositorySearchCtrl = new ReferencableEntriesSearchController(getWindowControl(), ureq, WikiResource.TYPE_NAME,
				translate("command.choose"));
		listenTo(repositorySearchCtrl);
		cmc = new CloseableModalController(getWindowControl(), translate("close"),
				repositorySearchCtrl.getInitialComponent(), true, translate("command.create"));
		cmc.activate();
	}

	private void doPreviewFeed(UserRequest ureq) {
		if (wikiEntry == null) {
			showError("error.repoentrymissing");
		} else {
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

	private void doEditFeed(UserRequest ureq) {
		if (wikiEntry == null) {
			showError("error.repoentrymissing");
		} else {
			CourseNodeFactory.getInstance().launchReferencedRepoEntryEditor(ureq, getWindowControl(), courseNode);
		}
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}
}
