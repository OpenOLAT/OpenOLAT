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
package org.olat.modules.portfolio.ui.editor;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.RichTextElement;
import org.olat.core.gui.components.form.flexible.elements.StaticTextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.CodeHelper;
import org.olat.core.util.Formatter;
import org.olat.core.util.StringHelper;
import org.olat.core.util.filter.FilterFactory;
import org.olat.modules.portfolio.PortfolioService;
import org.olat.modules.portfolio.model.HTMLPart;
import org.olat.modules.portfolio.ui.editor.event.ChangePartEvent;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 01.07.2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class HTMLRawEditorController extends FormBasicController implements PageElementEditorController {
	
	private RichTextElement htmlItem;
	private StaticTextElement staticItem;
	
	private HTMLPart htmlPart;
	private boolean editMode = false;
	
	@Autowired
	private PortfolioService portfolioService;
	
	public HTMLRawEditorController(UserRequest ureq, WindowControl wControl, HTMLPart htmlPart) {
		super(ureq, wControl, LAYOUT_BAREBONE);
		this.htmlPart = htmlPart;
		
		initForm(ureq);
		setEditMode(editMode);
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
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		String cmpId = "html-" + CodeHelper.getRAMUniqueID() + "h";
		String content = htmlPart.getContent();
		htmlItem = uifactory.addRichTextElementForStringDataCompact(cmpId, null, content, 8, 80, null, formLayout, ureq.getUserSession(), getWindowControl());
		htmlItem.getEditorConfiguration().setSendOnBlur(true);
		htmlItem.getEditorConfiguration().disableImageAndMovie();
		htmlItem.getEditorConfiguration().setAutoResizeEnabled(true, -1, 40, 0);
		
		String formattedContent = Formatter.formatLatexFormulas(contentOrExample(content));
		staticItem = uifactory.addStaticTextElement(cmpId + "_static", formattedContent, formLayout);
	}

	@Override
	protected void propagateDirtinessToContainer(FormItem fiSrc, FormEvent fe) {
		//super.propagateDirtinessToContainer(fiSrc, fe);
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(htmlItem == source) {
			String content = htmlItem.getValue();
			htmlPart.setContent(content);
			htmlPart = portfolioService.updatePart(htmlPart);
			String formattedContent = Formatter.formatLatexFormulas(contentOrExample(content));
			staticItem.setValue(formattedContent);
			fireEvent(ureq, new ChangePartEvent(htmlPart));
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		String content = htmlItem.getValue();
		htmlPart.setContent(content);
		htmlPart = portfolioService.updatePart(htmlPart);

		String formattedContent = Formatter.formatLatexFormulas(contentOrExample(content));
		staticItem.setValue(formattedContent);
		fireEvent(ureq, new ChangePartEvent(htmlPart));
	}
	
	private String contentOrExample(String content) {
		String raw = FilterFactory.getHtmlTagsFilter().filter(content);
		String staticContent = content;
		if (!StringHelper.containsNonWhitespace(raw)) {
			staticContent = getTranslator().translate("raw.example");
		}
		return staticContent;
	}

	@Override
	protected void doDispose() {
		//
	}
}
