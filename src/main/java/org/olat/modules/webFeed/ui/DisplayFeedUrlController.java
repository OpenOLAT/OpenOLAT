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
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.translator.Translator;
import org.olat.modules.webFeed.Feed;
import org.olat.modules.webFeed.FeedViewHelper;

/**
 * Controller for displaying the feed-url to the students. When the element is
 * clicked, a warning occurs that the url is private.
 * 
 * <P>
 * Initial Date: May 20, 2009 <br>
 * 
 * @author gwassmann
 */
public class DisplayFeedUrlController extends FormBasicController {
	private Feed feed;
	private FeedViewHelper helper;
	private TextElement feedUrl;

	boolean userHasBeenNotifiedOfConfidentialityOfUrl = false;

	/**
	 * Constructor
	 * 
	 * @param ureq
	 * @param control
	 * @param feed
	 */
	public DisplayFeedUrlController(UserRequest ureq, WindowControl control, Feed feed, FeedViewHelper helper, Translator translator) {
		super(ureq, control, FormBasicController.LAYOUT_VERTICAL);
		this.feed = feed;
		this.helper = helper;
		setTranslator(translator);
		initForm(ureq);
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (source == feedUrl && event.wasTriggerdBy(FormEvent.ONCLICK)) {
			if (feed.isInternal() && !userHasBeenNotifiedOfConfidentialityOfUrl) {
				showWarning("feed.url.is.personal.warning");
				userHasBeenNotifiedOfConfidentialityOfUrl = true;
			}
		}
	}

	@Override
	protected void formOK(UserRequest ureq) {
	// this is actually not a proper form. don't do anything.
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		feedUrl = uifactory.addTextElement("feedUrl", "feed.url.label", 5000, helper.getFeedUrl(), this.flc);
		// no editing. selecting allowed only
		feedUrl.setLabel(null, null);
		feedUrl.setEnabled(false);
		feedUrl.addActionListener(FormEvent.ONCLICK);
	}

	/**
	 * Sets the URL to display.
	 * 
	 * @param url
	 */
	public void setUrl(String url) {
		feedUrl.setValue(url);
	}
}
