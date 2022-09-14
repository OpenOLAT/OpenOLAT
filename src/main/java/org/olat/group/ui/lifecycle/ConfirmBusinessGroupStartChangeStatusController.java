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
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.id.Roles;
import org.olat.core.util.Util;
import org.olat.group.BusinessGroup;
import org.olat.group.BusinessGroupLifecycleManager;
import org.olat.group.BusinessGroupService;
import org.olat.group.BusinessGroupStatusEnum;
import org.olat.group.ui.BGMailHelper;
import org.olat.group.ui.main.BusinessGroupListController;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 22 sept. 2021<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class ConfirmBusinessGroupStartChangeStatusController extends FormBasicController {

	private final BusinessGroupStatusEnum nextStatus;
	private final List<BusinessGroup> groups;
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private BusinessGroupService businessGroupService;
	@Autowired
	private BusinessGroupLifecycleManager businessGroupLifecycleManager;
	
	public ConfirmBusinessGroupStartChangeStatusController(UserRequest ureq, WindowControl wControl,
			List<BusinessGroup> groupsToDelete, BusinessGroupStatusEnum nextStatus) {
		super(ureq, wControl, "confirm_start_change", Util.createPackageTranslator(BusinessGroupListController.class, ureq.getLocale()));
		this.nextStatus = nextStatus;
		this.groups = groupsToDelete;
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		String i18nAction = "send.mail";
		if(formLayout instanceof FormLayoutContainer) {
			FormLayoutContainer layoutCont = (FormLayoutContainer)formLayout;
			String names = BGMailHelper.joinNames(groups);
			if(nextStatus == BusinessGroupStatusEnum.inactive) {
				if(groups.size() == 1) {
					layoutCont.contextPut("msg", translate("dialog.modal.bg.start.inactivate.text.singular", names));
				} else {
					layoutCont.contextPut("msg", translate("dialog.modal.bg.start.inactivate.text.plural", names));
				}
				i18nAction = "inactivate.group.start";
			} else if(nextStatus == BusinessGroupStatusEnum.trash || nextStatus == BusinessGroupStatusEnum.deleted) {
				if(groups.size() == 1) {
					layoutCont.contextPut("msg", translate("dialog.modal.bg.start.delete.text.singular", names));
				} else {
					layoutCont.contextPut("msg", translate("dialog.modal.bg.start.delete.text.plural", names));
				}
				i18nAction = "soft.delete.group.start";
			}
		}

		uifactory.addFormCancelButton("cancel", formLayout, ureq, getWindowControl());
		uifactory.addFormSubmitButton("change.status", i18nAction, formLayout);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		doChangeStatus(ureq);
		fireEvent(ureq, Event.DONE_EVENT);
	}
	
	private void doChangeStatus(UserRequest ureq) {
		Roles roles = ureq.getUserSession().getRoles();

		for(BusinessGroup businessGroup:groups) {
			//check security
			boolean ow = roles.isAdministrator() || roles.isGroupManager()
					|| businessGroupService.hasRoles(getIdentity(), businessGroup, GroupRoles.coach.name());
			if (ow) {
				if(nextStatus == BusinessGroupStatusEnum.inactive) {
					businessGroupLifecycleManager.sendInactivationEmail(businessGroup, getIdentity());
				} else if(nextStatus == BusinessGroupStatusEnum.trash) {
					businessGroupLifecycleManager.sendDeleteSoftlyEmail(businessGroup, getIdentity());
				}
			}
		}
		dbInstance.commit();

		if(nextStatus == BusinessGroupStatusEnum.inactive) {
			showInfo("info.start.inactivation");
		} else if(nextStatus == BusinessGroupStatusEnum.trash) {
			showInfo("info.start.deletion");
		}
	}

	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}
}
