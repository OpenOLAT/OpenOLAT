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

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NavigableSet;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.olat.basesecurity.IdentityRef;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.TimeFlexiCellRenderer;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.stack.PopEvent;
import org.olat.core.gui.components.stack.TooledStackedPanel;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.id.Identity;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.id.context.StateEntry;
import org.olat.core.util.StringHelper;
import org.olat.core.util.resource.OresHelper;
import org.olat.course.CourseFactory;
import org.olat.course.ICourse;
import org.olat.course.assessment.AssessmentHelper;
import org.olat.course.assessment.CourseAssessmentService;
import org.olat.course.assessment.handler.AssessmentConfig;
import org.olat.course.assessment.handler.AssessmentConfig.Mode;
import org.olat.course.assessment.ui.tool.IdentityListCourseNodeController;
import org.olat.course.assessment.ui.tool.IdentityListCourseNodeTableModel.IdentityCourseElementCols;
import org.olat.course.assessment.ui.tool.IdentityListCourseNodeToolsController;
import org.olat.course.nodes.CourseNode;
import org.olat.course.nodes.IQTESTCourseNode;
import org.olat.course.nodes.STCourseNode;
import org.olat.course.nodes.iq.QTI21IdentityListCourseNodeToolsController.AssessmentTestSessionDetailsComparator;
import org.olat.course.run.environment.CourseEnvironment;
import org.olat.course.run.scoring.AssessmentEvaluation;
import org.olat.course.run.scoring.ScoreEvaluation;
import org.olat.course.run.userview.UserCourseEnvironment;
import org.olat.fileresource.types.ImsQTI21Resource;
import org.olat.ims.qti21.AssessmentTestSession;
import org.olat.ims.qti21.QTI21DeliveryOptions;
import org.olat.ims.qti21.QTI21Service;
import org.olat.ims.qti21.model.jpa.AssessmentTestSessionStatistics;
import org.olat.ims.qti21.model.xml.QtiNodesExtractor;
import org.olat.ims.qti21.resultexport.IdentitiesList;
import org.olat.ims.qti21.resultexport.QTI21ExportResultsController;
import org.olat.ims.qti21.ui.QTI21ResetDataController;
import org.olat.ims.qti21.ui.QTI21RetrieveTestsController;
import org.olat.ims.qti21.ui.assessment.CorrectionOverviewController;
import org.olat.ims.qti21.ui.assessment.ValidationXmlSignatureController;
import org.olat.ims.qti21.ui.statistics.QTI21StatisticsToolController;
import org.olat.modules.ModuleConfiguration;
import org.olat.modules.assessment.AssessmentEntry;
import org.olat.modules.assessment.AssessmentToolOptions;
import org.olat.modules.assessment.Role;
import org.olat.modules.assessment.model.AssessmentEntryStatus;
import org.olat.modules.assessment.ui.AssessedIdentityElementRow;
import org.olat.modules.assessment.ui.AssessmentToolContainer;
import org.olat.modules.assessment.ui.AssessmentToolSecurityCallback;
import org.olat.modules.assessment.ui.event.CompleteAssessmentTestSessionEvent;
import org.olat.modules.dcompensation.DisadvantageCompensation;
import org.olat.modules.dcompensation.DisadvantageCompensationService;
import org.olat.modules.grade.GradeModule;
import org.olat.modules.grade.GradeScale;
import org.olat.modules.grade.GradeScoreRange;
import org.olat.modules.grade.GradeService;
import org.olat.modules.grading.GradingAssignment;
import org.olat.modules.grading.GradingService;
import org.olat.repository.RepositoryEntry;
import org.springframework.beans.factory.annotation.Autowired;

import uk.ac.ed.ph.jqtiplus.node.test.AssessmentTest;

/**
 * 
 * Initial date: 18 déc. 2017<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class IQIdentityListCourseNodeController extends IdentityListCourseNodeController {
	
	private static final String ORES_TYPE_EXPORT_RESULTS = "Export";
	
	private FormLink extraTimeButton;
	private FormLink exportResultsButton;
	private FormLink statsButton;
	private FormLink validateButton;
	private FormLink correctionButton;
	private FormLink pullButton;
	private FormLink resetButton;
	
	private FormLink bulkExportResultsButton;
	private FormLink bulkStatsButton;
	private FormLink bulkCorrectionButton;

	private Controller retrieveConfirmationCtr;
	private QTI21ResetDataController resetDataCtrl;
	private ConfirmExtraTimeController extraTimeCtrl;
	private QTI21ExportResultsController exportResultsCtrl;
	private ValidationXmlSignatureController validationCtrl;
	private CorrectionOverviewController correctionIdentitiesCtrl;
	private ConfirmReopenAssessmentEntriesController reopenForCorrectionCtrl;
	
	private boolean modelDirty = false;

	@Autowired
	private QTI21Service qtiService;
	@Autowired
	private GradeModule gradeModule;
	@Autowired
	private GradeService gradeService;
	@Autowired
	private GradingService gradingService;
	@Autowired
	private CourseAssessmentService courseAssessmentService;
	@Autowired
	private DisadvantageCompensationService disadvantageCompensationService;
	
	public IQIdentityListCourseNodeController(UserRequest ureq, WindowControl wControl, TooledStackedPanel stackPanel,
			RepositoryEntry courseEntry, CourseNode courseNode, UserCourseEnvironment coachCourseEnv,
			AssessmentToolContainer toolContainer, AssessmentToolSecurityCallback assessmentCallback, boolean showTitle) {
		super(ureq, wControl, stackPanel, courseEntry, courseNode, coachCourseEnv, toolContainer, assessmentCallback, showTitle);
		if(stackPanel != null) {
			stackPanel.addListener(this);
		}
		flc.contextPut("showTitle", Boolean.valueOf(showTitle));
		flc.setDirty(true);
	}
	
	@Override
	protected String getTableId() {
		if(isTestQTI21()) {
			IQTESTCourseNode testCourseNode = (IQTESTCourseNode)courseNode;
			RepositoryEntry qtiTestEntry = getReferencedRepositoryEntry();
			if(testCourseNode != null && testCourseNode.hasQTI21TimeLimit(qtiTestEntry, courseEntry, getIdentity())) {
				return"qti21-assessment-tool-identity-list-extra-v3";
			}
			return"qti21-assessment-tool-identity-list-v3";
		}
		return "qti-assessment-tool-identity-list-v3";
	}

	@Override
	protected void initStatusColumns(FlexiTableColumnModel columnsModel) {
		super.initStatusColumns(columnsModel);
		IQTESTCourseNode testCourseNode = (IQTESTCourseNode)courseNode;
		AssessmentConfig assessmentConfig = courseAssessmentService.getAssessmentConfig(courseEntry, courseNode);
		if(testCourseNode != null && Mode.setByNode.equals(assessmentConfig.getCompletionMode())) {
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(IdentityCourseElementCols.currentRunStart,
					new TimeFlexiCellRenderer(getLocale(), true)));
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(IdentityCourseElementCols.currentCompletion));
		}
		
		RepositoryEntry qtiTestEntry = getReferencedRepositoryEntry();
		if(testCourseNode != null && testCourseNode.hasQTI21TimeLimit(qtiTestEntry, courseEntry, getIdentity())) {
			int timeLimitInSeconds = testCourseNode.getQTI21TimeLimitMaxInSeconds(qtiTestEntry);
			boolean suspendEnabled = isSuspendEnable();
			if(!suspendEnabled) {
				Date endDate = null;
				if(testCourseNode.getModuleConfiguration().getBooleanSafe(IQEditController.CONFIG_KEY_DATE_DEPENDENT_TEST, false)) {
					endDate = testCourseNode.getModuleConfiguration().getDateValue(IQEditController.CONFIG_KEY_END_TEST_DATE);
				}
				FlexiCellRenderer renderer = new EndTimeCellRenderer(timeLimitInSeconds, endDate, getLocale());
				columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel("table.header.end.date",
						IdentityCourseElementCols.details.ordinal(), renderer));
			}

			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel("table.header.extra.time",
					IdentityCourseElementCols.details.ordinal(), new ExtraTimeCellRenderer()));
		}
	}
	
	private boolean isSuspendEnable() {
		ModuleConfiguration config = courseNode.getModuleConfiguration();
		QTI21DeliveryOptions testOptions = qtiService.getDeliveryOptions(getReferencedRepositoryEntry());
		boolean configRef = config.getBooleanSafe(IQEditController.CONFIG_KEY_CONFIG_REF, false);
		boolean suspendEnabled = false;
		if(!configRef) {
			suspendEnabled = config.getBooleanSafe(IQEditController.CONFIG_KEY_ENABLESUSPEND, testOptions.isEnableSuspend());
		} else {
			suspendEnabled = testOptions.isEnableSuspend();
		}
		return suspendEnabled;
	}

	@Override
	protected void initMultiSelectionTools(UserRequest ureq, FormLayoutContainer formLayout) {
		super.initGradeScaleEditButton(formLayout);
		super.initBulkStatusTools(ureq, formLayout);
		
		RepositoryEntry testEntry = getReferencedRepositoryEntry();
		if(((IQTESTCourseNode)courseNode).hasQTI21TimeLimit(testEntry, courseEntry, getIdentity())) {
			extraTimeButton = uifactory.addFormLink("extra.time", formLayout, Link.BUTTON);
			extraTimeButton.setIconLeftCSS("o_icon o_icon-fw o_icon_extra_time");
			tableEl.addBatchButton(extraTimeButton);
		}
		boolean qti21 = isTestQTI21();
		
		if(qti21) {
			statsButton = uifactory.addFormLink("button.stats", formLayout, Link.BUTTON);
			statsButton.setIconLeftCSS("o_icon o_icon-fw o_icon_statistics_tool");
			
			bulkStatsButton = uifactory.addFormLink("bulk.stats", "button.stats", null, formLayout, Link.BUTTON);
			bulkStatsButton.setIconLeftCSS("o_icon o_icon-fw o_icon_statistics_tool");
			tableEl.addBatchButton(bulkStatsButton);

			exportResultsButton = uifactory.addFormLink("button.export", formLayout, Link.BUTTON);
			exportResultsButton.setIconLeftCSS("o_icon o_icon-fw o_icon_export");
			
			bulkExportResultsButton = uifactory.addFormLink("bulk.export", "button.export", null, formLayout, Link.BUTTON);
			bulkExportResultsButton.setIconLeftCSS("o_icon o_icon-fw o_icon_export");
			tableEl.addBatchButton(bulkExportResultsButton);
		}
		
		if(!coachCourseEnv.isCourseReadOnly()) {
			pullButton = uifactory.addFormLink("retrieve.tests.title", formLayout, Link.BUTTON);
			pullButton.setIconLeftCSS("o_icon o_icon_pull");

			if(qti21) {
				if(getAssessmentCallback().isAdmin()) {
					resetButton = uifactory.addFormLink("tool.delete.data", formLayout, Link.BUTTON); 
					resetButton.setIconLeftCSS("o_icon o_icon_delete_item");
				}
				String correctionMode = courseNode.getModuleConfiguration().getStringValue(IQEditController.CONFIG_CORRECTION_MODE);
				if(!IQEditController.CORRECTION_GRADING.equals(correctionMode) &&
						(IQEditController.CORRECTION_MANUAL.equals(correctionMode) || qtiService.needManualCorrection(testEntry))) {
					correctionButton = uifactory.addFormLink("correction.test.title", formLayout, Link.BUTTON);
					correctionButton.setElementCssClass("o_sel_correction");
					correctionButton.setIconLeftCSS("o_icon o_icon-fw o_icon_correction");
					
					bulkCorrectionButton = uifactory.addFormLink("bulk.test.title", "correction.test.start", null, formLayout, Link.BUTTON);
					bulkCorrectionButton.setIconLeftCSS("o_icon o_icon-fw o_icon_correction");
					tableEl.addBatchButton(bulkCorrectionButton);
				}
				if(courseNode.getModuleConfiguration().getBooleanSafe(IQEditController.CONFIG_DIGITAL_SIGNATURE, false)) {
					validateButton = uifactory.addFormLink("validate.xml.signature", formLayout, Link.BUTTON);
					validateButton.setIconLeftCSS("o_icon o_icon-fw o_icon_correction");
				}
			}
		}
		
		super.initBulkEmailTool(ureq, formLayout);
	}
	
	private boolean isTestRunning() {
		IdentitiesList identities = getIdentities(false);
		if(isTestQTI21()) {
			return qtiService.isRunningAssessmentTestSession(getCourseRepositoryEntry(),
					courseNode.getIdent(), getReferencedRepositoryEntry(), identities.getIdentities());
		}
		return false;
	}
	
	private boolean isTestQTI21() {
		return ImsQTI21Resource.TYPE_NAME.equals(getReferencedRepositoryEntry().getOlatResource().getResourceableTypeName());
	}

	@Override
	public void reload(UserRequest ureq) {
		super.reload(ureq);

		RepositoryEntry testEntry = getReferencedRepositoryEntry();
		boolean timeLimit = ((IQTESTCourseNode)courseNode).hasQTI21TimeLimit(testEntry, courseEntry, getIdentity());
		Map<Long,ExtraInfos> extraInfos = getExtraInfos();
		List<AssessedIdentityElementRow> rows = usersTableModel.getObjects();
		for(AssessedIdentityElementRow row:rows) {
			ExtraInfos infos = extraInfos.get(row.getIdentityKey());
			if(infos != null) {
				if(timeLimit) {
					row.setDetails(infos);
				}
				row.setMaxScore(infos.getMaxScore());
				if(infos.getCompletion() != null) {
					row.getCurrentCompletion().setCompletion(infos.getCompletion());
				} else {
					row.getCurrentCompletion().setCompletion(null);
				}
				if(infos.getStart() != null) {
					row.getCurrentRunStart().setDate(infos.getStart());
				} else {
					row.getCurrentRunStart().setDate(null);
				}
			} else {
				row.setDetails(null);
				row.getCurrentCompletion().setCompletion(null);
				row.getCurrentRunStart().setDate(null);
			}
		}
		
		if(pullButton != null) {
			boolean enabled = isTestRunning();
			pullButton.setEnabled(enabled);
		}
		
		this.modelDirty = true;
	}
	
	/**
	 * @return A map identity key to extra time
	 */
	private Map<Long,ExtraInfos> getExtraInfos() {
		Map<Long,ExtraInfos> identityToExtraTime = new HashMap<>();
		List<AssessmentTestSession> sessions = qtiService
				.getAssessmentTestSessions(getCourseRepositoryEntry(), courseNode.getIdent(), getReferencedRepositoryEntry());
		//sort by identity, then by creation date
		Collections.sort(sessions, new AssessmentTestSessionComparator());

		Long currentIdentityKey = null;
		for(AssessmentTestSession session:sessions) {
			Long identityKey = session.getIdentity().getKey();
			if(currentIdentityKey == null || !currentIdentityKey.equals(identityKey)) {
				Date end = null;
				Date start = null;
				Double completion = null;
				Integer extraTimeInSeconds = session.getExtraTime();
				if(session.getFinishTime() == null && session.getTerminationTime() == null) {
					start = session.getCreationDate();
					if(session.getNumOfQuestions() != null && session.getNumOfQuestions().intValue() > 0 && session.getNumOfAnsweredQuestions() != null) {
						completion = session.getNumOfAnsweredQuestions().doubleValue() / session.getNumOfQuestions().doubleValue();
					}
				} else {
					end = session.getFinishTime() == null ? session.getTerminationTime() : session.getFinishTime();
				}
				
				ExtraInfos infos = new ExtraInfos(extraTimeInSeconds, start, end, completion, session.getMaxScore());
				identityToExtraTime.put(identityKey, infos);
				currentIdentityKey = identityKey;
			}
		}

		List<DisadvantageCompensation> compensations = disadvantageCompensationService
				.getActiveDisadvantageCompensations(courseEntry, courseNode.getIdent());
		for(DisadvantageCompensation compensation:compensations) {
			Long identityKey = compensation.getIdentity().getKey();
			Integer extraTimeInSeconds = compensation.getExtraTime();
			ExtraInfos infos = identityToExtraTime.computeIfAbsent(identityKey,
					key -> new ExtraInfos());
			infos.setCompensationExtraTimeInSeconds(extraTimeInSeconds);
		}

		return identityToExtraTime;	
	}

	@Override
	public void activate(UserRequest ureq, List<ContextEntry> entries, StateEntry state) {
		super.activate(ureq, entries, state);
		
		if(entries != null && !entries.isEmpty()) {
			ContextEntry entry = entries.get(0);
			String resourceType = entry.getOLATResourceable().getResourceableTypeName();
			if(ORES_TYPE_EXPORT_RESULTS.equalsIgnoreCase(resourceType)) {
				doExportResults(ureq);
			}
		}
	}

	@Override
	public void event(UserRequest ureq, Component source, Event event) {
		if(stackPanel == source) {
			if(event instanceof PopEvent pe) {
				if(modelDirty && pe.getController() == correctionIdentitiesCtrl) {
					reload(ureq);
				}
			}
		}
		super.event(ureq, source, event);
	}

	@Override
	public void event(UserRequest ureq, Controller source, Event event) {
		if(validationCtrl == source) {
			cmc.deactivate();
			cleanUp();
		} else if(retrieveConfirmationCtr == source || resetDataCtrl == source || extraTimeCtrl == source) {
			if(event == Event.DONE_EVENT || event == Event.CHANGED_EVENT) {
				reload(ureq);
			}
			cmc.deactivate();
			cleanUp();
		} else if(correctionIdentitiesCtrl == source) {
			if(event == Event.CANCELLED_EVENT) {
				stackPanel.popController(correctionIdentitiesCtrl);
				reload(ureq);
				fireEvent(ureq, Event.CHANGED_EVENT);
			} else if(event instanceof CompleteAssessmentTestSessionEvent catse) {
				doUpdateCourseNode(catse.getTestSessions(), catse.getAssessmentTest(), catse.getStatus());
				reload(ureq);
				stackPanel.popController(correctionIdentitiesCtrl);
				fireEvent(ureq, Event.CHANGED_EVENT);
			} else if(event == Event.CHANGED_EVENT) {
				modelDirty = true;
				fireEvent(ureq, Event.CHANGED_EVENT);
			}
		} else if(source instanceof QTI21IdentityListCourseNodeToolsController) {
			if(event instanceof CompleteAssessmentTestSessionEvent catse) {
				doUpdateCourseNode(catse.getTestSessions(), catse.getAssessmentTest(), catse.getStatus());
				reload(ureq);
				fireEvent(ureq, Event.CHANGED_EVENT);
			} else if(event == Event.DONE_EVENT) {
				reload(ureq);
			}
		} else if(reopenForCorrectionCtrl == source) {
			CorrectionOverviewController correctionCtrl = (CorrectionOverviewController)reopenForCorrectionCtrl.getUserObject();
			cmc.deactivate();
			cleanUp();
			if(event == Event.CHANGED_EVENT) {
				doReopenAssessmentEntries(correctionCtrl);
				doOpenCorrection(correctionCtrl);
			} else if(event == Event.DONE_EVENT) {
				doOpenCorrection(correctionCtrl);
			}
		}
		super.event(ureq, source, event);
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(extraTimeButton == source) {
			doConfirmExtraTime(ureq);
		} else if(exportResultsButton == source) {
			doExportResults(ureq);
		} else if(bulkExportResultsButton == source) {
			doBulkExportResults(ureq);
		} else if(statsButton == source) {
			doLaunchStatistics(ureq);
		}  else if(bulkStatsButton == source) {
			doLaunchBulkStatistics(ureq);
		} else if(validateButton == source) {
			doValidateSignature(ureq);
		} else if(correctionButton == source) {
			doStartCorrection(ureq);
		} else if(bulkCorrectionButton == source) {
			doStartBulkCorrection(ureq);
		} else if(pullButton == source) {
			doConfirmPull(ureq);
		} else if(resetButton == source) {
			doConfirmResetData(ureq);
		} else {
			super.formInnerEvent(ureq, source, event);
		}
	}
	
	@Override
	protected void cleanUp() {
		removeAsListenerAndDispose(reopenForCorrectionCtrl);
		removeAsListenerAndDispose(retrieveConfirmationCtr);
		removeAsListenerAndDispose(validationCtrl);
		removeAsListenerAndDispose(extraTimeCtrl);
		removeAsListenerAndDispose(resetDataCtrl);
		reopenForCorrectionCtrl = null;
		retrieveConfirmationCtr = null;
		validationCtrl = null;
		extraTimeCtrl = null;
		resetDataCtrl = null;
		super.cleanUp();
	}
	
	@Override
	protected Controller createCalloutController(UserRequest ureq, Identity assessedIdentity) {
		if(isTestQTI21()) {
			return new QTI21IdentityListCourseNodeToolsController(ureq, getWindowControl(), stackPanel,
					(IQTESTCourseNode)courseNode, assessedIdentity, coachCourseEnv);
		}
		return new IdentityListCourseNodeToolsController(ureq, getWindowControl(),
				courseNode, assessedIdentity, coachCourseEnv);
	}
	
	private void doExportResults(UserRequest ureq) {
		IdentitiesList identities = getIdentities(true);
		doExportResults(ureq, identities);
	}

	private void doBulkExportResults(UserRequest ureq) {
		List<Identity> identities = getSelectedIdentities(row -> true);
		doExportResults(ureq, new IdentitiesList(identities, null, false, false));
	}
	
	private void doExportResults(UserRequest ureq, IdentitiesList identities) {
		if (!identities.isEmpty()) {
			CourseEnvironment courseEnv = getCourseEnvironment();

			WindowControl swControl = addToHistory(ureq, OresHelper.createOLATResourceableType(ORES_TYPE_EXPORT_RESULTS), null);
			exportResultsCtrl = new QTI21ExportResultsController(ureq, swControl,
					courseEnv, (IQTESTCourseNode)courseNode, identities, getAssessmentCallback());
			listenTo(exportResultsCtrl);
			stackPanel.pushController(translate("export.results.crumb"), exportResultsCtrl);
		} else {
			showWarning("error.no.assessed.users");
		}
	}
	
	private void doValidateSignature(UserRequest ureq) {
		if(guardModalController(validationCtrl)) return;
		
		validationCtrl = new ValidationXmlSignatureController(ureq, getWindowControl());
		listenTo(validationCtrl);
		cmc = new CloseableModalController(getWindowControl(), "close", validationCtrl.getInitialComponent(),
				true, translate("validate.xml.signature"));
		cmc.activate();
		listenTo(cmc);
	}
	
	private void doStartCorrection(UserRequest ureq) {
		AssessmentToolOptions asOptions = getOptions();
		doStartCorrection(ureq, asOptions);
	}
	
	private void doStartBulkCorrection(UserRequest ureq) {
		AssessmentToolOptions asOptions = new AssessmentToolOptions();
		asOptions.setIdentities(getSelectedIdentities(row -> true));
		doStartCorrection(ureq, asOptions);
	}
	
	private void doStartCorrection(UserRequest ureq, AssessmentToolOptions asOptions) {
		CorrectionOverviewController correctionCtrl = new CorrectionOverviewController(ureq, getWindowControl(), stackPanel,
				getCourseEnvironment(), asOptions, (IQTESTCourseNode)courseNode);
		
		final Set<Long> selectedIdentityKeys;
		if(asOptions.getIdentities() != null) {
			selectedIdentityKeys = asOptions.getIdentities().stream()
					.map(Identity::getKey)
					.collect(Collectors.toSet());
		} else {
			selectedIdentityKeys = null;
		}
		
		long numOfAssessmentEntriesDone = usersTableModel.getObjects().stream()
				.filter(row -> row.getAssessmentStatus() == AssessmentEntryStatus.done)
				.filter(row -> selectedIdentityKeys == null || selectedIdentityKeys.contains(row.getIdentityKey()))
				.count();
		if(correctionCtrl.getNumOfAssessmentTestSessions() == 0) {
			showWarning("grade.nobody");
		} else if(numOfAssessmentEntriesDone > 0) {
			doReopenForCorrection(ureq, correctionCtrl, numOfAssessmentEntriesDone);
		} else {
			doOpenCorrection(correctionCtrl);
		}
	}
	
	private void doOpenCorrection(CorrectionOverviewController correctionCtrl) {
		correctionIdentitiesCtrl = correctionCtrl;
		listenTo(correctionIdentitiesCtrl);
		stackPanel.pushController(translate("correction.test.title"), correctionIdentitiesCtrl);
	}
	
	private void doReopenForCorrection(UserRequest ureq, CorrectionOverviewController correctionCtrl, long numOfAssessmentEntriesDone) {
		if(guardModalController(reopenForCorrectionCtrl)) return;
		
		reopenForCorrectionCtrl = new ConfirmReopenAssessmentEntriesController(ureq, getWindowControl(), numOfAssessmentEntriesDone);
		reopenForCorrectionCtrl.setUserObject(correctionCtrl);
		listenTo(reopenForCorrectionCtrl);
		
		cmc = new CloseableModalController(getWindowControl(), "close", reopenForCorrectionCtrl.getInitialComponent(),
				true, translate("reopen.assessments.title"));
		cmc.activate();
		listenTo(cmc);
	}
	
	private void doReopenAssessmentEntries(CorrectionOverviewController correctionCtrl) {
		List<Identity> assessedIdentities = correctionCtrl.getAssessedIdentities();
		Set<Long> assessedIdentitiesKeys = assessedIdentities.stream()
				.map(Identity::getKey)
				.collect(Collectors.toSet());
		List<AssessedIdentityElementRow> rows = usersTableModel.getObjects();
		ICourse course = CourseFactory.loadCourse(courseEntry);
		for(AssessedIdentityElementRow row:rows) {
			if(row.getAssessmentStatus() == AssessmentEntryStatus.done && assessedIdentitiesKeys.contains(row.getIdentityKey())) {
				Identity assessedIdentity = securityManager.loadIdentityByKey(row.getIdentityKey());
				doSetStatus(assessedIdentity, AssessmentEntryStatus.inReview, courseNode, course);
				dbInstance.commitAndCloseSession();			}
		}
	}
	
	private void doUpdateCourseNode(List<AssessmentTestSession> testSessionsToComplete, AssessmentTest assessmentTest, AssessmentEntryStatus status) {
		if(testSessionsToComplete == null || testSessionsToComplete.isEmpty()) return;
		
		Double cutValue = QtiNodesExtractor.extractCutValue(assessmentTest);
		
		CourseEnvironment courseEnv = getCourseEnvironment();
		RepositoryEntry testEntry = getReferencedRepositoryEntry();
		AssessmentConfig assessmentConfig = courseAssessmentService.getAssessmentConfig(courseEntry, courseNode);
		
		NavigableSet<GradeScoreRange> gradeScoreRanges = null;
		
		boolean canChangeUserVisibility = coachCourseEnv.isAdmin()
				|| coachCourseEnv.getCourseEnvironment().getRunStructure().getRootNode().getModuleConfiguration().getBooleanSafe(STCourseNode.CONFIG_COACH_USER_VISIBILITY);
		boolean userVisibleAfter = canChangeUserVisibility
				? ((IQTESTCourseNode)courseNode).isScoreVisibleAfterCorrection()
				: false;
		
		for(AssessmentTestSession testSession:testSessionsToComplete) {
			UserCourseEnvironment assessedUserCourseEnv = AssessmentHelper
					.createAndInitUserCourseEnvironment(testSession.getIdentity(), courseEnv);
			AssessmentEvaluation scoreEval = courseAssessmentService.getAssessmentEvaluation(courseNode, assessedUserCourseEnv);
			
			BigDecimal finalScore = testSession.getFinalScore();
			Float score = finalScore == null ? null : finalScore.floatValue();
			String grade = scoreEval.getGrade();
			String gradeSystemIdent = scoreEval.getGradeSystemIdent();
			String performanceClassIdent = scoreEval.getPerformanceClassIdent();
			Boolean passed = scoreEval.getPassed();
			if(testSession.getManualScore() != null && finalScore != null) {
				if (assessmentConfig.hasGrade() && gradeModule.isEnabled()) {
					if (gradeScoreRanges == null) {
						GradeScale gradeScale = gradeService.getGradeScale(
								courseEnv.getCourseGroupManager().getCourseEntry(), courseNode.getIdent());
						gradeScoreRanges = gradeService.getGradeScoreRanges(gradeScale, getLocale());
					}
					if (assessmentConfig.isAutoGrade() || StringHelper.containsNonWhitespace(scoreEval.getGrade())) {
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
			AssessmentEntryStatus finalStatus = status == null ? scoreEval.getAssessmentStatus() : status;
			Boolean userVisible = scoreEval.getUserVisible();
			if(userVisible == null && finalStatus == AssessmentEntryStatus.done) {
				userVisible = Boolean.valueOf(userVisibleAfter);
			}
			ScoreEvaluation manualScoreEval = new ScoreEvaluation(score, grade, gradeSystemIdent, performanceClassIdent,
					passed, finalStatus, userVisible, scoreEval.getCurrentRunStartDate(),
					scoreEval.getCurrentRunCompletion(), scoreEval.getCurrentRunStatus(), testSession.getKey());
			courseAssessmentService.updateScoreEvaluation(courseNode, manualScoreEval, assessedUserCourseEnv,
					getIdentity(), false, Role.coach);
			
			if(assessmentConfig.isExternalGrading() && status == AssessmentEntryStatus.done) {
				AssessmentEntry assessmentEntry = courseAssessmentService.getAssessmentEntry(courseNode, assessedUserCourseEnv);
				GradingAssignment assignment = gradingService.getGradingAssignment(testEntry, assessmentEntry);
				if(assignment != null) {
					Long metadataTime = qtiService.getMetadataCorrectionTimeInSeconds(testEntry, testSession);
					gradingService.assignmentDone(assignment, metadataTime, userVisible);
				}
			}
		}
	}
	
	private void doConfirmPull(UserRequest ureq) {
		AssessmentToolOptions asOptions = getOptions();
		RepositoryEntry testEntry = getReferencedRepositoryEntry();
		CourseEnvironment courseEnv = getCourseEnvironment();
		if(ImsQTI21Resource.TYPE_NAME.equals(testEntry.getOlatResource().getResourceableTypeName())) {
			retrieveConfirmationCtr = new QTI21RetrieveTestsController(ureq, getWindowControl(),
					courseEnv, asOptions, (IQTESTCourseNode)courseNode);
			listenTo(retrieveConfirmationCtr);
			
			String title = translate("tool.pull");
			cmc = new CloseableModalController(getWindowControl(), null, retrieveConfirmationCtr.getInitialComponent(), true, title, true);
			listenTo(cmc);
			cmc.activate();
		} else {
			showWarning("error.qti12");
		}
	}
	
	private void doConfirmResetData(UserRequest ureq) {
		AssessmentToolOptions asOptions = getOptions();
		CourseEnvironment courseEnv = getCourseEnvironment();
		resetDataCtrl = new QTI21ResetDataController(ureq, getWindowControl(), courseEnv, asOptions, (IQTESTCourseNode)courseNode);
		listenTo(resetDataCtrl);
		
		String title = translate("reset.test.data.title");
		cmc = new CloseableModalController(getWindowControl(), null, resetDataCtrl.getInitialComponent(), true, title, true);
		listenTo(cmc);
		cmc.activate();
	}
	
	private void doLaunchStatistics(UserRequest ureq) {
		RepositoryEntry testEntry = getReferencedRepositoryEntry();
		if(ImsQTI21Resource.TYPE_NAME.equals(testEntry.getOlatResource().getResourceableTypeName())) {
			Controller statisticsCtrl = new QTI21StatisticsToolController(ureq, getWindowControl(), 
					stackPanel, getCourseEnvironment(), getOptions(), (IQTESTCourseNode)courseNode);
			listenTo(statisticsCtrl);
			stackPanel.pushController(translate("button.stats"), statisticsCtrl);
		} else {
			showWarning("error.qti12");
		}
	}
	
	private void doLaunchBulkStatistics(UserRequest ureq) {
		RepositoryEntry testEntry = getReferencedRepositoryEntry();
		if(ImsQTI21Resource.TYPE_NAME.equals(testEntry.getOlatResource().getResourceableTypeName())) {
			AssessmentToolOptions options = new AssessmentToolOptions();
			List<Identity> selectedIdentities = getSelectedIdentities(row -> true);
			options.setIdentities(selectedIdentities);
			Controller statisticsCtrl = new QTI21StatisticsToolController(ureq, getWindowControl(), 
					stackPanel, getCourseEnvironment(), options, (IQTESTCourseNode)courseNode);
			listenTo(statisticsCtrl);
			stackPanel.pushController(translate("button.stats"), statisticsCtrl);
		} else {
			showWarning("error.qti12");
		}
	}
	
	private void doConfirmExtraTime(UserRequest ureq) {
		Predicate<AssessedIdentityElementRow> filter = row -> row.getAssessmentStatus() != AssessmentEntryStatus.done;
		List<IdentityRef> identities = getSelectedIdentitiesRef(filter);
		if(identities == null || identities.isEmpty()) {
			showWarning("warning.users.extra.time");
			return;
		}

		List<AssessmentTestSession> testSessions = new ArrayList<>(identities.size());
		for(IdentityRef identity:identities) {
			List<AssessmentTestSessionStatistics> sessionsStatistics = qtiService
					.getAssessmentTestSessionsStatistics(getCourseRepositoryEntry(), courseNode.getIdent(), identity, true);
			if(!sessionsStatistics.isEmpty()) {
				if(sessionsStatistics.size() > 1) {
					Collections.sort(sessionsStatistics, new AssessmentTestSessionDetailsComparator());
				}
				AssessmentTestSession lastSession = sessionsStatistics.get(0).getTestSession();
				if(lastSession != null && lastSession.getFinishTime() == null) {
					testSessions.add(lastSession);
				}
			}
		}
		
		if(testSessions == null || testSessions.isEmpty()) {
			showWarning("warning.users.extra.time");
			return;
		}
		
		extraTimeCtrl = new ConfirmExtraTimeController(ureq, getWindowControl(), getCourseRepositoryEntry(), testSessions);
		listenTo(extraTimeCtrl);

		String title = translate("extra.time");
		cmc = new CloseableModalController(getWindowControl(), null, extraTimeCtrl.getInitialComponent(), true, title, true);
		listenTo(cmc);
		cmc.activate();
	}
	
	/**
	 * Sort by identity
	 * Initial date: 20 déc. 2017<br>
	 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
	 *
	 */
	private static final class AssessmentTestSessionComparator implements Comparator<AssessmentTestSession> {

		@Override
		public int compare(AssessmentTestSession session1, AssessmentTestSession session2) {
			Long id1 = session1.getIdentity().getKey();
			Long id2 = session2.getIdentity().getKey();
			
			int c = id1.compareTo(id2);
			if(c == 0) {
				Date start1 = session1.getCreationDate();
				Date start2 = session2.getCreationDate();
				c = start2.compareTo(start1);
			}
			return c;
		}
	}
}