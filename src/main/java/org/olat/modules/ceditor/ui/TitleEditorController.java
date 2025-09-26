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
package org.olat.modules.ceditor.ui;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.RichTextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.StringHelper;
import org.olat.core.util.filter.FilterFactory;
import org.olat.modules.ceditor.PageElementEditorController;
import org.olat.modules.ceditor.PageElementStore;
import org.olat.modules.ceditor.model.TitleElement;
import org.olat.modules.ceditor.model.TitleSettings;
import org.olat.modules.ceditor.ui.event.ChangePartEvent;
import org.olat.modules.ceditor.ui.event.DropToEditorEvent;
import org.olat.modules.ceditor.ui.event.DropToPageElementEvent;
import org.olat.modules.ceditor.ui.event.EditPageElementEvent;

/**
 * 
 * Initial date: 04.07.2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class TitleEditorController extends FormBasicController implements PageElementEditorController {
	
	private RichTextElement titleItem;
	
	private TitleElement title;
	private final PageElementStore<TitleElement> store;
	private final boolean inForm;

	public TitleEditorController(UserRequest ureq, WindowControl wControl, TitleElement title, PageElementStore<TitleElement> store, boolean inForm) {
		super(ureq, wControl, "title_editor");
		this.title = title;
		this.store = store;
		this.inForm = inForm;

		initForm(ureq);

		setBlockLayoutClass();
		setHeight();
	}

	private void setBlockLayoutClass() {
		flc.contextPut("blockLayoutClass", BlockLayoutClassFactory.buildClass(title.getTitleSettings().getLayoutSettings(), inForm));
	}

	private void setHeight() {
		TitleSettings titleSettings = title.getTitleSettings();
		int size = titleSettings.getSize();
		if (size >= 1 && size <= 6) {
			int heightInPixels = 42 - (size - 1) * 6; // trial and error
			titleItem.getEditorConfiguration().setEditorHeight(heightInPixels + "px");
		}
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		TitleSettings titleSettings = title.getTitleSettings();
		String content = TitleElement.toHtmlForEditor(title.getContent(), titleSettings);
		titleItem = uifactory.addRichTextElementForStringDataCompact("title", null, content, 1, 80, null, formLayout, ureq.getUserSession(), getWindowControl());
		titleItem.getEditorConfiguration().setSendOnBlur(true);
		titleItem.getEditorConfiguration().disableMenuAndMenuBar();
		titleItem.setElementCssClass("o_tiny_icon_placeholder o_title_editor");
		titleItem.setPlaceholderKey("title.placeholder", null);
	}

	@Override
	protected void propagateDirtinessToContainer(FormItem fiSrc, FormEvent fe) {
		//
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(source instanceof TitleInspectorController && event instanceof ChangePartEvent) {
			ChangePartEvent cpe = (ChangePartEvent)event;
			if(cpe.getElement().equals(title)) {
				title = (TitleElement)cpe.getElement();
				doUpdate();
			}
		}
		super.event(ureq, source, event);
	}

	@Override
	public void event(UserRequest ureq, Component source, Event event) {
		if (event instanceof DropToEditorEvent dropToEditorEvent) {
			syncContent(ureq, dropToEditorEvent.getContent());
		} else if (event instanceof DropToPageElementEvent dropToPageElementEvent) {
			syncContent(ureq, dropToPageElementEvent.getContent());
		} else if (event instanceof EditPageElementEvent) {
			if (StringHelper.containsNonWhitespace(title.getContent())) {
				String formattedContent = getFormattedContent(title.getContent());
				titleItem.setValue(formattedContent);
			} else {
				titleItem.setValue("");
			}
		}
		super.event(ureq, source, event);
	}

	private String getFormattedContent(String content) {
		String rawContent = FilterFactory.getHtmlTagsFilter().filter(content);
		TitleSettings titleSettings = title.getTitleSettings();
		return TitleElement.toHtmlForEditor(rawContent, titleSettings);
	}

	private void syncContent(UserRequest ureq, String content) {
		String formattedContent = getFormattedContent(content);
		if (!titleItem.getValue().equals(formattedContent)) {
			titleItem.setValue(formattedContent);
			doSave(ureq);
		}
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(titleItem == source && RichTextElement.SAVE_INLINE_EVENT.equals(event.getCommand())) {
			doSave(ureq);
			titleItem.setValue(getFormattedContent(titleItem.getValue()));
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		doSave(ureq);
	}
	
	private void doUpdate() {
		TitleSettings titleSettings = title.getTitleSettings();
		String content = title.getContent();
		String htmlContent = TitleElement.toHtmlForEditor(content, titleSettings);
		titleItem.setValue(htmlContent);
		setBlockLayoutClass();
		setHeight();
	}
	
	private void doSave(UserRequest ureq) {
		String htmlContent = titleItem.getValue();
		String content = FilterFactory.getHtmlTagsFilter().filter(htmlContent);
		title.setContent(content);
		title = store.savePageElement(title);
		fireEvent(ureq, new ChangePartEvent(title));
	}
}
