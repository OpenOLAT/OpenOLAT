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
import org.olat.core.gui.components.form.flexible.elements.SelectionElement;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.elements.StaticTextElement;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
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
import org.olat.repository.RepositoryEntryStatusEnum;
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
	
	private static final String YES_KEY = "y";
	private static final String[] yesKeys = new String[] { YES_KEY };

	private RepositoryEntry entry;
	private StaticTextElement resourceName;
	private StaticTextElement initialAuthor;
	private TextElement type;
	private SelectionElement canCopy;
	private SelectionElement canReference;
	private SelectionElement canDownload;
	private SelectionElement allUsers;
	private SelectionElement guests;
	private SingleSelection publishedStatus;
	
	private RepositoryHandler handler;
	
	

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
	
	public boolean isAllUsers() {
		return allUsers.isSelected(0);
	}
	
	public boolean isGuests() {
		return guests.isSelected(0);
	}

	/**
	 * Return the publication status
	 */
	public RepositoryEntryStatusEnum getEntryStatus() {
		return RepositoryEntryStatusEnum.valueOf(publishedStatus.getSelectedKey());
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
		final boolean closedOrDeleted = entry.getEntryStatus() == RepositoryEntryStatusEnum.closed
				|| entry.getEntryStatus() == RepositoryEntryStatusEnum.deleted;
		
		String[] publishedKeys;
		String[] publishedValues;
		if(closedOrDeleted) {
			publishedKeys = new String[] {
					RepositoryEntryStatusEnum.preparation.name(), RepositoryEntryStatusEnum.review.name(),
					RepositoryEntryStatusEnum.coachpublished.name(), RepositoryEntryStatusEnum.published.name(),
					RepositoryEntryStatusEnum.closed.name(), RepositoryEntryStatusEnum.deleted.name()
			};
			publishedValues = new String[] {
					translate("cif.status.preparation"), translate("cif.status.review"),
					translate("cif.status.coachpublished"), translate("cif.status.published"),
					translate("cif.status.closed"), translate("cif.status.deleted")
			};
		} else {
			publishedKeys = new String[] {
					RepositoryEntryStatusEnum.preparation.name(), RepositoryEntryStatusEnum.review.name(),
					RepositoryEntryStatusEnum.coachpublished.name(), RepositoryEntryStatusEnum.published.name()
			};
			publishedValues = new String[] {
					translate("cif.status.preparation"), translate("cif.status.review"),
					translate("cif.status.coachpublished"), translate("cif.status.published")
			};
		}
		publishedStatus = uifactory.addDropdownSingleselect("publishedStatus", "cif.publish", formLayout, publishedKeys, publishedValues, null);
		publishedStatus.setElementCssClass("o_sel_repositoryentry_access_publication");
		publishedStatus.setEnabled(!managedAccess && !closedOrDeleted);
		publishedStatus.addActionListener(FormEvent.ONCHANGE);

		uifactory.addSpacerElement("usersSpacer", formLayout, true);
		
		allUsers = uifactory.addCheckboxesVertical("cif.allusers", "cif.allusers", formLayout, yesKeys, new String[] { translate("cif.release") }, 1);
		allUsers.setElementCssClass("o_sel_repositoryentry_access_all_users");
		allUsers.setEnabled(!managedAccess && !closedOrDeleted);
		guests = uifactory.addCheckboxesVertical("cif.guests", "cif.guests", formLayout, yesKeys, new String[] { translate("cif.release") }, 1);
		guests.setElementCssClass("o_sel_repositoryentry_access_all_users");
		guests.setEnabled(!managedAccess && !closedOrDeleted);
		guests.setVisible(loginModule.isGuestLoginEnabled());
		
		uifactory.addSpacerElement("authorSpacer", formLayout, true);

		canReference = uifactory.addCheckboxesVertical("cif_canReference", "cif.author.can", formLayout, yesKeys, new String[] { translate("cif.canReference") }, 1);
		canReference.setEnabled(!managedSettings && !closedOrDeleted);
		canCopy = uifactory.addCheckboxesVertical("cif_canCopy", null, formLayout, yesKeys, new String[] { translate("cif.canCopy") }, 1);
		canCopy.setEnabled(!managedSettings && !closedOrDeleted);
		canDownload = uifactory.addCheckboxesVertical("cif_canDownload", null, formLayout, yesKeys, new String[] { translate("cif.canDownload") }, 1);
		canDownload.setEnabled(!managedSettings && !closedOrDeleted);
		canDownload.setVisible(handler.supportsDownload());

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
		allUsers.select(YES_KEY, entry.isAllUsers());
		guests.select(YES_KEY, entry.isGuests());
		publishedStatus.select(entry.getEntryStatus().name(), true);
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