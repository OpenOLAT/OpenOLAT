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
package org.olat.core.gui.control.generic.wizard;

import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.impl.elements.FormLinkImpl;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.translator.Translator;
import org.olat.core.util.CodeHelper;

/**
 * 
 * Initial date: 13 Jan 2021<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class BasicStepCollection implements StepCollection {
	
	private FormItem titleEl;
	
	public void setTitle(Translator translator, String i18nKey) {
		titleEl = new FormLinkImpl(i18nKey, i18nKey);
		titleEl.setTranslator(translator);
	}
	
	public void setTitle(Translator translator, String i18nKey, String[] i18nArgs) {
		String translatedTitle = translator.translate(i18nKey, i18nArgs);
		setTitle(translatedTitle);
	}
	
	public void setTitle(String translatedTitle) {
		titleEl = new FormLinkImpl(CodeHelper.getUniqueID(), null, translatedTitle, Link.FLEXIBLEFORMLNK + Link.NONTRANSLATED);
	}

	@Override
	public FormItem getTitle() {
		return titleEl;
	}

}
