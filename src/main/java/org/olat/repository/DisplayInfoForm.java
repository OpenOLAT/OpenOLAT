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


import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.SelectionElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
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
	private SelectionElement canLaunch;
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
		
		canLaunch = uifactory.addCheckboxesVertical("cif_canLaunch", "cif.canLaunch", formLayout, new String[]{"xx"}, new String[]{null}, null, 1);
		canLaunch.select("xx", entry.getCanLaunch());
		canLaunch.setVisible(handler != null && handler.supportsLaunch(this.entry));

		canDownload = uifactory.addCheckboxesVertical("cif_canDownload", "cif.canDownload", formLayout, new String[]{"xx"}, new String[]{null}, null, 1);
		canDownload.select("xx", entry.getCanDownload());
		canDownload.setVisible(handler != null && handler.supportsDownload(this.entry));
		
		String[] keys = new String[] {
					"" + RepositoryEntry.ACC_OWNERS,
					"" + RepositoryEntry.ACC_OWNERS_AUTHORS,
					"" + RepositoryEntry.ACC_USERS,
					"" + RepositoryEntry.ACC_USERS_GUESTS,
					RepositoryEntry.MEMBERS_ONLY//fxdiff VCRP-1,2: access control of resources
		};
		String[] values = new String[] {
					translate("cif.access.owners"),
					translate("cif.access.owners_authors"),
					translate("cif.access.users"),
					translate("cif.access.users_guests"),
					translate("cif.access.membersonly")//fxdiff VCRP-1,2: access control of resources
		};
		access = uifactory.addRadiosVertical("cif_access", "cif.access", formLayout, keys, values);
		//fxdiff VCRP-1,2: access control of resources
		if(entry.isMembersOnly()) {
			access.select(RepositoryEntry.MEMBERS_ONLY, true);
		} else if (entry.getAccess() > 0) {
			access.select(""+entry.getAccess(), true);
		}

		flc.setEnabled(false);
	}

	@Override
	protected void doDispose() {
		//
	}
}