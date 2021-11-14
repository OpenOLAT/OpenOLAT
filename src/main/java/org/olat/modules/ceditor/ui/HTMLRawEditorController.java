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

import java.util.ArrayList;
import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.RichTextElement;
import org.olat.core.gui.components.form.flexible.elements.StaticTextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.render.DomWrapperElement;
import org.olat.core.util.CodeHelper;
import org.olat.core.util.Formatter;
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
	
	private Link column1Link;
	private Link column2Link;
	private Link column3Link;
	private Link column4Link;
	
	private RichTextElement htmlItem;
	private StaticTextElement staticItem;
	
	private HTMLElement htmlPart;
	private boolean editMode = false;
	private final boolean minimalEditor;
	private final PageElementStore<HTMLElement> store;
	
	public HTMLRawEditorController(UserRequest ureq, WindowControl wControl, HTMLElement htmlPart, PageElementStore<HTMLElement> store,
			boolean minimalEditor) {
		super(ureq, wControl, "html_raw_editor");
		this.htmlPart = htmlPart;
		this.store = store;
		this.minimalEditor = minimalEditor;
		
		initForm(ureq);
		setEditMode(editMode);
		
		column1Link = LinkFactory.createToolLink("text.column.1", translate("text.column.1"), this);
		column2Link = LinkFactory.createToolLink("text.column.2", translate("text.column.2"), this);
		column3Link = LinkFactory.createToolLink("text.column.3", translate("text.column.3"), this);
		column4Link = LinkFactory.createToolLink("text.column.4", translate("text.column.4"), this);

		if(StringHelper.containsNonWhitespace(htmlPart.getLayoutOptions())) {
			TextSettings settings = ContentEditorXStream.fromXml(htmlPart.getLayoutOptions(), TextSettings.class);
			setActiveColumLink(settings.getNumOfColumns());
		} else {
			setActiveColumLink(1);
		}
	}

	@Override
	public boolean isEditMode() {
		return editMode;
	}

	@Override
	public void setEditMode(boolean editMode) {
		this.editMode = editMode;
		htmlItem.setVisible(editMode);
		staticItem.setVisible(!editMode);
		flc.getFormItemComponent().contextPut("editMode", Boolean.valueOf(editMode));
	}

	@Override
	public List<Link> getOptionLinks() {
		List<Link> links = new ArrayList<>(5);
		links.add(column4Link);
		links.add(column3Link);
		links.add(column2Link);
		links.add(column1Link);
		return links;
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

		String formattedContent = Formatter.formatLatexFormulas(content);
		staticItem = uifactory.addStaticTextElement(cmpId + "_static", null, formattedContent, formLayout);
		staticItem.setDomWrapperElement(DomWrapperElement.div); // content contains multiple P elements
		
		((FormLayoutContainer)formLayout).contextPut("htmlCmpId", cmpId);
	}

	@Override
	protected void propagateDirtinessToContainer(FormItem fiSrc, FormEvent fe) {
		//
	}

	@Override
	public void event(UserRequest ureq, Component source, Event event) {
		if(column1Link == source) {
			doSetColumn(1);
		} else if(column2Link == source) {
			doSetColumn(2);
		} else if(column3Link == source) {
			doSetColumn(3);
		} else if(column4Link == source) {
			doSetColumn(4);
		}
		super.event(ureq, source, event);
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(htmlItem == source) {
			String content = htmlItem.getValue();
			htmlPart.setContent(content);
			htmlPart = store.savePageElement(htmlPart);
			String formattedContent = Formatter.formatLatexFormulas(content);
			staticItem.setValue(formattedContent);
			fireEvent(ureq, new ChangePartEvent(htmlPart));
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		String content = htmlItem.getValue();
		htmlPart.setContent(content);
		htmlPart = store.savePageElement(htmlPart);

		String formattedContent = Formatter.formatLatexFormulas(content);
		staticItem.setValue(formattedContent);
		fireEvent(ureq, new ChangePartEvent(htmlPart));
	}
	
	private void setActiveColumLink(int numOfColumns) {
		column1Link.setIconLeftCSS("o_icon o_icon_column");
		column2Link.setIconLeftCSS("o_icon o_icon_columns");
		column3Link.setIconLeftCSS("o_icon o_icon_columns");
		column4Link.setIconLeftCSS("o_icon o_icon_columns");
		if(numOfColumns == 1) {
			column1Link.setIconLeftCSS("o_icon o_icon_check");
		} else if(numOfColumns == 2) {
			column2Link.setIconLeftCSS("o_icon o_icon_check");
		} else if(numOfColumns == 3) {
			column3Link.setIconLeftCSS("o_icon o_icon_check");
		} else if(numOfColumns == 4) {
			column4Link.setIconLeftCSS("o_icon o_icon_check");
		}
		flc.getFormItemComponent().contextPut("htmlRawClass", "o_ce_html_raw o_html_col" + numOfColumns);
		flc.setDirty(true);
	}
	
	private void doSetColumn(int numOfColumns) {
		TextSettings settings;
		if(StringHelper.containsNonWhitespace(htmlPart.getLayoutOptions())) {
			settings = ContentEditorXStream.fromXml(htmlPart.getLayoutOptions(), TextSettings.class);
		} else {
			settings = new TextSettings();
		}
		
		settings.setNumOfColumns(numOfColumns);

		String settingsXml = ContentEditorXStream.toXml(settings);
		htmlPart.setLayoutOptions(settingsXml);
		htmlPart = store.savePageElement(htmlPart);
		setActiveColumLink(numOfColumns);
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
