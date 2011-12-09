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

import org.olat.ControllerFactory;
import org.olat.core.gui.UserRequest;

import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.SelectionElement;
import org.olat.core.gui.components.form.flexible.elements.StaticTextElement;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
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
public class DetailsReadOnlyForm extends FormBasicController {

	private RepositoryEntry entry;
	private String typeName;
	private boolean enableAuthorView = false;
	
	private StaticTextElement displayNameReadOnly;
	final public static int MAX_DISPLAYNAME = 100;
	private StaticTextElement cifId;
	private StaticTextElement resourceName;
	private StaticTextElement initialAuthor;
	private StaticTextElement type;
	private StaticTextElement descriptionReadOnly;
	private SelectionElement canCopy;
	private SelectionElement canReference;
	private SelectionElement canLaunch;
	private SelectionElement canDownload;
	private SelectionElement access;
	
	/**
	 * The details form is initialized with data collected from entry and typeName. Handler is looked-up by
	 * the given handlerName and not by the entry's resourceableType. This is to allow for an entry with no resourceable
	 * to initialize correctly (c.f. RepositoryAdd workflow). The typeName may be null.
	 * 
	 * @param name
	 * @param t
	 * @param entry
	 * @param typeName
	 * @param enableAuthorView
	 */
	public DetailsReadOnlyForm(UserRequest ureq, WindowControl wControl, RepositoryEntry entry, String typeName, boolean enableAuthorView) {
		super(ureq, wControl);
		this.entry = entry;
		this.typeName = typeName;
		this.enableAuthorView = enableAuthorView;
		initForm (ureq);
	}

	
	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		cifId = uifactory.addStaticTextElement("cif_id", "cif.id", entry.getKey().toString(), formLayout);
		cifId.setVisible(enableAuthorView);
		
		displayNameReadOnly = uifactory.addStaticTextElement("cif_displayname", "cif.displayname", entry.getDisplayname(), formLayout);
		initialAuthor = uifactory.addStaticTextElement("cif_initialAuthor", "cif.initialAuthor", entry.getInitialAuthor(), formLayout);
		descriptionReadOnly = uifactory.addStaticTextElement("cif_description", "cif.description", (entry.getDescription() != null) ? entry.getDescription() : " ", formLayout);
		
		resourceName = uifactory.addStaticTextElement("cif_resourcename", "cif.resourcename", (entry.getResourcename() == null) ? "-" : entry.getResourcename(), formLayout);
	
		StringBuilder typeDisplayText = new StringBuilder(100);
		if (typeName != null) { // add image and typename code
			RepositoryEntryIconRenderer reir = new RepositoryEntryIconRenderer(getLocale());
			typeDisplayText.append("<span class=\"b_with_small_icon_left ");
			typeDisplayText.append(reir.getIconCssClass(entry));
			typeDisplayText.append("\">");
			String tName = ControllerFactory.translateResourceableTypeName(typeName, getLocale());
			typeDisplayText.append(tName);
			typeDisplayText.append("</span>"); 
		} else {
			typeDisplayText.append(translate("cif.type.na"));
		}
		
		type = uifactory.addStaticTextElement("cif_type", "cif.type", typeDisplayText.toString(), formLayout);
		
		RepositoryHandler handler = null;
		if (typeName != null) handler = RepositoryHandlerFactory.getInstance().getRepositoryHandler(typeName);
		
		canCopy = uifactory.addCheckboxesVertical("cif_canCopy", "cif.canCopy", formLayout, new String[]{"xx"}, new String[]{null}, null, 1);
		canCopy.setVisible(enableAuthorView);
		canCopy.setEnabled(false); // It is readonly

		canReference = uifactory.addCheckboxesVertical("cif_canReference", "cif.canReference", formLayout, new String[]{"xx"}, new String[]{null}, null, 1);
		canReference.select("xx", entry.getCanReference());
		canReference.setVisible(enableAuthorView);
		canReference.setEnabled(false); // It is readonly
	
		canLaunch = uifactory.addCheckboxesVertical("cif_canLaunch", "cif.canLaunch", formLayout, new String[]{"xx"}, new String[]{null}, null, 1);
		canLaunch.select("xx", entry.getCanLaunch());
		canLaunch.setVisible(handler != null && handler.supportsLaunch(this.entry));
		canLaunch.setEnabled(false); // It is readonly
		if (handler == null || !handler.supportsLaunch(this.entry)) {
			canLaunch.setExampleKey("cif.canLaunch.na", null);
		}
		
		canDownload = uifactory.addCheckboxesVertical("cif_canDownload", "cif.canDownload", formLayout, new String[]{"xx"}, new String[]{null}, null, 1);
		canDownload.select("xx", entry.getCanDownload());
		canDownload.setVisible(handler != null && handler.supportsDownload(this.entry));
		canDownload.setEnabled(false); // It is readonly
		if (handler == null || !handler.supportsDownload(this.entry)) {
			canDownload.setExampleKey("cif.canDownload.na", null);
		}
		
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
		access.setVisible(enableAuthorView);
		//fxdiff VCRP-1,2: access control of resources
		if(entry.isMembersOnly()) {
			access.select(RepositoryEntry.MEMBERS_ONLY, true);
		} else {
			access.select(Integer.toString(entry.getAccess()), true);
		}
	}

	
	public boolean validateFormLogic (UserRequest ureq) {
		return true;
	}

	@Override
	protected void formOK(UserRequest ureq) {
	}


	@Override
	protected void doDispose() {
		//
	}

}