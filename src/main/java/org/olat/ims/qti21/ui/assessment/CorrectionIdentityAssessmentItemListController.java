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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SelectionEvent;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.stack.BreadcrumbPanel;
import org.olat.core.gui.components.stack.PopEvent;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.gui.translator.Translator;
import org.olat.core.id.Identity;
import org.olat.core.id.OLATResourceable;
import org.olat.core.util.Util;
import org.olat.core.util.coordinate.CoordinatorManager;
import org.olat.core.util.coordinate.LockResult;
import org.olat.core.util.resource.OresHelper;
import org.olat.ims.qti21.AssessmentItemSession;
import org.olat.ims.qti21.AssessmentTestHelper;
import org.olat.ims.qti21.AssessmentTestSession;
import org.olat.ims.qti21.QTI21Service;
import org.olat.ims.qti21.model.ParentPartItemRefs;
import org.olat.ims.qti21.model.xml.ManifestMetadataBuilder;
import org.olat.ims.qti21.ui.assessment.CorrectionIdentityAssessmentItemTableModel.IdentityItemCols;
import org.olat.ims.qti21.ui.assessment.components.AutoCorrectedFlexiCellRenderer;
import org.olat.ims.qti21.ui.assessment.components.CorrectedFlexiCellRenderer;
import org.olat.ims.qti21.ui.assessment.components.NotCorrectedFlexiCellRenderer;
import org.olat.ims.qti21.ui.assessment.components.QuestionTypeFlexiCellRenderer;
import org.olat.ims.qti21.ui.assessment.components.ToReviewFlexiCellRenderer;
import org.olat.ims.qti21.ui.assessment.event.SelectAssessmentItemEvent;
import org.olat.ims.qti21.ui.assessment.model.AssessmentItemCorrection;
import org.olat.ims.qti21.ui.assessment.model.AssessmentItemListEntry;
import org.olat.ims.qti21.ui.assessment.model.CorrectionIdentityAssessmentItemRow;
import org.olat.ims.qti21.ui.editor.AssessmentTestComposerController;
import org.olat.modules.assessment.model.AssessmentEntryStatus;
import org.olat.modules.assessment.ui.ScoreCellRenderer;
import org.olat.modules.assessment.ui.event.CompleteAssessmentTestSessionEvent;
import org.olat.modules.grading.GradingAssignment;
import org.olat.modules.grading.GradingTimeRecordRef;
import org.olat.user.UserManager;
import org.springframework.beans.factory.annotation.Autowired;

import uk.ac.ed.ph.jqtiplus.node.item.AssessmentItem;
import uk.ac.ed.ph.jqtiplus.node.test.AssessmentItemRef;
import uk.ac.ed.ph.jqtiplus.node.test.AssessmentTest;
import uk.ac.ed.ph.jqtiplus.resolution.ResolvedAssessmentItem;
import uk.ac.ed.ph.jqtiplus.resolution.ResolvedAssessmentTest;
import uk.ac.ed.ph.jqtiplus.state.ItemSessionState;
import uk.ac.ed.ph.jqtiplus.state.TestPlanNode;
import uk.ac.ed.ph.jqtiplus.state.TestPlanNode.TestNodeType;
import uk.ac.ed.ph.jqtiplus.state.TestPlanNodeKey;
import uk.ac.ed.ph.jqtiplus.state.TestSessionState;
import uk.ac.ed.ph.jqtiplus.types.Identifier;

/**
 * Show the list of assessment item of a single assessed identity.
 * 
 * 
 * Initial date: 2 mars 2018<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class CorrectionIdentityAssessmentItemListController extends FormBasicController {
	
	private FormLink saveButton;
	private FormLink backLink;
	private FormLink backOverviewButton;
	private FlexiTableElement tableEl;
	private final BreadcrumbPanel stackPanel;
	private CorrectionIdentityAssessmentItemTableModel tableModel;

	private CloseableModalController cmc;
	private ConfirmSaveTestsController confirmSaveTestCtrl;
	private CorrectionIdentityAssessmentItemNavigationController identityItemCtrl;

	private final String title;
	private LockResult lockResult;
	private final boolean readOnly;
	private final boolean saveEnabled;
	private GradingAssignment assignment;
	private final Identity assessedIdentity;
	private final CorrectionOverviewModel model;
	private GradingTimeRecordRef gradingTimeRecord;

	@Autowired
	private QTI21Service qtiService;
	@Autowired
	private UserManager userManager;
	
	public CorrectionIdentityAssessmentItemListController(UserRequest ureq, WindowControl wControl, BreadcrumbPanel stackPanel,
			CorrectionOverviewModel model, Identity assessedIdentity, boolean readOnly) {
		super(ureq, wControl, "correction_identity_assessment_item_list");
		
		this.stackPanel = stackPanel;
		this.model = model;
		this.readOnly = readOnly;
		this.assessedIdentity = assessedIdentity;
		saveEnabled = true;
		title = userManager.getUserDisplayName(assessedIdentity);
		
		initForm(ureq);
		loadModel(true);
	}
	
	public CorrectionIdentityAssessmentItemListController(UserRequest ureq, WindowControl wControl, BreadcrumbPanel stackPanel,
			CorrectionOverviewModel model, Identity assessedIdentity, GradingAssignment assignment, GradingTimeRecordRef gradingTimeRecord,
			boolean readOnly, boolean anonymous) {
		super(ureq, wControl, "correction_identity_assessment_item_list");
		
		this.stackPanel = stackPanel;
		this.model = model;
		this.assessedIdentity = assessedIdentity;
		this.readOnly = readOnly;
		this.saveEnabled = !readOnly;
		this.assignment = assignment;
		this.gradingTimeRecord = gradingTimeRecord;
		title = anonymous ? translate("anonymous.user") : userManager.getUserDisplayName(assessedIdentity);
		initForm(ureq);
		loadModel(true);
	}
	
	public CorrectionIdentityAssessmentItemListController(UserRequest ureq, WindowControl wControl, BreadcrumbPanel stackPanel,
			CorrectionOverviewModel model, Identity assessedIdentity, String title, boolean readOnly) {
		super(ureq, wControl, "correction_identity_assessment_item_list");
		this.stackPanel = stackPanel;
		this.model = model;
		this.title = title;
		this.readOnly = readOnly;
		this.assessedIdentity = assessedIdentity;
		saveEnabled = false;

		initForm(ureq);
		loadModel(true);
	}
	
	public AssessmentTestSession getAssessmentTestSession() {
		return model.getLastSessions().get(assessedIdentity);
	}
	
	public GradingAssignment getGradingAssignment() {
		return assignment;
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		stackPanel.addListener(this);
		
		if(formLayout instanceof FormLayoutContainer) {
			FormLayoutContainer layoutCont = (FormLayoutContainer)formLayout;
			layoutCont.contextPut("title", title);
		}
		
		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(IdentityItemCols.section));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(IdentityItemCols.itemTitle, "select"));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, IdentityItemCols.itemKeywords, "select"));
		Translator qti21Translator = Util.createPackageTranslator(AssessmentTestComposerController.class, getLocale());
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(IdentityItemCols.itemType, new QuestionTypeFlexiCellRenderer(qti21Translator)));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(IdentityItemCols.score, new ScoreCellRenderer()));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(IdentityItemCols.answered, "answered"));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(IdentityItemCols.autoCorrected, "corrected", new AutoCorrectedFlexiCellRenderer()));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(IdentityItemCols.corrected, "corrected", new CorrectedFlexiCellRenderer()));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(IdentityItemCols.notCorrected, "notCorrected", new NotCorrectedFlexiCellRenderer()));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(IdentityItemCols.toReview, "toReview", new ToReviewFlexiCellRenderer()));
		
		tableModel = new CorrectionIdentityAssessmentItemTableModel(columnsModel, getLocale());
		tableEl = uifactory.addTableElement(getWindowControl(), "table", tableModel, getTranslator(), formLayout);
		tableEl.setExportEnabled(true);
		tableEl.setAndLoadPersistedPreferences(ureq, "corr-identity-assessment-item-list");
		tableEl.setElementCssClass("o_sel_correction_assessment_items_list");
		
		backLink = uifactory.addFormLink("back", formLayout, Link.LINK_BACK);
		if(saveEnabled && !readOnly) {
			saveButton = uifactory.addFormLink("save.tests", formLayout, Link.BUTTON);
			saveButton.setElementCssClass("o_sel_correction_save_test");
		} else {
			backOverviewButton = uifactory.addFormLink("back.overview", formLayout, Link.BUTTON);
		}
	}
	
	private void loadModel(boolean reset) {
		ResolvedAssessmentTest resolvedAssessmentTest = model.getResolvedAssessmentTest();
		Map<Identifier, AssessmentItemRef> identifierToRefs = new HashMap<>();
		for(AssessmentItemRef itemRef:resolvedAssessmentTest.getAssessmentItemRefs()) {
			identifierToRefs.put(itemRef.getIdentifier(), itemRef);
		}
		
		AssessmentTestSession candidateSession = getAssessmentTestSession();
		List<AssessmentItemSession> allItemSessions = qtiService.getAssessmentItemSessions(candidateSession);
		Map<String, AssessmentItemSession> identifierToItemSessions = new HashMap<>();
		for(AssessmentItemSession itemSession:allItemSessions) {
			identifierToItemSessions.put(itemSession.getAssessmentItemIdentifier(), itemSession);
		}

		//reorder to match the list of assessment items
		List<CorrectionIdentityAssessmentItemRow> rows = new ArrayList<>();
		TestSessionState testSessionState = model.getTestSessionStates().get(assessedIdentity);
		List<TestPlanNode> nodes = testSessionState.getTestPlan().getTestPlanNodeList();
		for(TestPlanNode node:nodes) {
			if(node.getTestNodeType() == TestNodeType.ASSESSMENT_ITEM_REF) {
				TestPlanNodeKey key = node.getKey();
				AssessmentItemRef itemRef = identifierToRefs.get(key.getIdentifier());
				AssessmentItemSession itemSession = identifierToItemSessions.get(key.getIdentifier().toString());
				ResolvedAssessmentItem resolvedAssessmentItem = resolvedAssessmentTest.getResolvedAssessmentItem(itemRef);
				ManifestMetadataBuilder metadata = model.getMetadata(itemRef);
				AssessmentItem item = resolvedAssessmentItem.getRootNodeLookup().extractIfSuccessful();
				ItemSessionState itemSessionState = testSessionState.getItemSessionStates().get(key);
				boolean manualCorrection = model.isManualCorrection(itemRef);
				CorrectionIdentityAssessmentItemRow row = new CorrectionIdentityAssessmentItemRow(assessedIdentity, item, itemRef,
						metadata, candidateSession, itemSession, itemSessionState, manualCorrection);
				row.setTitle(title);
				row.setTitleCssClass("o_icon_user");
				rows.add(row);
			}
		}
		
		tableModel.setObjects(rows);
		tableEl.reset(reset, reset, true);
	}

	@Override
	protected void doDispose() {
		stackPanel.removeListener(this);
		if(lockResult != null && lockResult.isSuccess()) {
			doUnlock();
		}
        super.doDispose();
	}
	
	private void doUnlock() {
		if(lockResult != null && lockResult.isSuccess()) {
			CoordinatorManager.getInstance().getCoordinator().getLocker().releaseLock(lockResult);
		}
	}

	@Override
	public void event(UserRequest ureq, Component source, Event event) {
		if(stackPanel == source) {
			if(event instanceof PopEvent) {
				PopEvent pe = (PopEvent)event;
				if(pe.getController() == identityItemCtrl) {
					loadModel(false);
				}
			}
		}
		super.event(ureq, source, event);
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(identityItemCtrl == source) {
			if(event == Event.CANCELLED_EVENT || event == Event.BACK_EVENT) {
				doUnlock();
				loadModel(false);
				stackPanel.popController(identityItemCtrl);
			} else if(event instanceof SelectAssessmentItemEvent) {
				stackPanel.popController(identityItemCtrl);
				SelectAssessmentItemEvent saie = (SelectAssessmentItemEvent)event;
				doSelect(ureq, (CorrectionIdentityAssessmentItemRow)saie.getListEntry());
			} else if(event == Event.CHANGED_EVENT) {
				fireEvent(ureq, event);
			}
		} else if(confirmSaveTestCtrl == source) {
			if(event == Event.DONE_EVENT) {
				doSaveTests(ureq);
			}
			cmc.deactivate();
			cleanUp();
		} else if(cmc == source) {
			cleanUp();
		}
		super.event(ureq, source, event);
	}
	
	private void cleanUp() {
		removeAsListenerAndDispose(confirmSaveTestCtrl);
		removeAsListenerAndDispose(cmc);
		confirmSaveTestCtrl = null;
		cmc = null;
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}
	
	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(tableEl == source) {
			if(event instanceof SelectionEvent) {
				SelectionEvent se = (SelectionEvent)event;
				String cmd = se.getCommand();
				if("select".equals(cmd)) {
					CorrectionIdentityAssessmentItemRow row = tableModel.getObject(se.getIndex());
					doSelect(ureq, row);
				}
			}
		} else if(saveButton == source) {
			doConfirmSaveTests(ureq);
		} else if(backLink == source || backOverviewButton == source) {
			fireEvent(ureq, Event.BACK_EVENT);
		}
		super.formInnerEvent(ureq, source, event);
	}
	
	
	private void doConfirmSaveTests(UserRequest ureq) {
		if(guardModalController(confirmSaveTestCtrl)) return;
		
		int notCorrectedQuestions = 0;
		List<CorrectionIdentityAssessmentItemRow> rows = tableModel.getObjects();
		for(CorrectionIdentityAssessmentItemRow row:rows) {
			if(!row.isCorrected()) {
				notCorrectedQuestions += 1;
			}
		}
		
		confirmSaveTestCtrl = new ConfirmSaveTestsController(ureq, getWindowControl(), notCorrectedQuestions > 0);
		listenTo(confirmSaveTestCtrl);
		
		cmc = new CloseableModalController(getWindowControl(), "close", confirmSaveTestCtrl.getInitialComponent(),
				true, translate("save.tests"));
		listenTo(cmc);
		cmc.activate();
	}
	
	private void doSaveTests(UserRequest ureq) {
		AssessmentTestSession candidateSession = getAssessmentTestSession();
		List<AssessmentTestSession> sessions = Collections.singletonList(candidateSession);
		AssessmentTest assessmentTest = model.getResolvedAssessmentTest().getRootNodeLookup().extractIfSuccessful();
		fireEvent(ureq, new CompleteAssessmentTestSessionEvent(sessions, assessmentTest, AssessmentEntryStatus.done));
	}
	
	private void doSelect(UserRequest ureq, CorrectionIdentityAssessmentItemRow row) {
		removeAsListenerAndDispose(identityItemCtrl);
		doUnlock();

		AssessmentItemRef itemRef = row.getItemRef();
		AssessmentTestSession candidateSession = getAssessmentTestSession();
		TestSessionState testSessionState = qtiService.loadTestSessionState(candidateSession);
		List<TestPlanNode> nodes = testSessionState.getTestPlan().getNodes(itemRef.getIdentifier());
		
		AssessmentItemSession reloadItemSession = null;
		if(nodes.size() == 1) {
			TestPlanNode itemNode = nodes.get(0);
			String stringuifiedIdentifier = itemNode.getKey().getIdentifier().toString();
			ParentPartItemRefs parentParts = AssessmentTestHelper
					.getParentSection(itemNode.getKey(), testSessionState, model.getResolvedAssessmentTest());
			reloadItemSession = qtiService
					.getOrCreateAssessmentItemSession(candidateSession, parentParts, stringuifiedIdentifier, itemRef.getIdentifier().toString());
		}
		
		// lock on item, need to check the lock on identity / test
		String lockSubKey = "item-" + reloadItemSession.getKey();
		OLATResourceable testOres = OresHelper.clone(model.getTestEntry().getOlatResource());
		lockResult = CoordinatorManager.getInstance().getCoordinator().getLocker().acquireLock(testOres, getIdentity(), lockSubKey, getWindow());
		if(lockResult.isSuccess() || readOnly) {
			if(nodes.size() == 1) {
				TestPlanNode itemNode = nodes.get(0);
				ItemSessionState itemSessionState = testSessionState.getItemSessionStates().get(itemNode.getKey());
				AssessmentItemCorrection itemCorrection = new AssessmentItemCorrection(assessedIdentity, 
						candidateSession, testSessionState, reloadItemSession, itemSessionState,
						itemRef, itemNode);
				itemCorrection.setItemSession(reloadItemSession);
				
				ResolvedAssessmentItem resolvedAssessmentItem = model.getResolvedAssessmentTest().getResolvedAssessmentItem(itemRef);
				AssessmentItem assessmentItem = resolvedAssessmentItem.getRootNodeLookup().extractIfSuccessful();
				identityItemCtrl = new CorrectionIdentityAssessmentItemNavigationController(ureq, getWindowControl(),
						model.getTestEntry(), model.getResolvedAssessmentTest(), itemCorrection, row,
						tableModel.getObjects(), model, gradingTimeRecord, readOnly, false);
				listenTo(identityItemCtrl);
				stackPanel.pushController(assessmentItem.getTitle(), identityItemCtrl);
				updatePreviousNext();
			}
		} else {
			String lockOwnerName = userManager.getUserDisplayName(lockResult.getOwner());
			String msg = lockResult.isDifferentWindows() ? "warning.assessment.item.locked.same.user" : "warning.assessment.item.locked";
			showWarning(msg, new String[] { lockOwnerName });
		}
	}
	
	private void updatePreviousNext() {
		if(identityItemCtrl != null) {
			List<CorrectionIdentityAssessmentItemRow> rows = tableModel.getObjects();
			AssessmentItemListEntry itemSession = identityItemCtrl.getAssessmentItemSession();
			String previousText = translate("previous.item");
			String nextText = translate("next.item");
			boolean previousEnable = false;
			boolean nextEnable = false;
			
			int index = rows.indexOf(itemSession);
			if(index - 1 >= 0 && rows.size() > index - 1) {
				previousText = rows.get(index - 1).getLabel();
				previousEnable = true;
			}
			
			if(index + 1 >= 0 && rows.size() > index + 1) {
				nextText = rows.get(index + 1).getLabel();
				nextEnable = true;
			}

			identityItemCtrl.updatePreviousNext(previousText, previousEnable, nextText, nextEnable);
		}
	}
}
