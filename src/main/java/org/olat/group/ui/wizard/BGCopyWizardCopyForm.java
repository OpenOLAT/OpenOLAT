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
* <p>
*/ 

package org.olat.group.ui.wizard;

import java.util.Arrays;

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
	
	SelectionElement ce;
	String[] keys, values;
	
	BGCopyWizardCopyForm(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl);
		
		keys = new String[] {
				"Tools",
				"Areas",
				"Rights",
				"Owners",
				"Participants",
				"MembersVisibility",
				"WaitingList"
		};
		
		values = new String[] {
				translate("bgcopywizard.copyform.tools"),
				translate("bgcopywizard.copyform.areas"),
				translate("bgcopywizard.copyform.rights"),
				translate("bgcopywizard.copyform.owners"),
				translate("bgcopywizard.copyform.participants"),
				translate("bgcopywizard.copyform.membersvisibility"),
				translate("bgcopywizard.copyform.waitingList")
		};
		
		initForm(ureq);
	}

	boolean isCopyTools() {return getBool("Tools"); }
	boolean isCopyAreas() {return getBool("Areas");}
	boolean isCopyOwners() {return getBool("Owners");}
	boolean isCopyRights() {return getBool("Rights");}
	boolean isCopyParticipants() {return getBool("Participants");}
	boolean isCopyMembersVisibility() {return getBool("MembersVisibility");}
	boolean isCopyWaitingList() {return getBool("WaitingList");}

	private boolean getBool (String k) {
		return ce.isSelected(Arrays.asList(keys).indexOf(k));
	}
	
	@Override
	protected void formOK(UserRequest ureq) {
		fireEvent(ureq, Event.DONE_EVENT);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		
		ce = uifactory.addCheckboxesVertical("toCopy", "bgcopywizard.copyform.label", formLayout, keys, values, null, 1);
		uifactory.addFormSubmitButton("continue", formLayout);
	}

	@Override
	protected void doDispose() {
		//
	}
}
