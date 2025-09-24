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
import org.olat.core.gui.components.text.TextComponent;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.core.util.filter.FilterFactory;
import org.olat.modules.ceditor.PageElement;
import org.olat.modules.ceditor.PageElementEditorController;
import org.olat.modules.ceditor.model.HTMLElement;
import org.olat.modules.ceditor.model.HTMLRawElement;
import org.olat.modules.ceditor.model.ParagraphElement;
import org.olat.modules.ceditor.model.TitleElement;
import org.olat.modules.ceditor.model.TitleSettings;
import org.olat.modules.ceditor.ui.event.ChangePartEvent;

import com.google.common.base.Objects;

/**
 * 
 * Initial date: 8 sept. 2022<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class TextRunComponent extends PageRunComponent {

	private final boolean inForm;

	public TextRunComponent(TextComponent component, boolean inForm) {
		super(component);
		this.inForm = inForm;
	}
	
	@Override
	public void dispatchEvent(UserRequest ureq, Controller source, Event event) {
		if ((source instanceof ModalInspectorController || source instanceof PageElementEditorController)
				&& event instanceof ChangePartEvent changePartEvent) {
			String newValue = null;
			String newCssClass = null;
			PageElement element = changePartEvent.getElement();
			if (element instanceof TitleElement titleElement) {
				TitleSettings titleSettings = titleElement.getTitleSettings();
				newCssClass = TitleElement.toCssClassWithMarkerClass(titleSettings, inForm);
				if (StringHelper.containsNonWhitespace(titleElement.getContent())) {
					newValue = TitleElement.toHtml(titleElement.getContent(), titleSettings);
				} else {
					String content = Util.createPackageTranslator(TitleEditorController.class, ureq.getLocale()).translate("title.placeholder");
					newValue = TitleElement.toHtmlPlaceholder(content, titleSettings);
				}
			} else if (element instanceof HTMLElement htmlElement) {
				newValue = htmlElement.getContent();
				String elementCssClass = null;
				if (element instanceof HTMLRawElement htmlRawElement) {
					elementCssClass = ComponentsFactory.getElementCssClass(htmlRawElement);
					newCssClass = ComponentsFactory.getCssClass(htmlRawElement, inForm);
				} else if (element instanceof ParagraphElement paragraphElement) {
					elementCssClass = ComponentsFactory.getElementCssClass(paragraphElement);
					newCssClass = ComponentsFactory.getCssClass(paragraphElement, inForm);
				}
				if (getComponent() instanceof TextComponent textComponent) {
					textComponent.setElementCssClass(elementCssClass);
				}
			}
			if (getComponent() instanceof TextComponent textComponent) {
				if (source instanceof PageElementEditorController && element instanceof ParagraphElement paragraphElement) {
					String raw = FilterFactory.getHtmlTagsFilter().filter(paragraphElement.getContent());
					if (StringHelper.containsNonWhitespace(raw)) {
						newValue = ComponentsFactory.getContent(paragraphElement, true).getDisplayText();
					} else {
						String placeholder = Util.createPackageTranslator(TextRunComponent.class, ureq.getLocale()).translate("text.placeholder");
						newValue = ComponentsFactory.getContent(paragraphElement, placeholder).getDisplayText();
					}
				}
				if (newValue != null && !Objects.equal(newValue, textComponent.getDisplayText())) {
					textComponent.setText(newValue);
				}
				if (newCssClass != null && !Objects.equal(newCssClass, textComponent.getCssClass())) {
					textComponent.setCssClass(newCssClass);
				}
			}
		}
	}
}
