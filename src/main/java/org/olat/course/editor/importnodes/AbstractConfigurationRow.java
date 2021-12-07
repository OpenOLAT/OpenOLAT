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
package org.olat.course.editor.importnodes;

import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTreeTableNode;

/**
 * 
 * Initial date: 4 oct. 2021<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public abstract class AbstractConfigurationRow implements FlexiTreeTableNode {
	
	private final AbstractConfigurationRow parent;
	
	private String message;
	private boolean selected;

	protected AbstractConfigurationRow(AbstractConfigurationRow parent) {
		this.parent = parent;
	}

	@Override
	public AbstractConfigurationRow getParent() {
		return parent;
	}

	@Override
	public String getCrump() {
		return null;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public boolean isSelected() {
		return selected;
	}

	public void setSelected(boolean selected) {
		this.selected = selected;
	}
}
