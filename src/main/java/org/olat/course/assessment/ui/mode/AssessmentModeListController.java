/**
 * <a href="https://www.openolat.org">
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
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.course.assessment.ui.mode;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.olat.core.commons.fullWebApp.LockRequest;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.BooleanCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableComponent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableSearchEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SelectionEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.StaticFlexiCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.tab.FlexiFiltersTab;
import org.olat.core.gui.components.form.flexible.impl.elements.table.tab.FlexiFiltersTabFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.tab.FlexiTableFilterTabEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.tab.TabSelectionBehavior;
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
import org.olat.core.gui.control.generic.modal.DialogBoxController;
import org.olat.core.gui.control.generic.modal.DialogBoxUIFactory;
import org.olat.core.gui.media.MediaResource;
import org.olat.core.gui.media.NotFoundMediaResource;
import org.olat.core.logging.Tracing;
import org.olat.core.util.StringHelper;
import org.olat.core.util.coordinate.CoordinatorManager;
import org.olat.core.util.event.GenericEventListener;
import org.olat.course.assessment.AssessmentMode;
import org.olat.course.assessment.AssessmentMode.Status;
import org.olat.course.assessment.AssessmentModeCoordinationService;
import org.olat.course.assessment.AssessmentModeManager;
import org.olat.course.assessment.AssessmentModeNotificationEvent;
import org.olat.course.assessment.AssessmentModeToArea;
import org.olat.course.assessment.AssessmentModeToCurriculumElement;
import org.olat.course.assessment.AssessmentModeToGroup;
import org.olat.course.assessment.model.TransientAssessmentMode;
import org.olat.course.assessment.ui.mode.AssessmentModeListModel.Cols;
import org.olat.course.assessment.ui.tool.ConfirmStopAssessmentModeController;
import org.olat.course.assessment.ui.tool.event.AssessmentModeStatusEvent;
import org.olat.group.BusinessGroup;
import org.olat.group.area.BGArea;
import org.olat.modules.curriculum.CurriculumElement;
import org.olat.repository.RepositoryEntry;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 12.12.2014<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
public class AssessmentModeListController extends FormBasicController implements GenericEventListener {

	private FormLink addLink;
	private FormLink deleteLink;
	private FlexiTableElement tableEl;
	private AssessmentModeListModel model;
	private final TooledStackedPanel toolbarPanel;
	
	private Controller editCtrl;
	private CloseableModalController cmc;
	private ToolsController toolsCtrl;
	private DialogBoxController startDialogBox;
	private DialogBoxController deleteDialogBox;
	private ConfirmStopAssessmentModeController stopCtrl;
	private CloseableCalloutWindowController toolsCalloutCtrl;
	
	private final RepositoryEntry entry;
	private final AssessmentModeSecurityCallback secCallback;
	
	@Autowired
	private AssessmentModeManager assessmentModeMgr;
	@Autowired
	private AssessmentModeCoordinationService assessmentModeCoordinationService;
	
	public AssessmentModeListController(UserRequest ureq, WindowControl wControl, TooledStackedPanel toolbarPanel,
			RepositoryEntry entry, AssessmentModeSecurityCallback secCallback) {
		super(ureq, wControl, "mode_list");
		this.entry = entry;
		this.secCallback = secCallback;
		this.toolbarPanel = toolbarPanel;
		toolbarPanel.addListener(this);
		
		initForm(ureq);
		loadModel();
		initFiltersPresets(ureq);
		
		CoordinatorManager.getInstance().getCoordinator().getEventBus()
			.registerFor(this, getIdentity(), AssessmentModeNotificationEvent.ASSESSMENT_MODE_NOTIFICATION);
	}
	
	@Override
	protected void doDispose() {
		toolbarPanel.removeListener(this);
		//deregister for assessment mode
		CoordinatorManager.getInstance().getCoordinator().getEventBus()
			.deregisterFor(this, AssessmentModeNotificationEvent.ASSESSMENT_MODE_NOTIFICATION);
        super.doDispose();
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		boolean canEditAssessmentMode = secCallback.canEditAssessmentMode();
		if(canEditAssessmentMode) {
			addLink = uifactory.addFormLink("add", "add", "add.mode", null, formLayout, Link.BUTTON);
			addLink.setElementCssClass("o_sel_assessment_mode_add");
			addLink.setIconLeftCSS("o_icon o_icon_add");
			
			deleteLink = uifactory.addFormLink("delete", "delete", "delete.mode", null, formLayout, Link.BUTTON);
			deleteLink.setIconLeftCSS("o_icon o_icon_delete_item");
		}
		
		//add the table
		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(Cols.status, new ModeStatusCellRenderer(getTranslator())));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(Cols.name));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(Cols.begin));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(Cols.end));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(Cols.mode));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(Cols.leadTime, new TimeCellRenderer(getTranslator())));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(Cols.followupTime, new TimeCellRenderer(getTranslator())));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(Cols.target, new TargetAudienceCellRenderer(getTranslator())));
		
		if(secCallback.canStartStopAssessment()) {
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel("start.exam", Cols.start.ordinal(), "start",
				new BooleanCellRenderer(new StaticFlexiCellRenderer(translate("start"), "start", "btn btn-default btn-sm", "o_icon o_icon-fw o_icon_status_in_progress"), null)));
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel("stop.exam", Cols.stop.ordinal(), "stop",
				new BooleanCellRenderer(new StaticFlexiCellRenderer(translate("finish"), "stop","btn btn-default btn-sm", "o_icon o_icon-fw o_as_mode_stop"), null)));
		}
		
		DefaultFlexiColumnModel configSebCol = new DefaultFlexiColumnModel("table.header.config.seb", Cols.configSeb.ordinal(), "configSeb",
				new BooleanCellRenderer(new StaticFlexiCellRenderer("", "configSeb", null, "o_icon-fw o_icon_download", translate("table.header.config.seb.hint")), null));
		configSebCol.setHeaderTooltip(translate("table.header.config.seb.hint"));
		configSebCol.setAlwaysVisible(true);
		columnsModel.addFlexiColumnModel(configSebCol);
		
		if(secCallback.canEditAssessmentMode()) {
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel("edit", translate("edit"), "edit"));
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel("table.header.actions", Cols.toolsLink.ordinal(), "copy",
					new ToolsCellRenderer("table.header.actions", "copy")));
		}
		
		model = new AssessmentModeListModel(columnsModel, getTranslator(), assessmentModeCoordinationService);
		tableEl = uifactory.addTableElement(getWindowControl(), "table", model, 20, false, getTranslator(), formLayout);
		
		tableEl.setMultiSelect(canEditAssessmentMode);
		tableEl.setSelectAllEnable(canEditAssessmentMode);
		tableEl.setSearchEnabled(true);
		if(canEditAssessmentMode && deleteLink != null) {
			tableEl.addBatchButton(deleteLink);
		}
		initFiltersPresets(ureq);
	}
	
	private void loadModel() {
		List<AssessmentMode> modes = new ArrayList<>(assessmentModeMgr.getAssessmentModeFor(entry));
		// remove filtered assessment modes
		modes.removeIf(mode -> isExcludedByStatusFilter(mode.getStatus()) || isExcludedBySearchString(mode));

		model.setObjects(modes);
		tableEl.reloadData();
		if(deleteLink != null) {
			deleteLink.setVisible(!modes.isEmpty());
		}
	}

	private boolean isExcludedBySearchString(AssessmentMode mode) {
		return StringHelper.containsNonWhitespace(tableEl.getQuickSearchString()) && !mode.getName().toLowerCase().contains(tableEl.getQuickSearchString().toLowerCase());
	}

	private boolean isExcludedByStatusFilter(Status modeStatus) {
		return tableEl.getSelectedFilterTab() != null && !tableEl.getSelectedFilterTab().getId().equals("all") && !tableEl.getSelectedFilterTab().getId().equals(modeStatus.name());
	}

	@Override
	public void event(Event event) {
		 if (event instanceof AssessmentModeNotificationEvent amne) {
			 LockRequest request = amne.getAssessementMode();
			 if(request instanceof TransientAssessmentMode mode
					 && mode.getRepositoryEntryKey().equals(entry.getKey())
					 && model.updateModeStatus(mode)) {
				 tableEl.getComponent().setDirty(true);
			 }
		}
	}

	@Override
	public void event(UserRequest ureq, Controller source, Event event) {
		if(editCtrl == source) {
			toolbarPanel.popUpToController(this);
			removeAsListenerAndDispose(editCtrl);
			editCtrl = null;
			if(event == Event.CHANGED_EVENT || event == Event.DONE_EVENT) {
				loadModel();
				fireEvent(ureq, new AssessmentModeStatusEvent());
			}
		} else if(deleteDialogBox == source) {
			if(DialogBoxUIFactory.isYesEvent(event) || DialogBoxUIFactory.isOkEvent(event)) {
				@SuppressWarnings("unchecked")
				List<AssessmentMode> rows = (List<AssessmentMode>)deleteDialogBox.getUserObject();
				doDelete(rows);
			}
		} else if(startDialogBox == source) {
			if(DialogBoxUIFactory.isYesEvent(event) || DialogBoxUIFactory.isOkEvent(event)) {
				AssessmentMode row = (AssessmentMode)startDialogBox.getUserObject();
				doStart(ureq, row);
			}
		} else if(stopCtrl == source) {
			if(event == Event.DONE_EVENT) {
				loadModel();
				fireEvent(ureq, new AssessmentModeStatusEvent());
			}
			cmc.deactivate();
			cleanUp();
		} else if(cmc == source) {
			cleanUp();
		}
		super.event(ureq, source, event);
	}
	
	private void cleanUp() {
		removeAsListenerAndDispose(toolsCalloutCtrl);
		removeAsListenerAndDispose(toolsCtrl);
		removeAsListenerAndDispose(stopCtrl);
		removeAsListenerAndDispose(cmc);
		toolsCalloutCtrl = null;
		toolsCtrl = null;
		stopCtrl = null;
		cmc = null;
	}

	@Override
	public void event(UserRequest ureq, Component source, Event event) {
		if(source == toolbarPanel) {
			if(event instanceof PopEvent pe) {
				if(pe.getController() == editCtrl) {
					loadModel();
				}
			}
		} else {
			super.event(ureq, source, event);
		}
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(addLink == source) {
			doAdd(ureq);
		} else if(deleteLink == source) {
			Set<Integer> index = tableEl.getMultiSelectedIndex();
			if(index == null || index.isEmpty()) {
				showWarning("error.atleastone");
			} else {
				List<AssessmentMode> rows = new ArrayList<>(index.size());
				for(Integer i:index) {
					rows.add(model.getObject(i));
				}
				doConfirmDelete(ureq, rows);
			}
		} else if(tableEl == source) {
			if (event instanceof SelectionEvent se) {
				String cmd = se.getCommand();
				AssessmentMode row = model.getObject(se.getIndex());
				if("edit".equals(cmd)) {
					doEdit(ureq, row);
				} else if("copy".equals(cmd)) {
					doOpenTools(ureq, row);
				} else if("start".equals(cmd)) {
					doConfirmStart(ureq, row);
				} else if("stop".equals(cmd)) {
					doConfirmStop(ureq, row);
				} else if("configSeb".equals(cmd)) {
					doDownloadConfigSeb(ureq, row);
				}
			} else if (event instanceof FlexiTableSearchEvent
					|| event instanceof FlexiTableFilterTabEvent) {
				loadModel();
			}

		}
		super.formInnerEvent(ureq, source, event);
	}

	private void doOpenTools(UserRequest ureq, AssessmentMode mode) {
		toolsCtrl = new ToolsController(ureq, getWindowControl(), mode);
		listenTo(toolsCtrl);

		toolsCalloutCtrl = new CloseableCalloutWindowController(ureq, getWindowControl(),
				toolsCtrl.getInitialComponent(), "o-tools-" + mode.getKey(), "", true, "");
		listenTo(toolsCalloutCtrl);
		toolsCalloutCtrl.activate();
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}

	private List<Status> getDistinctModeStatus() {
		return model.getObjects().stream()
				.map(AssessmentMode::getStatus)
				.distinct()
				.toList();
	}

	private void initFiltersPresets(UserRequest ureq) {
		List<FlexiFiltersTab> tabs = new ArrayList<>();

		FlexiFiltersTab allTab = FlexiFiltersTabFactory.tabWithFilters("all", translate("all"),
				TabSelectionBehavior.clear, List.of());
		allTab.setFiltersExpanded(true);
		tabs.add(allTab);

		for (Status modeStatus : getDistinctModeStatus()) {
			AssessmentModeHelper helper = new AssessmentModeHelper(getTranslator());
			FlexiFiltersTab filterStatus = FlexiFiltersTabFactory.tabWithFilters(modeStatus.name(), helper.getStatusLabel(modeStatus),
					TabSelectionBehavior.clear, List.of());
			tabs.add(filterStatus);
		}

		tableEl.setFilterTabs(true, tabs);
		tableEl.setSelectedFilterTab(ureq, allTab);
	}

	private void doAdd(UserRequest ureq) {
		removeAsListenerAndDispose(editCtrl);
		AssessmentMode newMode = assessmentModeMgr.createAssessmentMode(entry);
		editCtrl = new AssessmentModeEditController(ureq, getWindowControl(), entry, newMode);
		listenTo(editCtrl);
		toolbarPanel.pushController(translate("new.mode"), editCtrl);
	}
	
	private void doConfirmDelete(UserRequest ureq, List<AssessmentMode> modeToDelete) {
		StringBuilder sb = new StringBuilder();
		boolean canDelete = true;
		for(AssessmentMode mode:modeToDelete) {
			if(mode == null) continue;
			
			if(StringHelper.containsNonWhitespace(sb.toString())) sb.append(", ");
			sb.append(mode.getName());
			
			Status status = mode.getStatus();
			if(status == Status.leadtime || status == Status.assessment || status == Status.followup) {
				canDelete = false;
			}
		}
		
		if(canDelete) {
			String names = StringHelper.escapeHtml(sb.toString());
			String title = translate("confirm.delete.title");
			String text = translate("confirm.delete.text", names);
			deleteDialogBox = activateYesNoDialog(ureq, title, text, deleteDialogBox);
			deleteDialogBox.setUserObject(modeToDelete);
		} else {
			showWarning("error.in.assessment");
		}
	}
	
	private void doDelete(List<AssessmentMode> modesToDelete) {
		for(AssessmentMode modeToDelete:modesToDelete) {
			assessmentModeMgr.delete(modeToDelete);
		}
		loadModel();
		tableEl.deselectAll();
	}
	
	private void doCopy(UserRequest ureq, AssessmentMode mode) {
		AssessmentMode modeToCopy = assessmentModeMgr.getAssessmentModeById(mode.getKey());
		AssessmentMode newMode = assessmentModeMgr.createAssessmentMode(modeToCopy);
		newMode.setName(translate("copy.name", modeToCopy.getName()));
		
		AssessmentModeEditController modeEditCtrl = new AssessmentModeEditController(ureq, getWindowControl(), entry, newMode);
		Set<AssessmentModeToGroup> assessmentModeToGroups = modeToCopy.getGroups();
		if(assessmentModeToGroups != null) {
			Set<BusinessGroup> businessGroups = assessmentModeToGroups.stream()
					.map(AssessmentModeToGroup::getBusinessGroup)
					.collect(Collectors.toSet());
			modeEditCtrl.setBusinessGroups(businessGroups);
		}
		
		Set<AssessmentModeToArea> assessmentModeToAreas = modeToCopy.getAreas();
		if(assessmentModeToAreas != null) {
			Set<BGArea> areas = assessmentModeToAreas.stream()
					.map(AssessmentModeToArea::getArea)
					.collect(Collectors.toSet());
			modeEditCtrl.setAreas(areas);
		}
		
		Set<AssessmentModeToCurriculumElement> assessmentModeToCurriculums = modeToCopy.getCurriculumElements();
		if(assessmentModeToCurriculums != null) {
			Set<CurriculumElement> curriculumElements = assessmentModeToCurriculums.stream()
					.map(AssessmentModeToCurriculumElement::getCurriculumElement)
					.collect(Collectors.toSet());
			modeEditCtrl.setCurriculumElements(curriculumElements);
		}
		
		listenTo(modeEditCtrl);
		toolbarPanel.pushController(newMode.getName(), modeEditCtrl);
		
		editCtrl = modeEditCtrl;
	}
	
	private void doEdit(UserRequest ureq, AssessmentMode mode) {
		removeAsListenerAndDispose(editCtrl);
		
		AssessmentMode reloadedMode = assessmentModeMgr.getAssessmentModeById(mode.getKey());
		if(reloadedMode == null) {
			showWarning("warning.assessment.mode.already.deleted");
			loadModel();
			return;
		} else if(reloadedMode.getLectureBlock() != null) {
			editCtrl = new AssessmentModeForLectureEditController(ureq, getWindowControl(), entry, mode);
		} else {
			editCtrl = new AssessmentModeEditController(ureq, getWindowControl(), entry, mode);
		}
		listenTo(editCtrl);
		
		String title = translate("form.mode.title", mode.getName());
		toolbarPanel.pushController(title, editCtrl);
	}
	
	private void doConfirmStart(UserRequest ureq, AssessmentMode mode) {
		String title = translate("confirm.start.title");
		String text = translate("confirm.start.text");
		startDialogBox = activateYesNoDialog(ureq, title, text, startDialogBox);
		startDialogBox.setUserObject(mode);
	}

	private void doStart(UserRequest ureq, AssessmentMode mode) {
		assessmentModeCoordinationService.startAssessment(mode);
		getLogger().info(Tracing.M_AUDIT, "Start assessment mode : {} ({}) in course: {} ({})",
				mode.getName(), mode.getKey(), entry.getDisplayname(), entry.getKey());
		loadModel();
		fireEvent(ureq, new AssessmentModeStatusEvent());
	}
	
	private void doConfirmStop(UserRequest ureq, AssessmentMode mode) {
		if(guardModalController(stopCtrl)) return;
		
		mode = assessmentModeMgr.getAssessmentModeById(mode.getKey());
		if(mode == null) {
			showWarning("warning.assessment.mode.already.deleted");
			loadModel();
		} else {
			stopCtrl = new ConfirmStopAssessmentModeController(ureq, getWindowControl(), mode);
			listenTo(stopCtrl);
			
			String title = translate("confirm.stop.title");
			cmc = new CloseableModalController(getWindowControl(), translate("close"), stopCtrl.getInitialComponent(), true, title, true);
			cmc.activate();
			listenTo(cmc);
		}
	}
	
	private void doDownloadConfigSeb(UserRequest ureq, AssessmentMode mode) {
		MediaResource resource;
		if(StringHelper.containsNonWhitespace(mode.getSafeExamBrowserConfigPList())) {
			resource = new SafeExamBrowserConfigurationMediaResource(mode.getSafeExamBrowserConfigPList());
		} else {
			resource = new NotFoundMediaResource();
		}
		ureq.getDispatchResult().setResultingMediaResource(resource);
	}

	private static class ToolsCellRenderer extends StaticFlexiCellRenderer {

		public ToolsCellRenderer(String label, String action) {
			super(null, action, false, false, null, "o_icon o_icon_actions o_icon-fw o_icon-lg", label);
		}

		@Override
		protected String getId(Object cellValue, int row, FlexiTableComponent source) {
			AssessmentMode assessmentMode = (AssessmentMode) source.getFormItem().getTableDataModel().getObject(row);
			return "o-tools-" + assessmentMode.getKey();
		}
	}

	private class ToolsController extends BasicController {

		private final Link copyLink;
		private final AssessmentMode assessmentMode;

		protected ToolsController(UserRequest ureq, WindowControl wControl, AssessmentMode assessmentMode) {
			super(ureq, wControl);
			this.assessmentMode = assessmentMode;

			VelocityContainer mainVC = createVelocityContainer("tools");

			List<String> links = new ArrayList<>();
			mainVC.contextPut("links", links);
			copyLink = LinkFactory.createLink("duplicate", "copy", getTranslator(), mainVC, this, Link.LINK);
			copyLink.setIconLeftCSS("o_icon o_icon-fw o_icon_copy");
			mainVC.put("duplicate", copyLink);
			links.add("duplicate");

			putInitialPanel(mainVC);
		}

		@Override
		protected void event(UserRequest ureq, Component source, Event event) {
			if (source == copyLink) {
				close();
				doCopy(ureq, assessmentMode);
			}
		}

		private void close() {
			toolsCalloutCtrl.deactivate();
			cleanUp();
		}
	}
}
