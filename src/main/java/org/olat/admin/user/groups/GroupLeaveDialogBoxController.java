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
package org.olat.admin.user.groups;

import java.util.Collections;
import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.id.Identity;
import org.olat.core.util.Util;
import org.olat.group.BusinessGroup;
import org.olat.group.ui.main.BusinessGroupListController;
import org.olat.user.UserManager;

/**
 * 
 * 
 * 
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class GroupLeaveDialogBoxController extends FormBasicController {
	
	private MultipleSelectionElement sendMail;
	private MultipleSelectionElement groupDeleteEl;
	
	private final String[] keys = {"send"};
	
	private final Identity leavingIdentity;
	private final List<BusinessGroup> groupsToLeave;
	private final List<BusinessGroup> groupsToDelete;
	
	public GroupLeaveDialogBoxController(UserRequest ureq, WindowControl wControl, Identity leavingIdentity,
			List<BusinessGroup> groupsToLeave, List<BusinessGroup> groupsToDelete) {
		super(ureq, wControl, null, Util.createPackageTranslator(BusinessGroupListController.class, ureq.getLocale()));
		this.leavingIdentity = leavingIdentity;
		this.groupsToLeave = groupsToLeave;
		this.groupsToDelete = groupsToDelete;
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		StringBuilder groupToLeaveNames = new StringBuilder();
		for(BusinessGroup group:groupsToLeave) {
			if(groupToLeaveNames.length() > 0) groupToLeaveNames.append(", ");
			groupToLeaveNames.append(group.getName());
		}
		String identityName = UserManager.getInstance().getUserDisplayName(leavingIdentity);
		String leaveText = translate("unsubscribe.text", new String[]{identityName, groupToLeaveNames.toString()});
		uifactory.addStaticTextElement("leaving.desc", null, leaveText, formLayout);
		String[] values = new String[]{
				translate("dialog.modal.bg.mail.text")
		};
		sendMail = uifactory.addCheckboxesHorizontal("send.mail", null, formLayout, keys, values);

		if(!groupsToDelete.isEmpty()) {
			String deletMsg = translate("unsubscribe.group.del");
			uifactory.addStaticTextElement("delete.desc", null, deletMsg, formLayout);
			 
			String[] delValues = new String[]{
					translate("group.delete.confirmation")
			};
			groupDeleteEl = uifactory.addCheckboxesHorizontal("group.del", null, formLayout, keys, delValues);
			groupDeleteEl.select(keys[0], true);
		}
		
		FormLayoutContainer buttonsContainer = FormLayoutContainer.createButtonLayout("buttons", getTranslator());
		buttonsContainer.setRootForm(mainForm);
		formLayout.add(buttonsContainer);
		uifactory.addFormSubmitButton("deleteButton", "ok", buttonsContainer);
		uifactory.addFormCancelButton("cancel", buttonsContainer, ureq, getWindowControl());
	}

	public boolean isSendMail() {
		return sendMail.isAtLeastSelected(1);
	}
	
	public boolean isGroupDelete() {
		return groupDeleteEl == null ? false : groupDeleteEl.isAtLeastSelected(1);
	}

	public List<BusinessGroup> getGroupsToDelete() {
		if(groupDeleteEl != null && groupDeleteEl.isAtLeastSelected(1)) {
			return groupsToDelete;
		}
		return Collections.emptyList();
	}
	
	public List<BusinessGroup> getGroupsToLeave() {
		return groupsToLeave;
	}

	@Override
	protected void formOK(UserRequest ureq) {
		fireEvent(ureq, Event.DONE_EVENT);
	}

	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}
}
