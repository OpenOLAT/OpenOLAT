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
package org.olat.course.assessment.ui.mode;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

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
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SelectionEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.StaticFlexiCellRenderer;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.stack.PopEvent;
import org.olat.core.gui.components.stack.TooledStackedPanel;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.gui.control.generic.modal.DialogBoxController;
import org.olat.core.gui.control.generic.modal.DialogBoxUIFactory;
import org.olat.core.util.StringHelper;
import org.olat.core.util.coordinate.CoordinatorManager;
import org.olat.core.util.event.GenericEventListener;
import org.olat.course.assessment.AssessmentMode;
import org.olat.course.assessment.AssessmentMode.Status;
import org.olat.course.assessment.AssessmentModeCoordinationService;
import org.olat.course.assessment.AssessmentModeManager;
import org.olat.course.assessment.AssessmentModeNotificationEvent;
import org.olat.course.assessment.model.TransientAssessmentMode;
import org.olat.course.assessment.ui.mode.AssessmentModeListModel.Cols;
import org.olat.course.assessment.ui.tool.ConfirmStopAssessmentModeController;
import org.olat.course.assessment.ui.tool.event.AssessmentModeStatusEvent;
import org.olat.repository.RepositoryEntry;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 12.12.2014<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
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
	private DialogBoxController startDialogBox;
	private DialogBoxController deleteDialogBox;
	private ConfirmStopAssessmentModeController stopCtrl;
	
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
		
		CoordinatorManager.getInstance().getCoordinator().getEventBus()
			.registerFor(this, getIdentity(), AssessmentModeNotificationEvent.ASSESSMENT_MODE_NOTIFICATION);
	}
	
	@Override
	protected void doDispose() {
		toolbarPanel.removeListener(this);
		//deregister for assessment mode
		CoordinatorManager.getInstance().getCoordinator().getEventBus()
			.deregisterFor(this, AssessmentModeNotificationEvent.ASSESSMENT_MODE_NOTIFICATION);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		if(secCallback.canEditAssessmentMode()) {
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
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(Cols.leadTime, new TimeCellRenderer(getTranslator())));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(Cols.followupTime, new TimeCellRenderer(getTranslator())));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(Cols.target, new TargetAudienceCellRenderer(getTranslator())));
		
		if(secCallback.canStartStopAssessment()) {
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel("start", Cols.start.ordinal(), "start",
				new BooleanCellRenderer(new StaticFlexiCellRenderer(translate("start"), "start", "btn btn-default btn-sm", "o_icon o_icon-fw o_as_mode_assessment"), null)));
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel("stop", Cols.stop.ordinal(), "stop",
				new BooleanCellRenderer(new StaticFlexiCellRenderer(translate("stop"), "stop"), null)));
		}
		if(secCallback.canEditAssessmentMode()) {
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel("edit", translate("edit"), "edit"));
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel("copy", translate("copy"), "copy"));
		}
		
		model = new AssessmentModeListModel(columnsModel, getTranslator(), assessmentModeCoordinationService);
		tableEl = uifactory.addTableElement(getWindowControl(), "table", model, 20, false, getTranslator(), formLayout);
		tableEl.setMultiSelect(secCallback.canEditAssessmentMode());
		tableEl.setSelectAllEnable(secCallback.canEditAssessmentMode());
	}
	
	private void loadModel() {
		List<AssessmentMode> modes = assessmentModeMgr.getAssessmentModeFor(entry);
		model.setObjects(modes);
		tableEl.reloadData();
		// don't show table and button if there is nothing
		tableEl.setVisible(!modes.isEmpty());
		if(deleteLink != null) {
			deleteLink.setVisible(!modes.isEmpty());
		}
	}

	@Override
	public void event(Event event) {
		 if (event instanceof AssessmentModeNotificationEvent) {
			 AssessmentModeNotificationEvent amne = (AssessmentModeNotificationEvent)event;
			 TransientAssessmentMode mode = amne.getAssessementMode();
			 if(mode.getRepositoryEntryKey().equals(entry.getKey())
					 && model.updateModeStatus(amne.getAssessementMode())) {
				 tableEl.getComponent().setDirty(true);
			 }
		}
	}

	@Override
	public void event(UserRequest ureq, Controller source, Event event) {
		if(editCtrl == source) {
			loadModel();
			toolbarPanel.popUpToController(this);
			removeAsListenerAndDispose(editCtrl);
			editCtrl = null;
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
		removeAsListenerAndDispose(stopCtrl);
		removeAsListenerAndDispose(cmc);
		stopCtrl = null;
		cmc = null;
	}

	@Override
	public void event(UserRequest ureq, Component source, Event event) {
		if(source == toolbarPanel) {
			if(event instanceof PopEvent) {
				PopEvent pe = (PopEvent)event;
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
					rows.add(model.getObject(i.intValue()));
				}
				doConfirmDelete(ureq, rows);
			}
		} else if(tableEl == source) {
			if(event instanceof SelectionEvent) {
				SelectionEvent se = (SelectionEvent)event;
				String cmd = se.getCommand();
				AssessmentMode row = model.getObject(se.getIndex());
				if("edit".equals(cmd)) {
					doEdit(ureq, row);
				} else if("copy".equals(cmd)) {
					doCopy(ureq, row);
				} else if("start".equals(cmd)) {
					doConfirmStart(ureq, row);
				} else if("stop".equals(cmd)) {
					doConfirmStop(ureq, row);
				}
			}
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}

	private void doAdd(UserRequest ureq) {
		removeAsListenerAndDispose(editCtrl);
		AssessmentMode newMode = assessmentModeMgr.createAssessmentMode(entry);
		editCtrl = new AssessmentModeEditController(ureq, getWindowControl(), entry.getOlatResource(), newMode);
		listenTo(editCtrl);
		toolbarPanel.pushController(translate("new.mode"), editCtrl);
	}
	
	private void doConfirmDelete(UserRequest ureq, List<AssessmentMode> modeToDelete) {
		StringBuilder sb = new StringBuilder();
		boolean canDelete = true;
		for(AssessmentMode mode:modeToDelete) {
			if(sb.length() > 0) sb.append(", ");
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
		newMode.setName(translate("copy.name", new String[] { modeToCopy.getName() }));
		
		AssessmentModeEditController modeEditCtrl = new AssessmentModeEditController(ureq, getWindowControl(), entry.getOlatResource(), newMode);
		modeEditCtrl.selectBusinessGroups(modeToCopy.getGroups());
		modeEditCtrl.selectAreas(modeToCopy.getAreas());
		modeEditCtrl.selectCurriculumElements(modeToCopy.getCurriculumElements());
		
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
			editCtrl = new AssessmentModeForLectureEditController(ureq, getWindowControl(), entry.getOlatResource(), mode);
		} else {
			editCtrl = new AssessmentModeEditController(ureq, getWindowControl(), entry.getOlatResource(), mode);
		}
		listenTo(editCtrl);
		
		String title = translate("form.mode.title", new String[]{ mode.getName() });
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
			cmc = new CloseableModalController(getWindowControl(), "close", stopCtrl.getInitialComponent(), true, title, true);
			cmc.activate();
			listenTo(cmc);
		}
	}
}
