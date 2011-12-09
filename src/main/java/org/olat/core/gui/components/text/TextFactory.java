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
package org.olat.core.gui.components.text;

import org.olat.core.gui.components.Container;
import org.olat.core.gui.translator.Translator;

/**
 * Description:<br>
 * Use this factory to create simple text component elements. Text fragments can
 * be updated later with setter methods. In AJAX mode this means that only those
 * fragments are replaced using DOM replacement technologies.
 * 
 * <P>
 * Initial Date: 11.11.2009 <br>
 * 
 * @author gnaegi
 */
public class TextFactory {

	/**
	 * Factory method to create a text component with a translated plain vanilla
	 * text string. The text is displayed as is and not translated at render time.
	 * 
	 * @param name the name of the component
	 * @param text the plain vanilla text
	 * @param cssClass the css class(es) to be used or NULL if not available
	 * @param renderAsSpan true: use a span to wrap the text; false: use a div to
	 *          wrap the text
	 * @param container The container to which this component should be added or
	 *          NULL if you add it your self (e.g using another name as the
	 *          component name)
	 */
	public static TextComponent createTextComponentFromString(String name, String text, String cssClass, boolean renderAsSpan,
			Container container) {
		TextComponent comp = new TextComponent(name, text, cssClass, renderAsSpan);
		if (container != null) {
			container.put(name, comp);
		}
		return comp;
	}

	/**
	 * Constructor to create a text component with an untranslated i18n key. The
	 * i18n key is first translated with the given translator and then displayed.
	 * 
	 * @param name the name of the component
	 * @param i18nKey the i18n key to be translated during render time
	 * @param translator the translator that can translate the i18n key
	 * @param cssClass the css class(es) to be used or NULL if not available
	 * @param renderAsSpan true: use a span to wrap the text; false: use a div to
	 *          wrap the text
	 * @param container The container to which this component should be added or
	 *          NULL if you add it your self (e.g using another name as the
	 *          component name)
	 */
	public static TextComponent createTextComponentFromI18nKey(String name, String i18nKey, Translator translator, String cssClass,
			boolean renderAsSpan, Container container) {
		TextComponent comp = new TextComponent(name, i18nKey, translator, cssClass, renderAsSpan);
		if (container != null) {
			container.put(name, comp);
		}
		return comp;
	}

}
