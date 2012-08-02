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
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.form.flexible.impl.elements.FormSubmit;
import org.olat.core.gui.control.Controller;
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
public class BusinessGroupSearchController extends FormBasicController{

	private TextElement id; // only for admins
	private TextElement displayName;
	private TextElement owner;
	private TextElement description;
	private TextElement courseTitle;
	private FormSubmit searchButton;
	private SingleSelection rolesEl;
	private SingleSelection publicEl;
	private SingleSelection resourceEl;
	private MultipleSelectionElement headlessEl;
	
	private final boolean showId;
	private final boolean showRoles;
	private final boolean showAdminTools;
	private String limitUsername;
	
	private String[] roleKeys = {"all", "owner", "attendee", "waiting"};
	private String[] openKeys = {"all", "yes", "no"};
	private String[] resourceKeys = {"all", "yes", "no"};

	/**
	 * Generic search form.
	 * @param name Internal form name.
	 * @param translator Translator
	 * @param withCancel Display a cancel button?
	 * @param isAdmin Is calling identity an administrator? If yes, allow search by ID
	 * @param limitTypes Limit searches to specific types.
	 */
	public BusinessGroupSearchController(UserRequest ureq, WindowControl wControl, boolean showId, boolean showRoles, boolean showAdminTools) {
		super(ureq, wControl, "group_search");
		this.showId = showId;
		this.showRoles = showRoles;
		this.showAdminTools = showAdminTools;
		initForm(ureq);
	}
	
	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		FormLayoutContainer leftContainer = FormLayoutContainer.createDefaultFormLayout("left_1", getTranslator());
		leftContainer.setRootForm(mainForm);
		formLayout.add(leftContainer);
		
		if(showId) {
			id = uifactory.addTextElement("cif_id", "cif.id", 12, "", leftContainer);
			id.setRegexMatchCheck("\\d*", "search.id.format");
		}

		displayName = uifactory.addTextElement("cif_displayname", "cif.displayname", 255, "", leftContainer);
		displayName.setFocus(true);
		
		owner = uifactory.addTextElement("cif_owner", "cif.owner", 255, "", leftContainer);
		if (limitUsername != null) {
			owner.setValue(limitUsername);
			owner.setEnabled(false);
		}
		description = uifactory.addTextElement("cif_description", "cif.description", 255, "", leftContainer);
		
		courseTitle = uifactory.addTextElement("cif_coursetitle", "cif.coursetitle", 255, "", leftContainer);

		FormLayoutContainer rightContainer = FormLayoutContainer.createDefaultFormLayout("right_1", getTranslator());
		rightContainer.setRootForm(mainForm);
		formLayout.add(rightContainer);
		
		//roles
		if(showRoles) {
			String[] roleValues = new String[roleKeys.length];
			for(int i=roleKeys.length; i-->0; ) {
				roleValues[i] = translate("search." + roleKeys[i]);
			}
			rolesEl = uifactory.addRadiosHorizontal("roles", "search.roles", rightContainer, roleKeys, roleValues);
			rolesEl.select("all", true);
		}

		//public
		String[] openValues = new String[openKeys.length];
		for(int i=openKeys.length; i-->0; ) {
			openValues[i] = translate("search." + openKeys[i]);
		}
		publicEl = uifactory.addRadiosHorizontal("openBg", "search.open", rightContainer, openKeys, openValues);
		publicEl.select("all", true);

		//resources
		String[] resourceValues = new String[resourceKeys.length];
		for(int i=resourceKeys.length; i-->0; ) {
			resourceValues[i] = translate("search." + resourceKeys[i]);
		}
		resourceEl = uifactory.addRadiosHorizontal("resourceBg", "search.resources", rightContainer, resourceKeys, resourceValues);
		resourceEl.select("all", true);
		
		if(showAdminTools) {
			String[] keys = new String[] { "headless" };
			String[] values = new String[] { translate("search.headless.check") };
			headlessEl = uifactory.addCheckboxesHorizontal("headless.groups", "search.headless", rightContainer, keys, values, null);
		}

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
	 * @return Description field value.
	 */
	public String getDescription() {
		return description.getValue();
	}
	
	/**
	 * @return Course title field value
	 */
	public String getCourseTitle() {
		return courseTitle.getValue();
	}
	
	/**
	 * @return True if the text search fields are empty
	 */
	public boolean isEmpty() {
		return displayName.isEmpty() && owner.isEmpty() && description.isEmpty()
				&& (id != null && id.isEmpty()) && courseTitle.isEmpty();
	}
	
	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = true;

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

		return allOk && super.validateFormLogic(ureq);
	}

	@Override
	protected void formOK (UserRequest ureq) {
		fireSearchEvent(ureq);
	}
	
	@Override
	protected void formInnerEvent (UserRequest ureq, FormItem source, FormEvent event) {
		if (source == searchButton) {
			fireSearchEvent(ureq);
		}
	}
	
	private void fireSearchEvent(UserRequest ureq) {
		SearchEvent e = new SearchEvent();
		e.setId(getId());
		e.setName(getName());
		e.setDescription(getDescription());
		e.setOwnerName(getOwner());
		e.setCourseTitle(getCourseTitle());
		
		if(headlessEl != null && headlessEl.isAtLeastSelected(1)) {
			e.setHeadless(true);
		} else {
			e.setHeadless(false);
		}
		
		if(rolesEl != null && rolesEl.isOneSelected()) {
			e.setAttendee(rolesEl.isSelected(0) || rolesEl.isSelected(1));
			e.setOwner(rolesEl.isSelected(0) || rolesEl.isSelected(2));
			e.setWaiting(rolesEl.isSelected(0) || rolesEl.isSelected(3));
		}
		
		if(publicEl.isOneSelected()) {
			if(publicEl.isSelected(1)) {
				e.setPublicGroups(Boolean.TRUE);
			} else if(publicEl.isSelected(2)) {
				e.setPublicGroups(Boolean.FALSE);
			}
		}
		
		if(resourceEl.isOneSelected()) {
			if(resourceEl.isSelected(1)) {
				e.setResources(Boolean.TRUE);
			} else if(resourceEl.isSelected(2)) {
				e.setResources(Boolean.FALSE);
			}
		}
		fireEvent(ureq, e);
	}
}