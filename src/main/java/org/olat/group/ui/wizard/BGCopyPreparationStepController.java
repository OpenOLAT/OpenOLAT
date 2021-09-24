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
package org.olat.group.ui.wizard;

import java.util.ArrayList;
import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.SelectionElement;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.wizard.StepFormBasicController;
import org.olat.core.gui.control.generic.wizard.StepsEvent;
import org.olat.core.gui.control.generic.wizard.StepsRunContext;

/**
 * 
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class BGCopyPreparationStepController extends StepFormBasicController {
	
	private SelectionElement ce;
	private String[] keys, values;
	private final boolean coursesEnabled;
	private final boolean rightsEnabled;
	private final boolean areasEnabled;
	
	public BGCopyPreparationStepController(UserRequest ureq, WindowControl wControl, Form rootForm,
			StepsRunContext runContext, boolean coursesEnabled, boolean areasEnabled, boolean rightsEnabled) {
		super(ureq, wControl, rootForm, runContext, LAYOUT_DEFAULT, null);

		this.coursesEnabled = coursesEnabled;
		this.rightsEnabled = rightsEnabled;
		this.areasEnabled = areasEnabled;
		
		List<String> keyList = new ArrayList<>(8);
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
	
	public Boolean getCopyCourses() {
		return getBool("Courses");
	}

	public Boolean getCopyTools() {
		return getBool("Tools");
	}
	
	public Boolean getCopyAreas() {
		return getBool("Areas");
	}
	
	public Boolean getCopyOwners() {
		return getBool("Owners");
	}
	
	public Boolean getCopyRights() {
		return getBool("Rights");
	}
	
	public Boolean getCopyParticipants() {
		return getBool("Participants");
	}
	
	public Boolean getCopyMembersVisibility() {
		return getBool("MembersVisibility");
	}
	
	public Boolean getCopyWaitingList() {
		return getBool("WaitingList");
	}
	
	private Boolean getBool (String k) {
		int index = -1;
		for(int i=keys.length; i-->0; ) {
			if(k.equals(keys[i])) {
				index = i;
				break;
			}
		}
		if(index >= 0) {
			return Boolean.valueOf(ce.isSelected(index));
		}
		return Boolean.FALSE;
	}
	
	@Override
	protected void formOK(UserRequest ureq) {
		addToRunContext("resources", getCopyCourses());
		addToRunContext("tools", getCopyTools());
		addToRunContext("areas", getCopyAreas());
		addToRunContext("rights", getCopyRights());
		addToRunContext("owners", getCopyOwners());
		addToRunContext("participants", getCopyParticipants());
		addToRunContext("membersvisibility", getCopyMembersVisibility());
		addToRunContext("waitingList", getCopyWaitingList());
		fireEvent(ureq, StepsEvent.ACTIVATE_NEXT);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		ce = uifactory.addCheckboxesVertical("toCopy", "bgcopywizard.copyform.label", formLayout, keys, values, 1);
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
	}

	@Override
	protected void doDispose() {
		//
	}
}
