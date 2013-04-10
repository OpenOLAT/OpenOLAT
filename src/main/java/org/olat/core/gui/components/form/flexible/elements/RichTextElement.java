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
package org.olat.core.gui.components.form.flexible.elements;

import org.olat.core.gui.components.form.flexible.impl.elements.richText.RichTextConfiguration;

/**
 * Description:<br>
 * The rich text flexi form element is a text area that can be formatted using
 * WYSIWYG techniques.The formatting is limited to what is possible in a
 * browser.
 * 
 * <P>
 * Initial Date: 21.04.2009 <br>
 * 
 * @author gnaegi
 */
public interface RichTextElement extends TextElement {

	/**
	 * Get the rich text editor configuration object
	 * 
	 * @return
	 */
	RichTextConfiguration getEditorConfiguration();
	
	/**
	 * Get the raw value of the text element as submitted by the user. Be aware
	 * that this method does NOT filter against XSS attacks! Use this only when
	 * you manually check for XSS attacks or you allow users at this point to
	 * add unsave data including javascript code.
	 * 
	 * @return
	 */
	public String getRawValue();
}
