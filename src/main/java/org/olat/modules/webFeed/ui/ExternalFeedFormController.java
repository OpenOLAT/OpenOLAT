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

import java.util.Date;

import org.apache.commons.lang.StringEscapeUtils;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.translator.Translator;
import org.olat.modules.webFeed.managers.FeedManager;
import org.olat.modules.webFeed.managers.ValidatedURL;
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
public class ExternalFeedFormController extends FormBasicController {
	private Feed feed;
	private TextElement title, description, feedUrl;
	private FormLink cancelButton;

	/**
	 * @param ureq
	 * @param control
	 * @param feed
	 */
	public ExternalFeedFormController(UserRequest ureq, WindowControl control, Feed podcast, Translator translator) {
		super(ureq, control);
		this.feed = podcast;
		setTranslator(translator);
		initForm(ureq);
	}

	/**
	 * @see org.olat.core.gui.components.form.flexible.impl.FormBasicController#formOK(org.olat.core.gui.UserRequest)
	 */
	@Override
	public void formOK(UserRequest ureq) {
		feed.setTitle(StringEscapeUtils.escapeHtml(title.getValue()).toString());
		feed.setDescription(description.getValue());
		feed.setExternalFeedUrl(feedUrl.isEmpty() ? null : feedUrl.getValue());
		feed.setLastModified(new Date());
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
		}
	}

	/**
	 * @see org.olat.core.gui.components.form.flexible.impl.FormBasicController#validateFormLogic(org.olat.core.gui.UserRequest)
	 */
	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
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

		String descriptionText = description.getValue();
		boolean descOk = true;
		if(descriptionText.length() <= 4000) {
			description.clearError();
		} else {
			description.setErrorKey("input.toolong", new String[]{"4000"});
			descOk = false;
		}
		return descOk && validUrl && super.validateFormLogic(ureq);
	}

	/**
	 * @see org.olat.core.gui.components.form.flexible.impl.FormBasicController#doDispose()
	 */
	@Override
	protected void doDispose() {
	// nothing to dispose
	}

	/**
	 * @see org.olat.modules.webFeed.ui.podcast.FeedFormController#initForm(org.olat.core.gui.components.form.flexible.FormItemContainer,
	 *      org.olat.core.gui.control.Controller, org.olat.core.gui.UserRequest)
	 */
	@SuppressWarnings("unused")
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		this.setFormTitle("feed.edit");
		
		title = uifactory.addTextElement("title", "feed.title.label", 256, feed.getTitle(), this.flc);
		title.setMandatory(true);
		title.setNotEmptyCheck("feed.form.field.is_mandatory");

		// Description
		//description = formItemsFactory.addTextAreaElement("description", 5000, 0, 2, true, feed.getDescription(),
		//		"feed.form.description", this.flc);
		description = uifactory.addRichTextElementForStringDataMinimalistic("description", "feed.form.description", feed
				.getDescription(), 5, -1, false, formLayout, ureq.getUserSession(), getWindowControl());
		description.setMandatory(true);
		description.setNotEmptyCheck("feed.form.field.is_mandatory");
		// The feed url
		feedUrl = uifactory.addTextElement("feedUrl", "feed.form.feedurl", 5000, feed.getExternalFeedUrl(), this.flc);
		feedUrl.setDisplaySize(70);
		
		String type = feed.getResourceableTypeName();
		if(type != null && type.indexOf("BLOG") >= 0) {
			feedUrl.setExampleKey("feed.form.feedurl.example", null);
		} else {
			feedUrl.setExampleKey("feed.form.feedurl.example_podcast", null);
		}
		
		// Submit and cancelButton buttons
		final FormLayoutContainer buttonLayout = FormLayoutContainer.createButtonLayout("button_layout", getTranslator());
		this.flc.add(buttonLayout);

		uifactory.addFormSubmitButton("submit", buttonLayout);
		cancelButton = uifactory.addFormLink("cancel", buttonLayout, Link.BUTTON);
	}
}
