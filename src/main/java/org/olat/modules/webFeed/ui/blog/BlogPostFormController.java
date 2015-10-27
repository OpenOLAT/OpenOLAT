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

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.DateChooser;
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
		post.setPublishDate(publishDateChooser.getDate());
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
		setFormContextHelp("Working With Course Elements#_blog_lerner");
		formLayout.setElementCssClass("o_sel_blog_form");

		title = uifactory.addTextElement("title", "feed.title.label", 256, post.getTitle(), flc);
		title.setElementCssClass("o_sel_blog_title");
		title.setMandatory(true);
		title.setNotEmptyCheck("feed.form.field.is_mandatory");

		// Description
		description = uifactory.addRichTextElementForStringData("description", "feed.form.description", post.getDescription(), 8, -1,
				false, baseDir, null, formLayout, ureq.getUserSession(), getWindowControl());
		description.setElementCssClass("o_sel_blog_description");
		RichTextConfiguration descRichTextConfig = description.getEditorConfiguration();
		// set upload dir to the media dir
		descRichTextConfig.setFileBrowserUploadRelPath("media");
		// disable XSS unsave buttons for movie (no media in standard profile)
		descRichTextConfig.disableMedia();

		// Content
		content = uifactory.addRichTextElementForStringData("content", "blog.form.content", post.getContent(), 18, -1, false,
				baseDir, null, formLayout, ureq.getUserSession(), getWindowControl());
		content.setElementCssClass("o_sel_blog_content");
		RichTextConfiguration richTextConfig = content.getEditorConfiguration();
		// set upload dir to the media dir
		richTextConfig.setFileBrowserUploadRelPath("media");
		// disable XSS unsave buttons for movie (no media in standard profile)
		richTextConfig.disableMedia();

		Calendar cal = Calendar.getInstance(ureq.getLocale());
		if (post.getPublishDate() != null) {
			cal.setTime(post.getPublishDate());
		}
		publishDateChooser = uifactory.addDateChooser("publishDateChooser", "feed.publish.date", cal.getTime(), formLayout);
		publishDateChooser.setNotEmptyCheck("feed.publish.date.is.required");
		publishDateChooser.setValidDateCheck("feed.publish.date.invalid");
		publishDateChooser.setDateChooserTimeEnabled(true);

		// Submit and cancel buttons
		final FormLayoutContainer buttonLayout = FormLayoutContainer.createButtonLayout("button_layout", getTranslator());
		formLayout.add(buttonLayout);

		uifactory.addFormSubmitButton("feed.publish", buttonLayout);
		draftLink = uifactory.addFormLink("feed.save.as.draft", buttonLayout, Link.BUTTON);
		draftLink.addActionListener(FormEvent.ONCLICK);
		uifactory.addFormCancelButton("cancel", buttonLayout, ureq, getWindowControl());
	}
}
