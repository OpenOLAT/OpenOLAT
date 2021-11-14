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
package org.olat.course.learningpath.ui;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.TreeNodeFlexiCellRenderer;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.stack.TooledController;
import org.olat.core.gui.components.stack.TooledStackedPanel;
import org.olat.core.gui.components.stack.TooledStackedPanel.Align;
import org.olat.core.gui.components.tree.GenericTreeModel;
import org.olat.core.gui.components.tree.TreeNode;
import org.olat.core.gui.components.util.SelectionValues;
import org.olat.core.gui.components.util.SelectionValues.SelectionValue;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.closablewrapper.CalloutSettings;
import org.olat.core.gui.control.generic.closablewrapper.CloseableCalloutWindowController;
import org.olat.core.helpers.Settings;
import org.olat.core.util.Formatter;
import org.olat.core.util.nodes.INode;
import org.olat.course.assessment.CourseAssessmentService;
import org.olat.course.assessment.IndentedNodeRenderer;
import org.olat.course.assessment.handler.AssessmentConfig;
import org.olat.course.assessment.ui.tool.AssessmentStatusCellRenderer;
import org.olat.course.learningpath.LearningPathService;
import org.olat.course.learningpath.manager.LearningPathCourseTreeModelBuilder;
import org.olat.course.learningpath.ui.LearningPathDataModel.LearningPathCols;
import org.olat.course.nodes.CourseNode;
import org.olat.course.run.userview.UserCourseEnvironment;
import org.olat.modules.assessment.AssessmentEntry;
import org.olat.modules.assessment.AssessmentService;
import org.olat.modules.assessment.ObligationOverridable;
import org.olat.modules.assessment.Overridable;
import org.olat.modules.assessment.model.AssessmentEntryStatus;
import org.olat.modules.assessment.model.AssessmentObligation;
import org.olat.repository.RepositoryEntry;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 16 Aug 2019<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class LearningPathListController extends FormBasicController implements TooledController {
	
	private static final String KEY_EXCLUDED_SHOW = "excluded.show";
	private static final String KEY_EXCLUDED_HIDE = "excluded.hide";
	private static final String CMD_END_DATE = "endDate";
	private static final String CMD_OBLIGATION = "obligation";
	
	private final AtomicInteger counter = new AtomicInteger();
	private final TooledStackedPanel stackPanel;
	private SingleSelection excludedToggleEl;
	private FlexiTableElement tableEl;
	private LearningPathDataModel dataModel;
	private Link resetStatusLink;
	
	private CloseableCalloutWindowController ccwc;
	private Controller endDateEditCtrl;
	private Controller obligationEditCtrl;
	
	private final UserCourseEnvironment userCourseEnv;
	private final RepositoryEntry courseEntry;
	private final boolean canEdit;
	
	@Autowired
	private AssessmentService assessmentService;
	@Autowired
	private CourseAssessmentService courseAssessmentService;
	@Autowired
	private LearningPathService learningPathService;

	public LearningPathListController(UserRequest ureq, WindowControl wControl, TooledStackedPanel stackPanel,
			UserCourseEnvironment userCourseEnv, boolean canEdit) {
		super(ureq, wControl, "identity_nodes");
		this.userCourseEnv = userCourseEnv;
		this.stackPanel = stackPanel;
		this.courseEntry = userCourseEnv.getCourseEnvironment().getCourseGroupManager().getCourseEntry();
		this.canEdit = canEdit;
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		if (canEdit) {
			SelectionValues excludedKV = new SelectionValues();
			excludedKV.add(new SelectionValue(KEY_EXCLUDED_HIDE, translate("excluded.hide"), "o_primary", true));
			excludedKV.add(new SelectionValue(KEY_EXCLUDED_SHOW, translate("excluded.show"), "o_primary", true));
			excludedToggleEl = uifactory.addButtonGroupSingleSelectHorizontal("excluded.toggle", formLayout, excludedKV);
			excludedToggleEl.select(excludedToggleEl.getKey(0), true);
			excludedToggleEl.addActionListener(FormEvent.ONCHANGE);
		}
		
		
		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();

		// Learning path status icon
		// Do not show before the css with the pathes is done
//		LearningPathStatusCellRenderer lpStatusRenderer = new LearningPathStatusCellRenderer(getLocale(), true, false);
//		DefaultFlexiColumnModel learningPathStatusModel = new DefaultFlexiColumnModel(LearningPathCols.learningPathStatus);
//		learningPathStatusModel.setCellRenderer(lpStatusRenderer);
//		columnsModel.addFlexiColumnModel(learningPathStatusModel);
		
		// Course node
		IndentedNodeRenderer intendedNodeRenderer = new IndentedNodeRenderer();
		intendedNodeRenderer.setIndentationEnabled(false);
		FlexiCellRenderer nodeRenderer = new TreeNodeFlexiCellRenderer(intendedNodeRenderer);
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(LearningPathCols.node, nodeRenderer));
		FlexiCellRenderer progressRenderer = new LearningPathProgressRenderer(getLocale(), true, false);
		
		// Progress icon
		DefaultFlexiColumnModel progressModel = new DefaultFlexiColumnModel(LearningPathCols.progress, progressRenderer);
		progressModel.setExportable(false);
		columnsModel.addFlexiColumnModel(progressModel);
		
		// Progress text
		LearningPathProgressRenderer learningProgressRenderer = new LearningPathProgressRenderer(getLocale(), false, true);
		DefaultFlexiColumnModel learningProgressModel = new DefaultFlexiColumnModel(LearningPathCols.learningProgress);
		learningProgressModel.setCellRenderer(learningProgressRenderer);
		columnsModel.addFlexiColumnModel(learningProgressModel);
		
		// Status
		FlexiCellRenderer statusRenderer = new AssessmentStatusCellRenderer(getTranslator(), false);
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(LearningPathCols.status, statusRenderer));
		
		// Course element configs
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(LearningPathCols.obligation));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(LearningPathCols.duration));
		
		// Dates
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(LearningPathCols.start));
		DefaultFlexiColumnModel firstVisitColumnModel = new DefaultFlexiColumnModel(LearningPathCols.firstVisit);
		firstVisitColumnModel.setDefaultVisible(false);
		columnsModel.addFlexiColumnModel(firstVisitColumnModel);
		DefaultFlexiColumnModel lastVisitColumnModel = new DefaultFlexiColumnModel(LearningPathCols.lastVisit);
		lastVisitColumnModel.setDefaultVisible(false);
		columnsModel.addFlexiColumnModel(lastVisitColumnModel);
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(LearningPathCols.end));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(LearningPathCols.fullyAssessedDate));

		dataModel = new LearningPathDataModel(columnsModel);
		tableEl = uifactory.addTableElement(getWindowControl(), "table", dataModel, 250, false, getTranslator(), formLayout);
		tableEl.setElementCssClass("o_lp_list");
		tableEl.setEmptyTableMessageKey("table.empty");
		tableEl.setAndLoadPersistedPreferences(ureq, "learning-path-list");
		tableEl.setBordered(true);
		tableEl.setNumOfRowsEnabled(false);
		
		loadModel();
		openNodes();
	}
	
	@Override
	public void initTools() {
		// Never enable this function in a productive environment. It may lead to corrupt data.
		if (Settings.isDebuging()) {
			resetStatusLink = LinkFactory.createToolLink("reset.all.status", translate("reset.all.status"), this);
			resetStatusLink.setIconLeftCSS("o_icon o_icon-lg o_icon_exclamation");
			stackPanel.addTool(resetStatusLink, Align.right);
		}
	}

	void loadModel() {
		userCourseEnv.getScoreAccounting().evaluateAll(true);
		LearningPathCourseTreeModelBuilder learningPathCourseTreeModelBuilder = new LearningPathCourseTreeModelBuilder(userCourseEnv);
		if (excludedToggleEl != null) {
			boolean showExcluded = excludedToggleEl.isKeySelected(KEY_EXCLUDED_SHOW);
			learningPathCourseTreeModelBuilder.setShowExcluded(showExcluded);
		}
		GenericTreeModel learningPathTreeModel = learningPathCourseTreeModelBuilder.build();
		List<LearningPathRow> rows = forgeRows(learningPathTreeModel);
		dataModel.setObjects(rows);
		tableEl.reset(true, true, true);
	}

	private List<LearningPathRow> forgeRows(GenericTreeModel learningPathTreeModel) {
		List<LearningPathRow> rows = new ArrayList<>();
		TreeNode rootNode = learningPathTreeModel.getRootNode();
		forgeRowAndChildren(rows, rootNode, null);
		return rows;
	}

	private void forgeRowAndChildren(List<LearningPathRow> rows, INode iNode, LearningPathRow parent) {
		if (iNode instanceof LearningPathTreeNode) {
			LearningPathTreeNode learningPathNode = (LearningPathTreeNode) iNode;
			forgeRowAndChildren(rows, learningPathNode, parent);
		}
	}

	private void forgeRowAndChildren(List<LearningPathRow> rows, LearningPathTreeNode learningPathNode,
			LearningPathRow parent) {
		LearningPathRow row = forgeRow(learningPathNode, parent);
		rows.add(row);

		int childCount = learningPathNode.getChildCount();
		for (int childIndex = 0; childIndex < childCount; childIndex++) {
			INode child = learningPathNode.getChildAt(childIndex);
			forgeRowAndChildren(rows, child, row);
		}
	}

	private LearningPathRow forgeRow(LearningPathTreeNode treeNode, LearningPathRow parent) {
		LearningPathRow row = new LearningPathRow(treeNode);
		row.setParent(parent);
		forgeEndDate(row);
		forgeObligation(row);
		return row;
	}

	private void forgeEndDate(LearningPathRow row) {
		Overridable<Date> endDate = row.getEndDate();
		if (endDate == null) {
			return;
		}
		if (!canEdit && !endDate.isOverridden()) {
			// Show date as plain text
			return;
		}
		
		if (endDate.getCurrent() != null) {
			Date currentEndDate = endDate.getCurrent();
			StringBuilder sb = new StringBuilder();
			sb.append(Formatter.getInstance(getLocale()).formatDateAndTime(currentEndDate));
			if (endDate.isOverridden()) {
				sb.append(" <i class='o_icon o_icon_info'> </i>");
			}
			FormLink endDateLink = uifactory.addFormLink("o_end_" + counter.getAndIncrement(), CMD_END_DATE,
					sb.toString(), null, null, Link.NONTRANSLATED);
			endDateLink.setUserObject(row.getCourseNode());
			row.setEndDateFormItem(endDateLink);
		}
	}
	
	private void forgeObligation(LearningPathRow row) {
		ObligationOverridable obligation = row.getObligation();
		
		if (isObligationOverridableOpenable(row)) {
			StringBuilder sb = new StringBuilder();
			if (AssessmentObligation.mandatory == obligation.getCurrent()) {
				sb.append(translate("config.obligation.mandatory"));
			} else if (AssessmentObligation.optional == obligation.getCurrent()) {
				sb.append(translate("config.obligation.optional"));
			} else if (AssessmentObligation.excluded == obligation.getCurrent()) {
				sb.append(translate("config.obligation.excluded"));
			}
			if (obligation.isOverridden()) {
				sb.append(" <i class='o_icon o_icon_info'> </i>");
			}
			FormLink formLink = uifactory.addFormLink("o_obli_" + counter.getAndIncrement(), CMD_OBLIGATION,
					sb.toString(), null, null, Link.NONTRANSLATED);
			formLink.setUserObject(row.getCourseNode());
			row.setObligationFormItem(formLink);
		} else {
			String translatedObligation = null;
			if (AssessmentObligation.mandatory == obligation.getCurrent()) {
				translatedObligation = translate("config.obligation.mandatory");
			} else if (AssessmentObligation.optional == obligation.getCurrent()) {
				translatedObligation = translate("config.obligation.optional");
			} else if (AssessmentObligation.excluded == obligation.getCurrent()) {
				translatedObligation = translate("config.obligation.excluded");
			}
			row.setTranslatedObligation(translatedObligation);
		}
	}

	private boolean isObligationOverridableOpenable(LearningPathRow row) {
		AssessmentConfig assessmentConfig = courseAssessmentService.getAssessmentConfig(row.getCourseNode());
		if (!assessmentConfig.isObligationOverridable()) {
			return false;
		}
		
		ObligationOverridable obligation = row.getObligation();
		if (!canEdit && !obligation.isOverridden()) {
			return false;
		}
		
		return true;
	}
	
	private void openNodes() {
		dataModel.closeAll();
		for (int rowIndex = 0; rowIndex < dataModel.getRowCount(); rowIndex ++) {
			LearningPathRow row = dataModel.getObject(rowIndex);
			boolean notOpen =  AssessmentObligation.optional == row.getObligation().getCurrent()
					|| (row.getFullyAssessed() != null && row.getFullyAssessed().booleanValue())
					|| (AssessmentEntryStatus.notReady == row.getStatus());
			boolean open = !notOpen;
			if (open) {
				dataModel.open(rowIndex);
			}
		}
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (source == excludedToggleEl) {
			loadModel();
		} else if (source instanceof FormLink) {
			FormLink link = (FormLink) source;
			if (CMD_END_DATE.equals(link.getCmd())) {
				doEditEndDate(ureq, link);
			} else if (CMD_OBLIGATION.equals(link.getCmd())) {
				doEditObligation(ureq, link);
			}
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if (source == endDateEditCtrl) {
			if (event == FormEvent.DONE_EVENT) {
				loadModel();
			}
			ccwc.deactivate();
			cleanUp();
		} else if (source == obligationEditCtrl) {
			if (event == FormEvent.DONE_EVENT) {
				loadModel();
			}
			ccwc.deactivate();
			cleanUp();
		} else if (source == ccwc) {
			cleanUp();
		}
		super.event(ureq, source, event);
	}

	private void cleanUp() {
		removeAsListenerAndDispose(endDateEditCtrl);
		removeAsListenerAndDispose(ccwc);
		endDateEditCtrl = null;
		ccwc = null;
	}

	@Override
	public void event(UserRequest ureq, Component source, Event event) {
		if (source == resetStatusLink) {
			doResetStatus();
		}
		super.event(ureq, source, event);
	}
	
	private void doEditEndDate(UserRequest ureq, FormLink link) {
		removeAsListenerAndDispose(ccwc);
		removeAsListenerAndDispose(endDateEditCtrl);
		
		CourseNode courseNode = (CourseNode)link.getUserObject();
		AssessmentEntry assessmentEntry = assessmentService.loadAssessmentEntry(
				userCourseEnv.getIdentityEnvironment().getIdentity(), courseEntry, courseNode.getIdent());
		
		endDateEditCtrl = new EndDateEditController(ureq, getWindowControl(), assessmentEntry, canEdit);
		listenTo(endDateEditCtrl);
		
		CalloutSettings settings = new CalloutSettings();
		ccwc = new CloseableCalloutWindowController(ureq, getWindowControl(), endDateEditCtrl.getInitialComponent(),
				link.getFormDispatchId(), "", true, "", settings);
		listenTo(ccwc);
		ccwc.activate();
	}

	private void doEditObligation(UserRequest ureq, FormLink link) {
		removeAsListenerAndDispose(ccwc);
		removeAsListenerAndDispose(obligationEditCtrl);
		
		CourseNode courseNode = (CourseNode)link.getUserObject();
		obligationEditCtrl = new ObligationEditController(ureq, getWindowControl(), courseEntry, courseNode, userCourseEnv, canEdit);
		listenTo(obligationEditCtrl);
		
		CalloutSettings settings = new CalloutSettings();
		ccwc = new CloseableCalloutWindowController(ureq, getWindowControl(), obligationEditCtrl.getInitialComponent(),
				link.getFormDispatchId(), "", true, "", settings);
		listenTo(ccwc);
		ccwc.activate();
		
	}

	private void doResetStatus() {
		List<AssessmentEntry> assessmentEntries = assessmentService.loadAssessmentEntriesByAssessedIdentity(getIdentity(), courseEntry);
		for (AssessmentEntry assessmentEntry : assessmentEntries) {
			assessmentEntry.setFullyAssessed(null);
			assessmentEntry.setAssessmentStatus(null);
			assessmentService.updateAssessmentEntry(assessmentEntry);
		}
		userCourseEnv.getScoreAccounting().evaluateAll(true);
		loadModel();
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}
}
