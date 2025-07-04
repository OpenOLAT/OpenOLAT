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
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NavigableSet;

import jakarta.servlet.http.HttpServletResponse;

import org.olat.core.commons.services.pdf.PdfModule;
import org.olat.core.commons.services.pdf.PdfOutputOptions;
import org.olat.core.commons.services.pdf.PdfService;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.EscapeMode;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.ActionDelegateCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.ActionsColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.BooleanCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableComponent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SelectionEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.StaticFlexiCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.TextFlexiCellRenderer;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.stack.TooledStackedPanel;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.creator.ControllerCreator;
import org.olat.core.gui.control.generic.closablewrapper.CloseableCalloutWindowController;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.gui.control.generic.modal.DialogBoxController;
import org.olat.core.gui.control.generic.modal.DialogBoxUIFactory;
import org.olat.core.gui.control.winmgr.Command;
import org.olat.core.gui.control.winmgr.CommandFactory;
import org.olat.core.gui.media.FileMediaResource;
import org.olat.core.gui.media.MediaResource;
import org.olat.core.gui.render.Renderer;
import org.olat.core.gui.render.StringOutput;
import org.olat.core.gui.render.URLBuilder;
import org.olat.core.gui.translator.Translator;
import org.olat.core.id.Identity;
import org.olat.core.util.StringHelper;
import org.olat.course.CourseFactory;
import org.olat.course.assessment.CourseAssessmentService;
import org.olat.course.assessment.handler.AssessmentConfig;
import org.olat.course.assessment.ui.reset.ConfirmResetDataController;
import org.olat.course.assessment.ui.reset.ResetDataContext;
import org.olat.course.assessment.ui.reset.ResetDataContext.ResetCourse;
import org.olat.course.assessment.ui.reset.ResetDataContext.ResetParticipants;
import org.olat.course.nodes.IQTESTCourseNode;
import org.olat.course.nodes.STCourseNode;
import org.olat.course.nodes.iq.IQEditController;
import org.olat.course.nodes.iq.QTI21AssessmentRunController;
import org.olat.course.run.environment.CourseEnvironment;
import org.olat.course.run.scoring.AssessmentEvaluation;
import org.olat.course.run.scoring.ResetCourseDataHelper;
import org.olat.course.run.scoring.ScoreEvaluation;
import org.olat.course.run.scoring.ScoreScalingHelper;
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
import org.olat.ims.qti21.ui.QTI21AssessmentTestSessionDetails.SessionStatus;
import org.olat.ims.qti21.ui.QTI21AssessmentTestSessionTableModel.TSCols;
import org.olat.ims.qti21.ui.assessment.CorrectionIdentityAssessmentItemListController;
import org.olat.ims.qti21.ui.assessment.CorrectionOverviewModel;
import org.olat.ims.qti21.ui.components.AssessmentTestEntryRenderer;
import org.olat.ims.qti21.ui.components.AssessmentTestSessionDetailsNumberRenderer;
import org.olat.ims.qti21.ui.components.AssessmentTestSessionStatusRenderer;
import org.olat.ims.qti21.ui.logviewer.LogViewerController;
import org.olat.instantMessaging.InstantMessagingService;
import org.olat.modules.ModuleConfiguration;
import org.olat.modules.assessment.AssessmentToolOptions;
import org.olat.modules.assessment.Role;
import org.olat.modules.assessment.model.AssessmentEntryStatus;
import org.olat.modules.assessment.ui.ScoreCellRenderer;
import org.olat.modules.assessment.ui.event.CompleteAssessmentTestSessionEvent;
import org.olat.modules.grade.GradeModule;
import org.olat.modules.grade.GradeScale;
import org.olat.modules.grade.GradeScoreRange;
import org.olat.modules.grade.GradeService;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntrySecurity;
import org.olat.repository.RepositoryManager;
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
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
public class QTI21AssessmentDetailsController extends FormBasicController {

	private FormLink resetButton;
	private FormLink deleteButton;
	private FlexiTableElement tableEl;
	private final TooledStackedPanel stackPanel;
	private DefaultFlexiColumnModel correctionCol;
	private QTI21AssessmentTestSessionTableModel tableModel;
	
	private RepositoryEntry entry;
	private final String subIdent;
	private final Identity assessedIdentity;
	
	private final boolean readOnly;
	private final IQTESTCourseNode courseNode;
	private final RepositoryEntrySecurity reSecurity;
	private final UserCourseEnvironment coachCourseEnv;
	private final UserCourseEnvironment assessedUserCourseEnv;

	private ToolsController toolsCtrl;
	private CloseableModalController cmc;
	private LogViewerController logViewerCtrl;
	private AssessmentResultController resultCtrl;
	private QTI21DeleteDataController deleteToolCtrl;
	private ConfirmResetDataController confirmResetDataCtrl;
	private DialogBoxController retrieveConfirmationCtr;
	private CloseableCalloutWindowController calloutCtrl;
	private CorrectionIdentityAssessmentItemListController correctionCtrl;
	private ConfirmReopenAssessmentEntryController reopenForCorrectionCtrl;
	private ConfirmAssessmentTestSessionInvalidationController invalidateConfirmationCtr;
	private ConfirmAssessmentTestSessionRevalidationController revalidateConfirmationCtr;

	@Autowired
	private PdfModule pdfModule;
	@Autowired
	private PdfService pdfService;
	@Autowired
	private UserManager userManager;
	@Autowired
	protected QTI21Service qtiService;
	@Autowired
	private InstantMessagingService imService;
	@Autowired
	private RepositoryManager repositoryManager;
	@Autowired
	private CourseAssessmentService courseAssessmentService;
	@Autowired
	private GradeModule gradeModule;
	@Autowired
	private GradeService gradeService;
	
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
		this.coachCourseEnv = coachCourseEnv;
		this.assessedUserCourseEnv = assessedUserCourseEnv;
		subIdent = courseNode.getIdent();
		readOnly = coachCourseEnv.isCourseReadOnly();
		assessedIdentity = assessedUserCourseEnv.getIdentityEnvironment().getIdentity();
		reSecurity = repositoryManager.isAllowed(ureq, assessableEntry);

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
		subIdent = null;
		readOnly = false;
		courseNode = null;
		coachCourseEnv = null;
		assessedUserCourseEnv = null;
		this.stackPanel = stackPanel;
		this.assessedIdentity = assessedIdentity;
		reSecurity = repositoryManager.isAllowed(ureq, assessableEntry);

		initForm(ureq);
		updateModel();
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(TSCols.run));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, TSCols.id));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, TSCols.startTime));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(TSCols.terminationTime));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(TSCols.lastModified));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(TSCols.duration,
				new TextFlexiCellRenderer(EscapeMode.none)));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(TSCols.status,
				new AssessmentTestSessionStatusRenderer(getTranslator())));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, TSCols.testEntry,
				new AssessmentTestEntryRenderer()));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(TSCols.numOfItemSessions,
				new AssessmentTestSessionDetailsNumberRenderer(getTranslator())));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(TSCols.responded,
				new AssessmentTestSessionDetailsNumberRenderer(getTranslator())));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(TSCols.corrected,
				new AssessmentTestSessionDetailsNumberRenderer(getTranslator())));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(TSCols.score,
				new TextFlexiCellRenderer(EscapeMode.none)));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(TSCols.manualScore,
				new ScoreCellRenderer()));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(TSCols.finalScore,
				new ScoreCellRenderer()));
		
		if(!readOnly) {
			correctionCol = new DefaultFlexiColumnModel(TSCols.correct, "correction");
			correctionCol.setIconHeader("o_icon o_icon_correction");
			correctionCol.setDefaultVisible(false);
			correctionCol.setAlwaysVisible(false);
			correctionCol.setCellRenderer(new CorrectionCellRender());
			columnsModel.addFlexiColumnModel(correctionCol);
			
			DefaultFlexiColumnModel resultsCol = new DefaultFlexiColumnModel(TSCols.results, "open",
					new BooleanCellRenderer(new StaticFlexiCellRenderer("", "open", null, "o_icon o_icon_magnifying_glass", translate("results.report")), null));
			resultsCol.setIconHeader("o_icon o_icon_magnifying_glass");
			columnsModel.addFlexiColumnModel(resultsCol);
		}
		
		columnsModel.addFlexiColumnModel(new ActionsColumnModel(TSCols.tools));

		tableModel = new QTI21AssessmentTestSessionTableModel(columnsModel);
		tableEl = uifactory.addTableElement(getWindowControl(), "sessions", tableModel, 20, false, getTranslator(), formLayout);
		tableEl.setEmptyTableMessageKey("results.empty");

		if(reSecurity.isEntryAdmin() && !readOnly) {
			if(courseNode != null) {
				resetButton = uifactory.addFormLink("menu.reset.title", formLayout, Link.BUTTON);
				resetButton.setIconLeftCSS("o_icon o_icon_reset_data"); 
			} else {
				deleteButton = uifactory.addFormLink("menu.reset.title", formLayout, Link.BUTTON);
				deleteButton.setIconLeftCSS("o_icon o_icon_delete_item"); 
			}
		}
		
		tableEl.setAndLoadPersistedPreferences(ureq, "qti-details-v2.1-" + entry.getKey() + "-" + subIdent);
	}
	
	protected void updateModel() {
		boolean grading = false;
		boolean manualCorrections = false;
		if(courseNode != null) {
			String correctionMode = courseNode.getModuleConfiguration().getStringValue(IQEditController.CONFIG_CORRECTION_MODE);
			grading = IQEditController.CORRECTION_GRADING.equals(correctionMode);
			manualCorrections = !grading && IQEditController.CORRECTION_MANUAL.equals(correctionMode);
		}
		
		List<AssessmentTestSessionStatistics> sessionsStatistics = qtiService.getAssessmentTestSessionsStatistics(entry, subIdent, assessedIdentity, false);
		Collections.sort(sessionsStatistics, new AssessmentTestSessionStatisticsCreationDateComparator());
		
		int pos = 1;
		List<QTI21AssessmentTestSessionDetails> infos = new ArrayList<>();
		final Map<RepositoryEntry,Boolean> manualCorrectionsMap = new HashMap<>();
		for(AssessmentTestSessionStatistics sessionStatistics:sessionsStatistics) {
			RepositoryEntry testEntry = sessionStatistics.testSession().getTestEntry();
			if(!grading) {
				manualCorrections = manualCorrections || manualCorrectionsMap
						.computeIfAbsent(testEntry, re -> qtiService.needManualCorrection(re))
						.booleanValue();
			}
			infos.add(forgeDetailsRow(sessionStatistics, pos++));
		}
		if(correctionCol != null) {
			boolean visible = manualCorrections || tableEl.isColumnModelVisible(correctionCol);
			correctionCol.setAlwaysVisible(manualCorrections);
			correctionCol.setDefaultVisible(visible);
			tableEl.setColumnModelVisible(correctionCol, visible);
		}
		
		Collections.sort(infos, new AssessmentTestSessionDetailsComparator());
		tableModel.setObjects(infos);
		tableEl.reset(true, true, true);
		
		if(resetButton != null) {
			resetButton.setVisible(!sessionsStatistics.isEmpty());
		}
		if(deleteButton != null) {
			deleteButton.setVisible(!sessionsStatistics.isEmpty());
		}
	}
	
	private QTI21AssessmentTestSessionDetails forgeDetailsRow(AssessmentTestSessionStatistics sessionStatistics, int pos) {
		AssessmentTestSession testSession = sessionStatistics.testSession();
		
		int responded = 0;
		int numOfItems = 0;
		boolean error = false;
		boolean suspended = false;
		try {
			TestSessionState testSessionState = qtiService.loadTestSessionState(testSession);
			TestPlan testPlan = testSessionState.getTestPlan();
			List<TestPlanNode> nodes = testPlan.getTestPlanNodeList();
			suspended |= testSessionState.getDurationIntervalStartTime() == null;
			
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
		} catch(Exception e) {
			logError("Cannot read test results", e);
			error = true;
		}
		
		error |= testSession.isExploded();
		SessionStatus status = evaluateStatus(testSession, error, suspended);
		QTI21AssessmentTestSessionDetails row = new QTI21AssessmentTestSessionDetails(testSession,
				numOfItems, responded, sessionStatistics.numOfCorrectedItems(), sessionStatistics.automaticScore(), status, error, pos);
		FormLink tools = ActionsColumnModel.createLink(uifactory, getTranslator());
		row.setToolsLink(tools);
		tools.setUserObject(row);
		return row;
	}
	
	private SessionStatus evaluateStatus(AssessmentTestSession session, boolean error, boolean suspended) {
		if(error) {
			return SessionStatus.ERROR;
		}
		if(session.isCancelled()) {
			return SessionStatus.CANCELLED;
		}
		if(session.getFinishTime() != null && session.getTerminationTime() == null) {
			return SessionStatus.REVIEWING;
		}
		if(session.getFinishTime() != null && session.getTerminationTime() != null) {
			return SessionStatus.TERMINATED;
		}
		if(suspended) {
			return SessionStatus.SUSPENDED;
		}
		return SessionStatus.RUNNING;
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(cmc == source) {
			cleanUp();
		} else if(correctionCtrl == source) {
			if(event instanceof CompleteAssessmentTestSessionEvent catse) {
				if(courseNode != null) {
					doUpdateCourseNode(correctionCtrl.getAssessmentTestSession(), catse.getAssessmentTest(), AssessmentEntryStatus.done);
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
			} else if(event == Event.BACK_EVENT) {
				stackPanel.popController(correctionCtrl);
				cleanUp();
				updateModel();
				fireEvent(ureq, Event.CHANGED_EVENT);
			}
		} else if(retrieveConfirmationCtr == source) {
			if(DialogBoxUIFactory.isYesEvent(event) || DialogBoxUIFactory.isOkEvent(event)) {
				doPullSession(ureq, (AssessmentTestSession)retrieveConfirmationCtr.getUserObject());
				updateModel();
			}
		} else if(invalidateConfirmationCtr == source || revalidateConfirmationCtr == source || deleteToolCtrl == source) {
			if(event == Event.DONE_EVENT || event == Event.CHANGED_EVENT) {
				updateModel();
				fireEvent(ureq, Event.CHANGED_EVENT);
			}
			cmc.deactivate();
			cleanUp();
		} else if(confirmResetDataCtrl == source) {
			if(event == Event.DONE_EVENT || event == Event.CHANGED_EVENT) {
				doResetCourseNodeData(ureq, confirmResetDataCtrl.getDataContext());
				updateModel();
				fireEvent(ureq, Event.CHANGED_EVENT);
			}
			cmc.deactivate();
			cleanUp();
		} else if(reopenForCorrectionCtrl == source) {
			cmc.deactivate();
			AssessmentTestSession session = reopenForCorrectionCtrl.getAssessmentTestSession();
			cleanUp();
			if(event == Event.DONE_EVENT || event == Event.CHANGED_EVENT) {
				fireEvent(ureq, Event.CHANGED_EVENT);
				AssessmentTestSession testSession = qtiService.getAssessmentTestSession(session.getKey());
				doOpenCorrection(ureq, testSession);
			}
		} else if(logViewerCtrl == source) {
			cmc.deactivate();
			cleanUp();
		} else if(toolsCtrl == source) {
			if(event == Event.DONE_EVENT) {
				calloutCtrl.deactivate();
				cleanUp();
			}
		}
		super.event(ureq, source, event);
	}
	
	private void cleanUp() {
		removeAsListenerAndDispose(invalidateConfirmationCtr);
		removeAsListenerAndDispose(revalidateConfirmationCtr);
		removeAsListenerAndDispose(retrieveConfirmationCtr);
		removeAsListenerAndDispose(reopenForCorrectionCtrl);
		removeAsListenerAndDispose(confirmResetDataCtrl);
		removeAsListenerAndDispose(correctionCtrl);
		removeAsListenerAndDispose(deleteToolCtrl);
		removeAsListenerAndDispose(logViewerCtrl);
		removeAsListenerAndDispose(calloutCtrl);
		removeAsListenerAndDispose(resultCtrl);
		removeAsListenerAndDispose(toolsCtrl);
		removeAsListenerAndDispose(cmc);
		invalidateConfirmationCtr = null;
		revalidateConfirmationCtr = null;
		retrieveConfirmationCtr = null;
		reopenForCorrectionCtrl = null;
		confirmResetDataCtrl = null;
		correctionCtrl = null;
		deleteToolCtrl = null;
		logViewerCtrl = null;
		calloutCtrl = null;
		resultCtrl = null;
		toolsCtrl = null;
		cmc = null;
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(deleteButton == source) {
			doDeleteData(ureq);
		} else if(resetButton == source) {
			doConfirmResetCourseNodeData(ureq);
		} else if(tableEl == source) {
			if(event instanceof SelectionEvent se) {
				String cmd = se.getCommand();
				QTI21AssessmentTestSessionDetails row = tableModel.getObject(se.getIndex());
				AssessmentTestSession testSession = qtiService.getAssessmentTestSession(row.getTestSession().getKey());
				if("open".equals(cmd)) {
					if(testSession.getFinishTime() == null) {
						doConfirmPullSession(ureq, testSession);
					} else {
						doOpenResult(ureq, testSession);
					}
				} else if("correction".equals(cmd) ) {
					doCorrection(ureq, testSession);
				} else if("preview".equals(cmd)) {
					doOpenCorrection(ureq, testSession);
				}
			}
		} else if(source instanceof FormLink link) {
			if(link.getCmd().startsWith("tools")) {
				doTools(ureq, link, (QTI21AssessmentTestSessionDetails)link.getUserObject());
			}
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}

	private void doCorrection(UserRequest ureq, AssessmentTestSession session) {
		boolean assessmentEntryDone = isAssessmentEntryDone();
		if(assessmentEntryDone && !readOnly) {
			confirmReopenAssessment(ureq, session);
		} else {
			doOpenCorrection(ureq, session);
		}
	}
	
	private void doOpenCorrection(UserRequest ureq, AssessmentTestSession session) {
		boolean assessmentEntryDone = isAssessmentEntryDone();
		boolean running = session.getTerminationTime() == null && session.getFinishTime() == null;
		RepositoryEntry testEntry = session.getTestEntry();
		File unzippedDirRoot = FileResourceManager.getInstance().unzipFileResource(testEntry.getOlatResource());
		ResolvedAssessmentTest resolvedAssessmentTest = qtiService.loadAndResolveAssessmentTest(unzippedDirRoot, false, false);
		ManifestBuilder manifestBuilder = ManifestBuilder.read(new File(unzippedDirRoot, "imsmanifest.xml"));
		try {
			TestSessionState testSessionState = qtiService.loadTestSessionState(session);
			// use mutable maps to allow updates
			Map<Identity,AssessmentTestSession> lastSessions = new HashMap<>();
			lastSessions.put(assessedIdentity, session);
			Map<Identity, TestSessionState> testSessionStates = new HashMap<>();
			testSessionStates.put(assessedIdentity, testSessionState);
			boolean correctionReadOnly = readOnly || assessmentEntryDone || running;
			CorrectionOverviewModel model = new CorrectionOverviewModel(entry, courseNode, testEntry,
					resolvedAssessmentTest, manifestBuilder, lastSessions, testSessionStates, getTranslator());
			correctionCtrl = new CorrectionIdentityAssessmentItemListController(ureq, getWindowControl(), stackPanel,
					model, assessedIdentity, correctionReadOnly);
			listenTo(correctionCtrl);
			stackPanel.pushController(translate("correct"), correctionCtrl);
		} catch(Exception e) {
			logError("Cannot read results", e);
			showError("error.assessment.test.session");
		}
	}
	
	private void confirmReopenAssessment(UserRequest ureq, AssessmentTestSession session) {
		if(guardModalController(reopenForCorrectionCtrl)) return;
		
		reopenForCorrectionCtrl = new ConfirmReopenAssessmentEntryController(ureq, getWindowControl(),
				assessedUserCourseEnv, courseNode, session);
		listenTo(reopenForCorrectionCtrl);
		
		cmc = new CloseableModalController(getWindowControl(), translate("close"), reopenForCorrectionCtrl.getInitialComponent(),
				true, translate("reopen.assessment.title"));
		cmc.activate();
		listenTo(cmc);
	}
	
	private boolean isAssessmentEntryDone() {
		if(assessedUserCourseEnv != null) {
			AssessmentEvaluation eval = assessedUserCourseEnv.getScoreAccounting().getScoreEvaluation(courseNode);
			return eval != null && eval.getAssessmentStatus() == AssessmentEntryStatus.done;
		}
		return false;
	}
	
	private void doUpdateCourseNode(AssessmentTestSession session, AssessmentTest assessmentTest, AssessmentEntryStatus entryStatus) {
		Double cutValue = QtiNodesExtractor.extractCutValue(assessmentTest);
		
		AssessmentConfig assessmentConfig = courseAssessmentService.getAssessmentConfig(entry, courseNode);
		ScoreEvaluation scoreEval = courseAssessmentService.getAssessmentEvaluation(courseNode, assessedUserCourseEnv);
		BigDecimal finalScore = session.getFinalScore();
		Float score = finalScore == null ? null : finalScore.floatValue();
		BigDecimal scoreScale = ScoreScalingHelper.getScoreScale(courseNode);
		Float weightScore = ScoreScalingHelper.getWeightedFloatScore(score, scoreScale);
		String grade = scoreEval.getGrade();
		String gradeSystemIdent = scoreEval.getGradeSystemIdent();
		String performanceClassIdent = scoreEval.getPerformanceClassIdent();
		Boolean passed = scoreEval.getPassed();
		if(session.getManualScore() != null && finalScore != null) {
			if (assessmentConfig.hasGrade() && gradeModule.isEnabled()) {
				if (assessmentConfig.isAutoGrade() || StringHelper.containsNonWhitespace(scoreEval.getGrade())) {
					GradeScale gradeScale = gradeService.getGradeScale(
							assessedUserCourseEnv.getCourseEnvironment().getCourseGroupManager().getCourseEntry(),
							courseNode.getIdent());
					NavigableSet<GradeScoreRange> gradeScoreRanges = null;
					gradeScoreRanges = gradeService.getGradeScoreRanges(gradeScale, getLocale());
					GradeScoreRange gradeScoreRange = gradeService.getGradeScoreRange(gradeScoreRanges, score);
					grade = gradeScoreRange.getGrade();
					gradeSystemIdent = gradeScoreRange.getGradeSystemIdent();
					performanceClassIdent = gradeScoreRange.getPerformanceClassIdent();
					passed = gradeScoreRange.getPassed();
				}
			} else if (cutValue != null) {
				boolean calculated = finalScore.compareTo(BigDecimal.valueOf(cutValue.doubleValue())) >= 0;
				passed = Boolean.valueOf(calculated);
			}
		}
		AssessmentEntryStatus finalStatus = entryStatus == null ? scoreEval.getAssessmentStatus() : entryStatus;
		Boolean userVisible = scoreEval.getUserVisible();
		if(userVisible == null && finalStatus == AssessmentEntryStatus.done) {
			boolean canChangeUserVisibility = coachCourseEnv.isAdmin()
					|| coachCourseEnv.getCourseEnvironment().getRunStructure().getRootNode().getModuleConfiguration().getBooleanSafe(STCourseNode.CONFIG_COACH_USER_VISIBILITY);
			userVisible = canChangeUserVisibility
					? courseNode.isScoreVisibleAfterCorrection()
					: Boolean.FALSE;
		}
		ScoreEvaluation manualScoreEval = new ScoreEvaluation(score, weightScore, scoreScale, grade, gradeSystemIdent, performanceClassIdent,
				passed, finalStatus, userVisible, scoreEval.getCurrentRunStartDate(), 
				scoreEval.getCurrentRunCompletion(), scoreEval.getCurrentRunStatus(), session.getKey());
		courseAssessmentService.updateScoreEvaluation(courseNode, manualScoreEval, assessedUserCourseEnv,
				getIdentity(), false, Role.coach);
	}
	
	private void doUpdateEntry(AssessmentTestSession session) {
		qtiService.updateAssessmentEntry(session, true);
	}
	
	private void doDeleteData(UserRequest ureq) {
		AssessmentToolOptions asOptions = new AssessmentToolOptions();
		asOptions.setAdmin(reSecurity.isEntryAdmin());
		asOptions.setIdentities(Collections.singletonList(assessedIdentity));
		
		deleteToolCtrl = new QTI21DeleteDataController(ureq, getWindowControl(), entry, asOptions);
		listenTo(deleteToolCtrl);

		cmc = new CloseableModalController(getWindowControl(), translate("close"), deleteToolCtrl.getInitialComponent(),
				true, translate("table.header.results"));
		cmc.activate();
		listenTo(cmc);
	}

	private void doConfirmResetCourseNodeData(UserRequest ureq) {
		RepositoryEntry courseEntry = assessedUserCourseEnv.getCourseEnvironment().getCourseGroupManager().getCourseEntry();
		ResetDataContext dataContext = new ResetDataContext(courseEntry);
		dataContext.setResetParticipants(ResetParticipants.selected);
		dataContext.setSelectedParticipants(List.of(assessedIdentity));
		dataContext.setResetCourse(ResetCourse.elements);
		dataContext.setCourseNodes(List.of(courseNode));
		
		confirmResetDataCtrl = new ConfirmResetDataController(ureq, getWindowControl(), dataContext, null);
		listenTo(confirmResetDataCtrl);

		cmc = new CloseableModalController(getWindowControl(), translate("close"), confirmResetDataCtrl.getInitialComponent(),
				true, translate("table.header.results"));
		cmc.activate();
		listenTo(cmc);
	}

	private void doResetCourseNodeData(UserRequest ureq, ResetDataContext dataContext) {
		List<Identity> identities = dataContext.getSelectedParticipants();
		
		ResetCourseDataHelper resetCourseNodeHelper = new ResetCourseDataHelper(assessedUserCourseEnv.getCourseEnvironment());
		MediaResource archiveResource = resetCourseNodeHelper
				.resetCourseNodes(identities, dataContext.getCourseNodes(), false, getIdentity(), Role.coach);
		if(archiveResource != null) {
			Command downloadCmd = CommandFactory.createDownloadMediaResource(ureq, archiveResource);
			getWindowControl().getWindowBackOffice().sendCommandTo(downloadCmd);
		}
	}

	private void doConfirmPullSession(UserRequest ureq, AssessmentTestSession session) {
		String title = translate("pull");
		String fullname = userManager.getUserDisplayName(session.getIdentity());
		String text = translate("retrievetest.confirm.text", fullname);
		retrieveConfirmationCtr = activateOkCancelDialog(ureq, title, text, retrieveConfirmationCtr);
		retrieveConfirmationCtr.setUserObject(session);
	}
	
	private void doPullSession(UserRequest ureq, AssessmentTestSession session) {
		//reload it to prevent lazy loading issues
		session = qtiService.getAssessmentTestSession(session.getKey());
		session = qtiService.pullSession(session, getSignatureOptions(session), getIdentity());
		if(courseNode != null) {
			courseNode.pullAssessmentTestSession(session, assessedUserCourseEnv, getIdentity(), Role.coach, getLocale());

			String channel = assessedIdentity == null ? session.getAnonymousIdentifier() : assessedIdentity.getKey().toString();
			RepositoryEntry courseEntry = assessedUserCourseEnv.getCourseEnvironment().getCourseGroupManager().getCourseEntry();
			imService.endChannel(getIdentity(), courseEntry.getOlatResource(), courseNode.getIdent(), channel);
		}
		updateModel();
		fireEvent(ureq, Event.CHANGED_EVENT);
	}
	
	private DigitalSignatureOptions getSignatureOptions(AssessmentTestSession session) {
		RepositoryEntry testEntry = session.getTestEntry();
		QTI21DeliveryOptions deliveryOptions = qtiService.getDeliveryOptions(testEntry);
		
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
				QTI21AssessmentRunController.decorateCourseConfirmation(session, options, courseEnv, courseNode, testEntry, null, getLocale());
			}
		}
		return options;
	}

	private void doOpenResult(UserRequest ureq, AssessmentTestSession session) {
		if(guardModalController(resultCtrl)) return;
		
		//reload it to prevent lazy loading issues
		session = qtiService.getAssessmentTestSession(session.getKey());

		FileResourceManager frm = FileResourceManager.getInstance();
		File fUnzippedDirRoot = frm.unzipFileResource(session.getTestEntry().getOlatResource());
		URI assessmentObjectUri = qtiService.createAssessmentTestUri(fUnzippedDirRoot);
		File submissionDir = qtiService.getSubmissionDirectory(session);
		String mapperUri = registerCacheableMapper(ureq, "QTI21DetailsResources::" + session.getKey(),
				new ResourcesMapper(assessmentObjectUri, fUnzippedDirRoot, submissionDir));
		
		resultCtrl = new AssessmentResultController(ureq, getWindowControl(), assessedIdentity, false, session,
				fUnzippedDirRoot, mapperUri, null, QTI21AssessmentResultsOptions.allOptions(), true, true, true);
		listenTo(resultCtrl);
		cmc = new CloseableModalController(getWindowControl(), translate("close"), resultCtrl.getInitialComponent(),
				true, translate("results.report"));
		cmc.activate();
		listenTo(cmc);
	}
	
	private void confirmInvalidateTestSession(UserRequest ureq, AssessmentTestSession session) {
		if(guardModalController(invalidateConfirmationCtr)) return;
		
		AssessmentTestSession lastSession = tableModel.getLastTestSession();
		boolean isLastSession = session.equals(lastSession);
		if(courseNode == null) {
			invalidateConfirmationCtr = new ConfirmAssessmentTestSessionInvalidationController(ureq, getWindowControl(),
				session, isLastSession, session.getTestEntry(), assessedIdentity);
		} else {
			invalidateConfirmationCtr = new ConfirmAssessmentTestSessionInvalidationController(ureq, getWindowControl(),
				session, isLastSession, courseNode, assessedUserCourseEnv);
		}
		listenTo(invalidateConfirmationCtr);

		String title = translate("invalidate.test.confirm.title");
		cmc = new CloseableModalController(getWindowControl(), translate("close"), invalidateConfirmationCtr.getInitialComponent(),
				true, title);
		cmc.activate();
		listenTo(cmc);
	}
	
	private void confirmRevalidateTestSession(UserRequest ureq, AssessmentTestSession session) {
		if(guardModalController(revalidateConfirmationCtr)) return;
		
		if(courseNode == null) {
			revalidateConfirmationCtr = new ConfirmAssessmentTestSessionRevalidationController(ureq, getWindowControl(),
				session, entry, assessedIdentity);
		} else {
			revalidateConfirmationCtr = new ConfirmAssessmentTestSessionRevalidationController(ureq, getWindowControl(),
				session, courseNode, assessedUserCourseEnv);
		}
		listenTo(revalidateConfirmationCtr);

		String title = translate("revalidate.test.confirm.title");
		cmc = new CloseableModalController(getWindowControl(), translate("close"), revalidateConfirmationCtr.getInitialComponent(),
				true, title);
		cmc.activate();
		listenTo(cmc);
	}
	
	private void doTools(UserRequest ureq, FormLink link, QTI21AssessmentTestSessionDetails row) {
		removeAsListenerAndDispose(cmc);
		removeAsListenerAndDispose(toolsCtrl);
		
		toolsCtrl = new ToolsController(ureq, getWindowControl(), row);
		listenTo(toolsCtrl);

		calloutCtrl = new CloseableCalloutWindowController(ureq, getWindowControl(),
				toolsCtrl.getInitialComponent(), link.getFormDispatchId(), "", true, "");
		listenTo(calloutCtrl);
		calloutCtrl.activate();
	}
	
	private void doDownloadLog(UserRequest ureq, AssessmentTestSession session) {
		File logFile = qtiService.getAssessmentSessionAuditLogFile(session);
		if(logFile != null && logFile.exists()) {
			String filename = generateDownloadName("auditlog", session) + ".log";
			ureq.getDispatchResult().setResultingMediaResource(new LogDownload(logFile, filename));
		} else {
			showWarning("warning.download.log");
		}
	}
	
	private void doDownloadPdf(UserRequest ureq, QTI21AssessmentTestSessionDetails row) {
		final AssessmentTestSession session = qtiService.getAssessmentTestSession(row.getTestSession().getKey());
		FileResourceManager frm = FileResourceManager.getInstance();
		final File fUnzippedDirRoot = frm.unzipFileResource(session.getTestEntry().getOlatResource());
		URI assessmentObjectUri = qtiService.createAssessmentTestUri(fUnzippedDirRoot);
		File submissionDir = qtiService.getSubmissionDirectory(session);

		ControllerCreator creator = (uureq, wwControl) -> {
			String mapperUri = registerCacheableMapper(uureq, "QTI21DetailsResources::" + session.getKey(),
					new ResourcesMapper(assessmentObjectUri, fUnzippedDirRoot, submissionDir));
			final AssessmentTestSession ssession = qtiService.getAssessmentTestSession(row.getTestSession().getKey());
			AssessmentResultController printViewCtrl = new AssessmentResultController(uureq, wwControl, assessedIdentity, false,
					ssession, fUnzippedDirRoot, mapperUri, null, QTI21AssessmentResultsOptions.allOptions(), false, true, false);
			listenTo(printViewCtrl);
			return printViewCtrl;
		};
		
		String filename = generateDownloadName("results_", session);
		MediaResource pdf = pdfService.convert(filename, getIdentity(), creator,
				getWindowControl(), PdfOutputOptions.defaultOptions());
		ureq.getDispatchResult().setResultingMediaResource(pdf);
	}
	
	private void doOpenLogViewer(UserRequest ureq, AssessmentTestSession session) {
		File logFile = qtiService.getAssessmentSessionAuditLogFile(session);
		String filename = generateDownloadName("auditlog_formatted_", session);
		logViewerCtrl = new LogViewerController(ureq, getWindowControl(), session, logFile, filename);
		listenTo(logViewerCtrl);

		cmc = new CloseableModalController(getWindowControl(), translate("close"), logViewerCtrl.getInitialComponent(),
				true, translate("open.log.viewer") + "<br>And some information");
		cmc.setCustomWindowCSS("o_modal_large");
		cmc.setTitle(getLogModalTitle());
		
		cmc.activate();
		listenTo(cmc);
	}
	
	private String getLogModalTitle() {
		StringBuilder title = new StringBuilder(64);
		if(entry != null) {
			title.append(StringHelper.escapeHtml(entry.getDisplayname()));
		}
		
		if(courseNode != null) {
			if(title.length() > 0) title.append(" / ");
			title.append(StringHelper.escapeHtml(courseNode.getLongTitle()));
		}
		
		if(assessedIdentity != null) {
			if(title.length() > 0) title.append(" / ");
			if(StringHelper.containsNonWhitespace(assessedIdentity.getUser().getFirstName())) {
				title.append(StringHelper.escapeHtml(assessedIdentity.getUser().getFirstName()));
			}
			if(StringHelper.containsNonWhitespace(assessedIdentity.getUser().getLastName())) {
				title.append(" ").append(StringHelper.escapeHtml(assessedIdentity.getUser().getLastName()));
			}
		}
		
		return translate("open.log.viewer") + "<span class='o_formated_log_title'>" + title.toString() + "</span>";
	}
	
	private String generateDownloadName(String prefix, AssessmentTestSession session) {
		String filename = prefix + "_";
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
		return filename;
	}
	
	private class ToolsController extends BasicController {
		
		private Link pdfLink;
		private Link logLink;
		private Link pullLink;
		private Link logViewerLink;
		private Link invalidateLink;
		private Link revalidateLink;
		private Link correctionLink;
		private Link viewResultsLink;
		private Link previewLink;
		
		private final QTI21AssessmentTestSessionDetails row;
		
		public ToolsController(UserRequest ureq, WindowControl wControl, QTI21AssessmentTestSessionDetails row) {
			super(ureq, wControl);
			this.row = row;
			
			VelocityContainer mainVC = createVelocityContainer("details_tools");
			if(pdfModule.isEnabled()) {
				pdfLink = LinkFactory.createLink("download.pdf", mainVC, this);
				pdfLink.setIconLeftCSS("o_icon o_icon-fw o_filetype_pdf");
			}
			
			logLink = LinkFactory.createLink("download.log", mainVC, this);
			logLink.setIconLeftCSS("o_icon o_icon-fw o_icon_file_export");
			
			logViewerLink = LinkFactory.createLink("open.log.viewer", mainVC, this);
			logViewerLink.setIconLeftCSS("o_icon o_icon-fw o_icon_log");
			
			Boolean correction = tableModel.isCorrectionAllowed(row);
			if(correction != null && correction.booleanValue() && !readOnly) {
				correctionLink = LinkFactory.createLink("correct", mainVC, this);
				correctionLink.setIconLeftCSS("o_icon o_icon-fw o_icon_correction");
			} else if(correction != null && !correction.booleanValue()) {
				previewLink = LinkFactory.createLink("preview", mainVC, this);
				previewLink.setIconLeftCSS("o_icon o_icon-fw o_icon_correction");
			}
			
			if(row.getTestSession().getFinishTime() != null || row.getTestSession().getTerminationTime() != null) {
				viewResultsLink = LinkFactory.createLink("view.results", mainVC, this);
				viewResultsLink.setIconLeftCSS("o_icon o_icon-fw o_icon_magnifying_glass");
			}
			if(!readOnly && row.getTestSession().getTerminationTime() == null) {
				pullLink = LinkFactory.createLink("pull", mainVC, this);
				pullLink.setIconLeftCSS("o_icon o_icon-fw o_icon_pull");
			}
			
			if(!readOnly) {
				if(row.getTestSession().isCancelled()) {
					revalidateLink = LinkFactory.createLink("revalidate.test", mainVC, this);
					revalidateLink.setIconLeftCSS("o_icon o_icon-fw o_icon_log");
				} else if (row.getTestSession().getFinishTime() != null || row.getTestSession().getTerminationTime() != null) {
					invalidateLink = LinkFactory.createLink("invalidate", mainVC, this);
					invalidateLink.setIconLeftCSS("o_icon o_icon-fw o_icon_ban");
				}
			}
			putInitialPanel(mainVC);
		}

		@Override
		protected void event(UserRequest ureq, Component source, Event event) {
			fireEvent(ureq, Event.DONE_EVENT);
			if(pdfLink == source) {
				doDownloadPdf(ureq, row);
			} else if(logLink == source) {
				doDownloadLog(ureq, row.getTestSession());
			} else if(invalidateLink == source) { 
				confirmInvalidateTestSession(ureq, row.getTestSession());
			} else if(revalidateLink == source) {
				confirmRevalidateTestSession(ureq, row.getTestSession());
			} else if(logViewerLink == source) {
				doOpenLogViewer(ureq, row.getTestSession());
			} else if(viewResultsLink == source) {
				doOpenResult(ureq, row.getTestSession());
			} else if(pullLink == source) {
				doConfirmPullSession(ureq, row.getTestSession());
			} else if(correctionLink == source) {
				doCorrection(ureq, row.getTestSession());
			} else if(previewLink == source) {
				doOpenCorrection(ureq, row.getTestSession());
			}
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
	
	public static class AssessmentTestSessionStatisticsCreationDateComparator implements Comparator<AssessmentTestSessionStatistics> {

		@Override
		public int compare(AssessmentTestSessionStatistics q1, AssessmentTestSessionStatistics q2) {
			Date d1 = q1.testSession().getCreationDate();
			Date d2 = q2.testSession().getCreationDate();
			return d1.compareTo(d2);
		}
	}
	
	private class CorrectionCellRender implements FlexiCellRenderer, ActionDelegateCellRenderer {
		
		private final FlexiCellRenderer correctionDelegate = new StaticFlexiCellRenderer("", "correction", null, "o_icon o_icon_correction", translate("correct"));
		private final FlexiCellRenderer previewDelegate = new StaticFlexiCellRenderer("", "preview", null, "o_icon o_icon_preview", translate("preview"));
		private final List<String> actions = List.of("correction", "preview");
		
		@Override
		public List<String> getActions() {
			return actions;
		}
		
		@Override
		public void render(Renderer renderer, StringOutput target, Object cellValue, int row,
				FlexiTableComponent source, URLBuilder ubu, Translator translator) {
			if(Boolean.TRUE.equals(cellValue)) {
				correctionDelegate.render(renderer, target, cellValue, row, source, ubu, translator);
			} else if(Boolean.FALSE.equals(cellValue)) {
				previewDelegate.render(renderer, target, cellValue, row, source, ubu, translator);
			}
		}
	}
}