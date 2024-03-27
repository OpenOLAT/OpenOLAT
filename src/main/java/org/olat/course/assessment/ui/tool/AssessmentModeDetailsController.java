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
package org.olat.course.assessment.ui.tool;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.course.assessment.AssessmentMode;
import org.olat.course.assessment.AssessmentModeCoordinationService;
import org.olat.course.assessment.model.AssessmentModeStatistics;
import org.olat.course.assessment.ui.mode.AssessmentModeHelper;
import org.olat.course.assessment.ui.tool.component.AssessmentModeProgressionItem;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Initial date: Mar 25, 2024
 *
 * @author skapoor, sumit.kapoor@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
public class AssessmentModeDetailsController extends BasicController {

	private final VelocityContainer mainVC;

	@Autowired
	private AssessmentModeCoordinationService assessmentModeCoordinationService;

	protected AssessmentModeDetailsController(UserRequest ureq, WindowControl wControl, AssessmentMode mode) {
		super(ureq, wControl, Util.createPackageTranslator(AssessmentModeHelper.class, ureq.getLocale()));

		mainVC = createVelocityContainer("assessment_details");
		AssessmentModeHelper helper = new AssessmentModeHelper(getTranslator());

		mainVC.contextPut("title", "<i class='o_icon o_icon_assessment_mode'>  </i>" + "&nbsp;" + StringHelper.escapeHtml(mode.getName()));
		mainVC.contextPut("modeState", helper.getModeState(mode));
		mainVC.contextPut("beginEnd", helper.getBeginEndTooltip(mode));
		mainVC.contextPut("leadFollowUpTime", helper.getLeadFollowupTime(mode));
		forgeStatistics(mode);

		putInitialPanel(mainVC);
	}

	private void forgeStatistics(AssessmentMode mode) {
		AssessmentModeStatistics statistics = assessmentModeCoordinationService.getStatistics(mode);
		if (statistics != null) {
			statistics.setStatus(mode.getStatus());// direct from the database
			AssessmentModeProgressionItem waitBarItem = new AssessmentModeProgressionItem("tooltipId", mode, getTranslator());
			waitBarItem.setMax(statistics.getNumPlanned());
			waitBarItem.setActual(statistics.getNumInOpenOlat());
			mainVC.put("modeItem", waitBarItem.getComponent());
		}
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		//
	}
}
