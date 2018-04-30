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
package org.olat.ims.qti21.ui;

import java.io.File;
import java.math.BigDecimal;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.EscapeMode;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.BooleanCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SelectionEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.StaticFlexiCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.TextFlexiCellRenderer;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.stack.TooledStackedPanel;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.gui.control.generic.modal.DialogBoxController;
import org.olat.core.gui.control.generic.modal.DialogBoxUIFactory;
import org.olat.core.gui.media.FileMediaResource;
import org.olat.core.id.Identity;
import org.olat.core.util.StringHelper;
import org.olat.course.CourseFactory;
import org.olat.course.nodes.IQTESTCourseNode;
import org.olat.course.nodes.iq.IQEditController;
import org.olat.course.nodes.iq.QTI21AssessmentRunController;
import org.olat.course.run.environment.CourseEnvironment;
import org.olat.course.run.scoring.ScoreEvaluation;
import org.olat.course.run.userview.UserCourseEnvironment;
import org.olat.fileresource.FileResourceManager;
import org.olat.ims.qti21.AssessmentTestSession;
import org.olat.ims.qti21.QTI21AssessmentResultsOptions;
import org.olat.ims.qti21.QTI21DeliveryOptions;
import org.olat.ims.qti21.QTI21Service;
import org.olat.ims.qti21.model.DigitalSignatureOptions;
import org.olat.ims.qti21.model.jpa.AssessmentTestSessionStatistics;
import org.olat.ims.qti21.model.xml.ManifestBuilder;
import org.olat.ims.qti21.model.xml.QtiNodesExtractor;
import org.olat.ims.qti21.ui.QTI21AssessmentTestSessionTableModel.TSCols;
import org.olat.ims.qti21.ui.assessment.CorrectionIdentityAssessmentItemListController;
import org.olat.ims.qti21.ui.assessment.CorrectionOverviewModel;
import org.olat.modules.ModuleConfiguration;
import org.olat.modules.assessment.AssessmentEntry;
import org.olat.modules.assessment.AssessmentService;
import org.olat.modules.assessment.AssessmentToolOptions;
import org.olat.modules.assessment.Role;
import org.olat.modules.assessment.ui.event.CompleteAssessmentTestSessionEvent;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryManager;
import org.olat.repository.model.RepositoryEntrySecurity;
import org.olat.user.UserManager;
import org.springframework.beans.factory.annotation.Autowired;

import uk.ac.ed.ph.jqtiplus.node.test.AssessmentTest;
import uk.ac.ed.ph.jqtiplus.resolution.ResolvedAssessmentTest;
import uk.ac.ed.ph.jqtiplus.state.ItemSessionState;
import uk.ac.ed.ph.jqtiplus.state.TestPlan;
import uk.ac.ed.ph.jqtiplus.state.TestPlanNode;
import uk.ac.ed.ph.jqtiplus.state.TestPlanNode.TestNodeType;
import uk.ac.ed.ph.jqtiplus.state.TestPlanNodeKey;
import uk.ac.ed.ph.jqtiplus.state.TestSessionState;

/**
 * This controller is used by the assessment tools of the course and
 * of the test resource. The assessment tool of the resource doesn't
 * provide any user course environment or course node. Be aware!
 * 
 * 
 * Initial date: 28.06.2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class QTI21AssessmentDetailsController extends FormBasicController {

	private FormLink resetButton;
	private FlexiTableElement tableEl;
	private final TooledStackedPanel stackPanel;
	private QTI21AssessmentTestSessionTableModel tableModel;
	
	private RepositoryEntry entry;
	private RepositoryEntry testEntry;
	private final String subIdent;
	private final boolean manualCorrections;
	private final Identity assessedIdentity;
	
	private final boolean readOnly;
	private final IQTESTCourseNode courseNode;
	private final RepositoryEntrySecurity reSecurity;
	private final UserCourseEnvironment assessedUserCourseEnv;
	
	private CloseableModalController cmc;
	private AssessmentResultController resultCtrl;
	private QTI21ResetDataController resetToolCtrl;
	private DialogBoxController retrieveConfirmationCtr;
	private CorrectionIdentityAssessmentItemListController correctionCtrl;
	
	@Autowired
	private UserManager userManager;
	@Autowired
	protected QTI21Service qtiService;
	@Autowired
	private RepositoryManager repositoryManager;
	@Autowired
	private AssessmentService assessmentService;
	
	/**
	 * The constructor used by the assessment tool of the course.
	 * 
	 * @param ureq
	 * @param wControl
	 * @param assessableEntry
	 * @param courseNode
	 * @param coachCourseEnv
	 * @param assessedUserCourseEnv
	 */
	public QTI21AssessmentDetailsController(UserRequest ureq, WindowControl wControl, TooledStackedPanel stackPanel,
			RepositoryEntry assessableEntry, IQTESTCourseNode courseNode,
			UserCourseEnvironment coachCourseEnv, UserCourseEnvironment assessedUserCourseEnv) {
		super(ureq, wControl, "assessment_details");
		entry = assessableEntry;
		this.stackPanel = stackPanel;
		this.courseNode = courseNode;
		subIdent = courseNode.getIdent();
		readOnly = coachCourseEnv.isCourseReadOnly();
		this.assessedUserCourseEnv = assessedUserCourseEnv;
		testEntry = courseNode.getReferencedRepositoryEntry();
		assessedIdentity = assessedUserCourseEnv.getIdentityEnvironment().getIdentity();
		manualCorrections = qtiService.needManualCorrection(testEntry)
				|| IQEditController.CORRECTION_MANUAL.equals(courseNode.getModuleConfiguration().getStringValue(IQEditController.CONFIG_CORRECTION_MODE));
		
		RepositoryEntry courseEntry = assessedUserCourseEnv.getCourseEnvironment().getCourseGroupManager().getCourseEntry();
		reSecurity = repositoryManager.isAllowed(ureq, courseEntry);

		initForm(ureq);
		updateModel();
	}
	
	/**
	 * The constructor used by the assessment tool of the test resource itself.
	 * 
	 * @param ureq
	 * @param wControl
	 * @param assessableEntry
	 * @param assessedIdentity
	 */
	public QTI21AssessmentDetailsController(UserRequest ureq, WindowControl wControl, TooledStackedPanel stackPanel,
			RepositoryEntry assessableEntry, Identity assessedIdentity) {
		super(ureq, wControl, "assessment_details");
		entry = assessableEntry;
		testEntry = assessableEntry;
		subIdent = null;
		readOnly = false;
		courseNode = null;
		assessedUserCourseEnv = null;
		this.stackPanel = stackPanel;
		this.assessedIdentity = assessedIdentity;
		manualCorrections = qtiService.needManualCorrection(assessableEntry);
		reSecurity = repositoryManager.isAllowed(ureq, assessableEntry);

		initForm(ureq);
		updateModel();
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(TSCols.terminationTime));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(TSCols.lastModified));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(TSCols.duration, new TextFlexiCellRenderer(EscapeMode.none)));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(TSCols.numOfItemSessions, new TextFlexiCellRenderer(EscapeMode.none)));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(TSCols.responded, new TextFlexiCellRenderer(EscapeMode.none)));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(TSCols.corrected, new TextFlexiCellRenderer(EscapeMode.none)));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(TSCols.score, new TextFlexiCellRenderer(EscapeMode.none)));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(TSCols.manualScore, new TextFlexiCellRenderer(EscapeMode.none)));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(TSCols.finalScore, new TextFlexiCellRenderer(EscapeMode.none)));
		
		if(readOnly) {
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel("select", translate("select"), "open"));
		} else {
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(TSCols.open.i18nHeaderKey(), TSCols.open.ordinal(), "open",
					new BooleanCellRenderer(new StaticFlexiCellRenderer(translate("results.report"), "open"),
							new StaticFlexiCellRenderer(translate("pull"), "open"))));
		}
		if(manualCorrections && !readOnly) {
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(TSCols.correction.i18nHeaderKey(), TSCols.correction.ordinal(), "correction",
					new BooleanCellRenderer(new StaticFlexiCellRenderer(translate("correction"), "correction"), null)));
		}
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel("download.log", translate("download.log"), "log"));

		tableModel = new QTI21AssessmentTestSessionTableModel(columnsModel, getTranslator());
		tableEl = uifactory.addTableElement(getWindowControl(), "sessions", tableModel, 20, false, getTranslator(), formLayout);
		tableEl.setEmtpyTableMessageKey("results.empty");

		if(reSecurity.isEntryAdmin() && !readOnly) {
			resetButton = uifactory.addFormLink("menu.reset.title", formLayout, Link.BUTTON);
			resetButton.setIconLeftCSS("o_icon o_icon_delete_item"); 	
		}
	} 

	@Override
	protected void doDispose() {
		//
	}
	
	protected void updateModel() {
		List<AssessmentTestSessionStatistics> sessionsStatistics = qtiService.getAssessmentTestSessionsStatistics(entry, subIdent, assessedIdentity);
		List<QTI21AssessmentTestSessionDetails> infos = new ArrayList<>();
		for(AssessmentTestSessionStatistics sessionStatistics:sessionsStatistics) {
			AssessmentTestSession testSession = sessionStatistics.getTestSession();
			TestSessionState testSessionState = qtiService.loadTestSessionState(testSession);
			TestPlan testPlan = testSessionState.getTestPlan();
			List<TestPlanNode> nodes = testPlan.getTestPlanNodeList();
			
			int responded = 0;
			int numOfItems = 0;
			for(TestPlanNode node:nodes) {
				TestNodeType testNodeType = node.getTestNodeType();
				ItemSessionState itemSessionState = testSessionState.getItemSessionStates().get(node.getKey());

				TestPlanNodeKey testPlanNodeKey = node.getKey();
				if(testPlanNodeKey != null && testPlanNodeKey.getIdentifier() != null
						&& testNodeType == TestNodeType.ASSESSMENT_ITEM_REF) {
					numOfItems++;
					if(itemSessionState.isResponded()) {
						responded++;
					}
				}
			}

			infos.add(new QTI21AssessmentTestSessionDetails(testSession,
					numOfItems, responded, sessionStatistics.getNumOfCorrectedItems()));
		}
		
		
		Collections.sort(infos, new AssessmentTestSessionDetailsComparator());
		tableModel.setObjects(infos);
		tableEl.reloadData();
		tableEl.reset();
		
		if(resetButton != null) {
			resetButton.setVisible(!sessionsStatistics.isEmpty());
		}
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(cmc == source) {
			cleanUp();
		} else if(correctionCtrl == source) {
			if(event instanceof CompleteAssessmentTestSessionEvent) {
				CompleteAssessmentTestSessionEvent catse = (CompleteAssessmentTestSessionEvent)event;
				if(courseNode != null) {
					doUpdateCourseNode(correctionCtrl.getAssessmentTestSession(), catse.getAssessmentTest());
				} else {
					doUpdateEntry(correctionCtrl.getAssessmentTestSession());
				}
				updateModel();
				fireEvent(ureq, Event.CHANGED_EVENT);
				stackPanel.popController(correctionCtrl);
				cleanUp();
			} else if(event == Event.CHANGED_EVENT) {
				updateModel();
				fireEvent(ureq, Event.CHANGED_EVENT);
			} else if(event == Event.CANCELLED_EVENT) {
				stackPanel.popController(correctionCtrl);
				cleanUp();
			}
		} else if(retrieveConfirmationCtr == source) {
			if(DialogBoxUIFactory.isYesEvent(event) || DialogBoxUIFactory.isOkEvent(event)) {
				doPullSession(ureq, (AssessmentTestSession)retrieveConfirmationCtr.getUserObject());
				updateModel();
			}
		} else if(resetToolCtrl == source) {
			if(event == Event.DONE_EVENT || event == Event.CHANGED_EVENT) {
				updateModel();
				fireEvent(ureq, Event.CHANGED_EVENT);
			}
			cmc.deactivate();
			cleanUp();
		}
		super.event(ureq, source, event);
	}
	
	private void cleanUp() {
		removeAsListenerAndDispose(correctionCtrl);
		removeAsListenerAndDispose(resetToolCtrl);
		removeAsListenerAndDispose(resultCtrl);
		removeAsListenerAndDispose(cmc);
		correctionCtrl = null;
		resetToolCtrl = null;
		resultCtrl = null;
		cmc = null;
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(resetButton == source) {
			doResetData(ureq);
		} else if(tableEl == source) {
			if(event instanceof SelectionEvent) {
				SelectionEvent se = (SelectionEvent)event;
				String cmd = se.getCommand();
				QTI21AssessmentTestSessionDetails row = tableModel.getObject(se.getIndex());
				AssessmentTestSession testSession = qtiService.getAssessmentTestSession(row.getTestSession().getKey());
				if("open".equals(cmd)) {
					if(testSession.getFinishTime() == null) {
						doConfirmPullSession(ureq, testSession);
					} else {
						doOpenResult(ureq, testSession);
					}
				} else if("correction".equals(cmd)) {
					doCorrection(ureq, testSession);
				} else if("log".equals(cmd)) {
					doDownloadLog(ureq, testSession);
				}
			}
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}

	private void doCorrection(UserRequest ureq, AssessmentTestSession session) {
		File unzippedDirRoot = FileResourceManager.getInstance().unzipFileResource(testEntry.getOlatResource());
		ResolvedAssessmentTest resolvedAssessmentTest = qtiService.loadAndResolveAssessmentTest(unzippedDirRoot, false, false);
		ManifestBuilder manifestBuilder = ManifestBuilder.read(new File(unzippedDirRoot, "imsmanifest.xml"));
		TestSessionState testSessionState = qtiService.loadTestSessionState(session);
		// use mutable maps to allow updates
		Map<Identity,AssessmentTestSession> lastSessions = new HashMap<>();
		lastSessions.put(assessedIdentity, session);
		Map<Identity, TestSessionState> testSessionStates = new HashMap<>();
		testSessionStates.put(assessedIdentity, testSessionState);
		CorrectionOverviewModel model = new CorrectionOverviewModel(entry, courseNode, testEntry,
				resolvedAssessmentTest, manifestBuilder, lastSessions, testSessionStates);
		correctionCtrl = new CorrectionIdentityAssessmentItemListController(ureq, getWindowControl(), stackPanel, model, assessedIdentity);
		listenTo(correctionCtrl);
		stackPanel.pushController(translate("correction"), correctionCtrl);
	}
	
	private void doUpdateCourseNode(AssessmentTestSession session, AssessmentTest assessmentTest) {
		Double cutValue = QtiNodesExtractor.extractCutValue(assessmentTest);
		
		ScoreEvaluation scoreEval = courseNode.getUserScoreEvaluation(assessedUserCourseEnv);
		BigDecimal finalScore = session.getFinalScore();
		Float score = finalScore == null ? null : finalScore.floatValue();
		Boolean passed = scoreEval.getPassed();
		if(session.getManualScore() != null && finalScore != null && cutValue != null) {
			boolean calculated = finalScore.compareTo(BigDecimal.valueOf(cutValue.doubleValue())) >= 0;
			passed = Boolean.valueOf(calculated);
		}
		ScoreEvaluation manualScoreEval = new ScoreEvaluation(score, passed,
				scoreEval.getAssessmentStatus(), null, scoreEval.getFullyAssessed(), 
				scoreEval.getCurrentRunCompletion(), scoreEval.getCurrentRunStatus(), session.getKey());
		courseNode.updateUserScoreEvaluation(manualScoreEval, assessedUserCourseEnv, getIdentity(), false, Role.coach);
	}
	
	private void doUpdateEntry(AssessmentTestSession session) {
		AssessmentEntry assessmentEntry = assessmentService.loadAssessmentEntry(assessedIdentity, entry, null, entry);
		assessmentEntry.setScore(session.getFinalScore());
		assessmentEntry.setAssessmentId(session.getKey());
		assessmentService.updateAssessmentEntry(assessmentEntry);
	}
	
	private void doResetData(UserRequest ureq) {
		AssessmentToolOptions asOptions = new AssessmentToolOptions();
		asOptions.setAdmin(reSecurity.isEntryAdmin());
		asOptions.setIdentities(Collections.singletonList(assessedIdentity));
		
		if(courseNode != null) {
			resetToolCtrl = new QTI21ResetDataController(ureq, getWindowControl(),
					assessedUserCourseEnv.getCourseEnvironment(), asOptions, courseNode);
		} else {
			resetToolCtrl = new QTI21ResetDataController(ureq, getWindowControl(), entry, asOptions);
		}
		listenTo(resetToolCtrl);

		cmc = new CloseableModalController(getWindowControl(), "close", resetToolCtrl.getInitialComponent(),
				true, translate("table.header.results"));
		cmc.activate();
		listenTo(cmc);
	}

	private void doConfirmPullSession(UserRequest ureq, AssessmentTestSession session) {
		String title = translate("pull");
		String fullname = userManager.getUserDisplayName(session.getIdentity());
		String text = translate("retrievetest.confirm.text", new String[]{ fullname });
		retrieveConfirmationCtr = activateOkCancelDialog(ureq, title, text, retrieveConfirmationCtr);
		retrieveConfirmationCtr.setUserObject(session);
	}
	
	private void doPullSession(UserRequest ureq, AssessmentTestSession session) {
		//reload it to prevent lazy loading issues
		session = qtiService.getAssessmentTestSession(session.getKey());
		qtiService.pullSession(session, getSignatureOptions(session), getIdentity());
		if(courseNode != null) {
			courseNode.pullAssessmentTestSession(session, assessedUserCourseEnv, getIdentity(), Role.coach);
		}
		updateModel();
		fireEvent(ureq, Event.CHANGED_EVENT);
	}
	
	private DigitalSignatureOptions getSignatureOptions(AssessmentTestSession session) {
		RepositoryEntry sessionTestEntry = session.getTestEntry();
		QTI21DeliveryOptions deliveryOptions = qtiService.getDeliveryOptions(sessionTestEntry);
		
		boolean digitalSignature = deliveryOptions.isDigitalSignature();
		boolean sendMail = deliveryOptions.isDigitalSignatureMail();
		if(courseNode != null) {
			ModuleConfiguration config = courseNode.getModuleConfiguration();
			digitalSignature = config.getBooleanSafe(IQEditController.CONFIG_DIGITAL_SIGNATURE,
					deliveryOptions.isDigitalSignature());
			sendMail = config.getBooleanSafe(IQEditController.CONFIG_DIGITAL_SIGNATURE_SEND_MAIL,
					deliveryOptions.isDigitalSignatureMail());
		}
		
		DigitalSignatureOptions options = new DigitalSignatureOptions(digitalSignature, sendMail, entry, testEntry);
		if(digitalSignature) {
			if(courseNode == null) {
				 AssessmentEntryOutcomesListener.decorateResourceConfirmation(entry, testEntry, session, options, null, getLocale());
			} else {
				CourseEnvironment courseEnv = CourseFactory.loadCourse(entry).getCourseEnvironment();
				QTI21AssessmentRunController.decorateCourseConfirmation(session, options, courseEnv, courseNode, sessionTestEntry, null, getLocale());
			}
		}
		return options;
	}

	private void doOpenResult(UserRequest ureq, AssessmentTestSession session) {
		if(resultCtrl != null) return;
		
		//reload it to prevent lazy loading issues
		session = qtiService.getAssessmentTestSession(session.getKey());

		FileResourceManager frm = FileResourceManager.getInstance();
		File fUnzippedDirRoot = frm.unzipFileResource(session.getTestEntry().getOlatResource());
		URI assessmentObjectUri = qtiService.createAssessmentTestUri(fUnzippedDirRoot);
		File submissionDir = qtiService.getSubmissionDirectory(session);
		String mapperUri = registerCacheableMapper(ureq, "QTI21DetailsResources::" + session.getKey(),
				new ResourcesMapper(assessmentObjectUri, submissionDir));
		
		resultCtrl = new AssessmentResultController(ureq, getWindowControl(), assessedIdentity, false, session,
				fUnzippedDirRoot, mapperUri, null, QTI21AssessmentResultsOptions.allOptions(), true, true, true);
		listenTo(resultCtrl);
		cmc = new CloseableModalController(getWindowControl(), "close", resultCtrl.getInitialComponent(),
				true, translate("results.report"));
		cmc.activate();
		listenTo(cmc);
	}
	
	private void doDownloadLog(UserRequest ureq, AssessmentTestSession session) {
		File logFile = qtiService.getAssessmentSessionAuditLogFile(session);
		if(logFile != null && logFile.exists()) {
			String filename = "auditlog_";
			if(session.getAnonymousIdentifier() != null) {
				filename += session.getAnonymousIdentifier();
			} else {
				filename += session.getIdentity().getUser().getFirstName()
						+ "_" + session.getIdentity().getUser().getLastName();
			}
			filename += "_" + entry.getDisplayname();
			if(courseNode != null) {
				if(StringHelper.containsNonWhitespace(courseNode.getShortTitle())) {
					filename += "_" + courseNode.getShortTitle();
				} else {
					filename += "_" + courseNode.getLongTitle();
				}
			}
			
			filename += ".log";
			ureq.getDispatchResult().setResultingMediaResource(new LogDownload(logFile, filename));
		} else {
			showWarning("warning.download.log");
		}
	}
	
	private static class LogDownload extends FileMediaResource {
		
		private final String filename;
		
		public LogDownload(File file, String filename) {
			super(file, true);
			this.filename = filename;
		}

		@Override
		public String getContentType() {
			return "application/octet-stream";
		}

		@Override
		public void prepare(HttpServletResponse hres) {
			hres.setHeader("Content-Disposition", "attachment; filename*=UTF-8''" + StringHelper.urlEncodeUTF8(filename));
			hres.setHeader("Content-Description", StringHelper.urlEncodeUTF8(filename));
		}
	}
	
	public static class AssessmentTestSessionDetailsComparator implements Comparator<QTI21AssessmentTestSessionDetails> {
		
		private final AssessmentTestSessionComparator comparator = new AssessmentTestSessionComparator();

		@Override
		public int compare(QTI21AssessmentTestSessionDetails q1, QTI21AssessmentTestSessionDetails q2) {
			return comparator.compare(q1.getTestSession(), q2.getTestSession());
		}
	}
}