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
package org.olat.repository.ui.author;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.Util;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryAllowToLeaveOptions;
import org.olat.repository.RepositoryEntryManagedFlag;
import org.olat.repository.RepositoryModule;
import org.olat.repository.RepositoryService;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 28.01.2015<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class AuthoringEditAllowToLeaveOptionController extends FormBasicController {
	
	private static final String[] leaveKeys = new String[]{
		RepositoryEntryAllowToLeaveOptions.atAnyTime.name(),
		RepositoryEntryAllowToLeaveOptions.afterEndDate.name(),
		RepositoryEntryAllowToLeaveOptions.never.name()
	};
	
	private SingleSelection leaveEl;
	
	private RepositoryEntry entry;
	@Autowired
	private RepositoryModule repositoryModule;
	
	public AuthoringEditAllowToLeaveOptionController(UserRequest ureq, WindowControl wControl, RepositoryEntry entry) {
		super(ureq, wControl);
		setTranslator(Util.createPackageTranslator(RepositoryService.class, getLocale(), getTranslator()));
		this.entry = entry;
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		setFormTitle("rentry.leaving.title");
		
		String[] leaveValues = new String[]{
				translate("rentry.leave.atanytime"),
				translate("rentry.leave.afterenddate"),
				translate("rentry.leave.never")
		};
		
		final boolean managedLeaving = RepositoryEntryManagedFlag.isManaged(entry, RepositoryEntryManagedFlag.membersmanagement);
		leaveEl = uifactory.addDropdownSingleselect("entry.leave", "rentry.leave.option", formLayout, leaveKeys, leaveValues, null);
		boolean found = false;
		for(String leaveKey:leaveKeys) {
			if(leaveKey.equals(entry.getAllowToLeaveOption().name())) {
				leaveEl.select(leaveKey, true);
				found = true;
			}
		}
		if(!found) {
			if(managedLeaving) {
				leaveEl.select(RepositoryEntryAllowToLeaveOptions.never.name(), true);
			} else {
				RepositoryEntryAllowToLeaveOptions defaultOption = repositoryModule.getAllowToLeaveDefaultOption();
				leaveEl.select(defaultOption.name(), true);
			}
		}
		leaveEl.setEnabled(!managedLeaving);
		
		if(!managedLeaving) {
			uifactory.addFormSubmitButton("submit", formLayout);
		}
	}
	
	public RepositoryEntryAllowToLeaveOptions getSelectedLeaveSetting() {
		RepositoryEntryAllowToLeaveOptions setting;
		if(leaveEl.isOneSelected()) {
			setting = RepositoryEntryAllowToLeaveOptions.valueOf(leaveEl.getSelectedKey());
		} else {
			setting = RepositoryEntryAllowToLeaveOptions.atAnyTime;
		}
		return setting;
	}

	@Override
	protected void formOK(UserRequest ureq) {
		fireEvent(ureq, Event.DONE_EVENT);
	}
}
