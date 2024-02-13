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
import java.net.URI;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.olat.basesecurity.IdentityRef;
import org.olat.core.commons.services.pdf.PdfModule;
import org.olat.core.commons.services.pdf.PdfOutputOptions;
import org.olat.core.commons.services.pdf.PdfService;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.stack.TooledStackedPanel;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.creator.ControllerCreator;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.gui.control.generic.wizard.Step;
import org.olat.core.gui.control.generic.wizard.StepsMainRunController;
import org.olat.core.gui.control.winmgr.Command;
import org.olat.core.gui.control.winmgr.CommandFactory;
import org.olat.core.gui.control.winmgr.functions.FunctionCommand;
import org.olat.core.gui.media.MediaResource;
import org.olat.core.id.Identity;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.course.CourseFactory;
import org.olat.course.ICourse;
import org.olat.course.assessment.AssessmentInspectionService;
import org.olat.course.assessment.ui.inspection.CreateInspectionContext;
import org.olat.course.assessment.ui.inspection.CreateInspectionFinishStepCallback;
import org.olat.course.assessment.ui.inspection.CreateInspection_1a_CreateConfigurationStep;
import org.olat.course.assessment.ui.inspection.CreateInspection_3_InspectionStep;
import org.olat.course.assessment.ui.reset.ConfirmResetDataController;
import org.olat.course.assessment.ui.reset.ResetDataContext;
import org.olat.course.assessment.ui.reset.ResetDataContext.ResetCourse;
import org.olat.course.assessment.ui.reset.ResetDataContext.ResetParticipants;
import org.olat.course.assessment.ui.tool.tools.AbstractToolsController;
import org.olat.course.nodes.CourseNode;
import org.olat.course.nodes.CourseNodeConfiguration;
import org.olat.course.nodes.CourseNodeFactory;
import org.olat.course.nodes.IQTESTCourseNode;
import org.olat.course.run.environment.CourseEnvironment;
import org.olat.course.run.scoring.ResetCourseDataHelper;
import org.olat.course.run.userview.UserCourseEnvironment;
import org.olat.fileresource.FileResourceManager;
import org.olat.ims.qti21.AssessmentTestSession;
import org.olat.ims.qti21.QTI21AssessmentResultsOptions;
import org.olat.ims.qti21.QTI21Service;
import org.olat.ims.qti21.model.jpa.AssessmentTestSessionStatistics;
import org.olat.ims.qti21.model.xml.ManifestBuilder;
import org.olat.ims.qti21.ui.AssessmentResultController;
import org.olat.ims.qti21.ui.AssessmentTestDisplayController;
import org.olat.ims.qti21.ui.AssessmentTestSessionComparator;
import org.olat.ims.qti21.ui.ConfirmReopenAssessmentEntryController;
import org.olat.ims.qti21.ui.QTI21RetrieveTestsController;
import org.olat.ims.qti21.ui.ResourcesMapper;
import org.olat.ims.qti21.ui.assessment.CorrectionIdentityAssessmentItemListController;
import org.olat.ims.qti21.ui.assessment.CorrectionOverviewModel;
import org.olat.instantMessaging.InstantMessageTypeEnum;
import org.olat.instantMessaging.InstantMessagingService;
import org.olat.instantMessaging.OpenInstantMessageEvent;
import org.olat.instantMessaging.model.RosterChannelInfos;
import org.olat.instantMessaging.model.RosterChannelInfos.RosterStatus;
import org.olat.instantMessaging.ui.ChatViewConfig;
import org.olat.instantMessaging.ui.RosterFormDisplay;
import org.olat.modules.assessment.AssessmentEntry;
import org.olat.modules.assessment.Role;
import org.olat.modules.assessment.model.AssessmentEntryStatus;
import org.olat.modules.assessment.ui.AssessmentToolSecurityCallback;
import org.olat.modules.assessment.ui.event.CompleteAssessmentTestSessionEvent;
import org.olat.modules.bigbluebutton.BigBlueButtonModule;
import org.olat.modules.dcompensation.DisadvantageCompensation;
import org.olat.modules.dcompensation.DisadvantageCompensationService;
import org.olat.modules.dcompensation.DisadvantageCompensationStatusEnum;
import org.olat.modules.dcompensation.ui.ConfirmDeleteDisadvantageCompensationController;
import org.olat.modules.teams.TeamsModule;
import org.olat.repository.RepositoryEntry;
import org.olat.user.UserManager;
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
	private Link chatLink;
	private Link resetDataLink;
	private Link inspectionLink;
	private Link exportPdfResultsLink;
	private Link compensationExtraTimeLink;
	private Link removeCompensationExtraTimeLink;
	private TooledStackedPanel stackPanel;
	
	private CloseableModalController cmc;
	private ConfirmReopenController reopenCtrl;
	private ConfirmExtraTimeController extraTimeCtrl;
	private StepsMainRunController newInspectionWizard;
	private ConfirmResetDataController confirmResetDataCtrl;
	private QTI21RetrieveTestsController retrieveConfirmationCtr;
	private CorrectionIdentityAssessmentItemListController correctionCtrl;
	private ConfirmReopenAssessmentEntryController reopenForCorrectionCtrl;
	private ConfirmCompensationExtraTimeController compensationExtraTimeCtrl;
	private ConfirmDeleteDisadvantageCompensationController removeCompensationExtraTimeCtrl;
	
	private RepositoryEntry testEntry;
	private RepositoryEntry courseEntry;
	private IQTESTCourseNode testCourseNode;
	
	private final boolean manualCorrections;
	private AssessmentTestSession lastSession;
	private final AssessmentToolSecurityCallback secCallback;
	private final UserCourseEnvironment coachCourseEnv;
	
	@Autowired
	private PdfModule pdfModule;
	@Autowired
	private PdfService pdfService;
	@Autowired
	private QTI21Service qtiService;
	@Autowired
	private UserManager userManager;
	@Autowired
	private InstantMessagingService imService;
	@Autowired
	private TeamsModule teamsModule;
	@Autowired
	private BigBlueButtonModule bigBlueButtonModule;
	@Autowired
	private AssessmentInspectionService inspectionService;
	@Autowired
	private DisadvantageCompensationService disadvantageCompensationService;
	
	public QTI21IdentityListCourseNodeToolsController(UserRequest ureq, WindowControl wControl, TooledStackedPanel stackPanel,
			IQTESTCourseNode courseNode, Identity assessedIdentity, UserCourseEnvironment coachCourseEnv,
			AssessmentToolSecurityCallback secCallback) {
		super(ureq, wControl, courseNode, assessedIdentity, coachCourseEnv);

		setTranslator(Util.createPackageTranslator(AssessmentTestDisplayController.class, getLocale(), getTranslator()));
		
		this.stackPanel = stackPanel;
		this.secCallback = secCallback;
		this.testCourseNode = courseNode;
		this.coachCourseEnv = coachCourseEnv;
		testEntry = courseNode.getReferencedRepositoryEntry();
		
		String correctionMode = courseNode.getModuleConfiguration().getStringValue(IQEditController.CONFIG_CORRECTION_MODE);
		manualCorrections = !IQEditController.CORRECTION_GRADING.equals(correctionMode)
				&& (IQEditController.CORRECTION_MANUAL.equals(correctionMode) || qtiService.needManualCorrection(testEntry));
		
		courseEntry = coachCourseEnv.getCourseEnvironment().getCourseGroupManager().getCourseEntry();
		
		List<AssessmentTestSessionStatistics> sessionsStatistics = qtiService
				.getAssessmentTestSessionsStatistics(courseEntry, courseNode.getIdent(), assessedIdentity, true);
		if(!sessionsStatistics.isEmpty()) {
			Collections.sort(sessionsStatistics, new AssessmentTestSessionDetailsComparator());
			lastSession = sessionsStatistics.get(0).testSession();
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

		boolean hasTimeLimit = testCourseNode.hasQTI21TimeLimit(testEntry, courseEntry, getIdentity());
		boolean lastSessionActive = (lastSession != null && lastSession.getFinishTime() == null);
		
		if(lastSession != null && !lastSessionActive && pdfModule.isEnabled()) {
			exportPdfResultsLink = addLink("tool.export.pdf.results", "tool.export.pdf.results", "o_icon o_icon-fw o_icon_export");
		}
		if (!coachCourseEnv.isCourseReadOnly()) {
			addSeparator();
		}
		
		if(lastSessionActive && hasTimeLimit) {
			extraTimeLink = addLink("tool.extra.time", "tool.extra.time", "o_icon o_icon-fw o_icon_extra_time");
		}
		if(hasTimeLimit) {
			compensationExtraTimeLink = addLink("tool.extra.time.compensation", "tool.extra.time.compensation", "o_icon o_icon-fw o_icon_disadvantage_compensation");
			if(hasActiveDisadvantageCompensation()) {
				removeCompensationExtraTimeLink = addLink("tool.remove.extra.time.compensation", "tool.remove.extra.time.compensation", "o_icon o_icon-fw o_icon_disadvantage_compensation");
			}
		}
		if(lastSessionActive) {
			pullTestLink = addLink("tool.pull", "tool.pull", "o_icon o_icon-fw o_icon_pull");
			
			RosterChannelInfos channel = imService.getRoster(courseEntry.getOlatResource(), testCourseNode.getIdent(), assessedIdentity.getKey().toString(), getIdentity());
			String i18nKey = "tool.chat.open";
			if(channel == null || channel.getRosterStatus() == RosterStatus.completed || channel.getRosterStatus() == RosterStatus.ended) {
				i18nKey = "tool.chat.new";
			}
			chatLink = addLink(i18nKey, "tool.chat", "o_icon o_icon-fw o_icon_chats");
		}
		
		if (!coachCourseEnv.isCourseReadOnly()) {
			addSeparator();
			inspectionLink = addLink("new.individual.inspection", "new.inspection", "o_icon o_icon-fw o_icon_inspection");
		}
	}
	
	private boolean hasActiveDisadvantageCompensation() {
		List<DisadvantageCompensation> compensations = disadvantageCompensationService
				.getDisadvantageCompensations(assessedIdentity, courseEntry, testCourseNode.getIdent());
		for(DisadvantageCompensation compensation:compensations) {
			if(compensation.getStatusEnum() == DisadvantageCompensationStatusEnum.active) {
				return true;
			}
		}
		return false;
	}

	@Override
	protected void initResetAttempts() {
		//closed test reopen
		if(!coachCourseEnv.isCourseReadOnly()
				&& lastSession != null && (lastSession.getFinishTime() != null || lastSession.getTerminationTime() != null)) {
			reopenLink = addLink("reopen.test", "reopen.test", "o_icon o_icon-fw o_icon_reopen");
		}

		super.initResetAttempts();
		if(!coachCourseEnv.isCourseReadOnly() && lastSession != null) {
			addSeparator();
			//reset data
			resetDataLink = addLink("reset.test.data.title", "tool.reset.data", "o_icon o_icon-fw o_icon_reset_data");
		}
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if(correctionLink == source) {
			fireEvent(ureq, Event.CLOSE_EVENT);
			doCorrection(ureq);
		} else if(pullTestLink == source) {
			fireEvent(ureq, Event.CLOSE_EVENT);
			doConfirmPullSession(ureq, lastSession);
		} else if(resetDataLink == source) {
			fireEvent(ureq, Event.CLOSE_EVENT);
			doConfirmResetData(ureq);
		} else if(extraTimeLink == source) {
			fireEvent(ureq, Event.CLOSE_EVENT);
			doConfirmExtraTime(ureq);
		} else if(compensationExtraTimeLink == source) {
			fireEvent(ureq, Event.CLOSE_EVENT);
			doConfirmCompensationExtraTime(ureq);
		} else if(removeCompensationExtraTimeLink == source) {
			fireEvent(ureq, Event.CLOSE_EVENT);
			doConfirmRemoveCompensationExtraTime(ureq);
		} else if(reopenLink == source) {
			fireEvent(ureq, Event.CLOSE_EVENT);
			doConfirmReopenTest(ureq);
		} else if(exportPdfResultsLink == source) {
			fireEvent(ureq, Event.CLOSE_EVENT);
			doExportResults(ureq);
		} else if(chatLink == source) {
			fireEvent(ureq, Event.CLOSE_EVENT);
			doOpenChat(ureq);
		} else if(inspectionLink == source) {
			fireEvent(ureq, Event.CLOSE_EVENT);
			doNewInspection(ureq);
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
		} else if(extraTimeCtrl == source || reopenCtrl == source || compensationExtraTimeCtrl == source
				|| removeCompensationExtraTimeCtrl == source) {
			cmc.deactivate();
			cleanUp();
			fireAlteredEvent(ureq, event);
		} else if(confirmResetDataCtrl == source) {
			if(event == Event.DONE_EVENT || event == Event.CHANGED_EVENT) {
				doResetData(ureq);
			}
			cmc.deactivate();
			cleanUp();
			if(event == Event.DONE_EVENT || event == Event.CHANGED_EVENT) {
				fireAlteredEvent(ureq, event);
			}
		} else if(reopenForCorrectionCtrl == source) {
			cmc.deactivate();
			cleanUp();
			doOpenCorrection(ureq);
		} else if(cmc == source) {
			cleanUp();
			fireEvent(ureq, Event.CHANGED_EVENT);
		} else if(newInspectionWizard == source) {
			if(event == Event.CANCELLED_EVENT || event == Event.DONE_EVENT || event == Event.CHANGED_EVENT) {
				getWindowControl().pop();
				cleanUp();
			}
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

	@Override
	protected void cleanUp() {
		super.cleanUp();
		removeAsListenerAndDispose(removeCompensationExtraTimeCtrl);
		removeAsListenerAndDispose(compensationExtraTimeCtrl);
		removeAsListenerAndDispose(reopenForCorrectionCtrl);
		removeAsListenerAndDispose(confirmResetDataCtrl);
		removeAsListenerAndDispose(newInspectionWizard);
		removeAsListenerAndDispose(correctionCtrl);
		removeAsListenerAndDispose(extraTimeCtrl);
		removeAsListenerAndDispose(cmc);
		removeCompensationExtraTimeCtrl = null;
		compensationExtraTimeCtrl = null;
		reopenForCorrectionCtrl = null;
		confirmResetDataCtrl = null;
		newInspectionWizard = null;
		correctionCtrl = null;
		extraTimeCtrl = null;
		cmc = null;
	}
	
	private void doExportResults(UserRequest ureq) {
		getWindowControl().getWindowBackOffice().sendCommandTo(FunctionCommand.showInfoMessage("", translate("info.start.download.pdf")));
		
		final File fUnzippedDirRoot = FileResourceManager.getInstance().unzipFileResource(testEntry.getOlatResource());
		final URI assessmentObjectUri = qtiService.createAssessmentTestUri(fUnzippedDirRoot);

		ControllerCreator creator = (uureq, wwControl) -> {
			File submissionDir = qtiService.getSubmissionDirectory(lastSession);
			String mapperUriForPdf = registerCacheableMapper(uureq, "QTI21DetailsResources::" + lastSession.getKey(),
					new ResourcesMapper(assessmentObjectUri, fUnzippedDirRoot, submissionDir));
			AssessmentTestSession candidateSession = qtiService.getAssessmentTestSession(lastSession.getKey());
			AssessmentResultController printViewCtrl = new AssessmentResultController(uureq, wwControl, assessedIdentity, false, candidateSession,
					fUnzippedDirRoot, mapperUriForPdf, null, QTI21AssessmentResultsOptions.allOptions(), false, true, false);
			listenTo(printViewCtrl);
			return printViewCtrl;
		};
		
		String filename = generateDownloadName(lastSession);
		MediaResource pdf = pdfService.convert(filename, getIdentity(), creator,
				getWindowControl(), PdfOutputOptions.defaultOptions());
		Command downloadCmd = CommandFactory.createDownloadMediaResourceAsync(ureq, filename + ".pdf", pdf);
		getWindowControl().getWindowBackOffice().sendCommandTo(downloadCmd);
	}
	
	private String generateDownloadName(AssessmentTestSession session) {
		String filename = "results_";
		if(session.getAnonymousIdentifier() != null) {
			filename += session.getAnonymousIdentifier();
		} else {
			filename += session.getIdentity().getUser().getFirstName()
					+ "_" + session.getIdentity().getUser().getLastName();
		}
		
		filename += "_" + courseEntry.getDisplayname();
		
		String subIdent = session.getSubIdent();
		if(StringHelper.containsNonWhitespace(subIdent)) {
			ICourse course = CourseFactory.loadCourse(courseEntry);
			CourseNode node = course.getRunStructure().getNode(subIdent);
			if(node != null) {
				if(StringHelper.containsNonWhitespace(node.getShortTitle())) {
					filename += "_" + node.getShortTitle();
				} else {
					filename += "_" + node.getLongTitle();
				}
			}
		}
		return filename;
	}
	
	private void doCorrection(UserRequest ureq) {
		boolean assessmentEntryDone = isAssessementEntryDone();
		if(assessmentEntryDone) {
			doReopenForCorrection(ureq);
		} else {
			doOpenCorrection(ureq);
		}	
	}
	
	private void doReopenForCorrection(UserRequest ureq) {
		if(guardModalController(reopenForCorrectionCtrl)) return;
		
		reopenForCorrectionCtrl = new ConfirmReopenAssessmentEntryController(ureq, getWindowControl(),
				assessedUserCourseEnv, (IQTESTCourseNode)courseNode, null);
		listenTo(reopenForCorrectionCtrl);
		
		cmc = new CloseableModalController(getWindowControl(), translate("close"), reopenForCorrectionCtrl.getInitialComponent(),
				true, translate("reopen.assessment.title"));
		cmc.activate();
		listenTo(cmc);
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
				resolvedAssessmentTest, manifestBuilder, lastSessionMap, testSessionStates, getTranslator());
		
		boolean assessmentEntryDone = isAssessementEntryDone();
		boolean running = lastSession.getFinishTime() == null && lastSession.getTerminationTime() == null;
		boolean readOnly = assessmentEntryDone || running;
		correctionCtrl = new CorrectionIdentityAssessmentItemListController(ureq, getWindowControl(), stackPanel, model,
				assessedIdentity, readOnly);
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
	
	private void doConfirmResetData(UserRequest ureq) {
		ResetDataContext dataContext = new ResetDataContext(courseEntry);
		dataContext.setResetCourse(ResetCourse.elements);
		dataContext.setCourseNodes(List.of(courseNode));
		dataContext.setResetParticipants(ResetParticipants.selected);
		dataContext.setSelectedParticipants(List.of(assessedIdentity));
		
		confirmResetDataCtrl = new ConfirmResetDataController(ureq, getWindowControl(), dataContext, secCallback);
		listenTo(confirmResetDataCtrl);
		
		String title = translate("reset.test.data.title", courseNode.getShortTitle());
		cmc = new CloseableModalController(getWindowControl(), null, confirmResetDataCtrl.getInitialComponent(), true, title, true);
		listenTo(cmc);
		cmc.activate();
	}
	
	private void doResetData(UserRequest ureq) {
		CourseEnvironment courseEnv = assessedUserCourseEnv.getCourseEnvironment();
		ResetCourseDataHelper resetCourseNodeHelper = new ResetCourseDataHelper(courseEnv);
		MediaResource archiveResource = resetCourseNodeHelper
				.resetCourseNodes(List.of(assessedIdentity), List.of(courseNode), false, getIdentity(), Role.coach);
		if(archiveResource != null) {
			Command downloadCmd = CommandFactory.createDownloadMediaResource(ureq, archiveResource);
			getWindowControl().getWindowBackOffice().sendCommandTo(downloadCmd);
		}
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
	
	private void doConfirmCompensationExtraTime(UserRequest ureq) {
		compensationExtraTimeCtrl = new ConfirmCompensationExtraTimeController(ureq, getWindowControl(),
				assessedIdentity, courseEntry, courseNode, lastSession);
		listenTo(compensationExtraTimeCtrl);

		String fullName  = userManager.getUserDisplayName(assessedIdentity);
		String title = translate("extra.time.compensation", fullName);
		cmc = new CloseableModalController(getWindowControl(), null, compensationExtraTimeCtrl.getInitialComponent(), true, title, true);
		listenTo(cmc);
		cmc.activate();
	}
	
	private void doConfirmRemoveCompensationExtraTime(UserRequest ureq) {
		List<DisadvantageCompensation> compensations = disadvantageCompensationService
				.getDisadvantageCompensations(assessedIdentity, courseEntry, testCourseNode.getIdent());
		removeCompensationExtraTimeCtrl = new ConfirmDeleteDisadvantageCompensationController(ureq, getWindowControl(),
				compensations, lastSession);
		listenTo(removeCompensationExtraTimeCtrl);

		String fullName  = userManager.getUserDisplayName(assessedIdentity);
		String title = translate("remove.extra.time.compensation", fullName);
		cmc = new CloseableModalController(getWindowControl(), null, removeCompensationExtraTimeCtrl.getInitialComponent(), true, title, true);
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
	
	private void doOpenChat(UserRequest ureq) {
		final String channel = assessedIdentity.getKey().toString();
		// Make sure the assessed used is in the roster
		String assessedFullName = userManager.getUserDisplayName(assessedIdentity);
		imService.addToRoster(assessedIdentity, courseEntry.getOlatResource(), testCourseNode.getIdent(), channel, assessedFullName, false, false);
		// Add the coach to the roster and go
		final String from = userManager.getUserDisplayName(getIdentity());
		imService.addToRoster(getIdentity(), courseEntry.getOlatResource(), testCourseNode.getIdent(), channel, from, false, true);
		imService.sendStatusMessage(getIdentity(), from, false, InstantMessageTypeEnum.join,
				courseEntry.getOlatResource(), testCourseNode.getIdent(), channel);
		imService.deleteNotifications(courseEntry.getOlatResource(), testCourseNode.getIdent(), channel);

		ChatViewConfig viewConfig = new ChatViewConfig();
		viewConfig.setRoomName(translate("im.title"));
		viewConfig.setResourceInfos(courseNode.getShortTitle());
		CourseNodeConfiguration nodeConfig = CourseNodeFactory.getInstance()
				.getCourseNodeConfigurationEvenForDisabledBB(courseNode.getType());
		viewConfig.setResourceIconCssClass(nodeConfig.getIconCSSClass());
		viewConfig.setCanClose(true);
		viewConfig.setCanReactivate(true);
		viewConfig.setCanMeeting((bigBlueButtonModule.isEnabled() && bigBlueButtonModule.isChatExamsEnabled())
				|| (teamsModule.isEnabled() && teamsModule.isChatExamsEnabled()));
		viewConfig.setWidth(620);
		viewConfig.setHeight(480);
		viewConfig.setRosterDisplay(RosterFormDisplay.supervisor);
		OpenInstantMessageEvent event = new OpenInstantMessageEvent(courseEntry.getOlatResource(), testCourseNode.getIdent(), channel, viewConfig, true, false);
		ureq.getUserSession().getSingleUserEventCenter().fireEventToListenersOf(event, InstantMessagingService.TOWER_EVENT_ORES);
	}
	
	private void doNewInspection(UserRequest ureq) {
		List<IdentityRef> participants = List.of(assessedIdentity);
		List<DisadvantageCompensation> compensations = disadvantageCompensationService
				.getActiveDisadvantageCompensations(getCourseRepositoryEntry(), courseNode.getIdent());
		List<DisadvantageCompensation> participantsCompensations = compensations.stream()
				.filter(compensation -> assessedIdentity.equals(compensation.getIdentity()))
				.toList();
	
		CreateInspectionContext context = new CreateInspectionContext(courseEntry, secCallback);
		context.setCourseNode(courseNode);
		context.setParticipants(participants, participantsCompensations);
		CreateInspectionFinishStepCallback finish = new CreateInspectionFinishStepCallback(context);
		
		Step start;
		if(inspectionService.hasInspectionConfigurations(getCourseRepositoryEntry())) {
			start = new CreateInspection_3_InspectionStep(ureq, context);
		} else {
			start = new CreateInspection_1a_CreateConfigurationStep(ureq, context);
		}
		newInspectionWizard = new StepsMainRunController(ureq, getWindowControl(), start, finish, null, translate("bulk.inspection.title"), "");
		listenTo(newInspectionWizard);
		getWindowControl().pushAsModalDialog(newInspectionWizard.getInitialComponent());
	}
	
	public static class AssessmentTestSessionDetailsComparator implements Comparator<AssessmentTestSessionStatistics> {
		
		private final AssessmentTestSessionComparator comparator = new AssessmentTestSessionComparator();

		@Override
		public int compare(AssessmentTestSessionStatistics q1, AssessmentTestSessionStatistics q2) {
			return comparator.compare(q1.testSession(), q2.testSession());
		}
	}
}