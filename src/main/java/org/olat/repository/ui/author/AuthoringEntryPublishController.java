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
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.SelectionElement;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.elements.StaticTextElement;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.Util;
import org.olat.course.CourseModule;
import org.olat.fileresource.types.ScormCPFileResource;
import org.olat.ims.qti.fileresource.SurveyFileResource;
import org.olat.ims.qti.fileresource.TestFileResource;
import org.olat.login.LoginModule;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryManagedFlag;
import org.olat.repository.RepositoryService;
import org.olat.repository.handlers.RepositoryHandler;
import org.olat.repository.handlers.RepositoryHandlerFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Form controller to configure the access and publish settings of a repository
 * entry (OAUG settings).
 * 
 * Initial date: 02.07.2014<br>
 * 
 * @author gnaegi, gnaegi@frentix.com, http://www.frentix.com
 * 
 */
public class AuthoringEntryPublishController extends FormBasicController {

	private RepositoryEntry entry;
	private StaticTextElement resourceName;
	private StaticTextElement initialAuthor;
	private TextElement type;
	private SelectionElement canCopy;
	private SelectionElement canReference;
	private SelectionElement canDownload;
	
	private RepositoryHandler handler;

	private SingleSelection authorsSwitch, usersSwitch;
	private SingleSelection publishedForUsers;
	private FormLayoutContainer authorConfigLayout, userConfigLayout;
	
	private static final String YES_KEY = "y";
	private static final String NO_KEY = "n";
	private static final String[] yesNoKeys = new String[]{YES_KEY, NO_KEY};

	private static final String OAU_KEY = "u";
	private static final String OAUG_KEY = "g";
	private static final String MEMBERSONLY_KEY = "m";
	private String[] publishedKeys;

	@Autowired
	private LoginModule loginModule;
	
	/**
	 * The details form is initialized with data collected from entry and
	 * typeName. Handler is looked-up by the given handlerName and not by the
	 * entry's resourceableType. This is to allow for an entry with no
	 * resourceable to initialize correctly (c.f. RepositoryAdd workflow). The
	 * typeName may be null.
	 */
	public AuthoringEntryPublishController(UserRequest ureq,
			WindowControl wControl, RepositoryEntry entry) {
		super(ureq, wControl);
		setTranslator(Util.createPackageTranslator(RepositoryService.class, getLocale(), getTranslator()));
		this.entry = entry;

		String typeName = entry.getOlatResource().getResourceableTypeName();
		if (typeName != null) {
			handler = RepositoryHandlerFactory.getInstance().getRepositoryHandler(typeName);
		}
		
		if (loginModule.isGuestLoginLinksEnabled()) {
			publishedKeys = new String[]{OAU_KEY, OAUG_KEY, MEMBERSONLY_KEY};
		} else {
			publishedKeys = new String[]{OAU_KEY, MEMBERSONLY_KEY};
		} 
						
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
	 * Return true when 'canDownload' is selected.
	 */
	public boolean canDownload() {
		return canDownload.isSelected(0);
	}

	/**
	 * Return selected access key (ACC_OWNERS, ACC_OWNERS_AUTHORS, ACC_USERS,
	 * ACC_USERS_GUESTS)
	 */
	public int getAccess() {
		// default only for owners
		int access = RepositoryEntry.ACC_OWNERS;
		if (authorsSwitch.getSelectedKey().equals(YES_KEY)) {
			// raise to author level
			access = RepositoryEntry.ACC_OWNERS_AUTHORS;
		}
		if (usersSwitch.getSelectedKey().equals(YES_KEY)) {
			if (publishedForUsers.getSelectedKey().equals(OAU_KEY)) {
				// further raise to user level
				access = RepositoryEntry.ACC_USERS;
			} else if (publishedForUsers.getSelectedKey().equals(OAUG_KEY)) {
				// further raise to guest level
				access = RepositoryEntry.ACC_USERS_GUESTS;
			} else if (publishedForUsers.getSelectedKey().equals(MEMBERSONLY_KEY)) {
			// Members-only is either owner or owner-author level, never user level
				access = RepositoryEntry.ACC_OWNERS;
			}
		}
		return access;
	}

	public boolean isMembersOnly() {
		return (usersSwitch.getSelectedKey().equals(YES_KEY) && publishedForUsers.getSelectedKey().equals(MEMBERSONLY_KEY));
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		setFormTitle("rentry.publish");
		setFormContextHelp("Course Settings#_zugriff");
		formLayout.setElementCssClass("o_sel_repositoryentry_access");

		String resourceType = entry.getOlatResource().getResourceableTypeName();
		if (TestFileResource.TYPE_NAME.equals(resourceType)
			|| SurveyFileResource.TYPE_NAME.equals(resourceType)
			|| ScormCPFileResource.TYPE_NAME.equals(resourceType)) {
			String warning = translate("warn.resource.need.course");
			flc.contextPut("off_warn", warning);
		}
		if (CourseModule.ORES_TYPE_COURSE.equals(resourceType)) {
			setFormDescription("rentry.publish.course.desc");			
		} else {
			setFormDescription("rentry.publish.other.desc");			
		}
		
		// make configuration read only when managed by external system
		final boolean managedSettings = RepositoryEntryManagedFlag.isManaged(entry, RepositoryEntryManagedFlag.settings);
		final boolean managedAccess = RepositoryEntryManagedFlag.isManaged(entry, RepositoryEntryManagedFlag.access);

		String[] yesNoValues = new String[]{translate("yes"), translate("no")};		
		authorsSwitch = uifactory.addRadiosHorizontal("authorsSwitch", "rentry.publish.authors", formLayout, yesNoKeys, yesNoValues);
		authorsSwitch.setEnabled(!managedAccess);
		authorsSwitch.addActionListener(FormEvent.ONCHANGE);
		authorConfigLayout = FormLayoutContainer.createBareBoneFormLayout("authorConfigLayout", getTranslator());
		formLayout.add(authorConfigLayout);
		canReference = uifactory.addCheckboxesVertical("cif_canReference",null, authorConfigLayout, new String[] { YES_KEY }, new String[] { translate("cif.canReference") }, 1);
		canReference.setEnabled(!managedSettings);
		canCopy = uifactory.addCheckboxesVertical("cif_canCopy", null, authorConfigLayout, new String[] { YES_KEY }, new String[] { translate("cif.canCopy") }, 1);
		canCopy.setEnabled(!managedSettings);
		canDownload = uifactory.addCheckboxesVertical("cif_canDownload", null, authorConfigLayout, new String[] { YES_KEY }, new String[] { translate("cif.canDownload") }, 1);
		canDownload.setEnabled(!managedSettings);
		canDownload.setVisible(handler.supportsDownload());
		uifactory.addSpacerElement("authorSpacer", authorConfigLayout, true);

		String[] publishedValues;
		if (loginModule.isGuestLoginLinksEnabled()) {
			publishedValues = new String[]{translate("cif.access.users"), translate("cif.access.users_guests"), translate("cif.access.membersonly")};
		} else {
			publishedValues = new String[]{translate("cif.access.users"), translate("cif.access.membersonly")};
		}
			
		usersSwitch = uifactory.addRadiosHorizontal("usersSwitch", "rentry.publish.users", formLayout, yesNoKeys, yesNoValues);
		usersSwitch.addActionListener(FormEvent.ONCHANGE);
		usersSwitch.setEnabled(!managedAccess);
		userConfigLayout = FormLayoutContainer.createBareBoneFormLayout("userConfigLayout", getTranslator());
		formLayout.add(userConfigLayout);
		publishedForUsers = uifactory.addDropdownSingleselect("publishedForUsers", null, userConfigLayout, publishedKeys, publishedValues, null);
		publishedForUsers.setEnabled(!managedAccess);
		publishedForUsers.addActionListener(FormEvent.ONCHANGE);
		uifactory.addSpacerElement("userSpacer", userConfigLayout, true);

		if (!managedAccess || !managedSettings) {
			uifactory.addFormSubmitButton("submit", formLayout);
		}
		
		initFormData();
	}

	private void initFormData() {
		// init author visibility and flags
		canReference.select(YES_KEY, entry.getCanReference()); 
		canCopy.select(YES_KEY, entry.getCanCopy()); 
		canDownload.select(YES_KEY, entry.getCanDownload());
		if (entry.getAccess() >= RepositoryEntry.ACC_OWNERS_AUTHORS) {
			authorsSwitch.select(YES_KEY, true);
		} else {
			authorsSwitch.select(NO_KEY, true);
			authorConfigLayout.setVisible(false);
		}
		// init user visibility
		if (entry.getAccess() == RepositoryEntry.ACC_USERS) {
			publishedForUsers.select(OAU_KEY, true);
			usersSwitch.select(YES_KEY, true);
		} else if (loginModule.isGuestLoginLinksEnabled() && entry.getAccess() == RepositoryEntry.ACC_USERS_GUESTS){
			publishedForUsers.select(OAUG_KEY, true);			
			usersSwitch.select(YES_KEY, true);
		} else if (entry.isMembersOnly()) {
			publishedForUsers.select(MEMBERSONLY_KEY, true);
			usersSwitch.select(YES_KEY, true);
			authorsSwitch.setEnabled(false);
		} else {
			publishedForUsers.select(OAU_KEY, true);
			usersSwitch.select(NO_KEY, true);
			userConfigLayout.setVisible(false);
		}
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		super.formInnerEvent(ureq, source, event);
		if (source == authorsSwitch) {
			if (authorsSwitch.getSelectedKey().equals(YES_KEY)) {			
				authorConfigLayout.setVisible(true);
			} else {
				authorConfigLayout.setVisible(false);
				if (!publishedForUsers.getSelectedKey().equals(MEMBERSONLY_KEY)) {
					usersSwitch.select(NO_KEY, false);
					userConfigLayout.setVisible(false);
				}
			}
		} else if (source == usersSwitch || source == publishedForUsers) {
			if (usersSwitch.getSelectedKey().equals(YES_KEY)) {			
				userConfigLayout.setVisible(true);
				if (publishedForUsers.getSelectedKey().equals(MEMBERSONLY_KEY)) {
					authorConfigLayout.setVisible(false);
					authorsSwitch.select(NO_KEY, true);
					authorsSwitch.setEnabled(false);
				} else {
					authorsSwitch.select(YES_KEY, true);
					authorsSwitch.setEnabled(true);
					authorConfigLayout.setVisible(true);
				}
			} else {
				userConfigLayout.setVisible(false);
				authorsSwitch.setEnabled(true);
			}
		}
	}
	
	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}
	
	@Override
	protected void formOK(UserRequest ureq) {
		fireEvent(ureq, Event.DONE_EVENT);
	}
	
	@Override
	protected void doDispose() {
		//
	}

}