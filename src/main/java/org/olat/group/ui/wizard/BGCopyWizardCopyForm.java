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

package org.olat.group.ui.wizard;

import java.util.ArrayList;
import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.SelectionElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;

/**
 * Description:<BR>
 * Form to choose the elements of a group that should be copied
 * <P>
 * Initial Date: Oct 20, 2004
 * 
 * @author gnaegi
 */
class BGCopyWizardCopyForm extends FormBasicController {
	
	private SelectionElement ce;
	private String[] keys, values;
	private final boolean coursesEnabled;
	private final boolean rightsEnabled;
	private final boolean areasEnabled;
	
	BGCopyWizardCopyForm(UserRequest ureq, WindowControl wControl, boolean coursesEnabled, boolean areasEnabled, boolean rightsEnabled) {
		super(ureq, wControl);
		
		this.coursesEnabled = coursesEnabled;
		this.rightsEnabled = rightsEnabled;
		this.areasEnabled = areasEnabled;
		
		List<String> keyList = new ArrayList<String>(8);
		keyList.add("Tools");
		if(coursesEnabled) {
			keyList.add("Courses");
		}
		if(areasEnabled) {
			keyList.add("Areas");
		}
		if(rightsEnabled) {
			keyList.add("Rights");
		}
		keyList.add("Owners");
		keyList.add("Participants");
		keyList.add("WaitingList");
		keyList.add("MembersVisibility");
		
		keys = keyList.toArray(new String[keyList.size()]);
		values = new String[keys.length];
		for(int i=keys.length; i-->0; ) {
				values[i] = translate("bgcopywizard.copyform." + keys[i].toLowerCase());
		};
		
		initForm(ureq);
	}

	public boolean isCopyCourses() {
		return getBool("Courses");
	}
	public boolean isCopyTools() {
		return getBool("Tools");
	}
	public boolean isCopyAreas() {
		return getBool("Areas");
	}
	public boolean isCopyOwners() {
		return getBool("Owners");
	}
	public boolean isCopyRights() {
		return getBool("Rights");
	}
	public boolean isCopyParticipants() {
		return getBool("Participants");
	}
	public boolean isCopyMembersVisibility() {
		return getBool("MembersVisibility");
	}
	public boolean isCopyWaitingList() {
		return getBool("WaitingList");
	}

	private boolean getBool (String k) {
		int index = -1;
		for(int i=keys.length; i-->0; ) {
			if(k.equals(keys[i])) {
				index = i;
				break;
			}
		}
		if(index >= 0) {
			return ce.isSelected(index);
		}
		return false;
	}
	
	@Override
	protected void formOK(UserRequest ureq) {
		fireEvent(ureq, Event.DONE_EVENT);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		ce = uifactory.addCheckboxesVertical("toCopy", "bgcopywizard.copyform.label", formLayout, keys, values, null, 1);
		ce.select("Tools", true);
		if(coursesEnabled) {
			ce.select("Courses", true);
		}
		if(areasEnabled) {
			ce.select("Areas", true);
		}
		if(rightsEnabled) {
			ce.select("Rights", false);
		}
		ce.select("Owners", false);
		ce.select("Participants", false);
		ce.select("WaitingList", false);
		ce.select("MembersVisibility", true);
		uifactory.addFormSubmitButton("continue", formLayout);
	}

	@Override
	protected void doDispose() {
		//
	}
}
