/**
* OLAT - Online Learning and Training<br>
* http://www.olat.org
* <p>
* Licensed under the Apache License, Version 2.0 (the "License"); <br>
* you may not use this file except in compliance with the License.<br>
* You may obtain a copy of the License at
* <p>
* http://www.apache.org/licenses/LICENSE-2.0
* <p>
* Unless required by applicable law or agreed to in writing,<br>
* software distributed under the License is distributed on an "AS IS" BASIS, <br>
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
* See the License for the specific language governing permissions and <br>
* limitations under the License.
* <p>
* Copyright (c) since 2004 at Multimedia- & E-Learning Services (MELS),<br>
* University of Zurich, Switzerland.
* <hr>
* <a href="http://www.openolat.org">
* OpenOLAT - Online Learning and Training</a><br>
* This file has been modified by the OpenOLAT community. Changes are licensed
* under the Apache 2.0 license as the original file.
*/

package org.olat.course.nodes.wiki;

import org.olat.NewControllerFactory;
import org.olat.basesecurity.GroupRoles;
import org.olat.basesecurity.OrganisationRoles;
import org.olat.core.commons.services.notifications.SubscriptionContext;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.panel.Panel;
import org.olat.core.gui.components.tabbedpane.TabbedPane;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.ControllerEventListener;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.gui.control.generic.tabbable.ActivateableTabbableDefaultController;
import org.olat.core.id.Roles;
import org.olat.core.logging.AssertException;
import org.olat.core.util.StringHelper;
import org.olat.course.ICourse;
import org.olat.course.assessment.AssessmentHelper;
import org.olat.course.condition.Condition;
import org.olat.course.condition.ConditionEditController;
import org.olat.course.editor.NodeEditController;
import org.olat.course.nodes.WikiCourseNode;
import org.olat.course.run.environment.CourseEnvironment;
import org.olat.course.run.userview.UserCourseEnvironment;
import org.olat.course.tree.CourseEditorTreeModel;
import org.olat.fileresource.types.WikiResource;
import org.olat.modules.ModuleConfiguration;
import org.olat.modules.wiki.WikiMainController;
import org.olat.modules.wiki.WikiManager;
import org.olat.modules.wiki.WikiSecurityCallback;
import org.olat.modules.wiki.WikiSecurityCallbackImpl;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryManager;
import org.olat.repository.RepositoryService;
import org.olat.repository.controllers.ReferencableEntriesSearchController;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Description: <BR/>Edit controller for single page course nodes <P/> Initial
 * Date: Oct 12, 2004
 * 
 * @author Felix Jost
 */
public class WikiEditController extends ActivateableTabbableDefaultController implements ControllerEventListener {
	public static final String PANE_TAB_ACCESSIBILITY = "pane.tab.accessibility";
	public static final String PANE_TAB_WIKICONFIG = "pane.tab.wikiconfig";
	public static final String PANE_TAB_WIKIDISPLAYCONFIG = "pane.tab.wikidisplayconfig";
	
	private static final String[] paneKeys = { PANE_TAB_WIKICONFIG, PANE_TAB_ACCESSIBILITY };

	private static final String CHOSEN_ENTRY = "chosen_entry";
	private static final String CONFIG_KEY_REPOSITORY_SOFTKEY = "reporef";

	private ModuleConfiguration moduleConfiguration;	
	private WikiCourseNode wikiCourseNode;
	private ConditionEditController accessCondContr;
	private TabbedPane tabs;
	private Panel main;
	private VelocityContainer content;
	private ReferencableEntriesSearchController searchController;
	private WikiMainController wikiCtr;
	private CloseableModalController cmcWikiCtr;
	private CloseableModalController cmcSearchController;
	private Link previewLink;
	private Link chooseButton;
	private Link changeButton;
	private Link editLink;
	private VelocityContainer editAccessVc;
	private ConditionEditController editCondContr;
	private ICourse course;
	
	@Autowired
	private RepositoryService repositoryService;

	/**
	 * Constructor for wiki page editor controller
	 * 
	 * @param config The node module configuration
	 * @param ureq The user request
	 * @param wikiCourseNode The current wiki page course node
	 * @param course
	 */
	public WikiEditController(ModuleConfiguration config, UserRequest ureq, WindowControl wControl, WikiCourseNode wikiCourseNode,
			ICourse course, UserCourseEnvironment euce) {
		super(ureq, wControl);
		this.moduleConfiguration = config;
		this.wikiCourseNode = wikiCourseNode;
		//o_clusterOk by guido: save to hold reference to course inside editor
		this.course = course;
		
		main = new Panel("wikimain");
		
		content = createVelocityContainer("edit");
		chooseButton = LinkFactory.createButtonSmall("command.create", content, this);
		chooseButton.setElementCssClass("o_sel_wiki_choose_repofile");
		changeButton = LinkFactory.createButtonSmall("command.change", content, this);
		changeButton.setElementCssClass("o_sel_wiki_choose_repofile");
				
		editAccessVc = this.createVelocityContainer("edit_access");
		CourseEditorTreeModel editorModel = course.getEditorTreeModel();
		// Accessibility precondition
		Condition accessCondition = wikiCourseNode.getPreConditionAccess();
		accessCondContr = new ConditionEditController(ureq, getWindowControl(), euce, accessCondition,
				AssessmentHelper.getAssessableNodes(editorModel, wikiCourseNode));		
		listenTo(accessCondContr);
		editAccessVc.put("readerCondition", accessCondContr.getInitialComponent());
		
		//wiki read / write preconditions
		Condition editCondition = wikiCourseNode.getPreConditionEdit();
		editCondContr = new ConditionEditController(ureq, getWindowControl(), euce, editCondition, AssessmentHelper
				.getAssessableNodes(editorModel, wikiCourseNode));		
		listenTo(editCondContr);
		editAccessVc.put("editCondition", editCondContr.getInitialComponent());
		

		if (config.get(CONFIG_KEY_REPOSITORY_SOFTKEY) != null) {
			// fetch repository entry to display the repository entry title of the
			// chosen wiki
			RepositoryEntry re = getWikiRepoReference(config, false);
			if (re == null) { // we cannot display the entrie's name, because the
				// repository entry had been deleted between the time
				// when it was chosen here, and now				
				this.showError("error.repoentrymissing");
				content.contextPut("showPreviewLink", Boolean.FALSE);
				content.contextPut(CHOSEN_ENTRY, translate("no.entry.chosen"));
			} else {
				// no securitycheck on wiki, editable by everybody
				editLink = LinkFactory.createButtonSmall("edit", content, this);
				content.contextPut("showPreviewLink", Boolean.TRUE);
				String displayname = StringHelper.escapeHtml(re.getDisplayname());
				previewLink = LinkFactory.createCustomLink("command.preview", "command.preview", displayname, Link.NONTRANSLATED, content, this);
				previewLink.setIconLeftCSS("o_icon o_icon-fw o_icon_preview");
				previewLink.setCustomEnabledLinkCSS("o_preview");
				previewLink.setTitle(getTranslator().translate("command.preview"));
			}
		} else {
			// no valid config yet
			content.contextPut("showPreviewLink", Boolean.FALSE);
			content.contextPut(CHOSEN_ENTRY, translate("no.entry.chosen"));
		}

		main.setContent(content);
	}

	@Override
	public void event(UserRequest ureq, Component source, Event event) {
		if (source == previewLink) {				
			doPreview(ureq);
		} else if (source == chooseButton || source == changeButton) {
			searchController = new ReferencableEntriesSearchController(getWindowControl(), ureq, WikiResource.TYPE_NAME, translate("command.choose"));			
			listenTo(searchController);
			cmcSearchController = new CloseableModalController(getWindowControl(), translate("close"), searchController.getInitialComponent(), true, translate("command.create"));
			cmcSearchController.activate();			
		}  else if (source == editLink) {
			RepositoryEntry repositoryEntry = wikiCourseNode.getReferencedRepositoryEntry();
			if (repositoryEntry == null) {
				// do nothing
				return;
			}
			String bPath = "[RepositoryEntry:" + repositoryEntry.getKey() + "][Editor:0]";
			NewControllerFactory.getInstance().launch(bPath, ureq, getWindowControl());
		}
	}

	@Override
	public void event(UserRequest urequest, Controller source, Event event) {
		if (source == searchController) {
			cmcSearchController.deactivate();
			// repository search controller done
			if (event == ReferencableEntriesSearchController.EVENT_REPOSITORY_ENTRY_SELECTED) {
				RepositoryEntry re = searchController.getSelectedEntry();				
				if (re != null) {
					setWikiRepoReference(re, moduleConfiguration);
					content.contextPut("showPreviewLink", Boolean.TRUE);
					String displayname = StringHelper.escapeHtml(re.getDisplayname());
					previewLink = LinkFactory.createCustomLink("command.preview", "command.preview", displayname, Link.NONTRANSLATED, content, this);
					previewLink.setIconLeftCSS("o_icon o_icon-fw o_icon_preview");
					previewLink.setCustomEnabledLinkCSS("o_preview");
					previewLink.setTitle(getTranslator().translate("command.preview"));
					// no securitycheck on wiki, editable by everybody
					editLink = LinkFactory.createButtonSmall("edit", content, this);
					// fire event so the updated config is saved by the
					// editormaincontroller
					fireEvent(urequest, NodeEditController.NODECONFIG_CHANGED_EVENT);
				}
			}	// else cancelled repo search
		} else if (source == accessCondContr) {
			if (event == Event.CHANGED_EVENT) {
				Condition cond = accessCondContr.getCondition();
				wikiCourseNode.setPreConditionAccess(cond);
				fireEvent(urequest, NodeEditController.NODECONFIG_CHANGED_EVENT);
			}
		} else if (source == editCondContr) {
			if (event == Event.CHANGED_EVENT) {
				Condition cond = editCondContr.getCondition();
				wikiCourseNode.setPreConditionEdit(cond);
				fireEvent(urequest, NodeEditController.NODECONFIG_CHANGED_EVENT);
			}
		} else if (source == cmcWikiCtr) {
			if (event == CloseableModalController.CLOSE_MODAL_EVENT) {
				cmcWikiCtr.dispose();
				wikiCtr.dispose();
			}
		} 
	}
	
	private void doPreview(UserRequest ureq) {
		// Preview as modal dialogue only if the config is valid		
		RepositoryEntry re = getWikiRepoReference(moduleConfiguration, false);
		if (re == null) { // we cannot preview it, because the repository entry
			// had been deleted between the time when it was
			// chosen here, and now				
			showError("error.repoentrymissing");
		} else {
			Roles roles = ureq.getUserSession().getRoles();
			boolean isAdministrator = (roles.isAdministrator() || roles.isLearnResourceManager())
					&& repositoryService.hasRoleExpanded(getIdentity(), re,
							OrganisationRoles.administrator.name(), OrganisationRoles.learnresourcemanager.name());
			boolean isResourceOwner = repositoryService.hasRole(getIdentity(), re, GroupRoles.owner.name());
			
			CourseEnvironment cenv = course.getCourseEnvironment();
			SubscriptionContext subsContext = WikiManager.createTechnicalSubscriptionContextForCourse(cenv, wikiCourseNode);
			WikiSecurityCallback callback = new WikiSecurityCallbackImpl(null, isAdministrator, false, false, isResourceOwner, subsContext);
			wikiCtr = WikiManager.getInstance().createWikiMainController(ureq, getWindowControl(), re.getOlatResource(), callback, null);
			cmcWikiCtr = new CloseableModalController(getWindowControl(), translate("command.close"), wikiCtr.getInitialComponent());				
			listenTo(cmcWikiCtr);
			cmcWikiCtr.activate();
		}
	}

	@Override
	public void addTabs(TabbedPane tabbedPane) {
		tabs = tabbedPane;
		tabbedPane.addTab(translate(PANE_TAB_ACCESSIBILITY), editAccessVc);
		tabbedPane.addTab(translate(PANE_TAB_WIKICONFIG), main);
	}

	@Override
	protected void doDispose() {
    //child controllers registered with listenTo() get disposed in BasicController
		if (wikiCtr != null) {
			wikiCtr.dispose();
			wikiCtr = null;
		}		
	}

	@Override
	public String[] getPaneKeys() {
		return paneKeys;
	}

	@Override
	public TabbedPane getTabbedPane() {
		return tabs;
	}

	/**
	 * @param config the moduleConfig
	 * @param strict an assertion exception is thrown if no entry is found when
	 *          strict is set to true, otherwise, null is returned
	 * @return the repositoryentry or null if not in strict mode and no entry
	 *         found
	 * @throws AssertException when in strict mode and no entry is found
	 */
	public static RepositoryEntry getWikiRepoReference(ModuleConfiguration config, boolean strict) {
		if (config == null) throw new AssertException("missing config in wiki course node");
		String repoSoftkey = (String) config.get(WikiEditController.CONFIG_KEY_REPOSITORY_SOFTKEY);
		if (repoSoftkey == null) throw new AssertException("invalid config when being asked for references");
		RepositoryManager rm = RepositoryManager.getInstance();
		return rm.lookupRepositoryEntryBySoftkey(repoSoftkey, strict);
	}

	/**
	 * set an repository reference to an wiki course node
	 * @param re
	 * @param moduleConfiguration
	 */
	public static void setWikiRepoReference(RepositoryEntry re, ModuleConfiguration moduleConfiguration) {
		moduleConfiguration.set(CONFIG_KEY_REPOSITORY_SOFTKEY, re.getSoftkey());
	}
	
	/**
	 * @param moduleConfiguration
	 * @return boolean
	 */
	public static boolean isModuleConfigValid(ModuleConfiguration moduleConfiguration) {
		return (moduleConfiguration.get(CONFIG_KEY_REPOSITORY_SOFTKEY) != null);
	}

	
	/**
	 * @param config the moduleConfig
	 * @param strict an assertion exception is thrown if no entry is found when
	 *          strict is set to true, otherwise, null is returned
	 * @return the repositoryentry or null if not in strict mode and no entry
	 *         found
	 * @throws AssertException when in strict mode and no entry is found
	 */
	public static RepositoryEntry getWikiReference(ModuleConfiguration config, boolean strict) {
		if (config == null) {
			if (strict) throw new AssertException("missing config in Wiki");
			else return null;
		}
		String repoSoftkey = (String) config.get(WikiEditController.CONFIG_KEY_REPOSITORY_SOFTKEY);
		if (repoSoftkey == null) {
			if (strict) throw new AssertException("invalid config when being asked for references");
			else return null;
		}
		RepositoryManager rm = RepositoryManager.getInstance();
		return rm.lookupRepositoryEntryBySoftkey(repoSoftkey, strict);
	}

	/**
	 * remove ref to wiki from the config
	 * @param moduleConfig
	 */
	public static void removeWikiReference(ModuleConfiguration moduleConfig) {
		moduleConfig.remove(WikiEditController.CONFIG_KEY_REPOSITORY_SOFTKEY);
	}
	
}
