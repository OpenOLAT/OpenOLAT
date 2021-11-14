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
import org.olat.core.commons.persistence.DB;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.id.Roles;
import org.olat.core.util.Util;
import org.olat.group.BusinessGroup;
import org.olat.group.BusinessGroupLifecycleManager;
import org.olat.group.BusinessGroupModule;
import org.olat.group.BusinessGroupService;
import org.olat.group.BusinessGroupStatusEnum;
import org.olat.group.ui.BGMailHelper;
import org.olat.group.ui.main.BusinessGroupListController;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 2 sept. 2021<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class ConfirmBusinessGroupChangeStatusController extends FormBasicController {

	private MultipleSelectionElement notificationEl;
	
	private boolean hasMembers = false;
	private final BusinessGroupStatusEnum newStatus;
	private final List<BusinessGroup> groups;
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private BusinessGroupModule businessGroupModule;
	@Autowired
	private BusinessGroupService businessGroupService;
	@Autowired
	private BusinessGroupLifecycleManager businessGroupLifecycleManager;
	
	public ConfirmBusinessGroupChangeStatusController(UserRequest ureq, WindowControl wControl,
			List<BusinessGroup> groupsToDelete, BusinessGroupStatusEnum newStatus) {
		super(ureq, wControl, "confirm_status", Util.createPackageTranslator(BusinessGroupListController.class, ureq.getLocale()));
		this.newStatus = newStatus;
		this.groups = groupsToDelete;
		for(BusinessGroup group:groups) {
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
			FormLayoutContainer layoutCont = (FormLayoutContainer)formLayout;
			String names = BGMailHelper.joinNames(groups);
			if(newStatus == BusinessGroupStatusEnum.inactive) {
				if(groups.size() == 1) {
					layoutCont.contextPut("msg", translate("dialog.modal.bg.inactivate.text.singular", new String[] { names }));
				} else {
					layoutCont.contextPut("msg", translate("dialog.modal.bg.inactivate.text.plural", new String[] { names }));
				}
			} else if(newStatus == BusinessGroupStatusEnum.trash || newStatus == BusinessGroupStatusEnum.deleted) {
				if(groups.size() == 1) {
					layoutCont.contextPut("msg", translate("dialog.modal.bg.delete.text.singular", new String[] { names }));
				} else {
					layoutCont.contextPut("msg", translate("dialog.modal.bg.delete.text.plural", new String[] { names }));
				}
			}
		}

		String[] notifications = new String[] { translate("dialog.modal.bg.mail.text") };
		notificationEl = uifactory.addCheckboxesHorizontal("notifications", "dialog.modal.bg.mail.text", formLayout, new String[]{ "" },  notifications);
		notificationEl.setVisible(hasMembers);
		if((newStatus == BusinessGroupStatusEnum.inactive && businessGroupModule.isMailAfterDeactivation())
				|| ((newStatus == BusinessGroupStatusEnum.trash || newStatus == BusinessGroupStatusEnum.deleted)
						&& businessGroupModule.isMailAfterSoftDelete())) {
			notificationEl.select("", true);
		}

		uifactory.addFormCancelButton("cancel", formLayout, ureq, getWindowControl());
		String changeKey = "change.status";
		if(newStatus == BusinessGroupStatusEnum.inactive) {
			changeKey = "inactivate.group";
		} else if(newStatus == BusinessGroupStatusEnum.trash || newStatus == BusinessGroupStatusEnum.deleted) {
			changeKey = "soft.delete.group.action";
		}
		uifactory.addFormSubmitButton("change.status", changeKey, formLayout);
	}

	protected boolean isSendMail() {
		return notificationEl.isVisible() && notificationEl.isAtLeastSelected(1);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		doChangeStatus(ureq);
		fireEvent(ureq, Event.DONE_EVENT);
	}
	
	private void doChangeStatus(UserRequest ureq) {
		Roles roles = ureq.getUserSession().getRoles();
		boolean withMail = isSendMail();
		
		for(BusinessGroup businessGroup:groups) {
			//check security
			boolean ow = roles.isAdministrator() || roles.isGroupManager()
					|| businessGroupService.hasRoles(getIdentity(), businessGroup, GroupRoles.coach.name());
			if (ow) {
				if(newStatus == BusinessGroupStatusEnum.inactive) {
					businessGroupLifecycleManager.inactivateBusinessGroup(businessGroup, getIdentity(), withMail);
				} else if(newStatus == BusinessGroupStatusEnum.trash) {
					businessGroupLifecycleManager.deleteBusinessGroupSoftly(businessGroup, getIdentity(), withMail);
				}
			}
		}
		dbInstance.commit();
		if(newStatus == BusinessGroupStatusEnum.inactive) {
			showInfo("info.group.inactivated");
		} else if(newStatus == BusinessGroupStatusEnum.trash) {
			showInfo("info.group.deleted");
		}
	}

	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}
}
