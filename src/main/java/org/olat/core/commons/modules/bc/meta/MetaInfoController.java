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
package org.olat.core.commons.modules.bc.meta;

import java.text.DateFormat;
import java.util.HashSet;
import java.util.Set;

import org.olat.core.CoreSpringFactory;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.elements.StaticTextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.folder.FolderHelper;
import org.olat.core.util.Formatter;
import org.olat.core.util.StringHelper;
import org.olat.core.util.vfs.OlatRelPathImpl;
import org.olat.core.util.vfs.VFSItem;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.core.util.vfs.VFSLockManager;
import org.olat.core.util.vfs.lock.LockInfo;
import org.olat.user.UserManager;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Read only variant to show the meta datas of a file
 * 
 * 
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class MetaInfoController extends FormBasicController {
	private VFSItem item;
	private FormLink moreMetaDataLink;
	private StaticTextElement  publisher, creator, sourceEl, city, pages, language, url;
	private SingleSelection locked;
	private Set<FormItem> metaFields;
	private String resourceUrl;
	
	@Autowired
	private UserManager userManager;
	@Autowired
	private VFSLockManager vfsLockManager;

	/**
	 * Use this controller for editing meta data of an existing file.
	 * 
	 * @param ureq
	 * @param control
	 */
	public MetaInfoController(UserRequest ureq, WindowControl control, VFSItem item, String resourceUrl) {
		super(ureq, control);
		this.item = item;
		this.resourceUrl = resourceUrl;
		initForm(ureq);
	}
	
	/**
	 * @see org.olat.core.gui.components.form.flexible.impl.FormBasicController#doDispose()
	 */
	@Override
	protected void doDispose() {
		// nothing so far
	}

	/**
	 * @see org.olat.core.gui.components.form.flexible.impl.FormBasicController#formOK(org.olat.core.gui.UserRequest)
	 */
	@Override
	protected void formOK(UserRequest ureq) {
		//do nothing
	}

	/**
	 * @see org.olat.core.gui.components.form.flexible.impl.FormBasicController#formCancelled(org.olat.core.gui.UserRequest)
	 */
	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}

	/**
	 * @see org.olat.core.gui.components.form.flexible.impl.FormBasicController#formInnerEvent(org.olat.core.gui.UserRequest,
	 *      org.olat.core.gui.components.form.flexible.FormItem,
	 *      org.olat.core.gui.components.form.flexible.impl.FormEvent)
	 */
	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (source == moreMetaDataLink && event.wasTriggerdBy(FormEvent.ONCLICK)) {
			// show metadata
			// and hide link
			setMetaFieldsVisible(true);
			flc.setDirty(true);
			moreMetaDataLink.setVisible(false);
		}
	}

	/**
	 * @see org.olat.core.gui.components.form.flexible.impl.FormBasicController#initForm(org.olat.core.gui.components.form.flexible.FormItemContainer,
	 *      org.olat.core.gui.control.Controller, org.olat.core.gui.UserRequest)
	 */
	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		setFormTitle("mf.metadata.title");
		setFormContextHelp("Folders#_metadata");

		// filename
		uifactory.addStaticTextElement("mf.filename", item.getName(), formLayout);

		MetaInfo meta = item == null ? null :
			CoreSpringFactory.getImpl(MetaInfoFactory.class).createMetaInfoFor((OlatRelPathImpl) item);

		// title
		String titleVal = StringHelper.escapeHtml(meta != null ? meta.getTitle() : null);
		uifactory.addStaticTextElement("mf.title", titleVal, formLayout);

		// comment/description
		String commentVal =  StringHelper.xssScan(meta != null ? meta.getComment() : null);
		uifactory.addStaticTextElement("mf.comment", commentVal, formLayout);

		// creator
		String creatorVal = StringHelper.escapeHtml(meta != null ? meta.getCreator() : null);
		creator = uifactory.addStaticTextElement("mf.creator", creatorVal, formLayout);

		// publisher
		String publisherVal = StringHelper.escapeHtml(meta != null ? meta.getPublisher() : null);
		publisher = uifactory.addStaticTextElement("mf.publisher", publisherVal, formLayout);

		// source/origin
		String sourceVal = StringHelper.escapeHtml(meta != null ? meta.getSource() : null);
		sourceEl = uifactory.addStaticTextElement("mf.source", sourceVal, formLayout);

		// city
		String cityVal = StringHelper.escapeHtml(meta != null ? meta.getCity() : null);
		city = uifactory.addStaticTextElement("mf.city", cityVal, formLayout);

		// publish date
		FormLayoutContainer publicationDate = FormLayoutContainer.createHorizontalFormLayout("publicationDateLayout", getTranslator());
		publicationDate.setLabel("mf.publishDate", null);
		formLayout.add(publicationDate);

		String[] pubDate = (meta != null ? meta.getPublicationDate() : new String[] { "", "" });
		uifactory.addStaticTextElement("mf.month", StringHelper.escapeHtml(pubDate[1]), publicationDate);
		uifactory.addStaticTextElement("mf.year", StringHelper.escapeHtml(pubDate[0]), publicationDate);


		// number of pages
		String pageVal = StringHelper.escapeHtml(meta != null ? meta.getPages() : null);
		pages = uifactory.addStaticTextElement("mf.pages", pageVal, formLayout);

		// language
		String langVal = StringHelper.escapeHtml(meta != null ? meta.getLanguage() : null);
		language = uifactory.addStaticTextElement("mf.language", langVal, formLayout);

		// url/link
		String urlVal = StringHelper.escapeHtml(meta != null ? meta.getUrl() : null);
		url = uifactory.addStaticTextElement("mf.url", urlVal, formLayout);

		/* static fields */
		String sizeText, typeText;
		if (item instanceof VFSLeaf) {
			sizeText = Formatter.formatBytes(((VFSLeaf) item).getSize());
			typeText = FolderHelper.extractFileType(item.getName(), getLocale());
		} else {
			sizeText = "-";
			typeText = translate("mf.type.directory");
		}

		// Targets to hide
		metaFields = new HashSet<FormItem>();
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

		if(meta != null && !meta.isDirectory()) {
			LockInfo lock = vfsLockManager.getLock(item);
			//locked
			String lockedTitle = getTranslator().translate("mf.locked");
			String unlockedTitle = getTranslator().translate("mf.unlocked");
			locked = uifactory.addRadiosHorizontal("locked","mf.locked",formLayout, new String[]{"lock","unlock"}, new String[]{lockedTitle, unlockedTitle});
			if(vfsLockManager.isLocked(item)) {
				locked.select("lock", true);
			} else {
				locked.select("unlock", true);
			}
			locked.setEnabled(false);
			
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
				if(lock.isWebDAVLock()) {
					lockedDetails += " (WebDAV)";
				}
			} else {
				lockedDetails = getTranslator().translate("mf.unlocked.description");
			}
			uifactory.addStaticTextElement("mf.lockedBy", lockedDetails, formLayout);
			
			// username
			String author = StringHelper.escapeHtml(meta == null ? "" : meta.getHTMLFormattedAuthor());
			uifactory.addStaticTextElement("mf.author", author, formLayout);

			// filesize
			uifactory.addStaticTextElement("mf.size", StringHelper.escapeHtml(sizeText), formLayout);

			// last modified date
			String lastModified = meta == null ? "" : StringHelper.formatLocaleDate(meta.getLastModified(), getLocale());
			uifactory.addStaticTextElement("mf.lastModified", lastModified, formLayout);

			// file type
			uifactory.addStaticTextElement("mf.type", StringHelper.escapeHtml(typeText), formLayout);

			String downloads = meta == null ? "" : String.valueOf(meta.getDownloadCount());
			uifactory.addStaticTextElement("mf.downloads", downloads, formLayout);

			// Don't show any meta data except title and comment if the item is
			// a directory.
			// Hide the metadata.
			setMetaFieldsVisible(false);
			if (moreMetaDataLink != null) {
				moreMetaDataLink.setVisible(false);
			}
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

		// cancel buttons
		final FormLayoutContainer buttonLayout = FormLayoutContainer.createButtonLayout("buttonLayout", getTranslator());
		formLayout.add(buttonLayout);
		uifactory.addFormCancelButton("cancel", buttonLayout, ureq, getWindowControl());
	}

	/**
	 * @return True if one or more metadata fields are non-emtpy.
	 */
	private boolean hasMetadata(MetaInfo meta) {
		if (meta != null) { return StringHelper.containsNonWhitespace(meta.getCreator())
				|| StringHelper.containsNonWhitespace(meta.getPublisher()) || StringHelper.containsNonWhitespace(meta.getSource())
				|| StringHelper.containsNonWhitespace(meta.getCity()) || StringHelper.containsNonWhitespace(meta.getPublicationDate()[0])
				|| StringHelper.containsNonWhitespace(meta.getPublicationDate()[1]) || StringHelper.containsNonWhitespace(meta.getPages())
				|| StringHelper.containsNonWhitespace(meta.getLanguage()) || StringHelper.containsNonWhitespace(meta.getUrl());
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
}