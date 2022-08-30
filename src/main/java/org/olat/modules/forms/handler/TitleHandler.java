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
import java.util.concurrent.atomic.AtomicInteger;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.components.text.TextFactory;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.translator.Translator;
import org.olat.core.util.Util;
import org.olat.modules.ceditor.CloneElementHandler;
import org.olat.modules.ceditor.ContentEditorXStream;
import org.olat.modules.ceditor.PageElement;
import org.olat.modules.ceditor.PageElementCategory;
import org.olat.modules.ceditor.PageElementEditorController;
import org.olat.modules.ceditor.PageElementInspectorController;
import org.olat.modules.ceditor.PageElementRenderingHints;
import org.olat.modules.ceditor.PageElementStore;
import org.olat.modules.ceditor.PageRunElement;
import org.olat.modules.ceditor.SimpleAddPageElementHandler;
import org.olat.modules.ceditor.model.TitleElement;
import org.olat.modules.ceditor.model.TitleSettings;
import org.olat.modules.ceditor.ui.PageRunComponent;
import org.olat.modules.ceditor.ui.TitleEditorController;
import org.olat.modules.ceditor.ui.TitleInspectorController;
import org.olat.modules.forms.SessionFilter;
import org.olat.modules.forms.model.xml.Title;
import org.olat.modules.forms.ui.ReportHelper;
import org.olat.modules.forms.ui.model.EvaluationFormComponentElement;
import org.olat.modules.forms.ui.model.EvaluationFormComponentReportElement;
import org.olat.modules.forms.ui.model.EvaluationFormExecutionElement;
import org.olat.modules.forms.ui.model.EvaluationFormReportElement;
import org.olat.modules.forms.ui.model.ExecutionIdentity;

/**
 * 
 * Initial date: 04.07.2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class TitleHandler implements EvaluationFormElementHandler, PageElementStore<TitleElement>,
		SimpleAddPageElementHandler, CloneElementHandler, EvaluationFormReportHandler {

	private static final AtomicInteger idGenerator = new AtomicInteger();
	
	@Override
	public String getType() {
		return "formhtitle";
	}

	@Override
	public String getIconCssClass() {
		return "o_icon_header";
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
		if(element instanceof Title) {
			return new TitleEditorController(ureq, wControl, (Title)element, this);
		}
		return null;
	}
	
	@Override
	public PageElementInspectorController getInspector(UserRequest ureq, WindowControl wControl, PageElement element) {
		if(element instanceof Title) {
			return new TitleInspectorController(ureq, wControl, (Title)element, this);
		}
		return null;
	}

	@Override
	public PageElement createPageElement(Locale locale) {
		Translator translator = Util.createPackageTranslator(TitleEditorController.class, locale);
		String content = translator.translate("title.example");
		Title part = new Title();
		part.setId(UUID.randomUUID().toString());
		part.setContent(content);
		TitleSettings settings = new TitleSettings();
		settings.setSize(3);
		String settingsXml = ContentEditorXStream.toXml(settings);
		part.setLayoutOptions(settingsXml);
		return part;
	}

	@Override
	public PageElement clonePageElement(PageElement element) {
		if (element instanceof Title) {
			Title title = (Title)element;
			Title clone = new Title();
			clone.setId(UUID.randomUUID().toString());
			clone.setContent(title.getContent());
			clone.setLayoutOptions(title.getLayoutOptions());
			return clone;
		}
		return null;
	}

	@Override
	public TitleElement savePageElement(TitleElement element) {
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
		String content = "";
		if(element instanceof Title) {
			content = ((Title)element).getContent();
		}
		return TextFactory.createTextComponentFromString("title_" + idGenerator.incrementAndGet(), content, null, false, null);
	}

	@Override
	public EvaluationFormReportElement getReportElement(UserRequest ureq, WindowControl windowControl, PageElement element,
			SessionFilter filter, ReportHelper reportHelper) {
		return new EvaluationFormComponentReportElement(getComponent(element));
	}

}
