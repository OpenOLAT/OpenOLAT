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
package org.olat.core.gui.components.form.flexible.impl.elements;

import java.util.List;

import org.olat.core.gui.components.ComponentRenderer;
import org.olat.core.gui.components.textboxlist.TextBoxItem;
import org.olat.core.gui.components.textboxlist.TextBoxListComponent;
import org.olat.core.gui.components.textboxlist.TextBoxListTagifyRenderer;
import org.olat.core.gui.translator.Translator;

/**
 * Description:<br>
 * the concrete class of TextBoxListComponent. 
 * 
 * <P>
 * Initial Date:  27.08.2010 <br>
 * @author Roman Haag, roman.haag@frentix.com, http://www.frentix.com
 */
public class TextBoxListElementComponent extends TextBoxListComponent  {

	private static final ComponentRenderer RENDERER_TAGIFY = new TextBoxListTagifyRenderer();
	private TextBoxListElementImpl element;
	
	public TextBoxListElementComponent(TextBoxListElementImpl element, String name, String inputHint, List<TextBoxItem> initialItems, Translator translator) {
		super(name, inputHint, initialItems, translator);
		this.element = element;
	}
	
	public TextBoxListElementImpl getTextElementImpl(){
		return element;
	}

	@Override
	public ComponentRenderer getHTMLRendererSingleton() {
		return RENDERER_TAGIFY;
	}
}
