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
package org.olat.modules.ceditor.handler;

import java.util.Locale;

import org.olat.core.CoreSpringFactory;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.ComponentEventListener;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.Util;
import org.olat.modules.ceditor.PageElement;
import org.olat.modules.ceditor.PageElementCategory;
import org.olat.modules.ceditor.PageElementHandler;
import org.olat.modules.ceditor.PageElementInspectorController;
import org.olat.modules.ceditor.PageElementStore;
import org.olat.modules.ceditor.PageRunElement;
import org.olat.modules.ceditor.PageService;
import org.olat.modules.ceditor.RenderingHints;
import org.olat.modules.ceditor.SimpleAddPageElementHandler;
import org.olat.modules.ceditor.model.CodeElement;
import org.olat.modules.ceditor.model.jpa.CodePart;
import org.olat.modules.ceditor.ui.CodeEditorController;
import org.olat.modules.ceditor.ui.CodeInspectorController;
import org.olat.modules.ceditor.ui.CodeRunController;

/**
 * Initial date: 2023-12-11<br>
 *
 * @author cpfranger, christoph.pfranger@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
public class CodeElementHandler implements PageElementHandler, PageElementStore<CodeElement>,
		SimpleAddPageElementHandler, ComponentEventListener {

	public CodeElementHandler() {
	}

	@Override
	public String getType() {
		return "code";
	}

	@Override
	public String getIconCssClass() {
		return "o_icon_code";
	}

	@Override
	public PageElementCategory getCategory() {
		return PageElementCategory.content;
	}

	@Override
	public PageRunElement getContent(UserRequest ureq, WindowControl wControl, PageElement element, RenderingHints options) {
		if (element instanceof CodeElement codeElement) {
			return new CodeRunController(ureq, wControl, codeElement);
		}
		return null;
	}

	@Override
	public Controller getEditor(UserRequest ureq, WindowControl wControl, PageElement element) {
		if (element instanceof CodePart codePart) {
			return new CodeEditorController(ureq, wControl, codePart, this);
		}
		return null;
	}

	@Override
	public PageElementInspectorController getInspector(UserRequest ureq, WindowControl wControl, PageElement element) {
		if (element instanceof CodePart codePart) {
			return new CodeInspectorController(ureq, wControl, codePart, this);
		}
		return null;
	}

	@Override
	public PageElement createPageElement(Locale locale) {
		CodePart codePart = new CodePart();
		String content = Util.createPackageTranslator(CodeEditorController.class, locale).translate("code.example");
		codePart.setContent(content);
		return codePart;
	}

	@Override
	public CodeElement savePageElement(CodeElement element) {
		return CoreSpringFactory.getImpl(PageService.class).updatePart((CodePart) element);
	}

	@Override
	public void dispatchEvent(UserRequest ureq, Component source, Event event) {
	}
}
