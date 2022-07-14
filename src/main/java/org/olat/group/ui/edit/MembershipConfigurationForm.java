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
package org.olat.group.ui.edit;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.SelectionElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.id.Roles;
import org.olat.group.BusinessGroup;
import org.olat.group.BusinessGroupModule;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 31.10.2014<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class MembershipConfigurationForm extends FormBasicController {

	private SelectionElement allowToLeaveEl;
	
	private final boolean managed;
	private final boolean readOnly;
	
	@Autowired
	private BusinessGroupModule businessGroupModule;

	/**
	 * 
	 * @param ureq
	 * @param wControl
	 * @param managed
	 */
	public MembershipConfigurationForm(UserRequest ureq, WindowControl wControl, boolean managed, boolean readOnly) {
		super(ureq, wControl, LAYOUT_DEFAULT_6_6);
		this.managed = managed;
		this.readOnly = readOnly;
		initForm(ureq);
	}
	
	public boolean isAllowToLeaveBusinessGroup() {
		return allowToLeaveEl.isSelected(0);
	}
	
	public void setMembershipConfiguration(BusinessGroup group) {
		if(allowToLeaveEl.isEnabled()) {
			allowToLeaveEl.select("xx", group.isAllowToLeave());
		}
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}
	
	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(allowToLeaveEl == source) {
			fireEvent (ureq, Event.CHANGED_EVENT);
		}
	}
	
	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		allowToLeaveEl = uifactory.addCheckboxesHorizontal("allow.leaving", "allow.leaving.group", formLayout, new String[]{"xx"}, new String[]{""});
		if(readOnly) {
			allowToLeaveEl.setEnabled(false);
		} else {
			allowToLeaveEl.addActionListener(FormEvent.ONCLICK);
		}
		
		if(managed) {
			allowToLeaveEl.setEnabled(false);
		} else if(businessGroupModule.isAllowLeavingGroupOverride()) {
			allowToLeaveEl.setEnabled(true);
		} else {
			allowToLeaveEl.setEnabled(false);
			Roles roles = ureq.getUserSession().getRoles();
			if(roles.isAuthor()) {
				allowToLeaveEl.select("xx", businessGroupModule.isAllowLeavingGroupCreatedByAuthors());
			} else {
				allowToLeaveEl.select("xx", businessGroupModule.isAllowLeavingGroupCreatedByLearners());
			}
		}
	}
}