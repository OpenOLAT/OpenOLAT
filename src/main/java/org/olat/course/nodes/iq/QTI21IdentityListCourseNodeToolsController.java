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
package org.olat.course.nodes.iq;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.gui.control.generic.modal.DialogBoxController;
import org.olat.core.gui.control.generic.modal.DialogBoxUIFactory;
import org.olat.core.id.Identity;
import org.olat.core.util.Util;
import org.olat.course.CourseFactory;
import org.olat.course.assessment.ui.tool.tools.AbstractToolsController;
import org.olat.course.nodes.IQTESTCourseNode;
import org.olat.course.run.environment.CourseEnvironment;
import org.olat.course.run.userview.UserCourseEnvironment;
import org.olat.ims.qti21.AssessmentTestSession;
import org.olat.ims.qti21.QTI21DeliveryOptions;
import org.olat.ims.qti21.QTI21Service;
import org.olat.ims.qti21.model.DigitalSignatureOptions;
import org.olat.ims.qti21.model.jpa.AssessmentTestSessionStatistics;
import org.olat.ims.qti21.ui.AssessmentTestDisplayController;
import org.olat.ims.qti21.ui.AssessmentTestSessionComparator;
import org.olat.ims.qti21.ui.assessment.IdentityAssessmentTestCorrectionController;
import org.olat.modules.ModuleConfiguration;
import org.olat.repository.RepositoryEntry;
import org.olat.user.UserManager;
import org.springframework.beans.factory.annotation.Autowired;


/**
 * 
 * Initial date: 23 nov. 2017<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class QTI21IdentityListCourseNodeToolsController extends AbstractToolsController {

	private Link correctionLink, extraTimeLink, pullTestLink, reopenLink, deleteDataLink;
	
	private CloseableModalController cmc;
	private ConfirmReopenController reopenCtrl;
	private ConfirmResetController confirmResetCtrl;
	private ConfirmExtraTimeController extraTimeCtrl;
	private DialogBoxController retrieveConfirmationCtr;
	private IdentityAssessmentTestCorrectionController correctionCtrl;
	
	private RepositoryEntry testEntry;
	private RepositoryEntry courseEntry;
	private IQTESTCourseNode testCourseNode;
	
	private final boolean manualCorrections;
	private AssessmentTestSession lastSession;
	
	@Autowired
	private QTI21Service qtiService;
	@Autowired
	private UserManager userManager;
	
	public QTI21IdentityListCourseNodeToolsController(UserRequest ureq, WindowControl wControl,
			IQTESTCourseNode courseNode, Identity assessedIdentity, UserCourseEnvironment coachCourseEnv) {
		super(ureq, wControl, courseNode, assessedIdentity, coachCourseEnv);

		setTranslator(Util.createPackageTranslator(AssessmentTestDisplayController.class, getLocale(), getTranslator()));
		
		this.testCourseNode = courseNode;
		testEntry = courseNode.getReferencedRepositoryEntry();
		manualCorrections = qtiService.needManualCorrection(testEntry)
				|| IQEditController.CORRECTION_MANUAL.equals(courseNode.getModuleConfiguration().getStringValue(IQEditController.CONFIG_CORRECTION_MODE));
		
		courseEntry = coachCourseEnv.getCourseEnvironment().getCourseGroupManager().getCourseEntry();
		List<AssessmentTestSessionStatistics> sessionsStatistics = qtiService
				.getAssessmentTestSessionsStatistics(courseEntry, courseNode.getIdent(), assessedIdentity);
		if(sessionsStatistics.size() > 0) {
			Collections.sort(sessionsStatistics, new AssessmentTestSessionDetailsComparator());
			lastSession = sessionsStatistics.get(0).getTestSession();
		}

		initTools();	
	}

	@Override
	protected void initDetails() {
		super.initDetails();
		
		//correction 
		if(manualCorrections && !isCourseReadonly() && lastSession != null) {
			correctionLink = addLink("tool.correction", "tool.correction", "o_icon o_icon-fw o_icon_correction");
		}
	}

	@Override
	protected void initStatus() {
		super.initStatus();

		addSeparator();
		
		if(lastSession != null && lastSession.getFinishTime() == null) {
			// test: extra time
			if(testCourseNode.hasQTI21TimeLimit(testEntry)) {
				extraTimeLink = addLink("tool.extra.time", "tool.extra.time", "o_icon o_icon-fw o_icon_extra_time");
			}
			// test: retrieve
			pullTestLink = addLink("tool.pull", "tool.pull", "o_icon o_icon-fw o_icon_pull");
		}
	}

	@Override
	protected void initResetAttempts() {
		//closed test reopen
		if(lastSession != null && (lastSession.getFinishTime() != null || lastSession.getTerminationTime() != null)) {
			reopenLink = addLink("reopen.test", "reopen.test", "o_icon o_icon-fw o_icon_reopen");
		}

		super.initResetAttempts();
		if(lastSession != null) {
			addSeparator();
			//delete data
			deleteDataLink = addLink("reset.test.data.title", "tool.delete.data", "o_icon o_icon-fw o_icon_delete_item");
		}
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if(correctionLink == source) {
			fireEvent(ureq, Event.CLOSE_EVENT);
			doOpenCorrection(ureq);
		} else if(pullTestLink == source) {
			fireEvent(ureq, Event.CLOSE_EVENT);
			doConfirmPullSession(ureq, lastSession);
		} else if(deleteDataLink == source) {
			fireEvent(ureq, Event.CLOSE_EVENT);
			doConfirmDeleteData(ureq);
		} else if(extraTimeLink == source) {
			fireEvent(ureq, Event.CLOSE_EVENT);
			doConfirmExtraTime(ureq);
		} else if(reopenLink == source) {
			fireEvent(ureq, Event.CLOSE_EVENT);
			doConfirmReopenTest(ureq);
		}
		super.event(ureq, source, event);
	}
	
	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(retrieveConfirmationCtr == source) {
			if(DialogBoxUIFactory.isYesEvent(event) || DialogBoxUIFactory.isOkEvent(event)) {
				doPullSession(lastSession);
				fireEvent(ureq, Event.CHANGED_EVENT);
			} else {
				fireEvent(ureq, Event.DONE_EVENT);
			}
		} else if(correctionCtrl == source) {
			if(event == Event.DONE_EVENT || event == Event.CANCELLED_EVENT) {
				cmc.deactivate();
				cleanUp();
				fireEvent(ureq, Event.CHANGED_EVENT);
			}
		} else if(confirmResetCtrl == source || extraTimeCtrl == source || reopenCtrl == source) {
			cmc.deactivate();
			cleanUp();
			fireAlteredEvent(ureq, event);
		} else if(cmc == source) {
			cleanUp();
			fireEvent(ureq, Event.CHANGED_EVENT);
		}
		super.event(ureq, source, event);
	}
	
	private void fireAlteredEvent(UserRequest ureq, Event event) {
		if(event == Event.DONE_EVENT) {
			fireEvent(ureq, Event.CHANGED_EVENT);
		} else if(event == Event.CANCELLED_EVENT) {
			fireEvent(ureq, event);
		} else {
			fireEvent(ureq, Event.DONE_EVENT);
		}
	}
	
	private void cleanUp() {
		removeAsListenerAndDispose(confirmResetCtrl);
		removeAsListenerAndDispose(correctionCtrl);
		removeAsListenerAndDispose(extraTimeCtrl);
		removeAsListenerAndDispose(cmc);
		confirmResetCtrl = null;
		correctionCtrl = null;
		extraTimeCtrl = null;
		cmc = null;
	}
	
	private void doOpenCorrection(UserRequest ureq) {
		correctionCtrl = new IdentityAssessmentTestCorrectionController(ureq, getWindowControl(), lastSession);
		listenTo(correctionCtrl);
		
		cmc = new CloseableModalController(getWindowControl(), "close", correctionCtrl.getInitialComponent(),
				true, translate("tool.correction"));
		cmc.activate();
		listenTo(cmc);
	}
	
	private void doConfirmPullSession(UserRequest ureq, AssessmentTestSession session) {
		String title = translate("tool.pull");
		String fullname = userManager.getUserDisplayName(session.getIdentity());
		String text = translate("retrievetest.confirm.text", new String[]{ fullname });
		retrieveConfirmationCtr = activateOkCancelDialog(ureq, title, text, retrieveConfirmationCtr);
		retrieveConfirmationCtr.setUserObject(session);
	}
	
	private void doPullSession(AssessmentTestSession session) {
		qtiService.pullSession(session, getSignatureOptions(session), getIdentity());
	}
	
	private DigitalSignatureOptions getSignatureOptions(AssessmentTestSession session) {
		RepositoryEntry sessionTestEntry = session.getTestEntry();
		QTI21DeliveryOptions deliveryOptions = qtiService.getDeliveryOptions(sessionTestEntry);
		
		boolean digitalSignature = deliveryOptions.isDigitalSignature();
		boolean sendMail = deliveryOptions.isDigitalSignatureMail();

		ModuleConfiguration config = courseNode.getModuleConfiguration();
		digitalSignature = config.getBooleanSafe(IQEditController.CONFIG_DIGITAL_SIGNATURE,
			deliveryOptions.isDigitalSignature());
		sendMail = config.getBooleanSafe(IQEditController.CONFIG_DIGITAL_SIGNATURE_SEND_MAIL,
			deliveryOptions.isDigitalSignatureMail());

		DigitalSignatureOptions options = new DigitalSignatureOptions(digitalSignature, sendMail, courseEntry, testEntry);
		if(digitalSignature) {
			CourseEnvironment courseEnv = CourseFactory.loadCourse(courseEntry).getCourseEnvironment();
			QTI21AssessmentRunController.decorateCourseConfirmation(session, options, courseEnv, courseNode, sessionTestEntry, null, getLocale());
		}
		return options;
	}
	
	private void doConfirmDeleteData(UserRequest ureq) {
		confirmResetCtrl = new ConfirmResetController(ureq, getWindowControl(), courseEntry, courseNode, testEntry, assessedIdentity);
		listenTo(confirmResetCtrl);

		String title = translate("reset.test.data.title");
		cmc = new CloseableModalController(getWindowControl(), null, confirmResetCtrl.getInitialComponent(), true, title, true);
		listenTo(cmc);
		cmc.activate();
	}
	
	private void doConfirmExtraTime(UserRequest ureq) {
		List<AssessmentTestSession> testSessions = Collections.singletonList(lastSession);
		extraTimeCtrl = new ConfirmExtraTimeController(ureq, getWindowControl(), testSessions);
		listenTo(extraTimeCtrl);

		String title = translate("extra.time");
		cmc = new CloseableModalController(getWindowControl(), null, extraTimeCtrl.getInitialComponent(), true, title, true);
		listenTo(cmc);
		cmc.activate();
	}
	
	private void doConfirmReopenTest(UserRequest ureq) {
		reopenCtrl = new ConfirmReopenController(ureq, getWindowControl(),
				assessedUserCourseEnv.getCourseEnvironment(), testCourseNode, lastSession);
		listenTo(reopenCtrl);

		String title = translate("reopen.test");
		cmc = new CloseableModalController(getWindowControl(), null, reopenCtrl.getInitialComponent(), true, title, true);
		listenTo(cmc);
		cmc.activate();
	}
	
	public static class AssessmentTestSessionDetailsComparator implements Comparator<AssessmentTestSessionStatistics> {
		
		private final AssessmentTestSessionComparator comparator = new AssessmentTestSessionComparator();

		@Override
		public int compare(AssessmentTestSessionStatistics q1, AssessmentTestSessionStatistics q2) {
			return comparator.compare(q1.getTestSession(), q2.getTestSession());
		}
	}
}