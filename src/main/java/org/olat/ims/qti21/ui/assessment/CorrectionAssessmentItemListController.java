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
import java.util.function.Predicate;

import org.olat.core.commons.services.pdf.PdfModule;
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
import org.olat.core.gui.components.form.flexible.impl.elements.table.ActionsColumnModel;
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
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.stack.PopEvent;
import org.olat.core.gui.components.stack.TooledStackedPanel;
import org.olat.core.gui.components.util.SelectionValues;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.generic.closablewrapper.CloseableCalloutWindowController;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.gui.media.MediaResource;
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
import org.olat.ims.qti21.QTI21Module;
import org.olat.ims.qti21.QTI21Module.CorrectionWorkflow;
import org.olat.ims.qti21.QTI21Service;
import org.olat.ims.qti21.model.ParentPartItemRefs;
import org.olat.ims.qti21.model.QTI21QuestionType;
import org.olat.ims.qti21.model.xml.ManifestMetadataBuilder;
import org.olat.ims.qti21.resultexport.EssaysPdfMediaResource;
import org.olat.ims.qti21.ui.assessment.BulkScoreController.Mode;
import org.olat.ims.qti21.ui.assessment.CorrectionAssessmentItemTableModel.ItemCols;
import org.olat.ims.qti21.ui.assessment.components.AnsweredFlexiCellRenderer;
import org.olat.ims.qti21.ui.assessment.components.QuestionTypeFlexiCellRenderer;
import org.olat.ims.qti21.ui.assessment.components.ToReviewFlexiCellRenderer;
import org.olat.ims.qti21.ui.assessment.event.SelectAssessmentItemEvent;
import org.olat.ims.qti21.ui.assessment.model.AssessmentItemCorrection;
import org.olat.ims.qti21.ui.assessment.model.AssessmentItemListEntry;
import org.olat.ims.qti21.ui.assessment.model.CorrectionAssessmentItemRow;
import org.olat.ims.qti21.ui.assessment.model.ItemSessionKey;
import org.olat.ims.qti21.ui.editor.AssessmentTestComposerController;
import org.olat.modules.assessment.model.AssessmentEntryStatus;
import org.olat.modules.assessment.ui.event.CompleteAssessmentTestSessionEvent;
import org.olat.user.UserManager;
import org.springframework.beans.factory.annotation.Autowired;

import uk.ac.ed.ph.jqtiplus.node.item.AssessmentItem;
import uk.ac.ed.ph.jqtiplus.node.test.AssessmentItemRef;
import uk.ac.ed.ph.jqtiplus.node.test.AssessmentTest;
import uk.ac.ed.ph.jqtiplus.resolution.ResolvedAssessmentItem;
import uk.ac.ed.ph.jqtiplus.resolution.ResolvedAssessmentTest;
import uk.ac.ed.ph.jqtiplus.state.ItemSessionState;
import uk.ac.ed.ph.jqtiplus.state.TestPlanNode;
import uk.ac.ed.ph.jqtiplus.state.TestPlanNodeKey;
import uk.ac.ed.ph.jqtiplus.state.TestSessionState;

/**
 * A table with the list of assessment items of the test
 * with statistics about the users who answered (or not answered)
 * every assessment item.
 * 
 * 
 * Initial date: 26 févr. 2018<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class CorrectionAssessmentItemListController extends FormBasicController {
	
	public static final String FILTER_TO_CORRECT = "tocorrect";
	public static final String FILTER_TO_REVIEW = "toreview";
	public static final String FILTER_MANUAL = "manual";
	public static final String FILTER_ADJUSTED = "adjusted";
	
	private static final String ALL_TAB = "All";
	private static final String TO_REVIEW_TAB = "ToReview";
	private static final String TO_CORRECT_TAB = "ToCorrect";
	private static final String MANUAL_TAB = "Manual";
	private static final String ADJUSTED_TAB = "Adjusted";
	
	private static final String CMD_AUTO_CORRECTED = "autocorrected";
	private static final String CMD_MANUALLY_CORRECTED = "manuallycorrected";
	private static final String CMD_NOT_CORRECTED = "notcorrected";
	private static final String CMD_ADJUSTED = "adjusted";
	private static final String CMD_TO_REVIEW = "toReview";
	private static final String CMD_SELECT = "select";
	
	private final TooledStackedPanel stackPanel;
	
	private FormLink saveTestsButton;
	private FlexiTableElement tableEl;
	private CorrectionAssessmentItemTableModel tableModel;

	private FlexiFiltersTab allTab;
	private DefaultFlexiColumnModel notCorrectedCol;
	private DefaultFlexiColumnModel manuallyCorrectedCol;
	
	private ToolsController toolsCtrl;
	private CloseableModalController cmc;
	private BulkScoreController bulkPointsCtrl;
	private ConfirmSaveTestsController confirmSaveTestCtrl;
	private CloseableCalloutWindowController toolsCalloutCtrl;
	private CorrectionIdentityAssessmentItemNavigationController identityItemCtrl;

	private int count = 0;
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
	private PdfModule pdfModule;
	
	public CorrectionAssessmentItemListController(UserRequest ureq, WindowControl wControl, TooledStackedPanel stackPanel,
			CorrectionOverviewModel model) {
		super(ureq, wControl, "correction_assessment_item_list");
		this.model = model;
		this.stackPanel = stackPanel;
		anonymous = qtiModule.getCorrectionWorkflow() == CorrectionWorkflow.anonymous;
		stackPanel.addListener(this);

		initForm(ureq);
		loadModel(true, false);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		if(model.hasErrors() && formLayout instanceof FormLayoutContainer) {
			String errorMsg = getErrorMessage();
			((FormLayoutContainer)formLayout).contextPut("errorMsg", errorMsg);
		}
		
		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ItemCols.section));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ItemCols.itemTitle, CMD_SELECT));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, ItemCols.itemKeywords, CMD_SELECT));
		Translator qti21Translator = Util.createPackageTranslator(AssessmentTestComposerController.class, getLocale());
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ItemCols.itemType,
				new QuestionTypeFlexiCellRenderer(qti21Translator)));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ItemCols.answered,
				new AnsweredFlexiCellRenderer()));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ItemCols.autoCorrected, CMD_AUTO_CORRECTED));
		manuallyCorrectedCol = new DefaultFlexiColumnModel(ItemCols.manuallyCorrected, CMD_MANUALLY_CORRECTED);
		columnsModel.addFlexiColumnModel(manuallyCorrectedCol);
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ItemCols.adjusted, CMD_ADJUSTED));
		notCorrectedCol = new DefaultFlexiColumnModel(ItemCols.notCorrected);
		columnsModel.addFlexiColumnModel(notCorrectedCol);
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ItemCols.toReview, CMD_TO_REVIEW,
				new ToReviewFlexiCellRenderer()));
		columnsModel.addFlexiColumnModel(new ActionsColumnModel(ItemCols.tools));
		
		tableModel = new CorrectionAssessmentItemTableModel(columnsModel, getLocale());
		tableEl = uifactory.addTableElement(getWindowControl(), "table", tableModel, getTranslator(), formLayout);
		tableEl.setElementCssClass("o_sel_correction_assessment_items_list");
		tableEl.setExportEnabled(true);
		tableEl.setAndLoadPersistedPreferences(ureq, "corr-assessment-item-list-v2.0");
		
		saveTestsButton = uifactory.addFormLink("save.tests", formLayout, Link.BUTTON);
		saveTestsButton.setElementCssClass("o_sel_correction_save_tests");
		
		initFilters();
		initFilterPresets();
		tableEl.setSelectedFilterTab(ureq, allTab);
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
	}
	
	private void loadModel(boolean reset, boolean lastSessions) {
		if(lastSessions) {
			model.loadLastSessions();
		}
		
		ResolvedAssessmentTest resolvedAssessmentTest = model.getResolvedAssessmentTest();
		List<AssessmentItemRef> itemRefs = resolvedAssessmentTest.getAssessmentItemRefs();
		List<CorrectionAssessmentItemRow> itemRows = new ArrayList<>(itemRefs.size());
		Map<String, CorrectionAssessmentItemRow> itemRefIdToRows = new HashMap<>();
		for(AssessmentItemRef itemRef:itemRefs) {
			ResolvedAssessmentItem resolvedAssessmentItem = resolvedAssessmentTest.getResolvedAssessmentItem(itemRef);
			AssessmentItem assessmentItem = resolvedAssessmentItem.getRootNodeLookup().extractIfSuccessful();
			ManifestMetadataBuilder metadata = model.getMetadata(itemRef);
			
			FormLink toolsLink = ActionsColumnModel.createLink(uifactory, getTranslator());
			CorrectionAssessmentItemRow itemRow = new CorrectionAssessmentItemRow(itemRef, assessmentItem, metadata, toolsLink);
			toolsLink.setUserObject(itemRow);
			itemRows.add(itemRow);
			itemRefIdToRows.put(itemRef.getIdentifier().toString(), itemRow);
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
		
		for(Map.Entry<Identity, AssessmentTestSession> entry:model.getLastSessions().entrySet()) {
			if(model.getLastSessions().containsKey(entry.getKey())) {
				TestSessionState testSessionState = model.getTestSessionStates().get(entry.getKey());
				Map<TestPlanNodeKey, ItemSessionState> itemSessionStates = (testSessionState == null ? null : testSessionState.getItemSessionStates());
				if(itemSessionStates != null) {
					for(Map.Entry<TestPlanNodeKey, ItemSessionState> itemEntry:itemSessionStates.entrySet()) {
						String itemRefIdentifier = itemEntry.getKey().getIdentifier().toString();
						CorrectionAssessmentItemRow row = itemRefIdToRows.get(itemRefIdentifier);
						if(row != null) {
							AssessmentItemSession itemSession = itemSessionMap
									.get(new ItemSessionKey(entry.getValue().getKey(), itemRefIdentifier));
							appendStatistics(row, itemSession, itemEntry.getValue());
						}
					}
				}
			}
		}
		
		boolean hasManualCorrection = false;
		for(CorrectionAssessmentItemRow itemRow:itemRows) {
			forgeRow(itemRow);
			hasManualCorrection |= itemRow.isManualCorrection();
		}

		tableModel.setObjects(itemRows);
		tableEl.reset(reset, reset, true);
		
		setColumnVisible(notCorrectedCol, hasManualCorrection);
		setColumnVisible(manuallyCorrectedCol, hasManualCorrection);
	}
	
	private void setColumnVisible(DefaultFlexiColumnModel col, boolean manualCorrections) {
		if(col == null) return;
		col.setAlwaysVisible(manualCorrections);
		col.setDefaultVisible(manualCorrections);
		tableEl.setColumnModelVisible(col, manualCorrections);
	}
	
	private void filterModel() {
		tableModel.filter(tableEl.getQuickSearchString(), tableEl.getFilters());
		tableEl.reset(true, true, true);
	}
	
	private void forgeRow(CorrectionAssessmentItemRow itemRow) {
		String val;
		String iconCssClass;
		String ariaTitle;
		boolean toCorrect = itemRow.isManualCorrection() && itemRow.getNumNotCorrected() > 0;
		if(toCorrect) {
			iconCssClass = "o_icon o_icon-fw o_icon_correction_to_correct";
			val = Integer.toString(itemRow.getNumNotCorrected());
			ariaTitle = translate("filter.to.correct");
		} else {
			iconCssClass = "o_icon o_icon-fw o_icon_ok";
			val = "";
			ariaTitle = translate("corrected");
		}
		FormLink notCorrected = uifactory.addFormLink("notcorrected_" + (count++), CMD_NOT_CORRECTED, val, tableEl, Link.LINK | Link.NONTRANSLATED);
		notCorrected.setIconLeftCSS(iconCssClass);
		notCorrected.setEnabled(toCorrect);
		notCorrected.setTitle(ariaTitle);
		itemRow.setNotCorrectedLink(notCorrected);
		notCorrected.setUserObject(itemRow);
	}
	
	private void appendStatistics(CorrectionAssessmentItemRow row, AssessmentItemSession itemSession, ItemSessionState itemSessionState) {
		row.addSession();
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
		
		boolean manualCorrection = model.isManualCorrection(row.getItemRef());
		row.setManualCorrection(manualCorrection);
		if(manualCorrection) {
			row.addManuallyCorrected();
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
		doUnlock();
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
					loadModel(false, true);
					filterModel();
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
				loadModel(false, true);
				filterModel();
				stackPanel.popController(identityItemCtrl);
			} else if(event instanceof SelectAssessmentItemEvent) {
				stackPanel.popController(identityItemCtrl);
				SelectAssessmentItemEvent saie = (SelectAssessmentItemEvent)event;
				doSelect(ureq, saie.getListEntry(), identityItemCtrl.getAssessmentEntryList());
			} else if(event == Event.CHANGED_EVENT) {
				fireEvent(ureq, Event.CHANGED_EVENT);
			}
		} else if(confirmSaveTestCtrl == source) {
			if(event == Event.DONE_EVENT) {
				doSaveTests(ureq);
			}
			cmc.deactivate();
			cleanUp();
		} else if(bulkPointsCtrl == source) {
			if(event == Event.DONE_EVENT) {
				loadModel(false, false);
				filterModel();
			}
			cmc.deactivate();
			cleanUp();
		} else if(toolsCalloutCtrl == source || toolsCtrl == source) {
			toolsCalloutCtrl.deactivate();
			cleanUp();
		} else if(cmc == source) {
			cleanUp();
		}
		super.event(ureq, source, event);
	}
	
	private void cleanUp() {
		removeAsListenerAndDispose(confirmSaveTestCtrl);
		removeAsListenerAndDispose(toolsCalloutCtrl);
		removeAsListenerAndDispose(bulkPointsCtrl);
		removeAsListenerAndDispose(toolsCtrl);
		removeAsListenerAndDispose(cmc);
		confirmSaveTestCtrl = null;
		toolsCalloutCtrl = null;
		bulkPointsCtrl = null;
		toolsCtrl = null;
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
				CorrectionAssessmentItemRow row = tableModel.getObject(se.getIndex());
				if(CMD_SELECT.equals(cmd)) {
					doSelect(ureq, row, r -> true);// accept all
				} else if(CMD_TO_REVIEW.equals(cmd)) {
					doSelect(ureq, row, AssessmentItemListEntry::isToReview);
				} else if(CMD_AUTO_CORRECTED.equals(cmd)) {
					doSelect(ureq, row, entry -> !row.isManualCorrection() && entry.getManualScore() == null);
				} else if(CMD_MANUALLY_CORRECTED.equals(cmd)) {
					doSelect(ureq, row, entry -> row.isManualCorrection());
				} else if(CMD_ADJUSTED.equals(cmd)) {
					doSelect(ureq, row, entry -> !row.isManualCorrection() && entry.getManualScore() != null);
				}
			} else if(event instanceof FlexiTableFilterTabEvent || event instanceof FlexiTableSearchEvent) {
				filterModel();
			}
		} else if(saveTestsButton == source) {
			doConfirmSaveTests(ureq);
		} else if(source instanceof FormLink link) {
			if("tools".equals(link.getCmd()) && link.getUserObject() instanceof CorrectionAssessmentItemRow row) {
				doOpenTools(ureq, row, link);
			} else if(CMD_NOT_CORRECTED.equals(link.getCmd()) && link.getUserObject() instanceof CorrectionAssessmentItemRow row) {
				doSelect(ureq, row, entry -> entry.getManualScore() == null);	
			}
		}
		super.formInnerEvent(ureq, source, event);
	}
	
	
	private void doOpenTools(UserRequest ureq, CorrectionAssessmentItemRow row, FormLink link) {
		removeAsListenerAndDispose(toolsCtrl);
		removeAsListenerAndDispose(toolsCalloutCtrl);

		toolsCtrl = new ToolsController(ureq, getWindowControl(), row);
		listenTo(toolsCtrl);

		toolsCalloutCtrl = new CloseableCalloutWindowController(ureq, getWindowControl(),
				toolsCtrl.getInitialComponent(), link, "", true, "");
		listenTo(toolsCalloutCtrl);
		toolsCalloutCtrl.activate();
	}
	
	private void doSetReviewFlag(CorrectionAssessmentItemRow row, boolean toReview) {
		String itemRefId = row.getItemRef().getIdentifier().toString();
		qtiService.setAssessmentItemSessionReviewFlag(model.getCourseEntry(), model.getSubIdent(), model.getTestEntry(), itemRefId, toReview);
		loadModel(false, false);
	}
	
	private void doBulkPoints(UserRequest ureq, CorrectionAssessmentItemRow row, Mode mode) {
		if(guardModalController(bulkPointsCtrl)) return;
		bulkPointsCtrl = new BulkScoreController(ureq, getWindowControl(), model, row, mode);
		listenTo(bulkPointsCtrl);
		
		String title = mode == Mode.ADD ? translate("point.add.title") : translate("point.set.title");
		cmc = new CloseableModalController(getWindowControl(), translate("close"), bulkPointsCtrl.getInitialComponent(), true, title);
		listenTo(cmc);
		cmc.activate();
	}
	
	private void doSelect(UserRequest ureq, CorrectionAssessmentItemRow row, Predicate<AssessmentItemListEntry> filter) {
		removeAsListenerAndDispose(identityItemCtrl);
		
		AssessmentItemRef itemRef = row.getItemRef();
		String itemRefIdentifier = row.getItemRef().getIdentifier().toString();
		List<AssessmentItemSession> allItemSessions = qtiService
				.getAssessmentItemSessions(model.getCourseEntry(), model.getSubIdent(), model.getTestEntry(), itemRefIdentifier);
		Map<AssessmentTestSession,AssessmentItemSession> testToItemSession = new HashMap<>();
		for(AssessmentItemSession itemSession:allItemSessions) {
			AssessmentTestSession testSession = itemSession.getAssessmentTestSession();
			testToItemSession.put(testSession, itemSession);
		}
		
		//reorder to match the list of identities
		List<Identity> assessedIdentities = model.getAssessedIdentities();
		List<AssessmentItemListEntry> reorderItemSessions = new ArrayList<>(assessedIdentities.size());
		for(Identity assessedIdentity:assessedIdentities) {
			AssessmentTestSession testSession = model.getLastSessions().get(assessedIdentity);
			TestSessionState testSessionState = model.getTestSessionStates().get(assessedIdentity);
			if(testSession != null && testSessionState != null) {
				List<TestPlanNode> nodes = testSessionState.getTestPlan().getNodes(itemRef.getIdentifier());
				if(nodes != null) {
					AssessmentItemSession itemSession = testToItemSession.get(testSession);
		
					String title;
					if(anonymous) {
						title = model.getAnonymizedName(assessedIdentity);
					} else {
						title = userManager.getUserDisplayName(assessedIdentity);
					}
					String labelWithType = translate("participant.name", title);
					AssessmentItemListEntry entry = new AssessmentItemListEntry(assessedIdentity, testSession, itemSession, itemRef, title, labelWithType, "o_icon_user");
					if(filter.test(entry)) {
						reorderItemSessions.add(entry);
					}
				}
			}
		}
		
		if(!reorderItemSessions.isEmpty()) {
			doSelect(ureq, reorderItemSessions.get(0), reorderItemSessions);
		} else {
			showWarning("waring.atleast.one");
		}
	}
	
	private void doSelect(UserRequest ureq, AssessmentItemListEntry listEntry, List<? extends AssessmentItemListEntry> selectedItemSessions) {
		removeAsListenerAndDispose(identityItemCtrl);
		doUnlock();
		
		AssessmentItemRef itemRef = listEntry.getItemRef();
		AssessmentItemSession reloadItemSession = null;
		if(listEntry.getItemSession() != null) {
			reloadItemSession = qtiService.getAssessmentItemSession(listEntry.getItemSession());
		}

		// lock on item, need to check the lock on identity / test
		String lockSubKey = "item-" + listEntry.getAssessedIdentity().getKey() + "-" + listEntry.getItemRef().getIdentifier().toString();
		OLATResourceable testOres = OresHelper.clone(model.getTestEntry().getOlatResource());
		lockResult = CoordinatorManager.getInstance().getCoordinator().getLocker().acquireLock(testOres, getIdentity(), lockSubKey, getWindow());
		if(lockResult.isSuccess()) {
			Identity assessedIdentity = listEntry.getAssessedIdentity();
			AssessmentTestSession candidateSession = listEntry.getTestSession();
			TestSessionState testSessionState = qtiService.loadTestSessionState(listEntry.getTestSession());

			List<TestPlanNode> nodes = testSessionState.getTestPlan().getNodes(itemRef.getIdentifier());
			if(nodes.size() == 1) {
				if(reloadItemSession == null) {
					TestPlanNode itemNode = nodes.get(0);
					String stringuifiedIdentifier = itemNode.getKey().getIdentifier().toString();
					ParentPartItemRefs parentParts = AssessmentTestHelper
							.getParentSection(itemNode.getKey(), testSessionState, model.getResolvedAssessmentTest());
					reloadItemSession = qtiService
							.getOrCreateAssessmentItemSession(candidateSession, parentParts, stringuifiedIdentifier, itemRef.getIdentifier().toString());
				}
				
				TestPlanNode itemNode = nodes.get(0);
				ItemSessionState itemSessionState = testSessionState.getItemSessionStates().get(itemNode.getKey());
				AssessmentItemCorrection itemCorrection = new AssessmentItemCorrection(assessedIdentity, 
						candidateSession, testSessionState, reloadItemSession, itemSessionState,
						itemRef, itemNode);
				itemCorrection.setItemSession(reloadItemSession);
				
				boolean running = candidateSession.getTerminationTime() == null;
				boolean readOnly = model.isAssessmentEntryDone(assessedIdentity) || running;
				identityItemCtrl = new CorrectionIdentityAssessmentItemNavigationController(ureq, getWindowControl(),
						model.getTestEntry(), model.getResolvedAssessmentTest(), itemCorrection, listEntry,
						selectedItemSessions, model, null, readOnly, running, anonymous);
				listenTo(identityItemCtrl);
				updatePreviousNext();
				
				stackPanel.pushController(listEntry.getLabel(), identityItemCtrl);
			}
		} else {
			String lockOwnerName = userManager.getUserDisplayName(lockResult.getOwner());
			String mgs = lockResult.isDifferentWindows() ? "warning.assessment.item.locked.same.user" : "warning.assessment.item.locked";
			showWarning(mgs, new String[] { lockOwnerName });
		}
	}
	
	private void updatePreviousNext() {
		if(identityItemCtrl != null) {
			AssessmentItemListEntry itemSession = identityItemCtrl.getAssessmentItemSession();
			List<? extends AssessmentItemListEntry> selectedItemSessions = identityItemCtrl.getAssessmentEntryList();
			boolean previousEnable = false;
			boolean nextEnable = false;
			
			int index = selectedItemSessions.indexOf(itemSession);
			if(index - 1 >= 0 && selectedItemSessions.size() > index - 1) {
				previousEnable = true;
			}
			if(index + 1 >= 0 && selectedItemSessions.size() > index + 1) {
				nextEnable = true;
			}
			identityItemCtrl.updatePreviousNext(previousEnable, nextEnable);
		}
	}
	
	private void doConfirmSaveTests(UserRequest ureq) {
		int notCorrectedQuestions = 0;
		List<CorrectionAssessmentItemRow> rows = tableModel.getObjects();
		for(CorrectionAssessmentItemRow row:rows) {
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
		List<AssessmentTestSession> rows = new ArrayList<>(model.getLastSessions().values());
		AssessmentTest assessmentTest = model.getResolvedAssessmentTest().getRootNodeLookup().extractIfSuccessful();
		fireEvent(ureq, new CompleteAssessmentTestSessionEvent(rows, assessmentTest, AssessmentEntryStatus.done));
	}
	
	private void doExportEssayPdfs(UserRequest ureq, CorrectionAssessmentItemRow row) {
		MediaResource resource = new EssaysPdfMediaResource(model, row.getItemRef(), anonymous, getLocale(), getIdentity(), getWindowControl());
		ureq.getDispatchResult().setResultingMediaResource(resource);
	}
	
	private class ToolsController extends BasicController {
		
		private final Link reviewAllLink;
		private final Link unreviewAllLink;
		private final Link addPointsLink;
		private final Link setScoreLink;
		private Link pdfExportLink;
		private final CorrectionAssessmentItemRow row;
		
		public ToolsController(UserRequest ureq, WindowControl wControl, CorrectionAssessmentItemRow row) {
			super(ureq, wControl);
			this.row = row;
			
			VelocityContainer mainVC = createVelocityContainer("tools");
			reviewAllLink = LinkFactory.createLink("tool.review.all", "review", getTranslator(), mainVC, this, Link.LINK);
			unreviewAllLink = LinkFactory.createLink("tool.unreview.all", "unreview", getTranslator(), mainVC, this, Link.LINK);
			addPointsLink = LinkFactory.createLink("tool.add.point", "add.points", getTranslator(), mainVC, this, Link.LINK);
			setScoreLink = LinkFactory.createLink("tool.set.score", "set.score", getTranslator(), mainVC, this, Link.LINK);
			if (pdfModule.isEnabled() && row.getItemType() == QTI21QuestionType.essay) {
				pdfExportLink = LinkFactory.createLink("tool.pdf.exprt", "pdf.exprot", getTranslator(), mainVC, this, Link.LINK);
			}
			
			putInitialPanel(mainVC);
		}

		@Override
		protected void event(UserRequest ureq, Component source, Event event) {
			if(reviewAllLink == source) {
				doSetReviewFlag(row, true);
				fireEvent(ureq, Event.DONE_EVENT);
			} else if(unreviewAllLink == source) {
				doSetReviewFlag(row, false);
				fireEvent(ureq, Event.DONE_EVENT);
			} else if(addPointsLink == source) {
				fireEvent(ureq, Event.DONE_EVENT);
				doBulkPoints(ureq, row, Mode.ADD);
			} else if(setScoreLink == source) {
				fireEvent(ureq, Event.DONE_EVENT);
				doBulkPoints(ureq, row, Mode.SET);
			} else if(pdfExportLink == source) {
				doExportEssayPdfs(ureq, row);
				fireEvent(ureq, Event.DONE_EVENT);
			}
		}
	}
}
