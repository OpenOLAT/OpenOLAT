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

import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.generic.dtabs.Activateable2;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.id.context.StateEntry;
import org.olat.core.util.Util;
import org.olat.course.assessment.AssessmentModule;
import org.olat.modules.assessment.ui.event.UserSelectionEvent;
import org.olat.repository.RepositoryEntry;

/**
 * 
 * Initial date: 24.05.2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class AssessmentOverviewController extends BasicController implements Activateable2 {
		
	protected static final Event SELECT_USERS_EVENT = new Event("assessment-tool-select-users");
	protected static final Event SELECT_PASSED_EVENT = new Event("assessment-tool-select-passed");
	protected static final Event SELECT_FAILED_EVENT = new Event("assessment-tool-select-failed");
	
	private final VelocityContainer mainVC;
	private final AssessmentToReviewSmallController toReviewCtrl;
	private final AssessmentStatisticsSmallController statisticsCtrl;
	
	private final Link passedLink;
	private final Link failedLink;
	private final Link assessedIdentitiesLink;
		
	public AssessmentOverviewController(UserRequest ureq, WindowControl wControl,
			RepositoryEntry testEntry, AssessmentToolSecurityCallback assessmentCallback) {
		super(ureq, wControl, Util.createPackageTranslator(AssessmentModule.class, ureq.getLocale()));
		
		mainVC = createVelocityContainer("overview");
		
		toReviewCtrl = new AssessmentToReviewSmallController(ureq, getWindowControl(), testEntry, assessmentCallback);
		listenTo(toReviewCtrl);
		mainVC.put("toReview", toReviewCtrl.getInitialComponent());
		
		statisticsCtrl = new AssessmentStatisticsSmallController(ureq, getWindowControl(), testEntry, assessmentCallback);
		listenTo(statisticsCtrl);
		mainVC.put("statistics", statisticsCtrl.getInitialComponent());
		
		int numOfParticipants = statisticsCtrl.getMemberStatistics().getNumOfParticipants();
		int numOfOtherUsers = statisticsCtrl.getMemberStatistics().getNumOfOtherUsers();
		String[] args = new String[]{ Integer.toString(numOfParticipants), Integer.toString(numOfOtherUsers) };
		String assessedIdentitiesText = numOfOtherUsers > 0
				? translate("assessment.tool.num.assessed.participants.others", args)
				: translate("assessment.tool.num.assessed.participants", args);
		assessedIdentitiesLink = LinkFactory.createLink("assessed.identities", "assessed.identities", getTranslator(), mainVC, this, Link.NONTRANSLATED);
		assessedIdentitiesLink.setCustomDisplayText(assessedIdentitiesText);
		assessedIdentitiesLink.setIconLeftCSS("o_icon o_icon_user o_icon-fw");
		
		int numOfPassed = statisticsCtrl.getNumOfPassed();
		passedLink = LinkFactory.createLink("passed.identities", "passed.identities", getTranslator(), mainVC, this, Link.NONTRANSLATED);
		passedLink.setCustomDisplayText(translate("assessment.tool.numOfPassed", new String[]{ Integer.toString(numOfPassed) }));
		passedLink.setIconLeftCSS("o_passed o_icon o_icon_passed o_icon-fw");

		int numOfFailed = statisticsCtrl.getNumOfFailed();
		failedLink = LinkFactory.createLink("failed.identities", "failed.identities", getTranslator(), mainVC, this, Link.NONTRANSLATED);
		failedLink.setCustomDisplayText(translate("assessment.tool.numOfFailed", new String[]{ Integer.toString(numOfFailed) }));
		failedLink.setIconLeftCSS("o_failed o_icon o_icon_failed o_icon-fw");

		putInitialPanel(mainVC);
	}

	@Override
	public void activate(UserRequest ureq, List<ContextEntry> entries, StateEntry state) {
		//
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(toReviewCtrl == source) {
			if(event instanceof UserSelectionEvent) {
				fireEvent(ureq, event);
			}
		}
		super.event(ureq, source, event);
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if(assessedIdentitiesLink == source) {
			fireEvent(ureq, SELECT_USERS_EVENT);
		} else if(passedLink == source) {
			fireEvent(ureq, SELECT_PASSED_EVENT);
		} else if(failedLink == source) {
			fireEvent(ureq, SELECT_FAILED_EVENT);
		}
	}
}
