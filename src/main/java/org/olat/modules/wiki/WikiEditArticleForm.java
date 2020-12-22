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
* Copyright (c) since 2004 at Multimedia- & E-Learning Services (MELS),<br>
* University of Zurich, Switzerland.
* <hr>
* <a href="http://www.openolat.org">
* OpenOLAT - Online Learning and Training</a><br>
* This file has been modified by the OpenOLAT community. Changes are licensed
* under the Apache 2.0 license as the original file.
*/

package org.olat.modules.wiki;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.form.flexible.impl.elements.FormSubmit;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;


/**
 * Description:<br>
 * Form for editing a textfield with wiki content inside
 * 
 * <P>
 * Initial Date:  May 7, 2006 <br>
 * @author guido
 */
public class WikiEditArticleForm extends FormBasicController {

	private FormLink deleteButton;
	private TextElement wikiContent;
	private TextElement updateComment;

	private final WikiSecurityCallback securityCallback;
	
	public WikiEditArticleForm(UserRequest ureq, WindowControl wControl,
			WikiPage page, WikiSecurityCallback securityCallback) {
		super(ureq, wControl, FormBasicController.LAYOUT_VERTICAL);
		this.securityCallback = securityCallback;
		initForm(ureq);
		setPage(page);
	}

	protected String getWikiContent(){
		return wikiContent.getValue();
	}
	
	protected String getUpdateComment(){
		return updateComment.getValue();
	}
	
	protected void resetUpdateComment(){
		updateComment.setValue("");
	}
	
	protected void setPage(WikiPage page) {
		wikiContent.setValue(page.getContent());
		wikiContent.getRootForm().setDirtyMarking(false);
		
		try {
			boolean canDelete = getIdentity().getKey().equals(Long.valueOf(page.getInitalAuthor()))
					|| securityCallback.mayEditWikiMenu();
			deleteButton.setVisible(canDelete);
		} catch (Exception e) {
			logError("", e);
			deleteButton.setVisible(false);
		}
	}
	
	protected void setDirty(boolean dirt) {
		wikiContent.getRootForm().setDirtyMarking(dirt);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		fireEvent (ureq, Event.DONE_EVENT);
	}
	
	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent (ureq, Event.CANCELLED_EVENT);
	}
	
	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		fireEvent (ureq, new Event(source.getName()));
	}
	
	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		
		wikiContent = uifactory.addTextAreaElement("wikiContentElement", 20, 110, "", formLayout);
		wikiContent.setElementCssClass("o_sel_wiki_content");
		wikiContent.setLabel(null, null);
		wikiContent.preventValueTrim(true);//OO-31 prevent trimming, so first line can be with inset (wiki pre-formatted)
		wikiContent.setFocus(true);

		updateComment = uifactory.addTextElement("wikiUpdateComment", null, 40, "", formLayout);
		updateComment.setExampleKey ("update.comment", null);
		// Button layout
		final FormLayoutContainer buttonLayout = FormLayoutContainer.createButtonLayout("button_layout", getTranslator());
		formLayout.add(buttonLayout);
		FormSubmit submit = uifactory.addFormSubmitButton("save", buttonLayout);
		submit.setElementCssClass("o_sel_wiki_save");
		FormLink saveAndClose =uifactory.addFormLink("save.and.close", buttonLayout, Link.BUTTON);
		saveAndClose.setElementCssClass("o_sel_wiki_save_and_close");
		
		deleteButton = uifactory.addFormLink("delete.page", buttonLayout, Link.BUTTON);
		uifactory.addFormLink("preview", buttonLayout, Link.BUTTON);
		uifactory.addFormLink("media.upload", buttonLayout, Link.BUTTON);
		uifactory.addFormLink("manage.media", buttonLayout, Link.BUTTON);

		uifactory.addFormCancelButton("cancel", buttonLayout, ureq, getWindowControl());
	}

	@Override
	protected void doDispose() {
		//
	}
}
