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

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.olat.core.commons.persistence.DBFactory;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.form.flexible.impl.elements.FormLinkImpl;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.modal.DialogBoxController;
import org.olat.core.gui.control.generic.modal.DialogBoxUIFactory;

/**
 * Description:<br>
 * Controller for editing a checklist.
 * 
 * <P>
 * Initial Date:  22.07.2009 <br>
 * @author bja <bja@bps-system.de>
 */
public class ChecklistEditCheckpointsController extends FormBasicController {

	// GUI
	private FormLayoutContainer titleContainer;
	private FormLayoutContainer buttonContainer;
	private DialogBoxController yesNoController;
	private List<TextElement> titleInputList;
	private List<TextElement> descriptionInputList;
	private List<SingleSelection> modeInputList;
	private List<FormLink> delButtonList;
	private String submitKey;
	private FormLink addButton;
	private CheckpointComparator checkpointComparator = ChecklistUIFactory.comparatorTitleAsc;
	
	// data
	private long counter = 0;
	private Checklist checklist;
	private List<Checkpoint> checkpointsInVc;
	
	// helpers
	private boolean deletedOK = true;
	
	private final ChecklistManager checklistManager;

	public ChecklistEditCheckpointsController(UserRequest ureq, WindowControl wControl, Checklist checklist, String submitKey, CheckpointComparator checkpointComparator) {
		super(ureq, wControl);
		
		this.checklist = checklist;
		this.submitKey = submitKey;
		checklistManager = ChecklistManager.getInstance();
		
		if (checkpointComparator != null) {
			this.checkpointComparator = checkpointComparator;
		}
		
		int size = checklist.getCheckpoints().size();
		checkpointsInVc  = new ArrayList<>(size);
		titleInputList = new ArrayList<>(size);
		descriptionInputList = new ArrayList<>(size);
		modeInputList = new ArrayList<>(size);
		delButtonList = new ArrayList<>(size);
		
		initForm(ureq);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		checklist = checklistManager.loadChecklist(checklist);
		for (int i = 0; i < titleInputList.size(); i++) {
			boolean deleted = ! titleInputList.get(i).isVisible();
			Checkpoint checkpoint = (Checkpoint)titleInputList.get(i).getUserObject();
			if(deleted) {
				Checkpoint currentCheckpoint = checklist.getCheckpoint(checkpoint);
				checklist.removeCheckpoint(currentCheckpoint);
			} else {
				Checkpoint currentCheckpoint = checklist.getCheckpoint(checkpoint);
				if(currentCheckpoint == null) {
					currentCheckpoint = checkpoint;//the point is a new one
				}
				currentCheckpoint.setChecklist(checklist);
				currentCheckpoint.setLastModified(new Date());
				currentCheckpoint.setTitle(titleInputList.get(i).getValue());
				currentCheckpoint.setDescription(descriptionInputList.get(i).getValue());
				currentCheckpoint.setMode(modeInputList.get(i).getSelectedKey());
				if(currentCheckpoint.getKey() == null) {
					checklist.addCheckpoint(i, currentCheckpoint);
				}
			}
		}
		checklist = checklistManager.updateChecklist(checklist);
		DBFactory.getInstance().commit();
		loadCheckpointInVC();
		titleContainer.setDirty(true);
		// Inform all listeners about the changes
		fireEvent(ureq, Event.CHANGED_EVENT);
	}
	
	private void addNewFormCheckpoint(int index, Checkpoint checkpoint) {
		// add checkpoint title
		String pointTitle = checkpoint.getTitle() == null ? "" : checkpoint.getTitle();
		TextElement title = uifactory.addTextElement("title" + counter, null, -1, pointTitle, titleContainer);
		title.showError(false);
		title.setDisplaySize(20);
		title.setMandatory(true);
		title.setNotEmptyCheck("cl.table.title.error");
		title.setUserObject(checkpoint);
		titleInputList.add(index, title);
		
		// add checkpoint description
		TextElement description = uifactory.addTextElement("description" + counter, null, -1, checkpoint.getDescription(), titleContainer);
		description.setDisplaySize(35);
		description.setMandatory(false);
		description.setUserObject(checkpoint);
		descriptionInputList.add(index, description);
		
		// add link comment
		String[] keys = CheckpointMode.getModes();
		String[] values = new String[keys.length];
		for (int i = 0; i < keys.length; i++) {
			values[i] = translate(keys[i]);
		}
		SingleSelection mode = uifactory.addDropdownSingleselect("modus" + counter, "form.enableCancelEnroll", titleContainer, keys, values, null);
		mode.select(checkpoint.getMode(), checkpoint.getMode() != null);
		mode.setUserObject(checkpoint);
		modeInputList.add(index, mode);
		
		// add link deletion action button
		FormLink delButton = new FormLinkImpl("delete" + counter, "delete" + counter, "cl.table.delete", Link.BUTTON_SMALL);
		delButton.setUserObject(checkpoint);
		titleContainer.add(delButton);
		delButtonList.add(index, delButton);
		// increase the counter to enable unique component names
		counter++;
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (source.getComponent() instanceof Link) {
			if (addButton.equals(source)) {
				int index = checklist.getCheckpoints().size();
				// add a new form link
				Checkpoint newCheckpoint = new Checkpoint();
				newCheckpoint.setChecklist(checklist);
				newCheckpoint.setLastModified(new Date());
				newCheckpoint.setTitle("");
				newCheckpoint.setDescription("");
				newCheckpoint.setMode(CheckpointMode.MODE_EDITABLE);
				checklist.addCheckpoint(index, newCheckpoint);
				addNewFormCheckpoint(index, newCheckpoint);
				checkpointsInVc.add(newCheckpoint);
				flc.setDirty(true);
			} else if (delButtonList.contains(source)) {
				// special case: only one line existent
				if (checklist.getCheckpoints().size() == 1) {
					// clear this line
					titleInputList.get(0).setValue("");
					descriptionInputList.get(0).setValue("");
				} else {
					Checkpoint checkpoint = (Checkpoint) ((FormLink) source).getUserObject();
					removeFormLink(checkpoint);
				}
				deletedOK = false;
			}
		}
		super.formInnerEvent(ureq, source, event);
	}

	private void removeFormLink(Checkpoint checkpoint) {
		for (int i = 0; i < titleInputList.size(); i++) {
			if (titleInputList.get(i).getUserObject().equals(checkpoint)) {
				titleInputList.get(i).setVisible(false);
			}
		}
	}

	@Override
	protected void initForm(FormItemContainer fic, Controller controller, UserRequest ureq) {
		if(titleContainer != null) fic.remove(titleContainer);
		if(buttonContainer != null) fic.remove(buttonContainer);
		
		titleContainer = FormLayoutContainer.createCustomFormLayout("titleLayout", getTranslator(), velocity_root + "/edit.html");
		fic.add(titleContainer);
		// create gui elements for all checkpoints
		loadCheckpointInVC();
		
		addButton = new FormLinkImpl("add" + counter, "add" + counter, "cl.table.add", Link.BUTTON_SMALL);
		addButton.setUserObject(checklist.getCheckpointsSorted(checkpointComparator).get(checklist.getCheckpoints().size() - 1));
		titleContainer.add(addButton);
		
		titleContainer.contextPut("checkpoints", checkpointsInVc);
		titleContainer.contextPut("titleInputList", titleInputList);
		titleContainer.contextPut("descriptionInputList", descriptionInputList);
		titleContainer.contextPut("modeInputList", modeInputList);
		titleContainer.contextPut("addButton", addButton);
		titleContainer.contextPut("delButtonList", delButtonList);

		buttonContainer = FormLayoutContainer.createButtonLayout("buttonLayout", getTranslator());
		fic.add(buttonContainer);
		
		uifactory.addFormCancelButton("cancel", buttonContainer, ureq, getWindowControl());
		uifactory.addFormSubmitButton("subm", submitKey, buttonContainer);
	}
	
	private void loadCheckpointInVC() {
		checkpointsInVc.clear();
		titleInputList.clear();
		descriptionInputList.clear();
		modeInputList.clear();
		delButtonList.clear();
		
		int numOfCheckpoints = checklist.getCheckpoints().size();
		if(numOfCheckpoints == 0) {
			Checkpoint newCheckpoint = new Checkpoint();
			newCheckpoint.setChecklist(checklist);
			newCheckpoint.setLastModified(new Date());
			newCheckpoint.setTitle("");
			newCheckpoint.setDescription("");
			newCheckpoint.setMode(CheckpointMode.MODE_EDITABLE);
			checklist.addCheckpoint(0, newCheckpoint);
			addNewFormCheckpoint(0, newCheckpoint);
			
			List<Checkpoint> checkpoints = checklist.getCheckpointsSorted(checkpointComparator);
			checkpointsInVc.addAll(checkpoints);
		} else {
			List<Checkpoint> checkpoints = checklist.getCheckpointsSorted(checkpointComparator);
			for (int i = 0; i<numOfCheckpoints; i++) {
				Checkpoint checkpoint = checkpoints.get(i);
				addNewFormCheckpoint(i, checkpoint);
			}
			checkpointsInVc.addAll(checkpoints);
		}
	}
	
	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(source == yesNoController) {
			if(DialogBoxUIFactory.isYesEvent(event)) {
				deletedOK = true;
				mainForm.submit(ureq);
				yesNoController.dispose();
				yesNoController = null;
			} else {
				yesNoController.dispose();
				yesNoController = null;
			}
		}
	}
	
	@Override
	protected void formNOK(UserRequest ureq) {
		if(!deletedOK) {
			yesNoController = DialogBoxUIFactory.createYesNoDialog(ureq, getWindowControl(), translate("cl.edit.deleted.title"), translate("cl.edit.deleted.text"));
			yesNoController.addControllerListener(this);
			yesNoController.activate();
		}
	}
	
	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean isOk = super.validateFormLogic(ureq);
		if(isOk) isOk = deletedOK;
		
		return isOk;
	}
	
	@Override	
	protected void formCancelled(UserRequest ureq) {
		// reset complete form
		this.checklist = ChecklistManager.getInstance().loadChecklist(checklist); // reload data from database
		int size = checklist.getCheckpoints().size();
		this.titleInputList = new ArrayList<>(size);
		this.descriptionInputList = new ArrayList<>(size);
		this.modeInputList = new ArrayList<>(size);
		this.delButtonList = new ArrayList<>(size);
		mainForm.setDirtyMarking(false);
		initForm(flc, this, ureq);
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}
}