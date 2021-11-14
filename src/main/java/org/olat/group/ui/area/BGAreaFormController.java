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
package org.olat.group.ui.area;

import java.util.HashSet;
import java.util.Set;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.RichTextElement;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.StringHelper;
import org.olat.group.area.BGArea;

/**
 * Provides a dialog to create or edit business groups.
 * 
 * @author twuersch
 * 
 */
public class BGAreaFormController extends FormBasicController {

	private TextElement name;

	private RichTextElement description;

	private String origName;

	private Set<String> validNames;

	private boolean bulkMode = false;

	private BGArea bgArea;

	/**
	 * Creates this controller.
	 * 
	 * @param ureq The user request.
	 * @param wControl The window control.
	 * @param bgArea The business group area object this dialog is referring to .
	 * @param bulkMode <code>true</code> means edit more than one group at once.
	 */
	public BGAreaFormController(UserRequest ureq, WindowControl wControl, BGArea bgArea, boolean bulkMode) {
		super(ureq, wControl, FormBasicController.LAYOUT_DEFAULT);
		this.bgArea = bgArea;
		this.bulkMode = bulkMode;
		initForm(ureq);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.olat.core.gui.components.form.flexible.impl.FormBasicController#initForm
	 * (org.olat.core.gui.components.form.flexible.FormItemContainer,
	 * org.olat.core.gui.control.Controller, org.olat.core.gui.UserRequest)
	 */
	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		// add the name field
		name = uifactory.addTextElement("area.form.name", "area.form.name", 255, "", formLayout);
		name.setMandatory(true);
		if (bulkMode) {
			name.setExampleKey("area.form.name.example", null);
		}
		
		// add the description field
		description = uifactory.addRichTextElementForStringDataMinimalistic("area.form.description", "area.form.description", "", 10, -1,
				formLayout, getWindowControl());

		if (bgArea != null) {
			name.setValue(bgArea.getName());
			description.setValue(bgArea.getDescription());
			origName = bgArea.getName();
		}

		// Create submit and cancel buttons
		final FormLayoutContainer buttonLayout = FormLayoutContainer.createButtonLayout("buttonLayout", getTranslator());
		formLayout.add(buttonLayout);
		uifactory.addFormSubmitButton("finish", buttonLayout);
		uifactory.addFormCancelButton("cancel", buttonLayout, ureq, getWindowControl());
	}

	/**
	 * @see org.olat.core.gui.components.form.flexible.impl.FormBasicController#validateFormLogic(org.olat.core.gui.UserRequest)
	 */
	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		// check name first
		if (!StringHelper.containsNonWhitespace(this.name.getValue())) {
			name.setErrorKey("form.legende.mandatory", new String[] {});
			return false;
		}
		if (bulkMode) {
			// check all names to be valid and check that at least one is entered
			// e.g. find "," | "   , " | ",,," errors => no group entered
			String selectionAsCsvStr = name.getValue();
			String[] activeSelection = selectionAsCsvStr != null ? selectionAsCsvStr.split(",") : new String[] {};
			validNames = new HashSet<>();
			Set<String> wrongNames = new HashSet<>();
			for (int i = 0; i < activeSelection.length; i++) {
				if ((activeSelection[i].trim()).matches(BGArea.VALID_AREANAME_REGEXP)) {
					validNames.add(activeSelection[i].trim());
				} else {
					wrongNames.add(activeSelection[i].trim());
				}
			}
			if (validNames.size() == 0 && wrongNames.size() == 0) {
				// no valid name and no invalid names, this is no names
				name.setErrorKey("area.form.error.illegalName", new String[] {});
				return false;
			} else if (wrongNames.size() == 1) {
				// one invalid name
				name.setErrorKey("area.form.error.illegalName", new String[] {});
				return false;
			} else if (wrongNames.size() > 1) {
				// two or more invalid names
				String[] args = new String[] { StringHelper.formatAsCSVString(wrongNames) };
				name.setErrorKey("create.form.error.illegalNames", args);
				return false;
			}
		} else {
			if (!name.getValue().matches(BGArea.VALID_AREANAME_REGEXP)) {
				name.setErrorKey("area.form.error.illegalName", new String[] {});
				return false;
			}
		}
		name.clearError();
		// done with name checks, now check description
		if (description.getValue().length() > 4000) {
			// description has maximum length
			description.setErrorKey("input.toolong", new String[] {"4000"});
			return false;
		}
		// ok, all checks passed
		return true;
	}

	@Override
	protected void formOK(UserRequest ureq) {
		fireEvent(ureq, Event.DONE_EVENT);
	}

	/**
	 * @see org.olat.core.gui.components.form.flexible.impl.FormBasicController#formNOK(org.olat.core.gui.UserRequest)
	 */
	@Override
	protected void formNOK(UserRequest ureq) {
		fireEvent(ureq, Event.FAILED_EVENT);
	}

	/**
	 * @see org.olat.core.gui.components.form.flexible.impl.FormBasicController#formCancelled(org.olat.core.gui.UserRequest)
	 */
	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}

	/**
	 * Sets this business group area's name.
	 * 
	 * @param areaName The new name.
	 */
	public void setAreaName(String areaName) {
		name.setValue(areaName);
	}

	/**
	 * Gets the description text.
	 * 
	 * @return The description text.
	 */
	public String getAreaDescription() {
		return description.getValue();
	}

	/**
	 * Gets the group names (used in bulk mode).
	 * 
	 * @return The group names.
	 */
	public Set<String> getGroupNames() {
		return validNames;
	}

	/**
	 * Gets the name of this business group area.
	 * 
	 * @return The name of this business group area.
	 */
	public String getAreaName() {
		return name.getValue().trim();
	}

	/**
	 * Resets the name of this business group area to its original value.
	 */
	public void resetAreaName() {
		name.setValue(origName);
	}
}
