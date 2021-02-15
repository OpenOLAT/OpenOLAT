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
package org.olat.core.gui.components.textboxlist;

/**
 * 
 * Initial date: 14 nov. 2019<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class TextBoxItemImpl implements TextBoxItem {
	
	private final String label;
	private final String value;
	private final String color;
	private final boolean editable;
	private final Object userObject;
	
	public TextBoxItemImpl(String label, String value) {
		this(label, value, null, true, null);
	}
	
	public TextBoxItemImpl(String label, String value, String color, boolean editable) {
		this(label, value, color, editable, null);
	}
	
	public TextBoxItemImpl(String label, String value, String color, boolean editable, Object userObject) {
		this.label = label;
		this.color = color;
		this.value = value;
		this.editable = editable;
		this.userObject = userObject;
	}

	@Override
	public String getValue() {
		return value;
	}

	@Override
	public String getLabel() {
		return label;
	}

	@Override
	public String getColor() {
		return color;
	}
	
	@Override
	public boolean isEditable() {
		return editable;
	}

	public Object getUserObject() {
		return userObject;
	}
}
