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
package org.olat.modules.ceditor.ui.handler;

import java.util.Locale;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.text.TextComponent;
import org.olat.core.gui.components.text.TextFactory;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.CodeHelper;
import org.olat.core.util.Formatter;
import org.olat.core.util.StringHelper;
import org.olat.modules.ceditor.ContentEditorXStream;
import org.olat.modules.ceditor.PageElement;
import org.olat.modules.ceditor.PageElementEditorController;
import org.olat.modules.ceditor.PageElementHandler;
import org.olat.modules.ceditor.PageElementRenderingHints;
import org.olat.modules.ceditor.PageRunElement;
import org.olat.modules.ceditor.SimpleAddPageElementHandler;
import org.olat.modules.ceditor.model.TextSettings;
import org.olat.modules.ceditor.ui.HTMLRawEditorController;
import org.olat.modules.ceditor.ui.PageRunComponent;
import org.olat.modules.portfolio.model.HTMLPart;

/**
 * 
 * Initial date: 01.07.2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class HTMLRawPageElementHandler implements PageElementHandler, SimpleAddPageElementHandler {

	@Override
	public String getType() {
		return "htmlraw";
	}

	@Override
	public String getIconCssClass() {
		// For now we use the paragraph icon until we have a minimized paragraph element o_icon_code
		return "o_icon_paragraph";
	}

	@Override
	public PageRunElement getContent(UserRequest ureq, WindowControl wControl, PageElement element, PageElementRenderingHints options) {
		String content = "";
		int numOfColumns = 1;
		if(element instanceof HTMLPart) {
			HTMLPart htmlPart = (HTMLPart)element;
			content = htmlPart.getContent();
			content = Formatter.formatLatexFormulas(content);
			
			if(StringHelper.containsNonWhitespace(htmlPart.getLayoutOptions())) {
				TextSettings settings = ContentEditorXStream.fromXml(htmlPart.getLayoutOptions(), TextSettings.class);
				numOfColumns = settings.getNumOfColumns();
			}
		}
		TextComponent cmp = TextFactory.createTextComponentFromString("htmlRawCmp" + CodeHelper.getRAMUniqueID(), content, null, false, null);
		cmp.setElementCssClass("o_pf_html_raw o_html_col" + numOfColumns);
		return new PageRunComponent(cmp);
	}

	@Override
	public PageElementEditorController getEditor(UserRequest ureq, WindowControl wControl, PageElement element) {
		if(element instanceof HTMLPart) {
			return new HTMLRawEditorController(ureq, wControl, (HTMLPart)element);
		}
		return null;
	}

	@Override
	public PageElement createPageElement(Locale locale) {
		return new HTMLPart();
	}
}
