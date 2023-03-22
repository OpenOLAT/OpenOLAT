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
import org.olat.core.gui.components.form.flexible.impl.elements.table.StickyActionColumnModel;
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
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.generic.closablewrapper.CalloutSettings;
import org.olat.core.gui.control.generic.closablewrapper.CloseableCalloutWindowController;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.gui.control.generic.wizard.StepsMainRunController;
import org.olat.core.gui.control.winmgr.Command;
import org.olat.core.gui.control.winmgr.CommandFactory;
import org.olat.core.gui.media.MediaResource;
import org.olat.core.helpers.Settings;
import org.olat.core.id.Identity;
import org.olat.core.id.IdentityEnvironment;
import org.olat.core.util.Formatter;
import org.olat.core.util.Util;
import org.olat.core.util.nodes.INode;
import org.olat.course.assessment.CourseAssessmentService;
import org.olat.course.assessment.IndentedNodeRenderer;
import org.olat.course.assessment.handler.AssessmentConfig;
import org.olat.course.assessment.ui.reset.ConfirmResetDataController;
import org.olat.course.assessment.ui.reset.ResetData1OptionsStep;
import org.olat.course.assessment.ui.reset.ResetDataContext;
import org.olat.course.assessment.ui.reset.ResetDataContext.ResetCourse;
import org.olat.course.assessment.ui.reset.ResetDataContext.ResetParticipants;
import org.olat.course.assessment.ui.reset.ResetDataFinishStepCallback;
import org.olat.course.assessment.ui.tool.AssessmentStatusCellRenderer;
import org.olat.course.learningpath.LearningPathConfigs;
import org.olat.course.learningpath.LearningPathConfigs.FullyAssessedResult;
import org.olat.course.learningpath.LearningPathService;
import org.olat.course.learningpath.manager.LearningPathCourseTreeModelBuilder;
import org.olat.course.learningpath.manager.LearningPathNodeAccessProvider;
import org.olat.course.learningpath.ui.LearningPathDataModel.LearningPathCols;
import org.olat.course.nodes.CourseNode;
import org.olat.course.nodes.STCourseNode;
import org.olat.course.run.scoring.ResetCourseDataHelper;
import org.olat.course.run.userview.UserCourseEnvironment;
import org.olat.course.run.userview.UserCourseEnvironmentImpl;
import org.olat.modules.assessment.AssessmentEntry;
import org.olat.modules.assessment.AssessmentService;
import org.olat.modules.assessment.ObligationOverridable;
import org.olat.modules.assessment.Overridable;
import org.olat.modules.assessment.Role;
import org.olat.modules.assessment.model.AssessmentEntryStatus;
import org.olat.modules.assessment.model.AssessmentObligation;
import org.olat.modules.assessment.ui.AssessedIdentityListController;
import org.olat.modules.assessment.ui.AssessmentToolSecurityCallback;
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
	private static final String CMD_RESET_FULLY_ASSESSED = "resetFullyAssessed";
	private static final String CMD_TOOLS = "tools";
	private static final String CMD_END_DATE = "endDate";
	private static final String CMD_OBLIGATION = "obligation";
	
	private final AtomicInteger counter = new AtomicInteger();
	private final TooledStackedPanel stackPanel;
	private SingleSelection excludedToggleEl;
	private FlexiTableElement tableEl;
	private LearningPathDataModel dataModel;
	private Link resetStatusLink;
	private Link resetDataLink;
	
	private CloseableModalController cmc;
	private CloseableCalloutWindowController ccwc;
	private Controller fullyAssessedResetCtrl;
	private Controller endDateEditCtrl;
	private Controller obligationEditCtrl;
	private StepsMainRunController resetDataCtrl;
	private ToolsController toolsCtrl;
	private ConfirmResetDataController confirmResetDataCtrl;
	
	private final UserCourseEnvironment userCourseEnv;
	private final RepositoryEntry courseEntry;
	private final boolean canEdit;
	
	@Autowired
	private AssessmentService assessmentService;
	@Autowired
	private CourseAssessmentService courseAssessmentService;
	@Autowired
	private LearningPathService learningPathService;
	@Autowired
	private LearningPathNodeAccessProvider learningPathNodeAccessProvider;

	public LearningPathListController(UserRequest ureq, WindowControl wControl, TooledStackedPanel stackPanel,
			UserCourseEnvironment userCourseEnv, boolean canEdit) {
		super(ureq, wControl, "identity_nodes");
		setTranslator(Util.createPackageTranslator(AssessedIdentityListController.class, getLocale(), getTranslator()));
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
		
		// Progress icon
		FlexiCellRenderer progressRenderer = new LearningPathProgressRenderer(courseEntry, getLocale(), true, false);
		DefaultFlexiColumnModel progressModel = new DefaultFlexiColumnModel(LearningPathCols.progress, progressRenderer);
		progressModel.setExportable(false);
		columnsModel.addFlexiColumnModel(progressModel);
		
		// Progress text
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(LearningPathCols.learningProgress));
		
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
		
		StickyActionColumnModel toolsColumnModel = new StickyActionColumnModel(LearningPathCols.tools);
		toolsColumnModel.setSortable(false);
		toolsColumnModel.setIconHeader("o_icon o_icon_actions o_icon-fws o_icon-lg");
		toolsColumnModel.setExportable(false);
		toolsColumnModel.setAlwaysVisible(true);
		columnsModel.addFlexiColumnModel(toolsColumnModel);

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
		
		if(canEdit) {
			resetDataLink = LinkFactory.createToolLink("reset.data", translate("reset.data"), this);
			resetDataLink.setIconLeftCSS("o_icon o_icon-lg o_icon_reset_data");
			stackPanel.addTool(resetDataLink, Align.right);
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
		if (iNode instanceof LearningPathTreeNode learningPathNode) {
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
		forgeTools(row);
		forgeProgress(row);
		forgeEndDate(row);
		forgeObligation(row);
		return row;
	}
	
	private void forgeTools(LearningPathRow row) {
		if(canEdit) {
			FormLink toolsLink = uifactory.addFormLink("o_tools_" + counter.getAndIncrement(), CMD_TOOLS, "", null, null, Link.NONTRANSLATED);
			toolsLink.setIconLeftCSS("o_icon o_icon_actions o_icon-fw o_icon-lg");
			toolsLink.setUserObject(row);
			row.setToolsLink(toolsLink);
		}
	}

	/**
	 * Inspired by LearningProgressRenderer
	 *
	 * @param row
	 */
	private void forgeProgress(LearningPathRow row) {
		if (Boolean.TRUE.equals(row.getFullyAssessed())) {
			if (!canEdit || row.getCourseNode() instanceof STCourseNode) {
				row.setProgressText(translate("fully.assessed"));
			} else {
				FormLink progressLink = uifactory.addFormLink("o_progress_" + counter.getAndIncrement(),
						CMD_RESET_FULLY_ASSESSED, "fully.assessed", tableEl);
				progressLink.setUserObject(row.getLearningPathNode());
				row.setProgressLink(progressLink);
			}
		} else if (AssessmentEntryStatus.notReady.equals(row.getStatus())) {
			// render nothing
		} else {
			forgeProgressPercent(row);
		}
	}

	private void forgeProgressPercent(LearningPathRow row) {
		if (row.getLearningPathNode().getCompletion() != null) {
			String progressPercent = String.valueOf(Math.round(row.getLearningPathNode().getCompletion() * 100d)) + "%";
			row.setProgressText(progressPercent);
		}
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
		AssessmentConfig assessmentConfig = courseAssessmentService.getAssessmentConfig(courseEntry, row.getCourseNode());
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
		} else if (source instanceof FormLink link) {
			if (CMD_RESET_FULLY_ASSESSED.equals(link.getCmd())) {
				doConfirmResetFullyAssessed(ureq, link);
			}else if (CMD_END_DATE.equals(link.getCmd())) {
				doEditEndDate(ureq, link);
			} else if (CMD_OBLIGATION.equals(link.getCmd()) && link.getUserObject() instanceof CourseNode node) {
				doEditObligation(ureq, node, link);
			} else if (CMD_TOOLS.equals(link.getCmd()) && link.getUserObject() instanceof LearningPathRow row) {
				doOpenTools(ureq, row, link);
			}
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if (source == fullyAssessedResetCtrl || source == endDateEditCtrl || source == obligationEditCtrl) {
			if (event == Event.DONE_EVENT) {
				loadModel();
			}
			ccwc.deactivate();
			cleanUp();
		} else if(resetDataCtrl == source) {
			if(event == Event.CANCELLED_EVENT || event == Event.DONE_EVENT || event == Event.CHANGED_EVENT) {
				getWindowControl().pop();
				if(event == Event.DONE_EVENT || event == Event.CHANGED_EVENT) {
					loadModel();
				}
				cleanUp();
			}
		} else if(source == confirmResetDataCtrl) {
			if (event == Event.DONE_EVENT) {
				doResetDataCourseNode(ureq, confirmResetDataCtrl.getDataContext());
				loadModel();
			}
			cmc.deactivate();
			cleanUp();
		} else if(source == toolsCtrl) {
			if(event == Event.CLOSE_EVENT) {
				ccwc.deactivate();
				cleanUp();
			}
		} else if (source == ccwc || source == cmc) {
			cleanUp();
		}
		super.event(ureq, source, event);
	}

	private void cleanUp() {
		removeAsListenerAndDispose(fullyAssessedResetCtrl);
		removeAsListenerAndDispose(confirmResetDataCtrl);
		removeAsListenerAndDispose(obligationEditCtrl);
		removeAsListenerAndDispose(endDateEditCtrl);
		removeAsListenerAndDispose(resetDataCtrl);
		removeAsListenerAndDispose(ccwc);
		removeAsListenerAndDispose(cmc);
		fullyAssessedResetCtrl = null;
		confirmResetDataCtrl = null;
		obligationEditCtrl = null;
		endDateEditCtrl = null;
		resetDataCtrl = null;
		ccwc = null;
		cmc = null;
	}

	@Override
	public void event(UserRequest ureq, Component source, Event event) {
		if (source == resetStatusLink) {
			doResetStatus();
		} else if(source == resetDataLink) {
			doResetData(ureq);
		}
		super.event(ureq, source, event);
	}
	
	private void doConfirmResetFullyAssessed(UserRequest ureq, FormLink link) {
		removeAsListenerAndDispose(ccwc);
		removeAsListenerAndDispose(fullyAssessedResetCtrl);
		
		LearningPathTreeNode lpTreeNode = (LearningPathTreeNode)link.getUserObject();
		fullyAssessedResetCtrl = new FullyAssessedResetController(ureq, getWindowControl(), userCourseEnv, lpTreeNode);
		listenTo(fullyAssessedResetCtrl);
		
		CalloutSettings settings = new CalloutSettings();
		ccwc = new CloseableCalloutWindowController(ureq, getWindowControl(), fullyAssessedResetCtrl.getInitialComponent(),
				link.getFormDispatchId(), "", true, "", settings);
		listenTo(ccwc);
		ccwc.activate();
	}
	
	private void doResetFullyAssessed(LearningPathTreeNode lpTreeNode) {
		CourseNode courseNode = lpTreeNode.getCourseNode();
		CourseNode parent = lpTreeNode.getParent() != null
				? ((LearningPathTreeNode)lpTreeNode.getParent()).getCourseNode()
				: null;
		LearningPathConfigs configs = learningPathService.getConfigs(courseNode, parent);
		FullyAssessedResult result = configs.isFullyAssessedOnConfirmation(false);
		result = LearningPathConfigs.fullyAssessed(true, result.isFullyAssessed(), result.isDone());
		learningPathNodeAccessProvider.updateFullyAssessed(courseNode, userCourseEnv, result);
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

	private void doEditObligation(UserRequest ureq, CourseNode courseNode, FormLink link) {
		removeAsListenerAndDispose(ccwc);
		removeAsListenerAndDispose(obligationEditCtrl);
		
		obligationEditCtrl = new ObligationEditController(ureq, getWindowControl(), courseEntry, courseNode, userCourseEnv, canEdit);
		listenTo(obligationEditCtrl);
		
		CalloutSettings settings = new CalloutSettings();
		ccwc = new CloseableCalloutWindowController(ureq, getWindowControl(), obligationEditCtrl.getInitialComponent(),
				link.getFormDispatchId(), "", true, "", settings);
		listenTo(ccwc);
		ccwc.activate();
		
	}

	private void doResetStatus() {
		Identity identityToReset = userCourseEnv.getIdentityEnvironment().getIdentity();
		List<AssessmentEntry> assessmentEntries = assessmentService.loadAssessmentEntriesByAssessedIdentity(identityToReset, courseEntry);
		for (AssessmentEntry assessmentEntry : assessmentEntries) {
			assessmentEntry.setFullyAssessed(null);
			assessmentEntry.setAssessmentStatus(null);
			assessmentService.updateAssessmentEntry(assessmentEntry);
		}
		userCourseEnv.getScoreAccounting().evaluateAll(true);
		loadModel();
	}
	
	private void doResetData(UserRequest ureq) {
		ResetDataContext dataContext = new ResetDataContext(courseEntry);
		dataContext.setResetCourse(ResetCourse.all);
		dataContext.setCourseNodes(List.of());
		dataContext.setResetParticipants(ResetParticipants.selected);
		dataContext.setSelectedParticipants(List.of(userCourseEnv.getIdentityEnvironment().getIdentity()));

		AssessmentToolSecurityCallback secCallback = AssessmentToolSecurityCallback.nothing();
		IdentityEnvironment identityEnv = new IdentityEnvironment(this.getIdentity(), ureq.getUserSession().getRoles());
		UserCourseEnvironmentImpl coachCourseEnv = new UserCourseEnvironmentImpl(identityEnv, userCourseEnv.getCourseEnvironment());
		ResetData1OptionsStep step = new ResetData1OptionsStep(ureq, dataContext, coachCourseEnv, secCallback, true, false);
		String title = translate("wizard.reset.data.title");
		ResetDataFinishStepCallback finishCallback = new ResetDataFinishStepCallback(dataContext, secCallback);
		resetDataCtrl = new StepsMainRunController(ureq, getWindowControl(), step, finishCallback, null, title, "");
		listenTo(resetDataCtrl);
		getWindowControl().pushAsModalDialog(resetDataCtrl.getInitialComponent());
	}
	
	private void doConfirmResetData(UserRequest ureq, CourseNode courseNode) {
		ResetDataContext dataContext = new ResetDataContext(courseEntry);
		if(courseNode.getParent() == null) {
			dataContext.setResetCourse(ResetCourse.all);
		} else {
			dataContext.setResetCourse(ResetCourse.elements);
			dataContext.setCourseNodes(List.of(courseNode));
		}
		dataContext.setResetParticipants(ResetParticipants.selected);
		dataContext.setSelectedParticipants(List.of(userCourseEnv.getIdentityEnvironment().getIdentity()));
		confirmResetDataCtrl = new ConfirmResetDataController(ureq, getWindowControl(), dataContext, null);
		listenTo(confirmResetDataCtrl);
		
		String title = translate("reset.data.title", courseNode.getShortTitle());
		cmc = new CloseableModalController(getWindowControl(), null, confirmResetDataCtrl.getInitialComponent(), true, title, true);
		listenTo(cmc);
		cmc.activate();
	}
	
	private void doResetDataCourseNode(UserRequest ureq, ResetDataContext dataContext) {
		List<Identity> identities = dataContext.getSelectedParticipants();
		ResetCourseDataHelper resetCourseNodeHelper = new ResetCourseDataHelper(userCourseEnv.getCourseEnvironment());
		MediaResource archiveResource = null;
		if(dataContext.getResetCourse() == ResetCourse.all) {
			archiveResource = resetCourseNodeHelper.resetCourse(identities, getIdentity(), Role.coach);
		} else if(!dataContext.getCourseNodes().isEmpty()) {
			archiveResource = resetCourseNodeHelper.resetCourseNodes(identities, dataContext.getCourseNodes(), false, getIdentity(), Role.coach);
		}
		if(archiveResource != null) {
			Command downloadCmd = CommandFactory.createDownloadMediaResource(ureq, archiveResource);
			getWindowControl().getWindowBackOffice().sendCommandTo(downloadCmd);
		}
	}
	
	private void doOpenTools(UserRequest ureq, LearningPathRow row, FormLink link) {
		removeAsListenerAndDispose(toolsCtrl);
		removeAsListenerAndDispose(ccwc);

		toolsCtrl = new ToolsController(ureq, getWindowControl(), row);
		listenTo(toolsCtrl);

		ccwc = new CloseableCalloutWindowController(ureq, getWindowControl(),
				toolsCtrl.getInitialComponent(), link.getFormDispatchId(), "", true, "");
		listenTo(ccwc);
		ccwc.activate();
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}
	
	private class ToolsController extends BasicController {
		
		private Link resetNodeDataLink;
		private Link editObligationLink;
		private Link resetFullyAssessedLink;
		
		private final LearningPathRow row;
		
		public ToolsController(UserRequest ureq, WindowControl wControl, LearningPathRow row) {
			super(ureq, wControl, Util.createPackageTranslator(AssessedIdentityListController.class, ureq.getLocale()));
			this.row = row;
			
			VelocityContainer mainVC = createVelocityContainer("identity_nodes_tools");
			
			if (Boolean.TRUE.equals(row.getFullyAssessed()) && canEdit && !(row.getCourseNode() instanceof STCourseNode)) {
				resetFullyAssessedLink = LinkFactory.createLink("reset.fully.assessed.link", "reset.fully.assessed.link", getTranslator(), mainVC, this, Link.LINK);
				resetFullyAssessedLink.setIconLeftCSS( "o_icon o_icon-fw o_icon_activate");
			}
			if (isObligationOverridableOpenable(row)) {
				editObligationLink = LinkFactory.createLink("edit.obligation", "edit.obligation", getTranslator(), mainVC, this, Link.LINK);
				editObligationLink.setIconLeftCSS( "o_icon o_icon-fw o_icon_edit");
			}
			
			resetNodeDataLink = LinkFactory.createLink("reset.data", "reset.data", getTranslator(), mainVC, this, Link.LINK);
			resetNodeDataLink.setIconLeftCSS( "o_icon o_icon-fw o_icon_reset_data");

			putInitialPanel(mainVC);
		}

		@Override
		protected void event(UserRequest ureq, Component source, Event event) {
			fireEvent(ureq, Event.CLOSE_EVENT);
			if(resetFullyAssessedLink == source) {
				doResetFullyAssessed(row.getLearningPathNode());
			} else if(resetNodeDataLink == source) {
				doConfirmResetData(ureq, row.getCourseNode());
			} else if(editObligationLink == source) {
				doEditObligation(ureq, row.getCourseNode(), row.getToolsLink());
			}
		}
	}
}
