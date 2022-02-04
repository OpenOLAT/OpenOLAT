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
package org.olat.modules.assessment.ui;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.generic.spacesaver.ExpandController;
import org.olat.course.assessment.model.SearchAssessedIdentityParams;
import org.olat.repository.RepositoryEntry;

/**
 * 
 * Initial date: 19 Jan 2022<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class AssessmentStatisticsController extends BasicController {
	
	private final ExpandController expandCtrl;
	private final AssessmentStatsController statsCtrl;

	public AssessmentStatisticsController(UserRequest ureq, WindowControl wControl, RepositoryEntry courseEntry,
			AssessmentToolSecurityCallback assessmentCallback, SearchAssessedIdentityParams params) {
		super(ureq, wControl);
		VelocityContainer mainVC = createVelocityContainer("statistic");
		
		expandCtrl = new ExpandController(ureq, wControl, "assessment-statistic-" + courseEntry.getKey().toString());
		listenTo(expandCtrl);
		mainVC.put("expand", expandCtrl.getInitialComponent());
		
		statsCtrl = new AssessmentStatsController(ureq, getWindowControl(), assessmentCallback, params, true, false);
		listenTo(statsCtrl);
		expandCtrl.setExpandableController(statsCtrl);
		
		putInitialPanel(mainVC);
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if (source == statsCtrl) {
			fireEvent(ureq, event);
		}
		super.event(ureq, source, event);
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		//
	}

}
