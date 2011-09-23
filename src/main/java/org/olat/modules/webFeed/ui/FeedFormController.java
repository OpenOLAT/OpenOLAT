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
 * Copyright (c) frentix GmbH<br>
 * http://www.frentix.com<br>
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
import org.olat.core.gui.components.image.ImageComponent;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.media.MediaResource;
import org.olat.core.util.Util;
import org.olat.core.util.vfs.LocalFileImpl;
import org.olat.core.util.vfs.VFSMediaResource;
import org.olat.modules.webFeed.managers.FeedManager;
import org.olat.modules.webFeed.models.Feed;

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
	private TextElement title;
	private FileElement file;
	private RichTextElement description;
	private FormLink cancelButton;
	private FormLink deleteImageLink;
	private boolean imageDeleted = false;
	private ImageComponent image;
	private FormLayoutContainer imageContainer;

	/**
	 * @param ureq
	 * @param control
	 */
	public FeedFormController(UserRequest ureq, WindowControl wControl, Feed feed, FeedUIFactory uiFactory) {
		super(ureq, wControl);
		this.feed = feed;
		setTranslator(uiFactory.getTranslator());
		initForm(ureq);
	}

	/**
	 * @see org.olat.core.gui.components.form.flexible.impl.FormBasicController#doDispose()
	 */
	@Override
	protected void doDispose() {
	// nothing to dispose
	}

	/**
	 * @see org.olat.core.gui.components.form.flexible.impl.FormBasicController#formOK(org.olat.core.gui.UserRequest)
	 */
	@Override
	protected void formOK(UserRequest ureq) {
		feed.setTitle(title.getValue());
		feed.setDescription(description.getValue());
		feed.setLastModified(new Date());
		// The image is retrieved by the main controller
		this.fireEvent(ureq, Event.CHANGED_EVENT);
	}

	/**
	 * @see org.olat.core.gui.components.form.flexible.impl.FormBasicController#formInnerEvent(org.olat.core.gui.UserRequest,
	 *      org.olat.core.gui.components.form.flexible.FormItem,
	 *      org.olat.core.gui.components.form.flexible.impl.FormEvent)
	 */
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (source == cancelButton && event.wasTriggerdBy(FormEvent.ONCLICK)) {
			fireEvent(ureq, Event.CANCELLED_EVENT);
		} else if (source == file && event.wasTriggerdBy(FormEvent.ONCHANGE)) {
			// display the uploaded file
			if (file.isUploadSuccess()) {
				File newFile = file.getUploadFile();
				String newFilename = file.getUploadFileName();
				boolean isValidFileType = newFilename.toLowerCase().matches(".*[.](png|jpg|jpeg|gif)");
				if (!isValidFileType) {
					file.setErrorKey("feed.form.file.type.error.images", null);
					unsetImage();
				} else {
					file.clearError();
					MediaResource newResource = new VFSMediaResource(new LocalFileImpl(newFile));
					setImage(newResource);
				}
			}
		} else if (source == deleteImageLink && event.wasTriggerdBy(FormEvent.ONCLICK)) {
			unsetImage();
		}
	}

	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		String descriptionText = description.getValue();
		boolean allOk = true;
		if(descriptionText.length() <= 4000) {
			description.clearError();
		} else {
			description.setErrorKey("input.toolong", new String[]{"4000"});
			allOk = false;
		}
		return allOk && super.validateFormLogic(ureq);
	}

	/**
	 * Sets the image
	 * 
	 * @param newResource
	 */
	private void setImage(MediaResource newResource) {
		image.setMediaResource(newResource);
		image.setMaxWithAndHeightToFitWithin(150, 150);
		imageContainer.setVisible(true);
		// This is needed. ImageContainer is not displayed otherwise.
		this.getInitialComponent().setDirty(true);
		imageDeleted = false;
		file.setLabel(null, null);
	}

	/**
	 * Unsets the image
	 */
	private void unsetImage() {
		imageContainer.setVisible(false);
		file.reset();
		file.getComponent().setDirty(true);
		image.setMediaResource(null);
		imageDeleted = true;
		file.setLabel("feed.file.label", null);
	}

	/**
	 * @see org.olat.core.gui.components.form.flexible.impl.FormBasicController#initForm(org.olat.core.gui.components.form.flexible.FormItemContainer,
	 *      org.olat.core.gui.control.Controller, org.olat.core.gui.UserRequest)
	 */
	@Override
	// formLayout == this.flc && listener == this !!!
	@SuppressWarnings("unused")
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		this.setFormTitle("feed.edit");
		// this.setFormContextHelp(packageName, pageName, hoverTextKey);

		// title might be longer from external source
		String saveTitle = PersistenceHelper.truncateStringDbSave(feed.getTitle(), 256, true);
		title = uifactory.addTextElement("title", "feed.title.label", 256, saveTitle, this.flc);
		title.setMandatory(true);
		title.setNotEmptyCheck("feed.form.field.is_mandatory");

		description = uifactory.addRichTextElementForStringDataMinimalistic("description", "feed.form.description", feed
				.getDescription(), 5, -1, false, formLayout, ureq.getUserSession(), getWindowControl());
		description.setMandatory(true);
		description.setMaxLength(4000);
		description.setNotEmptyCheck("feed.form.field.is_mandatory");
		RichTextConfiguration richTextConfig = description.getEditorConfiguration();
		// set upload dir to the media dir
		richTextConfig.setFileBrowserUploadRelPath("media");

		String VELOCITY_ROOT = Util.getPackageVelocityRoot(this.getClass());
		imageContainer = FormLayoutContainer.createCustomFormLayout("imageContainer", getTranslator(), VELOCITY_ROOT + "/image_container.html");
		imageContainer.setLabel("feed.file.label", null);
		flc.add(imageContainer);
		// Add a delete link and an image component to the image container.
		deleteImageLink = uifactory.addFormLink("feed.image.delete", imageContainer);
		image = new ImageComponent("icon");
		imageContainer.put("image", image);

		file = uifactory.addFileElement("feed.file.label", this.flc);
		file.addActionListener(this, FormEvent.ONCHANGE);

		if (feed.getImageName() != null) {
			MediaResource imageResource = FeedManager.getInstance().createFeedMediaFile(feed, feed.getImageName());
			setImage(imageResource);
		} else {
			// No image -> hide the image container
			imageContainer.setVisible(false);
		}

		Set<String> mimeTypes = new HashSet<String>();
		mimeTypes.add("image/jpeg");
		mimeTypes.add("image/jpg");
		mimeTypes.add("image/png");
		mimeTypes.add("image/gif");
		file.limitToMimeType(mimeTypes, "feed.form.file.type.error.images", null);

		// Submit and cancelButton buttons
		final FormLayoutContainer buttonLayout = FormLayoutContainer.createButtonLayout("button_layout", getTranslator());
		this.flc.add(buttonLayout);

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
	public boolean imageDeleted() {
		return imageDeleted;
	}
}
