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
 * BPS Bildungsportal Sachsen GmbH, http://www.bps-system.de
 * <p>
 */
package de.bps.olat.modules.cl;

import java.util.BitSet;
import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.panel.Panel;
import org.olat.core.gui.components.table.DefaultColumnDescriptor;
import org.olat.core.gui.components.table.TableController;
import org.olat.core.gui.components.table.TableGuiConfiguration;
import org.olat.core.gui.components.table.TableMultiSelectEvent;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.gui.control.generic.modal.DialogBoxController;
import org.olat.core.gui.control.generic.modal.DialogBoxUIFactory;
import org.olat.core.logging.activity.CourseLoggingAction;
import org.olat.core.logging.activity.StringResourceableType;
import org.olat.core.logging.activity.ThreadLocalUserActivityLogger;
import org.olat.course.ICourse;
import org.olat.util.logging.activity.LoggingResourceable;

/**
 * Description:<br>
 * Display table with checkpoints of the checklist and the choice
 * for the actual identity.
 * 
 * <P>
 * Initial Date:  22.07.2009 <br>
 * @author bja <bja@bps-system.de>
 */
public class ChecklistDisplayController extends BasicController {

	// GUI
	private boolean canEdit, canManage, readOnly;
	private ChecklistAuthorOptionsForm authorOptions;
	private TableController runChecklistTable;
	private Panel panel;
	private CloseableModalController cmcEdit, cmcManage;
	private DialogBoxController yesNoDialog;
	private Controller manageController, editController;
	
	// data
	private Checklist checklist;
	private ICourse course;
	private List<Checkpoint> visibleCheckpoints;
	private ChecklistRunTableDataModel runTableData;
	private BitSet selection;
	
	public ChecklistDisplayController(UserRequest ureq, WindowControl wControl, Checklist checklist, boolean canEdit, boolean canManage, boolean readOnly, ICourse course) {
		super(ureq, wControl);
		// initialize attributes
		this.checklist = checklist;
		this.course = course;
		this.canEdit = canEdit;
		this.readOnly = readOnly;
		this.canManage = canManage;
		this.visibleCheckpoints = checklist.getVisibleCheckpoints();
		
		// display checklist
		displayChecklist(ureq);
	}
	
	private void displayChecklist(UserRequest ureq) {
		// add title
		VelocityContainer displayChecklistVC = createVelocityContainer("display");
		
		String title = checklist.getTitle() == null ? "" : checklist.getTitle();
		displayChecklistVC.contextPut("checklistTitle", title);
		// add edit and manage button
		if((canEdit || canManage) && course != null) {
			displayChecklistVC.contextPut("showAuthorBtns", Boolean.TRUE);
			removeAsListenerAndDispose(authorOptions);
			authorOptions = new ChecklistAuthorOptionsForm(ureq, getWindowControl(), canEdit && !readOnly, canManage);
			listenTo(authorOptions);
			displayChecklistVC.put("authorOptions", authorOptions.getInitialComponent());
		} else {
			displayChecklistVC.contextPut("showAuthorBtns", Boolean.FALSE);
		}
		
		panel = new Panel("runTable");
		
		initTable(ureq);
		
		displayChecklistVC.put("runTable", panel);

		putInitialPanel(displayChecklistVC);
	}
	
	private void initTable(UserRequest ureq) {
		// reload data
		loadData();
		// prepare table for run view
		runTableData = new ChecklistRunTableDataModel(visibleCheckpoints, getTranslator());
		
		TableGuiConfiguration tableConfig = new TableGuiConfiguration();
		tableConfig.setTableEmptyMessage(translate("cl.table.empty"), null, "o_cl_icon");
		
		removeAsListenerAndDispose(runChecklistTable);
		runChecklistTable = new TableController(tableConfig, ureq, getWindowControl(), getTranslator());
		listenTo(runChecklistTable);
		
		runChecklistTable.addColumnDescriptor(new DefaultColumnDescriptor("cl.table.title", 0, null, ureq.getLocale()));
		runChecklistTable.addColumnDescriptor(new DefaultColumnDescriptor("cl.table.description", 1, null, ureq.getLocale()));
		runChecklistTable.addColumnDescriptor(new DefaultColumnDescriptor("cl.table.mode", 2, null, ureq.getLocale()));
		runChecklistTable.setMultiSelect(true);
		if(readOnly) {
			runChecklistTable.setMultiSelectAsDisabled(true);
		} else {
			runChecklistTable.addMultiSelectAction("cl.table.run.action", "save");
		}
		runChecklistTable.setTableDataModel(runTableData);
		
		for(int i = 0; i < visibleCheckpoints.size(); i++) {
			Checkpoint checkpoint = runTableData.getObject(i);
			boolean selected = checkpoint.getSelectionFor(ureq.getIdentity()).booleanValue();
			runChecklistTable.setMultiSelectSelectedAt(i, selected);
			if(checkpoint.getMode().equals(CheckpointMode.MODE_LOCKED) ||
				(checkpoint.getMode().equals(CheckpointMode.MODE_EDITABLE_ONCE) && selected)) {
				runChecklistTable.setMultiSelectReadonlyAt(i, true);
			} else {
				runChecklistTable.setMultiSelectReadonlyAt(i, false);
			}
		}
		
		panel.setContent(runChecklistTable.getInitialComponent());
	}
	
	private void loadData() {
		this.checklist = ChecklistManager.getInstance().loadChecklist(checklist);
		this.visibleCheckpoints = checklist.getVisibleCheckpoints();
	}
	
	private void updateCheckpoints(UserRequest ureq) {
		ChecklistManager manager = ChecklistManager.getInstance();
		int size = visibleCheckpoints.size();
		for(int i = 0; i < size; i++) {
			Checkpoint checkpoint = visibleCheckpoints.get(i);
			Boolean selected = checkpoint.getSelectionFor(ureq.getIdentity());
			if(selected.booleanValue() != selection.get(i)) {
				checkpoint.setSelectionFor(ureq.getIdentity(), selection.get(i));
				manager.updateCheckpoint(checkpoint);
				// do logging
				String listTitle = checklist.getTitle() == null ? "" : checklist.getTitle();
				String pointTitle = checkpoint.getTitle() == null ? "" : checkpoint.getTitle();
				
				ThreadLocalUserActivityLogger.log(CourseLoggingAction.CHECKLIST_ELEMENT_CHECKPOINT_UPDATED, getClass(), 
						LoggingResourceable.wrapNonOlatResource(StringResourceableType.checklist, Long.toString(checklist.getKey()), listTitle),
						LoggingResourceable.wrapNonOlatResource(StringResourceableType.checkpoint, Long.toString(checkpoint.getKey()), pointTitle));
			}
		}
		initTable(ureq);
	}

	/**
	 * @see org.olat.core.gui.control.DefaultController#doDispose()
	 */
	@Override
	protected void doDispose() {
		if(runChecklistTable != null) {
			runChecklistTable.dispose();
			runChecklistTable = null;
		}
		if(authorOptions != null) {
			authorOptions.dispose();
			authorOptions = null;
		}
		if(cmcEdit != null) {
			cmcEdit.dispose();
			cmcEdit = null;
		}
		if(editController != null) {
			editController.dispose();
			editController = null;
		}
		if(cmcManage != null) {
			cmcManage.dispose();
			cmcManage = null;
		}
		if(manageController != null) {
			manageController.dispose();
			manageController = null;
		}
		if(yesNoDialog != null) {
			yesNoDialog.dispose();
			yesNoDialog = null;
		}
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(source == runChecklistTable) {
			if(event instanceof TableMultiSelectEvent) {
				TableMultiSelectEvent tmse = (TableMultiSelectEvent)event;
				selection = tmse.getSelection();
				boolean doUpdate = true;
				int size = visibleCheckpoints.size();
				// check for once editable checkpoints -> show confirmation dialog
				for(int i = 0; i < size; i++) {
					Checkpoint checkpoint = visibleCheckpoints.get(i);
					Boolean selected = checkpoint.getSelectionFor(ureq.getIdentity());
					if(checkpoint.getMode().equals(CheckpointMode.MODE_EDITABLE_ONCE) && selected.booleanValue() != selection.get(i)) {
						removeAsListenerAndDispose(yesNoDialog);
						yesNoDialog = DialogBoxUIFactory.createYesNoDialog(ureq, getWindowControl(), translate("dialog.save.title"), translate("dialog.save.text"));
						listenTo(yesNoDialog);
						yesNoDialog.activate();
						doUpdate = false;
						break;
					}
				}
				// no confirmation necessary, do update immediately
				if(doUpdate) updateCheckpoints(ureq);
			}
		} else if(source == yesNoDialog) {
			// do update after confirmation
			if(DialogBoxUIFactory.isYesEvent(event)) updateCheckpoints(ureq);
			removeAsListenerAndDispose(yesNoDialog);
		} else if(source == authorOptions) {
			if(event == ChecklistAuthorOptionsForm.CONFIG_CHECKPOINT) {
				removeAsListenerAndDispose(editController);
				CheckpointComparator checkpointComparator = null;
				boolean asc = runChecklistTable.getTableSortAsc();
				switch (runChecklistTable.getTableSortCol()) {
					case 1:
						if (asc) {
							checkpointComparator = ChecklistUIFactory.comparatorTitleAsc;
						} else {
							checkpointComparator = ChecklistUIFactory.comparatorTitleDesc;
						}
						break;
					case 2:
						if (asc) {
							checkpointComparator = ChecklistUIFactory.comparatorDescriptionAsc;
						} else {
							checkpointComparator = ChecklistUIFactory.comparatorDescriptionDesc;
						}
						break;
					case 3:
						if (asc) {
							checkpointComparator = ChecklistUIFactory.comparatorModeAsc;
						} else {
							checkpointComparator = ChecklistUIFactory.comparatorModeDesc;
						}
						break;
				}
				editController = ChecklistUIFactory.getInstance().createEditCheckpointsController(ureq, getWindowControl(), checklist, "cl.save.close", checkpointComparator);
				listenTo(editController);
				
				removeAsListenerAndDispose(cmcEdit);
				cmcEdit = new CloseableModalController(getWindowControl(), translate("cl.close"), editController.getInitialComponent(), true, translate("cl.edit.title"));
				listenTo(cmcEdit);
				
				cmcEdit.activate();
			} else if(event == ChecklistAuthorOptionsForm.MANAGE_CHECKPOINT) {
				removeAsListenerAndDispose(manageController);
				manageController = new ChecklistManageCheckpointsController(ureq, getWindowControl(), checklist, course, readOnly);
				listenTo(manageController);
				
				removeAsListenerAndDispose(cmcManage);
				cmcManage = new CloseableModalController(getWindowControl(), translate("cl.close"), manageController.getInitialComponent(), true, translate("cl.manage.title"));
				listenTo(cmcManage);
				
				cmcManage.activate();
			}
		} else if(source == cmcEdit) {
			initTable(ureq);
		} else if(source == editController) {
			if(event == Event.CHANGED_EVENT || event == Event.CANCELLED_EVENT) {
				initTable(ureq);
				cmcEdit.deactivate();
			}
		} else if(source == cmcManage) {
			initTable(ureq);
		} else if(source == manageController && event == Event.DONE_EVENT) {
			initTable(ureq);
			cmcManage.deactivate();
		}
	}

	/**
	 * @see org.olat.core.gui.control.DefaultController#event(org.olat.core.gui.UserRequest, org.olat.core.gui.components.Component, org.olat.core.gui.control.Event)
	 */
	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		// nothing to do
	}

}

class ChecklistAuthorOptionsForm extends FormBasicController {

	public static final Event MANAGE_CHECKPOINT = new Event("cl.manage.button");
	public static final Event CONFIG_CHECKPOINT = new Event("cl.config.button");

	private FormLink manageCheckpointsBtn, configCheckpointsBtn;
	private boolean canEdit, canManage;

	public ChecklistAuthorOptionsForm(UserRequest ureq, WindowControl wControl, boolean canEdit, boolean canManage) {
		super(ureq, wControl, LAYOUT_HORIZONTAL);
		this.canEdit = canEdit;
		this.canManage = canManage;
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		FormLayoutContainer mainLayout = FormLayoutContainer.createHorizontalFormLayout("mainLayout", getTranslator());
		formLayout.add(mainLayout);
		if(canManage) {
			manageCheckpointsBtn = uifactory.addFormLink("manageCheckpointsButton", "cl.manage.button", "", mainLayout, Link.BUTTON);
		}
		
		if(canEdit) {
			configCheckpointsBtn = uifactory.addFormLink("configCheckpointsButton", "cl.config.button", "", mainLayout, Link.BUTTON);
		}
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(source == manageCheckpointsBtn) {
			fireEvent(ureq, MANAGE_CHECKPOINT);
		} else if(source == configCheckpointsBtn) {
			fireEvent(ureq, CONFIG_CHECKPOINT);
		}
	}

	@Override
	protected void doDispose() {
		// nothing to do
	}

	@Override
	protected void formOK(UserRequest ureq) {
		// nothing to do
	}

}
