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
package org.olat.course.nodes.iq;

import java.util.Collection;

import org.olat.basesecurity.GroupRoles;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.util.SelectionValues;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.StringHelper;
import org.olat.course.editor.NodeEditController;
import org.olat.course.nodes.CourseNode;
import org.olat.modules.ModuleConfiguration;

/**
 * 
 * Initial date: 21 févr. 2022<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class IQCommunicationConfigurationController extends FormBasicController {
	
	private SingleSelection participantsEl;
	private MultipleSelectionElement rolesEl;
	
	private final ModuleConfiguration modConfig;
	
	public IQCommunicationConfigurationController(UserRequest ureq, WindowControl wControl, CourseNode courseNode) {
		super(ureq, wControl);
		this.modConfig = courseNode.getModuleConfiguration();
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		setFormTitle("im.config.title");
		
		SelectionValues yesNoValues = new SelectionValues();
		yesNoValues.add(SelectionValues.entry("true", translate("yes")));
		yesNoValues.add(SelectionValues.entry("false", translate("no")));
		
		participantsEl = uifactory.addRadiosHorizontal("im.config.participants", "im.config.participants", formLayout, yesNoValues.keys(), yesNoValues.values());
		boolean participantCanStart = modConfig.getBooleanSafe(IQEditController.CONFIG_KEY_IM_PARTICIPANT_CAN_START, false);
		if(participantCanStart) {
			participantsEl.select("true", true);
		} else {
			participantsEl.select("false", true);
		}

		SelectionValues rolesValues = new SelectionValues();
		rolesValues.add(SelectionValues.entry(GroupRoles.owner.name(), translate("owner")));
		rolesValues.add(SelectionValues.entry(GroupRoles.coach.name(), translate("coach")));
		rolesEl = uifactory.addCheckboxesVertical("im.config.roles", "im.config.roles", formLayout,
				rolesValues.keys(), rolesValues.values(), 1);
		String rolesStr = modConfig.getStringValue(IQEditController.CONFIG_KEY_IM_NOTIFICATIONS_ROLES, "coach");
		if(StringHelper.containsNonWhitespace(rolesStr)) {
			String[] rolesArr = rolesStr.split("[,]");
			for(int i=rolesArr.length; i-->0; ) {
				String role = rolesArr[i];
				if(rolesValues.containsKey(role)) {
					rolesEl.select(role, true);
				}
			}
		}
		
		FormLayoutContainer buttonsCont = FormLayoutContainer.createButtonLayout("buttons", getTranslator());
		formLayout.add(buttonsCont);
		uifactory.addFormSubmitButton("save", buttonsCont);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		boolean participantCanStart = participantsEl.isOneSelected() && "true".equals(participantsEl.getSelectedKey());
		modConfig.setBooleanEntry(IQEditController.CONFIG_KEY_IM_PARTICIPANT_CAN_START, participantCanStart);
		
		Collection<String> roles = rolesEl.getSelectedKeys();
		String rolesStr = String.join(",", roles);
		modConfig.setStringValue(IQEditController.CONFIG_KEY_IM_NOTIFICATIONS_ROLES, rolesStr);

		fireEvent(ureq, NodeEditController.NODECONFIG_CHANGED_EVENT);
	}
}
