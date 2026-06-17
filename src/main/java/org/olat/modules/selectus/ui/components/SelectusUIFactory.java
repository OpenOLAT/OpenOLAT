/**
 * <a href="https://www.openolat.org">
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
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.modules.selectus.ui.components;

import java.util.Collections;

import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.FormUIFactory;
import org.olat.core.gui.components.form.flexible.elements.TextElement;

/**
 * 
 * Initial date: 12 mai 2026<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
public class SelectusUIFactory {
	
	private SelectusUIFactory() {
		//
	}
	
	public static final ReflectionStaticElement addReflectionStaticText(String name, String i18nLabel, TextElement elementToCopy, FormItemContainer formLayout) {
		ReflectionStaticElementImpl reflectEl = new ReflectionStaticElementImpl(name);
		if(elementToCopy != null) {
			reflectEl.setTextElements(Collections.singletonList(elementToCopy));
		}
		FormUIFactory.setLabelIfNotNull(i18nLabel, reflectEl);
		if(formLayout != null) {
			formLayout.add(reflectEl);
		}
		return reflectEl;
	}

}
