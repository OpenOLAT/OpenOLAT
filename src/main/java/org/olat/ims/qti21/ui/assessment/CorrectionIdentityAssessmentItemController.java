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

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.StringHelper;
import org.olat.fileresource.FileResourceManager;
import org.olat.ims.qti21.AssessmentItemSession;
import org.olat.ims.qti21.AssessmentSessionAuditLogger;
import org.olat.ims.qti21.AssessmentTestHelper;
import org.olat.ims.qti21.AssessmentTestSession;
import org.olat.ims.qti21.QTI21Service;
import org.olat.ims.qti21.model.ParentPartItemRefs;
import org.olat.ims.qti21.ui.ResourcesMapper;
import org.olat.ims.qti21.ui.assessment.event.SelectAssessmentItemEvent;
import org.olat.ims.qti21.ui.assessment.model.AssessmentItemCorrection;
import org.olat.ims.qti21.ui.assessment.model.AssessmentItemListEntry;
import org.olat.repository.RepositoryEntry;
import org.springframework.beans.factory.annotation.Autowired;

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
	
	private FormLink backButton;
	private FormLink nextQuestionButton;
	private FormLink previousQuestionButton;
	private FormLink saveNextQuestionButton;
	private SingleSelection assessmentEntryListEl;

	private final String mapperUri;
	private final URI assessmentObjectUri;
	private final ResourcesMapper resourcesMapper;
	
	private CorrectionOverviewModel model;
	private final RepositoryEntry testEntry;
	private AssessmentItemCorrection itemCorrection;
	private final AssessmentItemListEntry assessmentEntry;
	private final ResolvedAssessmentTest resolvedAssessmentTest;
	private final List<? extends AssessmentItemListEntry> assessmentEntryList;
	private Map<Long, File> submissionDirectoryMaps = new HashMap<>();
	
	private CorrectionIdentityInteractionsController identityInteractionsCtrl;

	@Autowired
	private QTI21Service qtiService;
	
	public CorrectionIdentityAssessmentItemController(UserRequest ureq, WindowControl wControl,
			RepositoryEntry testEntry, ResolvedAssessmentTest resolvedAssessmentTest,
			AssessmentItemCorrection itemCorrection, AssessmentItemListEntry assessmentEntry,
			List<? extends AssessmentItemListEntry> assessmentEntryList, CorrectionOverviewModel model) {
		super(ureq, wControl, "correction_identity_assessment_item");

		FileResourceManager frm = FileResourceManager.getInstance();
		File fUnzippedDirRoot = frm.unzipFileResource(testEntry.getOlatResource());
		assessmentObjectUri = qtiService.createAssessmentTestUri(fUnzippedDirRoot);
		
		this.model = model;
		this.testEntry = testEntry;
		this.itemCorrection = itemCorrection;
		this.assessmentEntry = assessmentEntry;
		this.assessmentEntryList = assessmentEntryList;
		this.resolvedAssessmentTest = resolvedAssessmentTest;
		
		resourcesMapper = new ResourcesMapper(assessmentObjectUri, submissionDirectoryMaps);
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
		
		backButton = uifactory.addFormLink("back", formLayout, Link.BUTTON);
		backButton.setIconLeftCSS("o_icon o_icon_back");
		
		nextQuestionButton = uifactory.addFormLink("next.item", formLayout, Link.BUTTON);
		nextQuestionButton.setIconRightCSS("o_icon o_icon_next");
		
		String[] identityKeys = new String[assessmentEntryList.size()];
		String[] identityValues = new String[assessmentEntryList.size()];
		for(int i=assessmentEntryList.size(); i-->0; ) {
			identityKeys[i] = Integer.toString(i);
			identityValues[i] = assessmentEntryList.get(i).getLabel();
		}
		assessmentEntryListEl = uifactory.addDropdownSingleselect("to.assess", formLayout, identityKeys, identityValues);
		assessmentEntryListEl.setDomReplacementWrapperRequired(false);
		assessmentEntryListEl.addActionListener(FormEvent.ONCHANGE);
		int index = assessmentEntryList.indexOf(getAssessmentItemSession());
		if(index >= 0) {
			assessmentEntryListEl.select(Integer.toString(index), true);
		}

		previousQuestionButton = uifactory.addFormLink("previous.item", formLayout, Link.BUTTON);
		previousQuestionButton.setIconLeftCSS("o_icon o_icon_previous");

		identityInteractionsCtrl = new CorrectionIdentityInteractionsController(ureq, getWindowControl(), 
				testEntry, resolvedAssessmentTest, itemCorrection, submissionDirectoryMaps, mapperUri,
				mainForm);
		listenTo(identityInteractionsCtrl);
		formLayout.add("interactions", identityInteractionsCtrl.getInitialFormItem());
		
		uifactory.addFormCancelButton("cancel", formLayout, ureq, getWindowControl());
		uifactory.addFormSubmitButton("save", formLayout);
		saveNextQuestionButton = uifactory.addFormLink("save.next", formLayout, Link.BUTTON);
	}
	
	protected void updatePreviousNext(String previousText, boolean previousEnable, String nextText, boolean nextEnable) {
		previousQuestionButton.getComponent().setCustomDisplayText(previousText);
		previousQuestionButton.setEnabled(previousEnable);
		nextQuestionButton.getComponent().setCustomDisplayText(nextText);
		nextQuestionButton.setEnabled(nextEnable);
		saveNextQuestionButton.setEnabled(nextEnable);
	}
	
	@Override
	protected void doDispose() {
		//
	}

	@Override
	protected void formOK(UserRequest ureq) {
		doSave();
		identityInteractionsCtrl.updateStatus();
	}

	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}
	
	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(saveNextQuestionButton == source) {
			doSave();
			doNext(ureq);
		} else if(assessmentEntryListEl == source) {
			String selectEntry = assessmentEntryListEl.getSelectedKey();
			if(StringHelper.isLong(selectEntry)) {
				int selectedIndex = Integer.parseInt(selectEntry);
				if(selectedIndex >= 0 && selectedIndex < this.assessmentEntryList.size()) {
					AssessmentItemListEntry nextEntry = assessmentEntryList.get(selectedIndex);
					fireEvent(ureq, new SelectAssessmentItemEvent(nextEntry));
				}
			}
		} else if(nextQuestionButton == source) {
			doNext(ureq);
		} else if(previousQuestionButton == source) {
			doPrevious(ureq);
		} else if(backButton == source) {
			fireEvent(ureq, Event.BACK_EVENT);
		} else {
			super.formInnerEvent(ureq, source, event);
		}
	}
	
	private void doNext(UserRequest ureq) {
		AssessmentItemListEntry currentEntry = getAssessmentItemSession();
		int index = assessmentEntryList.indexOf(currentEntry) + 1;
		if(index >= 0 && index < assessmentEntryList.size()) {
			AssessmentItemListEntry nextEntry = assessmentEntryList.get(index);
			fireEvent(ureq, new SelectAssessmentItemEvent(nextEntry));
		} else {
			nextQuestionButton.setEnabled(false);
		}
	}
	
	private void doPrevious(UserRequest ureq) {
		AssessmentItemListEntry currentEntry = getAssessmentItemSession();
		int index = assessmentEntryList.indexOf(currentEntry) - 1;
		if(index >= 0 && index < assessmentEntryList.size()) {
			AssessmentItemListEntry nextEntry = assessmentEntryList.get(index);
			fireEvent(ureq, new SelectAssessmentItemEvent(nextEntry));
		} else {
			previousQuestionButton.setEnabled(false);
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
		} catch(IOException e) {
			logError("", e);
		}
	}
}
