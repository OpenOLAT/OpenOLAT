/**
 * 
 * BPS Bildungsportal Sachsen GmbH<br>
 * Bahnhofstrasse 6<br>
 * 09111 Chemnitz<br>
 * Germany<br>
 * 
 * Copyright (c) 2005-2009 by BPS Bildungsportal Sachsen GmbH<br>
 * http://www.bps-system.de<br>
 *
 * All rights reserved.
 */
package de.bps.olat.modules.cl;

import java.util.ArrayList;
import java.util.Date;

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
	private FormLayoutContainer titleContainer, buttonContainer;
	private DialogBoxController yesNoController;
	private ArrayList<TextElement> titleInputList;
	private ArrayList<TextElement> descriptionInputList;
	private ArrayList<SingleSelection> modeInputList;
	private ArrayList<FormLink> delButtonList;
	private String submitKey;
	private FormLink addButton;
	private CheckpointComparator checkpointComparator = ChecklistUIFactory.comparatorTitleAsc;
	
	// data
	private long counter = 0;
	private Checklist checklist;
	
	// helpers
	private boolean deletedOK = true;

	public ChecklistEditCheckpointsController(UserRequest ureq, WindowControl wControl, Checklist checklist, String submitKey, CheckpointComparator checkpointComparator) {
		super(ureq, wControl);
		
		this.checklist = checklist;
		this.submitKey = submitKey;
		
		if (checkpointComparator != null) {
			this.checkpointComparator = checkpointComparator;
		}
		
		int size = checklist.getCheckpoints().size();
		this.titleInputList = new ArrayList<TextElement>(size);
		this.descriptionInputList = new ArrayList<TextElement>(size);
		this.modeInputList = new ArrayList<SingleSelection>(size);
		this.delButtonList = new ArrayList<FormLink>(size);
		
		initForm(this.flc, this, ureq);
	}

	/**
	 * @see org.olat.core.gui.control.DefaultController#doDispose()
	 */
	@Override
	public void doDispose() {
		// nothing to dispose
	}

	@Override
	protected void formOK(UserRequest ureq) {
		for (int i = 0; i < this.checklist.getCheckpoints().size(); i++) {
			Checkpoint checkpoint = this.checklist.getCheckpoints().get(i);
			checkpoint.setChecklist(this.checklist);
			checkpoint.setLastModified(new Date());
			checkpoint.setTitle(this.titleInputList.get(i).getValue());
			checkpoint.setDescription(this.descriptionInputList.get(i).getValue());
			checkpoint.setMode(this.modeInputList.get(i).getSelectedKey());
		}
		ChecklistManager.getInstance().updateChecklist(this.checklist);
		// Inform all listeners about the changes
		fireEvent(ureq, Event.CHANGED_EVENT);
	}
	
	private void addNewFormCheckpoint(int index, Checkpoint checkpoint) {
		// add checkpoint title
		TextElement title = uifactory.addTextElement("title" + counter, null, -1, checkpoint.getTitle(), titleContainer);
		title.showError(false);//TODO:SK:2009-11-20:PB:should be default -> check layout in velocity.
		title.setDisplaySize(20);
		title.setMandatory(true);
		title.setNotEmptyCheck("cl.table.title.error");//TODO:Stefan Köber: please verify that the default not empty check does the same as you ItemValidatorProvider
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
				// add a new form link
				Checkpoint newCheckpoint = new Checkpoint();
				newCheckpoint.setChecklist(this.checklist);
				newCheckpoint.setLastModified(new Date());
				newCheckpoint.setTitle("");
				newCheckpoint.setDescription("");
				newCheckpoint.setMode(CheckpointMode.MODE_EDITABLE);
				int index = this.checklist.getCheckpoints().size();
				this.checklist.addCheckpoint(index, newCheckpoint);
				addNewFormCheckpoint(index, newCheckpoint);
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
		checklist.removeCheckpoint(checkpoint);
		int i;
		for (i = 0; i < titleInputList.size(); i++) {
			if (titleInputList.get(i).getUserObject().equals(checkpoint)) {
				break;
			}
		}
		titleContainer.remove(titleInputList.remove(i));
		for (i = 0; i < descriptionInputList.size(); i++) {
			if (descriptionInputList.get(i).getUserObject().equals(checkpoint)) {
				break;
			}
		}
		titleContainer.remove(descriptionInputList.remove(i));
		for (i = 0; i < modeInputList.size(); i++) {
			if (modeInputList.get(i).getUserObject().equals(checkpoint)) {
				break;
			}
		}
		titleContainer.remove(modeInputList.remove(i));
		for (i = 0; i < delButtonList.size(); i++) {
			if (delButtonList.get(i).getUserObject().equals(checkpoint)) {
				break;
			}
		}
		titleContainer.remove(delButtonList.remove(i));
	}

	@Override
	@SuppressWarnings("unused")
	protected void initForm(FormItemContainer fic, Controller controller, UserRequest ureq) {
		if(titleContainer != null) fic.remove(titleContainer);
		if(buttonContainer != null) fic.remove(buttonContainer);
		
		titleContainer = FormLayoutContainer.createCustomFormLayout("titleLayout", getTranslator(), velocity_root + "/edit.html");
		fic.add(titleContainer);
		// create gui elements for all checkpoints
		if(checklist.getCheckpoints().size() == 0) {
			Checkpoint newCheckpoint = new Checkpoint();
			newCheckpoint.setChecklist(this.checklist);
			newCheckpoint.setLastModified(new Date());
			newCheckpoint.setTitle("");
			newCheckpoint.setDescription("");
			newCheckpoint.setMode(CheckpointMode.MODE_EDITABLE);
			this.checklist.addCheckpoint(0, newCheckpoint);
			addNewFormCheckpoint(0, newCheckpoint);
		} else {
			for (int i = 0; i < checklist.getCheckpoints().size(); i++) {
				Checkpoint checkpoint = checklist.getCheckpointsSorted(checkpointComparator).get(i);
				addNewFormCheckpoint(i, checkpoint);
			}
		}
		addButton = new FormLinkImpl("add" + counter, "add" + counter, "cl.table.add", Link.BUTTON_SMALL);
		addButton.setUserObject(checklist.getCheckpointsSorted(checkpointComparator).get(checklist.getCheckpoints().size() - 1));
		titleContainer.add(addButton);

		titleContainer.contextPut("checkpoints", checklist.getCheckpointsSorted(checkpointComparator));
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
		this.titleInputList = new ArrayList<TextElement>(size);
		this.descriptionInputList = new ArrayList<TextElement>(size);
		this.modeInputList = new ArrayList<SingleSelection>(size);
		this.delButtonList = new ArrayList<FormLink>(size);
		mainForm.setDirtyMarking(false);
		initForm(this.flc, this, ureq);
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}

}
