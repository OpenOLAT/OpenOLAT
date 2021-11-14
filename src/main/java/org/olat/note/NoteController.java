/**
* OLAT - Online Learning and Training<br>
* http://www.olat.org
* <p>
* Licensed under the Apache License, Version 2.0 (the "License"); <br>
* you may not use this file except in compliance with the License.<br>
* You may obtain a copy of the License at
* <p>
* http://www.apache.org/licenses/LICENSE-2.0
* <p>
* Unless required by applicable law or agreed to in writing,<br>
* software distributed under the License is distributed on an "AS IS" BASIS, <br>
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
* See the License for the specific language governing permissions and <br>
* limitations under the License.
* <p>
* Copyright (c) since 2004 at Multimedia- & E-Learning Services (MELS),<br>
* University of Zurich, Switzerland.
* <hr>
* <a href="http://www.openolat.org">
* OpenOLAT - Online Learning and Training</a><br>
* This file has been modified by the OpenOLAT community. Changes are licensed
* under the Apache 2.0 license as the original file.
*/

package org.olat.note;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.elements.RichTextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.FormLinkImpl;
import org.olat.core.gui.components.form.flexible.impl.elements.FormSubmit;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.id.Identity;
import org.olat.core.id.OLATResourceable;
import org.olat.core.util.StringHelper;
import org.olat.core.util.event.EventBus;
import org.olat.core.util.event.GenericEventListener;
import org.olat.core.util.resource.OLATResourceableJustBeforeDeletedEvent;
import org.olat.core.util.resource.OresHelper;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Initial Date: Dec 9, 2004
 * 
 * @author Alexander Schneider
 * @author Roman Haag, frentix GmbH, 17.06.09 refactored to use FlexiForm
 * 
 * Comment: Displays one note. Is called from every course or from the notelist
 * in the users home.
 * 
 */
public class NoteController extends FormBasicController implements GenericEventListener {
	
	private static final int NOTE_MAX_LENGTH = 400000;

	private Note n;
	private EventBus sec;
	private RichTextElement noteField;
	private FormLink editButton;
	private FormSubmit submitButton;
	
	@Autowired
	private NoteManager noteManager;

	/**
	 * @param ureq
	 * @param wControl
	 * @param n
	 * @param popupTrue
	 */
	public NoteController(UserRequest ureq, WindowControl wControl, Note n) {
		super(ureq, wControl, FormBasicController.LAYOUT_VERTICAL);
		String resourceTypeName = n.getResourceTypeName();
		Long resourceTypeId = n.getResourceTypeId();
		String noteTitle = n.getNoteTitle();

		init(ureq, resourceTypeName, resourceTypeId, noteTitle);
	}

	/**
	 * @param ureq
	 * @param ores the OLATResourceable to which this note refers to (the context
	 *          of the note, e.g. a certain course)
	 * @param noteTitle
	 * @param popupTrue
	 * @param wControl
	 */
	public NoteController(UserRequest ureq, OLATResourceable ores, String noteTitle, WindowControl wControl) {
		super(ureq, wControl, FormBasicController.LAYOUT_VERTICAL);
		String resourceTypeName = ores.getResourceableTypeName();
		Long resourceTypeId = ores.getResourceableId();

		init(ureq, resourceTypeName, resourceTypeId, noteTitle);
	}

	private void init(UserRequest ureq, String resourceTypeName, Long resourceTypeId, String noteTitle) {
		Identity owner = ureq.getIdentity();
		n = noteManager.loadNoteOrCreateInRAM(owner, resourceTypeName, resourceTypeId);
		n.setNoteTitle(noteTitle);

		// register for local event (for the same user), is used to dispose
		// window/popup if note is deleted while open!
		sec = ureq.getUserSession().getSingleUserEventCenter();
		sec.registerFor(this, ureq.getIdentity(), OresHelper.lookupType(Note.class));

		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		// At beginning, the forms shows the note as a disabled field and an edit button. When the user
		// clicks the edit button, the rich text field turns to the enabled state and the edit button
		// is set to visible false and the submit button to visible true.
		setFormTitle("note", new String[] { StringHelper.escapeHtml(n.getNoteTitle()) });
		// set custom css style to override default read-only view of rich text element
		setFormStyle("o_notes");

		// we don't use FormUIFactory.addFormSubmitButton(...) here since that would cause the following custom CSS setting to get ignored.
		editButton = new FormLinkImpl("edit", "edit", "edit", Link.BUTTON_SMALL);
		editButton.setCustomEnabledLinkCSS("pull-right btn btn-default");
		editButton.setIconLeftCSS("o_icon o_icon-fw o_icon_edit");
		formLayout.add(editButton);
		
		noteField = uifactory.addRichTextElementForStringData("noteField", null, n.getNoteText(), 20, -1, false, null, null, formLayout, ureq.getUserSession(), getWindowControl());
		noteField.setEnabled(false);
		noteField.setMaxLength(NOTE_MAX_LENGTH);

		submitButton = uifactory.addFormSubmitButton("submit", formLayout);
		submitButton.setVisible(false);
	}

	private void createOrUpdateNote(String content) {
		n.setNoteText(content);
		if (n.getKey() == null) {
			noteManager.saveNote(n);
			Long newKey = n.getKey();
			OLATResourceable ores = OresHelper.createOLATResourceableInstance(Note.class, newKey);
			sec.fireEventToListenersOf(new NoteEvent(getIdentity().getKey()), ores);
		} else {
			n = noteManager.updateNote(n);
		}
	}

	@Override
	protected void doDispose() {
		sec.deregisterFor(this, OresHelper.lookupType(Note.class));
        super.doDispose();
	}

	@Override
	public void event(Event event) {
		if (event instanceof OLATResourceableJustBeforeDeletedEvent) {
			OLATResourceableJustBeforeDeletedEvent bdev = (OLATResourceableJustBeforeDeletedEvent) event;
			Long key = n.getKey();
			if (key != null) { // already persisted
				if (bdev.getOresId().equals(key)) {
					dispose();
				}
			}
		}
	}

	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = super.validateFormLogic(ureq);
		
		String text = noteField.getValue();
		noteField.clearError();
		if(text.length() > NOTE_MAX_LENGTH) {
			noteField.setErrorKey("input.toolong", new String[]{ Integer.toString(NOTE_MAX_LENGTH) });
			allOk &= false;
		}
		
		return allOk;
	}

	@Override
	protected void formOK(UserRequest ureq) {
		// if the user clicked on the submit button...
		String text = noteField.getValue();
		// ...store the text...
		createOrUpdateNote(text);
		
		// ...and then hide the submit button, show the edit button, and make the field disabled (i.e. display-only) again.
		submitButton.setVisible(false);
		editButton.setVisible(true);
		noteField.setEnabled(false);
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		// If the user clicked the edit button, set the rich text input field to enabled and hide the edit button.
		if (source == editButton && editButton.isEnabled()) {
			noteField.setEnabled(true);
			editButton.setVisible(false);
			submitButton.setVisible(true);
			
			// this is to force the redraw of the form so that the submit button gets shown:
			flc.setDirty(true);
			
			// since clicking the edit button is registered as a change on the form, the submit button would get orange,
			// so we need the following line to set the form back to unchanged since at this point, the user has not
			// yet really changed anything.
			mainForm.setDirtyMarking(false);
		}
	}
}