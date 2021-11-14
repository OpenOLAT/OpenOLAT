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
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.logging.AssertException;
import org.olat.core.util.StringHelper;
import org.olat.group.BusinessGroup;

/**
 * Description:<BR>
 * Form to enter multiple group names
 * <P>
 * Initial Date: Oct 20, 2004
 * 
 * @author gnaegi
 */
public class GroupNamesForm extends FormBasicController {

	private TextElement groupNames;
	private TextElement bgMax;
	private List<String> groupNamesList;
	
	private Integer defaultMaxValue;

	public GroupNamesForm(UserRequest ureq, WindowControl wControl, Integer defaultMaxValue) {
		super(ureq, wControl);
		this.defaultMaxValue = defaultMaxValue;;
		initForm(ureq);
	}

	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		List<String> namesList = new ArrayList<>();
		String groupNamesString = groupNames.getValue();
		String[] groups = groupNamesString.split("[\t\n\f\r]");
		
		for (int i = 0; i < groups.length; i++) {
			String groupName = groups[i].trim();
			if(groupName.length()>BusinessGroup.MAX_GROUP_NAME_LENGTH) {
				groupNames.setErrorKey("bgcopywizard.multiple.groupnames.tooLongGroupname", null);
				return false;
			} else if (!groupName.matches(BusinessGroup.VALID_GROUPNAME_REGEXP)) {
				groupNames.setErrorKey("bgcopywizard.multiple.groupnames.illegalGroupname", null);
				return false;
			}
			// ignore lines that contains only whitespace, groupname must have
			// non-whitespace
			if (StringHelper.containsNonWhitespace(groupName)) {
				namesList.add(groupName);
			}
		}
		// list seems to be valid. store for later retrival
		groupNamesList = namesList;
		
		if (namesList.isEmpty()) {
			groupNames.setErrorKey("create.form.error.emptylist", null);
			return false;
		}
		
		return true;
	}

	/**
	 * @return A valid list of groupnames. The list is only valid if the form
	 *         returned the validation ok event!
	 */
	public List<String> getGroupNamesList() {
		if (groupNamesList == null) { 
			throw new AssertException("getGroupNamesList() called prior to form EVENT_VALIDATION_OK event");
		}
		return groupNamesList;
	}

	/**
	 * @return Integer max number of group participants
	 */
	public Integer getGroupMax() {
		String result = bgMax.getValue();
		if (result.length() == 0) return null;
		return Integer.valueOf(result);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		fireEvent (ureq, Event.DONE_EVENT);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		groupNames = uifactory.addTextAreaElement("groupNames", "bgcopywizard.multiple.groupnames", -1, 4, 10, true, false, "", formLayout);
		bgMax = uifactory.addTextElement("fe_bgMax", "create.form.title.max", 3, "", formLayout);
		if (defaultMaxValue != null) {
			bgMax.setValue (defaultMaxValue.toString());
		}
		bgMax.setRegexMatchCheck("^[0-9]*$", "create.form.error.numberOrNull");
		bgMax.setDisplaySize(3);
		uifactory.addFormSubmitButton("finish", formLayout);
	}
}
