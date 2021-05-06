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
package org.olat.modules.forms.handler;

import java.util.Locale;
import java.util.UUID;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.components.text.TextFactory;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.translator.Translator;
import org.olat.core.util.CodeHelper;
import org.olat.core.util.Util;
import org.olat.modules.ceditor.CloneElementHandler;
import org.olat.modules.ceditor.PageElement;
import org.olat.modules.ceditor.PageElementCategory;
import org.olat.modules.ceditor.PageElementEditorController;
import org.olat.modules.ceditor.PageElementRenderingHints;
import org.olat.modules.ceditor.PageElementStore;
import org.olat.modules.ceditor.PageRunElement;
import org.olat.modules.ceditor.SimpleAddPageElementHandler;
import org.olat.modules.ceditor.model.HTMLElement;
import org.olat.modules.ceditor.ui.ComponentsFactory;
import org.olat.modules.ceditor.ui.HTMLRawEditorController;
import org.olat.modules.ceditor.ui.PageRunComponent;
import org.olat.modules.forms.SessionFilter;
import org.olat.modules.forms.model.xml.HTMLParagraph;
import org.olat.modules.forms.ui.ReportHelper;
import org.olat.modules.forms.ui.model.EvaluationFormComponentElement;
import org.olat.modules.forms.ui.model.EvaluationFormComponentReportElement;
import org.olat.modules.forms.ui.model.EvaluationFormExecutionElement;
import org.olat.modules.forms.ui.model.EvaluationFormReportElement;
import org.olat.modules.forms.ui.model.ExecutionIdentity;

/**
 * 
 * Initial date: 19 sept. 2018<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class HTMLParagraphHandler implements EvaluationFormElementHandler, PageElementStore<HTMLElement>, SimpleAddPageElementHandler, CloneElementHandler, EvaluationFormReportHandler {

	@Override
	public String getType() {
		return "formhtmlparagraph";
	}

	@Override
	public String getIconCssClass() {
		return "o_icon_paragraph";
	}
	
	@Override
	public PageElementCategory getCategory() {
		return PageElementCategory.content;
	}

	@Override
	public PageRunElement getContent(UserRequest ureq, WindowControl wControl, PageElement element, PageElementRenderingHints hints) {
		Component cmp = getComponent(element);
		return new PageRunComponent(cmp);
	}

	@Override
	public PageElementEditorController getEditor(UserRequest ureq, WindowControl wControl, PageElement element) {
		if(element instanceof HTMLParagraph) {
			return new HTMLRawEditorController(ureq, wControl, (HTMLParagraph)element, this, true);
		}
		return null;
	}

	@Override
	public PageElement createPageElement(Locale locale) {
		Translator translator = Util.createPackageTranslator(HTMLRawEditorController.class, locale);
		String content = translator.translate("raw.example");
		HTMLParagraph part = new HTMLParagraph();
		part.setId(UUID.randomUUID().toString());
		part.setContent(content);
		return part;
	}

	@Override
	public PageElement clonePageElement(PageElement element) {
		if (element instanceof HTMLParagraph) {
			HTMLParagraph htmlParagraph = (HTMLParagraph)element;
			HTMLParagraph clone = new HTMLParagraph();
			clone.setId(UUID.randomUUID().toString());
			clone.setContent(htmlParagraph.getContent());
			clone.setLayoutOptions(htmlParagraph.getLayoutOptions());
			return clone;
		}
		return null;
	}

	@Override
	public HTMLElement savePageElement(HTMLElement element) {
		return element;
	}

	@Override
	public EvaluationFormExecutionElement getExecutionElement(UserRequest ureq, WindowControl wControl, Form rootForm,
			PageElement element, ExecutionIdentity executionIdentity) {
		PageRunElement runElement = getContent(ureq, wControl, element, null);
		if (runElement != null) {
			return new EvaluationFormComponentElement(runElement);
		}
		return null;
	}

	private Component getComponent(PageElement element) {
		if(element instanceof HTMLParagraph) {
			return ComponentsFactory.getContent((HTMLParagraph)element);
		}
		return TextFactory.createTextComponentFromString("htmlparagraph_" + CodeHelper.getRAMUniqueID(), "", null, false, null);
	}

	@Override
	public EvaluationFormReportElement getReportElement(UserRequest ureq, WindowControl windowControl, PageElement element,
			SessionFilter filter, ReportHelper reportHelper) {
		return new EvaluationFormComponentReportElement(getComponent(element));
	}

}
