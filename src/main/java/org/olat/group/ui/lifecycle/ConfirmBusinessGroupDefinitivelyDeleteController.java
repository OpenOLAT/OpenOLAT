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
package org.olat.group.ui.lifecycle;

import java.util.List;

import org.olat.basesecurity.GroupRoles;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.form.flexible.impl.elements.FormSubmit;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.id.Roles;
import org.olat.core.logging.activity.ThreadLocalUserActivityLogger;
import org.olat.core.util.Util;
import org.olat.group.BusinessGroup;
import org.olat.group.BusinessGroupLifecycleManager;
import org.olat.group.BusinessGroupService;
import org.olat.group.GroupLoggingAction;
import org.olat.group.ui.BGMailHelper;
import org.olat.group.ui.main.BusinessGroupListController;
import org.olat.util.logging.activity.LoggingResourceable;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 21 sept. 2021<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class ConfirmBusinessGroupDefinitivelyDeleteController extends FormBasicController {

	private MultipleSelectionElement notificationEl;
	
	private boolean hasMembers = false;
	private final boolean sendEventFirst;
	private List<BusinessGroup> businessGroups;
	
	@Autowired
	private BusinessGroupService businessGroupService;
	@Autowired
	private BusinessGroupLifecycleManager businessGroupLifecycleManager;

	public ConfirmBusinessGroupDefinitivelyDeleteController(UserRequest ureq, WindowControl wControl,
			List<BusinessGroup> businessGroups, boolean sendEventFirst) {
		super(ureq, wControl, "confirm_delete", Util.createPackageTranslator(BusinessGroupListController.class, ureq.getLocale()));
		this.businessGroups = businessGroups;
		this.sendEventFirst = sendEventFirst;
		for(BusinessGroup group:businessGroups) {
			int numOfMembers = businessGroupService.countMembers(group, GroupRoles.coach.name(), GroupRoles.participant.name());
			if(numOfMembers > 0) {
				hasMembers = true;
				break;
			}
		}
		
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		if(formLayout instanceof FormLayoutContainer) {
			String names = BGMailHelper.joinNames(businessGroups);
			String msg;
			if(businessGroups.size() == 1) {
				msg = translate("dialog.modal.bg.definitively.delete.text.singular", new String[] { names });
			} else {
				msg = translate("dialog.modal.bg.definitively.delete.text.plural", new String[] { names });
			}		
			((FormLayoutContainer)formLayout).contextPut("msg", msg);
		}
		
		String[] notifications = new String[] { translate("dialog.modal.bg.mail.text") };
		notificationEl = uifactory.addCheckboxesHorizontal("notifications", "dialog.modal.bg.mail.text", formLayout, new String[]{ "" },  notifications);
		notificationEl.setVisible(hasMembers);
		
		uifactory.addFormCancelButton("cancel", formLayout, ureq, getWindowControl());
		FormSubmit submit = uifactory.addFormSubmitButton("delete.group", "delete.group", formLayout);
		submit.setElementCssClass("btn btn-default btn-danger");
	}

	@Override
	protected void doDispose() {
		//
	}

	@Override
	protected void formOK(UserRequest ureq) {
		Roles roles = ureq.getUserSession().getRoles();
		boolean doSendMail = notificationEl.isVisible() && notificationEl.isAtLeastSelected(1);
		
		if(sendEventFirst) {
			fireEvent(ureq, Event.DONE_EVENT);
		}
		
		for(BusinessGroup group:businessGroups) {
			//check security
			boolean ow = roles.isAdministrator() || roles.isGroupManager()
					|| businessGroupService.hasRoles(getIdentity(), group, GroupRoles.coach.name());
			if (ow) {
				businessGroupLifecycleManager.deleteBusinessGroup(group, getIdentity(), doSendMail);
				ThreadLocalUserActivityLogger.log(GroupLoggingAction.GROUP_DELETED, getClass(), LoggingResourceable.wrap(group));
			}
		}
		
		if(!sendEventFirst) {
			fireEvent(ureq, Event.DONE_EVENT);
		}
		
		showInfo("info.group.deleted");
	}

	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}
}
