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
package org.olat.modules.webFeed.ui.podcast;

import java.io.File;
import java.util.Date;

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
import org.olat.core.gui.translator.Translator;
import org.olat.core.util.FileUtils;
import org.olat.core.util.StringHelper;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.modules.webFeed.managers.FeedManager;
import org.olat.modules.webFeed.models.Feed;
import org.olat.modules.webFeed.models.Item;

/**
 * Provides a form for editing episode data (title, description, file ...)
 * <p>
 * Events fired by this controller:
 * <ul>
 * <li>CANCELLED_EVENT</li>
 * <li>DONE_EVENT</li>
 * </ul>
 * Initial Date: Mar 2, 2009 <br>
 * 
 * @author gwassmann
 */
public class EpisodeFormController extends FormBasicController {
	
	public static final String MIME_TYPES_ALLOWED = ".*[.](flv|mp3|mp4|m4v|m4a|aac)";
	
	private Item episode;
	private Feed podcast;
	private TextElement title;
	private TextElement widthEl;	//fxdiff FXOLAT-118: size for video podcast
	private TextElement heightEl;
	private RichTextElement desc;
	private VFSContainer baseDir;
	private FileElement file;
	private FormLink cancelButton;

	/**
	 * @param ureq
	 * @param control
	 */
	public EpisodeFormController(UserRequest ureq, WindowControl control, Item episode, Feed podcast, Translator translator) {
		super(ureq, control);
		this.episode = episode;
		this.podcast = podcast;
		this.baseDir = FeedManager.getInstance().getItemContainer(episode, podcast);
		setTranslator(translator);
		initForm(ureq);
	}

	/**
	 * @see org.olat.core.gui.components.form.flexible.impl.FormBasicController#doDispose()
	 */
	@Override
	protected void doDispose() {
	// nothing to do
	}

	/**
	 * @see org.olat.core.gui.components.form.flexible.impl.FormBasicController#formOK(org.olat.core.gui.UserRequest)
	 */
	@Override
	protected void formOK(UserRequest ureq) {
		// Update episode. It is saved by the manager.
		episode.setTitle(title.getValue());
		// episode.setDescription(Formatter.escapeAll(description.getValue()).toString());
		episode.setDescription(desc.getValue());

		episode.setLastModified(new Date());
		episode.setMediaFile(getFile());
		// Set episode as published (no draft feature for podcast)
		episode.setDraft(false);
		
		//fxdiff FXOLAT-118: size for video podcast
		String width = widthEl.getValue();
		if(StringHelper.containsNonWhitespace(width)) {
			try {
				episode.setWidth(Integer.parseInt(width));
			} catch (NumberFormatException e) {
				//silently catch
			}
		}
		
		String height = heightEl.getValue();
		if(StringHelper.containsNonWhitespace(height)) {
			try {
				episode.setHeight(Integer.parseInt(height));
			} catch (NumberFormatException e) {
				//silently catch
			}
		}
		
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
				String newFilename = file.getUploadFileName();
				boolean isValidFileType = newFilename.toLowerCase().matches(MIME_TYPES_ALLOWED);
				boolean isFilenameValid = validateFilename(newFilename);
				if (!isValidFileType || !isFilenameValid) {
					if(!isValidFileType) {
						file.setErrorKey("feed.form.file.type.error", null);
					} else if (!isFilenameValid) {
						file.setErrorKey("podcastfile.name.notvalid", null);
					}
				} else {
					file.clearError();
				}
			}
		}
	}
	
	
	/**
	 * @see org.olat.core.gui.components.form.flexible.impl.FormBasicController#validateFormLogic(org.olat.core.gui.UserRequest)
	 */
	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		// Since mimetype restrictions have been proved to be problematic, let us
		// validate the file ending instead as a pragmatic solution.
		boolean allOk = true;
		
		String name = file.getUploadFileName();
		if (name != null) {
			boolean isValidFileType = name.toLowerCase().matches(MIME_TYPES_ALLOWED);
			boolean isFilenameValid = validateFilename(name);
			if (!isValidFileType || !isFilenameValid) {
				if(!isValidFileType) {
					file.setErrorKey("feed.form.file.type.error", null);
					allOk = false;
				} else if (!isFilenameValid) {
					file.setErrorKey("podcastfile.name.notvalid", null);
					allOk = false;
				}
			} else {
				file.clearError();
				flc.setDirty(true);
			}
		}
		
		//fxdiff FXOLAT-118: size for video podcast
		String width = widthEl.getValue();
		widthEl.clearError();
		if(StringHelper.containsNonWhitespace(width)) {
			try {
				episode.setWidth(Integer.parseInt(width));
			} catch (NumberFormatException e) {
				widthEl.setErrorKey("podcast.episode.file.size.error", null);
				allOk = false;
			}
		}
		
		String height = heightEl.getValue();
		heightEl.clearError();
		if(StringHelper.containsNonWhitespace(height)) {
			try {
				episode.setHeight(Integer.parseInt(height));
			} catch (NumberFormatException e) {
				heightEl.setErrorKey("podcast.episode.file.size.error", null);
				allOk = false;
			}
		}
		
		return allOk && super.validateFormLogic(ureq);
	}
	
	private boolean validateFilename(String filename) {
		boolean valid = FileUtils.validateFilename(filename);
		//the Flash Player has some problem with spaces too
		if(valid) {
			return filename.indexOf(' ') < 0;
		}
		return valid;
	}

	/**
	 * @see org.olat.core.gui.components.form.flexible.impl.FormBasicController#initForm(org.olat.core.gui.components.form.flexible.FormItemContainer,
	 *      org.olat.core.gui.control.Controller, org.olat.core.gui.UserRequest)
	 */
	@Override
	@SuppressWarnings("unused")
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		this.setFormTitle("feed.edit.item");
		this.setFormContextHelp(this.getClass().getPackage().getName(), "episode_form_help.html", "chelp.hover.episode");

		title = uifactory.addTextElement("title", "feed.title.label", 256, episode.getTitle(), this.flc);
		title.setMandatory(true);
		title.setNotEmptyCheck("feed.form.field.is_mandatory");

		desc = uifactory.addRichTextElementForStringData("desc", "feed.form.description", episode.getDescription(), 12, -1, false,
				false, baseDir, null, formLayout, ureq.getUserSession(), getWindowControl());
		RichTextConfiguration richTextConfig = desc.getEditorConfiguration();
		// set upload dir to the media dir
		richTextConfig.setFileBrowserUploadRelPath("media");

		file = uifactory.addFileElement("file", this.flc);
		file.setLabel("podcast.episode.file.label", null);
		file.setMandatory(true, "podcast.episode.mandatory");
		File mediaFile = FeedManager.getInstance().getItemEnclosureFile(episode, podcast);
		file.setInitialFile(mediaFile);
		file.addActionListener(this, FormEvent.ONCHANGE);
		
		String width = episode.getWidth() > 0 ? Integer.toString(episode.getWidth()) : "";
		widthEl = uifactory.addTextElement("video-width", "podcast.episode.file.width", 12, width, flc);
		String height = episode.getHeight() > 0 ? Integer.toString(episode.getHeight()) : "";
		heightEl = uifactory.addTextElement("video-height", "podcast.episode.file.height", 12, height, flc);

		// Submit and cancel buttons
		final FormLayoutContainer buttonLayout = FormLayoutContainer.createButtonLayout("button_layout", getTranslator());
		this.flc.add(buttonLayout);

		uifactory.addFormSubmitButton("feed.publish", buttonLayout);
		cancelButton = uifactory.addFormLink("cancel", buttonLayout, Link.BUTTON);
	}

	/**
	 * @return The file element of this form
	 */
	private FileElement getFile() {
		FileElement fileElement = null;
		if (file.isUploadSuccess()) {
			fileElement = file;
		}
		return fileElement;
	}
}
