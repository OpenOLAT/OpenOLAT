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

package org.olat.course.nodes.ta;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;

/**
 * Task status form. A tutor can define the status (ok,not ok, working on) of task.
 *
 * @author Christian Guretzki
 */
public class StatusForm extends FormBasicController {

	protected static final String FORMNAME = "statusform";

	/** Name of the form-element SingleSelectionElement. */
	protected static final String STATUS_SELECTION = "status_selection";
	
	// This status values will be stored in the db. 
	// Do not change it, if you have already persistent data.
	public static final String STATUS_VALUE_NOT_OK     = "not_ok";
	public static final String STATUS_VALUE_OK         = "ok";
	public static final String STATUS_VALUE_WORKING_ON = "working_on";
	public static final String STATUS_VALUE_UNDEFINED  = "undefined";
	
	// Keys to access Locale.properties file
	public static final String  STATUS_LOCALE_PROPERTY_PREFIX = "status.";
	private static final String PROPERTY_KEY_NOT_OK     = STATUS_LOCALE_PROPERTY_PREFIX + STATUS_VALUE_NOT_OK;
	private static final String PROPERTY_KEY_OK         = STATUS_LOCALE_PROPERTY_PREFIX + STATUS_VALUE_OK;
	private static final String PROPERTY_KEY_WORKING_ON = STATUS_LOCALE_PROPERTY_PREFIX + STATUS_VALUE_WORKING_ON;
	public static final String  PROPERTY_KEY_UNDEFINED  = STATUS_LOCALE_PROPERTY_PREFIX + STATUS_VALUE_UNDEFINED;
	
	/** Initial status value for a new course. */
	protected static final String STATUS_VALUE_INITIAL = STATUS_VALUE_UNDEFINED;

	private SingleSelection statusRadio;
	private String keys[], values[];
	private final boolean readOnly;
	
	/**
	 * 
	 * @param name   form name
	 * @param locale locale of the request 
	 */
	public StatusForm(UserRequest ureq, WindowControl wControl, boolean readOnly) {
		super(ureq, wControl);
		this.readOnly = readOnly;
		values = new String[]{translate(PROPERTY_KEY_UNDEFINED),
  			translate(PROPERTY_KEY_NOT_OK),
  			translate(PROPERTY_KEY_OK),
  			translate(PROPERTY_KEY_WORKING_ON)
		};
		keys = new String[]{
  			STATUS_VALUE_UNDEFINED,
  			STATUS_VALUE_NOT_OK ,
  			STATUS_VALUE_OK,
  			STATUS_VALUE_WORKING_ON
		};
		initForm(ureq);
	}
	
	public String getSelectedStatus() {
		return statusRadio.getSelectedKey();
	}
	public void setSelectedStatus(String value) {
		statusRadio.select(value, true);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		fireEvent(ureq, Event.DONE_EVENT);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		statusRadio = uifactory.addRadiosVertical("status", "form.status.selection", formLayout, keys, values);
		statusRadio.select(STATUS_VALUE_UNDEFINED, true);
		statusRadio.setEnabled(!readOnly);
		if(!readOnly) {
			uifactory.addFormSubmitButton("save", formLayout);
		}
	}

}
