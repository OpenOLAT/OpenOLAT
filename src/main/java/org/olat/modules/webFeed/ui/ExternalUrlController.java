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
import org.olat.core.util.StringHelper;
import org.olat.modules.webFeed.Feed;
import org.olat.modules.webFeed.manager.FeedManager;
import org.olat.modules.webFeed.manager.ValidatedURL;

/**
 *
 * Initial date: 18.09.2017<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class ExternalUrlController extends FormBasicController {

	private Feed feed;

	private TextElement externalFeedUrlEl;
	private FormLink cancelButton;

	public ExternalUrlController(UserRequest ureq, WindowControl windowControl, Feed feedResource) {
		super(ureq, windowControl);
		this.feed = feedResource;
		initForm(ureq);
	}

	public String getExternalFeedUrlEl() {
		String externalFeedUrl = externalFeedUrlEl.getValue();
		if (StringHelper.containsNonWhitespace(externalFeedUrl)) {
			return externalFeedUrl;
		}
		return null;
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		externalFeedUrlEl = uifactory.addTextElement("externalFeedUrlEl", "feed.form.feedurl", 5000, feed.getExternalFeedUrl(), flc);
		externalFeedUrlEl.setElementCssClass("o_sel_feed_url");
		externalFeedUrlEl.setDisplaySize(70);

		String type = feed.getResourceableTypeName();
		if(type != null && type.indexOf("BLOG") >= 0) {
			externalFeedUrlEl.setExampleKey("feed.form.feedurl.example", null);
		} else {
			externalFeedUrlEl.setExampleKey("feed.form.feedurl.example_podcast", null);
		}

		final FormLayoutContainer buttonLayout = FormLayoutContainer.createButtonLayout("button_layout", getTranslator());
		formLayout.add(buttonLayout);
		uifactory.addFormSubmitButton("submit", buttonLayout);
		cancelButton = uifactory.addFormLink("cancel", buttonLayout, Link.BUTTON);
	}

	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = super.validateFormLogic(ureq);
		allOk &= validateExternalFeedUrl();
		return allOk;
	}

	private boolean validateExternalFeedUrl(){
		boolean validUrl = false;
		if(externalFeedUrlEl.isEmpty()) {
			//allowed
			externalFeedUrlEl.clearError();
			validUrl = true;
		} else {
			//validated feed url
			String url = externalFeedUrlEl.getValue();
			String type = feed.getResourceableTypeName();
			ValidatedURL validatedUrl = FeedManager.getInstance().validateFeedUrl(url, type);
			if(!validatedUrl.getUrl().equals(url)) {
				externalFeedUrlEl.setValue(validatedUrl.getUrl());
			}
			switch(validatedUrl.getState()) {
				case VALID:
					externalFeedUrlEl.clearError();
					validUrl = true;
					break;
				case NO_ENCLOSURE:
					externalFeedUrlEl.setErrorKey("feed.form.feedurl.invalid.no_media", null);
					break;
				case NOT_FOUND:
					externalFeedUrlEl.setErrorKey("feed.form.feedurl.invalid.not_found", null);
					break;
				case MALFORMED:
					externalFeedUrlEl.setErrorKey("feed.form.feedurl.invalid", null);
					break;
			}
		}
		return validUrl;
	}

	@Override
	protected void formOK(UserRequest ureq) {
		fireEvent(ureq, Event.CHANGED_EVENT);
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (source == cancelButton) {
			fireEvent(ureq, Event.CANCELLED_EVENT);
		}
	}
}
