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

package org.olat.repository;


import java.util.ArrayList;
import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.SelectionElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.login.LoginModule;
import org.olat.repository.handlers.RepositoryHandler;
import org.olat.repository.handlers.RepositoryHandlerFactory;


/**
 * @author Ingmar Kroll
 * 
 * Comment:  
 * 
 */
public class DisplayInfoForm extends FormBasicController {

	private SelectionElement canCopy;
	private SelectionElement canReference;
	private SelectionElement canDownload;
	private SelectionElement access;
	
	private RepositoryEntry entry;
	private RepositoryHandler handler;
	
	public DisplayInfoForm(UserRequest ureq, WindowControl wControl, RepositoryEntry entry) {
		super(ureq, wControl);
		this.entry = entry;
		handler = RepositoryHandlerFactory.getInstance().getRepositoryHandler(entry);
		initForm (ureq);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		setFormTitle("tab.public");
		setFormContextHelp("org.olat.repository","rep-meta-olatauthor.html","help.hover.rep.detail");
		
		canCopy = uifactory.addCheckboxesVertical("cif_canCopy", "cif.canCopy", formLayout, new String[]{"xx"}, new String[]{null}, null, 1);
		canCopy.select("xx", entry.getCanCopy());
		
		canReference = uifactory.addCheckboxesVertical("cif_canReference", "cif.canReference", formLayout, new String[]{"xx"}, new String[]{null}, null, 1);
		canReference.select("xx", entry.getCanReference());
		
		canDownload = uifactory.addCheckboxesVertical("cif_canDownload", "cif.canDownload", formLayout, new String[]{"xx"}, new String[]{null}, null, 1);
		canDownload.select("xx", entry.getCanDownload());
		canDownload.setVisible(handler != null && handler.supportsDownload(this.entry));
		
		List<String> keyList = new ArrayList<String>();
		keyList.add(Integer.toString(RepositoryEntry.ACC_OWNERS));
		keyList.add(Integer.toString(RepositoryEntry.ACC_OWNERS_AUTHORS));
		keyList.add(Integer.toString(RepositoryEntry.ACC_USERS));
		if(LoginModule.isGuestLoginLinksEnabled()) {
			keyList.add(Integer.toString(RepositoryEntry.ACC_USERS_GUESTS));
		}
		keyList.add(RepositoryEntry.MEMBERS_ONLY);
		String[] keys = keyList.toArray(new String[keyList.size()]);

		List<String> valueList = new ArrayList<String>();
		valueList.add(translate("cif.access.owners"));
		valueList.add(translate("cif.access.owners_authors"));
		valueList.add(translate("cif.access.users"));
		if(LoginModule.isGuestLoginLinksEnabled()) {
			valueList.add(translate("cif.access.users_guests"));
		}
		valueList.add(translate("cif.access.membersonly"));
		String[] values = valueList.toArray(new String[valueList.size()]);
		
		access = uifactory.addRadiosVertical("cif_access", "cif.access", formLayout, keys, values);
		//fxdiff VCRP-1,2: access control of resources
		if(entry.isMembersOnly()) {
			access.select(RepositoryEntry.MEMBERS_ONLY, true);
		} else if (entry.getAccess() > 0) {
			if(!LoginModule.isGuestLoginLinksEnabled() && entry.getAccess() == RepositoryEntry.ACC_USERS_GUESTS) {
				access.select(Integer.toString(RepositoryEntry.ACC_USERS), true);
			} else {
				access.select(Integer.toString(entry.getAccess()), true);
			}
		}

		flc.setEnabled(false);
	}

	@Override
	protected void doDispose() {
		//
	}
}