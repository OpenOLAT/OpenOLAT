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
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.stack.PopEvent;
import org.olat.core.gui.components.stack.TooledStackedPanel;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.generic.closablewrapper.CloseableCalloutWindowController;
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
import org.olat.ims.qti21.QTI21Module;
import org.olat.ims.qti21.QTI21Module.CorrectionWorkflow;
import org.olat.ims.qti21.QTI21Service;
import org.olat.ims.qti21.model.ParentPartItemRefs;
import org.olat.ims.qti21.model.xml.ManifestMetadataBuilder;
import org.olat.ims.qti21.ui.assessment.CorrectionAssessmentItemTableModel.ItemCols;
import org.olat.ims.qti21.ui.assessment.components.AutoCorrectedFlexiCellRenderer;
import org.olat.ims.qti21.ui.assessment.components.CorrectedFlexiCellRenderer;
import org.olat.ims.qti21.ui.assessment.components.NotCorrectedFlexiCellRenderer;
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

	private final TooledStackedPanel stackPanel;
	
	private FormLink saveTestsButton;
	private FlexiTableElement tableEl;
	private CorrectionAssessmentItemTableModel tableModel;

	private ToolsController toolsCtrl;
	private CloseableModalController cmc;
	private ConfirmSaveTestsController confirmSaveTestCtrl;
	private CloseableCalloutWindowController toolsCalloutCtrl;
	private CorrectionIdentityAssessmentItemNavigationController identityItemCtrl;

	private int counter = 0;
	private LockResult lockResult;
	private final boolean anonymous;
	private final CorrectionOverviewModel model;

	@Autowired
	private QTI21Module qtiModule;
	@Autowired
	private QTI21Service qtiService;
	@Autowired
	private UserManager userManager;
	
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
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ItemCols.itemTitle, "select"));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, ItemCols.itemKeywords, "select"));
		Translator qti21Translator = Util.createPackageTranslator(AssessmentTestComposerController.class, getLocale());
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ItemCols.itemType, new QuestionTypeFlexiCellRenderer(qti21Translator)));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ItemCols.answered, "answered"));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ItemCols.notAnswered, "notAnswered"));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ItemCols.autoCorrected, "autoCorrected", new AutoCorrectedFlexiCellRenderer()));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ItemCols.corrected, "corrected", new CorrectedFlexiCellRenderer()));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ItemCols.notCorrected, "notCorrected", new NotCorrectedFlexiCellRenderer()));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ItemCols.toReview, "toReview", new ToReviewFlexiCellRenderer()));
		DefaultFlexiColumnModel toolsCol = new DefaultFlexiColumnModel(ItemCols.tools);
		toolsCol.setAlwaysVisible(true);
		columnsModel.addFlexiColumnModel(toolsCol);
		
		tableModel = new CorrectionAssessmentItemTableModel(columnsModel, getLocale());
		tableEl = uifactory.addTableElement(getWindowControl(), "table", tableModel, getTranslator(), formLayout);
		tableEl.setElementCssClass("o_sel_correction_assessment_items_list");
		tableEl.setExportEnabled(true);
		tableEl.setAndLoadPersistedPreferences(ureq, "corr-assessment-item-list");
		
		saveTestsButton = uifactory.addFormLink("save.tests", formLayout, Link.BUTTON);
		saveTestsButton.setElementCssClass("o_sel_correction_save_tests");
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
			
			FormLink toolsLink = uifactory.addFormLink("tools_" + (++counter), "tools", "", null, null, Link.NONTRANSLATED);
			toolsLink.setIconLeftCSS("o_icon o_icon_actions o_icon-lg");
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

		tableModel.setObjects(itemRows);
		tableEl.reset(reset, reset, true);
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
			if(manualScore == null) {
				row.addNotCorrected();
			} else {
				row.addCorrected();
			}
		} else if(manualScore != null) {
			row.addCorrected();
		} else {
			row.addAutoCorrected();
		}
	}

	@Override
	protected void doDispose() {
		stackPanel.removeListener(this);
		doUnlock();
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
		removeAsListenerAndDispose(toolsCtrl);
		removeAsListenerAndDispose(cmc);
		confirmSaveTestCtrl = null;
		toolsCalloutCtrl = null;
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
			if(event instanceof SelectionEvent) {
				SelectionEvent se = (SelectionEvent)event;
				String cmd = se.getCommand();
				CorrectionAssessmentItemRow row = tableModel.getObject(se.getIndex());
				if("select".equals(cmd)) {
					doSelect(ureq, row, r -> true);// accept all
				} else if("answered".equals(cmd)) {
					doSelect(ureq, row, new ResponsedPredicate(row, true));// accept all
				} else if("notAnswered".equals(cmd)) {
					doSelect(ureq, row, new ResponsedPredicate(row, false));// accept all
				} else if("autoCorrected".equals(cmd)) {
					doSelect(ureq, row, entry -> !row.isManualCorrection() && entry.getManualScore() == null);
				}  else if("corrected".equals(cmd)) {
					doSelect(ureq, row, entry -> entry.getManualScore() != null);
				} else if("notCorrected".equals(cmd)) {
					doSelect(ureq, row, entry -> row.isManualCorrection() && entry.getManualScore() == null);
				} else if("toReview".equals(cmd)) {
					doSelect(ureq, row, AssessmentItemListEntry::isToReview);
				}
			}
		} else if(saveTestsButton == source) {
			doConfirmSaveTests(ureq);
		} else if(source instanceof FormLink) {
			FormLink link = (FormLink)source;
			if("tools".equals(link.getCmd())) {
				doOpenTools(ureq, (CorrectionAssessmentItemRow)link.getUserObject(), link);
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
				toolsCtrl.getInitialComponent(), link.getFormDispatchId(), "", true, "");
		listenTo(toolsCalloutCtrl);
		toolsCalloutCtrl.activate();
	}
	
	private void doSetReviewFlag(CorrectionAssessmentItemRow row, boolean toReview) {
		String itemRefId = row.getItemRef().getIdentifier().toString();
		qtiService.setAssessmentItemSessionReviewFlag(model.getCourseEntry(), model.getSubIdent(), model.getTestEntry(), itemRefId, toReview);
		loadModel(false, false);
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
		int count = 1;
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
						title = translate("number.assessed.identity", new String[] { Integer.toString(count++)} );
					} else {
						title = userManager.getUserDisplayName(assessedIdentity);
					}
					AssessmentItemListEntry entry = new AssessmentItemListEntry(assessedIdentity, testSession, itemSession, itemRef, title, "o_icon_user");
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
							.getOrCreateAssessmentItemSession(candidateSession, parentParts, stringuifiedIdentifier);
				}
				
				TestPlanNode itemNode = nodes.get(0);
				ItemSessionState itemSessionState = testSessionState.getItemSessionStates().get(itemNode.getKey());
				AssessmentItemCorrection itemCorrection = new AssessmentItemCorrection(assessedIdentity, 
						candidateSession, testSessionState, reloadItemSession, itemSessionState,
						itemRef, itemNode);
				itemCorrection.setItemSession(reloadItemSession);

				boolean assessmentEntryDone = model.isAssessmentEntryDone(assessedIdentity);
				identityItemCtrl = new CorrectionIdentityAssessmentItemNavigationController(ureq, getWindowControl(),
						model.getTestEntry(), model.getResolvedAssessmentTest(), itemCorrection, listEntry,
						selectedItemSessions, model, null, assessmentEntryDone, true);
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
			String previousText = translate("previous.user");
			String nextText = translate("next.user");
			boolean previousEnable = false;
			boolean nextEnable = false;
			
			int index = selectedItemSessions.indexOf(itemSession);
			if(index - 1 >= 0 && selectedItemSessions.size() > index - 1) {
				previousText = selectedItemSessions.get(index - 1).getLabel();
				previousEnable = true;
			}
			if(index + 1 >= 0 && selectedItemSessions.size() > index + 1) {
				nextText = selectedItemSessions.get(index + 1).getLabel();
				nextEnable = true;
			}
			identityItemCtrl.updatePreviousNext(previousText, previousEnable, nextText, nextEnable);
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
		
		cmc = new CloseableModalController(getWindowControl(), "close", confirmSaveTestCtrl.getInitialComponent(),
				true, translate("save.tests"));
		listenTo(cmc);
		cmc.activate();
	}
	
	private void doSaveTests(UserRequest ureq) {
		List<AssessmentTestSession> rows = new ArrayList<>(model.getLastSessions().values());
		AssessmentTest assessmentTest = model.getResolvedAssessmentTest().getRootNodeLookup().extractIfSuccessful();
		fireEvent(ureq, new CompleteAssessmentTestSessionEvent(rows, assessmentTest, AssessmentEntryStatus.done));
	}
	
	private final class ResponsedPredicate implements Predicate<AssessmentItemListEntry> {
		
		private final boolean responded;
		private final CorrectionAssessmentItemRow row;
		
		public ResponsedPredicate(CorrectionAssessmentItemRow row, boolean responded) {
			this.row = row;
			this.responded = responded;
		}

		@Override
		public boolean test(AssessmentItemListEntry t) {
			TestSessionState testSessionState = model.getTestSessionStates().get(t.getAssessedIdentity());
			List<TestPlanNode> nodes = testSessionState.getTestPlan().getNodes(row.getItemRef().getIdentifier());
			if(!nodes.isEmpty()) {
				TestPlanNode itemNode = nodes.get(0);
				ItemSessionState itemSessionState = testSessionState.getItemSessionStates().get(itemNode.getKey());
				return itemSessionState != null && responded == itemSessionState.isResponded();
			}
			return false;
		}
	}
	
	private class ToolsController extends BasicController {
		
		private final Link reviewAllLink;
		private final Link unreviewAllLink;
		private final CorrectionAssessmentItemRow row;
		
		public ToolsController(UserRequest ureq, WindowControl wControl, CorrectionAssessmentItemRow row) {
			super(ureq, wControl);
			this.row = row;
			
			VelocityContainer mainVC = createVelocityContainer("tools");
			reviewAllLink = LinkFactory.createLink("tool.review.all", "review", getTranslator(), mainVC, this, Link.LINK);
			unreviewAllLink = LinkFactory.createLink("tool.unreview.all", "unreview", getTranslator(), mainVC, this, Link.LINK);
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
			}
		}

		@Override
		protected void doDispose() {
			//
		}
	}
}
