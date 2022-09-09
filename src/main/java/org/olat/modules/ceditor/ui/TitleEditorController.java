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
import org.olat.modules.ceditor.ui.event.ChangePartEvent;

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
	
	public TitleEditorController(UserRequest ureq, WindowControl wControl, TitleElement title, PageElementStore<TitleElement> store) {
		super(ureq, wControl, "title_editor");
		this.title = title;
		this.store = store;
		
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		String content = TitleElement.toHtml(title.getContent(), title.getTitleSettings());
		titleItem = uifactory.addRichTextElementForStringDataCompact("title", null, content, 2, 80, null, formLayout, ureq.getUserSession(), getWindowControl());
		titleItem.getEditorConfiguration().setSendOnBlur(true);
		titleItem.getEditorConfiguration().disableMenuAndMenuBar();
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
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(titleItem == source) {
			doSave(ureq);
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		doSave(ureq);
	}
	
	private void doUpdate() {
		String content = title.getContent();
		String htmlContent = TitleElement.toHtml(content, title.getTitleSettings());
		titleItem.setValue(htmlContent);
	}
	
	private void doSave(UserRequest ureq) {
		String htmlContent = titleItem.getValue();
		String content = FilterFactory.getHtmlTagsFilter().filter(htmlContent);
		title.setContent(content);
		title = store.savePageElement(title);
		fireEvent(ureq, new ChangePartEvent(title));
	}

	private String contentOrExample(String content) {
		String text = FilterFactory.getHtmlTagsFilter().filter(content);
		String staticContent;
		if (!StringHelper.containsNonWhitespace(text)) {
			staticContent = getTranslator().translate("title.example");
		} else {
			staticContent = TitleElement.toHtml(content, title.getTitleSettings());
		}
		return staticContent;
	}
}
