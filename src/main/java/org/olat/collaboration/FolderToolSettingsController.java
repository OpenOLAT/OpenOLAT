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
package org.olat.collaboration;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.elements.FormSubmit;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;

/**
 * 
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class FolderToolSettingsController extends FormBasicController {
	
	private FormSubmit submit;
	private SingleSelection folderAccessEl;
	private final int folderAccess;
	private final boolean canSave;
	
	public FolderToolSettingsController(UserRequest ureq, WindowControl wControl, int folderAccess) {
		super(ureq, wControl);
		this.folderAccess = folderAccess;
		this.canSave = true;
		initForm(ureq);
	}
	
	public FolderToolSettingsController(UserRequest ureq, WindowControl wControl, Form rootForm, int folderAccess) {
		super(ureq, wControl, LAYOUT_DEFAULT, null, rootForm);
		this.folderAccess = folderAccess;
		this.canSave = false;
		initForm(ureq);	
	}

	public int getFolderAccess() {
		if (folderAccessEl.isOneSelected() && folderAccessEl.getSelectedKey().equals("all")){
			return CollaborationTools.FOLDER_ACCESS_ALL;
		} else {
			return CollaborationTools.FOLDER_ACCESS_OWNERS;
		}
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		setFormTitle("folder.access.title");
		
		String[] keys = new String[] { "owner", "all" };
		String values[] = new String[] {
				translate("folder.access.owners"),
				translate("folder.access.all")
		};
		folderAccessEl = uifactory.addRadiosVertical("folder.access", "folder.access", formLayout, keys, values);
		String selectedKey = (folderAccess == CollaborationTools.FOLDER_ACCESS_ALL) ? "all" : "owner";
		folderAccessEl.select(selectedKey, true);
		if(canSave) {
			submit = uifactory.addFormSubmitButton("submit", formLayout);
		}
	}
	
	public void setEnabled(boolean enabled) {
		folderAccessEl.setEnabled(enabled);
		if(submit != null) {
			submit.setVisible(enabled);
		}
	}

	@Override
	protected void formOK(UserRequest ureq) {
		fireEvent(ureq, Event.DONE_EVENT);
	}
}
