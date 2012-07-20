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

package org.olat.group.ui.main;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.form.flexible.impl.elements.FormSubmit;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;

/**
 * 
 * Description:<br>
 * Search form for Business groups
 * 
 * <P>
 * Initial Date:  21 avr. 2011 <br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
//fxdiff VCRP-1,2: access control of resources
public class AdminBusinessGroupSearchController extends FormBasicController{

	private TextElement id; // only for admins
	private TextElement displayName;
	private TextElement owner;
	private TextElement description;
	private FormSubmit searchButton;
	private MultipleSelectionElement attendeeEl;
	private MultipleSelectionElement ownerEl;
	private MultipleSelectionElement waitingEl;
	private MultipleSelectionElement publicEl;
	
	private String limitUsername;
	private boolean isAdmin;

	/**
	 * Generic search form.
	 * @param name Internal form name.
	 * @param translator Translator
	 * @param withCancel Display a cancel button?
	 * @param isAdmin Is calling identity an administrator? If yes, allow search by ID
	 * @param limitTypes Limit searches to specific types.
	 */
	public AdminBusinessGroupSearchController(UserRequest ureq, WindowControl wControl, boolean isAdmin) {
		super(ureq, wControl);
		this.isAdmin = isAdmin;
		initForm(ureq);
	}
	
	public AdminBusinessGroupSearchController(UserRequest ureq, WindowControl wControl, boolean isAdmin, boolean extended) {
		super(ureq, wControl, "group_search");
		this.isAdmin = isAdmin;
		initForm(ureq);
	}
	
	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		FormLayoutContainer leftContainer = FormLayoutContainer.createDefaultFormLayout("left_1", getTranslator());
		leftContainer.setRootForm(mainForm);
		formLayout.add(leftContainer);

		displayName = uifactory.addTextElement("cif_displayname", "cif.displayname", 255, "", leftContainer);
		displayName.setFocus(true);
		
		owner = uifactory.addTextElement("cif_owner", "cif.owner", 255, "", leftContainer);
		if (limitUsername != null) {
			owner.setValue(limitUsername);
			owner.setEnabled(false);
		}
		description = uifactory.addTextElement("cif_description", "cif.description", 255, "", leftContainer);
		
		if(isAdmin) {
			id = uifactory.addTextElement("cif_id", "cif.id", 12, "", leftContainer);
			id.setVisible(isAdmin);
			id.setRegexMatchCheck("\\d*", "search.id.format");
		}
		
		FormLayoutContainer rightContainer = FormLayoutContainer.createDefaultFormLayout("right_1", getTranslator());
		rightContainer.setRootForm(mainForm);
		formLayout.add(rightContainer);

		String[] ownerValues = new String[]{ translate("search.owner") };
		ownerEl = uifactory.addCheckboxesVertical("search.owner", rightContainer, new String[]{"owner"}, ownerValues, null, 1);
		
		String[] attendeeValues = new String[]{ translate("search.attendee") };
		attendeeEl = uifactory.addCheckboxesVertical("search.attendee", rightContainer, new String[]{"attendee"}, attendeeValues, null, 1);
		
		String[] waitingValues = new String[]{ translate("search.waiting") };
		waitingEl = uifactory.addCheckboxesVertical("search.waiting", rightContainer, new String[]{"waiting"}, waitingValues, null, 1);

		String[] publicValues = new String[]{ translate("search.public") };
		publicEl = uifactory.addCheckboxesVertical("search.public", rightContainer, new String[]{"public"}, publicValues, null, 1);

		FormLayoutContainer buttonLayout = FormLayoutContainer.createButtonLayout("button_layout", getTranslator());
		formLayout.add(buttonLayout);
		searchButton = uifactory.addFormSubmitButton("search", "search", buttonLayout);
	}

	@Override
	protected void doDispose() {
		//
	}
	
	/**
	 * @return Return value of ID field.
	 */
	public Long getId() {
		if (id != null && !id.isEmpty()) {
			try {
				return new Long(id.getValue());
			} catch (NumberFormatException e) {
				id.setValue("");
				return null;
			}
		}
		return null;
	}

	/**
	 * @return Display name filed value.
	 */
	public String getName() {
		return displayName.getValue();
	}

	/**
	 * @return Author field value.
	 */
	public String getOwner() {
		return owner.getValue();
	}

	/**
	 * @return Descritpion field value.
	 */
	public String getDescription() {
		return description.getValue();
	}
	
	public boolean isEmpty() {
		return displayName.isEmpty() && owner.isEmpty() && description.isEmpty() && (id != null && id.isEmpty());
	}
	
	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = true;
		if (!isAdmin && isEmpty())	{
			showWarning("cif.error.allempty", null);
			allOk &= false;
		}
		
		if(id != null) {
			id.clearError();
			if (id != null && !id.isEmpty()) {
				try {
					new Long(id.getValue());
				} catch (NumberFormatException e) {
					id.setErrorKey("", null);
					allOk &= false;
				}
			}
		}
		
		if(!isAdmin) {
			boolean owner = ownerEl.isAtLeastSelected(1);
			boolean attendee = attendeeEl.isAtLeastSelected(1);
			boolean publicGroups = publicEl.isAtLeastSelected(1);
			if(!owner && !attendee && !publicGroups) {
				publicEl.select("public", true);
			}	
		}
		return allOk && super.validateFormLogic(ureq);
	}

	@Override
	protected void formOK (UserRequest ureq) {
		SearchEvent e = new SearchEvent();
		e.setId(getId());
		e.setName(getName());
		e.setDescription(getDescription());
		e.setOwnerName(getOwner());
		e.setOwner(ownerEl.isAtLeastSelected(1));
		e.setAttendee(attendeeEl.isAtLeastSelected(1));
		e.setWaiting(waitingEl.isAtLeastSelected(1));
		e.setPublicGroups(publicEl.isAtLeastSelected(1));
		fireEvent (ureq, e);
		fireEvent(ureq, Event.DONE_EVENT);
	}
	
	@Override
	protected void formInnerEvent (UserRequest ureq, FormItem source, FormEvent event) {
		if (source == searchButton) {
			SearchEvent e = new SearchEvent();
			e.setId(getId());
			e.setName(getName());
			e.setDescription(getDescription());
			e.setOwnerName(getOwner());
			e.setOwner(ownerEl.isAtLeastSelected(1));
			e.setAttendee(attendeeEl.isAtLeastSelected(1));
			e.setWaiting(waitingEl.isAtLeastSelected(1));
			e.setPublicGroups(publicEl.isAtLeastSelected(1));
			fireEvent(ureq, e);
			fireEvent(ureq, Event.DONE_EVENT);
		}
	}
}