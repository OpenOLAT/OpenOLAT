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
package org.olat.modules.selectus.ui.app_wizard;

import org.olat.core.gui.components.form.flexible.FormItem;

import org.olat.modules.selectus.model.ApplicationAttribute;
import org.olat.modules.selectus.model.PositionAttributeDefinition;

/**
 * 
 * Initial date: 10 sept. 2019<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class ApplicationAttributeWithDefinition {
	
	private final PositionAttributeDefinition definition;
	
	private ApplicationAttribute value;
	
	private FormItem primaryItem;
	private FormItem SecondaryItem;
	
	public ApplicationAttributeWithDefinition(PositionAttributeDefinition definition, ApplicationAttribute value) {
		this.definition = definition;
		this.value = value;
	}
	
	public PositionAttributeDefinition getDefinition() {
		return definition;
	}

	public ApplicationAttribute getValue() {
		return value;
	}

	public void setValue(ApplicationAttribute value) {
		this.value = value;
	}

	public FormItem getPrimaryItem() {
		return primaryItem;
	}

	public void setPrimaryItem(FormItem primaryItem) {
		this.primaryItem = primaryItem;
	}

	public FormItem getSecondaryItem() {
		return SecondaryItem;
	}

	public void setSecondaryItem(FormItem secondaryItem) {
		SecondaryItem = secondaryItem;
	}
}
