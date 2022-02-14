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

import java.util.ArrayList;
import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.util.Util;
import org.olat.course.assessment.AssessmentModule;
import org.olat.course.assessment.model.SearchAssessedIdentityParams;
import org.olat.modules.assessment.ui.event.UserSelectionEvent;
import org.olat.repository.RepositoryEntry;

/**
 * 
 * Initial date: 24.05.2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class AssessmentOverviewController extends BasicController {
	
	private final VelocityContainer mainVC;
	private final AssessmentToReviewSmallController toReviewCtrl;
	private final AssessmentStatsController statisticCtrl;
		
	public AssessmentOverviewController(UserRequest ureq, WindowControl wControl, RepositoryEntry testEntry,
			AssessableResource element, AssessmentToolSecurityCallback assessmentCallback) {
		super(ureq, wControl, Util.createPackageTranslator(AssessmentModule.class, ureq.getLocale()));
		
		mainVC = createVelocityContainer("overview");
		
		toReviewCtrl = new AssessmentToReviewSmallController(ureq, getWindowControl(), testEntry, assessmentCallback);
		listenTo(toReviewCtrl);
		mainVC.put("toReview", toReviewCtrl.getInitialComponent());
		
		SearchAssessedIdentityParams params = new SearchAssessedIdentityParams(testEntry, null, testEntry, assessmentCallback);
		List<Stat> stats = new ArrayList<>(2);
		if (element.hasPassedConfigured()) {
			stats.add(Stat.passed);
		} else {
			stats.add(Stat.status);
		}
		if (element.hasScoreConfigured()) {
			stats.add(Stat.score);
		}
		statisticCtrl = new AssessmentStatsController(ureq, getWindowControl(), assessmentCallback, params, stats, true, false);
		statisticCtrl.setExpanded(true);
		listenTo(statisticCtrl);
		mainVC.put("statistics", statisticCtrl.getInitialComponent());
		
		putInitialPanel(mainVC);
	}


	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if (statisticCtrl == source) {
			fireEvent(ureq, event);
		} else if (toReviewCtrl == source) {
			if(event instanceof UserSelectionEvent) {
				fireEvent(ureq, event);
			}
		}
		super.event(ureq, source, event);
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		//
	}
}
