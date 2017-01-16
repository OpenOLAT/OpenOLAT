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
package org.olat.modules.forms.ui.model;

import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.modules.forms.model.xml.TextInput;

/**
 * 
 * Initial date: 19 déc. 2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class TextInputWrapper {
	
	private final String name;
	private final String color;
	private final String content;
	private final TextInput textInput;
	private final TextElement textEl;
	private final FormLink saveButton;
	
	public TextInputWrapper(String name, String color, String content, TextInput textInput) {
		this.textInput = textInput;
		this.textEl = null;
		this.saveButton = null;
		this.name = name;
		this.color = color;
		this.content = content;
	}
	
	public TextInputWrapper(TextInput textInput, TextElement textEl, FormLink saveButton) {
		this.textInput = textInput;
		this.textEl = textEl;
		this.saveButton = saveButton;
		name = null;
		color = null;
		content = null;
	}
	
	
	public String getId() {
		return textInput.getId();
	}

	public TextInput getTextInput() {
		return textInput;
	}

	public TextElement getTextEl() {
		return textEl;
	}

	public FormLink getSaveButton() {
		return saveButton;
	}

	public String getName() {
		return name;
	}

	public String getColor() {
		return color;
	}

	public String getContent() {
		return content;
	}
}
