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
import org.olat.modules.ceditor.PageElement;
import org.olat.modules.ceditor.PageElementEditorController;
import org.olat.modules.ceditor.model.HTMLElement;
import org.olat.modules.ceditor.model.HTMLRawElement;
import org.olat.modules.ceditor.model.ParagraphElement;
import org.olat.modules.ceditor.model.TitleElement;
import org.olat.modules.ceditor.ui.event.ChangePartEvent;

import com.google.common.base.Objects;

/**
 * 
 * Initial date: 8 sept. 2022<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class TextRunComponent extends PageRunComponent {
	
	public TextRunComponent(TextComponent component) {
		super(component);
	}
	
	@Override
	public void dispatchEvent(UserRequest ureq, Controller source, Event event) {
		if((source instanceof ModalInspectorController || source instanceof PageElementEditorController)
				&& event instanceof ChangePartEvent) {
			ChangePartEvent cpe = (ChangePartEvent)event;
			
			String newValue = null;
			PageElement element = cpe.getElement();
			if(element instanceof TitleElement) {
				TitleElement title = (TitleElement)element;
				newValue = TitleElement.toHtml(title.getContent(), title.getTitleSettings());
			} else if(element instanceof HTMLElement) {
				HTMLElement raw = (HTMLElement)element;
				newValue = raw.getContent();
				
				String elementCssClass = null;
				if(element instanceof HTMLRawElement) {
					elementCssClass = ComponentsFactory.getElementCssClass((HTMLRawElement)element);
				} else if(element instanceof ParagraphElement) {
					elementCssClass = ComponentsFactory.getElementCssClass((ParagraphElement)element);
				}
				((TextComponent)getComponent()).setElementCssClass(elementCssClass);
			}
			if(newValue != null && !Objects.equal(newValue, ((TextComponent)getComponent()).getDisplayText())) {
				((TextComponent)getComponent()).setText(newValue);
			}
		}
	}
}
