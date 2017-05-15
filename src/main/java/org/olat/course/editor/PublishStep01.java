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
package org.olat.course.editor;

import org.olat.core.CoreSpringFactory;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.SelectionElement;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.wizard.BasicStep;
import org.olat.core.gui.control.generic.wizard.PrevNextFinishConfig;
import org.olat.core.gui.control.generic.wizard.Step;
import org.olat.core.gui.control.generic.wizard.StepFormBasicController;
import org.olat.core.gui.control.generic.wizard.StepFormController;
import org.olat.core.gui.control.generic.wizard.StepsEvent;
import org.olat.core.gui.control.generic.wizard.StepsRunContext;
import org.olat.core.gui.translator.Translator;
import org.olat.core.util.Util;
import org.olat.course.CourseModule;
import org.olat.course.ICourse;
import org.olat.fileresource.types.ScormCPFileResource;
import org.olat.ims.qti.fileresource.SurveyFileResource;
import org.olat.ims.qti.fileresource.TestFileResource;
import org.olat.login.LoginModule;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryAllowToLeaveOptions;
import org.olat.repository.RepositoryEntryManagedFlag;
import org.olat.repository.RepositoryModule;
import org.olat.repository.RepositoryService;
import org.olat.repository.handlers.RepositoryHandler;
import org.olat.repository.handlers.RepositoryHandlerFactory;
import org.olat.repository.ui.author.AuthoringEntryPublishController;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Description:<br>
 * Select the BARG level
 * 
 * <P>
 * Initial Date:  21.01.2008 <br>
 * @author patrickb
 * @author fkiefer
 */
class PublishStep01 extends BasicStep {

	private PrevNextFinishConfig prevNextConfig;

	public PublishStep01(UserRequest ureq, ICourse course, boolean hasPublishableChanges, boolean hasCatalog) {
		super(ureq);
		setI18nTitleAndDescr("publish.access.header", null);
		
		RepositoryModule repositoryModule = CoreSpringFactory.getImpl(RepositoryModule.class);
		if(repositoryModule.isCatalogEnabled()) {
			setNextStep(new PublishStepCatalog(ureq, course, hasPublishableChanges));
			if(hasCatalog) {
				prevNextConfig = PrevNextFinishConfig.BACK_NEXT_FINISH;
			} else {
				prevNextConfig = PrevNextFinishConfig.BACK_NEXT;
			}
		} else if(hasPublishableChanges) {
			setNextStep(new PublishStep00a(ureq));
			prevNextConfig = PrevNextFinishConfig.BACK_NEXT_FINISH;
		} else {
			setNextStep(Step.NOSTEP);
			prevNextConfig = PrevNextFinishConfig.BACK_FINISH;
		}
	}

	/**
	 * @see org.olat.core.gui.control.generic.wizard.BasicStep#getInitialPrevNextFinishConfig()
	 */
	@Override
	public PrevNextFinishConfig getInitialPrevNextFinishConfig() {
		//can go back and finish immediately
		return prevNextConfig;
	}

	/**
	 * @see org.olat.core.gui.control.generic.wizard.BasicStep#getStepController(org.olat.core.gui.UserRequest, org.olat.core.gui.control.WindowControl, org.olat.core.gui.control.generic.wizard.StepsRunContext, org.olat.core.gui.components.form.flexible.impl.Form)
	 */
	@Override
	public StepFormController getStepController(UserRequest ureq, WindowControl wControl, StepsRunContext stepsRunContext, Form form) {
		return new PublishStep01AccessForm(ureq, wControl, form, stepsRunContext);
	}

	class PublishStep01AccessForm extends StepFormBasicController {

		private final String[] leaveKeys = new String[]{
				RepositoryEntryAllowToLeaveOptions.atAnyTime.name(),
				RepositoryEntryAllowToLeaveOptions.afterEndDate.name(),
				RepositoryEntryAllowToLeaveOptions.never.name()
			};
		
		private SingleSelection leaveEl;
		
		private RepositoryEntry entry;
		private SelectionElement canCopy;
		private SelectionElement canReference;
		private SelectionElement canDownload;
		
		private RepositoryHandler handler;

		private SingleSelection authorsSwitch, usersSwitch;
		private SingleSelection publishedForUsers;
		private FormLayoutContainer authorConfigLayout, userConfigLayout;
		
		private static final String YES_KEY = "y";
		private static final String NO_KEY = "n";
		private final String[] yesNoKeys = new String[]{YES_KEY, NO_KEY};

		private static final String OAU_KEY = "u";
		private static final String OAUG_KEY = "g";
		private static final String MEMBERSONLY_KEY = "m";
		private String[] publishedKeys;
		
		@Autowired
		private LoginModule loginModule;
		@Autowired
		private RepositoryModule repositoryModule;
		
		PublishStep01AccessForm(UserRequest ureq, WindowControl control, Form rootForm, StepsRunContext runContext) {
			super(ureq, control, rootForm, runContext, LAYOUT_VERTICAL, null);
			Translator translator = Util.createPackageTranslator(Util.createPackageTranslator(RepositoryService.class,
					AuthoringEntryPublishController.class, getLocale()), getTranslator(), getLocale());
			setTranslator(translator);
			entry = (RepositoryEntry) getFromRunContext("repoEntry");
			initForm(ureq);
		}

		@Override
		protected void doDispose() {
			//
		}

		@Override
		protected void formOK(UserRequest ureq) {
			RepositoryEntryAllowToLeaveOptions setting;
			if(leaveEl.isOneSelected()) {
				setting = RepositoryEntryAllowToLeaveOptions.valueOf(leaveEl.getSelectedKey());
			} else {
				setting = RepositoryEntryAllowToLeaveOptions.atAnyTime;
			}			
			boolean membersOnly = (usersSwitch.getSelectedKey().equals(YES_KEY) && publishedForUsers.getSelectedKey().equals(MEMBERSONLY_KEY));
			CourseAccessAndProperties accessProperties = new CourseAccessAndProperties(entry, setting, getAccess(), membersOnly,
					canCopy.isSelected(0), canReference.isSelected(0), canDownload.isSelected(0));
						
			addToRunContext("accessAndProperties", accessProperties);		

			fireEvent(ureq, StepsEvent.ACTIVATE_NEXT);
		}

		@Override
		protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {			
			FormLayoutContainer headersLayout = FormLayoutContainer.createCustomFormLayout("access", getTranslator(), velocity_root + "/publish_courseaccess.html");
			formLayout.add(headersLayout);
			headersLayout.contextPut("catalogEnabled", repositoryModule.isCatalogEnabled());

			FormLayoutContainer publishLayout = FormLayoutContainer.createDefaultFormLayout("publish", getTranslator());
			formLayout.add(publishLayout);
			publishLayout.setFormTitle(translate("rentry.publish"));
			publishLayout.setFormContextHelp("Course Settings#_zugriff");
			publishLayout.setElementCssClass("o_sel_repositoryentry_access");
			
			if (loginModule.isGuestLoginLinksEnabled()) {
				publishedKeys = new String[]{OAU_KEY, OAUG_KEY, MEMBERSONLY_KEY};
			} else {
				publishedKeys = new String[]{OAU_KEY, MEMBERSONLY_KEY};
			} 

			String resourceType = entry.getOlatResource().getResourceableTypeName();
			if (TestFileResource.TYPE_NAME.equals(resourceType)
				|| SurveyFileResource.TYPE_NAME.equals(resourceType)
				|| ScormCPFileResource.TYPE_NAME.equals(resourceType)) {
				String warning = translate("warn.resource.need.course");
				flc.contextPut("off_warn", warning);
			}
			if (CourseModule.ORES_TYPE_COURSE.equals(resourceType)) {
				publishLayout.setFormDescription(translate("rentry.publish.course.desc"));			
			} else {
				publishLayout.setFormDescription(translate("rentry.publish.other.desc"));			
			}
			if (resourceType != null) {
				handler = RepositoryHandlerFactory.getInstance().getRepositoryHandler(resourceType);
			}
			
			// make configuration read only when managed by external system
			final boolean managedSettings = RepositoryEntryManagedFlag.isManaged(entry, RepositoryEntryManagedFlag.settings);
			final boolean managedAccess = RepositoryEntryManagedFlag.isManaged(entry, RepositoryEntryManagedFlag.access);

			String[] yesNoValues = new String[]{translate("yes"), translate("no")};		
			authorsSwitch = uifactory.addRadiosHorizontal("authorsSwitch", "rentry.publish.authors", publishLayout, yesNoKeys, yesNoValues);
			authorsSwitch.setEnabled(!managedAccess);
			authorsSwitch.addActionListener(FormEvent.ONCHANGE);
			authorConfigLayout = FormLayoutContainer.createBareBoneFormLayout("authorConfigLayout", getTranslator());
			publishLayout.add(authorConfigLayout);
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
				
			usersSwitch = uifactory.addRadiosHorizontal("usersSwitch", "rentry.publish.users", publishLayout, yesNoKeys, yesNoValues);
			usersSwitch.addActionListener(FormEvent.ONCHANGE);
			usersSwitch.setEnabled(!managedAccess);
			userConfigLayout = FormLayoutContainer.createBareBoneFormLayout("userConfigLayout", getTranslator());
			publishLayout.add(userConfigLayout);
			publishedForUsers = uifactory.addDropdownSingleselect("publishedForUsers", null, userConfigLayout, publishedKeys, publishedValues, null);
			publishedForUsers.setEnabled(!managedAccess);
			publishedForUsers.addActionListener(FormEvent.ONCHANGE);
			uifactory.addSpacerElement("userSpacer", userConfigLayout, true);

			FormLayoutContainer membershipLayout = FormLayoutContainer.createDefaultFormLayout("membership", getTranslator());
			formLayout.add(membershipLayout);
			
			membershipLayout.setFormTitle(translate("rentry.leaving.title"));
			
			String[] leaveValues = new String[]{
					translate("rentry.leave.atanytime"),
					translate("rentry.leave.afterenddate"),
					translate("rentry.leave.never")
			};
			
			final boolean managedLeaving = RepositoryEntryManagedFlag.isManaged(entry, RepositoryEntryManagedFlag.membersmanagement);
			leaveEl = uifactory.addDropdownSingleselect("entry.leave", "rentry.leave.option", membershipLayout, leaveKeys, leaveValues, null);
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
						authorsSwitch.select(NO_KEY, false);
						authorsSwitch.setEnabled(false);
					} else {
						authorsSwitch.select(YES_KEY, false);
						authorsSwitch.setEnabled(true);
						authorConfigLayout.setVisible(true);
					}
				} else {
					userConfigLayout.setVisible(false);
					authorsSwitch.setEnabled(true);
				}
			}
		}
		
		private int getAccess() {
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
	}
	
	
	
	public class CourseAccessAndProperties {
		private RepositoryEntry repoEntry;
		private RepositoryEntryAllowToLeaveOptions setting;
		private int access;
		private boolean membersOnly;
		private boolean canCopy;
		private boolean canReference;
		private boolean canDownload;		
		
		public RepositoryEntry getRepositoryEntry() {
			return repoEntry;
		}

		public void setRepositoryEntry(RepositoryEntry re) {
			this.repoEntry = re;
		}

		public RepositoryEntryAllowToLeaveOptions getSetting() {
			return setting;
		}

		public void setSetting(RepositoryEntryAllowToLeaveOptions setting) {
			this.setting = setting;
		}

		public int getAccess() {
			return access;
		}

		public void setAccess(int access) {
			this.access = access;
		}

		public boolean isMembersOnly() {
			return membersOnly;
		}

		public void setMembersOnly(boolean membersOnly) {
			this.membersOnly = membersOnly;
		}

		public boolean isCanCopy() {
			return canCopy;
		}

		public void setCanCopy(boolean canCopy) {
			this.canCopy = canCopy;
		}

		public boolean isCanReference() {
			return canReference;
		}

		public void setCanReference(boolean canReference) {
			this.canReference = canReference;
		}

		public boolean isCanDownload() {
			return canDownload;
		}

		public void setCanDownload(boolean canDownload) {
			this.canDownload = canDownload;
		}

		public CourseAccessAndProperties(RepositoryEntry re) {
			super();
			this.repoEntry = re;
		}

		public CourseAccessAndProperties(RepositoryEntry re, RepositoryEntryAllowToLeaveOptions setting, int access,
				boolean membersOnly, boolean canCopy, boolean canReference, boolean canDownload) {
			super();
			this.repoEntry = re;
			this.setting = setting;
			this.access = access;
			this.membersOnly = membersOnly;
			this.canCopy = canCopy;
			this.canReference = canReference;
			this.canDownload = canDownload;
		}			
		
		
	}
}