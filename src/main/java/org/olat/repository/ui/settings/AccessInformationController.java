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
package org.olat.repository.ui.settings;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.Util;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryStatusEnum;
import org.olat.repository.RepositoryService;

/**
 * 
 * Initial date: 8 Nov 2018<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class AccessInformationController extends FormBasicController {
	
	private final RepositoryEntry entry;
	
	public AccessInformationController(UserRequest ureq, WindowControl wControl, RepositoryEntry entry) {
		super(ureq, wControl, "access_infos", Util.createPackageTranslator(RepositoryService.class, ureq.getLocale()));
		this.entry = entry;
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		if(formLayout instanceof FormLayoutContainer) {
			FormLayoutContainer layoutCont = (FormLayoutContainer)formLayout;
			layoutCont.contextPut("ownerCatalog", getAccess(true));
			layoutCont.contextPut("ownerCondition", translate("access.info.conditions.always"));
			
			RepositoryEntryStatusEnum status = entry.getEntryStatus();
			// coach
			boolean coachAccess = status == RepositoryEntryStatusEnum.coachpublished
					|| status == RepositoryEntryStatusEnum.published
					|| status == RepositoryEntryStatusEnum.closed;
			layoutCont.contextPut("coachCatalog", getAccess(coachAccess));
			layoutCont.contextPut("coachCondition", translate("access.info.conditions.coach"));
			
			// participant
			boolean participantAccess = status == RepositoryEntryStatusEnum.coachpublished
					|| status == RepositoryEntryStatusEnum.published;
			layoutCont.contextPut("participantCatalog", getAccess(participantAccess));
			layoutCont.contextPut("participantCondition", translate("access.info.conditions.participant"));
			
			// all users
			boolean allUsersAccess = participantAccess && (entry.isBookable() || entry.isAllUsers());
			layoutCont.contextPut("allUsersCatalog", getAccess(allUsersAccess));
			if(allUsersAccess) {
				layoutCont.contextPut("allUsersCondition", translate("access.info.conditions.participant"));
			} else {
				layoutCont.contextPut("allUsersCondition", translate("access.info.conditions.not.share.allUsers"));
			}
			
			// guests
			boolean guestsAccess = participantAccess && entry.isGuests();
			layoutCont.contextPut("guestsCatalog", getAccess(guestsAccess));
			if(guestsAccess) {
				layoutCont.contextPut("guestsCondition", translate("access.info.conditions.participant"));
			} else {
				layoutCont.contextPut("guestsCondition", translate("access.info.conditions.not.share.guests"));
			}
		}
	}
	
	private String getAccess(boolean val) {
		return val ? translate("access.info.conditions.yes") : translate("access.info.conditions.no");
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}
}
