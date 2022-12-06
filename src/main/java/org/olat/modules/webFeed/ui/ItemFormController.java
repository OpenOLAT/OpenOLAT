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
import java.util.Calendar;
import java.util.Date;

import org.olat.core.commons.services.image.Size;
import org.olat.core.commons.services.video.MovieService;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.DateChooser;
import org.olat.core.gui.components.form.flexible.elements.FileElement;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.elements.RichTextElement;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.form.flexible.impl.elements.FileElementEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.richText.RichTextConfiguration;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.translator.Translator;
import org.olat.core.util.FileUtils;
import org.olat.core.util.Formatter;
import org.olat.core.util.StringHelper;
import org.olat.core.util.WebappHelper;
import org.olat.core.util.vfs.LocalFileImpl;
import org.olat.core.util.vfs.Quota;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.core.util.vfs.callbacks.FullAccessWithQuotaCallback;
import org.olat.modules.webFeed.Item;
import org.olat.modules.webFeed.manager.FeedManager;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * This Controller is responsible for editing a single feed item.
 *
 * Initial date: 16.06.2017<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public abstract class ItemFormController extends FormBasicController {

	private static final String ALLOWED_MIME_TYPES = ".*[.](flv|mp3|mp4|m4v|m4a|aac)";

	private Item item;

	private TextElement title;
	private RichTextElement description;
	private RichTextElement content;
	private FileElement file;
	private VFSContainer baseDir;
	private TextElement widthEl;
	private TextElement heightEl;
	private DateChooser publishDateChooser;
	private FormLink draftButton;
	private FormLink cancelButton;

	@Autowired
	private MovieService movieService;

	public ItemFormController(UserRequest ureq, WindowControl control, Item item, Translator translator) {
		super(ureq, control);
		this.item = item;
		this.baseDir = FeedManager.getInstance().getItemContainer(item);
		if(baseDir.getLocalSecurityCallback() == null) {
			Quota quota = FeedManager.getInstance().getQuota(item.getFeed());
			baseDir.setLocalSecurityCallback(new FullAccessWithQuotaCallback(quota));
		}

		setTranslator(translator);
		initForm(ureq);
	}

	protected abstract String getType();

	protected abstract boolean hasContent();

	protected abstract boolean hasMandatoryMedia();

	protected abstract boolean hasDraftMode();

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		formLayout.setElementCssClass("o_sel_feed_form");

		title = uifactory.addTextElement("title", "feed.title.label", 256, item.getTitle(), this.flc);
		title.setElementCssClass("o_sel_feed_title");
		title.setMandatory(true);
		title.setNotEmptyCheck("feed.form.field.is_mandatory");

		description = uifactory.addRichTextElementForStringData("description", "feed.form.description", item.getDescription(), 12, -1,
				true, baseDir, null, formLayout, ureq.getUserSession(), getWindowControl());
		description.setElementCssClass("o_sel_feed_description");
		RichTextConfiguration descRichTextConfig = description.getEditorConfiguration();
		// set upload dir to the media dir
		descRichTextConfig.setFileBrowserUploadRelPath("media");
		descRichTextConfig.setPathInStatusBar(false);
		// disable XSS unsave buttons for movie (no media in standard profile)
		descRichTextConfig.disableMedia();

		content = uifactory.addRichTextElementForStringData("content", "feed.form.content", item.getContent(), 18, -1, true,
				baseDir, null, formLayout, ureq.getUserSession(), getWindowControl());
		content.setElementCssClass("o_sel_feed_content");
		RichTextConfiguration richTextConfig = content.getEditorConfiguration();
		// set upload dir to the media dir
		richTextConfig.setFileBrowserUploadRelPath("media");
		richTextConfig.setPathInStatusBar(false);
		// disable XSS unsave buttons for movie (no media in standard profile)
		richTextConfig.disableMedia();
		content.setVisible(hasContent());

		file = uifactory.addFileElement(getWindowControl(), getIdentity(), "file", null, flc);
		file.setLabel("feed.item.file.label", null);
		file.setExampleKey("feed.form.file.type.explain", null);
		if (hasMandatoryMedia()) {
			file.setMandatory(true, "feed.item.file.mandatory");
		} else {
			file.setDeleteEnabled(true);
		}
		File mediaFile = FeedManager.getInstance().loadItemEnclosureFile(item);
		file.setInitialFile(mediaFile);
		file.addActionListener(FormEvent.ONCHANGE);
		if(baseDir.getLocalSecurityCallback() != null && baseDir.getLocalSecurityCallback().getQuota() != null) {
			Long uploadLimitKB = baseDir.getLocalSecurityCallback().getQuota().getUlLimitKB();
			String supportAddr = WebappHelper.getMailConfig("mailQuota");
			file.setMaxUploadSizeKB(uploadLimitKB.intValue(), "ULLimitExceeded", new String[] { Formatter.roundToString((uploadLimitKB.floatValue() / 1000f), 1), supportAddr });
		}

		String width = item.getWidth() != null && item.getWidth() > 0 ? Integer.toString(item.getWidth()) : "";
		widthEl = uifactory.addTextElement("video-width", "feed.item.file.width", 12, width, flc);
		String height = item.getHeight() != null && item.getHeight() > 0 ? Integer.toString(item.getHeight()) : "";
		heightEl = uifactory.addTextElement("video-height", "feed.item.file.height", 12, height, flc);

		Calendar cal = Calendar.getInstance(ureq.getLocale());
		if (item.getPublishDate() != null) {
			cal.setTime(item.getPublishDate());
		}
		publishDateChooser = uifactory.addDateChooser("publishDateChooser", "feed.publish.date", cal.getTime(), formLayout);
		publishDateChooser.setNotEmptyCheck("feed.publish.date.is.required");
		publishDateChooser.setValidDateCheck("feed.publish.date.invalid");
		publishDateChooser.setDateChooserTimeEnabled(true);
		publishDateChooser.setValidDateCheck("form.error.date");
		publishDateChooser.setVisible(hasDraftMode());

		final FormLayoutContainer buttonLayout = FormLayoutContainer.createButtonLayout("button_layout", getTranslator());
		this.flc.add(buttonLayout);
		uifactory.addFormSubmitButton("feed.publish", buttonLayout);
		if (hasDraftMode()) {
			draftButton = uifactory.addFormLink("feed.save.as.draft", buttonLayout, Link.BUTTON);
			draftButton.addActionListener(FormEvent.ONCLICK);
		}
		cancelButton = uifactory.addFormLink("cancel", buttonLayout, Link.BUTTON);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		setValues(ureq);

		item.setDraft(false);

		fireEvent(ureq, Event.CHANGED_EVENT);
	}

	/**
	 * Transfer the values from the form to the item object.
	 *
	 * @param ureq
	 */
	private void setValues(UserRequest ureq) {
		item.setTitle(title.getValue());
		item.setDescription(description.getValue());

		if (hasContent()) {
			item.setContent(content.getValue());
		}

		item.setMediaFile(file);

		String width = widthEl.getValue();
		if(StringHelper.containsNonWhitespace(width)) {
			try {
				item.setWidth(Integer.parseInt(width));
			} catch (NumberFormatException e) {
				// already checked in validateFormLogic()
			}
		}

		String height = heightEl.getValue();
		if(StringHelper.containsNonWhitespace(height)) {
			try {
				item.setHeight(Integer.parseInt(height));
			} catch (NumberFormatException e) {
				// already checked in validateFormLogic()
			}
		}

		/**
		 * Set the modifier key it is required. The modifier key is updated if:
		 *  - the item was no draft
		 *  - the modifier key was set once
		 *  - the item was a draft and has an other author
		 */
		if (!item.isDraft()
				|| item.getModifierKey() != null
				|| (item.isDraft() && !ureq.getIdentity().getKey().equals(item.getAuthorKey()))) {
			item.setModifierKey(ureq.getIdentity().getKey());
		}

		item.setPublishDate(publishDateChooser.getDate());
		item.setLastModified(new Date());
	}

	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = super.validateFormLogic(ureq);

		String name = file.getUploadFileName();
		if (name != null) {
			if (!validateFilename(name)) {
				allOk = false;
			} else {
				flc.setDirty(true);
			}

			// quota check whole feed
			if(baseDir.getLocalSecurityCallback() == null || baseDir.getLocalSecurityCallback().getQuota() != null) {
				Quota feedQuota = baseDir.getLocalSecurityCallback().getQuota();
				Long remainingQuotaKb = feedQuota.getRemainingSpace();
				if (remainingQuotaKb != -1 && file.getUploadFile().length() / 1024 > remainingQuotaKb) {
					String supportAddr = WebappHelper.getMailConfig("mailQuota");
					Long uploadLimitKB = feedQuota.getUlLimitKB();
					file.setErrorKey("ULLimitExceeded", Formatter.roundToString(uploadLimitKB.floatValue() / 1000f, 1), supportAddr);
					allOk = false;
				}
			}
		}

		String width = widthEl.getValue();
		if(StringHelper.containsNonWhitespace(width)) {
			try {
				Integer.parseInt(width);
			} catch (NumberFormatException e) {
				widthEl.setErrorKey("feed.item.file.size.error");
				allOk = false;
			}
		}
		
		publishDateChooser.clearError();
		if(!validateFormItem(ureq, publishDateChooser)) {
			allOk &= false;
		}

		String height = heightEl.getValue();
		if(StringHelper.containsNonWhitespace(height)) {
			try {
				Integer.parseInt(height);
			} catch (NumberFormatException e) {
				heightEl.setErrorKey("feed.item.file.size.error");
				allOk = false;
			}
		}

		return allOk;
	}

	private boolean validateFilename(String filename) {
		boolean allOk = true;

		boolean isFilenameValid = FileUtils.validateFilename(filename);
		if(!isFilenameValid) {
			file.setErrorKey("feed.item.file.name.notvalid");
			allOk = false;
		}

		// Since mimetype restrictions have been proved to be problematic, let
		// us validate the file ending instead as a pragmatic solution.
		boolean isFiletypeValid = filename.toLowerCase().matches(ALLOWED_MIME_TYPES);
		if(!isFiletypeValid) {
			file.setErrorKey("feed.form.file.type.error");
			allOk = false;
		}

		return allOk;
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (source == cancelButton && event.wasTriggerdBy(FormEvent.ONCLICK)) {
			fireEvent(ureq, Event.CANCELLED_EVENT);
		} else if (source == draftButton) {
			setValues(ureq);
			item.setDraft(true);
			fireEvent(ureq, Event.CHANGED_EVENT);
		} else if (source == file && event.wasTriggerdBy(FormEvent.ONCHANGE)) {
			fileUploaded();
		} else if (source == file
				&& event instanceof FileElementEvent
				&& FileElementEvent.DELETE.equals(event.getCommand())) {
			fileDeleted();
		}
	}

	/**
	 * Helper to process a uploaded file. Adjust the filename and the file
	 * suffix. Guess the width and height and fill the appropriate fields.
	 */
	private void fileUploaded() {
		file.clearError();
		if (file.isUploadSuccess()) {
			String newFilename = file.getUploadFileName()
					                 .toLowerCase()
					                 .replaceAll(" ", "_");
			file.setUploadFileName(newFilename);
			VFSLeaf movie = new LocalFileImpl(file.getUploadFile());

			if (newFilename.endsWith("mov") || newFilename.endsWith("m4v")) {
				// Check if it actually is a mp4 file, if so rename file to
				// mp4 to make later processes work smoothly. MOV is used
				// when uploading a video from an iOS device.
				if (movieService.isMP4(movie, newFilename)) {
					newFilename = newFilename.substring(0, newFilename.length() - 3) + "mp4";
					file.setUploadFileName(newFilename);
				}
			}

			if (validateFilename(newFilename)){
				// try to detect width and height for movies, prefill for user if possible
				Size size = movieService.getSize(movie, FileUtils.getFileSuffix(newFilename));
				if (size != null) {
					if (size.getWidth() > 1) {
						widthEl.setValue(Integer.toString(size.getWidth()));
					}
					if (size.getHeight() > 1) {
						heightEl.setValue(Integer.toString(size.getHeight()));
					}
				}
			}
		}
	}

	/**
	 * Helper to process a deleted file.
	 */
	private void fileDeleted() {
		if (file.getUploadFile() != null) {
			file.reset();
		} else {
			file.setInitialFile(null);
		}
		widthEl.setValue(null);
		heightEl.setValue(null);
	}

}
