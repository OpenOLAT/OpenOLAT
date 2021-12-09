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

import org.olat.core.commons.services.notifications.PublisherData;
import org.olat.core.commons.services.notifications.SubscriptionContext;
import org.olat.core.commons.services.notifications.ui.ContextualSubscriptionController;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.generic.messages.MessagePanelController;
import org.olat.core.util.Util;
import org.olat.course.CourseFactory;
import org.olat.course.ICourse;
import org.olat.course.assessment.AssessmentHelper;
import org.olat.course.assessment.AssessmentModule;
import org.olat.course.assessment.manager.AssessmentNotificationsHandler;
import org.olat.course.assessment.ui.tool.event.AssessmentModeStatusEvent;
import org.olat.course.assessment.ui.tool.event.CourseNodeEvent;
import org.olat.course.assessment.ui.tool.event.CourseNodeIdentityEvent;
import org.olat.course.certificate.CertificatesManager;
import org.olat.course.nodes.STCourseNode;
import org.olat.course.run.userview.UserCourseEnvironment;
import org.olat.modules.assessment.ui.AssessmentToolSecurityCallback;
import org.olat.repository.RepositoryEntry;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 07.10.2015<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class AssessmentCourseOverviewController extends BasicController {
	
	protected static final Event SELECT_USERS_EVENT = new Event("assessment-tool-select-users");
	protected static final Event SELECT_NODES_EVENT = new Event("assessment-tool-select-nodes");
	protected static final Event SELECT_PASSED_EVENT = new Event("assessment-tool-select-passed");
	protected static final Event SELECT_FAILED_EVENT = new Event("assessment-tool-select-failed");
	
	private final VelocityContainer mainVC;
	private final CourseNodeToReviewSmallController toReviewCtrl;
	private final Controller toReleaseCtrl;
	private final AssessmentModeOverviewListController assessmentModeListCtrl;
	private final AssessmentCourseStatisticsSmallController statisticsCtrl;

	private Link passedLink;
	private Link failedLink;
	private Link assessedIdentitiesLink;
	private Link assessableCoureNodesLink;

	@Autowired
	private CertificatesManager certificatesManager;
	@Autowired
	private AssessmentNotificationsHandler assessmentNotificationsHandler;
	
	public AssessmentCourseOverviewController(UserRequest ureq, WindowControl wControl,
			RepositoryEntry courseEntry, UserCourseEnvironment coachUserEnv, AssessmentToolSecurityCallback assessmentCallback) {
		super(ureq, wControl);
		setTranslator(Util.createPackageTranslator(AssessmentModule.class, getLocale(), getTranslator()));
		
		mainVC = createVelocityContainer("course_overview");
		
		ICourse course = CourseFactory.loadCourse(courseEntry);
		boolean hasAssessableNodes = course.hasAssessableNodes();
		mainVC.contextPut("hasAssessableNodes", Boolean.valueOf(hasAssessableNodes));
		
		// assessment changes subscription
		if (hasAssessableNodes) {
			SubscriptionContext subsContext = assessmentNotificationsHandler.getAssessmentSubscriptionContext(ureq.getIdentity(), course);
			if (subsContext != null) {
				PublisherData pData = assessmentNotificationsHandler.getAssessmentPublisherData(course, wControl.getBusinessControl().getAsString());
				Controller csc = new ContextualSubscriptionController(ureq, wControl, subsContext, pData);
				listenTo(csc); // cleanup on dispose
				mainVC.put("assessmentSubscription", csc.getInitialComponent());
			}
		}
		
		// certificate subscription
		SubscriptionContext subsContext = certificatesManager.getSubscriptionContext(course);
		if (subsContext != null) {
			String businessPath = wControl.getBusinessControl().getAsString();
			PublisherData pData = certificatesManager.getPublisherData(course, businessPath);
			Controller certificateSubscriptionCtrl = new ContextualSubscriptionController(ureq, wControl, subsContext, pData);
			listenTo(certificateSubscriptionCtrl);
			mainVC.put("certificationSubscription", certificateSubscriptionCtrl.getInitialComponent());
		}
		
		toReviewCtrl = new CourseNodeToReviewSmallController(ureq, getWindowControl(), courseEntry, assessmentCallback);
		listenTo(toReviewCtrl);
		mainVC.put("toReview", toReviewCtrl.getInitialComponent());
		
		if (coachUserEnv.isAdmin() || course.getRunStructure().getRootNode().getModuleConfiguration().getBooleanSafe(STCourseNode.CONFIG_COACH_USER_VISIBILITY, true)) {
			toReleaseCtrl = new CourseNodeToReleaseSmallController(ureq, getWindowControl(), courseEntry, assessmentCallback);
		} else {
			toReleaseCtrl = new MessagePanelController(ureq, wControl, "o_icon_results_hidden",
					translate("user.visibility.hidden.title"), translate("user.visibility.hidden.owner.only"));
		}
		listenTo(toReleaseCtrl);
		mainVC.put("toRelease", toReleaseCtrl.getInitialComponent());
		
		statisticsCtrl = new AssessmentCourseStatisticsSmallController(ureq, getWindowControl(), courseEntry, assessmentCallback);
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
		assessedIdentitiesLink.setElementCssClass("o_sel_assessment_tool_assessed_users");
		assessedIdentitiesLink.setIconLeftCSS("o_icon o_icon_user o_icon-fw");
		
		int numOfPassed = statisticsCtrl.getNumOfPassed();
		passedLink = LinkFactory.createLink("passed.identities", "passed.identities", getTranslator(), mainVC, this, Link.NONTRANSLATED);
		passedLink.setCustomDisplayText(translate("assessment.tool.numOfPassed", new String[]{ Integer.toString(numOfPassed) }));
		passedLink.setIconLeftCSS("o_passed o_icon o_icon_passed o_icon-fw");

		int numOfFailed = statisticsCtrl.getNumOfFailed();
		failedLink = LinkFactory.createLink("failed.identities", "failed.identities", getTranslator(), mainVC, this, Link.NONTRANSLATED);
		failedLink.setCustomDisplayText(translate("assessment.tool.numOfFailed", new String[]{ Integer.toString(numOfFailed) }));
		failedLink.setIconLeftCSS("o_failed o_icon o_icon_failed o_icon-fw");
		
		int numOfAssessableCourseNodes = hasAssessableNodes ?
				AssessmentHelper.countAssessableNodes(course.getRunStructure().getRootNode()) : 0;
		assessableCoureNodesLink = LinkFactory.createLink("assessable.nodes", "assessable.nodes", getTranslator(), mainVC, this, Link.NONTRANSLATED);
		assessableCoureNodesLink.setCustomDisplayText(translate("assessment.tool.numOfAssessableCourseNodes", new String[]{ Integer.toString(numOfAssessableCourseNodes) }));
		assessableCoureNodesLink.setElementCssClass("o_sel_assessment_tool_assessable_course_nodes");
		assessableCoureNodesLink.setIconLeftCSS("o_icon o_ms_icon o_icon-fw");
		
		assessmentModeListCtrl = new AssessmentModeOverviewListController(ureq, getWindowControl(), courseEntry, assessmentCallback);
		listenTo(assessmentModeListCtrl);
		if(assessmentModeListCtrl.getNumOfAssessmentModes() > 0) {
			mainVC.put("assessmentModes", assessmentModeListCtrl.getInitialComponent());
		}

		putInitialPanel(mainVC);
	}
	
	public void reloadAssessmentModes() {
		assessmentModeListCtrl.loadModel();
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(toReviewCtrl == source) {
			if(event instanceof CourseNodeIdentityEvent) {
				fireEvent(ureq, event);
			}
		} else if(toReleaseCtrl == source) {
			if(event instanceof CourseNodeIdentityEvent) {
				fireEvent(ureq, event);
			}
		
		} else if(assessmentModeListCtrl == source) {
			if(event instanceof CourseNodeEvent || event instanceof AssessmentModeStatusEvent) {
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
		} else if(assessableCoureNodesLink == source) {
			fireEvent(ureq, SELECT_NODES_EVENT);
		}
	}
}