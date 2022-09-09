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
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.CodeHelper;
import org.olat.core.util.StringHelper;
import org.olat.core.util.filter.FilterFactory;
import org.olat.modules.ceditor.ContentEditorXStream;
import org.olat.modules.ceditor.PageElementEditorController;
import org.olat.modules.ceditor.PageElementStore;
import org.olat.modules.ceditor.model.HTMLElement;
import org.olat.modules.ceditor.model.TextSettings;
import org.olat.modules.ceditor.ui.event.ChangePartEvent;

/**
 * 
 * Initial date: 01.07.2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class HTMLRawEditorController extends FormBasicController implements PageElementEditorController {
	
	private RichTextElement htmlItem;
	
	private HTMLElement htmlPart;
	private final boolean minimalEditor;
	private final PageElementStore<HTMLElement> store;
	
	public HTMLRawEditorController(UserRequest ureq, WindowControl wControl, HTMLElement htmlPart, PageElementStore<HTMLElement> store,
			boolean minimalEditor) {
		super(ureq, wControl, "html_raw_editor");
		this.htmlPart = htmlPart;
		this.store = store;
		this.minimalEditor = minimalEditor;
		
		initForm(ureq);

		if(StringHelper.containsNonWhitespace(htmlPart.getLayoutOptions())) {
			TextSettings settings = ContentEditorXStream.fromXml(htmlPart.getLayoutOptions(), TextSettings.class);
			setActiveColumLink(settings.getNumOfColumns());
		} else {
			setActiveColumLink(1);
		}
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		String cmpId = "html-" + CodeHelper.getRAMUniqueID() + "h";
		String content = contentOrExample(htmlPart.getContent());
		
		if(minimalEditor) {
			htmlItem = uifactory.addRichTextElementForParagraphEditor(cmpId, null, content, 8, 80, formLayout, getWindowControl());
		} else {
			htmlItem = uifactory.addRichTextElementForStringDataCompact(cmpId, null, content, 8, 80, null, formLayout, ureq.getUserSession(), getWindowControl());
		}

		htmlItem.getEditorConfiguration().setSendOnBlur(true);
		htmlItem.getEditorConfiguration().disableImageAndMovie();
		htmlItem.getEditorConfiguration().setAutoResizeEnabled(true, -1, 40, 0);

		((FormLayoutContainer)formLayout).contextPut("htmlCmpId", cmpId);
	}

	@Override
	protected void propagateDirtinessToContainer(FormItem fiSrc, FormEvent fe) {
		//
	}
	
	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(source instanceof HTMLRawInspectorController && event instanceof ChangePartEvent) {
			ChangePartEvent cpe = (ChangePartEvent)event;
			if(cpe.isElement(htmlPart)) {
				htmlPart = (HTMLElement)cpe.getElement();
				TextSettings settings = ContentEditorXStream.fromXml(htmlPart.getLayoutOptions(), TextSettings.class);
				setActiveColumLink(settings.getNumOfColumns());
			}
		}
		super.event(ureq, source, event);
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(htmlItem == source) {
			String content = htmlItem.getValue();
			htmlPart.setContent(content);
			htmlPart = store.savePageElement(htmlPart);
			fireEvent(ureq, new ChangePartEvent(htmlPart));
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		String content = htmlItem.getValue();
		htmlPart.setContent(content);
		htmlPart = store.savePageElement(htmlPart);
		fireEvent(ureq, new ChangePartEvent(htmlPart));
	}
	
	private void setActiveColumLink(int numOfColumns) {
		flc.getFormItemComponent().contextPut("htmlRawClass", "o_ce_html_raw o_html_col" + numOfColumns);
		flc.setDirty(true);
	}
	
	private String contentOrExample(String content) {
		String raw = FilterFactory.getHtmlTagsFilter().filter(content);
		String staticContent = content;
		if (!StringHelper.containsNonWhitespace(raw)) {
			staticContent = getTranslator().translate("raw.example");
		}
		return staticContent;
	}
}
