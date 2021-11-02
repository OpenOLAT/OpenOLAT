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
package org.olat.ims.qti21.ui.assessment;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.StringHelper;
import org.olat.core.util.session.UserSessionModule;
import org.olat.fileresource.FileResourceManager;
import org.olat.ims.qti21.AssessmentItemSession;
import org.olat.ims.qti21.AssessmentSessionAuditLogger;
import org.olat.ims.qti21.AssessmentTestHelper;
import org.olat.ims.qti21.AssessmentTestSession;
import org.olat.ims.qti21.CorrectionManager;
import org.olat.ims.qti21.QTI21Service;
import org.olat.ims.qti21.model.ParentPartItemRefs;
import org.olat.ims.qti21.ui.ResourcesMapper;
import org.olat.ims.qti21.ui.assessment.event.NextAssessmentItemEvent;
import org.olat.ims.qti21.ui.assessment.model.AssessmentItemCorrection;
import org.olat.ims.qti21.ui.assessment.model.AssessmentItemListEntry;
import org.olat.modules.grading.GradingService;
import org.olat.modules.grading.GradingTimeRecordRef;
import org.olat.repository.RepositoryEntry;
import org.springframework.beans.factory.annotation.Autowired;

import uk.ac.ed.ph.jqtiplus.node.test.AssessmentTest;
import uk.ac.ed.ph.jqtiplus.resolution.ResolvedAssessmentTest;
import uk.ac.ed.ph.jqtiplus.state.TestPlanNodeKey;
import uk.ac.ed.ph.jqtiplus.state.TestSessionState;

/**
 * 
 * Initial date: 23 févr. 2018<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class CorrectionIdentityAssessmentItemController extends FormBasicController {

	private FormLink nextQuestionButton;
	private FormLink backOverviewButton;
	private FormLink saveNextQuestionButton;
	private FormLink saveBackOverviewButton;

	private final String mapperUri;
	private final URI assessmentObjectUri;
	private final ResourcesMapper resourcesMapper;
	
	private final boolean readOnly;
	private final boolean pageIdentity;
	private CorrectionOverviewModel model;
	private final RepositoryEntry testEntry;
	private AssessmentItemCorrection itemCorrection;
	private final AssessmentItemListEntry assessmentEntry;
	private final ResolvedAssessmentTest resolvedAssessmentTest;
	private final List<? extends AssessmentItemListEntry> assessmentEntryList;
	private Map<Long, File> submissionDirectoryMaps = new HashMap<>();
	
	private long timeStartInMilliSeconds;
	private GradingTimeRecordRef gradingTimeRecord;
	
	private CorrectionIdentityInteractionsController identityInteractionsCtrl;

	@Autowired
	private QTI21Service qtiService;
	@Autowired
	private GradingService gradingService;
	@Autowired
	private UserSessionModule sessionModule;
	@Autowired
	private CorrectionManager correctionManager;
	
	public CorrectionIdentityAssessmentItemController(UserRequest ureq, WindowControl wControl,
			RepositoryEntry testEntry, ResolvedAssessmentTest resolvedAssessmentTest,
			AssessmentItemCorrection itemCorrection, AssessmentItemListEntry assessmentEntry,
			List<? extends AssessmentItemListEntry> assessmentEntryList, CorrectionOverviewModel model,
			GradingTimeRecordRef gradingTimeRecord, boolean readOnly, boolean pageIdentity) {
		super(ureq, wControl, "correction_identity_assessment_item");
		this.readOnly = readOnly;
		this.gradingTimeRecord = gradingTimeRecord;
		timeStartInMilliSeconds = System.currentTimeMillis();
		
		FileResourceManager frm = FileResourceManager.getInstance();
		File fUnzippedDirRoot = frm.unzipFileResource(testEntry.getOlatResource());
		assessmentObjectUri = qtiService.createAssessmentTestUri(fUnzippedDirRoot);
		
		this.model = model;
		this.testEntry = testEntry;
		this.pageIdentity = pageIdentity;
		this.itemCorrection = itemCorrection;
		this.assessmentEntry = assessmentEntry;
		this.assessmentEntryList = assessmentEntryList;
		this.resolvedAssessmentTest = resolvedAssessmentTest;
		
		resourcesMapper = new ResourcesMapper(assessmentObjectUri, fUnzippedDirRoot, submissionDirectoryMaps);
		mapperUri = registerCacheableMapper(null, "QTI21CorrectionsResources::" + testEntry.getKey(), resourcesMapper);
		
		initForm(ureq);	
	}
	
	public AssessmentItemListEntry getAssessmentItemSession() {
		return assessmentEntry;
	}
	
	public List<? extends AssessmentItemListEntry> getAssessmentEntryList() {
		return assessmentEntryList;
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		if(formLayout instanceof FormLayoutContainer) {
			FormLayoutContainer layoutCont = (FormLayoutContainer)formLayout;
			layoutCont.contextPut("label", assessmentEntry.getLabel());
			layoutCont.contextPut("labelCssClass", assessmentEntry.getLabelCssClass());
			if(StringHelper.containsNonWhitespace(assessmentEntry.getTitle())) {
				layoutCont.contextPut("title", assessmentEntry.getTitle());
			}
			if(StringHelper.containsNonWhitespace(assessmentEntry.getTitleCssClass())) {
				layoutCont.contextPut("titleCssClass", assessmentEntry.getTitleCssClass());
			}
		}

		identityInteractionsCtrl = new CorrectionIdentityInteractionsController(ureq, getWindowControl(), 
				testEntry, resolvedAssessmentTest, itemCorrection, submissionDirectoryMaps, readOnly,
				mapperUri, mainForm);
		listenTo(identityInteractionsCtrl);
		formLayout.add("interactions", identityInteractionsCtrl.getInitialFormItem());
		
		uifactory.addFormCancelButton("cancel", formLayout, ureq, getWindowControl());

		if(readOnly) {
			String nextI18n = pageIdentity ? "next.user" : "next.item";
			nextQuestionButton = uifactory.addFormLink("next.item", nextI18n, null, formLayout, Link.BUTTON);
			backOverviewButton = uifactory.addFormLink("back.overview", formLayout, Link.BUTTON);
		} else {
			uifactory.addFormSubmitButton("save", formLayout);
			String saveNextI18n = pageIdentity ? "save.next.identity" : "save.next";
			saveNextQuestionButton = uifactory.addFormLink("save.next", saveNextI18n, null, formLayout, Link.BUTTON);
			saveBackOverviewButton = uifactory.addFormLink("save.back", formLayout, Link.BUTTON);
		}
	}
	
	protected void updateNext(boolean nextEnable) {
		if(nextQuestionButton != null) {
			nextQuestionButton.setVisible(nextEnable);
		}
		if(saveNextQuestionButton != null) {
			saveNextQuestionButton.setVisible(nextEnable);
		}
		if(saveBackOverviewButton != null) {
			saveBackOverviewButton.setVisible(!nextEnable);
		}
		if(backOverviewButton != null) {
			backOverviewButton.setVisible(!nextEnable);
		}
	}
	
	@Override
	protected void doDispose() {
		recordDisposedTime();
	}
	
	private void recordDisposedTime() {
		if(gradingTimeRecord == null || timeStartInMilliSeconds == 0l) return;
		
		long time = System.currentTimeMillis() - timeStartInMilliSeconds;
		timeStartInMilliSeconds = 0l;
		if(time > sessionModule.getSessionTimeoutAuthenticated()) {
			time -= sessionModule.getSessionTimeoutAuthenticated();
		}
		gradingService.appendTimeTo(gradingTimeRecord, time, TimeUnit.MILLISECONDS);
	}
	
	private void recordTime(boolean continueRecording) {
		if(gradingTimeRecord == null || timeStartInMilliSeconds == 0l) return;
		
		long time = System.currentTimeMillis() - timeStartInMilliSeconds;
		timeStartInMilliSeconds = continueRecording ? System.currentTimeMillis() : 0l;
		gradingService.appendTimeTo(gradingTimeRecord, time, TimeUnit.MILLISECONDS);
	}
	
	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = super.validateFormLogic(ureq);
		allOk &= identityInteractionsCtrl.validateFormLogic(ureq);
		return allOk;
	}

	@Override
	protected void formOK(UserRequest ureq) {
		if(!readOnly) {
			doSave();
		}
		recordTime(true);
		fireEvent(ureq, Event.CHANGED_EVENT);
		identityInteractionsCtrl.updateStatus();
	}

	@Override
	protected void formCancelled(UserRequest ureq) {
		recordTime(false);
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}
	
	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(nextQuestionButton == source) {
			recordTime(false);
			fireEvent(ureq, new NextAssessmentItemEvent());
		} else if(backOverviewButton == source) {
			recordTime(false);
			fireEvent(ureq, Event.BACK_EVENT);
		} else if(saveNextQuestionButton == source) {
			if(!readOnly && identityInteractionsCtrl.validateFormLogic(ureq)) {
				doSave();
				recordTime(false);
				fireEvent(ureq, Event.CHANGED_EVENT);
				fireEvent(ureq, new NextAssessmentItemEvent());
			}
		} else if(saveBackOverviewButton == source) {
			if(!readOnly && identityInteractionsCtrl.validateFormLogic(ureq)) {
				doSave();
				recordTime(false);
				fireEvent(ureq, Event.CHANGED_EVENT);
				fireEvent(ureq, Event.BACK_EVENT);
			}
		} else {
			super.formInnerEvent(ureq, source, event);
		}
	}

	private void doSave() {
		TestSessionState testSessionState = itemCorrection.getTestSessionState();
		AssessmentTestSession candidateSession = itemCorrection.getTestSession();
		try(AssessmentSessionAuditLogger candidateAuditLogger = qtiService.getAssessmentSessionAuditLogger(candidateSession, false)) {
			TestPlanNodeKey testPlanNodeKey = itemCorrection.getItemNode().getKey();
			String stringuifiedIdentifier = testPlanNodeKey.getIdentifier().toString();
			
			ParentPartItemRefs parentParts = AssessmentTestHelper
					.getParentSection(testPlanNodeKey, testSessionState, resolvedAssessmentTest);
			AssessmentItemSession itemSession = qtiService
					.getOrCreateAssessmentItemSession(candidateSession, parentParts, stringuifiedIdentifier);

			itemSession.setManualScore(identityInteractionsCtrl.getManualScore());
			itemSession.setCoachComment(identityInteractionsCtrl.getComment());
			itemSession.setToReview(identityInteractionsCtrl.isToReview());
			
			itemSession = qtiService.updateAssessmentItemSession(itemSession);
			itemCorrection.setItemSession(itemSession);
			
			candidateAuditLogger.logCorrection(candidateSession, itemSession, getIdentity());
			
			candidateSession = qtiService.recalculateAssessmentTestSessionScores(candidateSession.getKey());
			itemCorrection.setTestSession(candidateSession);
			model.updateLastSession(itemCorrection.getAssessedIdentity(), candidateSession);
			
			if(model.getCourseNode() != null && model.getCourseEnvironment() != null) {
				AssessmentTest assessmentTest = resolvedAssessmentTest.getRootNodeLookup().extractIfSuccessful();
				correctionManager.updateCourseNode(candidateSession, assessmentTest,
						model.getCourseNode(), model.getCourseEnvironment(), getIdentity());
			}
		} catch(IOException e) {
			logError("", e);
		}
	}
}
