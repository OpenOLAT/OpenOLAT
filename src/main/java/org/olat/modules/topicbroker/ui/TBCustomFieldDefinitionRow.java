/**
 * <a href="https://www.openolat.org">
 * OpenOLAT - Online Learning and Training</a><br>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at the
 * <a href="https://www.apache.org/licenses/LICENSE-2.0">Apache homepage</a>
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
package org.olat.modules.topicbroker.ui;

import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.updown.UpDown;
import org.olat.modules.topicbroker.TBCustomFieldDefinition;
import org.olat.modules.topicbroker.TBCustomFieldDefinitionRef;
import org.olat.modules.topicbroker.TBCustomFieldType;

/**
 * 
 * Initial date: 26 Jun 2024<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
public class TBCustomFieldDefinitionRow implements TBCustomFieldDefinitionRef {
	
	private final Long key;
	private final String identifier;
	private final String name;
	private final TBCustomFieldType type;
	private String typeName;
	private final boolean displayInTable;
	private final int sortOrder;
	private UpDown upDown;
	private FormLink toolsLink;
	
	public TBCustomFieldDefinitionRow(TBCustomFieldDefinition definition) {
		this.key = definition.getKey();
		this.identifier = definition.getIdentifier();
		this.name = definition.getName();
		this.type = definition.getType();
		this.displayInTable = definition.isDisplayInTable();
		this.sortOrder = definition.getSortOrder();
	}
	
	@Override
	public Long getKey() {
		return key;
	}
	
	public String getIdentifier() {
		return identifier;
	}

	public String getName() {
		return name;
	}
	
	public TBCustomFieldType getType() {
		return type;
	}
	
	public String getTypeName() {
		return typeName;
	}

	public void setTypeName(String typeName) {
		this.typeName = typeName;
	}

	public boolean isDisplayInTable() {
		return displayInTable;
	}
	
	public int getSortOrder() {
		return sortOrder;
	}
	
	public UpDown getUpDown() {
		return upDown;
	}
	
	public void setUpDown(UpDown upDown) {
		this.upDown = upDown;
	}
	
	public FormLink getToolsLink() {
		return toolsLink;
	}
	
	public void setToolsLink(FormLink toolsLink) {
		this.toolsLink = toolsLink;
	}
}
