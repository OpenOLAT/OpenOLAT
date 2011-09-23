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
* <p>
*/ 

package org.olat.repository;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.SelectionElement;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.elements.StaticTextElement;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.repository.handlers.RepositoryHandler;
import org.olat.repository.handlers.RepositoryHandlerFactory;

/**
 * Initial Date:  08.07.2003
 *
 * @author Mike Stock
 * 
 * Comment:  
 * 
 */
public class PropPupForm extends FormBasicController {

	private RepositoryEntry entry;
	private StaticTextElement resourceName;
	private StaticTextElement initialAuthor;
	private TextElement type;
	private SelectionElement canCopy;
	private SelectionElement canReference;
	private SelectionElement canLaunch;
	private SelectionElement canDownload;
	private SingleSelection access;
	
	private String[] keys, values;
	
	private RepositoryHandler handler = null;
	/**
	 * The details form is initialized with data collected from entry and typeName. Handler is looked-up by
	 * the given handlerName and not by the entry's resourceableType. This is to allow for an entry with no resourceable
	 * to initialize correctly (c.f. RepositoryAdd workflow). The typeName may be null.
	 */
	public PropPupForm(UserRequest ureq, WindowControl wControl, RepositoryEntry entry) {
		super(ureq, wControl);
		this.entry = entry;
		
		String typeName = entry.getOlatResource().getResourceableTypeName();
		if (typeName != null) {
			handler = RepositoryHandlerFactory.getInstance().getRepositoryHandler(typeName);
		}
		
		keys = new String[] {
				"" + RepositoryEntry.ACC_OWNERS,
				"" + RepositoryEntry.ACC_OWNERS_AUTHORS,
				"" + RepositoryEntry.ACC_USERS,
				"" + RepositoryEntry.ACC_USERS_GUESTS
		};
		
		values = new String[] {
				translate("cif.access.owners"),
				translate("cif.access.owners_authors"),
				translate("cif.access.users"),
				translate("cif.access.users_guests"),
		};
	
		initForm(ureq);
	}

	/**
	 * @return Resource name filed.
	 */
	public String getResourceName() {
		return resourceName.getValue();
	}

	/**
	 * @return Author field.
	 */
	public String getAuthor() {
		return initialAuthor.getValue();
	}

	/**
	 * @return Type field.
	 */
	public String getType() {
		return type.getValue();
	}

	/**
	 * Return true when 'canCopy' is selected. 
	 */
	public boolean canCopy() {
		return canCopy.isSelected(0);
	}
		
	/**
	 * Return true when 'canReference' is selected. 
	 */
	public boolean canReference() {
		return canReference.isSelected(0);
	}

	/**
	 * Return true when 'canLaunch' is selected. 
	 */
	public boolean canLaunch() {
		return canLaunch.isSelected(0);
	}

	/**
	 * Return true when 'canDownload' is selected.
	 */
	public boolean canDownload() {
		return canDownload.isSelected(0);
	}
	
	/**
	 * Return selected access key (ACC_OWNERS, ACC_OWNERS_AUTHORS,
	 * ACC_USERS, ACC_USERS_GUESTS)
	 */
	public int getAccess() {
		return Integer.parseInt(access.getSelectedKey());
	}

	@Override
	protected void formOK(UserRequest ureq) {
		fireEvent (ureq, Event.DONE_EVENT);
	}
	
	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent (ureq, Event.CANCELLED_EVENT);
	}
	
	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		
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
			
		access = uifactory.addRadiosVertical("cif_access", "cif.access", formLayout, keys, values);
		access.select("" + entry.getAccess(), true);
	
		uifactory.addFormSubmitButton("submit", formLayout);
	}

	@Override
	protected void doDispose() {
		//
	}

}