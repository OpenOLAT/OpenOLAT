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
package org.olat.modules.forms.ui;

import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.modules.forms.model.xml.Choice;

/**
 * 
 * Initial date: 7 Feb 2019<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class ChoiceRow {
	
	private final Choice choice;
	private final Component upDown;
	private final TextElement valueEl;
	
	
	public ChoiceRow(Choice choice, Component upDown, TextElement valueEl) {
		this.choice = choice;
		this.valueEl = valueEl;
		this.upDown = upDown;
	}
	
	public String getChoiceId() {
		return choice.getId();
	}
	
	public Choice getChoice() {
		return choice;
	}
	
	public TextElement getValueEl() {
		return valueEl;
	}

	public Component getUpDown() {
		return upDown;
	}
	

}
