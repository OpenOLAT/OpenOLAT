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
package org.olat.modules.library.ui;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.elements.StaticTextElement;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.util.StringHelper;
import org.olat.core.util.mail.MailHelper;
import org.olat.fileresource.types.SharedFolderFileResource;
import org.olat.modules.library.LibraryManager;
import org.olat.modules.library.LibraryModule;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryService;
import org.olat.repository.controllers.ReferencableEntriesSearchController;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 29 mars 2019<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class LibraryAdminController extends FormBasicController {
	
	private static final String[] onKeys = new String[]{"xx"};
	
	private MultipleSelectionElement enableEl;
	private TextElement mailAfterUploadEl;
	private TextElement mailAfterFreeingEl;
	private FormLink addSharedFolderButton;
	private FormLink removeSharedFolderButton;
	private StaticTextElement sharedFolderNameEl;
	private FormLayoutContainer sharedFolderCont;
	
	private CloseableModalController cmc;
	private ReferencableEntriesSearchController chooseFolderCtr;
	
	@Autowired
	private LibraryModule libraryModule;
	@Autowired
	private LibraryManager libraryManager;
	@Autowired
	private RepositoryService repositoryService;
	
	public LibraryAdminController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl);
		initForm(ureq);
		updateUI();
		initSharedFolder();
	}
	
	private void initSharedFolder() {
		String entryKey = libraryModule.getLibraryEntryKey();
		if(StringHelper.isLong(entryKey)) {
			RepositoryEntry libraryEntry = repositoryService.loadByKey(Long.valueOf(entryKey));
			if(libraryEntry != null) {
				doSelectSharedFolder(libraryEntry);
			} else {
				doRemoveSharedFolder();
			}
		} else {
			doRemoveSharedFolder();
		}
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		setFormTitle("library.configuration.title");
		formLayout.setElementCssClass("o_sel_library_configuration");
		
		boolean enabled = libraryModule.isEnabled();
		String[] onValues = new String[] { translate("on") };
		enableEl = uifactory.addCheckboxesHorizontal("library.enable", "library.enable", formLayout, onKeys, onValues);
		enableEl.addActionListener(FormEvent.ONCHANGE);
		enableEl.select("xx", enabled);
		
		sharedFolderNameEl = uifactory.addStaticTextElement("library.shared.folder", "library.shared.folder",
				translate("library.no.sharedfolder"), formLayout);
		sharedFolderNameEl.setElementCssClass("o_sel_selected_shared_folder");
		
		sharedFolderCont = FormLayoutContainer.createButtonLayout("sharedButtons", getTranslator());
		formLayout.add(sharedFolderCont);
		removeSharedFolderButton = uifactory.addFormLink("remove.shared.folder", sharedFolderCont, Link.BUTTON);
		addSharedFolderButton = uifactory.addFormLink("add.shared.folder", sharedFolderCont, Link.BUTTON);
		addSharedFolderButton.setElementCssClass("o_sel_add_shared_folder");
		
		String mailAfterUpload = libraryModule.getEmailContactsToNotifyAfterUpload();
		mailAfterUploadEl = uifactory.addTextElement("library.configuration.mail.after.upload", 256, mailAfterUpload, formLayout);
		
		String mailAfterFreeing = libraryModule.getEmailContactsToNotifyAfterFreeing();
		mailAfterFreeingEl = uifactory.addTextElement("library.configuration.mail.after.freeing", 256, mailAfterFreeing, formLayout);

		FormLayoutContainer buttonsCont = FormLayoutContainer.createButtonLayout("buttons", getTranslator());
		formLayout.add(buttonsCont);
		uifactory.addFormSubmitButton("save", buttonsCont);
	}
	
	private void updateUI() {
		boolean enabled = enableEl.isAtLeastSelected(1);
		mailAfterUploadEl.setVisible(enabled);
		mailAfterFreeingEl.setVisible(enabled);
		sharedFolderNameEl.setVisible(enabled);
		sharedFolderCont.setVisible(enabled);
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(chooseFolderCtr == source) {
			if (event == ReferencableEntriesSearchController.EVENT_REPOSITORY_ENTRY_SELECTED) {
				RepositoryEntry repoEntry = chooseFolderCtr.getSelectedEntry();
				doSelectSharedFolder(repoEntry);
			}
			cmc.deactivate();
			cleanUp();
		} else if(cmc == source) {
			cleanUp();
		}
		super.event(ureq, source, event);
	}
	
	private void cleanUp() {
		removeAsListenerAndDispose(chooseFolderCtr);
		removeAsListenerAndDispose(cmc);
		chooseFolderCtr = null;
		cmc = null;
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(enableEl == source) {
			updateUI();
		} else if(removeSharedFolderButton == source) {
			doRemoveSharedFolder();
		} else if(addSharedFolderButton == source) {
			doDisplaySearchController(ureq);
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = super.validateFormLogic(ureq);
		allOk &= validateEmail(mailAfterUploadEl);
		allOk &= validateEmail(mailAfterFreeingEl);
		return allOk;
	}
	
	private boolean validateEmail(TextElement el) {
		boolean allOk = true;
		el.clearError();
		if(StringHelper.containsNonWhitespace(el.getValue()) && !MailHelper.isValidEmailAddress(el.getValue())) {
			el.setErrorKey("error.mail.not.valid", null);
			allOk &= false;
		}
		return allOk;
	}

	@Override
	protected void formOK(UserRequest ureq) {
		boolean enabled = enableEl.isAtLeastSelected(1);
		libraryModule.setEnabled(enabled);
		if(enabled) {
			libraryModule.setEmailContactsToNotifyAfterUpload(mailAfterUploadEl.getValue());
			libraryModule.setEmailContactsToNotifyAfterFreeing(mailAfterFreeingEl.getValue());
			
			RepositoryEntry sharedFolder = (RepositoryEntry)sharedFolderNameEl.getUserObject();
			if(sharedFolder == null) {
				libraryModule.setLibraryEntryKey(null);
				libraryManager.removeExistingLockFile();
			} else if(!sharedFolder.getKey().toString().equals(libraryModule.getLibraryEntryKey())) {
				libraryManager.removeExistingLockFile();
				libraryModule.setLibraryEntryKey(sharedFolder.getKey().toString());
				libraryManager.lockFolderAndPreventDoubleIndexing();
			}	
		} else {
			libraryManager.removeExistingLockFile();
		}
	}
	
	/**
	 * Displays the shared folder search controller
	 * 
	 * @param ureq
	 */
	private void doDisplaySearchController(UserRequest ureq) {
		if(guardModalController(chooseFolderCtr)) return;
		
		String choose = translate("library.catalog.choose.folder.link");
		chooseFolderCtr = new ReferencableEntriesSearchController(getWindowControl(), ureq, SharedFolderFileResource.TYPE_NAME, choose);
		listenTo(chooseFolderCtr);
		
		String title = translate("add.shared.folder");
		cmc = new CloseableModalController(getWindowControl(), "close", chooseFolderCtr.getInitialComponent(), true, title);
		listenTo(cmc);
		cmc.activate();
	}
	
	/**
	 * Updates configuration with selected shared folder.
	 * 
	 * @param repoEntry The shared folder entry
	 * @param ureq The user request
	 */
	private void doSelectSharedFolder(RepositoryEntry repoEntry) {
		sharedFolderNameEl.setValue(StringHelper.escapeHtml(repoEntry.getDisplayname()));
		sharedFolderNameEl.setUserObject(repoEntry);
		removeSharedFolderButton.setVisible(true);
	}

	/**
	 * Removes the current shared folder from the configuration
	 * 
	 * @param ureq The user request
	 */
	private void doRemoveSharedFolder() {			
		sharedFolderNameEl.setValue(translate("library.no.sharedfolder"));
		sharedFolderNameEl.setUserObject(null);
		removeSharedFolderButton.setVisible(false);
	}
}
