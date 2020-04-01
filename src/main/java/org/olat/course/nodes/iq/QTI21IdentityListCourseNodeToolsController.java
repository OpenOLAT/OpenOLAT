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

import java.io.File;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.stack.TooledStackedPanel;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.id.Identity;
import org.olat.core.util.Util;
import org.olat.course.assessment.CourseAssessmentService;
import org.olat.course.assessment.ui.tool.tools.AbstractToolsController;
import org.olat.course.nodes.IQTESTCourseNode;
import org.olat.course.run.userview.UserCourseEnvironment;
import org.olat.fileresource.FileResourceManager;
import org.olat.ims.qti21.AssessmentTestSession;
import org.olat.ims.qti21.QTI21Service;
import org.olat.ims.qti21.model.jpa.AssessmentTestSessionStatistics;
import org.olat.ims.qti21.model.xml.ManifestBuilder;
import org.olat.ims.qti21.ui.AssessmentTestDisplayController;
import org.olat.ims.qti21.ui.AssessmentTestSessionComparator;
import org.olat.ims.qti21.ui.QTI21ResetDataController;
import org.olat.ims.qti21.ui.QTI21RetrieveTestsController;
import org.olat.ims.qti21.ui.assessment.CorrectionIdentityAssessmentItemListController;
import org.olat.ims.qti21.ui.assessment.CorrectionOverviewModel;
import org.olat.modules.assessment.AssessmentEntry;
import org.olat.modules.assessment.model.AssessmentEntryStatus;
import org.olat.modules.assessment.ui.event.CompleteAssessmentTestSessionEvent;
import org.olat.repository.RepositoryEntry;
import org.springframework.beans.factory.annotation.Autowired;

import uk.ac.ed.ph.jqtiplus.resolution.ResolvedAssessmentTest;
import uk.ac.ed.ph.jqtiplus.state.TestSessionState;


/**
 * 
 * Initial date: 23 nov. 2017<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class QTI21IdentityListCourseNodeToolsController extends AbstractToolsController {

	private Link correctionLink;
	private Link extraTimeLink;
	private Link pullTestLink;
	private Link reopenLink;
	private Link deleteDataLink;
	private TooledStackedPanel stackPanel;
	
	private CloseableModalController cmc;
	private ConfirmReopenController reopenCtrl;
	private QTI21ResetDataController resetDataCtrl;
	private ConfirmExtraTimeController extraTimeCtrl;
	private QTI21RetrieveTestsController retrieveConfirmationCtr;
	private CorrectionIdentityAssessmentItemListController correctionCtrl;
	
	private RepositoryEntry testEntry;
	private RepositoryEntry courseEntry;
	private IQTESTCourseNode testCourseNode;
	
	private final boolean manualCorrections;
	private AssessmentTestSession lastSession;
	
	@Autowired
	private QTI21Service qtiService;
	@Autowired
	private CourseAssessmentService courseAssessmentService;
	
	public QTI21IdentityListCourseNodeToolsController(UserRequest ureq, WindowControl wControl, TooledStackedPanel stackPanel,
			IQTESTCourseNode courseNode, Identity assessedIdentity, UserCourseEnvironment coachCourseEnv) {
		super(ureq, wControl, courseNode, assessedIdentity, coachCourseEnv);

		setTranslator(Util.createPackageTranslator(AssessmentTestDisplayController.class, getLocale(), getTranslator()));
		
		this.stackPanel = stackPanel;
		this.testCourseNode = courseNode;
		testEntry = courseNode.getReferencedRepositoryEntry();
		
		String correctionMode = courseNode.getModuleConfiguration().getStringValue(IQEditController.CONFIG_CORRECTION_MODE);
		manualCorrections = !IQEditController.CORRECTION_GRADING.equals(correctionMode)
				&& (IQEditController.CORRECTION_MANUAL.equals(correctionMode) || qtiService.needManualCorrection(testEntry));
		
		courseEntry = coachCourseEnv.getCourseEnvironment().getCourseGroupManager().getCourseEntry();
		
		List<AssessmentTestSessionStatistics> sessionsStatistics = qtiService
				.getAssessmentTestSessionsStatistics(courseEntry, courseNode.getIdent(), assessedIdentity);
		if(!sessionsStatistics.isEmpty()) {
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
			if(event == Event.DONE_EVENT) {
				fireEvent(ureq, Event.CHANGED_EVENT);
			} else {
				fireEvent(ureq, Event.DONE_EVENT);
			}
			cmc.deactivate();
			cleanUp();
		} else if(correctionCtrl == source) {
			if(event instanceof CompleteAssessmentTestSessionEvent || event == Event.CANCELLED_EVENT || event == Event.BACK_EVENT) {
				stackPanel.popController(correctionCtrl);
				cleanUp();
				fireEvent(ureq, event);
			}
		} else if(resetDataCtrl == source || extraTimeCtrl == source || reopenCtrl == source) {
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
		removeAsListenerAndDispose(correctionCtrl);
		removeAsListenerAndDispose(extraTimeCtrl);
		removeAsListenerAndDispose(resetDataCtrl);
		removeAsListenerAndDispose(cmc);
		correctionCtrl = null;
		extraTimeCtrl = null;
		resetDataCtrl = null;
		cmc = null;
	}
	
	private void doOpenCorrection(UserRequest ureq) {
		File unzippedDirRoot = FileResourceManager.getInstance().unzipFileResource(testEntry.getOlatResource());
		ResolvedAssessmentTest resolvedAssessmentTest = qtiService.loadAndResolveAssessmentTest(unzippedDirRoot, false, false);
		ManifestBuilder manifestBuilder = ManifestBuilder.read(new File(unzippedDirRoot, "imsmanifest.xml"));
		TestSessionState testSessionState = qtiService.loadTestSessionState(lastSession);
		Map<Identity, AssessmentTestSession> lastSessionMap = new HashMap<>();
		lastSessionMap.put(assessedIdentity, lastSession);
		Map<Identity, TestSessionState> testSessionStates = new HashMap<>();
		testSessionStates.put(assessedIdentity, testSessionState);
		CorrectionOverviewModel model = new CorrectionOverviewModel(courseEntry, testCourseNode, testEntry,
				resolvedAssessmentTest, manifestBuilder, lastSessionMap, testSessionStates);
		boolean readOnly = isAssessementEntryDone();
		correctionCtrl = new CorrectionIdentityAssessmentItemListController(ureq, getWindowControl(), stackPanel, model, assessedIdentity, readOnly);
		listenTo(correctionCtrl);
		stackPanel.pushController(translate("tool.correction"), correctionCtrl);
	}
	
	private boolean isAssessementEntryDone() {
		AssessmentEntry entry = courseAssessmentService.getAssessmentEntry(testCourseNode, assessedUserCourseEnv);
		return entry != null && entry.getAssessmentStatus() == AssessmentEntryStatus.done;
	}
	
	private void doConfirmPullSession(UserRequest ureq, AssessmentTestSession session) {
		retrieveConfirmationCtr = new QTI21RetrieveTestsController(ureq, getWindowControl(), session, (IQTESTCourseNode)courseNode);
		listenTo(retrieveConfirmationCtr);
		
		String title = translate("tool.pull");
		cmc = new CloseableModalController(getWindowControl(), null, retrieveConfirmationCtr.getInitialComponent(), true, title, true);
		listenTo(cmc);
		cmc.activate();
	}
	
	private void doConfirmDeleteData(UserRequest ureq) {
		resetDataCtrl = new QTI21ResetDataController(ureq, getWindowControl(), courseEntry, (IQTESTCourseNode)courseNode, assessedIdentity);
		listenTo(resetDataCtrl);

		String title = translate("reset.test.data.title");
		cmc = new CloseableModalController(getWindowControl(), null, resetDataCtrl.getInitialComponent(), true, title, true);
		listenTo(cmc);
		cmc.activate();
	}
	
	private void doConfirmExtraTime(UserRequest ureq) {
		List<AssessmentTestSession> testSessions = Collections.singletonList(lastSession);
		extraTimeCtrl = new ConfirmExtraTimeController(ureq, getWindowControl(), courseEntry, testSessions);
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