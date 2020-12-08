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
package org.olat.modules.webFeed.ui;

import java.io.File;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import org.olat.core.commons.persistence.PersistenceHelper;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FileElement;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.elements.RichTextElement;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.form.flexible.impl.elements.richText.RichTextConfiguration;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.Formatter;
import org.olat.core.util.StringHelper;
import org.olat.core.util.WebappHelper;
import org.olat.core.util.vfs.LocalFileImpl;
import org.olat.core.util.vfs.Quota;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.modules.webFeed.Feed;
import org.olat.modules.webFeed.manager.FeedManager;
import org.olat.modules.webFeed.manager.ValidatedURL;
import org.olat.repository.RepositoryEntry;

/**
 * This controller is responsible for editing feed information. <br />
 * <h3>Events fired by this controller:</h3>
 * <ul>
 * <li>Event.CHANGED_EVENT</li>
 * <li>Event.CANCELLED_EVENT</li>
 * </ul>
 * <P>
 * Initial Date: Feb 5, 2009 <br>
 * 
 * @author Gregor Wassmann, frentix GmbH, http://www.frentix.com
 */
class FeedFormController extends FormBasicController {
	private Feed feed;
	private Quota feedQuota;
	private final boolean canChangeUrl;
	
	private TextElement titleEl;
	private FileElement file;
	private RichTextElement descriptionEl;
	private FormLink cancelButton;
	private FormLink deleteImage;
	/**
	 * if form edits an external feed:
	 */
	private TextElement feedUrl;
	
	private RepositoryEntry entry;
	private boolean imageDeleted = false;
	
	/**
	 * @param ureq
	 * @param control
	 */
	public FeedFormController(UserRequest ureq, WindowControl wControl,
			Feed feed, FeedUIFactory uiFactory, RepositoryEntry entry, boolean canChangeUrl) {
		super(ureq, wControl);
		this.feed = feed;
		this.entry = entry;
		this.canChangeUrl = canChangeUrl;
		feedQuota = FeedManager.getInstance().getQuota(feed);
		setTranslator(uiFactory.getTranslator());
		initForm(ureq);
	}

	
	@Override
	protected void doDispose() {
		// nothing to dispose
	}
	
	public boolean canChangeUrl() {
		return canChangeUrl && feedUrl != null;
	}

	@Override
	protected void formOK(UserRequest ureq) {
		feed.setTitle(titleEl.getValue());
		feed.setDescription(descriptionEl.getValue());
		
		if(canChangeUrl && feed.isExternal() && feedUrl != null) {
			feed.setExternalFeedUrl(feedUrl.isEmpty() ? null : feedUrl.getValue());
		}
		
		feed.setLastModified(new Date());
		// The image is retrieved by the main controller
		fireEvent(ureq, Event.CHANGED_EVENT);
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (source == cancelButton && event.wasTriggerdBy(FormEvent.ONCLICK)) {
			fireEvent(ureq, Event.CANCELLED_EVENT);
		} else if (source == file && event.wasTriggerdBy(FormEvent.ONCHANGE)) {
			// display the uploaded file
			if (file.isUploadSuccess()) {
				String newFilename = file.getUploadFileName();
				boolean isValidFileType = newFilename.toLowerCase().matches(".*[.](png|jpg|jpeg|gif)");
				if (!isValidFileType) {
					file.setErrorKey("feed.form.file.type.error.images", null);
				} else {
					file.clearError();
				}
				deleteImage.setVisible(true);
			}
		} else if(source == deleteImage) {
			VFSLeaf img = FeedManager.getInstance().createFeedMediaFile(feed, feed.getImageName(), null);
			if(file.getUploadFile() != null && file.getUploadFile() != file.getInitialFile()) {
				file.reset();
				if(img == null) {
					deleteImage.setVisible(false);
					imageDeleted = true;
				} else {
					deleteImage.setVisible(true);
					imageDeleted = false;
				}
			} else if(img != null) {
				imageDeleted = true;
				deleteImage.setVisible(false);
				file.setInitialFile(null);
			}

			flc.setDirty(true);
		}
	}

	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		String descriptionText = descriptionEl.getValue();
		boolean allOk = true;
		if(descriptionText.length() <= 4000) {
			descriptionEl.clearError();
		} else {
			descriptionEl.setErrorKey("input.toolong", new String[]{"4000"});
			allOk = false;
		}
		
		if (file.isUploadSuccess()) {
			File newFile = file.getUploadFile();
			Long remainingQuotaKb = feedQuota.getRemainingSpace();
			if (remainingQuotaKb != -1 && newFile.length() / 1024 > remainingQuotaKb) {
				String supportAddr = WebappHelper.getMailConfig("mailQuota");
				Long uploadLimitKB = feedQuota.getUlLimitKB();
				getWindowControl().setError(translate("ULLimitExceeded", new String[] { Formatter.roundToString(uploadLimitKB.floatValue() / 1024f, 1), supportAddr }));				
			}
		}

		return allOk && validateExternalFeedUrl() && super.validateFormLogic(ureq);
	}
	
	/**
	 * validates the external feed-url
	 * 
	 * @return returns true if the external-feed url is an empty string or a valid url
	 */
	private boolean validateExternalFeedUrl(){
		//if not external, there is no text-element, do not check, just return true
		if(!feed.isExternal() || !canChangeUrl) return true;
		
		boolean validUrl = false;
		if(feedUrl.isEmpty()) {
			//allowed
			feedUrl.clearError();
			validUrl = true;
		} else {
			//validated feed url
			String url = feedUrl.getValue();
			String type = feed.getResourceableTypeName();
			ValidatedURL validatedUrl = FeedManager.getInstance().validateFeedUrl(url, type);
			if(!validatedUrl.getUrl().equals(url)) {
				feedUrl.setValue(validatedUrl.getUrl());
			}
			switch(validatedUrl.getState()) {
				case VALID:
					feedUrl.clearError();
					validUrl = true;
					break;
				case NO_ENCLOSURE:
					feedUrl.setErrorKey("feed.form.feedurl.invalid.no_media", null);
					break;
				case NOT_FOUND:
					feedUrl.setErrorKey("feed.form.feedurl.invalid.not_found", null);
					break;
				case MALFORMED:
					feedUrl.setErrorKey("feed.form.feedurl.invalid", null);
					break;
			}
		}
		return validUrl;
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		// title might be longer from external source
		String title = PersistenceHelper.truncateStringDbSave(feed.getTitle(), 256, true);
		if(!StringHelper.containsNonWhitespace(title) && entry != null) {
			title = entry.getDisplayname();
		}
		titleEl = uifactory.addTextElement("title", "feed.title.label", 256, title, formLayout);
		titleEl.setElementCssClass("o_sel_feed_title");
		titleEl.setMandatory(true);
		titleEl.setNotEmptyCheck("feed.form.field.is_mandatory");
		
		String description = feed.getDescription();
		if(!StringHelper.containsNonWhitespace(title) && entry != null) {
			description = entry.getDescription();
		}

		descriptionEl = uifactory.addRichTextElementForStringDataMinimalistic("description", "feed.form.description", description, 5, -1, formLayout, getWindowControl());
		descriptionEl.setMaxLength(4000);
		RichTextConfiguration richTextConfig = descriptionEl.getEditorConfiguration();
		// set upload dir to the media dir
		richTextConfig.setFileBrowserUploadRelPath("media");

		// Add a delete link and an image component to the image container.
		deleteImage= uifactory.addFormLink("feed.image.delete", formLayout, Link.BUTTON);
		deleteImage.setVisible(false);

		file = uifactory.addFileElement(getWindowControl(), "feed.file.label", formLayout);
		file.setExampleKey("feed.form.file.type.explain.images", null);
		file.addActionListener(FormEvent.ONCHANGE);
		file.setPreview(ureq.getUserSession(), true);
		if (feed.getImageName() != null) {
			VFSLeaf imageResource = FeedManager.getInstance().createFeedMediaFile(feed, feed.getImageName(), null);
			if(imageResource instanceof LocalFileImpl) {
				file.setPreview(ureq.getUserSession(), true);
				file.setInitialFile(((LocalFileImpl)imageResource).getBasefile());
				deleteImage.setVisible(true);
			}
		}

		Set<String> mimeTypes = new HashSet<>();
		mimeTypes.add("image/jpeg");
		mimeTypes.add("image/jpg");
		mimeTypes.add("image/png");
		mimeTypes.add("image/gif");
		file.limitToMimeType(mimeTypes, "feed.form.file.type.error.images", null);
		
		int maxFileSizeKB = feedQuota.getUlLimitKB().intValue();
		String supportAddr = WebappHelper.getMailConfig("mailQuota");
		file.setMaxUploadSizeKB(maxFileSizeKB, "ULLimitExceeded", new String[]{ Integer.toString(maxFileSizeKB / 1024), supportAddr });

		// if external feed, display feed-url text-element:
		if(canChangeUrl && feed.isExternal()){
			feedUrl = uifactory.addTextElement("feedUrl", "feed.form.feedurl", 5000, feed.getExternalFeedUrl(), flc);
			feedUrl.setElementCssClass("o_sel_feed_url");
			feedUrl.setDisplaySize(70);

			String type = feed.getResourceableTypeName();
			if(type != null && type.indexOf("BLOG") >= 0) {
				feedUrl.setExampleKey("feed.form.feedurl.example", null);
			} else {
				feedUrl.setExampleKey("feed.form.feedurl.example_podcast", null);
			}
		}
		
		// Submit and cancelButton buttons
		final FormLayoutContainer buttonLayout = FormLayoutContainer.createButtonLayout("button_layout", getTranslator());
		formLayout.add(buttonLayout);

		uifactory.addFormSubmitButton("submit", buttonLayout);
		cancelButton = uifactory.addFormLink("cancel", buttonLayout, Link.BUTTON);
	}

	/**
	 * @return The file element of this form
	 */
	public FileElement getFile() {
		FileElement fileElement = null;
		if (file.isUploadSuccess()) {
			fileElement = file;
		}
		return fileElement;
	}

	/**
	 * @return true if the image was deleted.
	 */
	public boolean isImageDeleted() {
		return imageDeleted;
	}
}
