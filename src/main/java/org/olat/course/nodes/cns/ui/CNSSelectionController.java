/**
 * <a href="https://www.openolat.org">
 * OpenOLAT - Online Learning and Training</a><br>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at the
 * <a href="https://www.apache.org/licenses/LICENSE-2.0">Apache homepage</a>
 * <p>
 * Unless required by applicable law or agreed to in writing,<br>
 * software distributed under the License is distributed on an "AS IS" BASIS, <br>
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
 * See the License for the specific language governing permissions and <br>
 * limitations under the License.
 * <p>
 * Initial code contributed and copyrighted by<br>
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.course.nodes.cns.ui;

import java.util.ArrayList;
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
import org.olat.core.gui.components.form.flexible.impl.elements.ComponentWrapperElement;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiTableCssDelegate;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableComponentDelegate;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableRendererType;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SelectionEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.StaticFlexiCellRenderer;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.panel.InfoPanel;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.gui.control.generic.confirmation.ConfirmationController;
import org.olat.core.gui.control.generic.lightbox.LightboxController;
import org.olat.core.util.Util;
import org.olat.core.util.nodes.INode;
import org.olat.course.assessment.IndentedNodeRenderer;
import org.olat.course.learningpath.LearningPathStatus;
import org.olat.course.learningpath.ui.LearningPathListController;
import org.olat.course.learningpath.ui.LearningPathStatusCellRenderer;
import org.olat.course.nodes.CNSCourseNode;
import org.olat.course.nodes.CourseNode;
import org.olat.course.nodes.cns.CNSEnvironment;
import org.olat.course.nodes.cns.ui.CNSSelectionDataModel.SelectionCols;
import org.olat.course.nodes.st.Overview;
import org.olat.course.nodes.st.Overview.Builder;
import org.olat.course.nodes.st.OverviewController;
import org.olat.course.nodes.st.OverviewFactory;
import org.olat.course.run.scoring.AssessmentEvaluation;
import org.olat.course.run.userview.UserCourseEnvironment;
import org.olat.modules.assessment.model.AssessmentObligation;
import org.olat.repository.RepositoryEntry;

/**
 * 
 * Initial date: 10 Sep 2024<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
public class CNSSelectionController extends FormBasicController implements FlexiTableComponentDelegate {

	private static final String CMD_SELECT = "select";
	private static final String CMD_DETAILS = "details";
	
	private InfoPanel configPanel;
	private CNSSelectionDataModel selectionDataModel;
	private FlexiTableElement selectionTableEl;
	
	private LightboxController lightboxCtrl;
	private CNSSelectionDetailController detailCtrl;
	private CloseableModalController cmc;
	private ConfirmationController selectConfirmationCtrl;
	
	private final UserCourseEnvironment userCourseEnv;
	private final RepositoryEntry courseEntry;
	private final CNSCourseNode courseNode;
	private final List<CourseNode> childNodes;
	private final CNSEnvironment cnsEnv;
	private final List<Controller> overviewCtrls = new ArrayList<>();
	private final OverviewFactory overviewFactory;
	private final CNSSelectionStatusRenderer statusRenderer;
	private final int requiredSelections;
	private int numSelections;
	private boolean moreSelectionsRequired;

	public CNSSelectionController(UserRequest ureq, WindowControl wControl, CNSCourseNode courseNode, UserCourseEnvironment userCourseEnv, CNSEnvironment cnsEnv) {
		super(ureq, wControl, "selection");
		setTranslator(Util.createPackageTranslator(LearningPathListController.class, getLocale(), getTranslator()));
		this.userCourseEnv = userCourseEnv;
		courseEntry = userCourseEnv.getCourseEnvironment().getCourseGroupManager().getCourseEntry();
		this.courseNode = courseNode;
		childNodes = CNSUIFactory.getChildNodes(courseNode);
		this.cnsEnv = cnsEnv;
		overviewFactory = new OverviewFactory(userCourseEnv, null, null, false);
		statusRenderer = new CNSSelectionStatusRenderer(false);
		
		requiredSelections = Integer.valueOf(courseNode.getModuleConfiguration().getStringValue(CNSCourseNode.CONFIG_KEY_REQUIRED_SELECTIONS));
		
		initForm(ureq);
		loadModel(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		configPanel = new InfoPanel("configs");
		configPanel.setTitle(translate("config.overview.selection.title"));
		configPanel.setInformations(CNSUIFactory.getConfigMessageParticipant(getTranslator(), requiredSelections));
		configPanel.setPersistedStatusId(ureq, "cns-selection-config-" + courseEntry.getKey() + "::" + courseNode.getIdent());
		formLayout.add("config", new ComponentWrapperElement(configPanel));
		
		FlexiTableColumnModel selectionColumnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		
		DefaultFlexiColumnModel nodeModel = new DefaultFlexiColumnModel(SelectionCols.courseNode);
		IndentedNodeRenderer intendedNodeRenderer = new IndentedNodeRenderer();
		intendedNodeRenderer.setIndentationEnabled(false);
		nodeModel.setCellRenderer(new StaticFlexiCellRenderer(CMD_DETAILS, intendedNodeRenderer));
		nodeModel.setAlwaysVisible(true);
		selectionColumnsModel.addFlexiColumnModel(nodeModel);
		
		selectionColumnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(SelectionCols.status, new LearningPathStatusCellRenderer()));
		selectionColumnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(SelectionCols.select));
		
		selectionDataModel = new CNSSelectionDataModel(selectionColumnsModel);
		selectionTableEl = uifactory.addTableElement(getWindowControl(), "selectionTable", selectionDataModel, 20, false, getTranslator(), formLayout);
		
		selectionTableEl.setAvailableRendererTypes(FlexiTableRendererType.custom, FlexiTableRendererType.classic);
		selectionTableEl.setRendererType(FlexiTableRendererType.custom);
		VelocityContainer rowVC = createVelocityContainer("selection_row");
		rowVC.setDomReplacementWrapperRequired(false);
		selectionTableEl.setRowRenderer(rowVC, this);
		selectionTableEl.setCssDelegate(new CNSSelectionCssDelegate());
	}
	
	private void loadModel(UserRequest ureq) {
		overviewCtrls.forEach(this::removeAsListenerAndDispose);
		overviewCtrls.clear();
		numSelections = 0;
		
		Map<String, AssessmentEvaluation> nodeIdentToAssessmentEvaluation = cnsEnv.getNodeIdentToAssessmentEvaluation(childNodes);
		
		List<CNSSelectionRow> rows = new ArrayList<>(courseNode.getChildCount());
		for (int i = 0; i < courseNode.getChildCount(); i++) {
			INode childNode = courseNode.getChildAt(i);
			if (childNode instanceof CourseNode courseNode) {
				CNSSelectionRow row = new CNSSelectionRow(courseNode);
				
				AssessmentEvaluation assessmentEvaluation = nodeIdentToAssessmentEvaluation.get(childNode.getIdent());
				boolean selected = assessmentEvaluation != null
							&& assessmentEvaluation.getObligation() != null
							&& assessmentEvaluation.getObligation().getCurrent() != null
							&& AssessmentObligation.excluded != assessmentEvaluation.getObligation().getCurrent();
				row.setSelected(selected);
				
				if (row.isSelected()) {
					LearningPathStatus learningPathStatus = LearningPathStatus.of(assessmentEvaluation);
					row.setLearningPathStatus(learningPathStatus);
					numSelections++;
				}
				
				rows.add(row);
			}
		}
		
		moreSelectionsRequired = numSelections < requiredSelections;
		
		for (CNSSelectionRow row : rows) {
			forgeOverview(ureq, row);
			if (moreSelectionsRequired) {
				forgeSelectLink(row);
			}
		}
		
		selectionDataModel.setObjects(rows);
		selectionTableEl.reset(true, true, true);
		
		updateSelectionMessage();
		updateSelectionStatusUI();
	}

	private void forgeSelectLink(CNSSelectionRow row) {
		if (!row.isSelected()) {
			FormLink selectLink = uifactory.addFormLink("selectl_" + row.getCourseNode().getIdent(), CMD_SELECT, "select", null, flc, Link.LINK);
			selectLink.setUserObject(row);
			row.setSelectLink(selectLink);
		}
	}
	
	private void forgeOverview(UserRequest ureq, CNSSelectionRow row) {
		Builder overviewBuilder = Overview.builder();
		overviewBuilder.withGoToNodeLinkEnabled(false);
		overviewFactory.appendCourseNodeInfos(overviewBuilder, row.getCourseNode());
		overviewFactory.appendCourseStyleInfos(overviewBuilder, row.getCourseNode());
		overviewBuilder.withLearningPathStatus(row.getLearningPathStatus());
		Overview overview = overviewBuilder.build();
		
		List<Link> links = new ArrayList<>(2);
		Link detailsLink = LinkFactory.createCustomLink("o_cns_details_" + row.getCourseNode().getIdent(), CMD_DETAILS,
				null, Link.BUTTON + Link.NONTRANSLATED, null, this);
		detailsLink.setCustomDisplayText(translate("selection.node.details"));
		detailsLink.setUserObject(row);
		links.add(detailsLink);
		
		if (moreSelectionsRequired && !row.isSelected() && !userCourseEnv.isCourseReadOnly()) {
			Link selectLink = LinkFactory.createCustomLink("o_cns_select_" + row.getCourseNode().getIdent(), CMD_SELECT,
					null, Link.BUTTON + Link.NONTRANSLATED, null, this);
			selectLink.setCustomDisplayText(translate("select"));
			selectLink.setPrimary(true);
			selectLink.setUserObject(row);
			links.add(selectLink);
		}
		
		OverviewController overviewCtrl = new OverviewController(ureq, getWindowControl(), overview, null, links);
		listenTo(overviewCtrl);
		overviewCtrls.add(overviewCtrl);
		row.setOverviewCmp(overviewCtrl.getInitialComponent());
		
		// Add it to the flc to get the events
		flc.add("o_cns_overview_" + row.getCourseNode().getIdent(), new ComponentWrapperElement(overviewCtrl.getInitialComponent()));
	}

	private void updateSelectionMessage() {
		if (selectionDataModel.getObjects().size() < requiredSelections) {
			flc.contextPut("selectionWarning", translate("selections.msg.too.few.available"));
		} else if (numSelections < requiredSelections) {
			String message = translate("selections.msg.required.more", 
					String.valueOf(numSelections),
					String.valueOf(requiredSelections-numSelections),
					String.valueOf(requiredSelections));
			flc.contextPut("selectionWarning", message);
		} else {
			flc.contextRemove("selectionWarning");
		}
	}
	
	private void updateSelectionStatusUI() {
		CNSSelectionStatus status = getStatus();
		flc.contextPut("statusLabel", statusRenderer.render(getTranslator(), status));
	}
	
	private CNSSelectionStatus getStatus() {
		AssessmentEvaluation evaluation = userCourseEnv.getScoreAccounting().evalCourseNode(courseNode);
		if (evaluation != null && evaluation.getFullyAssessed() != null && evaluation.getFullyAssessed().booleanValue()) {
			return CNSSelectionStatus.done;
		}
		
		int numSelected = (int)selectionDataModel.getObjects().stream().filter(CNSSelectionRow::isSelected).count();
		if (numSelected >= requiredSelections) {
			return CNSSelectionStatus.selected;
		}
		
		return CNSSelectionStatus.inProgress;
	}
	
	@Override
	public Iterable<Component> getComponents(int row, Object rowObject) {
		List<Component> cmps = new ArrayList<>(1);
		if (rowObject instanceof CNSSelectionRow selectionRow) {
			if (selectionRow.getOverviewCmp() != null) {
				cmps.add(selectionRow.getOverviewCmp());
			}
		}
		return cmps;
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if (source == lightboxCtrl) {
			cleanUp();
		} else if (detailCtrl == source) {
			if (event == CNSSelectionDetailController.SELECT_EVENT) {
				CourseNode selectedCourseNode = detailCtrl.getRow().getCourseNode();
				lightboxCtrl.deactivate();
				cleanUp();
				doConfirmSelect(ureq, selectedCourseNode);
			}
		} else if (selectConfirmationCtrl == source) {
			if (event == Event.DONE_EVENT) {
				if (selectConfirmationCtrl.getUserObject() instanceof CourseNode selectedNode) {
					doSelect(ureq, selectedNode);
				}
			}
			cmc.deactivate();
			cleanUp();
		} else if (cmc == source) {
			cmc.deactivate();
			cleanUp();
		}
		super.event(ureq, source, event);
	}

	private void cleanUp() {
		removeAsListenerAndDispose(selectConfirmationCtrl);
		removeAsListenerAndDispose(cmc);
		removeAsListenerAndDispose(detailCtrl);
		removeAsListenerAndDispose(lightboxCtrl);
		selectConfirmationCtrl = null;
		cmc = null;
		detailCtrl = null;
		lightboxCtrl = null;
	}
	
	@Override
	public void event(UserRequest ureq, Component source, Event event) {
		if (source instanceof Link link) {
			String cmd = link.getCommand();
			if (link.getUserObject() instanceof CNSSelectionRow row) {
				if (CMD_DETAILS.equals(cmd)) {
					doOpenDetails(ureq, row);
				} else if (CMD_SELECT.equals(cmd)) {
					doConfirmSelect(ureq, row.getCourseNode());
				}
			}
		}
		super.event(ureq, source, event);
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (source == selectionTableEl) {
			if (event instanceof SelectionEvent selectionEvent) {
				String cmd = selectionEvent.getCommand();
				if (CMD_DETAILS.equals(cmd)) {
					CNSSelectionRow row = selectionDataModel.getObject(selectionEvent.getIndex());
					doOpenDetails(ureq, row);
				}
			}
		} else if (source instanceof FormLink link) {
			String cmd = link.getCmd();
			if (link.getUserObject() instanceof CNSSelectionRow row) {
				if (CMD_SELECT.equals(cmd)) {
					doConfirmSelect(ureq, row.getCourseNode());
				}
			}
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}
	
	private void doOpenDetails(UserRequest ureq, CNSSelectionRow row) {
		removeAsListenerAndDispose(detailCtrl);
		
		detailCtrl = new CNSSelectionDetailController(ureq, getWindowControl(), overviewFactory, row, userCourseEnv);
		listenTo(detailCtrl);
		
		lightboxCtrl = new LightboxController(ureq, getWindowControl(), detailCtrl);
		listenTo(lightboxCtrl);
		lightboxCtrl.activate();
	}

	private void doConfirmSelect(UserRequest ureq, CourseNode selectedNode) {
		selectConfirmationCtrl = new ConfirmationController(ureq, getWindowControl(), 
				translate("selection.select.confirm.text", selectedNode.getLongTitle()),
				null,
				translate("select"));
		selectConfirmationCtrl.setUserObject(selectedNode);
		listenTo(selectConfirmationCtrl);
		
		cmc = new CloseableModalController(getWindowControl(), translate("close"), selectConfirmationCtrl.getInitialComponent(),
				true, translate("selection.select.confirm.title"), true);
		listenTo(cmc);
		cmc.activate();
	}
	
	private void doSelect(UserRequest ureq, CourseNode selectedNode) {
		cnsEnv.select(selectedNode);
		loadModel(ureq);
		userCourseEnv.getScoreAccounting().evaluateAll(true);
		fireEvent(ureq, Event.CHANGED_EVENT);
	}
	
	private final class CNSSelectionCssDelegate extends DefaultFlexiTableCssDelegate {
		
		@Override
		public String getWrapperCssClass(FlexiTableRendererType type) {
			return null;
		}
		
		@Override
		public String getTableCssClass(FlexiTableRendererType type) {
			if (FlexiTableRendererType.custom == type) {
				return "o_cns_selection_table o_block_small_top";
			}
			return null;
		}
		
		@Override
		public String getRowCssClass(FlexiTableRendererType type, int pos) {
			return "o_cns_selection_row";
		}
	}

}
