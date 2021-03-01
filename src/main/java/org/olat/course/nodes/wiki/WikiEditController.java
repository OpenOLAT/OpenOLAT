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

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.stack.BreadcrumbPanel;
import org.olat.core.gui.components.tabbedpane.TabbedPane;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.ControllerEventListener;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.tabbable.ActivateableTabbableDefaultController;
import org.olat.core.logging.AssertException;
import org.olat.course.ICourse;
import org.olat.course.assessment.AssessmentHelper;
import org.olat.course.condition.Condition;
import org.olat.course.condition.ConditionEditController;
import org.olat.course.editor.NodeEditController;
import org.olat.course.nodes.WikiCourseNode;
import org.olat.course.run.userview.UserCourseEnvironment;
import org.olat.course.tree.CourseEditorTreeModel;
import org.olat.modules.ModuleConfiguration;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryManager;

/**
 * Description: <BR/>Edit controller for single page course nodes <P/> Initial
 * Date: Oct 12, 2004
 * 
 * @author Felix Jost
 */
public class WikiEditController extends ActivateableTabbableDefaultController implements ControllerEventListener {
	public static final String PANE_TAB_ACCESSIBILITY = "pane.tab.accessibility";
	public static final String PANE_TAB_WIKICONFIG = "pane.tab.wikiconfig";
	
	private static final String[] paneKeys = { PANE_TAB_WIKICONFIG, PANE_TAB_ACCESSIBILITY };

	private TabbedPane tabs;
	private Controller configCtrl;
	private VelocityContainer editAccessVc;
	private ConditionEditController accessCondCtrl;
	private ConditionEditController editCondContr;

	private final WikiCourseNode wikiCourseNode;
	
	public WikiEditController(UserRequest ureq, WindowControl wControl, BreadcrumbPanel stackPanel,
			WikiCourseNode wikiCourseNode, ICourse course, UserCourseEnvironment euce) {
		super(ureq, wControl);
		this.wikiCourseNode = wikiCourseNode;
		
		configCtrl = new WikiConfigsController(ureq, wControl, stackPanel, wikiCourseNode, course);
		listenTo(configCtrl);
		
		if (wikiCourseNode.hasCustomPreConditions()) {
			editAccessVc = this.createVelocityContainer("edit_access");
			CourseEditorTreeModel editorModel = course.getEditorTreeModel();
			// Accessibility precondition
			Condition accessCondition = wikiCourseNode.getPreConditionAccess();
			accessCondCtrl = new ConditionEditController(ureq, getWindowControl(), euce, accessCondition,
					AssessmentHelper.getAssessableNodes(editorModel, wikiCourseNode));
			listenTo(accessCondCtrl);
			editAccessVc.put("readerCondition", accessCondCtrl.getInitialComponent());
			
			//wiki read / write preconditions
			Condition editCondition = wikiCourseNode.getPreConditionEdit();
			editCondContr = new ConditionEditController(ureq, getWindowControl(), euce, editCondition, AssessmentHelper
					.getAssessableNodes(editorModel, wikiCourseNode));
			listenTo(editCondContr);
			editAccessVc.put("editCondition", editCondContr.getInitialComponent());
		}
	}

	@Override
	public void event(UserRequest ureq, Component source, Event event) {
		//
	}

	@Override
	public void event(UserRequest ureq, Controller source, Event event) {
		if (source == configCtrl) {
			fireEvent(ureq, event);
		} else if (source == accessCondCtrl) {
			if (event == Event.CHANGED_EVENT) {
				Condition cond = accessCondCtrl.getCondition();
				wikiCourseNode.setPreConditionAccess(cond);
				fireEvent(ureq, NodeEditController.NODECONFIG_CHANGED_EVENT);
			}
		} else if (source == editCondContr) {
			if (event == Event.CHANGED_EVENT) {
				Condition cond = editCondContr.getCondition();
				wikiCourseNode.setPreConditionEdit(cond);
				fireEvent(ureq, NodeEditController.NODECONFIG_CHANGED_EVENT);
			}
		}
	}

	@Override
	public void addTabs(TabbedPane tabbedPane) {
		tabs = tabbedPane;
		if (editAccessVc != null) {
			tabbedPane.addTab(translate(PANE_TAB_ACCESSIBILITY), editAccessVc);
			
		}
		tabbedPane.addTab(translate(PANE_TAB_WIKICONFIG), configCtrl.getInitialComponent());
	}

	@Override
	protected void doDispose() {
		//
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
		String repoSoftkey = (String) config.get(WikiCourseNode.CONFIG_KEY_REPOSITORY_SOFTKEY);
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
		moduleConfiguration.set(WikiCourseNode.CONFIG_KEY_REPOSITORY_SOFTKEY, re.getSoftkey());
	}
	
	/**
	 * @param moduleConfiguration
	 * @return boolean
	 */
	public static boolean isModuleConfigValid(ModuleConfiguration moduleConfiguration) {
		return (moduleConfiguration.get(WikiCourseNode.CONFIG_KEY_REPOSITORY_SOFTKEY) != null);
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
		String repoSoftkey = (String) config.get(WikiCourseNode.CONFIG_KEY_REPOSITORY_SOFTKEY);
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
		moduleConfig.remove(WikiCourseNode.CONFIG_KEY_REPOSITORY_SOFTKEY);
	}
	
}
