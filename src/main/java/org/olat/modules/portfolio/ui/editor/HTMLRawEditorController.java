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
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.CodeHelper;
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
public class HTMLRawEditorController extends FormBasicController {
	
	private RichTextElement htmlItem;
	
	private HTMLPart htmlPart;
	
	@Autowired
	private PortfolioService portfolioService;
	
	public HTMLRawEditorController(UserRequest ureq, WindowControl wControl, HTMLPart htmlPart) {
		super(ureq, wControl, LAYOUT_BAREBONE);
		this.htmlPart = htmlPart;
		
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		String cmpId = "html-" + CodeHelper.getRAMUniqueID() + "h";
		String content = htmlPart.getContent();
		htmlItem = uifactory.addRichTextElementForStringDataCompact(cmpId, null, content, 8, 80, null, formLayout, ureq.getUserSession(), getWindowControl());
		//htmlItem.getEditorConfiguration().setInline(true);
		
		//FormLayoutContainer buttonsCont = FormLayoutContainer.createDefaultFormLayout("buttons", getTranslator());
		//formLayout.add(buttonsCont);
		uifactory.addFormSubmitButton("save", formLayout);
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(htmlItem == source) {
			String content = htmlItem.getValue();
			htmlPart.setContent(content);
			htmlPart = (HTMLPart)portfolioService.updatePart(htmlPart);
			fireEvent(ureq, new ChangePartEvent(htmlPart));
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		String content = htmlItem.getValue();
		htmlPart.setContent(content);
		htmlPart = (HTMLPart)portfolioService.updatePart(htmlPart);
		fireEvent(ureq, new ChangePartEvent(htmlPart));
	}

	@Override
	protected void doDispose() {
		//
	}
}
