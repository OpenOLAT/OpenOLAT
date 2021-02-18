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
package org.olat.core.commons.modules.bc.meta;

import java.text.DateFormat;
import java.util.HashSet;
import java.util.Set;

import org.olat.core.commons.modules.bc.FolderLicenseHandler;
import org.olat.core.commons.services.license.License;
import org.olat.core.commons.services.license.LicenseModule;
import org.olat.core.commons.services.license.LicenseService;
import org.olat.core.commons.services.license.LicenseType;
import org.olat.core.commons.services.license.ui.LicenseSelectionConfig;
import org.olat.core.commons.services.license.ui.LicenseUIFactory;
import org.olat.core.commons.services.vfs.VFSMetadata;
import org.olat.core.commons.services.vfs.VFSRepositoryService;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.elements.TextAreaElement;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.folder.FolderHelper;
import org.olat.core.util.FileUtils;
import org.olat.core.util.Formatter;
import org.olat.core.util.StringHelper;
import org.olat.core.util.vfs.VFSConstants;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.core.util.vfs.VFSItem;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.core.util.vfs.VFSLockApplicationType;
import org.olat.core.util.vfs.VFSLockManager;
import org.olat.core.util.vfs.lock.LockInfo;
import org.olat.user.UserManager;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * This is the metadata flexiform controller with or without upload capability.
 * 
 * <P>
 * Initial Date: Jun 24, 2009 <br>
 * 
 * @author gwassmann
 */
public class MetaInfoFormController extends FormBasicController {
	private VFSItem item;
	private FormLink moreMetaDataLink;
	private String initialFilename;
	private TextElement filename, title, publisher, creator, sourceEl, city, pages, language, url, comment, publicationMonth, publicationYear;
	private SingleSelection licenseEl;
	private TextElement licensorEl;
	private TextAreaElement licenseFreetextEl;
	private SingleSelection locked;
	
	private boolean isSubform;
	private boolean showFilename = true;
	private Set<FormItem> metaFields;
	private String resourceUrl;
	
	@Autowired
	private UserManager userManager;
	@Autowired
	private VFSLockManager vfsLockManager;
	@Autowired
	private VFSRepositoryService vfsRepositoryService;
	@Autowired
	private LicenseModule licenseModule;
	@Autowired
	private LicenseService licenseService;
	@Autowired
	private FolderLicenseHandler licenseHandler;

	/**
	 * Use this controller for editing meta data of an existing file.
	 * 
	 * @param ureq
	 * @param control
	 */
	public MetaInfoFormController(UserRequest ureq, WindowControl control, VFSItem item, String resourceUrl) {
		super(ureq, control);
		isSubform = false;
		this.item = item;
		this.resourceUrl = resourceUrl;
		// load the metainfo
		initForm(ureq);
	}

	/**
	 * Use this constructor in a subform
	 * 
	 * @param ureq
	 * @param control
	 * @param uploadLimitKB
	 * @param remainingQuotaKB
	 */
	public MetaInfoFormController(UserRequest ureq, WindowControl control, Form parentForm, boolean showFilename) {
		super(ureq, control, FormBasicController.LAYOUT_DEFAULT, null, parentForm);
		this.isSubform = true;
		this.showFilename = showFilename;
		initForm(ureq);
	}
	
	/**
	 * Use this if you want to use this controller in a subform but for an already existing VFS item.
	 * 
	 * @param ureq User request
	 * @param wControl Window control
	 * @param parentForm Parent form
	 * @param vfsItem VFS item for which you want to edit the metadata
	 */
	public MetaInfoFormController(UserRequest ureq, WindowControl wControl, Form parentForm, VFSItem vfsItem) {
		super(ureq, wControl, FormBasicController.LAYOUT_DEFAULT, null, parentForm);
		this.isSubform = true;
		this.item = vfsItem;
		initForm(ureq);
	}
	
	@Override
	protected void doDispose() {
	// nothing so far
	}

	@Override
	protected void formOK(UserRequest ureq) {
		// done, parent controller takes care of saving metadata...
		fireEvent(ureq, Event.DONE_EVENT);
	}

	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (source == moreMetaDataLink && event.wasTriggerdBy(FormEvent.ONCLICK)) {
			// show metadata
			// and hide link
			setMetaFieldsVisible(true);
			flc.setDirty(true);
			moreMetaDataLink.setVisible(false);
		} else if (source == licenseEl) {
			LicenseUIFactory.updateVisibility(licenseEl, licensorEl, licenseFreetextEl);
		}
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		if(isSubform) {
			setFormTitle("mf.metadata.title");
		}
		setFormContextHelp("Course Element: Folder#_metadata_folder");
		
		VFSMetadata meta = item == null ? null : item.getMetaInfo();

		// title
		String titleVal = (meta != null ? meta.getTitle() : null);
		title = uifactory.addTextElement("title", "mf.title", -1, titleVal, formLayout);
		
		// filename
		initialFilename = (item == null ? null : item.getName());
		filename = uifactory.addTextElement("filename", "mf.filename", -1, initialFilename, formLayout);
		filename.setEnabled(item == null || item.canRename() == VFSConstants.YES);
		filename.setNotEmptyCheck("mf.error.empty");
		filename.setMandatory(true);
		filename.setVisible(showFilename);

		// comment/description
		String commentVal = (meta != null ? meta.getComment() : null);
		comment = uifactory.addTextAreaElement("comment", "mf.comment", -1, 3, 1, true, false, commentVal, formLayout);
		
		// license
		if (licenseModule.isEnabled(licenseHandler)) {
			License license = vfsRepositoryService.getOrCreateLicense(meta, getIdentity());

			LicenseSelectionConfig licenseSelectionConfig = LicenseUIFactory
					.createLicenseSelectionConfig(licenseHandler, license.getLicenseType());
			licenseEl = uifactory.addDropdownSingleselect("mf.license", formLayout,
					licenseSelectionConfig.getLicenseTypeKeys(),
					licenseSelectionConfig.getLicenseTypeValues(getLocale()));
			licenseEl.setMandatory(licenseSelectionConfig.isLicenseMandatory());
			if (licenseSelectionConfig.getSelectionLicenseTypeKey() != null) {
				licenseEl.select(licenseSelectionConfig.getSelectionLicenseTypeKey(), true);
			}
			licenseEl.addActionListener(FormEvent.ONCHANGE);
			
			licensorEl = uifactory.addTextElement("mf.licensor", 1000, license.getLicensor(), formLayout);

			String freetext = licenseService.isFreetext(license.getLicenseType()) ? license.getFreetext() : "";
			licenseFreetextEl = uifactory.addTextAreaElement("mf.freetext", 4, 72, freetext, formLayout);
			LicenseUIFactory.updateVisibility(licenseEl, licensorEl, licenseFreetextEl);
		}

		// creator
		String creatorVal = (meta != null ? meta.getCreator() : null);
		creator = uifactory.addTextElement("creator", "mf.creator", -1, creatorVal, formLayout);

		// publisher
		String publisherVal = (meta != null ? meta.getPublisher() : null);
		publisher = uifactory.addTextElement("publisher", "mf.publisher", -1, publisherVal, formLayout);

		// source/origin
		String sourceVal = (meta != null ? meta.getSource() : null);
		sourceEl = uifactory.addTextElement("source", "mf.source", -1, sourceVal, formLayout);

		// city
		String cityVal = (meta != null ? meta.getCity() : null);
		city = uifactory.addTextElement("city", "mf.city", -1, cityVal, formLayout);

		// publish date
		String datePage = velocity_root + "/date.html";
		FormLayoutContainer publicationDate = FormLayoutContainer.createCustomFormLayout("publicationDateLayout", getTranslator(), datePage);
		publicationDate.setLabel("mf.publishDate", null);
		formLayout.add(publicationDate);

		String[] pubDate = (meta != null ? meta.getPublicationDate() : new String[] { "", "" });
		publicationMonth = uifactory.addTextElement("publicationMonth", "mf.month", 2, StringHelper.escapeHtml(pubDate[1]), publicationDate);
		publicationMonth.setDomReplacementWrapperRequired(false);
		publicationMonth.setMaxLength(2);
		publicationMonth.setDisplaySize(2);

		publicationYear = uifactory.addTextElement("publicationYear", "mf.year", 4, StringHelper.escapeHtml(pubDate[0]), publicationDate);
		publicationYear.setDomReplacementWrapperRequired(false);
		publicationYear.setMaxLength(4);
		publicationYear.setDisplaySize(4);

		// number of pages
		String pageVal = (meta != null ? meta.getPages() : null);
		pages = uifactory.addTextElement("pages", "mf.pages", -1, pageVal, formLayout);

		// language
		String langVal = (meta != null ? meta.getLanguage() : null);
		language = uifactory.addTextElement("language", "mf.language", -1, langVal, formLayout);

		// url/link
		String urlVal = (meta != null ? meta.getUrl() : null);
		url = uifactory.addTextElement("url", "mf.url", -1, urlVal, formLayout);	

		/* static fields */
		String sizeText;
		String typeText;
		if (item instanceof VFSLeaf) {
			sizeText = Formatter.formatBytes(((VFSLeaf) item).getSize());
			typeText = FolderHelper.extractFileType(item.getName(), getLocale());
		} else {
			sizeText = "-";
			typeText = translate("mf.type.directory");
		}

		// Targets to hide
		metaFields = new HashSet<>();
		metaFields.add(creator);
		metaFields.add(publisher);
		metaFields.add(sourceEl);
		metaFields.add(city);
		metaFields.add(publicationDate);
		metaFields.add(pages);
		metaFields.add(language);
		metaFields.add(url);

		if (!hasMetadata(meta)) {
			moreMetaDataLink = uifactory.addFormLink("mf.more.meta.link", formLayout, Link.LINK_CUSTOM_CSS);
			setMetaFieldsVisible(false);
		}

		if (!isSubform) {

			if(meta != null && !(item instanceof VFSContainer)) {
				LockInfo lock = vfsLockManager.getLock(item);
				//locked
				String lockedTitle = getTranslator().translate("mf.locked");
				String unlockedTitle = getTranslator().translate("mf.unlocked");
				locked = uifactory.addRadiosHorizontal("locked","mf.locked",formLayout, new String[]{"lock","unlock"}, new String[]{lockedTitle, unlockedTitle});
				locked.setHelpText(getTranslator().translate("mf.locked.help"));
				if(vfsLockManager.isLocked(item, VFSLockApplicationType.vfs, null)) {
					locked.select("lock", true);
				} else {
					locked.select("unlock", true);
				}
				boolean lockForMe = vfsLockManager.isLockedForMe(item, getIdentity(), VFSLockApplicationType.vfs, null);
				locked.setEnabled(!lockForMe);
				
				//locked by
				String lockedDetails = "";
				if(lock != null) {
					String user = userManager.getUserDisplayName(lock.getLockedBy());
					user = StringHelper.escapeHtml(user);
					String date = "";
					if (lock.getCreationDate() != null) {
						date = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT, getLocale()).format(lock.getCreationDate());
					}
					lockedDetails = getTranslator().translate("mf.locked.description", new String[]{user, date});
				} else {
					lockedDetails = getTranslator().translate("mf.unlocked.description");
				}
				uifactory.addStaticTextElement("mf.lockedBy", lockedDetails, formLayout);
			}
			
			String author = userManager.getUserDisplayName(meta == null ? null : meta.getFileInitializedBy());
			uifactory.addStaticTextElement("mf.author", StringHelper.escapeHtml(author), formLayout);

			uifactory.addStaticTextElement("mf.size", StringHelper.escapeHtml(sizeText), formLayout);

			String lastModified = meta == null ? "" : Formatter.getInstance(getLocale()).formatDate(meta.getFileLastModified());
			uifactory.addStaticTextElement("mf.lastModified", lastModified, formLayout);
			
			String modifiedBy = userManager.getUserDisplayName(meta == null ? null : meta.getFileLastModifiedBy());
			uifactory.addStaticTextElement("mf.modified.by", StringHelper.escapeHtml(modifiedBy), formLayout);

			uifactory.addStaticTextElement("mf.type", StringHelper.escapeHtml(typeText), formLayout);

			String downloads = meta == null ? "" : String.valueOf(meta.getDownloadCount());
			uifactory.addStaticTextElement("mf.downloads", downloads, formLayout);
		}
		
		boolean xssErrors = false;
		if(item != null) {
			xssErrors = StringHelper.xssScanForErrors(item.getName());
		}
		
		if(StringHelper.containsNonWhitespace(resourceUrl) && !xssErrors) {
			String externalUrlPage = velocity_root + "/external_url.html";
			FormLayoutContainer extUrlCont = FormLayoutContainer.createCustomFormLayout("external.url", getTranslator(), externalUrlPage);
			extUrlCont.setLabel("external.url", null);
			extUrlCont.contextPut("resourceUrl", resourceUrl);
			extUrlCont.setRootForm(mainForm);
			formLayout.add(extUrlCont);
		}

		if (!isSubform && meta != null && item instanceof VFSContainer) {
			// Don't show any meta data except title and comment if the item is
			// a directory.
			// Hide the metadata.
			setMetaFieldsVisible(false);
			if (moreMetaDataLink != null) moreMetaDataLink.setVisible(false);
		}

		// save and cancel buttons
		if (!isSubform) {
			final FormLayoutContainer buttonLayout = FormLayoutContainer.createButtonLayout("buttonLayout", getTranslator());
			formLayout.add(buttonLayout);
			if(meta != null) {
				uifactory.addFormSubmitButton("submit", buttonLayout);
			}
			uifactory.addFormCancelButton("cancel", buttonLayout, ureq, getWindowControl());
		}
	}

	/**
	 * @return True if one or more metadata fields are non-emtpy.
	 */
	private boolean hasMetadata(VFSMetadata meta) {
		if (meta != null) { return StringHelper.containsNonWhitespace(meta.getCreator())
				|| StringHelper.containsNonWhitespace(meta.getPublisher()) || StringHelper.containsNonWhitespace(meta.getSource())
				|| StringHelper.containsNonWhitespace(meta.getCity()) || StringHelper.containsNonWhitespace(meta.getPublicationDate()[0])
				|| StringHelper.containsNonWhitespace(meta.getPublicationDate()[1]) || StringHelper.containsNonWhitespace(meta.getPages())
				|| StringHelper.containsNonWhitespace(meta.getLanguage()) || StringHelper.containsNonWhitespace(meta.getUrl())
				|| meta.getLicenseType() != null || StringHelper.containsNonWhitespace(meta.getLicenseTypeName())
				|| StringHelper.containsNonWhitespace(meta.getLicenseText()) || StringHelper.containsNonWhitespace(meta.getLicensor());
				}
		return false;
	}

	/**
	 * Shows or hides the metadata
	 * 
	 * @param visible
	 */
	private void setMetaFieldsVisible(boolean visible) {
		for (FormItem formItem : metaFields) {
			formItem.setVisible(visible);
		}
	}

	/**
	 * @return true if the item has been given a new name by the user.
	 */
	public boolean isFileRenamed() {
		if(initialFilename==null || filename==null) {
			return false;
		}
		return (!initialFilename.equals(filename.getValue()));
	}

	/**
	 * @return The filename
	 */
	public String getFilename() {
		return filename.getValue();
	}
	
	public TextElement getFilenameEl() {
		return filename;
	}
	
	public void setFilename(String name) {
		filename.setValue(name);
	}
	
	public VFSMetadata getMetaInfo(VFSMetadata meta) {
		meta.setCreator(creator.getValue());
		meta.setComment(comment.getValue());
		meta.setTitle(title.getValue());
		meta.setPublisher(publisher.getValue());
		meta.setPublicationDate(publicationMonth.getValue(), publicationYear.getValue());
		meta.setCity(city.getValue());
		meta.setLanguage(language.getValue());
		meta.setSource(sourceEl.getValue());
		meta.setUrl(url.getValue());
		meta.setPages(pages.getValue());
		License license = getLicenseFromFormItems();
		meta.setLicenseType(license.getLicenseType() == null ? null : license.getLicenseType());
		meta.setLicenseTypeName(license.getLicenseType() != null? license.getLicenseType().getName(): "");
		meta.setLicensor(license.getLicensor() != null? license.getLicensor(): "");
		meta.setLicenseText(LicenseUIFactory.getLicenseText(license));
		return meta;
	}
	
	private License getLicenseFromFormItems() {
		License license = licenseService.createLicense(null);
		String licensor = "";
		String freetext = "";
		if (licenseModule.isEnabled(licenseHandler)) {
			if (licenseEl != null && licenseEl.isOneSelected()) {
				String licenseTypeKey = licenseEl.getSelectedKey();
				LicenseType licneseType = licenseService.loadLicenseTypeByKey(licenseTypeKey);
				license.setLicenseType(licneseType);
			}
			if (licensorEl != null && licensorEl.isVisible() && StringHelper.containsNonWhitespace(licensorEl.getValue())) {
				licensor = licensorEl.getValue();
			}
			if (licenseFreetextEl != null && licenseFreetextEl.isVisible() && StringHelper.containsNonWhitespace(licenseFreetextEl.getValue())) {
				freetext = licenseFreetextEl.getValue();
			}
			licensorEl.setValue(license.getLicensor());
			licenseFreetextEl.setValue(license.getFreetext());
		}
		license.setLicensor(licensor);
		license.setFreetext(freetext);
		return license;
	}

	/**
	 * @return The updated MeatInfo object
	 */
	public VFSMetadata getMetaInfo() {
		if (!isSubform && (item instanceof VFSLeaf) && (locked != null && locked.isEnabled())) {
			//isSubForm
			boolean alreadyLocked = vfsLockManager.isLocked(item, VFSLockApplicationType.vfs, null);
			boolean currentlyLocked = locked.isSelected(0);
			if(!currentlyLocked || !alreadyLocked) {
				if(currentlyLocked) {
					vfsLockManager.lock(item, getIdentity(), VFSLockApplicationType.vfs, null);
				} else {
					vfsLockManager.unlock(item, VFSLockApplicationType.vfs);
				}
			}
		}
		
		VFSMetadata meta = item == null ? null : item.getMetaInfo();
		if(meta == null) {
			return null;
		}
		return getMetaInfo(meta);
	}

	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean valid = super.validateFormLogic(ureq);

		// validate publication month
		String monthStr = publicationMonth.getValue();
		if (StringHelper.containsNonWhitespace(monthStr)) {
			int month = -1;
			try {
				month = Integer.valueOf(monthStr.trim());
			} catch (NumberFormatException e) {
				// monthStr is non valid integer value
			}
			if (!(month >= 1 && month <= 12)) {
				// publicationMonth is invalid
				publicationMonth.setErrorKey("mf.wrong.month.value", null);
				valid = false;
			}
		}

		// validate publication year
		String yearStr = publicationYear.getValue();
		if (StringHelper.containsNonWhitespace(yearStr)) {
			try {
				Integer.valueOf(yearStr.trim());
			} catch (NumberFormatException e) {
				// yearStr is non valid integer value
				publicationYear.setErrorKey("mf.wrong.year.value", null);
				valid = false;
			}
		}
				
		if(isFileRenamed()) {
			//check if filetype is directory
			if(!FileUtils.validateFilename(getFilename())) {
				valid = false;
				if (item instanceof VFSContainer) {
					filename.setErrorKey("folder.name.notvalid", new String[0]);
				} else {					
					filename.setErrorKey("file.name.notvalid", new String[0]);
				}
			}			
		}
		
		if (licenseEl != null) {
			licenseEl.clearError();
			if (LicenseUIFactory.validateLicenseTypeMandatoryButNonSelected(licenseEl)) {
				licenseEl.setErrorKey("form.legende.mandatory", null);
				valid &= false;
			}
		}
		
		valid &= validateTextfield(language, 16);
		valid &= validateTextfield(title, 2000);
		valid &= validateTextfield(comment, 32000);
		valid &= validateTextfield(publisher, 2000);
		valid &= validateTextfield(creator, 2000);
		valid &= validateTextfield(city, 255);
		valid &= validateTextfield(sourceEl, 2000);
		valid &= validateTextfield(pages, 2000);
		valid &= validateTextfield(filename, 255);
		valid &= validateTextfield(url, 4000);
		
		return valid;
	}
	
	private boolean validateTextfield(TextElement textEl, int maxSize) {
		boolean allOk = true;
		
		textEl.clearError();
		if(textEl.getValue() != null && textEl.getValue().length() >= maxSize) {
			textEl.setErrorKey("form.error.toolong", new String[] { Integer.toString(maxSize) });
			allOk &= false;
		}
		
		return allOk;
	}
	
	/**
	 * Get the form item representing this form
	 * 
	 * @return
	 */
	public FormItem getFormItem() {
		return this.flc;
	}

}
