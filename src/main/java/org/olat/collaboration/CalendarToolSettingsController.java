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
package org.olat.collaboration;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.elements.FormSubmit;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;

/**
 * 
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class CalendarToolSettingsController extends FormBasicController {
	
	private FormSubmit submit;
	private SingleSelection access;
	private final int calendarAccess;
	private final boolean canSave;

	/**
	 * @param name
	 * @param news
	 */
	public CalendarToolSettingsController(UserRequest ureq, WindowControl wControl, int calendarAccess) {
		super(ureq, wControl);
		this.calendarAccess = calendarAccess;
		this.canSave = true;
		initForm(ureq);	
	}
	
	
	public CalendarToolSettingsController(UserRequest ureq, WindowControl wControl, Form rootForm, int calendarAccess) {
		super(ureq, wControl, LAYOUT_DEFAULT, null, rootForm);
		this.calendarAccess = calendarAccess;
		this.canSave = false;
		initForm(ureq);	
	}

	/**
	 * @return String
	 */
	public int getCalendarAccess() {
		if (access.getSelectedKey().equals("all")) return CollaborationTools.CALENDAR_ACCESS_ALL;
		else return CollaborationTools.CALENDAR_ACCESS_OWNERS;
	}

	@Override
	protected void formOK(UserRequest ureq) {
		fireEvent(ureq, Event.DONE_EVENT);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		setFormTitle("calendar.access.title");
		
		String[] keys = new String[] { "owner", "all" };
		String values[] = new String[] {
				translate("calendar.access.owners"),
				translate("calendar.access.all")
		};
		
		access = uifactory.addRadiosVertical("access", "calendar.access", formLayout, keys, values);
		
		if (calendarAccess == CollaborationTools.CALENDAR_ACCESS_ALL) access.select("all", true);
		else access.select("owner", true);
		
		if(canSave) {
			submit = uifactory.addFormSubmitButton("submit", formLayout);
		}
	}
	
	public void setEnabled(boolean enabled) {
		access.setEnabled(enabled);
		if(submit != null) {
			submit.setVisible(enabled);
		}
	}
}
