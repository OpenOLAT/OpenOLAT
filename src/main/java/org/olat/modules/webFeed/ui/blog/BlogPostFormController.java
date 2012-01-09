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
package org.olat.modules.webFeed.ui.blog;

import java.util.Calendar;
import java.util.Date;

import javax.management.timer.Timer;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.DateChooser;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.elements.IntegerElement;
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
import org.olat.core.util.vfs.Quota;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.core.util.vfs.callbacks.FullAccessWithQuotaCallback;
import org.olat.modules.webFeed.managers.FeedManager;
import org.olat.modules.webFeed.models.Feed;
import org.olat.modules.webFeed.models.Item;

/**
 * Form controller for blog posts.
 * 
 * <P>
 * Initial Date: Aug 3, 2009 <br>
 * 
 * @author gwassmann
 */
public class BlogPostFormController extends FormBasicController {

	private Item post;
	private VFSContainer baseDir;
	
	private TextElement title;
	private RichTextElement description, content;
	private DateChooser publishDateChooser;
	private IntegerElement hours;
	private TextElement mins;
	private FormLink draftLink;
	
	private boolean currentlyDraft;

	/**
	 * @param ureq
	 * @param control
	 */
	public BlogPostFormController(UserRequest ureq, WindowControl control, Item post, Feed blog, Translator translator) {
		super(ureq, control);
		this.post = post;
		this.currentlyDraft = post.isDraft();
		this.baseDir = FeedManager.getInstance().getItemContainer(post, blog);
		if(baseDir.getLocalSecurityCallback() == null) {
			Quota quota = FeedManager.getInstance().getQuota(blog.getResource());
			baseDir.setLocalSecurityCallback(new FullAccessWithQuotaCallback(quota));
		}
		setTranslator(translator);
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
		// Update post. It is saved by the manager.
		setValues();
		if(!currentlyDraft || post.getModifierKey() > 0) {
			post.setModifierKey(ureq.getIdentity().getKey());
		//fxdiff BAKS-18
		} else if(currentlyDraft && !ureq.getIdentity().getKey().equals(post.getAuthorKey())) {
			post.setModifierKey(ureq.getIdentity().getKey());
		}
		post.setDraft(false);
		fireEvent(ureq, Event.CHANGED_EVENT);
	}

	/**
	 * 
	 */
	private void setValues() {
		post.setTitle(title.getValue());
		post.setDescription(description.getValue());
		post.setContent(content.getValue());
		// The author is set already
		post.setLastModified(new Date());
		int hh = 0;
		try {
			hh = hours.getIntValue();
		} catch (Exception e) {
			hh = 0;
		}
		int mm;
		try {
			mm = Integer.parseInt(mins.getValue());
		} catch (NumberFormatException e) {
			mm = 0;
		}
		long time;
		if(publishDateChooser.getDate() != null) {
			time = publishDateChooser.getDate().getTime() + hh * Timer.ONE_HOUR + mm * Timer.ONE_MINUTE;
		} else {
			time = new Date().getTime() + hh * Timer.ONE_HOUR + mm * Timer.ONE_MINUTE;
		}
		post.setPublishDate(new Date(time));
	}

	/**
	 * @see org.olat.core.gui.components.form.flexible.impl.FormBasicController#formCancelled(org.olat.core.gui.UserRequest)
	 */
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}

	/**
	 * @see org.olat.core.gui.components.form.flexible.impl.FormBasicController#formInnerEvent(org.olat.core.gui.UserRequest,
	 *      org.olat.core.gui.components.form.flexible.FormItem,
	 *      org.olat.core.gui.components.form.flexible.impl.FormEvent)
	 */
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (source == draftLink) {
			setValues();
			if(!currentlyDraft || post.getModifierKey() > 0) {
				post.setModifierKey(ureq.getIdentity().getKey());
			//fxdiff BAKS-18
			} else if(currentlyDraft && !ureq.getIdentity().getKey().equals(post.getAuthorKey())) {
				post.setModifierKey(ureq.getIdentity().getKey());
			}
			post.setDraft(true);
			this.fireEvent(ureq, Event.CHANGED_EVENT);
		}
	}

	/**
	 * @see org.olat.core.gui.components.form.flexible.impl.FormBasicController#initForm(org.olat.core.gui.components.form.flexible.FormItemContainer,
	 *      org.olat.core.gui.control.Controller, org.olat.core.gui.UserRequest)
	 */
	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		this.setFormTitle("feed.edit.item");
		this.setFormContextHelp(this.getClass().getPackage().getName(), "post_form_help.html", "chelp.hover.form");

		title = uifactory.addTextElement("title", "feed.title.label", 256, post.getTitle(), this.flc);
		title.setMandatory(true);
		title.setNotEmptyCheck("feed.form.field.is_mandatory");

		// Description
		description = uifactory.addRichTextElementForStringData("description", "feed.form.description", post.getDescription(), 8, -1, false,
				false, baseDir, null, formLayout, ureq.getUserSession(), getWindowControl());
		RichTextConfiguration descRichTextConfig = description.getEditorConfiguration();
		// set upload dir to the media dir
		descRichTextConfig.setFileBrowserUploadRelPath("media");

		// Content
		content = uifactory.addRichTextElementForStringData("content", "blog.form.content", post.getContent(), 18, -1, false, false,
				baseDir, null, formLayout, ureq.getUserSession(), getWindowControl());
		RichTextConfiguration richTextConfig = content.getEditorConfiguration();
		// set upload dir to the media dir
		richTextConfig.setFileBrowserUploadRelPath("media");

		final FormLayoutContainer dateAndTimeLayout = FormLayoutContainer.createHorizontalFormLayout("feed.publish.date", getTranslator());
		formLayout.add(dateAndTimeLayout);
		dateAndTimeLayout.setLabel("feed.publish.date", null);
		dateAndTimeLayout.setMandatory(true);
		publishDateChooser = uifactory.addDateChooser("publishDateChooser", null, null, dateAndTimeLayout);
		publishDateChooser.setNotEmptyCheck("feed.publish.date.is.required");
		publishDateChooser.setValidDateCheck("feed.publish.date.invalid");
		Calendar cal = Calendar.getInstance(ureq.getLocale());
		if (post.getPublishDate() != null) {
			cal.setTime(post.getPublishDate());
		}
		publishDateChooser.setDate(cal.getTime());
		hours = uifactory.addIntegerElement("hour", null, cal.get(Calendar.HOUR_OF_DAY), dateAndTimeLayout);
		hours.setDisplaySize(2);
		hours.setMaxLength(2);

		String minutesIn2digits = Long.toString(cal.get(Calendar.MINUTE));
		if (minutesIn2digits.length() == 1) {
			// always show two digits for minutes
			minutesIn2digits = '0' + minutesIn2digits;
		}
		uifactory.addStaticTextElement("timeSeparator", null, ":", dateAndTimeLayout);
		// dTextElement("mins", cal.get(Calendar.MINUTE), dateAndTimeLayout);
		mins = uifactory.addTextElement("mins", null, 2, minutesIn2digits, dateAndTimeLayout);
		mins.setDisplaySize(2);
		mins.setRegexMatchCheck("\\d*", "feed.form.minutes.error");
		mins.setNotEmptyCheck("feed.form.minutes.error");

		uifactory.addStaticTextElement("o.clock", null, translate("feed.publish.time.o.clock"), dateAndTimeLayout);

		// Submit and cancel buttons
		final FormLayoutContainer buttonLayout = FormLayoutContainer.createButtonLayout("button_layout", getTranslator());
		this.flc.add(buttonLayout);

		uifactory.addFormSubmitButton("feed.publish", buttonLayout);
		draftLink = uifactory.addFormLink("feed.save.as.draft", buttonLayout, Link.BUTTON);
		draftLink.addActionListener(this, FormEvent.ONCLICK);
		uifactory.addFormCancelButton("cancel", buttonLayout, ureq, getWindowControl());
	}
}
