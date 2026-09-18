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

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.olat.basesecurity.BaseSecurityModule;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableExtendedFilter;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableFilterValue;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableSearchEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SelectionEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.filter.FlexiTableOneClickSelectionFilter;
import org.olat.core.gui.components.form.flexible.impl.elements.table.tab.FlexiFiltersTab;
import org.olat.core.gui.components.form.flexible.impl.elements.table.tab.FlexiFiltersTabFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.tab.FlexiTableFilterTabEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.tab.TabSelectionBehavior;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.stack.PopEvent;
import org.olat.core.gui.components.stack.TooledStackedPanel;
import org.olat.core.gui.components.util.SelectionValues;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.id.Identity;
import org.olat.core.id.Roles;
import org.olat.core.id.UserConstants;
import org.olat.core.util.coordinate.CoordinatorManager;
import org.olat.core.util.coordinate.LockResult;
import org.olat.course.assessment.AssessmentHelper;
import org.olat.course.run.userview.UserCourseEnvironment;
import org.olat.ims.qti21.AssessmentItemSession;
import org.olat.ims.qti21.AssessmentTestSession;
import org.olat.ims.qti21.QTI21Module;
import org.olat.ims.qti21.QTI21Module.CorrectionWorkflow;
import org.olat.ims.qti21.QTI21Service;
import org.olat.ims.qti21.ui.ConfirmReopenAssessmentEntryController;
import org.olat.ims.qti21.ui.assessment.CorrectionIdentityTableModel.IdentityCols;
import org.olat.ims.qti21.ui.assessment.components.AnsweredFlexiCellRenderer;
import org.olat.ims.qti21.ui.assessment.components.ToCorrectFlexiCellRenderer;
import org.olat.ims.qti21.ui.assessment.components.ToReviewFlexiCellRenderer;
import org.olat.ims.qti21.ui.assessment.model.CorrectionIdentityRow;
import org.olat.ims.qti21.ui.assessment.model.ItemSessionKey;
import org.olat.modules.assessment.model.AssessmentEntryStatus;
import org.olat.modules.assessment.ui.ScoreCellRenderer;
import org.olat.modules.assessment.ui.event.CompleteAssessmentTestSessionEvent;
import org.olat.user.UserManager;
import org.olat.user.propertyhandlers.UserPropertyHandler;
import org.springframework.beans.factory.annotation.Autowired;

import uk.ac.ed.ph.jqtiplus.node.test.AssessmentItemRef;
import uk.ac.ed.ph.jqtiplus.node.test.AssessmentTest;
import uk.ac.ed.ph.jqtiplus.state.ItemSessionState;
import uk.ac.ed.ph.jqtiplus.state.TestPlanNodeKey;
import uk.ac.ed.ph.jqtiplus.state.TestSessionState;

/**
 * A table of all the assessed identities with statistics
 * among their questions / assessment items.
 * 
 * 
 * Initial date: 23 févr. 2018<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class CorrectionIdentityListController extends FormBasicController {

	public static final String USER_PROPS_ID = CorrectionIdentityListController.class.getCanonicalName();

	public static final int USER_PROPS_OFFSET = 500;
	
	public static final String FILTER_TO_CORRECT = "tocorrect";
	public static final String FILTER_TO_REVIEW = "toreview";
	public static final String FILTER_MANUAL = "manual";
	public static final String FILTER_ADJUSTED = "adjusted";
	
	private static final String ALL_TAB = "All";
	private static final String TO_REVIEW_TAB = "ToReview";
	private static final String TO_CORRECT_TAB = "ToCorrect";
	private static final String MANUAL_TAB = "Manual";
	private static final String ADJUSTED_TAB = "Adjusted";

	private FlexiTableElement tableEl;
	private FormLink saveTestsButton;
	private final TooledStackedPanel stackPanel;
	private CorrectionIdentityTableModel tableModel;

	private FlexiFiltersTab allTab;
	private DefaultFlexiColumnModel correctedCol;
	private DefaultFlexiColumnModel notCorrectedCol;

	private CloseableModalController cmc;
	private ConfirmSaveTestsController confirmSaveTestCtrl;
	private ConfirmReopenAssessmentEntryController reopenForCorrectionCtrl;
	private CorrectionIdentityAssessmentItemListController identityItemListCtrl;

	private List<UserPropertyHandler> userPropertyHandlers;

	private LockResult lockResult;
	private final boolean anonymous;
	private final CorrectionOverviewModel model;
	
	@Autowired
	private QTI21Module qtiModule;
	@Autowired
	private QTI21Service qtiService;
	@Autowired
	private UserManager userManager;
	@Autowired
	private BaseSecurityModule securityModule;
	
	public CorrectionIdentityListController(UserRequest ureq, WindowControl wControl, TooledStackedPanel stackPanel,
			CorrectionOverviewModel model) {
		super(ureq, wControl, "correction_identity_list");
		setTranslator(userManager.getPropertyHandlerTranslator(getTranslator()));
		
		this.model = model;
		this.stackPanel = stackPanel;
		anonymous = qtiModule.getCorrectionWorkflow() == CorrectionWorkflow.anonymous;
		stackPanel.addListener(this);
		
		Roles roles = ureq.getUserSession().getRoles();
		boolean isAdministrativeUser = securityModule.isUserAllowedAdminProps(roles);
		userPropertyHandlers = userManager.getUserPropertyHandlersFor(USER_PROPS_ID, isAdministrativeUser);

		initForm(ureq);
		loadModel(true, false);
	}
	
	public int getNumberOfAssessedIdentities() {
		return model.getNumberOfAssessedIdentities();
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		if(model.hasErrors() && formLayout instanceof FormLayoutContainer layoutCont) {
			String errorMsg = getErrorMessage();
			layoutCont.contextPut("errorMsg", errorMsg);
		}
		
		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		if(anonymous) {
			DefaultFlexiColumnModel identifierCol = new DefaultFlexiColumnModel(IdentityCols.user, "select");
			identifierCol.setAlwaysVisible(true);
			columnsModel.addFlexiColumnModel(identifierCol);
		} else {
			int colPos = USER_PROPS_OFFSET;
			for (UserPropertyHandler userPropertyHandler : userPropertyHandlers) {
				if (userPropertyHandler == null) continue;

				String propName = userPropertyHandler.getName();
				boolean visible = userManager.isMandatoryUserProperty(USER_PROPS_ID , userPropertyHandler);

				DefaultFlexiColumnModel col = new DefaultFlexiColumnModel(visible, userPropertyHandler.i18nColumnDescriptorLabelKey(), colPos, "select", true, propName);
				if(UserConstants.LASTNAME.equals(propName)) {
					col.setDefaultVisible(true);
				}
				columnsModel.addFlexiColumnModel(col);
				colPos++;
			}
		}
		
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, IdentityCols.lastSession));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(IdentityCols.answered,
				new AnsweredFlexiCellRenderer()));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, IdentityCols.notAnswered));
		DefaultFlexiColumnModel scoreCol = new DefaultFlexiColumnModel(IdentityCols.score, new ScoreCellRenderer());
		scoreCol.setAlwaysVisible(true);
		columnsModel.addFlexiColumnModel(scoreCol);
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(IdentityCols.autoCorrected));
		correctedCol = new DefaultFlexiColumnModel(IdentityCols.corrected);
		columnsModel.addFlexiColumnModel(correctedCol);
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(IdentityCols.adjusted));
		notCorrectedCol = new DefaultFlexiColumnModel(IdentityCols.notCorrected,
				new ToCorrectFlexiCellRenderer(getTranslator()));
		columnsModel.addFlexiColumnModel(notCorrectedCol);
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(IdentityCols.toReview,
				new ToReviewFlexiCellRenderer()));
		
		tableModel = new CorrectionIdentityTableModel(columnsModel, getLocale());
		tableEl = uifactory.addTableElement(getWindowControl(), "table", tableModel, getTranslator(), formLayout);
		tableEl.setExportEnabled(true);
		tableEl.setMultiSelect(true);
		tableEl.setSelectAllEnable(true);
		initFilters();
		initFilterPresets();
		tableEl.setSelectedFilterTab(ureq, allTab);
		
		saveTestsButton = uifactory.addFormLink("save.tests", formLayout, Link.BUTTON);
	}
	
	private void initFilters() {
		List<FlexiTableExtendedFilter> filters = new ArrayList<>();

		SelectionValues toCorrectPK = new SelectionValues();
		toCorrectPK.add(SelectionValues.entry(FILTER_TO_CORRECT, translate("filter.to.correct")));
		FlexiTableOneClickSelectionFilter toCorrectFilter = new FlexiTableOneClickSelectionFilter(translate("filter.to.correct"),
				FILTER_TO_CORRECT, toCorrectPK, true);
		filters.add(toCorrectFilter);
		
		SelectionValues toReviewPK = new SelectionValues();
		toReviewPK.add(SelectionValues.entry(FILTER_TO_REVIEW, translate("filter.to.review")));
		FlexiTableOneClickSelectionFilter toReviewFilter = new FlexiTableOneClickSelectionFilter(translate("filter.to.review"),
				FILTER_TO_REVIEW, toReviewPK, true);
		filters.add(toReviewFilter);
		
		SelectionValues manualPK = new SelectionValues();
		manualPK.add(SelectionValues.entry(FILTER_MANUAL, translate("filter.manual")));
		FlexiTableOneClickSelectionFilter manualFilter = new FlexiTableOneClickSelectionFilter(translate("filter.manual"),
				FILTER_MANUAL, manualPK, true);
		filters.add(manualFilter);
		
		SelectionValues adjustedPK = new SelectionValues();
		adjustedPK.add(SelectionValues.entry(FILTER_ADJUSTED, translate("filter.adjusted")));
		FlexiTableOneClickSelectionFilter adjustedFilter = new FlexiTableOneClickSelectionFilter(translate("filter.adjusted"),
				FILTER_ADJUSTED, adjustedPK, true);
		filters.add(adjustedFilter);
		
		tableEl.setFilters(true, filters, true, false);
	}
	
	private void initFilterPresets() {
		List<FlexiFiltersTab> tabs = new ArrayList<>();
		
		allTab = FlexiFiltersTabFactory.tabWithImplicitFilters(ALL_TAB, translate("filter.all"),
				TabSelectionBehavior.reloadData, List.of());
		tabs.add(allTab);
		
		FlexiFiltersTab toCorrectTab = FlexiFiltersTabFactory.tabWithImplicitFilters(TO_CORRECT_TAB, translate("filter.to.correct"),
				TabSelectionBehavior.reloadData, List.of(FlexiTableFilterValue.valueOf(FILTER_TO_CORRECT, FILTER_TO_CORRECT)));
		tabs.add(toCorrectTab);
		
		FlexiFiltersTab toReviewTab = FlexiFiltersTabFactory.tabWithImplicitFilters(TO_REVIEW_TAB, translate("filter.to.review"),
				TabSelectionBehavior.reloadData, List.of(FlexiTableFilterValue.valueOf(FILTER_TO_REVIEW, FILTER_TO_REVIEW)));
		tabs.add(toReviewTab);
		
		FlexiFiltersTab manualTab = FlexiFiltersTabFactory.tabWithImplicitFilters(MANUAL_TAB, translate("filter.manual"),
				TabSelectionBehavior.reloadData, List.of(FlexiTableFilterValue.valueOf(FILTER_MANUAL, FILTER_MANUAL)));
		tabs.add(manualTab);
		
		FlexiFiltersTab adjustedTab = FlexiFiltersTabFactory.tabWithImplicitFilters(ADJUSTED_TAB, translate("filter.adjusted"),
				TabSelectionBehavior.reloadData, List.of(FlexiTableFilterValue.valueOf(FILTER_ADJUSTED, FILTER_ADJUSTED)));
		tabs.add(adjustedTab);
		
		tableEl.setFilterTabs(true, tabs);
	}
	
	public String getErrorMessage() {
		StringBuilder sb = new StringBuilder(1024);
		List<Identity> identities = model.getIdentityWithErrors();
		for(Identity identity:identities) {
			if(sb.length() > 0) sb.append(", ");
			sb.append(userManager.getUserDisplayName(identity));
		}
		return translate("error.assessment.test.session.identities", new String[] { sb.toString() });
	}
	
	public void reloadModel() {
		loadModel(true, true);
		filterModel();
	}
	
	private void loadModel(boolean reset, boolean lastSessions) {
		if(lastSessions) {
			model.loadLastSessions();
		}
		
		List<AssessmentItemRef> itemRefs = model.getResolvedAssessmentTest().getAssessmentItemRefs();
		Map<String, AssessmentItemRef> identifierToItemRefMap = new HashMap<>();
		for(AssessmentItemRef itemRef:itemRefs) {
			identifierToItemRefMap.put(itemRef.getIdentifier().toString(), itemRef);
		}
		
		List<AssessmentItemSession> itemSessions = qtiService
				.getAssessmentItemSessions(model.getCourseEntry(), model.getSubIdent(), model.getTestEntry(), null);
		Map<ItemSessionKey,AssessmentItemSession> itemSessionMap = new HashMap<>();
		for(AssessmentItemSession itemSession:itemSessions) {
			AssessmentTestSession candidateSession = itemSession.getAssessmentTestSession();
			if(model.getReversedLastSessions().containsKey(candidateSession)) {// the map contains all test sessions the user is allowed to correct
				String itemRefIdentifier = itemSession.getAssessmentItemIdentifier();
				itemSessionMap.put(new ItemSessionKey(candidateSession.getKey(), itemRefIdentifier), itemSession);
			}
		}
		
		List<CorrectionIdentityRow> rows = new ArrayList<>(model.getNumberOfAssessedIdentities());
		Map<Identity, CorrectionIdentityRow> identityToRows = new HashMap<>();
		for(Map.Entry<Identity, AssessmentTestSession> entry:model.getLastSessions().entrySet()) {
			Identity assessedIdentity = entry.getKey();
			TestSessionState testSessionState = model.getTestSessionStates().get(assessedIdentity);
			if(testSessionState != null) {
				String user = model.getAnonymizedName(assessedIdentity);
				CorrectionIdentityRow row = new CorrectionIdentityRow(user, assessedIdentity, entry.getValue(), userPropertyHandlers, getLocale());
				identityToRows.put(entry.getKey(), row);
				
				for(Map.Entry<TestPlanNodeKey, ItemSessionState> itemEntry:testSessionState.getItemSessionStates().entrySet()) {
					String itemRefIdentifier = itemEntry.getKey().getIdentifier().toString();
					AssessmentItemRef itemRef = identifierToItemRefMap.get(itemRefIdentifier);
					AssessmentItemSession itemSession = itemSessionMap.get(new ItemSessionKey(entry.getValue().getKey(), itemRefIdentifier));
					appendStatistics(row, itemSession, itemEntry.getValue(), itemRef);
				}
			}
		}
		
		for(Identity assessedIdentity:model.getAssessedIdentities()) {
			CorrectionIdentityRow row = identityToRows.remove(assessedIdentity);
			if(row != null) {
				rows.add(row);
			}
		}
		if(!identityToRows.isEmpty()) {
			rows.addAll(identityToRows.values());
		}
		
		tableModel.setObjects(rows);
		tableEl.reset(reset, reset, true);
		
		boolean hasManualCorrection = false;
		for(CorrectionIdentityRow row:rows) {
			hasManualCorrection |= row.isManualCorrection();
		}
		
		setColumnVisible(correctedCol, hasManualCorrection);
		setColumnVisible(notCorrectedCol, hasManualCorrection);
	}
	
	private void setColumnVisible(DefaultFlexiColumnModel col, boolean manualCorrections) {
		if(col == null) return;
		col.setDefaultVisible(manualCorrections);
		tableEl.setColumnModelVisible(col, manualCorrections);
	}
	
	private void filterModel() {
		tableModel.filter(tableEl.getQuickSearchString(), tableEl.getFilters());
		tableEl.reset(true, true, true);
	}
	
	private void appendStatistics(CorrectionIdentityRow row, AssessmentItemSession itemSession,
			ItemSessionState itemSessionState, AssessmentItemRef itemRef) {
		row.addSession();
		row.addQuestion();
		if(itemSessionState.isResponded()) {
			row.addAnswered();
		} else {
			row.addNotAnswered();
		}
		
		BigDecimal manualScore = null;
		if(itemSession != null) {
			manualScore = itemSession.getManualScore();
			if(itemSession.isToReview()) {
				row.addToReview();
			}
		}
		
		boolean manualCorrection = model.isManualCorrection(itemRef);
		if(!row.isManualCorrection()) {
			row.setManualCorrection(manualCorrection);
		}
		if(!manualCorrection) {
			row.addAutoCorrectedQuestion();
			if(!itemSessionState.isResponded()) {
				row.addAutoCorrectedNotAnswered();
			}
		}
		if(manualCorrection) {
			if(manualScore == null) {
				row.addNotCorrected();
			} else {
				row.addCorrected();
			}
		} else if(manualScore != null) {
			row.addAdjusted();
		} else {
			row.addAutoCorrected();
		}
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
				if(pe.getController() == identityItemListCtrl) {
					loadModel(false, true);
					filterModel();
				}
			}
		}
		super.event(ureq, source, event);
	}
	
	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(identityItemListCtrl == source) {
			if(event == Event.CANCELLED_EVENT || event == Event.BACK_EVENT) {
				doUnlock();
				loadModel(false, true);
				filterModel();
				stackPanel.popController(identityItemListCtrl);
			} else if(event == Event.CHANGED_EVENT) {
				fireEvent(ureq, Event.CHANGED_EVENT);
			}
		} else if(confirmSaveTestCtrl == source) {
			if(event == Event.DONE_EVENT) {
				doSaveTests(ureq);
			}
			cmc.deactivate();
			cleanUp();
		} else if(reopenForCorrectionCtrl == source) {
			cmc.deactivate();
			CorrectionIdentityRow row = (CorrectionIdentityRow)reopenForCorrectionCtrl.getUserObject();
			cleanUp();
			if(event == Event.CHANGED_EVENT || event == Event.DONE_EVENT) {
				model.discardAssessmentEntryDone(row.getIdentity());
				doOpenCorrection(ureq, row);
			}
		} else if(cmc == source) {
			cleanUp();
		}
		super.event(ureq, source, event);
	}
	
	private void cleanUp() {
		removeAsListenerAndDispose(reopenForCorrectionCtrl);
		removeAsListenerAndDispose(confirmSaveTestCtrl);
		removeAsListenerAndDispose(cmc);
		reopenForCorrectionCtrl = null;
		confirmSaveTestCtrl = null;
		cmc = null;
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(tableEl == source) {
			if(event instanceof SelectionEvent se) {
				String cmd = se.getCommand();
				if("select".equals(cmd)) {
					CorrectionIdentityRow row = tableModel.getObject(se.getIndex());
					doSelect(ureq, row);
				}
			} else if(event instanceof FlexiTableFilterTabEvent || event instanceof FlexiTableSearchEvent) {
				filterModel();
			}
		} else if(saveTestsButton == source) {
			doConfirmSaveTests(ureq);
		}
		super.formInnerEvent(ureq, source, event);
	}
	
	private void doSelect(UserRequest ureq, CorrectionIdentityRow row) {
		if(identityItemListCtrl != null) {
			stackPanel.popController(identityItemListCtrl);
		}
		
		Identity assessedIdentity = row.getIdentity();
		boolean assessmentEntryDone = model.isAssessmentEntryDone(assessedIdentity);
		if(assessmentEntryDone) {
			doReopenForCorrection(ureq, row);
		} else {
			doOpenCorrection(ureq, row);
		}
	}
	
	private void doReopenForCorrection(UserRequest ureq, CorrectionIdentityRow row) {
		if(guardModalController(reopenForCorrectionCtrl)) return;

		UserCourseEnvironment assessedUserCourseEnv = AssessmentHelper
				.createAndInitUserCourseEnvironment(row.getIdentity(), model.getCourseEnvironment());
		reopenForCorrectionCtrl = new ConfirmReopenAssessmentEntryController(ureq, getWindowControl(),
				assessedUserCourseEnv, model.getCourseNode(), row.getCandidateSession());
		reopenForCorrectionCtrl.setUserObject(row);
		listenTo(reopenForCorrectionCtrl);
		
		cmc = new CloseableModalController(getWindowControl(), translate("close"), reopenForCorrectionCtrl.getInitialComponent(),
				true, translate("reopen.assessment.title"));
		cmc.activate();
		listenTo(cmc);
	}

	private void doOpenCorrection(UserRequest ureq, CorrectionIdentityRow row) {
		Identity assessedIdentity = row.getIdentity();
		boolean assessmentEntryDone = model.isAssessmentEntryDone(assessedIdentity);
		String title = anonymous ? translate("participant.name", row.getUser()) : userManager.getUserDisplayName(row.getIdentity());
		identityItemListCtrl = new CorrectionIdentityAssessmentItemListController(ureq, getWindowControl(), stackPanel,
				model, assessedIdentity, title, assessmentEntryDone, anonymous);
		listenTo(identityItemListCtrl);
		
		String crumb;
		if(qtiModule.getCorrectionWorkflow() == CorrectionWorkflow.anonymous) {
			crumb = row.getUser();
		} else {
			crumb = userManager.getUserDisplayName(row.getIdentity());
		}
		stackPanel.pushController(crumb, identityItemListCtrl);
	}
	
	private void doConfirmSaveTests(UserRequest ureq) {
		int notCorrectedQuestions = 0;
		List<CorrectionIdentityRow> rows = tableModel.getObjects();
		for(CorrectionIdentityRow row:rows) {
			notCorrectedQuestions += row.getNumNotCorrected();
		}
		
		confirmSaveTestCtrl = new ConfirmSaveTestsController(ureq, getWindowControl(), notCorrectedQuestions > 0);
		listenTo(confirmSaveTestCtrl);
		
		cmc = new CloseableModalController(getWindowControl(), translate("close"), confirmSaveTestCtrl.getInitialComponent(),
				true, translate("save.tests"));
		listenTo(cmc);
		cmc.activate();
	}
	
	private void doSaveTests(UserRequest ureq) {
		Set<Integer> selections = tableEl.getMultiSelectedIndex();
		List<AssessmentTestSession> rows = new ArrayList<>(selections.size());
		for(Integer i:selections) {
			CorrectionIdentityRow row = tableModel.getObject(i.intValue());
			if(row != null) {
				rows.add(row.getCandidateSession());
			}
		}
		
		AssessmentTest assessmentTest = model.getResolvedAssessmentTest().getRootNodeLookup().extractIfSuccessful();
		fireEvent(ureq, new CompleteAssessmentTestSessionEvent(rows, assessmentTest, AssessmentEntryStatus.done));
	}
}
