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
package org.olat.course.assessment.ui.tool;

import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.generic.dtabs.Activateable2;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.id.context.StateEntry;
import org.olat.modules.assessment.ui.AssessmentToolSecurityCallback;
import org.olat.repository.RepositoryEntry;

/**
 * 
 * Initial date: 07.10.2015<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class AssessmentCourseOverviewController extends BasicController implements Activateable2 {
	
	protected static final Event SELECT_USERS_EVENT = new Event("assessment-tool-select-users");
	
	private final VelocityContainer mainVC;
	private final AssessmentToReviewSmallController toReviewCtrl;
	private final AssessmentCourseStatisticsSmallController statisticsCtrl;

	private Link assessedIdentitiesLink;
	
	public AssessmentCourseOverviewController(UserRequest ureq, WindowControl wControl,
			RepositoryEntry courseEntry, AssessmentToolSecurityCallback assessmentCallback) {
		super(ureq, wControl);
		
		mainVC = createVelocityContainer("course_overview");
		
		toReviewCtrl = new AssessmentToReviewSmallController(ureq, getWindowControl(), courseEntry, assessmentCallback);
		listenTo(toReviewCtrl);
		mainVC.put("toReview", toReviewCtrl.getInitialComponent());
		
		statisticsCtrl = new AssessmentCourseStatisticsSmallController(ureq, getWindowControl(), courseEntry, assessmentCallback);
		listenTo(statisticsCtrl);
		mainVC.put("statistics", statisticsCtrl.getInitialComponent());
		
		int numOfAssessedIdentities = statisticsCtrl.getNumOfAssessedIdentities();
		assessedIdentitiesLink = LinkFactory.createLink("assessed.identities", "assessed.identities", getTranslator(), mainVC, this, Link.NONTRANSLATED);
		assessedIdentitiesLink.setCustomDisplayText(translate("assessment.tool.numOfAssessedIdentities", new String[]{ Integer.toString(numOfAssessedIdentities) }));

		putInitialPanel(mainVC);
	}

	@Override
	protected void doDispose() {
		//
	}

	@Override
	public void activate(UserRequest ureq, List<ContextEntry> entries, StateEntry state) {
		//
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if(assessedIdentitiesLink == source) {
			fireEvent(ureq, SELECT_USERS_EVENT);
		}
	}
}
