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
package org.olat.course.learningpath.ui;

import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.stack.TooledController;
import org.olat.core.gui.components.stack.TooledStackedPanel;
import org.olat.core.gui.components.stack.TooledStackedPanel.Align;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.helpers.Settings;
import org.olat.course.run.userview.UserCourseEnvironment;
import org.olat.modules.assessment.AssessmentEntry;
import org.olat.modules.assessment.AssessmentService;
import org.olat.repository.RepositoryEntry;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 16 Aug 2019<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class IdentityOverviewController extends BasicController implements TooledController {

	private final TooledStackedPanel stackPanel;
	private final VelocityContainer mainVC;
	private Link resetStatusLink;
	
	private LearningPathListController learningPathListController;
	
	private RepositoryEntry courseEntry;
	
	@Autowired
	private AssessmentService assessmentService;

	public IdentityOverviewController(UserRequest ureq, WindowControl wControl, TooledStackedPanel stackPanel,
			UserCourseEnvironment userCourseEnvironment) {
		super(ureq, wControl);
		this.stackPanel = stackPanel;
		this.courseEntry = userCourseEnvironment.getCourseEnvironment().getCourseGroupManager().getCourseEntry();
		
		mainVC = createVelocityContainer("identity_overview");
		
		learningPathListController = new LearningPathListController(ureq, wControl, userCourseEnvironment);
		listenTo(learningPathListController);
		mainVC.put("list", learningPathListController.getInitialComponent());
		
		putInitialPanel(mainVC);
	}
	
	@Override
	public void initTools() {
		if (Settings.isDebuging()) {
			resetStatusLink = LinkFactory.createToolLink("reset.all.status", translate("reset.all.status"), this);
			resetStatusLink.setIconLeftCSS("o_icon o_icon-lg o_icon_exclamation");
			stackPanel.addTool(resetStatusLink, Align.right);
		}
	}
	
	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if (source == resetStatusLink) {
			doResetStatus();
		}
	}

	private void doResetStatus() {
		List<AssessmentEntry> assessmentEntries = assessmentService.loadAssessmentEntriesByAssessedIdentity(getIdentity(), courseEntry);
		for (AssessmentEntry assessmentEntry : assessmentEntries) {
			assessmentEntry.setFullyAssessed(null);
			assessmentEntry.setAssessmentStatus(null);
			assessmentService.updateAssessmentEntry(assessmentEntry);
		}
		learningPathListController.loadModel();
	}

	@Override
	protected void doDispose() {
		//
	}

}
