/**
 * <a href="https://www.openolat.org">
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
 * frentix GmbH, https://www.frentix.com
 * <p>
 */

package org.olat.course.nodes.portfolio;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.stack.BreadcrumbPanel;
import org.olat.core.gui.components.tabbedpane.TabbedPane;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.tabbable.ActivateableTabbableDefaultController;
import org.olat.course.ICourse;
import org.olat.course.editor.NodeEditController;
import org.olat.course.highscore.ui.HighScoreEditController;
import org.olat.course.nodeaccess.NodeAccessType;
import org.olat.course.nodes.MSCourseNode;
import org.olat.course.nodes.PortfolioCourseNode;
import org.olat.course.nodes.ms.MSEditFormController;
import org.olat.modules.ModuleConfiguration;
import org.olat.modules.assessment.AssessmentService;
import org.olat.repository.RepositoryEntry;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Initial Date:  6 oct. 2010 <br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 */
public class PortfolioCourseNodeEditController extends ActivateableTabbableDefaultController {
	
	public static final String PANE_TAB_CONFIG = "pane.tab.portfolio_config";
	public static final String PANE_TAB_SCORING = "pane.tab.portfolio_scoring";
	public static final String PANE_TAB_HIGHSCORE = "pane.tab.highscore";
	static final String[] paneKeys = { PANE_TAB_CONFIG, PANE_TAB_SCORING };
	
	private final VelocityContainer configContent;
	private final PortfolioConfigForm configForm;
	private final VelocityContainer scoringContent;
	private final MSEditFormController scoringController;
	private final Link enableEditingLink;
	private final HighScoreEditController highScoreNodeConfigController;
	
	private TabbedPane myTabbedPane;
	
	private final boolean hasAssessments;
	private final ModuleConfiguration config;
	private final PortfolioCourseNode courseNode;

	@Autowired
	private AssessmentService assessmentService;

	public PortfolioCourseNodeEditController(UserRequest ureq, WindowControl wControl, BreadcrumbPanel stackPanel,
			ICourse course, PortfolioCourseNode node, ModuleConfiguration config) {
		super(ureq, wControl);
		this.config = config;
		this.courseNode = node;
		
		configForm = new PortfolioConfigForm(ureq, wControl, stackPanel, course, node);
		listenTo(configForm);
		scoringController = new MSEditFormController(ureq, wControl, course, node, NodeAccessType.of(course),
				translate("grading.configuration.title"), "manual_user/learningresources/Portfolio_assignment_Grading/");
		listenTo(scoringController);
		
		scoringContent = createVelocityContainer("edit_scoring");
		scoringContent.put("assessmentform", scoringController.getInitialComponent());
		enableEditingLink = LinkFactory.createButtonSmall("enable.editing", scoringContent, this);
		enableEditingLink.setPrimary(true);
		enableEditingLink.setIconLeftCSS("o_icon o_icon-fw o_icon_unlocked");

		configContent = createVelocityContainer("edit");
		configContent.put("configForm", configForm.getInitialComponent());
		
		//highscore
		highScoreNodeConfigController = new HighScoreEditController(ureq, wControl, config, course);
		listenTo(highScoreNodeConfigController);
		
		// if there are already assessments, make read only
		RepositoryEntry courseEntry = course.getCourseEnvironment().getCourseGroupManager().getCourseEntry();
		hasAssessments = assessmentService.hasAssessments(courseEntry, node.getIdent());
		scoringContent.contextPut("hasAssessments", Boolean.valueOf(hasAssessments));
		if (hasAssessments) {
			scoringController.setDisplayOnly(true);
		}
		//Initialstate
		scoringContent.contextPut("isOverwriting", Boolean.valueOf(false));
	}
	
	public static boolean isModuleConfigValid(ModuleConfiguration moduleConfiguration) {
		return (moduleConfiguration.get(PortfolioCourseNodeConfiguration.MAP_KEY) != null)
				|| (moduleConfiguration.get(PortfolioCourseNodeConfiguration.REPO_SOFT_KEY) != null);
	}
	
	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if (source == enableEditingLink) {
			scoringController.setDisplayOnly(false);
			scoringContent.contextPut("isOverwriting", Boolean.TRUE);
		}
	}
	
	@Override
	public void event(UserRequest ureq, Controller source, Event event) {
		if (source == configForm) {
			if (event == Event.DONE_EVENT) {
				configForm.getUpdatedConfig();
				fireEvent(ureq, NodeEditController.NODECONFIG_CHANGED_EVENT);
				configContent.setDirty(true);
			} else if (event == NodeEditController.NODECONFIG_CHANGED_EVENT) {
				fireEvent(ureq, event);
			}
		} else if (source == scoringController) {
			if (event == Event.CANCELLED_EVENT) {
				if (hasAssessments) {
					scoringController.setDisplayOnly(true);}
				scoringContent.contextPut("isOverwriting", Boolean.valueOf(false));
			} else if (event == Event.DONE_EVENT){
				scoringController.updateModuleConfiguration(config);
				updateHighscoreTab();
				fireEvent(ureq, NodeEditController.NODECONFIG_CHANGED_EVENT);
				fireEvent(ureq, NodeEditController.REMINDER_VISIBILITY_EVENT);
			}
		} else if (source == highScoreNodeConfigController){
			if (event == Event.DONE_EVENT) {
				fireEvent(ureq, NodeEditController.NODECONFIG_CHANGED_EVENT);
			}
		}
	}
	
	private void updateHighscoreTab() {
		Boolean sf = courseNode.getModuleConfiguration().getBooleanSafe(MSCourseNode.CONFIG_KEY_HAS_SCORE_FIELD,false);
		myTabbedPane.setEnabled(myTabbedPane.indexOfTab(highScoreNodeConfigController.getInitialComponent()), sf);
	}

	@Override
	public void addTabs(TabbedPane tabbedPane) {
		myTabbedPane = tabbedPane;
		tabbedPane.addTab(translate(PANE_TAB_CONFIG), "o_sel_repo_entry", configContent);
		tabbedPane.addTab(translate(PANE_TAB_SCORING), "o_sel_portfolio_assessment", scoringContent);
		tabbedPane.addTab(translate(PANE_TAB_HIGHSCORE), highScoreNodeConfigController.getInitialComponent());
		updateHighscoreTab();
	}

	@Override
	public String[] getPaneKeys() {
		return paneKeys;
	}

	@Override
	public TabbedPane getTabbedPane() {
		return myTabbedPane;
	}
	
	public static void removeReference(ModuleConfiguration moduleConfig) {
		moduleConfig.remove(PortfolioCourseNodeConfiguration.MAP_KEY);
		moduleConfig.remove(PortfolioCourseNodeConfiguration.REPO_SOFT_KEY);
	}
	
	public static void setReference(RepositoryEntry repoEntry, ModuleConfiguration moduleConfig) {
		if(repoEntry != null && repoEntry.getSoftkey() != null) {
			moduleConfig.set(PortfolioCourseNodeConfiguration.REPO_SOFT_KEY, repoEntry.getSoftkey());
		}
	}
}
