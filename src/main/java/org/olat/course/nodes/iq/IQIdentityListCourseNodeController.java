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

import org.olat.basesecurity.GroupRoles;
import org.olat.basesecurity.IdentityRef;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.stack.TooledStackedPanel;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.gui.media.MediaResource;
import org.olat.core.id.Identity;
import org.olat.course.archiver.ScoreAccountingHelper;
import org.olat.course.assessment.AssessmentHelper;
import org.olat.course.assessment.ui.tool.IdentityListCourseNodeController;
import org.olat.course.assessment.ui.tool.IdentityListCourseNodeTableModel.IdentityCourseElementCols;
import org.olat.course.assessment.ui.tool.IdentityListCourseNodeToolsController;
import org.olat.course.nodes.IQTESTCourseNode;
import org.olat.course.nodes.iq.QTI21IdentityListCourseNodeToolsController.AssessmentTestSessionDetailsComparator;
import org.olat.course.run.environment.CourseEnvironment;
import org.olat.course.run.scoring.ScoreEvaluation;
import org.olat.course.run.userview.UserCourseEnvironment;
import org.olat.fileresource.types.ImsQTI21Resource;
import org.olat.group.BusinessGroup;
import org.olat.group.BusinessGroupService;
import org.olat.ims.qti.resultexport.QTI12ResultsExportMediaResource;
import org.olat.ims.qti.statistics.ui.QTI12PullTestsToolController;
import org.olat.ims.qti.statistics.ui.QTI12StatisticsToolController;
import org.olat.ims.qti21.AssessmentTestSession;
import org.olat.ims.qti21.QTI21DeliveryOptions;
import org.olat.ims.qti21.QTI21Service;
import org.olat.ims.qti21.model.jpa.AssessmentTestSessionStatistics;
import org.olat.ims.qti21.model.xml.QtiNodesExtractor;
import org.olat.ims.qti21.resultexport.QTI21ResultsExportMediaResource;
import org.olat.ims.qti21.ui.QTI21ResetDataController;
import org.olat.ims.qti21.ui.QTI21RetrieveTestsController;
import org.olat.ims.qti21.ui.assessment.CorrectionOverviewController;
import org.olat.ims.qti21.ui.assessment.ValidationXmlSignatureController;
import org.olat.ims.qti21.ui.statistics.QTI21StatisticsToolController;
import org.olat.modules.ModuleConfiguration;
import org.olat.modules.assessment.AssessmentToolOptions;
import org.olat.modules.assessment.Role;
import org.olat.modules.assessment.model.AssessmentEntryStatus;
import org.olat.modules.assessment.ui.AssessedIdentityElementRow;
import org.olat.modules.assessment.ui.AssessmentToolContainer;
import org.olat.modules.assessment.ui.AssessmentToolSecurityCallback;
import org.olat.modules.assessment.ui.event.CompleteAssessmentTestSessionEvent;
import org.olat.repository.RepositoryEntry;
import org.springframework.beans.factory.annotation.Autowired;

import de.bps.onyx.plugin.OnyxModule;
import uk.ac.ed.ph.jqtiplus.node.test.AssessmentTest;

/**
 * 
 * Initial date: 18 déc. 2017<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class IQIdentityListCourseNodeController extends IdentityListCourseNodeController {
	
	private FormLink extraTimeButton;
	private FormLink exportResultsButton;
	private FormLink statsButton;
	private FormLink validateButton;
	private FormLink correctionButton;
	private FormLink pullButton;
	private FormLink resetButton;

	private Controller retrieveConfirmationCtr;
	private QTI21ResetDataController resetDataCtrl;
	private ConfirmExtraTimeController extraTimeCtrl;
	private ValidationXmlSignatureController validationCtrl;
	private CorrectionOverviewController correctionIdentitiesCtrl;

	@Autowired
	private QTI21Service qtiService;
	@Autowired
	private BusinessGroupService groupService;

	
	public IQIdentityListCourseNodeController(UserRequest ureq, WindowControl wControl, TooledStackedPanel stackPanel,
			RepositoryEntry courseEntry, BusinessGroup group, IQTESTCourseNode courseNode, UserCourseEnvironment coachCourseEnv,
			AssessmentToolContainer toolContainer, AssessmentToolSecurityCallback assessmentCallback) {
		super(ureq, wControl, stackPanel, courseEntry, group, courseNode, coachCourseEnv, toolContainer, assessmentCallback);
	}
	
	@Override
	protected String getTableId() {
		if(isTestQTI21()) {
			return"qti21-assessment-tool-identity-list";
		}
		return "qti-assessment-tool-identity-list";
	}

	@Override
	protected void initStatusColumns(FlexiTableColumnModel columnsModel) {
		super.initStatusColumns(columnsModel);
		IQTESTCourseNode testCourseNode = (IQTESTCourseNode)courseNode;
		if(testCourseNode != null && testCourseNode.hasCompletion()) {
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(IdentityCourseElementCols.currentCompletion));
		}
		
		RepositoryEntry qtiTestEntry = getReferencedRepositoryEntry();
		if(testCourseNode != null && testCourseNode.hasQTI21TimeLimit(qtiTestEntry)) {
			int timeLimitInSeconds = testCourseNode.getQTI21TimeLimitMaxInSeconds(qtiTestEntry);
			boolean suspendEnabled = isSuspendEnable();
			FlexiCellRenderer renderer = new ExtraTimeCellRenderer(!suspendEnabled, timeLimitInSeconds, getLocale());
			String header = suspendEnabled ? "table.header.extra.time" : "table.header.end.date";
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(header, IdentityCourseElementCols.details.ordinal(), renderer));
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
		//bulk
		super.initMultiSelectionTools(ureq, formLayout);
		
		RepositoryEntry testEntry = getReferencedRepositoryEntry();
		if(((IQTESTCourseNode)courseNode).hasQTI21TimeLimit(testEntry)) {
			extraTimeButton = uifactory.addFormLink("extra.time", formLayout, Link.BUTTON);
			extraTimeButton.setIconLeftCSS("o_icon o_icon_extra_time");
		}
		boolean qti21 = isTestQTI21();
		boolean onyx = !qti21 && OnyxModule.isOnyxTest(testEntry.getOlatResource());
		
		statsButton = uifactory.addFormLink("button.stats", formLayout, Link.BUTTON);
		statsButton.setIconLeftCSS("o_icon o_icon-fw o_icon_statistics_tool");
		
		if(!coachCourseEnv.isCourseReadOnly()) {
			if(!onyx) {
				pullButton = uifactory.addFormLink("retrieve.tests.title", formLayout, Link.BUTTON);
				pullButton.setIconLeftCSS("o_icon o_icon_pull");
			}

			if(qti21) {
				if(assessmentCallback.isAdmin()) {
					resetButton = uifactory.addFormLink("tool.delete.data", formLayout, Link.BUTTON); 
					resetButton.setIconLeftCSS("o_icon o_icon_delete_item");
				}
				if(qtiService.needManualCorrection(testEntry)
						|| IQEditController.CORRECTION_MANUAL.equals(courseNode.getModuleConfiguration().getStringValue(IQEditController.CONFIG_CORRECTION_MODE))) {
					correctionButton = uifactory.addFormLink("correction.test.title", formLayout, Link.BUTTON);
					correctionButton.setElementCssClass("o_sel_correction");
					correctionButton.setIconLeftCSS("o_icon o_icon-fw o_icon_correction");
				}
				if(courseNode.getModuleConfiguration().getBooleanSafe(IQEditController.CONFIG_DIGITAL_SIGNATURE, false)) {
					validateButton = uifactory.addFormLink("validate.xml.signature", formLayout, Link.BUTTON);
					validateButton.setIconLeftCSS("o_icon o_icon-fw o_icon_correction");
				}
			}
		}
		
		if(!onyx) {
			exportResultsButton = uifactory.addFormLink("button.export", formLayout, Link.BUTTON);
			exportResultsButton.setIconLeftCSS("o_icon o_icon-fw o_icon_export");
		}
	}
	
	private boolean isTestRunning() {
		List<Identity> identities = getIdentities();
		if(isTestQTI21()) {
			return qtiService.isRunningAssessmentTestSession(getCourseRepositoryEntry(),
					courseNode.getIdent(), getReferencedRepositoryEntry(), identities);
		}

		for(Identity assessedIdentity:identities) {
			if(((IQTESTCourseNode)courseNode).isQTI12TestRunning(assessedIdentity, getCourseEnvironment())) {
				return true;
			}
		}
		return false;
	}
	
	private boolean isTestQTI21() {
		return ImsQTI21Resource.TYPE_NAME.equals(getReferencedRepositoryEntry().getOlatResource().getResourceableTypeName());
	}

	@Override
	protected void loadModel(UserRequest ureq) {
		super.loadModel(ureq);
		
		if(((IQTESTCourseNode)courseNode).hasQTI21TimeLimit(getReferencedRepositoryEntry())) {
			Map<Long,ExtraTimeInfos> extraTimeInfos = getExtraTimes();
			List<AssessedIdentityElementRow> rows = usersTableModel.getObjects();
			for(AssessedIdentityElementRow row:rows) {
				row.setDetails(extraTimeInfos.get(row.getIdentityKey()));
			}
		}
		
		if(pullButton != null) {
			boolean enabled = isTestRunning();
			pullButton.setEnabled(enabled);
		}
	}
	
	/**
	 * @return A map identity key to extra time
	 */
	private Map<Long,ExtraTimeInfos> getExtraTimes() {
		Map<Long,ExtraTimeInfos> identityToExtraTime = new HashMap<>();
		List<AssessmentTestSession> sessions = qtiService
				.getAssessmentTestSessions(getCourseRepositoryEntry(), courseNode.getIdent(), getReferencedRepositoryEntry());
		//sort by identity, then by creation date
		Collections.sort(sessions, new AssessmentTestSessionComparator());
		
		Long currentIdentityKey = null;
		for(AssessmentTestSession session:sessions) {
			Long identityKey = session.getIdentity().getKey();
			if(currentIdentityKey == null || !currentIdentityKey.equals(identityKey)) {
				if(session.getFinishTime() == null && session.getExtraTime() != null) {
					Integer extraTimeInSeconds = session.getExtraTime();
					Date start = session.getCreationDate();
					ExtraTimeInfos infos = new ExtraTimeInfos(extraTimeInSeconds, start);
					identityToExtraTime.put(identityKey, infos);
				}
				currentIdentityKey = identityKey;
			}
		}
		return identityToExtraTime;	
	}
	

	@Override
	public void event(UserRequest ureq, Controller source, Event event) {
		if(validationCtrl == source) {
			cmc.deactivate();
			cleanUp();
		} else if(retrieveConfirmationCtr == source || resetDataCtrl == source || extraTimeCtrl == source) {
			if(event == Event.DONE_EVENT || event == Event.CHANGED_EVENT) {
				loadModel(ureq);
			}
			cmc.deactivate();
			cleanUp();
		} else if(correctionIdentitiesCtrl == source) {
			if(event == Event.CANCELLED_EVENT) {
				stackPanel.popController(correctionIdentitiesCtrl);
				loadModel(ureq);				
				fireEvent(ureq, Event.CHANGED_EVENT);
			} else if(event instanceof CompleteAssessmentTestSessionEvent) {
				CompleteAssessmentTestSessionEvent catse = (CompleteAssessmentTestSessionEvent)event;
				doUpdateCourseNode(catse.getTestSessions(), catse.getAssessmentTest(), catse.getStatus());
				loadModel(ureq);	
				stackPanel.popController(correctionIdentitiesCtrl);		
				fireEvent(ureq, Event.CHANGED_EVENT);
			}
		} else if(source instanceof QTI21IdentityListCourseNodeToolsController) {
			if(event instanceof CompleteAssessmentTestSessionEvent) {
				CompleteAssessmentTestSessionEvent catse = (CompleteAssessmentTestSessionEvent)event;
				doUpdateCourseNode(catse.getTestSessions(), catse.getAssessmentTest(), catse.getStatus());
				loadModel(ureq);	
				fireEvent(ureq, Event.CHANGED_EVENT);
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
		} else if(statsButton == source) {
			doLaunchStatistics(ureq);
		} else if(validateButton == source) {
			doValidateSignature(ureq);
		} else if(correctionButton == source) {
			doStartCorrection(ureq);
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
		removeAsListenerAndDispose(retrieveConfirmationCtr);
		removeAsListenerAndDispose(validationCtrl);
		removeAsListenerAndDispose(extraTimeCtrl);
		removeAsListenerAndDispose(resetDataCtrl);
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
		return  new IdentityListCourseNodeToolsController(ureq, getWindowControl(),
				(IQTESTCourseNode)courseNode, assessedIdentity, coachCourseEnv);
	}
	
	private List<Identity> getIdentities() {
		AssessmentToolOptions asOptions = getOptions();
		List<Identity> identities = asOptions.getIdentities();
		if (group != null) {
			identities = groupService.getMembers(group, GroupRoles.participant.toString());
		} else if (identities != null) {
			identities = asOptions.getIdentities();			
		} else if (asOptions.isAdmin()){
			identities = ScoreAccountingHelper.loadParticipants(getCourseEnvironment());
		}
		return identities;
	}
	
	private void doExportResults(UserRequest ureq) {
		List<Identity> identities = getIdentities();
		if (identities != null && !identities.isEmpty()) {
			MediaResource resource;
			CourseEnvironment courseEnv = getCourseEnvironment();
			if(isTestQTI21()) {
				resource = new QTI21ResultsExportMediaResource(courseEnv, identities, (IQTESTCourseNode)courseNode, getLocale());
			} else {
				resource = new QTI12ResultsExportMediaResource(courseEnv, getLocale(), identities, (IQTESTCourseNode)courseNode);
			}
			ureq.getDispatchResult().setResultingMediaResource(resource);
		} else {
			showWarning("error.no.assessed.users");
		}
	}
	
	private void doValidateSignature(UserRequest ureq) {
		if(validationCtrl != null) return;
		
		validationCtrl = new ValidationXmlSignatureController(ureq, getWindowControl());
		listenTo(validationCtrl);
		cmc = new CloseableModalController(getWindowControl(), "close", validationCtrl.getInitialComponent(),
				true, translate("validate.xml.signature"));
		cmc.activate();
		listenTo(cmc);
	}
	
	private void doStartCorrection(UserRequest ureq) {
		AssessmentToolOptions asOptions = getOptions();

		correctionIdentitiesCtrl = new CorrectionOverviewController(ureq, getWindowControl(), stackPanel,
				getCourseEnvironment(), asOptions, (IQTESTCourseNode)courseNode);
		if(correctionIdentitiesCtrl.getNumberOfAssessedIdentities() == 0) {
			showWarning("grade.nobody");
			correctionIdentitiesCtrl = null;
		} else {
			listenTo(correctionIdentitiesCtrl);
			stackPanel.pushController(translate("correction.test.title"), correctionIdentitiesCtrl);
		}
	}
	
	private void doUpdateCourseNode(List<AssessmentTestSession> testSessionsToComplete, AssessmentTest assessmentTest, AssessmentEntryStatus status) {
		if(testSessionsToComplete == null || testSessionsToComplete.isEmpty()) return;
		
		Double cutValue = QtiNodesExtractor.extractCutValue(assessmentTest);
		
		for(AssessmentTestSession testSession:testSessionsToComplete) {
			UserCourseEnvironment assessedUserCourseEnv = AssessmentHelper
					.createAndInitUserCourseEnvironment(testSession.getIdentity(), getCourseEnvironment());
			ScoreEvaluation scoreEval = ((IQTESTCourseNode)courseNode).getUserScoreEvaluation(assessedUserCourseEnv);
			
			BigDecimal finalScore = testSession.getFinalScore();
			Float score = finalScore == null ? null : finalScore.floatValue();
			Boolean passed = scoreEval.getPassed();
			if(testSession.getManualScore() != null && finalScore != null && cutValue != null) {
				boolean calculated = finalScore.compareTo(BigDecimal.valueOf(cutValue.doubleValue())) >= 0;
				passed = Boolean.valueOf(calculated);
			}
			AssessmentEntryStatus finalStatus = status == null ? scoreEval.getAssessmentStatus() : status;
			ScoreEvaluation manualScoreEval = new ScoreEvaluation(score, passed,
					finalStatus, scoreEval.getUserVisible(), scoreEval.getFullyAssessed(),
					scoreEval.getCurrentRunCompletion(), scoreEval.getCurrentRunStatus(), testSession.getKey());
			((IQTESTCourseNode)courseNode).updateUserScoreEvaluation(manualScoreEval, assessedUserCourseEnv, getIdentity(), false, Role.coach);
		}
	}
	
	private void doConfirmPull(UserRequest ureq) {
		AssessmentToolOptions asOptions = getOptions();
		RepositoryEntry testEntry = getReferencedRepositoryEntry();
		CourseEnvironment courseEnv = getCourseEnvironment();
		if(ImsQTI21Resource.TYPE_NAME.equals(testEntry.getOlatResource().getResourceableTypeName())) {
			retrieveConfirmationCtr = new QTI21RetrieveTestsController(ureq, getWindowControl(),
					courseEnv, asOptions, (IQTESTCourseNode)courseNode);
		} else {
			retrieveConfirmationCtr = new QTI12PullTestsToolController(ureq, getWindowControl(),
					courseEnv, asOptions, (IQTESTCourseNode)courseNode);
		}
		listenTo(retrieveConfirmationCtr);
		
		String title = translate("tool.pull");
		cmc = new CloseableModalController(getWindowControl(), null, retrieveConfirmationCtr.getInitialComponent(), true, title, true);
		listenTo(cmc);
		cmc.activate();
	}
	
	private void doConfirmResetData(UserRequest ureq) {
		AssessmentToolOptions asOptions = getOptions();
		CourseEnvironment courseEnv = getCourseEnvironment();
		resetDataCtrl = new QTI21ResetDataController(ureq, getWindowControl(), courseEnv, asOptions, (IQTESTCourseNode)courseNode);
		listenTo(resetDataCtrl);
		
		String title = translate("tool.reset");
		cmc = new CloseableModalController(getWindowControl(), null, resetDataCtrl.getInitialComponent(), true, title, true);
		listenTo(cmc);
		cmc.activate();
	}
	
	private void doLaunchStatistics(UserRequest ureq) {
		Controller statisticsCtrl;
		RepositoryEntry testEntry = getReferencedRepositoryEntry();
		if(ImsQTI21Resource.TYPE_NAME.equals(testEntry.getOlatResource().getResourceableTypeName())) {
			statisticsCtrl = new QTI21StatisticsToolController(ureq, getWindowControl(), 
					stackPanel, getCourseEnvironment(), getOptions(), (IQTESTCourseNode)courseNode);
		} else {
			statisticsCtrl = new QTI12StatisticsToolController(ureq, getWindowControl(),
					stackPanel, getCourseEnvironment(), getOptions(), (IQTESTCourseNode)courseNode);
		}
		listenTo(statisticsCtrl);
		stackPanel.pushController(translate("button.stats"), statisticsCtrl);
	}
	
	private void doConfirmExtraTime(UserRequest ureq) {
		List<IdentityRef> identities = getSelectedIdentities();
		if(identities == null || identities.isEmpty()) {
			showWarning("warning.users.extra.time");
			return;
		}

		List<AssessmentTestSession> testSessions = new ArrayList<>(identities.size());
		for(IdentityRef identity:identities) {
			List<AssessmentTestSessionStatistics> sessionsStatistics = qtiService
					.getAssessmentTestSessionsStatistics(getCourseRepositoryEntry(), courseNode.getIdent(), identity);
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